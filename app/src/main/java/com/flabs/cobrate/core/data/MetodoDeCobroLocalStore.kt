package com.flabs.cobrate.core.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.flabs.cobrate.core.model.MetodoDeCobro
import com.flabs.cobrate.core.model.TipoMetodoCobro
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.metodosDeCobroDataStore by preferencesDataStore(name = "metodos_de_cobro")

/**
 * Persistencia local de métodos de cobro por negocio usando DataStore + JSON.
 * Mismo patrón que NegocioLocalStore.
 */
internal class MetodoDeCobroLocalStore(private val context: Context) {

    private fun keyParaNegocio(negocioId: String) = stringPreferencesKey("metodos_$negocioId")

    private val json = Json {
        ignoreUnknownKeys = true
    }

    suspend fun leerPorNegocio(negocioId: String): List<MetodoDeCobroDto> =
        context.metodosDeCobroDataStore.data.map { prefs ->
            prefs[keyParaNegocio(negocioId)]?.let { raw ->
                runCatching {
                    json.decodeFromString<List<MetodoDeCobroDto>>(raw)
                }.getOrElse { emptyList() }
            } ?: emptyList()
        }.first()

    suspend fun guardarPorNegocio(negocioId: String, metodos: List<MetodoDeCobro>) {
        val raw = json.encodeToString(metodos.map { it.aDto() })
        context.metodosDeCobroDataStore.edit { prefs ->
            prefs[keyParaNegocio(negocioId)] = raw
        }
    }

    suspend fun limpiarNegocio(negocioId: String) {
        context.metodosDeCobroDataStore.edit { prefs ->
            prefs.remove(keyParaNegocio(negocioId))
        }
    }

    suspend fun limpiarTodo() {
        context.metodosDeCobroDataStore.edit { it.clear() }
    }

    @Serializable
    internal data class MetodoDeCobroDto(
        val id: String,
        val negocioId: String,
        val tipo: String,
        val datoDeCobro: String,
        val nombreDestinatario: String?,
        val qrUrl: String?,
        val activo: Boolean,
        val oculto: Boolean,
        val creadoEn: Long
    )
}

internal fun MetodoDeCobro.aDto() = MetodoDeCobroLocalStore.MetodoDeCobroDto(
    id = id,
    negocioId = negocioId,
    tipo = tipo.name,
    datoDeCobro = datoDeCobro,
    nombreDestinatario = nombreDestinatario,
    qrUrl = qrUrl,
    activo = activo,
    oculto = oculto,
    creadoEn = creadoEn
)

internal fun MetodoDeCobroLocalStore.MetodoDeCobroDto.aModelo() = MetodoDeCobro(
    id = id,
    negocioId = negocioId,
    tipo = TipoMetodoCobro.valueOf(tipo),
    datoDeCobro = datoDeCobro,
    nombreDestinatario = nombreDestinatario,
    qrUrl = qrUrl,
    activo = activo,
    oculto = oculto,
    creadoEn = creadoEn
)
