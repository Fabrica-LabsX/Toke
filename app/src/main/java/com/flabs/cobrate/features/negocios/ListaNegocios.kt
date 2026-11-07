package com.flabs.cobrate.features.negocios

import com.flabs.cobrate.core.model.EstadoRelacion
import com.flabs.cobrate.core.model.Negocio
import com.flabs.cobrate.core.model.RelacionCuentaNegocio
import com.flabs.cobrate.core.model.RolCuentaNegocio

/**
 * Lógica de la lista de negocios de una cuenta (pantalla "Mis Negocios" y selector
 * "Cambiar de negocio"). Sin nada de interfaz: decide qué negocios aparecen, con qué rol
 * y en qué estado.
 */

enum class RolEnNegocio { DUENO, COLABORADOR }

enum class EstadoEnNegocio {
    /** Se puede entrar. */
    DISPONIBLE,
    /** El dueño pausó el acceso de esta cuenta. */
    PAUSADO,
    /** La cuenta pidió unirse y espera la aprobación del dueño. */
    PENDIENTE,
}

data class NegocioItem(
    val negocio: Negocio,
    val rol: RolEnNegocio,
    val estado: EstadoEnNegocio,
    /** Es el negocio con el que se está trabajando ahora. */
    val esActual: Boolean,
) {
    /** Solo se puede entrar a un negocio propio o con acceso activo. */
    val puedeEntrar: Boolean get() = estado == EstadoEnNegocio.DISPONIBLE
}

object ListaNegocios {

    /**
     * Arma la lista unificada:
     *  1. primero los negocios donde la cuenta es dueña;
     *  2. después los negocios donde colabora (relación de trabajador activa, pausada o
     *     solicitada). Un negocio propio nunca aparece también como colaboración.
     */
    fun construir(
        negocios: List<Negocio>,
        relaciones: List<RelacionCuentaNegocio>,
        cuentaId: String?,
        negocioActualId: String?,
    ): List<NegocioItem> {
        if (cuentaId == null) return emptyList()

        val propios = negocios
            .filter { it.dueñoCuentaId == cuentaId }
            .map { negocio ->
                NegocioItem(
                    negocio = negocio,
                    rol = RolEnNegocio.DUENO,
                    estado = EstadoEnNegocio.DISPONIBLE,
                    esActual = negocio.id == negocioActualId,
                )
            }

        val colaboraciones = relaciones
            .filter { it.cuentaId == cuentaId && it.rol == RolCuentaNegocio.TRABAJADOR }
            .mapNotNull { relacion ->
                val estado = estadoDe(relacion.estado) ?: return@mapNotNull null
                val negocio = negocios.find { it.id == relacion.negocioId } ?: return@mapNotNull null
                if (negocio.dueñoCuentaId == cuentaId) return@mapNotNull null
                NegocioItem(
                    negocio = negocio,
                    rol = RolEnNegocio.COLABORADOR,
                    estado = estado,
                    esActual = negocio.id == negocioActualId && estado == EstadoEnNegocio.DISPONIBLE,
                )
            }

        return propios + colaboraciones
    }

    private fun estadoDe(estado: EstadoRelacion): EstadoEnNegocio? = when (estado) {
        EstadoRelacion.ACTIVA -> EstadoEnNegocio.DISPONIBLE
        EstadoRelacion.PAUSADA -> EstadoEnNegocio.PAUSADO
        EstadoRelacion.SOLICITADA -> EstadoEnNegocio.PENDIENTE
        else -> null
    }
}
