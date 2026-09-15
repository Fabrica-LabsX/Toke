package com.flabs.cobrate.core.data

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

/**
 * Listener de cambios en tiempo real para la tabla 'operaciones'.
 * CAPA-REALTIME paso 2.
 *
 * Singleton que gestiona múltiples canales de Realtime (uno por negocio)
 * y emite todos los eventos capturados en un flujo compartido.
 */
internal object OperacionesRealtimeListener {

    private var job = SupervisorJob()
    private var scope = CoroutineScope(job + Dispatchers.IO)
    private val channels = mutableMapOf<String, RealtimeChannel>()

    private val _eventos = MutableSharedFlow<PostgresAction>(extraBufferCapacity = 64)
    /** Flujo de todos los eventos de Postgres recibidos (Insert/Update/Delete). */
    val eventos: SharedFlow<PostgresAction> = _eventos.asSharedFlow()

    /**
     * Inicia la suscripción al canal de Realtime para un negocio.
     * Filtra por 'negocio_id' para recibir solo lo relevante.
     * Si ya existe un canal para ese negocio, no hace nada.
     */
    fun iniciarEscucha(negocioId: String) {
        if (channels.containsKey(negocioId)) return
        
        val supabase = SupabaseClientProvider.supabase
        val newChannel = supabase.realtime.channel("operaciones_$negocioId") {}
        channels[negocioId] = newChannel

        scope.launch {
            newChannel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "operaciones"
                filter("negocio_id", FilterOperator.EQ, negocioId)
            }.collect { action ->
                _eventos.emit(action)
            }
        }

        scope.launch {
            newChannel.subscribe()
        }
    }

    /**
     * Cancela todas las suscripciones y libera los canales.
     * Cancela el Job de las corrutinas activas para evitar fugas y callbacks zombies.
     */
    fun detenerTodo() {
        // Cancelamos el job actual para detener todos los collectors de postgresChangeFlow
        job.cancel()
        
        // Creamos un nuevo Job y Scope para futuras conexiones (si las hubiera)
        job = SupervisorJob()
        scope = CoroutineScope(job + Dispatchers.IO)

        scope.launch {
            channels.values.forEach { 
                runCatching { it.unsubscribe() }
            }
            channels.clear()
        }
    }
}
