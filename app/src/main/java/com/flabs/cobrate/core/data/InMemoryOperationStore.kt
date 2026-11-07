package com.flabs.cobrate.core.data

import android.content.Context
import android.util.Log
import com.flabs.cobrate.core.logic.EstadoObservabilidadEnvio
import com.flabs.cobrate.core.logic.NetworkMonitor
import com.flabs.cobrate.core.model.Operacion
import com.flabs.cobrate.core.model.EstadoOperacion
import com.flabs.cobrate.core.model.OrigenOperacion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * Fuente de operaciones compartida en memoria (singleton) con persistencia local.
 *
 * Es un puente entre el flujo del trabajador y el del dueño para que ambos lean
 * y actualicen la MISMA lista. Además, cada cambio se persiste en
 * [OperationLocalStore] (DataStore) para que las operaciones sobrevivan a
 * cerrar y reabrir la app.
 */
object InMemoryOperationStore {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _operaciones = MutableStateFlow<List<Operacion>>(emptyList())
    val operaciones: StateFlow<List<Operacion>> = _operaciones

    private var currentSessionUid: String? = null

    // Mecanismos de Concurrencia por Negocio y Epoch de Sesión
    private val generacionesPorNegocio = ConcurrentHashMap<String, AtomicLong>()
    private val mutexReconciliacionPorNegocio = ConcurrentHashMap<String, Mutex>()
    private val epochSesion = AtomicLong(0L)

    private val idsEnVuelo = Collections.synchronizedSet(mutableSetOf<String>())

    private var localStore: OperationLocalStore? = null
    private val repository = OperacionRepository()
    private var appContext: Context? = null

    fun init(context: Context) {
        val ctx = context.applicationContext
        appContext = ctx
        if (localStore == null) {
            localStore = OperationLocalStore(ctx)
            
            scope.launch {
                CuentaStore.sesionActual.collect { cuenta ->
                    val newUid = cuenta?.id
                    if (newUid != currentSessionUid) {
                        currentSessionUid = newUid
                        if (newUid != null) {
                            cargarDeDisco(newUid)
                        } else {
                            _operaciones.value = emptyList()
                        }
                    }
                }
            }
        }
    }

    private suspend fun cargarDeDisco(uid: String) {
        val guardadas = localStore?.leerUnaVez(uid) ?: emptyList()
        _operaciones.value = guardadas
    }

    /** Invalida explícitamente la generación de un negocio bajo su mutex. */
    suspend fun invalidarReconciliacion(negocioId: String) {
        val mutex = mutexReconciliacionPorNegocio.computeIfAbsent(negocioId) { Mutex() }
        mutex.withLock {
            generacionesPorNegocio.computeIfAbsent(negocioId) { AtomicLong(0L) }.incrementAndGet()
        }
    }

    /** Aplica un cambio remoto de Realtime de forma atómica incrementando la generación del negocio. */
    suspend fun aplicarCambioRemotoRealtime(operacion: Operacion) {
        val mutex = mutexReconciliacionPorNegocio.computeIfAbsent(operacion.negocioId) { Mutex() }
        mutex.withLock {
            generacionesPorNegocio.computeIfAbsent(operacion.negocioId) { AtomicLong(0L) }.incrementAndGet()
            val actuales = _operaciones.value
            val index = actuales.indexOfFirst { it.id == operacion.id }
            val nuevas = if (index >= 0) {
                actuales.map { if (it.id == operacion.id) operacion else it }
            } else {
                actuales + operacion
            }
            _operaciones.value = nuevas
            persistir()
        }
    }

    /** Elimina de la memoria local un registro al recibir un DELETE de Realtime. */
    suspend fun eliminarPorIdRemoto(operacionId: String, negocioId: String) {
        val mutex = mutexReconciliacionPorNegocio.computeIfAbsent(negocioId) { Mutex() }
        mutex.withLock {
            generacionesPorNegocio.computeIfAbsent(negocioId) { AtomicLong(0L) }.incrementAndGet()
            val actuales = _operaciones.value
            if (actuales.any { it.id == operacionId }) {
                _operaciones.value = actuales.filterNot { it.id == operacionId }
                persistir()
            }
        }
    }

