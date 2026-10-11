package com.flabs.cobrate.core.logic

import com.flabs.cobrate.core.model.EstadoRelacion
import com.flabs.cobrate.core.model.RelacionCuentaNegocio
import com.flabs.cobrate.core.model.RolCuentaNegocio

enum class Requisito { AVISOS, BATERIA, ACCESO_A_NOTIFICACIONES }

object RequisitosPermisos {
    /** Dueño o administrador activo de al menos un negocio: los tres. Solo trabajador: AVISOS y BATERIA. */
    fun para(cuentaId: String, relaciones: List<RelacionCuentaNegocio>): Set<Requisito> {
        val esDuenoOAdminActivo = relaciones.any { rel ->
            rel.cuentaId == cuentaId &&
                    rel.rol == RolCuentaNegocio.ADMINISTRADOR &&
                    rel.estado == EstadoRelacion.ACTIVA
        }
        return if (esDuenoOAdminActivo) {
            setOf(Requisito.AVISOS, Requisito.BATERIA, Requisito.ACCESO_A_NOTIFICACIONES)
        } else {
            setOf(Requisito.AVISOS, Requisito.BATERIA)
        }
    }
}
