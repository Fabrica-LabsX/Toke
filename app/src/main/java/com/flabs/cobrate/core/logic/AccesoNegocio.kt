package com.flabs.cobrate.core.logic

import com.flabs.cobrate.core.model.EstadoRelacion
import com.flabs.cobrate.core.model.RelacionCuentaNegocio

/**
 * Regla pura de acceso a negocio (CAPA 36 + CAPA 47).
 * Determina el estado de acceso de una cuenta con un negocio.
 */
object AccesoNegocio {

    /** Estados de acceso posibles para un negocio. */
    enum class EstadoAcceso {
        NORMAL,
        PAUSADO,
        SOLICITADO,
        RECHAZADO
    }

    /** Determina el estado de acceso de la cuenta al negocio. */
    fun estadoDeAcceso(
        cuentaId: String,
        negocioId: String?,
        relaciones: List<RelacionCuentaNegocio>
    ): EstadoAcceso {
        if (negocioId == null) return EstadoAcceso.NORMAL
        val rel = relaciones.find { it.cuentaId == cuentaId && it.negocioId == negocioId }
        return when (rel?.estado) {
            EstadoRelacion.PAUSADA -> EstadoAcceso.PAUSADO
            EstadoRelacion.SOLICITADA -> EstadoAcceso.SOLICITADO
            EstadoRelacion.RECHAZADA -> EstadoAcceso.RECHAZADO
            else -> EstadoAcceso.NORMAL
        }
    }

    /** True si la cuenta tiene una relación PAUSADA con ese negocio. */
    fun estaPausado(
        cuentaId: String,
        negocioId: String?,
        relaciones: List<RelacionCuentaNegocio>
    ): Boolean {
        return estadoDeAcceso(cuentaId, negocioId, relaciones) == EstadoAcceso.PAUSADO
    }
}
