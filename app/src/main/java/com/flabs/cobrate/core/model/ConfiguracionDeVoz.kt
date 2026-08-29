package com.flabs.cobrate.core.model

import kotlin.math.roundToInt

/**
 * Configuración de aviso por voz del trabajador (CAPA 14).
 *
 * Representa todas las opciones configurables por el trabajador para el
 * anuncio vocal de pagos confirmados. Esta capa solo construye el motor y la
 * pantalla de configuración; la integración real en el flujo de confirmación
 * llega en una capa posterior.
 */
data class ConfiguracionDeVoz(
    // ¿Está activada la confirmación de pagos en voz alta?
    val activada: Boolean = true,
    // Modo de la frase que se anuncia (ver regla de los 3 modos).
    val modo: ModoVoz = ModoVoz.TOKI,
    // Sub-modo únicamente relevante cuando modo == TOKI (inclusión del monto).
    val subModoToki: SubModoToki? = null,
    // Voz seleccionada por el usuario (id de locale/engine); null = por defecto.
    val vozSeleccionada: String? = null,
    // Velocidad de habla (rango típico 0.1f..2.0f, 1.0f = normal).
    val velocidad: Float = 1.0f,
    // ¿Vibrar al anunciar el pago?
    val vibrarAlAnunciar: Boolean = true,
    // ¿Reproducir un efecto de sonido de entrada al anunciar?
    val efectoSonidoActivado: Boolean = false,
    // Id del efecto de sonido seleccionado (null si no aplica).
    val efectoSonidoId: String? = null
) {
    companion object {
        val POR_DEFECTO = ConfiguracionDeVoz()

        /** Velocidad redondeada a 2 decimales (evita ruido de punto flotante). */
        fun velocidadNormalizada(velocidad: Float): Float =
            (velocidad * 100f).roundToInt() / 100f
    }
}

/** Modos de voz al confirmarse un pago. */
enum class ModoVoz {
    /** Sin mencionar el monto, nunca. */
    CAUTELOSO,

    /** Menciona el monto en soles. */
    NEGOCIO,

    /** Estilo "¡TOKE!" (el monto según [SubModoToki]). */
    TOKI
}

/** Sub-modo exclusivo de [ModoVoz.TOKI]. */
enum class SubModoToki {
    /** No incluye el monto en la frase. */
    DISCRETO,

    /** Incluye el monto en soles. */
    NEGOCIO
}