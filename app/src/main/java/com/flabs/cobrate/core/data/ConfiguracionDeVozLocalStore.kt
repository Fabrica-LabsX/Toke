package com.flabs.cobrate.core.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.flabs.cobrate.core.model.ConfiguracionDeVoz
import com.flabs.cobrate.core.model.ModoVoz
import com.flabs.cobrate.core.model.ModoVozMigracion
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private val Context.configuracionVozDataStore by preferencesDataStore(name = "configuracion_voz")

/**
 * Persistencia local de la configuración de voz usando Jetpack DataStore
 * (Preferences) con serialización JSON manual vía kotlinx.serialization —
 * MISMO patrón que [CuentaLocalStore]/[NegocioLocalStore].
 *
 * Se guarda la configuración completa como una única cadena JSON bajo una
 * clave de Preferences.
 */
internal class ConfiguracionDeVozLocalStore(private val context: Context) {

    private val configKey = stringPreferencesKey("configuracion_voz")

    private val json = Json {
        ignoreUnknownKeys = true
        // Los campos nuevos de versiones futuras no deben romper la lectura.
        encodeDefaults = true
    }

    /** Lee la configuración persistida (null si aún no se ha guardado). */
    suspend fun leerUnaVez(): ConfiguracionDeVozDto? =
        context.configuracionVozDataStore.data.map { prefs ->
            prefs[configKey]?.let { raw ->
                runCatching {
                    json.decodeFromString<ConfiguracionDeVozDto>(raw)
                }.getOrNull()
            }
        }.first()

    /** Guarda la configuración, sobrescribiendo lo anterior. */
    suspend fun guardar(config: ConfiguracionDeVoz) {
        val raw = json.encodeToString(config.aDto())
        context.configuracionVozDataStore.edit { prefs ->
            prefs[configKey] = raw
        }
    }

    // ---- DTO serializable (espejo del dominio) ----

    @Serializable
    internal data class ConfiguracionDeVozDto(
        val activada: Boolean = true,
        // Texto para poder leer lo guardado con versiones anteriores (había un modo "TOKI").
        val modo: String = ModoVoz.CAUTELOSO.name,
        val subModoToki: String? = null,
        val vozSeleccionada: String? = null,
        val velocidad: Float = 1.0f,
        val vibrarAlAnunciar: Boolean = true,
        val efectoSonidoActivado: Boolean = false,
        val efectoSonidoId: String? = null
    )
}

// ---- Funciones de mapeo ----

private fun ConfiguracionDeVoz.aDto(): ConfiguracionDeVozLocalStore.ConfiguracionDeVozDto =
    ConfiguracionDeVozLocalStore.ConfiguracionDeVozDto(
        activada = activada,
        modo = modo.name,
        vozSeleccionada = vozSeleccionada,
        velocidad = velocidad,
        vibrarAlAnunciar = vibrarAlAnunciar,
        efectoSonidoActivado = efectoSonidoActivado,
        efectoSonidoId = efectoSonidoId
    )

internal fun ConfiguracionDeVozLocalStore.ConfiguracionDeVozDto.aModelo(): ConfiguracionDeVoz =
    ConfiguracionDeVoz(
        activada = activada,
        modo = ModoVozMigracion.desdeGuardado(modo, subModoToki),
        vozSeleccionada = vozSeleccionada,
        velocidad = velocidad,
        vibrarAlAnunciar = vibrarAlAnunciar,
        efectoSonidoActivado = efectoSonidoActivado,
        efectoSonidoId = efectoSonidoId
    )
