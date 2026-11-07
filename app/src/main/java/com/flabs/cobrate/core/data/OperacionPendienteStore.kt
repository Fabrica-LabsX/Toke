package com.flabs.cobrate.core.data

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.flabs.cobrate.core.data.dto.CambioDeEstadoLocalDto
import com.flabs.cobrate.core.data.dto.OperacionLocalDto
import com.flabs.cobrate.core.logic.AndroidNotifier
import com.flabs.cobrate.core.logic.EstadoObservabilidadEnvio
import com.flabs.cobrate.core.logic.LogSeguro
import com.flabs.cobrate.core.logic.NetworkMonitor
import com.flabs.cobrate.core.model.CambioDeEstado
import com.flabs.cobrate.core.model.EstadoOperacion
import com.flabs.cobrate.core.model.EstadoRelacion
import com.flabs.cobrate.core.model.Operacion
import com.flabs.cobrate.core.model.OrigenOperacion
import com.flabs.cobrate.core.model.RolCuentaNegocio
import com.flabs.cobrate.core.model.TipoMetodoCobro
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Estado local de sincronización remota para operaciones pendientes de transmisión.
 */
enum class EstadoSincronizacion {
    ESPERANDO_RED,
    ERROR
}

/**
 * Representa un elemento en la cola offline/reintento.
 */
data class ElementoPendiente(
    val operacion: Operacion,
    val ownerId: String?,
    val estadoSincronizacion: EstadoSincronizacion = EstadoSincronizacion.ESPERANDO_RED
)

/**
 * Cola de operaciones pendientes que fallaron al enviarse por falta de red o error de servidor.
 * CAPA TAREA E-2: Cola Offline + Retry + Observabilidad
 */
object OperacionPendienteStore {
    
    private const val TAG = "OperacionPendienteStore"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    private val mutexProcesamiento = Mutex()
    private var currentSessionUid: String? = null
    private var appContext: Context? = null
    
    // Memoria operativa: sostiene ÚNICAMENTE las operaciones del UID activo.
    private val _todasLasPendientes = MutableStateFlow<List<ElementoPendiente>>(emptyList())
    
    // Vista filtrada para la UI (solo el usuario actual)
    private val _operacionesPendientes = MutableStateFlow<List<Operacion>>(emptyList())
    val operacionesPendientes: StateFlow<List<Operacion>> = _operacionesPendientes.asStateFlow()

    private val Context.dataStore by preferencesDataStore(name = "operaciones_pendientes")
    private val pendientesKey = stringPreferencesKey("lista_pendientes")
    
    private val json = Json { ignoreUnknownKeys = true }
    private val repository = OperacionRepository()

    /**
     * Inicializa el store y gestiona la transición de sesión y monitoreo de red mediante un único colector.
     */
    fun init(context: Context) {
        val ctx = context.applicationContext
        appContext = ctx
        
        // 1. Único colector de transición de sesión con protección contra carreras A -> B
        scope.launch {
            CuentaStore.sesionActual.collect { cuenta ->
                val newUid = cuenta?.id
                if (newUid != currentSessionUid) {
                    currentSessionUid = newUid
                    _todasLasPendientes.value = emptyList()
                    _operacionesPendientes.value = emptyList()
                    
                    if (newUid != null) {
                        val guardadas = leerDeDisco(ctx).filter { it.ownerId == newUid }
                        // Guarda contra race condition A -> B
                        if (currentSessionUid == newUid) {
                            _todasLasPendientes.value = guardadas
                            Log.d(TAG, "Rehidratada cola en memoria únicamente para UID $newUid (${guardadas.size} elementos de este usuario)")
                        }
                    }
                }
            }
        }

        // 2. Colector de red
        scope.launch {
            NetworkMonitor.disponible.collect { disponible ->
                val uid = currentSessionUid
                if (disponible && uid != null && EstadoOperativoStore.encendido.value) {
                    if (_todasLasPendientes.value.any { it.ownerId == uid }) {
                        Log.d(TAG, "Red disponible. Procesando cola pendiente para UID $uid...")
                        procesarCola(ctx)
                    }
                }
            }
        }

        // 3. Sincronizar vista filtrada de la UI
        scope.launch {
            _todasLasPendientes.collect { todas ->
                val uid = currentSessionUid
                _operacionesPendientes.value = todas.filter { it.ownerId == uid }.map { it.operacion }
            }
        }
    }

