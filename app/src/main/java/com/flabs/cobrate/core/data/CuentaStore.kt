package com.flabs.cobrate.core.data

import android.content.Context
import com.flabs.cobrate.core.model.Cuenta
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Fuente de cuentas compartida en memoria (singleton) con persistencia local.
 *
 * MISMO patrón que [InMemoryOperationStore]: puente entre flujos para que todos
 * lean/actualicen la MISMA lista de cuentas, y cada cambio se persiste en
 * [CuentaLocalStore] (DataStore).
 *
 * Además expone la sesión actual ([sesionActual]) y persiste el id de la última
 * sesión activa, de modo que al reabrir la app se pueda restaurar sin pedir
 * login de nuevo.
 *
 * El hash de contraseña NO vive en el modelo de dominio [Cuenta]; [CuentaStore]
 * lo conserva en un mapa interno (id -> hash) y lo delega al DTO al persistir.
 */
object CuentaStore {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _cuentas = MutableStateFlow<List<Cuenta>>(emptyList())
    val cuentas: StateFlow<List<Cuenta>> = _cuentas

    private val _sesionActual = MutableStateFlow<Cuenta?>(null)
    val sesionActual: StateFlow<Cuenta?> = _sesionActual

    // id -> hash de contraseña (protección mínima de desarrollo local).
    private val _hashes = mutableMapOf<String, String>()

    private var localStore: CuentaLocalStore? = null
    private var sesionSegura: SesionSeguraStore? = null

    /**
     * Inicializa el store con el contexto de la app, carga las cuentas
     * persistidas y restaura la última sesión activa si existía. Llamar varias
     * veces es seguro (solo inicializa una vez).
     *
     * La sesión activa se lee de [SesionSeguraStore] (almacenamiento cifrado).
     * Si aún no existe ahí pero sí en el DataStore viejo, se migra una sola vez.
     */
    fun init(context: Context) {
        val appContext = context.applicationContext
        if (localStore == null) {
            val store = CuentaLocalStore(appContext)
            localStore = store
            val segura = SesionSeguraStore(appContext)
            sesionSegura = segura
            scope.launch {
                val dtos = store.leerUnaVez()
                _cuentas.value = dtos.map { it.aModelo() }
                dtos.forEach { _hashes[it.id] = it.hashPassword }

                var sesionId = segura.leerSesionActiva()
                if (sesionId == null) {
                    // Migración única: si había una sesión activa en el DataStore
                    // viejo, muévela al almacenamiento cifrado y elimínala de ahí.
                    val sesionVieja = store.leerSesionActivaViejaYLimpiar()
                    if (sesionVieja != null) {
                        segura.guardarSesionActiva(sesionVieja)
                        sesionId = sesionVieja
                    }
                }
                if (sesionId != null) {
                    _sesionActual.value = _cuentas.value.find { it.id == sesionId }
                }
            }
        }
    }

    /**
     * Crea (agrega) una cuenta nueva y la persiste, junto con su hash de
     * contraseña. No valida unicidad: eso lo hace AuthViewModel antes.
     */
    fun crear(cuenta: Cuenta, hashPassword: String) {
        _cuentas.value = _cuentas.value + cuenta
        _hashes[cuenta.id] = hashPassword
        persistir()
    }

    /** Busca una cuenta por su username (case-sensitive). Null si no existe. */
    fun buscarPorUsername(username: String): Cuenta? =
        _cuentas.value.find { it.username == username }

    /** Hash de contraseña de una cuenta por su id. Null si no existe. */
    fun hashDe(cuentaId: String): String? = _hashes[cuentaId]

    /** Establece la sesión activa y la persiste (cifrada) como última sesión. */
    fun iniciarSesion(cuenta: Cuenta) {
        _sesionActual.value = cuenta
        sesionSegura?.let { segura ->
            runCatching { segura.guardarSesionActiva(cuenta.id) }
        }
    }

    /** Limpia la sesión activa (y su registro persistido cifrado). */
    fun cerrarSesion() {
        _sesionActual.value = null
        sesionSegura?.let { segura ->
            runCatching { segura.limpiarSesion() }
        }
    }

    private fun persistir() {
        localStore?.let { store ->
            val snapshot = _cuentas.value
            val hashes = _hashes.toMap()
            scope.launch { store.guardar(snapshot, hashes) }
        }
    }
}