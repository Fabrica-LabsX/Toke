package com.flabs.cobrate.core.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.flabs.cobrate.core.model.EstadoRelacion
import com.flabs.cobrate.core.model.RelacionCuentaNegocio
import com.flabs.cobrate.core.model.RolCuentaNegocio
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Persistencia local de relaciones Cuenta↔Negocio usando Jetpack DataStore
 * (Preferences) con serialización JSON manual — MISMO patrón que
 * [CuentaLocalStore].
 */
internal class RelacionCuentaNegocioLocalStore(private val context: Context) {

    private val Context.dataStore by preferencesDataStore(name = "relaciones")

    private val relacionesKey = stringPreferencesKey("lista_relaciones")

    private val json = Json {
        ignoreUnknownKeys = true
    }

    /** Lee las relaciones persistidas una sola vez (DTOs). */
    suspend fun leerUnaVez(): List<RelacionDto> =
        context.dataStore.data.map { prefs ->
            prefs[relacionesKey]?.let { raw ->
                runCatching {
                    json.decodeFromString<List<RelacionDto>>(raw)
                }.getOrElse { emptyList() }
            } ?: emptyList()
        }.first()

    /** Guarda la lista completa de relaciones (sobrescribe lo anterior). */
    suspend fun guardar(relaciones: List<RelacionCuentaNegocio>) {
        val raw = json.encodeToString(relaciones.map { it.aDto() })
        context.dataStore.edit { prefs ->
            prefs[relacionesKey] = raw
        }
    }

    // ---- DTO serializable (espejo del dominio) ----

    @Serializable
    internal data class RelacionDto(
        val cuentaId: String,
        val negocioId: String,
        val rol: RolCuentaNegocio,
        val estado: EstadoRelacion,
        val fechaVinculacion: Long,
        val codigoUsado: String? = null
    )
}

// ---- Funciones de mapeo ----

private fun RelacionCuentaNegocio.aDto(): RelacionCuentaNegocioLocalStore.RelacionDto =
    RelacionCuentaNegocioLocalStore.RelacionDto(
        cuentaId = cuentaId,
        negocioId = negocioId,
        rol = rol,
        estado = estado,
        fechaVinculacion = fechaVinculacion,
        codigoUsado = codigoUsado
    )

internal fun RelacionCuentaNegocioLocalStore.RelacionDto.aModelo(): RelacionCuentaNegocio =
    RelacionCuentaNegocio(
        cuentaId = cuentaId,
        negocioId = negocioId,
        rol = rol,
        estado = estado,
        fechaVinculacion = fechaVinculacion,
        codigoUsado = codigoUsado
    )