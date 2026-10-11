package com.flabs.cobrate.core.model

/**
 * Representa un método de cobro del negocio con QR y visibilidad.
 * Ahora completo con persistencia y Storage.
 */
data class MetodoDeCobro(
    val id: String = "",
    val negocioId: String = "",
    val tipo: TipoMetodoCobro,
    val datoDeCobro: String = "",
    val nombreDestinatario: String? = null,
    val qrUrl: String? = null,
    val activo: Boolean = true,
    val oculto: Boolean = false,
    val creadoEn: Long = 0
)

/** Tipos de método de cobro soportados. */
enum class TipoMetodoCobro {
    YAPE,
    PLIN,
    TRANSFERENCIA,
    BCP,
    INTERBANK,
    BIM,
    AGORA_TUNKI,
    BBVA,
    BANBIF,
    LIGO,
    SCOTIABANK,
    PICHINCHA,
    SIP,
    LEMON_CASH
}
