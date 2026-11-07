package com.flabs.cobrate.core.data

import com.flabs.cobrate.core.model.CambioDeEstado
import com.flabs.cobrate.core.model.EstadoOperacion
import com.flabs.cobrate.core.model.Operacion
import com.flabs.cobrate.core.model.OrigenOperacion
import com.flabs.cobrate.core.model.TipoMetodoCobro
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Contrato del Repositorio de Operaciones para inyección desacoplada en ViewModels y Tests.
 */
internal interface OperacionRepositoryContract {
    suspend fun crear(operacion: Operacion): Result<Operacion>
    suspend fun obtenerPorId(id: String): Result<Operacion?>
    suspend fun obtenerDelNegocio(negocioId: String): Result<List<Operacion>>
    suspend fun obtenerDeVariosNegocios(negocioIds: List<String>): Result<List<Operacion>>
    suspend fun reclamar(operacionId: String): Result<ResultadoReclamo>
    suspend fun confirmarPago(operacionId: String): Result<ResultadoConfirmarPago>
}

/**
 * Repositorio de Operaciones conectado a Supabase.
 * CAPA 7e-1.
 *
 * Maneja la persistencia de [Operacion] y su historial de [CambioDeEstado]
 * en las tablas 'operaciones' y 'cambios_de_estado'.
 */
internal open class OperacionRepository : OperacionRepositoryContract {

    private val postgrest by lazy { SupabaseClientProvider.supabase.postgrest }

    /**
     * Inserta una operación y su historial inicial en Supabase.
     * REGLA DE NEGOCIO: Solo la sesión activa del DUEÑO del negocio puede crear operaciones.
     * Inserción sin pedir la fila de vuelta (returnMinimal) para no depender de la política SELECT.
     */
    override suspend fun crear(operacion: Operacion): Result<Operacion> = runCatching {
        val uid = CuentaStore.sesionActual.value?.id 
            ?: throw SecurityException("No hay sesión activa para crear la operación")
        val negocio = NegocioStore.buscarPorId(operacion.negocioId)
        if (negocio == null || negocio.dueñoCuentaId != uid) {
            throw SecurityException("Solo el dueño del negocio puede crear operaciones")
        }

        val ahora = System.currentTimeMillis()
        val dto = operacion.toDto(fechaCreacion = ahora)
        
        postgrest.from("operaciones").insert(dto)

        if (operacion.historial.isNotEmpty()) {
            val historialDtos = operacion.historial.map { it.toDto(operacion.id) }
            postgrest.from("cambios_de_estado").insert(historialDtos)
        }

        operacion
    }

    /**
     * Obtiene una operación por ID y reconstruye su historial completo.
     */
    override suspend fun obtenerPorId(id: String): Result<Operacion?> = runCatching {
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
     * Obtiene todas las operaciones de un negocio con ordenamiento determinista (fecha_hora DESC, id DESC).
     */
    override suspend fun obtenerDelNegocio(negocioId: String): Result<List<Operacion>> = runCatching {
        val operacionesDtos = postgrest.from("operaciones")
            .select {
                filter { eq("negocio_id", negocioId) }
                order("fecha_hora", Order.DESCENDING)
                order("id", Order.DESCENDING)
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

    /**
     * Obtiene todas las operaciones de varios negocios con sus respectivos historiales
     * en un número mínimo de peticiones.
     */
    override suspend fun obtenerDeVariosNegocios(negocioIds: List<String>): Result<List<Operacion>> = runCatching {
        if (negocioIds.isEmpty()) return@runCatching emptyList()

        val operacionesDtos = postgrest.from("operaciones")
            .select {
                filter { isIn("negocio_id", negocioIds) }
                order("fecha_hora", Order.DESCENDING)
                order("id", Order.DESCENDING)
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

    /**
     * Invoca la función RPC 'reclamar_operacion' para asignar una operación 
     * SIN_ASIGNAR al trabajador actual de forma atómica.
     */
    override suspend fun reclamar(operacionId: String): Result<ResultadoReclamo> = runCatching {
        postgrest.rpc(
            function = "reclamar_operacion",
            parameters = mapOf("operacion_id" to operacionId)
        ).decodeSingle<ResultadoReclamo>()
    }

    /**
     * Invoca la función RPC 'confirmar_pago' para mover la operación a CONFIRMADA.
     */
    override suspend fun confirmarPago(operacionId: String): Result<ResultadoConfirmarPago> = runCatching {
        postgrest.rpc(
            function = "confirmar_pago",
            parameters = mapOf("operacion_id" to operacionId)
        ).decodeSingle<ResultadoConfirmarPago>()
    }

    // ---- DTOs de Infraestructura (espejo de las tablas de Supabase) ----

    @Serializable
    internal data class OperacionDto(
        @SerialName("id") val id: String,
        @SerialName("negocio_id") val negocioId: String,
        @SerialName("trabajador_asignado_id") val trabajadorAsignadoId: String?,
        @SerialName("metodo_pago") val metodoPago: TipoMetodoCobro? = null,
        @SerialName("metodo_cobro_id") val metodoCobroId: String? = null,
        @SerialName("monto") val monto: Double,
        @SerialName("estado") val estado: EstadoOperacion,
        @SerialName("origen_tipo") val origenTipo: String,
        @SerialName("origen_evento_id") val origenEventoId: String?,
        @SerialName("fecha_hora") val fechaHora: Long,
        @SerialName("fecha_creacion") val fechaCreacion: Long? = null,
        // Columna existente: guarda el nombre del pagador que mostró la billetera.
        @SerialName("notif_snapshot_titulo") val pagador: String? = null
    )

    @Serializable
    internal data class CambioDeEstadoDto(
        @SerialName("operacion_id") val operacionId: String,
        @SerialName("estado") val estado: EstadoOperacion,
        @SerialName("timestamp") val timestamp: Long
    )
}

/** Resultado de la operación atómica de reclamo en el servidor. */
@Serializable
internal data class ResultadoReclamo(
    @SerialName("exito") val exito: Boolean,
    @SerialName("mensaje") val mensaje: String
)

/** Resultado de la confirmación de pago en el servidor. */
@Serializable
internal data class ResultadoConfirmarPago(
    @SerialName("exito") val exito: Boolean,
    @SerialName("mensaje") val mensaje: String
)

// ---- Funciones de Mapeo (Domain <-> DTO) ----

private fun Operacion.toDto(fechaCreacion: Long? = null) = OperacionRepository.OperacionDto(
    id = id,
    negocioId = negocioId,
    trabajadorAsignadoId = trabajadorAsignadoId,
    metodoPago = metodoPago,
    metodoCobroId = metodoCobroId,
    monto = monto,
    estado = estado,
    origenTipo = "DETECTADA",
    origenEventoId = (origen as? OrigenOperacion.Detectada)?.eventoId,
    fechaHora = fechaHora,
    fechaCreacion = fechaCreacion,
    pagador = pagador
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
    metodoPago = metodoPago,
    metodoCobroId = metodoCobroId,
    monto = monto,
    estado = estado,
    origen = OrigenOperacion.Detectada(origenEventoId ?: ""),
    fechaHora = fechaHora,
    historial = historial.map { it.toDomain() }.sortedBy { it.timestamp },
    pagador = pagador
)

private fun OperacionRepository.CambioDeEstadoDto.toDomain() = CambioDeEstado(
    timestamp = timestamp,
    estado = estado
)
