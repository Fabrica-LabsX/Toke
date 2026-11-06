package com.flabs.cobrate.core.data

import android.content.Context
import android.util.Log
import com.flabs.cobrate.core.logic.LogSeguro
import com.flabs.cobrate.core.logic.NetworkMonitor
import com.flabs.cobrate.core.logic.OperationNotifier
import com.flabs.cobrate.core.model.EstadoOperacion
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonPrimitive

/**
 * Resultado del cálculo del diferencial de canales para sincronizar escuchas Realtime.
 */
internal data class DiffResult(
    val aIniciar: Set<String>,
    val aDetener: Set<String>
)

/**
 * Función pura de producción para calcular qué canales iniciar y cuáles detener.
 */
internal object RealtimeChannelDiff {
    fun calcularDiferencial(canalesActivos: Set<String>, negocioIdsTarget: Set<String>): DiffResult {
        val target = negocioIdsTarget.toSet()
        val activos = canalesActivos.toSet()
        return DiffResult(
            aIniciar = target - activos,
            aDetener = activos - target
        )
    }
}

/**
 * Orquestador de la sincronización en tiempo real de operaciones.
 * CAPA-REALTIME paso 2.
 *
 * Escucha los eventos de [OperacionesRealtimeListener] y actualiza la caché
 * local ([InMemoryOperationStore]). Si el cambio proviene de otro dispositivo,
 * dispara las notificaciones y voz correspondientes.
 */
internal object RealtimeSyncManager {
    private const val TAG = "RealtimeSyncManager"
    private var job = SupervisorJob()
    private var scope = CoroutineScope(job + Dispatchers.IO)
    private var initialized = false

    // Canales de relaciones administrados por RealtimeSyncManager
    private val managedChannels = mutableMapOf<String, RealtimeChannel>()
    
    // CAPA 6-FIX: Aislamiento de sesión para evitar contaminación A -> B.
    private var activeSessionUid: String? = null

    /**
     * Inicia la escucha de eventos de Realtime para los negocios indicados sincronizando exactamente los canales.
     */
    fun configurarEscucha(context: Context, negocioIds: List<String>) {
        val currentUid = CuentaStore.sesionActual.value?.id
        if (currentUid == null) {
            Log.w(TAG, "No se puede configurar Realtime: no hay sesión activa")
            return
        }

        // Si la sesión cambió sin pasar por detenerTodo (raro), reiniciamos.
        if (activeSessionUid != null && activeSessionUid != currentUid) {
            Log.d(TAG, "Cambio de sesión detectado en manager. Reiniciando listeners...")
            scope.launch { detenerTodo() }
        }
        
        activeSessionUid = currentUid

        if (!initialized) {
            initialized = true
            // Monitorear conectividad para pausar/reanudar Realtime websocket según disponibilidad de red
            scope.launch {
                var redPrevia: android.net.Network? = null
                NetworkMonitor.redActiva.collect { red ->
                    runCatching {
                        val supabase = SupabaseClientProvider.supabase
                        if (red == null) {
                            if (redPrevia != null) {
                                Log.d(TAG, "Sin red validada: desconectando Realtime para pausar reintentos...")
                                supabase.realtime.disconnect()
                                redPrevia = null
                            }
                        } else if (red != redPrevia) {
                            Log.d(TAG, "Red validada disponible y cambió: reconectando Realtime...")
                            if (redPrevia != null) {
                                supabase.realtime.disconnect()
                            }
                            supabase.realtime.connect()
                            redPrevia = red
                        }
                    }.onFailure { err ->
                        LogSeguro.w(TAG, "Error al gestionar estado de Realtime con NetworkMonitor", err)
                    }
                }
            }

            // 1. Recolectar eventos de OPERACIONES
            scope.launch {
                OperacionesRealtimeListener.eventos.collect { action ->
                    if (validarSesion()) {
                        procesarEventoOperacionRemota(context, action)
                    }
                }
            }
            
            // 2. Recolectar eventos de MÉTODOS DE COBRO (CAPA 6)
            scope.launch {
                MetodoDeCobroRealtimeListener.eventos.collect { action ->
                    if (validarSesion()) {
                        procesarEventoMetodoRemoto(action)
                    }
                }
            }

            // 3. Recolectar eventos de RELACIÓN CUENTA-NEGOCIO (CAPA 38)
            escucharRelacionesCuenta(context)
        }
        
        // Sincronización diferencial pura de canales activos
        val canalesActivos = OperacionesRealtimeListener.canalesActivos()
        val diff = RealtimeChannelDiff.calcularDiferencial(canalesActivos, negocioIds.toSet())

        diff.aIniciar.forEach { id ->
            OperacionesRealtimeListener.iniciarEscucha(id)
            MetodoDeCobroRealtimeListener.iniciarEscucha(id)
        }

        diff.aDetener.forEach { id ->
            OperacionesRealtimeListener.detenerNegocio(id)
            MetodoDeCobroRealtimeListener.detenerNegocio(id)
        }

        // 4. Escuchar relaciones de los negocios donde la cuenta activa es el dueño real (CAPA 46)
        val negociosDueno = NegocioStore.negocios.value.filter { it.dueñoCuentaId == currentUid && it.id in negocioIds }
        negociosDueno.forEach { negocio ->
            escucharRelacionesDelNegocio(negocio.id)
        }
    }

