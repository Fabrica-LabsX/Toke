package com.flabs.cobrate.core.data

import com.flabs.cobrate.core.logic.LogSeguro
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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Listener de cambios en tiempo real para la tabla 'operaciones'.
 * CAPA-REALTIME paso 2.
 *
 * Singleton que gestiona múltiples canales de Realtime (uno por negocio)
 * y emite todos los eventos capturados en un flujo compartido de forma concurrente y segura.
 */
internal object OperacionesRealtimeListener {

    private const val TAG = "OperacionesRealtime"
    private var job = SupervisorJob()
    private var scope = CoroutineScope(job + Dispatchers.IO)
    private val channels = mutableMapOf<String, RealtimeChannel>()

    private val _eventos = MutableSharedFlow<PostgresAction>(extraBufferCapacity = 64)
    /** Flujo de todos los eventos de Postgres recibidos (Insert/Update/Delete). */
    val eventos: SharedFlow<PostgresAction> = _eventos.asSharedFlow()

    /** Retorna de forma atómica y segura el conjunto de IDs de negocios actualmente escuchados. */
    fun canalesActivos(): Set<String> = synchronized(channels) {
        channels.keys.toSet()
    }

    /**
     * Inicia la suscripción al canal de Realtime para un negocio de forma atómica.
     */
    fun iniciarEscucha(negocioId: String) {
        val newChannel = synchronized(channels) {
            if (channels.containsKey(negocioId)) return
            val supabase = SupabaseClientProvider.supabase
            val channel = supabase.realtime.channel("operaciones_$negocioId") {}
            channels[negocioId] = channel
            channel
        }

        scope.launch {
            runCatching {
                newChannel.postgresChangeFlow<PostgresAction>(schema = "public") {
                    table = "operaciones"
                    filter("negocio_id", FilterOperator.EQ, negocioId)
                }.collect { action ->
                    _eventos.emit(action)
                }
            }.onFailure { e ->
                LogSeguro.w(TAG, "Error en flujo Realtime para operaciones del negocio $negocioId", e)
            }
        }

        scope.launch {
            runCatching {
                newChannel.subscribe()
            }.onFailure { e ->
                LogSeguro.w(TAG, "Error al suscribirse al canal operaciones_$negocioId", e)
            }
        }
    }

    /**
     * Cancela todas las suscripciones y libera los canales de forma atómica y esperable.
     */
    suspend fun detenerTodo() = withContext(Dispatchers.IO) {
        job.cancel()
        
        job = SupervisorJob()
        scope = CoroutineScope(job + Dispatchers.IO)

        val canalesACancelar = synchronized(channels) {
            val lista = channels.values.toList()
            channels.clear()
            lista
        }

        canalesACancelar.forEach { ch ->
            runCatching { ch.unsubscribe() }.onFailure { e ->
                LogSeguro.w(TAG, "Fallo al desuscribir canal de operaciones", e)
            }
        }
    }

    /**
     * Detiene la escucha de un negocio específico de forma atómica.
     */
    fun detenerNegocio(negocioId: String) {
        val channel = synchronized(channels) {
            channels.remove(negocioId)
        }
        channel?.let { ch ->
            scope.launch {
                runCatching { ch.unsubscribe() }
            }
        }
    }
}
