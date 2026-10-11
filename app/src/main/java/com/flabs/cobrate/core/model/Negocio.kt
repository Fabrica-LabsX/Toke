package com.flabs.cobrate.core.model

/**
 * Representa un negocio (dueño) de TOKE V1.
 *
 * Modelo TOKE V1: cada negocio tiene un dueño ([dueñoCuentaId]) que lo crea y
 * administra, y un [tipo] que clasifica su rubro. La participación de otras
 * cuentas en el negocio se modela por separado en [RelacionCuentaNegocio].
 *
 * Sin lógica de negocio, solo la estructura de datos.
 */
data class Negocio(
    val id: String,
    val nombre: String,
    val tipo: TipoNegocio,
    // Cuenta que creó/administra el negocio.
    val dueñoCuentaId: String,
    // Color de marca de la empresa (formato Hex #RRGGBB).
    val colorMarca: String? = null
)

/** Rubro/tipo del negocio. */
enum class TipoNegocio {
    RESTAURANTE,
    MARISQUERIA,
    ABARROTES,
    ROPA,
    ELECTRODOMESTICOS,
    SERVICIOS,
    OTRO
}