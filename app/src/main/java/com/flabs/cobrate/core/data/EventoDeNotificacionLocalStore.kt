package com.flabs.cobrate.core.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.flabs.cobrate.core.model.EventoDeNotificacion
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Persistencia local de eventos de notificación de pago usando Jetpack
 * DataStore (Preferences) con serialización JSON manual vía
 * kotlinx.serialization — MISMO patrón que [InvitacionLocalStore].
 */
internal class EventoDeNotificacionLocalStore(private val context: Context) {

    private val Context.dataStore by preferencesDataStore(name = "eventos_notificacion")

    private val eventosKey = stringPreferencesKey("lista_eventos_notificacion")

    private val json = Json {
        ignoreUnknownKeys = true
    }

    /** Lee los eventos persistidos una sola vez (DTOs). */
    suspend fun leerUnaVez(): List<EventoDeNotificacionDto> =
        context.dataStore.data.map { prefs ->
            prefs[eventosKey]?.let { raw ->
                runCatching {
                    json.decodeFromString<List<EventoDeNotificacionDto>>(raw)
                }.getOrElse { emptyList() }
            } ?: emptyList()
        }.first()

    /** Guarda la lista completa de eventos (sobrescribe lo anterior). */
    suspend fun guardar(eventos: List<EventoDeNotificacion>) {
        val raw = json.encodeToString(eventos.map { it.aDto() })
        context.dataStore.edit { prefs ->
            prefs[eventosKey] = raw
        }
    }

    // ---- DTO serializable (espejo del dominio) ----

    @Serializable
    internal data class EventoDeNotificacionDto(
        val id: String,
        val negocioId: String,
        val paqueteOrigen: String,
        val titulo: String,
        val texto: String,
        val fechaHora: Long,
        val procesado: Boolean = false
    )
}

// ---- Funciones de mapeo ----

private fun EventoDeNotificacion.aDto(): EventoDeNotificacionLocalStore.EventoDeNotificacionDto =
    EventoDeNotificacionLocalStore.EventoDeNotificacionDto(
        id = id,
        negocioId = negocioId,
        paqueteOrigen = paqueteOrigen,
        titulo = titulo,
        texto = texto,
        fechaHora = fechaHora,
        procesado = procesado
    )

internal fun EventoDeNotificacionLocalStore.EventoDeNotificacionDto.aModelo(): EventoDeNotificacion =
    EventoDeNotificacion(
        id = id,
        negocioId = negocioId,
        paqueteOrigen = paqueteOrigen,
        titulo = titulo,
        texto = texto,
        fechaHora = fechaHora,
        procesado = procesado
    )