    suspend fun agregar(operacion: Operacion): Result<Unit> {
        idsEnVuelo.add(operacion.id)
        invalidarReconciliacion(operacion.negocioId)
        try {
            val result = repository.crear(operacion)
            if (result.isSuccess) {
                val creada = result.getOrThrow()
                aplicarCambioRemotoRealtime(creada)
                return Result.success(Unit)
            }
            
            val error = result.exceptionOrNull()

            if (com.flabs.cobrate.features.planes.PlanLogica.esCuotaAgotada(error?.message)) {
                // Cuota diaria del plan Free agotada: no se guarda para reintentar, el servidor no la va a aceptar hoy.
                Log.w("InMemoryOperationStore", "Cobro rechazado: cuota diaria agotada")
                com.flabs.cobrate.features.planes.PlanStore.marcarCuotaAgotada()
                return Result.failure(error!!)
            }

            if (error is SecurityException) {
                Log.w("InMemoryOperationStore", "Operación rechazada por permisos: ${error.message}")
                return Result.failure(error)
            }

            val sinRed = !NetworkMonitor.disponible.value
            val estadoSinc = if (sinRed) EstadoSincronizacion.ESPERANDO_RED else EstadoSincronizacion.ERROR

            appContext?.let { ctx ->
                val ownerId = CuentaStore.sesionActual.value?.id
                OperacionPendienteStore.agregar(ctx, operacion, ownerId, estadoSinc)
                if (_operaciones.value.none { it.id == operacion.id }) {
                    _operaciones.value = _operaciones.value + operacion
                    persistir()
                }
            }

            if (sinRed) {
                return Result.success(Unit)
            }
            
            return Result.failure(error ?: Exception("Error al crear operación en Supabase"))
        } finally {
            idsEnVuelo.remove(operacion.id)
        }
    }



    fun sincronizar(negocioIdSincronizado: String, nuevasOperaciones: List<Operacion>) {
        val operacionLocales = _operaciones.value
        val idsOffline = OperacionPendienteStore.operacionesPendientes.value.map { it.id }.toSet()

        val deOtrosNegocios = operacionLocales.filter { it.negocioId != negocioIdSincronizado }

        val resultadoMerge = nuevasOperaciones.map { remota ->
            val local = operacionLocales.find { it.id == remota.id }
            
            when {
                remota.id in idsOffline -> local ?: remota
                remota.id in idsEnVuelo -> local ?: remota
                local != null && local.estado.ordinal > remota.estado.ordinal -> local
                else -> remota
            }
        }

        val idsRemotos = nuevasOperaciones.map { it.id }.toSet()
        val soloLocalesDelNegocio = operacionLocales.filter { 
            it.negocioId == negocioIdSincronizado && it.id !in idsRemotos 
        }

        _operaciones.value = deOtrosNegocios + resultadoMerge + soloLocalesDelNegocio
        scope.launch { persistir() }
    }

    /**
     * Refresca una operación desde el servidor delegando en aplicarCambioRemotoRealtime si la encuentra.
     */
    suspend fun refrescarUna(operacionId: String) {
        repository.obtenerPorId(operacionId).onSuccess { actualizada ->
            if (actualizada != null) {
                aplicarCambioRemotoRealtime(actualizada)
            }
            // En caso de success(null) o failure, CONSERVA la copia local.
        }
    }

