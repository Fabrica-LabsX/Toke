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

    /**
     * CAPA 8-SANEAMIENTO: La clave ahora incluye el UID para aislamiento físico.
     */
    private fun keyParaUsuario(uid: String) = stringPreferencesKey("negocios_$uid")

    private val json = Json {
        ignoreUnknownKeys = true
    }

    /** Lee los negocios persistidos una sola vez para un usuario específico. */
    suspend fun leerUnaVez(uid: String): List<NegocioDto> =
        context.dataStore.data.map { prefs ->
            prefs[keyParaUsuario(uid)]?.let { raw ->
                runCatching {
                    json.decodeFromString<List<NegocioDto>>(raw)
                }.getOrElse { emptyList() }
            } ?: emptyList()
        }.first()

    /** Guarda la lista completa de negocios para un usuario (espera finalización). */
    suspend fun guardar(uid: String, negocios: List<Negocio>) {
        val raw = json.encodeToString(negocios.map { it.aDto() })
        context.dataStore.edit { prefs ->
            prefs[keyParaUsuario(uid)] = raw
        }
    }

    /** Borra los datos de un usuario específico. */
    suspend fun borrarParaUsuario(uid: String) {
        context.dataStore.edit { it.remove(keyParaUsuario(uid)) }
    }

    // ---- DTO serializable (espejo del dominio) ----

    @Serializable
    internal data class NegocioDto(
        val id: String,
        val nombre: String,
        val tipo: TipoNegocio,
        val dueñoCuentaId: String,
        val colorMarca: String? = null
    )
}

// ---- Funciones de mapeo ----

private fun Negocio.aDto(): NegocioLocalStore.NegocioDto =
    NegocioLocalStore.NegocioDto(
        id = id,
        nombre = nombre,
        tipo = tipo,
        dueñoCuentaId = dueñoCuentaId,
        colorMarca = colorMarca
    )

internal fun NegocioLocalStore.NegocioDto.aModelo(): Negocio =
    Negocio(
        id = id,
        nombre = nombre,
        tipo = tipo,
        dueñoCuentaId = dueñoCuentaId,
        colorMarca = colorMarca
    )