package com.flabs.cobrate.core.model

import kotlin.math.roundToInt

/**
 * Configuración de aviso por voz de quien usa la app.
 *
 * Representa las opciones configurables del anuncio vocal de pagos confirmados.
 */
data class ConfiguracionDeVoz(
    // ¿Está activada la confirmación de pagos en voz alta?
    val activada: Boolean = true,
    // Qué tanto dice la voz (ver [ModoVoz]).
    val modo: ModoVoz = ModoVoz.CAUTELOSO,
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
enum class ModoVoz(val titulo: String, val explicacion: String) {
    /** Solo avisa que llegó un pago: no dice billetera, quién pagó ni monto. */
    CAUTELOSO("Cauteloso", "Solo dice que llegó un pago. No menciona billetera, nombre ni monto."),

    /** Dicta toda la notificación: la billetera, quién pagó (si se sabe) y el monto. */
    BASICO("Básico", "Dice todo: la billetera, quién pagó y cuánto. Ideal si atiendes solo."),
}

/** Convierte lo que se guardó en versiones anteriores (había un modo "Toke") al modo actual. */
object ModoVozMigracion {
    fun desdeGuardado(modo: String?, subModoToki: String?): ModoVoz = when (modo) {
        "BASICO", "NEGOCIO" -> ModoVoz.BASICO
        "TOKI" -> if (subModoToki == "NEGOCIO") ModoVoz.BASICO else ModoVoz.CAUTELOSO
        else -> ModoVoz.CAUTELOSO
    }
}
