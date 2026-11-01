package com.flabs.cobrate.core.data

import com.flabs.cobrate.core.logic.LogSeguro
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Listener de Realtime para cambios en metodos_de_cobro por negocio.
 * Similar a OperacionesRealtimeListener.
 */
object MetodoDeCobroRealtimeListener {

    private const val TAG = "MetodoCobroRealtime"
    private var job = SupervisorJob()
    private var scope = CoroutineScope(job + Dispatchers.IO)

    private val channels = mutableMapOf<String, RealtimeChannel>()

    private val _eventos = MutableSharedFlow<PostgresAction>()
    val eventos: Flow<PostgresAction> = _eventos

    /**
     * Inicia la suscripción al canal para un negocio.
     */
    fun iniciarEscucha(negocioId: String) {
        if (channels.containsKey(negocioId)) return

        val supabase = SupabaseClientProvider.supabase
        val newChannel = supabase.realtime.channel("metodos_$negocioId") {}
        channels[negocioId] = newChannel

        scope.launch {
            runCatching {
                newChannel.postgresChangeFlow<PostgresAction>(schema = "public") {
                    table = "metodos_de_cobro"
                    filter("negocio_id", FilterOperator.EQ, negocioId)
                }.collect { action ->
                    _eventos.emit(action)
                }
            }.onFailure { e ->
                if (e is CancellationException) throw e
                LogSeguro.w(TAG, "Error en flujo Realtime para metodos_de_cobro del negocio $negocioId", e)
            }
        }

        scope.launch {
            runCatching {
                newChannel.subscribe()
            }.onFailure { e ->
                LogSeguro.w(TAG, "Error al suscribirse al canal metodos_$negocioId", e)
            }
        }
    }

    /**
     * Detiene todas las suscripciones de forma atómica y esperable.
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
                LogSeguro.w(TAG, "Fallo al desuscribir canal de métodos de cobro", e)
            }
        }
    }

    fun detenerNegocio(negocioId: String) {
        channels[negocioId]?.let { channel ->
            scope.launch {
                runCatching { channel.unsubscribe() }
                channels.remove(negocioId)
            }
        }
    }
}
