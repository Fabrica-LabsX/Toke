package com.flabs.cobrate.core.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.flabs.cobrate.core.model.EstadoInvitacion
import com.flabs.cobrate.core.model.InvitacionTrabajador
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Persistencia local de invitaciones de trabajador usando Jetpack DataStore
 * (Preferences) con serialización JSON manual vía kotlinx.serialization —
 * MISMO patrón que [CuentaLocalStore].
 */
private val Context.invitacionesDataStore by preferencesDataStore(name = "invitaciones")

internal class InvitacionLocalStore(private val context: Context) {

    private val invitacionesKey = stringPreferencesKey("lista_invitaciones")

    private val json = Json {
        ignoreUnknownKeys = true
    }

    /** Lee las invitaciones persistidas una sola vez (DTOs). */
    suspend fun leerUnaVez(): List<InvitacionDto> =
        context.invitacionesDataStore.data.map { prefs ->
            prefs[invitacionesKey]?.let { raw ->
                runCatching {
                    json.decodeFromString<List<InvitacionDto>>(raw)
                }.getOrElse { emptyList() }
            } ?: emptyList()
        }.first()

    /** Guarda la lista completa de invitaciones (sobrescribe lo anterior). */
    suspend fun guardar(invitaciones: List<InvitacionTrabajador>) {
        val raw = json.encodeToString(invitaciones.map { it.aDto() })
        context.invitacionesDataStore.edit { prefs ->
            prefs[invitacionesKey] = raw
        }
    }

    // ---- DTO serializable (espejo del dominio) ----

    @Serializable
    internal data class InvitacionDto(
        val id: String,
        val negocioId: String,
        val codigo: String,
        val estado: EstadoInvitacion,
        val fechaCreacion: Long = 0L,
        val expiracion: Long = 0L,
        val requiereAprobacion: Boolean = true
    )
}

// ---- Funciones de mapeo ----

private fun InvitacionTrabajador.aDto(): InvitacionLocalStore.InvitacionDto =
    InvitacionLocalStore.InvitacionDto(
        id = id,
        negocioId = negocioId,
        codigo = codigo,
        estado = estado,
        fechaCreacion = fechaCreacion,
        expiracion = expiracion,
        requiereAprobacion = requiereAprobacion
    )

internal fun InvitacionLocalStore.InvitacionDto.aModelo(): InvitacionTrabajador =
    InvitacionTrabajador(
        id = id,
        negocioId = negocioId,
        codigo = codigo,
        estado = estado,
        fechaCreacion = fechaCreacion,
        expiracion = expiracion,
        requiereAprobacion = requiereAprobacion
    )