package com.flabs.cobrate.core.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.flabs.cobrate.core.model.DiaSemana
import com.flabs.cobrate.core.model.EstadoServicios
import com.flabs.cobrate.core.model.ProgramacionServicios
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Persistencia local del estado de SERVICIOS usando Jetpack DataStore
 * (Preferences) con serialización JSON manual vía kotlinx.serialization —
 * MISMO patrón que [CuentaLocalStore]/[AlertaLocalStore].
 *
 * Se guarda la lista completa de estados (uno por negocio) como una única
 * cadena JSON bajo una clave de Preferences.
 */
internal class EstadoServiciosLocalStore(private val context: Context) {

    private val Context.dataStore by preferencesDataStore(name = "estado_servicios")

    private val estadosKey = stringPreferencesKey("lista_estado_servicios")

    private val json = Json {
        ignoreUnknownKeys = true
    }

    /** Lee los estados persistidos una sola vez (DTOs). */
    suspend fun leerUnaVez(): List<EstadoServiciosDto> =
        context.dataStore.data.map { prefs ->
            prefs[estadosKey]?.let { raw ->
                runCatching {
                    json.decodeFromString<List<EstadoServiciosDto>>(raw)
                }.getOrElse { emptyList() }
            } ?: emptyList()
        }.first()

    /** Guarda la lista completa de estados (sobrescribe lo anterior). */
    suspend fun guardar(estados: List<EstadoServicios>) {
        val raw = json.encodeToString(estados.map { it.aDto() })
        context.dataStore.edit { prefs ->
            prefs[estadosKey] = raw
        }
    }

    // ---- DTO serializable (espejo del dominio) ----

    @Serializable
    internal data class EstadoServiciosDto(
        val negocioId: String,
        val activado: Boolean = true,
        val programacion: ProgramacionServiciosDto? = null
    )

    @Serializable
    internal data class ProgramacionServiciosDto(
        val dias: List<DiaSemana> = emptyList(),
        val horaInicio: String = "",
        val horaFin: String = "",
        val activa: Boolean = false
    )
}

// ---- Funciones de mapeo ----

private fun EstadoServicios.aDto(): EstadoServiciosLocalStore.EstadoServiciosDto =
    EstadoServiciosLocalStore.EstadoServiciosDto(
        negocioId = negocioId,
        activado = activado,
        programacion = programacion?.let {
            EstadoServiciosLocalStore.ProgramacionServiciosDto(
                dias = it.dias,
                horaInicio = it.horaInicio,
                horaFin = it.horaFin,
                activa = it.activa
            )
        }
    )

internal fun EstadoServiciosLocalStore.EstadoServiciosDto.aModelo(): EstadoServicios =
    EstadoServicios(
        negocioId = negocioId,
        activado = activado,
        programacion = programacion?.let {
            ProgramacionServicios(
                dias = it.dias,
                horaInicio = it.horaInicio,
                horaFin = it.horaFin,
                activa = it.activa
            )
        }
    )