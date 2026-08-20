package com.flabs.cobrate.core.model

/**
 * Representa un turno de trabajo de un trabajador.
 *
 * Placeholder: sin lógica de cuadratura todavía (eso es CAPA 4 / futuro).
 */
data class Turno(
    val id: String,
    val trabajadorId: String,
    val horaInicio: Long,
    val horaFin: Long? = null
)