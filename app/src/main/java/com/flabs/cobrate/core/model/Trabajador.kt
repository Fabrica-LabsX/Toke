package com.flabs.cobrate.core.model

/**
 * Representa a un trabajador del negocio.
 * Sin lógica de negocio, solo el modelo de datos.
 */
data class Trabajador(
    val id: String,
    val nombre: String,
    val modo: ModoRegistro,
    val estado: EstadoTrabajador,
    // Permisos de caja. Placeholder: visible en UI marcado "Próximamente" (doc. de Caja),
    // sin lógica detrás todavía.
    val permisosDeCaja: Boolean = true
)

/** Forma en que el trabajador se registra o reporta. */
enum class ModoRegistro {
    APP,
    WHATSAPP
}

/** Estado operativo de un trabajador. */
enum class EstadoTrabajador {
    ACTIVO,
    PAUSADO,
    SUSPENDIDO
}