    /**
     * Valida que el procesamiento del evento ocurra bajo la sesión que inició el manager.
     */
    private fun validarSesion(): Boolean {
        val currentUid = CuentaStore.sesionActual.value?.id
        val match = currentUid != null && currentUid == activeSessionUid
        if (!match) {
            Log.w(TAG, "[STALE] Ignorando evento Realtime: sesión no coincide ($activeSessionUid vs $currentUid)")
        }
        return match
    }

    /**
     * Detiene todas las suscripciones de Realtime y cancela los procesos de escucha de forma esperable.
     */
    suspend fun detenerTodo() = withContext(Dispatchers.IO) {
        initialized = false
        activeSessionUid = null
        job.cancel()

        val canalesACancelar = synchronized(managedChannels) {
            val lista = managedChannels.values.toList()
            managedChannels.clear()
            lista
        }
        
        // Reset de job/scope para re-inicialización posterior
        job = SupervisorJob()
        scope = CoroutineScope(job + Dispatchers.IO)

        canalesACancelar.forEach { ch ->
            runCatching { ch.unsubscribe() }.onFailure { e ->
                LogSeguro.w(TAG, "Error al desuscribir canal administrado", e)
            }
        }
        
        runCatching { OperacionesRealtimeListener.detenerTodo() }
        runCatching { MetodoDeCobroRealtimeListener.detenerTodo() }
    }

    fun detenerNegocio(negocioId: String) {
        OperacionesRealtimeListener.detenerNegocio(negocioId)
        MetodoDeCobroRealtimeListener.detenerNegocio(negocioId)
    }

    /** Procesa cambios en la tabla 'operaciones' (INSERT, UPDATE, DELETE). */
    private suspend fun procesarEventoOperacionRemota(context: Context, action: PostgresAction) {
        when (action) {
            is PostgresAction.Delete -> {
                val id = action.oldRecord["id"]?.jsonPrimitive?.contentOrNull ?: return
                val negocioId = action.oldRecord["negocio_id"]?.jsonPrimitive?.contentOrNull ?: return
                Log.d(TAG, "DELETE remoto recibido para $id (negocio: $negocioId)")
                InMemoryOperationStore.eliminarPorIdRemoto(id, negocioId)
                return
            }
            is PostgresAction.Insert, is PostgresAction.Update -> {
                val record = if (action is PostgresAction.Insert) action.record else (action as PostgresAction.Update).record
                val id = record["id"]?.jsonPrimitive?.contentOrNull ?: return
                val nuevoEstadoStr = record["estado"]?.jsonPrimitive?.contentOrNull ?: return
                val nuevoTrabajadorId = record["trabajador_asignado_id"]?.jsonPrimitive?.contentOrNull
                
                val nuevoEstado = try { 
                    EstadoOperacion.valueOf(nuevoEstadoStr) 
                } catch (e: Exception) { 
                    return 
                }

                val operacionLocal = InMemoryOperationStore.operaciones.value.find { it.id == id }
                val esCambioRelevante = operacionLocal == null ||
                        operacionLocal.estado != nuevoEstado ||
                        operacionLocal.trabajadorAsignadoId != nuevoTrabajadorId

                if (esCambioRelevante) {
                    Log.d(TAG, "Cambio remoto relevante detectado para $id ($nuevoEstado). Refrescando...")
                    InMemoryOperationStore.refrescarUna(id)
                    
                    val actualizada = InMemoryOperationStore.operaciones.value.find { it.id == id }
                    if (actualizada != null) {
                        OperationNotifier.notificar(context, actualizada)
                    }
                } else {
                    Log.d(TAG, "Eco o cambio no relevante para $id. Ignorado.")
                }
            }
            else -> return
        }
    }

