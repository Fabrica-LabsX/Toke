package com.flabs.cobrate.core.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.flabs.cobrate.core.model.Negocio
import com.flabs.cobrate.core.model.TipoNegocio
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Persistencia local de negocios usando Jetpack DataStore (Preferences) con
 * serialización JSON manual vía kotlinx.serialization — MISMO patrón que
 * [CuentaLocalStore].
 *
 * Se guarda la lista completa de negocios como una única cadena JSON bajo una
 * clave de Preferences.
 */
internal class NegocioLocalStore(private val context: Context) {

    private val Context.dataStore by preferencesDataStore(name = "negocios")

    private val negociosKey = stringPreferencesKey("lista_negocios")

    private val json = Json {
        ignoreUnknownKeys = true
    }

    /** Lee los negocios persistidos una sola vez (DTOs). */
    suspend fun leerUnaVez(): List<NegocioDto> =
        context.dataStore.data.map { prefs ->
            prefs[negociosKey]?.let { raw ->
                runCatching {
                    json.decodeFromString<List<NegocioDto>>(raw)
                }.getOrElse { emptyList() }
            } ?: emptyList()
        }.first()

    /** Guarda la lista completa de negocios (sobrescribe lo anterior). */
    suspend fun guardar(negocios: List<Negocio>) {
        val raw = json.encodeToString(negocios.map { it.aDto() })
        context.dataStore.edit { prefs ->
            prefs[negociosKey] = raw
        }
    }

    // ---- DTO serializable (espejo del dominio) ----

    @Serializable
    internal data class NegocioDto(
        val id: String,
        val nombre: String,
        val tipo: TipoNegocio,
        val dueñoCuentaId: String
    )
}

// ---- Funciones de mapeo ----

private fun Negocio.aDto(): NegocioLocalStore.NegocioDto =
    NegocioLocalStore.NegocioDto(
        id = id,
        nombre = nombre,
        tipo = tipo,
        dueñoCuentaId = dueñoCuentaId
    )

internal fun NegocioLocalStore.NegocioDto.aModelo(): Negocio =
    Negocio(
        id = id,
        nombre = nombre,
        tipo = tipo,
        dueñoCuentaId = dueñoCuentaId
    )