package com.flabs.cobrate.core.data

import com.flabs.cobrate.core.model.CambioDeEstado
import com.flabs.cobrate.core.model.EstadoOperacion
import com.flabs.cobrate.core.model.Operacion
import com.flabs.cobrate.core.model.OrigenOperacion
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Repositorio de Operaciones conectado a Supabase.
 * CAPA 7e-1.
 *
 * Maneja la persistencia de [Operacion] y su historial de [CambioDeEstado]
 * en las tablas 'operaciones' y 'cambios_de_estado'.
 */
internal class OperacionRepository {

    private val postgrest = SupabaseClientProvider.supabase.postgrest

    /**
     * Inserta una operación y su historial inicial en Supabase.
     *
     * DECISIÓN: Al no contar con transacciones multi-tabla atómicas desde el cliente,
     * se inserta primero la operación y luego su historial. Si la inserción del
     * historial falla, se reporta como error pero la operación queda creada sin
     * historial en el servidor (inconsistencia que se asume para esta capa).
     */
    suspend fun crear(operacion: Operacion): Result<Operacion> = runCatching {
        val ahora = System.currentTimeMillis()
        val dto = operacion.toDto(fechaCreacion = ahora)
        
        postgrest.from("operaciones").insert(dto)

        if (operacion.historial.isNotEmpty()) {
            val historialDtos = operacion.historial.map { it.toDto(operacion.id) }
            postgrest.from("cambios_de_estado").insert(historialDtos)
        }

        // Recuperamos la versión final con historial reconstruido
        obtenerPorId(operacion.id).getOrThrow() ?: throw Exception("Error al recuperar operación tras creación")
    }

    /**
     * Actualiza el estado y datos de la operación, e inserta el nuevo cambio
     * en el historial.
     */
    suspend fun actualizar(operacion: Operacion): Result<Operacion> = runCatching {
        val dto = operacion.toDto()
        
        postgrest.from("operaciones").update(dto) {
            filter { eq("id", operacion.id) }
        }

        // Insertar solo la última entrada del historial (la que disparó la actualización)
        operacion.historial.lastOrNull()?.let { ultimo ->
            val historialDto = ultimo.toDto(operacion.id)
            postgrest.from("cambios_de_estado").insert(historialDto)
        }

        obtenerPorId(operacion.id).getOrThrow() ?: throw Exception("Error al recuperar operación tras actualización")
    }

    /**
     * Obtiene una operación por ID y reconstruye su historial completo.
     */
    suspend fun obtenerPorId(id: String): Result<Operacion?> = runCatching {
        val operacionDto = postgrest.from("operaciones")
            .select {
                filter { eq("id", id) }
            }
            .decodeSingleOrNull<OperacionDto>() ?: return@runCatching null

        val historialDtos = postgrest.from("cambios_de_estado")
            .select {
                filter { eq("operacion_id", id) }
                order("timestamp", Order.ASCENDING)
            }
            .decodeList<CambioDeEstadoDto>()

        operacionDto.toDomain(historialDtos)
    }

    /**
     * Obtiene todas las operaciones de un negocio con sus respectivos historiales.
     */
    suspend fun obtenerDelNegocio(negocioId: String): Result<List<Operacion>> = runCatching {
        val operacionesDtos = postgrest.from("operaciones")
            .select {
                filter { eq("negocio_id", negocioId) }
                order("fecha_hora", Order.DESCENDING)
            }
            .decodeList<OperacionDto>()

        if (operacionesDtos.isEmpty()) return@runCatching emptyList()

        val ids = operacionesDtos.map { it.id }
        val historialesPorOperacion = postgrest.from("cambios_de_estado")
            .select {
                filter { isIn("operacion_id", ids) }
            }
            .decodeList<CambioDeEstadoDto>()
            .groupBy { it.operacionId }

        operacionesDtos.map { dto ->
            dto.toDomain(historialesPorOperacion[dto.id] ?: emptyList())
        }
    }

    // ---- DTOs de Infraestructura (espejo de las tablas de Supabase) ----

    @Serializable
    internal data class OperacionDto(
        @SerialName("id") val id: String,
        @SerialName("negocio_id") val negocioId: String,
        @SerialName("trabajador_asignado_id") val trabajadorAsignadoId: String?,
        @SerialName("monto") val monto: Double,
        @SerialName("estado") val estado: EstadoOperacion,
        @SerialName("origen_tipo") val origenTipo: String,
        @SerialName("origen_evento_id") val origenEventoId: String?,
        @SerialName("origen_trabajador_id") val origenTrabajadorId: String?,
        @SerialName("fecha_hora") val fechaHora: Long,
        @SerialName("fecha_creacion") val fechaCreacion: Long? = null
    )

    @Serializable
    internal data class CambioDeEstadoDto(
        @SerialName("operacion_id") val operacionId: String,
        @SerialName("estado") val estado: EstadoOperacion,
        @SerialName("timestamp") val timestamp: Long
    )
}

// ---- Funciones de Mapeo (Domain <-> DTO) ----

private fun Operacion.toDto(fechaCreacion: Long? = null) = OperacionRepository.OperacionDto(
    id = id,
    negocioId = negocioId,
    trabajadorAsignadoId = trabajadorAsignadoId,
    monto = monto,
    estado = estado,
    origenTipo = when (origen) {
        is OrigenOperacion.Detectada -> "DETECTADA"
        OrigenOperacion.VaAPagar -> "VA_A_PAGAR"
    },
    origenEventoId = (origen as? OrigenOperacion.Detectada)?.eventoId,
    // NOTA: Aunque el modelo Operacion.kt (V1) no tiene creadaPorTrabajadorId en VaAPagar,
    // la tabla lo soporta. Se deja como null por ahora.
    origenTrabajadorId = null,
    fechaHora = fechaHora,
    fechaCreacion = fechaCreacion
)

private fun CambioDeEstado.toDto(operacionId: String) = OperacionRepository.CambioDeEstadoDto(
    operacionId = operacionId,
    estado = estado,
    timestamp = timestamp
)

private fun OperacionRepository.OperacionDto.toDomain(historial: List<OperacionRepository.CambioDeEstadoDto>) = Operacion(
    id = id,
    negocioId = negocioId,
    trabajadorAsignadoId = trabajadorAsignadoId,
    monto = monto,
    estado = estado,
    origen = when (origenTipo) {
        "DETECTADA" -> OrigenOperacion.Detectada(origenEventoId ?: "")
        else -> OrigenOperacion.VaAPagar
    },
    fechaHora = fechaHora,
    historial = historial.map { it.toDomain() }.sortedBy { it.timestamp }
)

private fun OperacionRepository.CambioDeEstadoDto.toDomain() = CambioDeEstado(
    timestamp = timestamp,
    estado = estado
)
