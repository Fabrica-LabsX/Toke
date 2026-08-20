package com.flabs.cobrate.core.model

/**
 * Plantillas de flujo predefinidas del producto.
 *
 * Fuente ÚNICA de las plantillas estándar. Tanto el flujo del Trabajador
 * ([PlantillaDeFlujo] por defecto) como la pantalla de plantillas del Dueño
 * leen de aquí, eliminando las definiciones duplicadas que antes existían en
 * WorkerViewModel y FlowTemplatesScreen.
 */
object PlantillasPredefinidas {

    /** Plantilla por defecto del trabajador. */
    val ventaInmediata = PlantillaDeFlujo(
        nombre = "Venta inmediata",
        estados = listOf("Pago confirmado", "Entregado", "Cerrado")
    )

    val conPreparacion = PlantillaDeFlujo(
        nombre = "Con preparación",
        estados = listOf("Pago confirmado", "En preparación", "Listo", "Entregado", "Cerrado")
    )

    val reserva = PlantillaDeFlujo(
        nombre = "Reserva",
        estados = listOf("Reservado", "Pago confirmado", "Entregado", "Cerrado")
    )

    /** Todas las plantillas predefinidas (para mostrar en el Dueño). */
    val todas: List<PlantillaDeFlujo> = listOf(ventaInmediata, conPreparacion, reserva)
}