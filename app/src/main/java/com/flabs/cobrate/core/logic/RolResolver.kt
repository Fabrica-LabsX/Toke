package com.flabs.cobrate.core.logic

import com.flabs.cobrate.core.model.EstadoRelacion
import com.flabs.cobrate.core.model.RelacionCuentaNegocio
import com.flabs.cobrate.core.model.RolCuentaNegocio

/**
 * Resuelve el rol efectivo de una cuenta a partir de sus relaciones activas.
 *
 * Funciones puras: no acceden a Stores ni a Context; reciben datos y devuelven
 * datos, para poder probarse de forma aislada.
 */
object RolResolver {

    /**
     * Determina el rol efectivo de [cuentaId] dado el conjunto de relaciones.
     *
     * Solo se consideran las relaciones ACTIVAS. Prioridad: si la cuenta es
     * ADMINISTRADOR en al menos un negocio activo, se reporta ADMINISTRADOR;
     * si no, TRABAJADOR si lo es en al menos uno; si no, SIN_NEGOCIOS.
     */
    fun rolEfectivoDe(
        cuentaId: String,
        relaciones: List<RelacionCuentaNegocio>
    ): RolEfectivo {
        val activas = relaciones.filter {
            it.cuentaId == cuentaId && it.estado == EstadoRelacion.ACTIVA
        }
        return when {
            activas.any { it.rol == RolCuentaNegocio.ADMINISTRADOR } -> RolEfectivo.ADMINISTRADOR
            activas.any { it.rol == RolCuentaNegocio.TRABAJADOR } -> RolEfectivo.TRABAJADOR
            else -> RolEfectivo.SIN_NEGOCIOS
        }
    }

    /** ¿La cuenta es administrador en al menos un negocio? */
    fun esAdministrador(
        cuentaId: String,
        relaciones: List<RelacionCuentaNegocio>
    ): Boolean = rolEfectivoDe(cuentaId, relaciones) == RolEfectivo.ADMINISTRADOR

    /** ¿La cuenta es trabajador en al menos un negocio? */
    fun esTrabajador(
        cuentaId: String,
        relaciones: List<RelacionCuentaNegocio>
    ): Boolean = rolEfectivoDe(cuentaId, relaciones) == RolEfectivo.TRABAJADOR
}

/** Rol efectivo resuelto de una cuenta en el sistema. */
enum class RolEfectivo {
    // Administrador en al menos un negocio activo.
    ADMINISTRADOR,
    // Trabajador en al menos un negocio activo (pero no administrador en ninguno).
    TRABAJADOR,
    // Sin relaciones activas (no pertenece a ningún negocio todavía).
    SIN_NEGOCIOS
}