    /**
     * Reconcilia el subconjunto Mirror del negocio activo de forma atómica e idempotente.
     */
    internal suspend fun reconciliarMirror(
        negocioId: String,
        repositoryOverride: OperacionRepositoryContract? = null
    ) {
        val repo = repositoryOverride ?: repository
        val sessionUidInicio = CuentaStore.sesionActual.value?.id ?: return
        val epochInicio = epochSesion.get()

        val generacionInicio = generacionesPorNegocio.computeIfAbsent(negocioId) { AtomicLong(0L) }.incrementAndGet()

        val result = repo.obtenerDelNegocio(negocioId)
        if (result.isFailure) {
            Log.w("InMemoryOperationStore", "Fallo al reconciliar negocio $negocioId. Conservando caché previa.")
            return
        }

        val remotas = result.getOrNull() ?: return
        
        // Filtrar remotas pertenecientes al contexto Mirror
        val remotasMirror = remotas.filter {
            it.negocioId == negocioId && (it.estado == EstadoOperacion.SIN_ASIGNAR || it.trabajadorAsignadoId == sessionUidInicio)
        }

        val candidateIdsParaVerificar = _operaciones.value.filter { op ->
            op.negocioId == negocioId &&
                    op.id !in idsEnVuelo &&
                    op.id !in OperacionPendienteStore.operacionesPendientes.value.map { it.id } &&
                    op.origen is OrigenOperacion.Detectada &&
                    (op.estado == EstadoOperacion.SIN_ASIGNAR || op.trabajadorAsignadoId == sessionUidInicio) &&
                    remotasMirror.none { it.id == op.id }
        }.map { it.id }

        val idsParaEliminar = mutableSetOf<String>()
        val operacionesParaActualizar = mutableListOf<Operacion>()

        for (candId in candidateIdsParaVerificar) {
            val singleResult = repo.obtenerPorId(candId)
            singleResult.onSuccess { remoteOp ->
                if (remoteOp == null) {
                    idsParaEliminar.add(candId)
                } else if (remoteOp.negocioId == negocioId && (remoteOp.estado == EstadoOperacion.SIN_ASIGNAR || remoteOp.trabajadorAsignadoId == sessionUidInicio)) {
                    operacionesParaActualizar.add(remoteOp)
                } else {
                    idsParaEliminar.add(candId)
                }
            }
        }

        val mutex = mutexReconciliacionPorNegocio.computeIfAbsent(negocioId) { Mutex() }
        mutex.withLock {
            val generacionActual = generacionesPorNegocio[negocioId]?.get()
            val currentUid = CuentaStore.sesionActual.value?.id
            val currentEpoch = epochSesion.get()

            if (generacionInicio != generacionActual || epochInicio != currentEpoch || sessionUidInicio != currentUid || currentUid == null) {
                Log.d("InMemoryOperationStore", "Reconciliación descartada por mutación concurrente o cambio de sesión ($negocioId)")
                return@withLock
            }

            var listaFinal = _operaciones.value

            remotasMirror.forEach { op ->
                val idx = listaFinal.indexOfFirst { it.id == op.id }
                listaFinal = if (idx >= 0) {
                    listaFinal.map { if (it.id == op.id) op else it }
                } else {
                    listaFinal + op
                }
            }

            operacionesParaActualizar.forEach { op ->
                val idx = listaFinal.indexOfFirst { it.id == op.id }
                listaFinal = if (idx >= 0) {
                    listaFinal.map { if (it.id == op.id) op else it }
                } else {
                    listaFinal + op
                }
            }

            if (idsParaEliminar.isNotEmpty()) {
                listaFinal = listaFinal.filterNot { it.id in idsParaEliminar }
            }

            _operaciones.value = listaFinal
            persistir()
        }
    }

    fun limpiar() {
        epochSesion.incrementAndGet()
        generacionesPorNegocio.clear()
        _operaciones.value = emptyList()
        currentSessionUid = null
    }

    private suspend fun persistir() {
        val uid = currentSessionUid ?: return
        val store = localStore ?: return
        val snapshot = _operaciones.value
        store.guardar(uid, snapshot)
    }
}
