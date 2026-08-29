package com.flabs.cobrate.core.model

/**
 * Estado del control "SERVICIOS" de un negocio (CAPA 5b).
 *
 * Regla de producto: SERVICIOS es un control de EJECUCIÓN, no de identidad.
 * NUNCA cierra sesión, NUNCA expulsa trabajadores y NUNCA borra datos
 * históricos al desactivarse. Solo controla si la distribución REMOTA de
 * eventos a otros dispositivos debe respetar el estado (pendiente, post-Supabase).
 *
 * La [programacion] solo modela la estructura (CAPA 5c en standby); aquí NO se
 * implementa ninguna lógica de programación.
 */
data class EstadoServicios(
    val negocioId: String,
    // True por defecto: los servicios están activados salvo que el dueño los apague.
    val activado: Boolean = true,
    // Estructura de programación (sin lógica todavía; CAPA 5c en standby).
    val programacion: ProgramacionServicios? = null
)

/** Programación horaria de SERVICIOS. Solo estructura; la lógica es CAPA 5c. */
data class ProgramacionServicios(
    val dias: List<DiaSemana>,
    val horaInicio: String,
    val horaFin: String,
    val activa: Boolean
)

/** Días de la semana para la programación de SERVICIOS. */
enum class DiaSemana {
    LUN,
    MAR,
    MIE,
    JUE,
    VIE,
    SAB,
    DOM
}