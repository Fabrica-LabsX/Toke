package com.flabs.cobrate.core.model

/**
 * Relación entre una [Cuenta] y un [Negocio].
 *
 * Es el corazón del modelo multi-negocio de TOKE V1: una cuenta participa en
 * un negocio bajo un [RolCuentaNegocio], lo que determina qué puede hacer en
 * ese negocio (administrar vs. operar).
 *
 * Sin lógica de negocio, solo la estructura de datos.
 */
data class RelacionCuentaNegocio(
    val cuentaId: String,
    val negocioId: String,
    val rol: RolCuentaNegocio,
    // Estado actual de la relación.
    val estado: EstadoRelacion = EstadoRelacion.ACTIVA,
    // Momento en que se vinculó la cuenta al negocio.
    val fechaVinculacion: Long = 0L,
    // Código con el que se solicitó ingresar al negocio (CAPA 42).
    val codigoUsado: String? = null
)

/** Rol de una cuenta dentro de un negocio. */
enum class RolCuentaNegocio {
    // Administra el negocio (dueño / admin).
    ADMINISTRADOR,
    // Opera el negocio (trabajador).
    TRABAJADOR
}

/** Estado de la relación de una cuenta con un negocio. */
enum class EstadoRelacion {
    // Relación vigente.
    ACTIVA,
    // Relación vigente pero silenciada.
    PAUSADA,
    // La cuenta fue expulsada del negocio.
    EXPULSADA,
    // La cuenta solicitó unirse pero requiere aprobación del dueño (CAPA 42).
    SOLICITADA,
    // La solicitud de ingreso fue rechazada por el dueño (CAPA 42).
    RECHAZADA
}