package com.flabs.cobrate.core.data

import android.content.Context
import android.util.Log
import com.flabs.cobrate.core.logic.LogSeguro
import com.flabs.cobrate.core.model.Cuenta
import com.flabs.cobrate.core.model.EstadoRelacion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

/**
 * Estados del flujo de sincronización remota de datos del usuario.
 */
enum class EstadoSincronizacionRemota {
    IDLE,
    SINCRONIZANDO,
    EXITO,
    ERROR
}

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

    // UID de la cuenta que ya ha completado su sincronización inicial exitosamente.
    private var ultimoUidSincronizado: String? = null

    // Control anticarreras de sincronización
    private var currentSyncGeneration = 0L

    private val _estadoSincronizacion = MutableStateFlow<EstadoSincronizacionRemota>(EstadoSincronizacionRemota.IDLE)
    val estadoSincronizacion: StateFlow<EstadoSincronizacionRemota> = _estadoSincronizacion.asStateFlow()

    // id -> hash de contraseña (LEGADO: ya no se usa para Supabase Auth).
    private val _hashes = mutableMapOf<String, String>()

    private var localStore: CuentaLocalStore? = null
    private var appContext: Context? = null

    private val relacionRepo by lazy { RelacionCuentaNegocioRepository() }
    private val negocioRepo by lazy { NegocioRepository() }
    private val cuentaRepo by lazy { CuentaRepository() }
    private val operacionRepo by lazy { OperacionRepository() }

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
        this.appContext = appContext
        if (localStore == null) {
            val store = CuentaLocalStore(appContext)
            localStore = store
            scope.launch {
                val dtos = store.leerUnaVez()
                _cuentas.value = dtos.map { it.aModelo() }
                dtos.forEach { _hashes[it.id] = it.hashPassword }

                val sesionSeguraStore = SesionSeguraStore(appContext)

                // Sincronizamos la sesión actual con Supabase Auth (CAPA 20).
                SupabaseAuthBridge.estadoSesion.collect { estado ->
                    LogSeguro.d("TokeSesion", "EstadoSesion recibido: ${estado::class.simpleName}")
                    when (estado) {
                        EstadoSesion.Inicializando -> {
                            LogSeguro.d("TokeSesion", "Inicializando sesión Supabase Auth...")
                        }
                        is EstadoSesion.Activa -> {
                            val userInfo = estado.usuario
                            val uid = userInfo.id
                            var cuenta = _cuentas.value.find { it.id == uid }

                            if (cuenta == null) {
                                val metaUsername = userInfo.userMetadata?.get("username")?.jsonPrimitive?.contentOrNull
                                    ?: userInfo.userMetadata?.get("full_name")?.jsonPrimitive?.contentOrNull
                                    ?: userInfo.userMetadata?.get("nombre")?.jsonPrimitive?.contentOrNull
                                    ?: userInfo.email?.substringBefore("@")
                                    ?: "Usuario anónimo"

                                cuenta = Cuenta(
                                    id = uid,
                                    username = metaUsername.ifBlank { "Usuario" },
                                    correoVerificado = userInfo.email,
                                    ultimoAcceso = System.currentTimeMillis(),
                                    fechaCreacion = System.currentTimeMillis()
                                )
                                crear(cuenta, "")
                            }
                            _sesionActual.value = cuenta
                            sesionSeguraStore.guardarSesionActiva(uid)
                            LogSeguro.d("TokeSesion", "Sesión Activa confirmada para UID ${uid.take(6)}. Guardado en SesionSeguraStore.")

                            if (uid == ultimoUidSincronizado && _estadoSincronizacion.value == EstadoSincronizacionRemota.EXITO) {
                                LogSeguro.d("CuentaStore", "[SYNC] SYNC SKIPPED (UID ${uid.take(6)} ya sincronizado con éxito)")
                                return@collect
                            }

                            LogSeguro.d("CuentaStore", "[SYNC] SYNC START (UID ${uid.take(6)})")

                            scope.launch {
                                cuentaRepo.upsert(cuenta)
                                    .onFailure {
                                        LogSeguro.e("CuentaStore", "Error al sincronizar cuenta en Supabase", it)
                                    }

                                sincronizarDatos(uid).onSuccess {
                                    ultimoUidSincronizado = uid
                                    LogSeguro.d("CuentaStore", "[SYNC] SYNC END EXITO (UID ${uid.take(6)})")
                                }.onFailure { err ->
                                    LogSeguro.w("CuentaStore", "[SYNC] SYNC END CON ERROR (UID ${uid.take(6)}). No se marcará como completado.", err)
                                }
                            }
                        }
                        EstadoSesion.CerradaPorUsuario, EstadoSesion.SinSesion -> {
                            LogSeguro.d("TokeSesion", "Sesión cerrada o sin sesión. Limpiando sesión local.")
                            _sesionActual.value = null
                            ultimoUidSincronizado = null
                            _estadoSincronizacion.value = EstadoSincronizacionRemota.IDLE
                            sesionSeguraStore.limpiarSesion()
                        }
                        is EstadoSesion.FalloDeRefresco -> {
                            if (estado.porRed) {
                                val uidGuardado = sesionSeguraStore.leerSesionActiva()
                                val cuentaLocal = if (uidGuardado != null) _cuentas.value.find { it.id == uidGuardado } else null
                                if (uidGuardado != null && cuentaLocal != null) {
                                    LogSeguro.d("TokeSesion", "Fallo de refresco por red. Estableciendo Sesión Provisional para UID ${uidGuardado.take(6)} sin sync.")
                                    _sesionActual.value = cuentaLocal
                                    ultimoUidSincronizado = null
                                } else {
                                    LogSeguro.d("TokeSesion", "Fallo de refresco por red sin cuenta local. Limpiando sesión.")
                                    _sesionActual.value = null
                                    ultimoUidSincronizado = null
                                    _estadoSincronizacion.value = EstadoSincronizacionRemota.IDLE
                                    sesionSeguraStore.limpiarSesion()
                                }
                            } else {
                                LogSeguro.d("TokeSesion", "Fallo de refresco técnico (no por red). Cerrando sesión.")
                                _sesionActual.value = null
                                ultimoUidSincronizado = null
                                _estadoSincronizacion.value = EstadoSincronizacionRemota.IDLE
                                sesionSeguraStore.limpiarSesion()
                                scope.launch {
                                    SupabaseAuthBridge.limpiarSesionLocal()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Sincroniza Relaciones y Negocios desde Supabase para la cuenta indicada de forma coordinada.
     * Devuelve Result.success(Unit) solo si todas las peticiones remotas necesarias completaron exitosamente.
     * Si falla, se conserva el estado local previo en caché.
     */
    suspend fun sincronizarDatos(cuentaId: String): Result<Unit> {
        val generation = synchronized(this) {
            ++currentSyncGeneration
        }
        _estadoSincronizacion.value = EstadoSincronizacionRemota.SINCRONIZANDO

        LogSeguro.d("CuentaStore", "[SYNC-START] Generación $generation para UID ${cuentaId.take(6)}")

        return try {
            // 1. Obtener relaciones de la cuenta
            val relacionesResult = relacionRepo.relacionesDe(cuentaId)
            if (relacionesResult.isFailure) {
                val err = relacionesResult.exceptionOrNull()
                LogSeguro.w("CuentaStore", "[SYNC-FAIL] Error consultando relaciones de ${cuentaId.take(6)}", err)
                if (generation == currentSyncGeneration) {
                    _estadoSincronizacion.value = EstadoSincronizacionRemota.ERROR
                }
                return Result.failure(err ?: Exception("Error consultando relaciones"))
            }

            val relaciones = relacionesResult.getOrThrow()

            // Validar si esta generación sigue siendo válida frente al UID activo
            if (cuentaId != _sesionActual.value?.id || generation != currentSyncGeneration) {
                LogSeguro.d("CuentaStore", "[SYNC-STALE] Generación $generation o UID obsoletos tras relaciones. Descartando respuesta.")
                return Result.failure(Exception("Sincronización obsoleta"))
            }

            // Sincronizar relaciones obtenidas con el store (preservando de otros UIDs)
            RelacionCuentaNegocioStore.sincronizar(cuentaId, relaciones)

            // 2. Obtener negocios propios y relacionados vía NegocioRepository
            val negociosResult = negocioRepo.obtenerNegociosDeCuenta(cuentaId)
            if (negociosResult.isFailure) {
                val err = negociosResult.exceptionOrNull()
                LogSeguro.w("CuentaStore", "[SYNC-FAIL] Error consultando negocios de ${cuentaId.take(6)}", err)
                if (generation == currentSyncGeneration) {
                    _estadoSincronizacion.value = EstadoSincronizacionRemota.ERROR
                }
                return Result.failure(err ?: Exception("Error consultando negocios"))
            }

            val negocios = negociosResult.getOrThrow()

            // Re-validar vigencia de generación
            if (cuentaId != _sesionActual.value?.id || generation != currentSyncGeneration) {
                LogSeguro.d("CuentaStore", "[SYNC-STALE] Generación $generation o UID obsoletos tras negocios. Descartando.")
                return Result.failure(Exception("Sincronización obsoleta"))
            }

            NegocioStore.sincronizar(negocios)

            // 3. Configurar escuchas de Realtime para negocios donde es dueño o tiene relación activa
            val negocioIdsOwned = negocios.filter { it.dueñoCuentaId == cuentaId }.map { it.id }
            val negocioIdsActiveRel = relaciones.filter { it.estado == EstadoRelacion.ACTIVA }.map { it.negocioId }
            val negociosParaEscuchar = (negocioIdsOwned + negocioIdsActiveRel).distinct()

            if (negociosParaEscuchar.isNotEmpty()) {
                appContext?.let { ctx ->
                    RealtimeSyncManager.configurarEscucha(ctx, negociosParaEscuchar)
                }
            }

            // Sincronizar cuentas de miembros y dueños para tener nombres siempre frescos
            val idsCuentasRelevantes = (negocios.map { it.dueñoCuentaId } + relaciones.map { it.cuentaId })
                .filter { it.isNotBlank() }
                .distinct()
            if (idsCuentasRelevantes.isNotEmpty()) {
                cuentaRepo.obtenerVariosPorId(idsCuentasRelevantes).onSuccess { listaCuentas ->
                    listaCuentas.forEach { upsert(it) }
                }
            }

            // 4. Sincronizar operaciones en bloque si existen negocios
            val todosNegocioIds = (negocios.map { it.id } + relaciones.map { it.negocioId }).distinct()
            if (todosNegocioIds.isNotEmpty()) {
                operacionRepo.obtenerDeVariosNegocios(todosNegocioIds).onSuccess { todasLasOps ->
                    if (cuentaId == _sesionActual.value?.id && generation == currentSyncGeneration) {
                        todosNegocioIds.forEach { negocioId ->
                            val opsDelNegocio = todasLasOps.filter { it.negocioId == negocioId }
                            InMemoryOperationStore.sincronizar(negocioId, opsDelNegocio)
                        }
                    }
                }.onFailure { err ->
                    LogSeguro.w("CuentaStore", "[SYNC-WARN] Fallo menor sincronizando operaciones", err)
                }
            }

            if (generation == currentSyncGeneration && cuentaId == _sesionActual.value?.id) {
                _estadoSincronizacion.value = EstadoSincronizacionRemota.EXITO
            }
            LogSeguro.d("CuentaStore", "[SYNC-SUCCESS] Generación $generation completada para ${cuentaId.take(6)}. Negocios: ${negocios.size}, Relaciones: ${relaciones.size}")
            Result.success(Unit)
        } catch (e: Exception) {
            LogSeguro.e("CuentaStore", "[SYNC-ERROR] Excepción durante sincronización de ${cuentaId.take(6)}", e)
            if (generation == currentSyncGeneration) {
                _estadoSincronizacion.value = EstadoSincronizacionRemota.ERROR
            }
            Result.failure(e)
        }
    }

    /**
     * Crea o actualiza una cuenta (si ya existe y cambiaron datos como el nombre) y la persiste.
     */
    fun crear(cuenta: Cuenta, hashPassword: String = "") {
        val existente = _cuentas.value.find { it.id == cuenta.id }
        if (existente != null) {
            if (existente.username != cuenta.username || existente.correoVerificado != cuenta.correoVerificado) {
                _cuentas.value = _cuentas.value.map { if (it.id == cuenta.id) cuenta else it }
                if (_sesionActual.value?.id == cuenta.id) {
                    _sesionActual.value = cuenta
                }
                persistir()
            }
            return
        }
        _cuentas.value = _cuentas.value + cuenta
        _hashes[cuenta.id] = hashPassword
        persistir()
    }

    /** Inserta o actualiza directamente una cuenta en memoria y persistencia local. */
    fun upsert(cuenta: Cuenta) {
        val existente = _cuentas.value.find { it.id == cuenta.id }
        if (existente != null) {
            _cuentas.value = _cuentas.value.map { if (it.id == cuenta.id) cuenta else it }
        } else {
            _cuentas.value = _cuentas.value + cuenta
        }
        if (_sesionActual.value?.id == cuenta.id) {
            _sesionActual.value = cuenta
        }
        persistir()
    }

    /**
     * Cambia el nombre de la cuenta activa: Supabase Auth, la tabla `cuentas` y lo guardado en el celular.
     * No pide verificación. Falla sin cambiar nada local si Supabase Auth no lo acepta.
     */
    suspend fun actualizarNombre(nombre: String): Result<Unit> {
        val actual = _sesionActual.value ?: return Result.failure(IllegalStateException("Sin sesión"))
        val limpio = nombre.trim()
        SupabaseAuthBridge.actualizarNombre(limpio).onFailure { return Result.failure(it) }
        val nueva = actual.copy(username = limpio)
        _cuentas.value = _cuentas.value.map { if (it.id == nueva.id) nueva else it }
        _sesionActual.value = nueva
        persistir()
        // La tabla `cuentas` se reintenta sola en el próximo inicio de sesión si esto falla.
        cuentaRepo.upsert(nueva).onFailure { LogSeguro.w("CuentaStore", "No se pudo guardar el nombre en cuentas", it) }
        return Result.success(Unit)
    }

    /** Busca una cuenta por su id. */
    fun buscarPorId(id: String): Cuenta? =
        _cuentas.value.find { it.id == id }

    /** Establece la sesión activa (usado para forzar actualización inmediata si es necesario). */
    fun iniciarSesion(cuenta: Cuenta) {
        _sesionActual.value = cuenta
    }

    /** Limpia la sesión activa y purga todos los datos en memoria de la cuenta anterior. */
    fun cerrarSesion() {
        _sesionActual.value = null
        ultimoUidSincronizado = null
        _cuentas.value = emptyList()

        appContext?.let { ctx ->
            SesionSeguraStore(ctx).limpiarSesion()
        }

        // Purgado atómico de todos los Stores con estado en memoria (CAPA 7-FIX)
        InMemoryOperationStore.limpiar()
        NegocioStore.limpiar()
        RelacionCuentaNegocioStore.limpiar()
        AlertaStore.limpiar()
        InvitacionStore.limpiar()
        MetodoDeCobroStore.limpiar()
        OperacionPendienteStore.limpiarMemoria()

        // Detener escuchas activas
        scope.launch {
            RealtimeSyncManager.detenerTodo()
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