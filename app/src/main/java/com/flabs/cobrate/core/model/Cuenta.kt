package com.flabs.cobrate.core.model

/**
 * Cuenta única multi-negocio de TOKE V1.
 *
 * Reemplaza el modelo de "un solo negocio implícito" de la app: una misma
 * cuenta puede tener relación con varios negocios (ver [RelacionCuentaNegocio]),
 * con roles por relación.
 *
 * Sin lógica de negocio, solo la estructura de datos.
 */
data class Cuenta(
    val id: String,
    // Nombre de usuario único de la cuenta.
    val username: String,
    // Correo verificado (null si aún no hay uno verificado).
    val correoVerificado: String? = null,
    // Teléfono verificado (null si aún no hay uno verificado).
    val telefonoVerificado: String? = null,
    // Última vez que la cuenta accedió al servicio.
    val ultimoAcceso: Long = 0L,
    val fechaCreacion: Long = 0L
)