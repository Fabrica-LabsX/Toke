package com.flabs.cobrate.core.logic

import com.flabs.cobrate.core.model.EstadoInvitacion
import com.flabs.cobrate.core.model.EstadoRelacion
import com.flabs.cobrate.core.model.InvitacionTrabajador
import com.flabs.cobrate.core.model.RelacionCuentaNegocio
import com.flabs.cobrate.core.model.RolCuentaNegocio
import java.util.Locale

/**
 * Reglas puras para la gestión de solicitudes de ingreso y códigos de invitación (CAPA 43).
 * Objeto sin Compose, sin Stores, totalmente testable.
 */
object SolicitudesIngreso {
    /** Relaciones TRABAJADOR en estado SOLICITADA del negocio, de la más antigua a la más reciente. */
    fun pendientes(relaciones: List<RelacionCuentaNegocio>, negocioId: String): List<RelacionCuentaNegocio> {
        return relaciones
            .filter {
                it.negocioId == negocioId &&
                it.rol == RolCuentaNegocio.TRABAJADOR &&
                it.estado == EstadoRelacion.SOLICITADA
            }
            .sortedBy { it.fechaVinculacion }
    }

    /** Códigos PENDIENTES y no vencidos, el que vence antes primero. */
    fun codigosVigentes(invitaciones: List<InvitacionTrabajador>, ahoraMs: Long): List<InvitacionTrabajador> {
        return invitaciones
            .filter {
                it.estado == EstadoInvitacion.PENDIENTE &&
                it.expiracion > ahoraMs
            }
            .sortedBy { it.expiracion }
    }

    /** Segundos que le quedan a un código (0 si ya venció). */
    fun segundosRestantes(invitacion: InvitacionTrabajador, ahoraMs: Long): Long {
        val diffMs = invitacion.expiracion - ahoraMs
        return if (diffMs <= 0L) 0L else diffMs / 1000L
    }

    /** Texto "mm:ss" para mostrar la cuenta regresiva. */
    fun formatearRestante(segundos: Long): String {
        val seg = if (segundos < 0L) 0L else segundos
        val minutos = seg / 60
        val segsRestantes = seg % 60
        return String.format(Locale.US, "%02d:%02d", minutos, segsRestantes)
    }
}
