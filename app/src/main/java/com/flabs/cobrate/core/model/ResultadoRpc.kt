package com.flabs.cobrate.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Modelo estándar para respuestas JSON de RPCs de Supabase (`{"exito": boolean, "mensaje": string}`).
 */
@Serializable
data class ResultadoRpc(
    @SerialName("exito") val exito: Boolean,
    @SerialName("mensaje") val mensaje: String
)
