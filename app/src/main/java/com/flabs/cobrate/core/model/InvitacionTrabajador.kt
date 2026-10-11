package com.flabs.cobrate.core.model

/**
 * Invitación de un trabajador a un negocio.
 *
 * Modelo de datos únicamente: la lógica de envío/aceptación/rechazo de
 * invitaciones (flujo funcional) llegará en una capa posterior. Este archivo
 * solo define la estructura.
 *
 * El trabajador aún no existe como cuenta al momento de la invitación, por eso
 * no hay un campo trabajadorId: la invitación se identifica por su [id] interno
 * y se comparte al trabajador mediante el [codigo].
 *
 * Sin lógica de negocio, solo la estructura de datos.
 */
data class InvitacionTrabajador(
    val id: String,
    val negocioId: String,
    // Código de invitación real, distinto del id (lo usa el trabajador para unirse).
    val codigo: String,
    val estado: EstadoInvitacion,
    val fechaCreacion: Long = 0L,
    // Momento en que la invitación deja de ser válida.
    val expiracion: Long = 0L,
    val nombrePropuesto: String? = null
)

/** Estados posibles de una invitación de trabajador. */
enum class EstadoInvitacion {
    PENDIENTE,
    ACEPTADA,
    RECHAZADA,
    EXPIRADA,
    REVOCADA
}