    /** Procesa cambios en la tabla 'metodos_de_cobro' (CAPA 6). */
    private fun procesarEventoMetodoRemoto(action: PostgresAction) {
        val json = Json { ignoreUnknownKeys = true }
        try {
            when (action) {
                is PostgresAction.Insert -> {
                    val dto = json.decodeFromJsonElement<MetodoDeCobroRepository.MetodoDeCobroDto>(action.record)
                    MetodoDeCobroStore.upsertRemoto(dto.toDomain())
                }
                is PostgresAction.Update -> {
                    val dto = json.decodeFromJsonElement<MetodoDeCobroRepository.MetodoDeCobroDto>(action.record)
                    MetodoDeCobroStore.upsertRemoto(dto.toDomain())
                }
                is PostgresAction.Delete -> {
                    val id = action.oldRecord["id"]?.jsonPrimitive?.contentOrNull ?: return
                    MetodoDeCobroStore.eliminarRemoto(id)
                }
                else -> return
            }
            Log.d(TAG, "Cambio procesado exitosamente para métodos de cobro (${action::class.simpleName})")
        } catch (e: Exception) {
            LogSeguro.e(TAG, "Error al procesar cambio remoto de método", e)
        }
    }

    /**
     * Inicia la escucha de cambios en tiempo real en relacion_cuenta_negocio para la cuenta activa (CAPA 38).
     */
    fun escucharRelacionesCuenta(context: Context) {
        val uid = CuentaStore.sesionActual.value?.id ?: return
        val key = "relaciones_$uid"

        val channel = synchronized(managedChannels) {
            if (managedChannels.containsKey(key)) return@escucharRelacionesCuenta
            val supabase = SupabaseClientProvider.supabase
            val ch = supabase.realtime.channel(key) {}
            managedChannels[key] = ch
            ch
        }

        scope.launch {
            launch {
                runCatching {
                    channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                        table = "relacion_cuenta_negocio"
                        filter("cuenta_id", FilterOperator.EQ, uid)
                    }.collect { action ->
                        if (validarSesion()) {
                            procesarEventoRelacionRemota(action)
                        }
                    }
                }.onFailure { e ->
                    LogSeguro.w(TAG, "Error en flujo Realtime de relaciones para cuenta $uid", e)
                }
            }

            runCatching {
                channel.subscribe()
            }.onFailure { e ->
                LogSeguro.w(TAG, "Error al suscribirse al canal $key", e)
            }
        }
    }

    /**
     * Inicia la escucha de cambios en tiempo real en relacion_cuenta_negocio para un negocio (CAPA 46).
     */
    fun escucharRelacionesDelNegocio(negocioId: String) {
        if (!validarSesion()) return
        val key = "relaciones_negocio_$negocioId"

        val channel = synchronized(managedChannels) {
            if (managedChannels.containsKey(key)) return@escucharRelacionesDelNegocio
            val supabase = SupabaseClientProvider.supabase
            val ch = supabase.realtime.channel(key) {}
            managedChannels[key] = ch
            ch
        }

        scope.launch {
            try {
                launch {
                    runCatching {
                        channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                            table = "relacion_cuenta_negocio"
                            filter("negocio_id", FilterOperator.EQ, negocioId)
                        }.collect { action ->
                            if (validarSesion()) {
                                procesarEventoRelacionNegocioRemota(negocioId, action)
                            }
                        }
                    }.onFailure { e ->
                        LogSeguro.w(TAG, "Error en flujo Realtime de relaciones para negocio $negocioId", e)
                    }
                }

                runCatching {
                    channel.subscribe()
                }.onFailure { e ->
                    LogSeguro.w(TAG, "Error al suscribirse al canal $key", e)
                }
            } catch (e: Exception) {
                LogSeguro.e(TAG, "Error al iniciar escucha de relaciones del negocio $negocioId", e)
            }
        }
    }

    private fun procesarEventoRelacionNegocioRemota(negocioId: String, action: PostgresAction) {
        val json = Json { ignoreUnknownKeys = true }
        try {
            when (action) {
                is PostgresAction.Insert, is PostgresAction.Update -> {
                    val record = if (action is PostgresAction.Insert) action.record else (action as PostgresAction.Update).record
                    val dto = json.decodeFromJsonElement<RelacionCuentaNegocioRepository.RelacionDto>(record)
                    val relacionDomain = dto.toDomain()
                    val relacionesActuales = RelacionCuentaNegocioStore.relaciones.value.filter { it.negocioId == negocioId }.toMutableList()
                    val idx = relacionesActuales.indexOfFirst { it.cuentaId == relacionDomain.cuentaId }
                    if (idx >= 0) {
                        relacionesActuales[idx] = relacionDomain
                    } else {
                        relacionesActuales.add(relacionDomain)
                    }
                    RelacionCuentaNegocioStore.sincronizarNegocio(negocioId, relacionesActuales)
                }
                is PostgresAction.Delete -> {
                    val cuentaId = action.oldRecord["cuenta_id"]?.jsonPrimitive?.contentOrNull ?: return
                    RelacionCuentaNegocioStore.quitarRelacion(negocioId, cuentaId)
                }
                else -> return
            }
        } catch (e: Exception) {
            LogSeguro.e(TAG, "Error al procesar cambio remoto de relación de negocio", e)
        }
    }

    private fun procesarEventoRelacionRemota(action: PostgresAction) {
        val json = Json { ignoreUnknownKeys = true }
        try {
            when (action) {
                is PostgresAction.Insert, is PostgresAction.Update -> {
                    val record = if (action is PostgresAction.Insert) action.record else (action as PostgresAction.Update).record
                    val dto = json.decodeFromJsonElement<RelacionCuentaNegocioRepository.RelacionDto>(record)
                    val relacionDomain = dto.toDomain()
                    val relacionesActuales = RelacionCuentaNegocioStore.relaciones.value.toMutableList()
                    val idx = relacionesActuales.indexOfFirst { it.cuentaId == relacionDomain.cuentaId && it.negocioId == relacionDomain.negocioId }
                    if (idx >= 0) {
                        relacionesActuales[idx] = relacionDomain
                    } else {
                        relacionesActuales.add(relacionDomain)
                    }
                    RelacionCuentaNegocioStore.sincronizar(relacionDomain.cuentaId, relacionesActuales.filter { it.cuentaId == relacionDomain.cuentaId })
                }
                is PostgresAction.Delete -> {
                    val cuentaId = action.oldRecord["cuenta_id"]?.jsonPrimitive?.contentOrNull ?: return
                    val negocioId = action.oldRecord["negocio_id"]?.jsonPrimitive?.contentOrNull ?: return
                    val relacionesFiltradas = RelacionCuentaNegocioStore.relaciones.value.filterNot {
                        it.cuentaId == cuentaId && it.negocioId == negocioId
                    }
                    RelacionCuentaNegocioStore.sincronizar(cuentaId, relacionesFiltradas)
                }
                else -> return
            }
        } catch (e: Exception) {
            LogSeguro.e(TAG, "Error al procesar cambio remoto de relación", e)
        }
    }
}
