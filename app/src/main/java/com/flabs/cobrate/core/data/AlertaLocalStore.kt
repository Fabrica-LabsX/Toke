package com.flabs.cobrate.core.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.flabs.cobrate.core.model.Alerta
import com.flabs.cobrate.core.model.TipoAlerta
import com.flabs.cobrate.core.model.TipoMetodoCobro
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Persistencia local de alertas aisladas por usuario usando Jetpack DataStore (Preferences).
 */
internal class AlertaLocalStore(private val context: Context) {

    private val Context.dataStore by preferencesDataStore(name = "alertas")

    private fun keyParaUsuario(uid: String) = stringPreferencesKey("alertas_$uid")

    private val json = Json {
        ignoreUnknownKeys = true
    }

    /** Lee las alertas persistidas una sola vez para un usuario específico. */
    suspend fun leerUnaVez(uid: String): List<Alerta> =
        context.dataStore.data.map { prefs ->
            prefs[keyParaUsuario(uid)]?.let { raw ->
                runCatching {
                    json.decodeFromString<List<AlertaDto>>(raw).mapNotNull { it.aModelo(uid) }
                }.getOrElse { emptyList() }
            } ?: emptyList()
        }.first()

    /** Guarda la lista completa de alertas para un usuario específico. */
    suspend fun guardar(uid: String, alertas: List<Alerta>) {
        val raw = json.encodeToString(alertas.map { it.aDto() })
        context.dataStore.edit { prefs ->
            prefs[keyParaUsuario(uid)] = raw
        }
    }

    // ---- DTO serializable ----

    @Serializable
    internal data class AlertaDto(
        val id: String,
        val tipo: TipoAlerta,
        val operacionId: String,
        val mensaje: String,
        val eventoId: String? = null,
        val negocioId: String = "",
        val cuentaId: String = "",
        val tipoMetodoCobro: TipoMetodoCobro? = null
    )
}

// ---- Funciones de mapeo ----

private fun Alerta.aDto(): AlertaLocalStore.AlertaDto =
    AlertaLocalStore.AlertaDto(
        id = id,
        tipo = tipo,
        operacionId = operacionId,
        mensaje = mensaje,
        eventoId = eventoId,
        negocioId = negocioId,
        cuentaId = cuentaId,
        tipoMetodoCobro = tipoMetodoCobro
    )

internal fun AlertaLocalStore.AlertaDto.aModelo(uid: String): Alerta? {
    if (negocioId.isEmpty() || cuentaId.isEmpty() || cuentaId != uid) return null
    return Alerta(
        id = id,
        tipo = tipo,
        operacionId = operacionId,
        mensaje = mensaje,
        eventoId = eventoId,
        negocioId = negocioId,
        cuentaId = cuentaId,
        tipoMetodoCobro = tipoMetodoCobro
    )
}
