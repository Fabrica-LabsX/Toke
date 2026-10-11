package com.flabs.cobrate.core.model

/**
 * Representa un miembro del equipo de un negocio (retornado por RPC equipo_de_negocio).
 * CAPA 22.
 */
data class MiembroEquipo(
    val cuentaId: String,
    val username: String,
    val rol: RolCuentaNegocio,
    val estado: EstadoRelacion
)
