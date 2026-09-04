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

    // id -> hash de contraseña (LEGADO: ya no se usa para Supabase Auth).
    private val _hashes = mutableMapOf<String, String>()

    private var localStore: CuentaLocalStore? = null

    private val relacionRepo = RelacionCuentaNegocioRepository()
    private val negocioRepo = NegocioRepository()
    private val cuentaRepo = CuentaRepository()

    /**
     * Inicializa el store con el contexto de la app y carga las cuentas
     * persistidas.
     *
     * DECISIÓN CAPA 7b-2: SesionSeguraStore ya no es la fuente de verdad para
     * la sesión activa. Ahora se sincroniza con SupabaseAuthBridge.sesionActual,
     * ya que el SDK de Supabase maneja su propia persistencia segura de tokens.
     */
    fun init(context: Context) {
        val appContext = context.applicationContext
        if (localStore == null) {
            val store = CuentaLocalStore(appContext)
            localStore = store
            scope.launch {
                val dtos = store.leerUnaVez()
                _cuentas.value = dtos.map { it.aModelo() }
                dtos.forEach { _hashes[it.id] = it.hashPassword }

                // Sincronizamos la sesión actual con Supabase.
                SupabaseAuthBridge.sesionActual.collect { userInfo ->
                    if (userInfo == null) {
                        _sesionActual.value = null
                    } else {
                        val uid = userInfo.id
                        var cuenta = _cuentas.value.find { it.id == uid }
                        
                        // Si la sesión existe en Supabase pero no tenemos la Cuenta local,
                        // la creamos bajo demanda (ej. login en nuevo dispositivo).
                        if (cuenta == null) {
                            cuenta = Cuenta(
                                id = uid,
                                username = userInfo.email ?: "Usuario anónimo",
                                correoVerificado = userInfo.email,
                                ultimoAcceso = System.currentTimeMillis(),
                                fechaCreacion = System.currentTimeMillis()
                            )
                            crear(cuenta, "")
                        }
                        _sesionActual.value = cuenta

                        // CAPA 7d-4: Sincronizamos la cuenta con la tabla public.cuentas
                        // antes de disparar la sincronización de datos.
                        scope.launch {
                            cuentaRepo.upsert(cuenta)
                                .onFailure {
                                    android.util.Log.e("CuentaStore", "Error al sincronizar cuenta en Supabase: ${it.message}")
                                }
                                .onSuccess {
                                    // Una vez asegurada la cuenta en Supabase, sincronizamos sus datos.
                                    sincronizarDatos(uid)
                                }
                        }
                    }
                }
            }
        }
    }

    /**
     * Sincroniza Relaciones y Negocios desde Supabase para la cuenta indicada.
     * Si falla, se conserva el estado local previo. CAPA 7d-2.
     */
    fun sincronizarDatos(cuentaId: String) {
        scope.launch {
            // Sincronizamos ambos en paralelo (o secuencial, pero sin bloquear el uno al otro)
            launch {
                relacionRepo.relacionesDe(cuentaId).onSuccess { relaciones ->
                    RelacionCuentaNegocioStore.sincronizar(cuentaId, relaciones)
                }.onFailure {
                    android.util.Log.e("CuentaStore", "Error sincronizando relaciones: ${it.message}")
                }
            }
            launch {
                negocioRepo.obtenerNegociosDeCuenta(cuentaId).onSuccess { negocios ->
                    NegocioStore.sincronizar(negocios)
                }.onFailure {
                    android.util.Log.e("CuentaStore", "Error sincronizando negocios: ${it.message}")
                }
            }
        }
    }

    /**
     * Crea (agrega) una cuenta nueva y la persiste.
     */
    fun crear(cuenta: Cuenta, hashPassword: String = "") {
        if (_cuentas.value.any { it.id == cuenta.id }) return
        _cuentas.value = _cuentas.value + cuenta
        _hashes[cuenta.id] = hashPassword
        persistir()
    }

    /** Busca una cuenta por su id. */
    fun buscarPorId(id: String): Cuenta? =
        _cuentas.value.find { it.id == id }

    /** Establece la sesión activa (usado para forzar actualización inmediata si es necesario). */
    fun iniciarSesion(cuenta: Cuenta) {
        _sesionActual.value = cuenta
    }

    /** Limpia la sesión activa. */
    fun cerrarSesion() {
        _sesionActual.value = null
    }

    private fun persistir() {
        localStore?.let { store ->
            val snapshot = _cuentas.value
            val hashes = _hashes.toMap()
            scope.launch { store.guardar(snapshot, hashes) }
        }
    }
}