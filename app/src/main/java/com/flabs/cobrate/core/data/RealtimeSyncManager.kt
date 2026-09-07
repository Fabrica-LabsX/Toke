package com.flabs.cobrate.core.data

import android.content.Context
import android.util.Log
import com.flabs.cobrate.core.logic.OperationNotifier
import com.flabs.cobrate.core.model.EstadoOperacion
import io.github.jan.supabase.realtime.PostgresAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonPrimitive

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
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var initialized = false

    /**
     * Inicia la escucha de eventos de Realtime para los negocios indicados.
     * Solo se inicializa una vez para recolectar el flujo de eventos.
     */
    fun configurarEscucha(context: Context, negocioIds: List<String>) {
        if (!initialized) {
            initialized = true
            scope.launch {
                OperacionesRealtimeListener.eventos.collect { action ->
                    procesarEventoRemoto(context, action)
                }
            }
        }
        
        negocioIds.forEach { id ->
            OperacionesRealtimeListener.iniciarEscucha(id)
        }
    }

    /**
     * Detiene todas las suscripciones de Realtime.
     */
    fun detenerTodo() {
        OperacionesRealtimeListener.detenerTodo()
    }

    private suspend fun procesarEventoRemoto(context: Context, action: PostgresAction) {
        val record = when (action) {
            is PostgresAction.Insert -> action.record
            is PostgresAction.Update -> action.record
            else -> return // Ignoramos DELETE por ahora.
        }

        val id = record["id"]?.jsonPrimitive?.contentOrNull ?: return
        val nuevoEstadoStr = record["estado"]?.jsonPrimitive?.contentOrNull ?: return
        val nuevoTrabajadorId = record["trabajador_asignado_id"]?.jsonPrimitive?.contentOrNull
        
        val nuevoEstado = try { 
            EstadoOperacion.valueOf(nuevoEstadoStr) 
        } catch (e: Exception) { 
            return 
        }

        // 1. Verificar si es un cambio relevante y EVITAR EL ECO.
        val operacionLocal = InMemoryOperationStore.operaciones.value.find { it.id == id }
        
        val esCambioRelevante = if (operacionLocal == null) {
            // Si no la tenemos, es relevante si es una nueva operación SIN_ASIGNAR.
            action is PostgresAction.Insert && nuevoEstado == EstadoOperacion.SIN_ASIGNAR
        } else {
            // Si la tenemos, es relevante si el estado cambió o si se asignó a alguien distinto.
            operacionLocal.estado != nuevoEstado || operacionLocal.trabajadorAsignadoId != nuevoTrabajadorId
        }

        if (esCambioRelevante) {
            Log.d(TAG, "Cambio remoto relevante detectado para $id ($nuevoEstado). Refrescando...")
            
            // 2. Refrescar la operación completa desde Supabase para asegurar consistencia.
            InMemoryOperationStore.refrescarUna(id)
            
            // 3. Notificar al usuario (Voz, Android, Vibración).
            // Obtenemos la versión actualizada de la caché tras el refresco.
            val actualizada = InMemoryOperationStore.operaciones.value.find { it.id == id }
            if (actualizada != null) {
                OperationNotifier.notificar(context, actualizada)
            }
        } else {
            Log.d(TAG, "Eco o cambio no relevante para $id. Ignorado.")
        }
    }
}
