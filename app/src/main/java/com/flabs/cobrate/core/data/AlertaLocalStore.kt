package com.flabs.cobrate.core.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.flabs.cobrate.core.model.Alerta
import com.flabs.cobrate.core.model.TipoAlerta
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Persistencia local de alertas del Dueño usando Jetpack DataStore
 * (Preferences) con serialización JSON manual vía kotlinx.serialization —
 * MISMO patrón que [EventoDeNotificacionLocalStore]/[InvitacionLocalStore].
 */
internal class AlertaLocalStore(private val context: Context) {

    private val Context.dataStore by preferencesDataStore(name = "alertas")

    private val alertasKey = stringPreferencesKey("lista_alertas")

    private val json = Json {
        ignoreUnknownKeys = true
    }

    /** Lee las alertas persistidas una sola vez (DTOs). */
    suspend fun leerUnaVez(): List<AlertaDto> =
        context.dataStore.data.map { prefs ->
            prefs[alertasKey]?.let { raw ->
                runCatching {
                    json.decodeFromString<List<AlertaDto>>(raw)
                }.getOrElse { emptyList() }
            } ?: emptyList()
        }.first()

    /** Guarda la lista completa de alertas (sobrescribe lo anterior). */
    suspend fun guardar(alertas: List<Alerta>) {
        val raw = json.encodeToString(alertas.map { it.aDto() })
        context.dataStore.edit { prefs ->
            prefs[alertasKey] = raw
        }
    }

    // ---- DTO serializable (espejo del dominio) ----

    @Serializable
    internal data class AlertaDto(
        val id: String,
        val tipo: TipoAlerta,
        val operacionId: String,
        val mensaje: String,
        val eventoId: String? = null
    )
}

// ---- Funciones de mapeo ----

private fun Alerta.aDto(): AlertaLocalStore.AlertaDto =
    AlertaLocalStore.AlertaDto(
        id = id,
        tipo = tipo,
        operacionId = operacionId,
        mensaje = mensaje,
        eventoId = eventoId
    )

internal fun AlertaLocalStore.AlertaDto.aModelo(): Alerta =
    Alerta(
        id = id,
        tipo = tipo,
        operacionId = operacionId,
        mensaje = mensaje,
        eventoId = eventoId
    )