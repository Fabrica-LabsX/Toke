package com.flabs.cobrate.core.data.dto

import com.flabs.cobrate.core.model.EstadoOperacion
import com.flabs.cobrate.core.model.TipoMetodoCobro
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Serializador seguro para [TipoMetodoCobro]. 
 * CAPA 7-FIX: Si el valor en el JSON es desconocido, devuelve null en lugar de fallar.
 */
@OptIn(ExperimentalSerializationApi::class)
object SafeTipoMetodoCobroSerializer : KSerializer<TipoMetodoCobro?> {
    override val descriptor: SerialDescriptor = 
        PrimitiveSerialDescriptor("TipoMetodoCobro", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: TipoMetodoCobro?) {
        if (value == null) {
            encoder.encodeNull()
        } else {
            encoder.encodeString(value.name)
        }
    }

    override fun deserialize(decoder: Decoder): TipoMetodoCobro? {
        val name = runCatching { decoder.decodeString() }.getOrNull() ?: return null
        return try {
            TipoMetodoCobro.valueOf(name)
        } catch (e: Exception) {
            null
        }
    }
}

/**
 * DTO unificado para persistencia local (Caché y Cola Offline).
 * CAPA 8-SANEAMIENTO.
 */
@Serializable
data class OperacionLocalDto(
    val id: String,
    val negocioId: String,
    val trabajadorAsignadoId: String? = null,
    @Serializable(with = SafeTipoMetodoCobroSerializer::class)
    val metodoPago: TipoMetodoCobro? = null,
    val monto: Double,
    val estado: EstadoOperacion,
    val origen: String,
    val fechaHora: Long,
    val historial: List<CambioDeEstadoLocalDto> = emptyList(),
    val ownerId: String? = null, // Solo usado por OperacionPendienteStore
    val estadoSincronizacion: String = "ESPERANDO_RED" // Usado por OperacionPendienteStore
)

@Serializable
data class CambioDeEstadoLocalDto(
    val timestamp: Long,
    val estado: EstadoOperacion
)
