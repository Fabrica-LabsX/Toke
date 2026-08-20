package com.flabs.cobrate.core.model

/**
 * Representa una operación de cobro en el sistema.
 * Sin lógica de negocio, solo la estructura de datos.
 *
 * Modelo TOKE V1: la operación pertenece a un [negocioId] y puede estar
 * asignada a un trabajador ([trabajadorAsignadoId]) o no (null si SIN_ASIGNAR).
 * El origen ([origen]) indica cómo se creó: pago detectado automáticamente o
 * cobro manual "Va a pagar".
 */
data class Operacion(
    val id: String,
    val negocioId: String,
    // Trabajador al que está asignada la operación (null si no asignada).
    val trabajadorAsignadoId: String? = null,
    val monto: Double,
    val estado: EstadoOperacion,
    val origen: OrigenOperacion,
    val fechaHora: Long,
    val historial: List<CambioDeEstado> = emptyList()
)

/** Estados posibles de una operación de cobro. */
enum class EstadoOperacion {
    // Sin trabajador asignado todavía.
    SIN_ASIGNAR,
    // Asignada a un trabajador (pendiente de cobro).
    ASIGNADA,
    // Pago confirmado.
    CONFIRMADA,
    // Operación cerrada.
    CERRADA
}

/** Origen/forma en que se creó la operación. */
sealed class OrigenOperacion {
    /** Pago detectado automáticamente (vía notificación). "Próximamente". */
    data class Detectada(val eventoId: String = "") : OrigenOperacion()

    /** Operación creada manualmente con "Va a pagar". */
    data object VaAPagar : OrigenOperacion()
}

/** Un cambio de estado en el historial de una operación. */
data class CambioDeEstado(
    val timestamp: Long,
    val estado: EstadoOperacion
)