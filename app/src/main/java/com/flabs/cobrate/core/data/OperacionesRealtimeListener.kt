package com.flabs.cobrate.core.data

import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Listener de cambios en tiempo real para la tabla 'operaciones'.
 * CAPA-REALTIME paso 1.
 *
 * Permite suscribirse a los cambios (INSERT/UPDATE) de las operaciones 
 * de un negocio específico a través de WebSockets de Supabase.
 */
internal class OperacionesRealtimeListener {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var channel: RealtimeChannel? = null

    private val _eventos = MutableStateFlow<PostgresAction?>(null)
    /** Expone el último evento de Postgres recibido (Insert/Update/Delete). */
    val eventos: StateFlow<PostgresAction?> = _eventos.asStateFlow()

    /**
     * Inicia la suscripción al canal de Realtime para un negocio.
     * Filtra por 'negocio_id' para recibir solo lo relevante.
     */
    fun iniciarEscucha(negocioId: String) {
        detenerEscucha()
        
        val supabase = SupabaseClientProvider.supabase
        val newChannel = supabase.realtime.channel("operaciones_$negocioId") {}
        channel = newChannel

        scope.launch {
            newChannel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "operaciones"
                filter("negocio_id", FilterOperator.EQ, negocioId)
            }.collect { action ->
                _eventos.value = action
            }
        }

        scope.launch {
            newChannel.subscribe()
        }
    }

    /**
     * Cancela la suscripción y libera el canal.
     */
    fun detenerEscucha() {
        val currentChannel = channel ?: return
        scope.launch {
            currentChannel.unsubscribe()
        }
        channel = null
    }
}
