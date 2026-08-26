package com.flabs.cobrate.core.model

/**
 * Representa un método de cobro configurado por el dueño.
 * Sin lógica de negocio, solo el modelo de datos.
 *
 * Nota: no incluye un campo `id` propio ni asociación a negocio; eso se añadirá
 * cuando se persista (CAPA posterior) y se relacione con Negocio. Por ahora no
 * se agrega para no adelantarse a capas futuras.
 */
data class MetodoDeCobro(
    val tipo: TipoMetodoCobro,
    val datoDeCobro: String,
    val activo: Boolean
)

/** Tipos de método de cobro soportados. */
enum class TipoMetodoCobro {
    YAPE,
    PLIN,
    TRANSFERENCIA,
    BCP,
    INTERBANK,
    BIM,
    // Valores añadidos en CAPA 6c-2 (extensión aditiva del enum).
    AGORA_TUNKI,
    BBVA,
    BANBIF,
    LIGO,
    SCOTIABANK,
    PICHINCHA,
    SIP,
    LEMON_CASH
}