    /** Agrega una operación a la cola local con su propietario y estado de sincronización. */
    suspend fun agregar(
        context: Context,
        operacion: Operacion,
        ownerId: String?,
        estadoSincronizacion: EstadoSincronizacion = EstadoSincronizacion.ESPERANDO_RED
    ) {
        val activeUid = CuentaStore.sesionActual.value?.id
        if (ownerId == null || ownerId != activeUid || activeUid != currentSessionUid) {
            Log.w(TAG, "Rechazando agregar a la cola offline: ownerId $ownerId no coincide con la sesión activa $activeUid")
            return
        }

        val actual = _todasLasPendientes.value.toMutableList()
        if (actual.any { it.operacion.id == operacion.id }) return // Evitar duplicados
        
        actual.add(ElementoPendiente(operacion, ownerId, estadoSincronizacion))
        _todasLasPendientes.value = actual
        guardarEnDisco(context, actual)
        
        val estadoEnvio = if (estadoSincronizacion == EstadoSincronizacion.ERROR) {
            EstadoObservabilidadEnvio.ERROR
        } else {
            EstadoObservabilidadEnvio.ESPERANDO_RED
        }
        notificarSiCorresponde(context, operacion, estadoEnvio)
        Log.d(TAG, "Operación ${operacion.id} agregada a la cola ($estadoSincronizacion, Owner: $ownerId)")
    }

    /**
     * API Pública Controlada: Reintenta manualmente el envío de UNA operación específica.
     */
    suspend fun reintentarUna(context: Context, operacionId: String): Result<Unit> {
        val currentUid = CuentaStore.sesionActual.value?.id
            ?: return Result.failure(Exception("Sin sesión activa"))

        val elemento = _todasLasPendientes.value.find { it.operacion.id == operacionId }
            ?: return Result.failure(Exception("Operación no encontrada en cola"))

        if (elemento.ownerId != currentUid) {
            return Result.failure(Exception("No autorizado"))
        }

        val operacion = elemento.operacion
        val result = repository.crear(operacion)

        if (result.isSuccess) {
            Log.d(TAG, "Reintento manual exitoso para ${operacion.id}. Eliminando de cola.")
            eliminarDeCola(context, operacion.id)
            notificarSiCorresponde(context, operacion, EstadoObservabilidadEnvio.ENVIADO)
            return Result.success(Unit)
        } else {
            if (result.exceptionOrNull() is SecurityException) {
                Log.w(TAG, "Reintento manual no autorizado para ${operacion.id}. Eliminando de cola.")
                eliminarDeCola(context, operacion.id)
                return Result.failure(result.exceptionOrNull()!!)
            }

            val sinRed = !NetworkMonitor.disponible.value
            val nuevoEstadoSinc = if (sinRed) EstadoSincronizacion.ESPERANDO_RED else EstadoSincronizacion.ERROR
            actualizarEstadoSincronizacion(context, operacionId, nuevoEstadoSinc)

            val estadoEnvio = if (sinRed) EstadoObservabilidadEnvio.ESPERANDO_RED else EstadoObservabilidadEnvio.ERROR
            notificarSiCorresponde(context, operacion, estadoEnvio)

            LogSeguro.e(TAG, "Reintento manual fallido para ${operacion.id}", result.exceptionOrNull())
            return Result.failure(result.exceptionOrNull() ?: Exception("Error en envío"))
        }
    }

