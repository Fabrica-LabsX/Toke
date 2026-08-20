package com.flabs.cobrate.core.model

/**
 * Representa un pago detectado (aviso de Yape/Plin u otro) que debe
 * asociarse a una operación.
 *
 * resultadoDeAsociacion es un placeholder: el campo existe, pero el motor
 * que lo completa aún no está implementado ("Próximamente").
 */
data class EventoDePago(
    val fuente: String,
    val montoDetectado: Double,
    val hora: Long,
    val resultadoDeAsociacion: String? = null
)