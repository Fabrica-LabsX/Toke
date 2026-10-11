package com.flabs.cobrate.core.logic

import com.flabs.cobrate.core.model.EstadoRelacion
import com.flabs.cobrate.core.model.RelacionCuentaNegocio

/**
 * Detección pura de transiciones de estado en relaciones Cuenta-Negocio (CAPA 50).
 * Totalmente testable, sin dependencias de Android, Compose ni Stores.
 */
object TransicionesRelacion {

    /** negocioIds donde la cuenta pasó a ACTIVA (no lo era antes o no existía la relación). */
    fun recienActivadas(
        cuentaId: String,
        antes: List<RelacionCuentaNegocio>,
        ahora: List<RelacionCuentaNegocio>
    ): List<String> {
        val activasAntes = antes
            .filter { it.cuentaId == cuentaId && it.estado == EstadoRelacion.ACTIVA }
            .map { it.negocioId }
            .toSet()

        return ahora
            .filter { it.cuentaId == cuentaId && it.estado == EstadoRelacion.ACTIVA }
            .map { it.negocioId }
            .filter { it !in activasAntes }
            .distinct()
    }

    /** negocioIds donde la cuenta dejó de estar ACTIVA (pausa, expulsión, salida o borrado de la relación). */
    fun dejaronDeSerActivas(
        cuentaId: String,
        antes: List<RelacionCuentaNegocio>,
        ahora: List<RelacionCuentaNegocio>
    ): List<String> {
        val activasAhora = ahora
            .filter { it.cuentaId == cuentaId && it.estado == EstadoRelacion.ACTIVA }
            .map { it.negocioId }
            .toSet()

        return antes
            .filter { it.cuentaId == cuentaId && it.estado == EstadoRelacion.ACTIVA }
            .map { it.negocioId }
            .filter { it !in activasAhora }
            .distinct()
    }
}