    /** Procesa las operaciones pendientes del usuario activo. */
    private suspend fun procesarCola(context: Context) {
        if (!EstadoOperativoStore.encendido.value) {
            Log.d(TAG, "Procesamiento de cola omitido porque TOKE está apagado")
            return
        }
        if (mutexProcesamiento.isLocked) return
        
        val currentUid = CuentaStore.sesionActual.value?.id ?: return

        mutexProcesamiento.withLock {
            val pendientes = _todasLasPendientes.value.toList()
            if (pendientes.isEmpty()) return@withLock
            
            for (elem in pendientes) {
                if (elem.ownerId != currentUid) continue

                val operacion = elem.operacion
                val result = repository.crear(operacion)

                if (result.isSuccess) {
                    Log.d(TAG, "Reintento de cola exitoso para ${operacion.id}.")
                    eliminarDeCola(context, operacion.id)
                    notificarSiCorresponde(context, operacion, EstadoObservabilidadEnvio.ENVIADO)
                } else if (result.exceptionOrNull() is SecurityException) {
                    Log.w(TAG, "Operación ${operacion.id} rechazada por permisos. Eliminando de cola.")
                    eliminarDeCola(context, operacion.id)
                } else {
                    val sinRed = !NetworkMonitor.disponible.value
                    val nuevoEstadoSinc = if (sinRed) EstadoSincronizacion.ESPERANDO_RED else EstadoSincronizacion.ERROR
                    actualizarEstadoSincronizacion(context, operacion.id, nuevoEstadoSinc)

                    val estadoEnvio = if (sinRed) EstadoObservabilidadEnvio.ESPERANDO_RED else EstadoObservabilidadEnvio.ERROR
                    notificarSiCorresponde(context, operacion, estadoEnvio)
                }
            }
        }
    }

    private suspend fun actualizarEstadoSincronizacion(context: Context, operacionId: String, nuevoEstado: EstadoSincronizacion) {
        val actual = _todasLasPendientes.value.map {
            if (it.operacion.id == operacionId) it.copy(estadoSincronizacion = nuevoEstado) else it
        }
        _todasLasPendientes.value = actual
        guardarEnDisco(context, actual)
    }

    /** Elimina una operación de la cola local tras enviarse con éxito, preservando otros UIDs en disco. */
    suspend fun eliminarDeCola(context: Context, operacionId: String) {
        val actualMemoria = _todasLasPendientes.value.filterNot { it.operacion.id == operacionId }
        _todasLasPendientes.value = actualMemoria

        context.dataStore.edit { prefs ->
            val rawExistente = prefs[pendientesKey]
            if (rawExistente != null) {
                val todosEnDisco = runCatching {
                    json.decodeFromString<List<OperacionLocalDto>>(rawExistente).mapNotNull { dto ->
                        dto.aModeloConElementoPendiente()
                    }
                }.getOrElse { emptyList() }

                val filtradosDisco = todosEnDisco.filterNot { it.operacion.id == operacionId }
                prefs[pendientesKey] = json.encodeToString(filtradosDisco.map { it.operacion.aDto(it.ownerId, it.estadoSincronizacion) })
            }
        }
    }

    /**
     * Valida que la cuenta actual sea Dueño o ADMINISTRADOR en estado ACTIVA del negocio activo y
     * tenga activado el aviso sin conexión (avisarCobroEnviado) antes de avisar de un cobro que espera red o falló.
     */
    fun notificarSiCorresponde(context: Context, operacion: Operacion, estadoEnvio: EstadoObservabilidadEnvio) {
        // Ya no se avisa de cobros enviados: solo se retira el aviso de "esperando red" si había uno.
        if (estadoEnvio == EstadoObservabilidadEnvio.ENVIADO) {
            AndroidNotifier.cancelarObservabilidadEnvio(context, operacion.id)
            return
        }
        val cuentaId = CuentaStore.sesionActual.value?.id ?: return
        val negocioIdActivo = NegocioStore.negocioActivoId.value ?: return

        if (negocioIdActivo != operacion.negocioId) return

        val negocio = NegocioStore.buscarPorId(negocioIdActivo)
        val esDuenoDirecto = negocio?.dueñoCuentaId == cuentaId

        val relacion = RelacionCuentaNegocioStore.relaciones.value.find {
            it.cuentaId == cuentaId && it.negocioId == negocioIdActivo
        }
        val esAdminActivo = relacion?.rol == RolCuentaNegocio.ADMINISTRADOR && relacion.estado == EstadoRelacion.ACTIVA

        if (!esDuenoDirecto && !esAdminActivo) {
            return
        }

        if (!PreferenciasAppStore.avisarCobroEnviado.value) {
            return
        }

        AndroidNotifier.mostrarObservabilidadEnvio(
            context = context,
            operacionId = operacion.id,
            monto = operacion.monto,
            metodoNombre = operacion.metodoPago?.name,
            estado = estadoEnvio
        )
    }

