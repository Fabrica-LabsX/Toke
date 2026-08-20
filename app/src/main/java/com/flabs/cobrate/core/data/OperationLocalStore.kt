package com.flabs.cobrate.core.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.flabs.cobrate.core.model.CambioDeEstado
import com.flabs.cobrate.core.model.EstadoOperacion
import com.flabs.cobrate.core.model.Operacion
import com.flabs.cobrate.core.model.OrigenOperacion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Persistencia local de la lista de [Operacion] usando Jetpack DataStore
 * (Preferences) con serialización JSON manual vía kotlinx.serialization.
 *
 * Se guarda la lista completa de operaciones como una única cadena JSON bajo
 * una clave de Preferences. Es la solución más simple para el punto actual del
 * producto: no justifica Room ni una base de datos.
 *
 * NOTA: los tipos de [Operacion] no son anotados con @Serializable (core/model
 * permanece intacto), por eso se definen aquí DTO serializables y funciones de
 * mapeo hacia/desde los modelos de dominio.
 */
internal class OperationLocalStore(private val context: Context) {

    private val Context.dataStore by preferencesDataStore(name = "operaciones")

    private val operacionesKey = stringPreferencesKey("lista_operaciones")

    private val json = Json {
        ignoreUnknownKeys = true
    }

    /** Expone la lista guardada como Flow (vacía si aún no hay datos). */
    fun operaciones(): Flow<List<Operacion>> =
        context.dataStore.data.map { prefs ->
            prefs[operacionesKey]?.let { raw ->
                runCatching {
                    json.decodeFromString<List<OperacionDto>>(raw).map { it.aModelo() }
                }.getOrElse { emptyList() }
            } ?: emptyList()
        }

    /** Guarda la lista completa de operaciones (sobrescribe lo anterior). */
    suspend fun guardar(operaciones: List<Operacion>) {
        val raw = json.encodeToString(operaciones.map { it.aDto() })
        context.dataStore.edit { prefs ->
            prefs[operacionesKey] = raw
        }
    }

    /** Lee la lista guardada una sola vez (para la carga inicial síncrona). */
    suspend fun leerUnaVez(): List<Operacion> = operaciones().first()

    // ---- DTOs serializables (espejo de los modelos de dominio) ----

    @Serializable
    internal data class OperacionDto(
        val id: String,
        val negocioId: String,
        val trabajadorAsignadoId: String? = null,
        val monto: Double,
        val estado: EstadoOperacion,
        // Origen serializado como tag ("Detectada"|"VaAPagar"); el campo origenId
        // conserva el id de evento en el caso Detectada.
        val origen: String,
        val fechaHora: Long,
        val historial: List<CambioDeEstadoDto> = emptyList(),
    )

    @Serializable
    internal data class CambioDeEstadoDto(
        val timestamp: Long,
        val estado: EstadoOperacion
    )
}

// ---- Funciones de mapeo ----

private fun Operacion.aDto(): OperationLocalStore.OperacionDto =
    OperationLocalStore.OperacionDto(
        id = id,
        negocioId = negocioId,
        trabajadorAsignadoId = trabajadorAsignadoId,
        monto = monto,
        estado = estado,
        origen = origen.aTag(),
        fechaHora = fechaHora,
        historial = historial.map { it.aDto() }
    )

/** Representa el [OrigenOperacion] como un tag para persistencia. */
private fun OrigenOperacion.aTag(): String = when (this) {
    is OrigenOperacion.Detectada -> "Detectada:$eventoId"
    OrigenOperacion.VaAPagar -> "VaAPagar"
}

private fun CambioDeEstado.aDto(): OperationLocalStore.CambioDeEstadoDto =
    OperationLocalStore.CambioDeEstadoDto(timestamp = timestamp, estado = estado)

private fun OperationLocalStore.OperacionDto.aModelo(): Operacion =
    Operacion(
        id = id,
        negocioId = negocioId,
        trabajadorAsignadoId = trabajadorAsignadoId,
        monto = monto,
        estado = estado,
        origen = origen.aOrigen(),
        fechaHora = fechaHora,
        historial = historial.map { it.aModelo() }
    )

/** Reconstruye un [OrigenOperacion] desde su tag persistido. */
private fun String.aOrigen(): OrigenOperacion =
    if (startsWith("Detectada:")) {
        OrigenOperacion.Detectada(eventoId = substringAfter("Detectada:"))
    } else {
        OrigenOperacion.VaAPagar
    }

private fun OperationLocalStore.CambioDeEstadoDto.aModelo(): CambioDeEstado =
    CambioDeEstado(timestamp = timestamp, estado = estado)