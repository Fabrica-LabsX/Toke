package com.flabs.cobrate.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Vista previa enriquecida de una invitación para el trabajador.
 * CAPA 7d-3 + ETAPA 4 FIX.
 *
 * Contiene los datos del negocio para la UI y los datos técnicos para 
 * reconstruir la InvitacionTrabajador sin hacer SELECT directo a la tabla.
 */
@Serializable
data class PreviewInvitacion(
    @SerialName("codigo") val codigo: String,
    @SerialName("negocio_id") val negocioId: String,
    @SerialName("estado") val estado: EstadoInvitacion,
    @SerialName("expiracion") val expiracion: Long,
    @SerialName("fecha_creacion") val fechaCreacion: Long,
    @SerialName("requiere_aprobacion") val requiereAprobacion: Boolean = true,
    @SerialName("negocio_nombre") val negocioNombre: String,
    @SerialName("dueno_username") val duenoUsername: String,
    @SerialName("dueno_nombre_visible") val duenoNombreVisible: String? = null
)

/** Convierte el preview en un objeto de dominio de invitación. */
fun PreviewInvitacion.toInvitacion() = InvitacionTrabajador(
    id = codigo,
    negocioId = negocioId,
    codigo = codigo,
    estado = estado,
    fechaCreacion = fechaCreacion,
    expiracion = expiracion,
    requiereAprobacion = requiereAprobacion
)
