package com.flabs.cobrate.core.logic

/**
 * Lógica pura para evaluar si TOKE debe operar en segundo plano (CAPA 60).
 */
object EstadoOperativoLogic {
    fun debeOperar(encendido: Boolean, haySesion: Boolean, permisoNotificaciones: Boolean): Boolean {
        return encendido && haySesion && permisoNotificaciones
    }
}
