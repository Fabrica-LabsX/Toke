package com.flabs.cobrate.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Vista previa ligera de una invitación para el trabajador.
 * CAPA 7d-3.
 */
@Serializable
data class PreviewInvitacion(
    @SerialName("negocio_nombre") val negocioNombre: String,
    @SerialName("dueno_username") val duenoUsername: String
)
