package com.flabs.cobrate.core.model

/**
 * Estado de un permiso del sistema relevante para TOKE V1.
 *
 * Representa el estado de permisos como el de acceso a notificaciones
 * (necesario para la detección automática de pagos) o el de ejecución en
 * segundo plano. Solo modela el estado; la gestión/otorgamiento de permisos
 * (UI, flujos) llega en una capa posterior.
 *
 * Sin lógica de negocio, solo la estructura de datos.
 */
data class EstadoPermiso(
    val tipo: TipoPermiso,
    val otorgado: Boolean,
    // Última vez que se verificó el estado real del permiso en el sistema.
    val ultimaVerificacion: Long = 0L
)

/** Tipos de permiso que TOKE V1 contempla. */
enum class TipoPermiso {
    // Acceso a las notificaciones (detección automática de pagos).
    ACCESO_NOTIFICACIONES,
    // Ejecución en segundo plano (detección sin que la app esté abierta).
    SEGUNDO_PLANO,
    BATERIA
}