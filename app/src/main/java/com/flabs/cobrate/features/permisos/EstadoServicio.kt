package com.flabs.cobrate.features.permisos

import com.flabs.cobrate.core.logic.Requisito

/**
 * Lógica de la pantalla "Permisos y Servicio". Sin nada de interfaz: decide en qué estado
 * está el servicio y qué permisos hay que mostrar según el rol de la cuenta.
 */
enum class EstadoServicio {
    /** Falta un permiso imprescindible: TOKE no puede leer los avisos de pago. */
    DESACTIVADO,
    /** Funciona, pero con una limitación (por ejemplo, se detiene al apagar la pantalla). */
    PARCIAL,
    /** Todo lo necesario está concedido. */
    OPERATIVO,
}

object EvaluadorServicio {

    /** Sin información de requisitos se asume que se necesitan todos los permisos. */
    fun requiereAccesoNotificaciones(requisitos: Set<Requisito>): Boolean =
        requisitos.isEmpty() || Requisito.ACCESO_A_NOTIFICACIONES in requisitos

    fun requiereBateria(requisitos: Set<Requisito>): Boolean =
        requisitos.isEmpty() || Requisito.BATERIA in requisitos

    fun evaluar(
        requiereAcceso: Boolean,
        accesoConcedido: Boolean,
        requiereBateria: Boolean,
        bateriaSinRestricciones: Boolean,
    ): EstadoServicio = when {
        requiereAcceso && !accesoConcedido -> EstadoServicio.DESACTIVADO
        requiereBateria && !bateriaSinRestricciones -> EstadoServicio.PARCIAL
        else -> EstadoServicio.OPERATIVO
    }
}