    /** Limpia memoria local en logout sin destruir el archivo DataStore en disco. */
    fun limpiarMemoria() {
        _todasLasPendientes.value = emptyList()
        _operacionesPendientes.value = emptyList()
        currentSessionUid = null
    }

    /** Borra la cola de memoria en logout sin destruir el disco. */
    suspend fun clear(context: Context? = null) {
        limpiarMemoria()
    }

    /** Limpia todo el disco en purga explícita. */
    suspend fun purgarDisco(context: Context) {
        limpiarMemoria()
        context.dataStore.edit { it.remove(pendientesKey) }
    }

    // ---- Persistencia ----

    private suspend fun leerDeDisco(context: Context): List<ElementoPendiente> =
        context.dataStore.data.map { prefs ->
            prefs[pendientesKey]?.let { raw ->
                runCatching {
                    json.decodeFromString<List<OperacionLocalDto>>(raw).mapNotNull { dto ->
                        dto.aModeloConElementoPendiente()
                    }
                }.getOrElse { emptyList() }
            } ?: emptyList()
        }.first()

    /** Escritura atómica dentro de una sola transacción dataStore.edit preservando otros UIDs. */
    private suspend fun guardarEnDisco(context: Context, listaItemsDelUidActivo: List<ElementoPendiente>) {
        val uid = currentSessionUid ?: return
        context.dataStore.edit { prefs ->
            val rawExistente = prefs[pendientesKey]
            val todosEnDisco = if (rawExistente != null) {
                runCatching {
                    json.decodeFromString<List<OperacionLocalDto>>(rawExistente).mapNotNull { dto ->
                        dto.aModeloConElementoPendiente()
                    }
                }.getOrElse { emptyList() }
            } else {
                emptyList()
            }

            // Preservar elementos de otros UIDs + guardar los actualizados de este UID
            val deOtrosUids = todosEnDisco.filter { it.ownerId != uid }
            val combinados = deOtrosUids + listaItemsDelUidActivo

            prefs[pendientesKey] = json.encodeToString(combinados.map { it.operacion.aDto(it.ownerId, it.estadoSincronizacion) })
        }
    }
}

// ---- Funciones de Mapeo (Domain <-> DTO) ----

private fun Operacion.aDto(ownerId: String?, estadoSincronizacion: EstadoSincronizacion) = OperacionLocalDto(
    id = id,
    negocioId = negocioId,
    trabajadorAsignadoId = trabajadorAsignadoId,
    metodoPago = metodoPago,
    monto = monto,
    estado = estado,
    origen = origen.aTag(),
    fechaHora = fechaHora,
    historial = historial.map { it.aDto() },
    ownerId = ownerId,
    estadoSincronizacion = estadoSincronizacion.name
)

private fun OrigenOperacion.aTag(): String = when (this) {
    is OrigenOperacion.Detectada -> "Detectada:$eventoId"
}

private fun CambioDeEstado.aDto() = CambioDeEstadoLocalDto(timestamp = timestamp, estado = estado)

private fun OperacionLocalDto.aModeloConElementoPendiente(): ElementoPendiente? {
    val origenParsed = origen.aOrigen()
    if (origenParsed == null) {
        Log.w("OperacionPendienteStore", "Descartando operación legacy incompatible con origen '$origen' (ID: $id)")
        return null
    }
    val op = Operacion(
        id = id,
        negocioId = negocioId,
        trabajadorAsignadoId = trabajadorAsignadoId,
        metodoPago = metodoPago,
        monto = monto,
        estado = estado,
        origen = origenParsed,
        fechaHora = fechaHora,
        historial = historial.map { it.aModelo() }
    )
    val estadoSinc = runCatching { EstadoSincronizacion.valueOf(estadoSincronizacion) }
        .getOrDefault(EstadoSincronizacion.ESPERANDO_RED)
    return ElementoPendiente(op, ownerId, estadoSinc)
}

private fun String.aOrigen(): OrigenOperacion? {
    if (startsWith("Detectada:")) {
        val eventoId = substringAfter("Detectada:")
        if (eventoId.isNotEmpty()) {
            return OrigenOperacion.Detectada(eventoId = eventoId)
        }
    }
    return null
}

private fun CambioDeEstadoLocalDto.aModelo() = CambioDeEstado(timestamp = timestamp, estado = estado)
