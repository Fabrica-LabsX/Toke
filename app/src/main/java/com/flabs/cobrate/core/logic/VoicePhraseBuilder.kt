package com.flabs.cobrate.core.logic

import com.flabs.cobrate.core.model.ConfiguracionDeVoz
import com.flabs.cobrate.core.model.ModoVoz
import java.util.Locale

/**
 * Construye la frase de voz que se anuncia al confirmarse un pago.
 *
 * Función pura: no depende de Android ni de estado global.
 *
 * Dos modos:
 * - CAUTELOSO: "Pago confirmado." No menciona billetera, quién pagó ni monto.
 * - BASICO: dicta todo lo que se sabe, p. ej. "Pago confirmado en Yape de Ana, 25.50 soles."
 *   Si no se conoce quién pagó, esa parte se omite.
 */
object VoicePhraseBuilder {

    /**
     * @param config configuración de voz vigente.
     * @param monto monto del pago en soles.
     * @param cliente quién pagó, si se sabe (el modelo de operaciones todavía no lo guarda).
     * @param billetera billetera o banco por el que llegó el pago, si se sabe.
     */
    fun construirFrase(
        config: ConfiguracionDeVoz,
        monto: Double,
        cliente: String? = null,
        billetera: String? = null
    ): String = when (config.modo) {
        ModoVoz.CAUTELOSO -> "Pago confirmado."
        ModoVoz.BASICO -> {
            val donde = billetera?.takeIf { it.isNotBlank() }?.trim()?.let { " en $it" } ?: ""
            val quien = cliente?.takeIf { it.isNotBlank() }?.trim()?.let { " de $it" } ?: ""
            "Pago confirmado$donde$quien, ${formatearMonto(monto)} soles."
        }
    }

    /** 25.0 -> "25", 25.5 -> "25.5", 25.55 -> "25.55". */
    private fun formatearMonto(monto: Double): String {
        val redondeado = Math.round(monto * 100.0) / 100.0
        return if (redondeado == redondeado.toLong().toDouble()) {
            redondeado.toLong().toString()
        } else {
            String.format(Locale.US, "%.2f", redondeado)
                .trimEnd('0')
                .trimEnd('.')
        }
    }
}
