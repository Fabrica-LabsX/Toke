package com.flabs.cobrate.core.logic

import com.flabs.cobrate.core.model.EstadoInvitacion
import com.flabs.cobrate.core.model.InvitacionTrabajador

/**
 * Validaciones puras de una [InvitacionTrabajador].
 *
 * Funciones puras de tipo "check": reciben la invitación (no acceden a Stores
 * ni a Context) y devuelven true/false o un motivo de rechazo.
 */
object InvitacionValidation {

    /** La invitación existe (no null). */
    fun invitacionExiste(invitacion: InvitacionTrabajador?): Boolean = invitacion != null

    /** La invitación está PENDIENTE (no ACEPTADA, no RECHAZADA, no EXPIRADA). */
    fun estaPendiente(invitacion: InvitacionTrabajador): Boolean =
        invitacion.estado == EstadoInvitacion.PENDIENTE

    /** La invitación no venció respecto a [timestamp]. */
    fun noVencio(invitacion: InvitacionTrabajador, timestamp: Long): Boolean =
        invitacion.expiracion > timestamp

    /**
     * La invitación es válida para aceptarse en [timestamp]: existe, está
     * PENDIENTE y no venció. Devuelve el motivo de rechazo si no es válida.
     */
    fun motivoDeRechazo(
        invitacion: InvitacionTrabajador?,
        timestamp: Long
    ): MotivoRechazo? = when {
        !invitacionExiste(invitacion) -> MotivoRechazo.NO_EXISTE
        !estaPendiente(invitacion!!) -> MotivoRechazo.NO_PENDIENTE
        !noVencio(invitacion, timestamp) -> MotivoRechazo.EXPIRADA
        else -> null
    }
}

/** Motivos por los que una invitación no puede aceptarse. */
enum class MotivoRechazo {
    NO_EXISTE,
    NO_PENDIENTE,
    EXPIRADA
}