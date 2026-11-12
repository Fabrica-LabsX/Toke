package com.flabs.cobrate.core.data


import com.flabs.cobrate.core.logic.LogSeguro
import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.flabs.cobrate.core.data.dto.CambioDeEstadoLocalDto
import com.flabs.cobrate.core.data.dto.OperacionLocalDto
import com.flabs.cobrate.core.model.CambioDeEstado
import com.flabs.cobrate.core.model.Operacion
import com.flabs.cobrate.core.model.OrigenOperacion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Persistencia local de operaciones por usuario (UID) usando DataStore + JSON.
 * CAPA 8-SANEAMIENTO.
 */
internal class OperationLocalStore(private val context: Context) {

    private val Context.dataStore by preferencesDataStore(name = "operaciones_cache")

    private fun keyParaUsuario(uid: String) = stringPreferencesKey("ops_$uid")

    private val json = Json {
        ignoreUnknownKeys = true
    }

    /** Expone la lista guardada como Flow para un usuario específico. */
    fun operaciones(uid: String): Flow<List<Operacion>> =
        context.dataStore.data.map { prefs ->
            prefs[keyParaUsuario(uid)]?.let { raw ->
                runCatching {
                    json.decodeFromString<List<OperacionLocalDto>>(raw).mapNotNull { it.aModelo() }
                }.getOrElse { emptyList() }
            } ?: emptyList()
        }

    /** Guarda la lista completa de operaciones para un usuario (espera finalización). */
    suspend fun guardar(uid: String, operaciones: List<Operacion>) {
        val raw = json.encodeToString(operaciones.map { it.aDto() })
        context.dataStore.edit { prefs ->
            prefs[keyParaUsuario(uid)] = raw
        }
    }

    /** Lee la lista guardada una sola vez (espera finalización). */
    suspend fun leerUnaVez(uid: String): List<Operacion> = operaciones(uid).first()
    
    /** Borra los datos de un usuario específico. */
    suspend fun borrarParaUsuario(uid: String) {
        context.dataStore.edit { it.remove(keyParaUsuario(uid)) }
    }
}

// ---- Funciones de mapeo ----

private fun Operacion.aDto(): OperacionLocalDto =
    OperacionLocalDto(
        id = id,
        negocioId = negocioId,
        trabajadorAsignadoId = trabajadorAsignadoId,
        metodoPago = metodoPago,
        monto = monto,
        estado = estado,
        origen = origen.aTag(),
        fechaHora = fechaHora,
        historial = historial.map { it.aDto() },
        pagador = pagador
    )

/** Representa el [OrigenOperacion] como un tag para persistencia. */
private fun OrigenOperacion.aTag(): String = when (this) {
    is OrigenOperacion.Detectada -> "Detectada:$eventoId"
}

private fun CambioDeEstado.aDto(): CambioDeEstadoLocalDto =
    CambioDeEstadoLocalDto(timestamp = timestamp, estado = estado)

private fun OperacionLocalDto.aModelo(): Operacion? {
    val origenParsed = origen.aOrigen()
    if (origenParsed == null) {
        LogSeguro.w("OperationLocalStore", "Descartando de la lista local la operación incompatible con origen '$origen' (ID: $id)")
        return null
    }
    return Operacion(
        id = id,
        negocioId = negocioId,
        trabajadorAsignadoId = trabajadorAsignadoId,
        metodoPago = metodoPago,
        monto = monto,
        estado = estado,
        origen = origenParsed,
        fechaHora = fechaHora,
        historial = historial.map { it.aModelo() },
        pagador = pagador
    )
}

/** Reconstruye un [OrigenOperacion] desde su tag persistido, exigiendo eventoId no vacío. */
private fun String.aOrigen(): OrigenOperacion? {
    if (startsWith("Detectada:")) {
        val eventoId = substringAfter("Detectada:")
        if (eventoId.isNotEmpty()) {
            return OrigenOperacion.Detectada(eventoId = eventoId)
        }
    }
    return null
}

private fun CambioDeEstadoLocalDto.aModelo(): CambioDeEstado =
    CambioDeEstado(timestamp = timestamp, estado = estado)
