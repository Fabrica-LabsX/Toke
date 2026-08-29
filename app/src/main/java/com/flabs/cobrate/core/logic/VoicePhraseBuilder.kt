package com.flabs.cobrate.core.logic

import com.flabs.cobrate.core.model.ConfiguracionDeVoz
import com.flabs.cobrate.core.model.ModoVoz
import com.flabs.cobrate.core.model.SubModoToki
import java.util.Locale

/**
 * Construye la frase de voz que se anuncia al confirmarse un pago (CAPA 14).
 *
 * Función pura: no depende de Android ni de estado global, solo de la
 * configuración y el monto, lo que la hace trivial de probar en unit tests.
 *
 * Regla de producto (3 modos):
 * - CAUTELOSO: "Pago confirmado de {cliente}." (NUNCA menciona el monto).
 * - NEGOCIO: "Pago confirmado de {cliente} por {monto} soles."
 * - TOKI: "¡TOKE! {cliente}, pago confirmado." + si subModoToki = NEGOCIO se
 *   agrega el monto; si es DISCRETO (o null) no se agrega.
 *
 * [cliente] es el nombre del cliente. Limitación conocida: el modelo
 * [com.flabs.cobrate.core.model.Operacion] no tiene aún el nombre del cliente,
 * por lo que la integración real usará "un cliente" hasta que exista ese campo.
 */
object VoicePhraseBuilder {

    /**
     * Construye la frase a pronunciar.
     *
     * @param config configuración de voz vigente.
     * @param monto monto del pago en soles.
     * @param cliente nombre del cliente; si es null/blanko se usa "un cliente".
     */
    fun construirFrase(
        config: ConfiguracionDeVoz,
        monto: Double,
        cliente: String? = null
    ): String {
        val nombre = cliente?.takeIf { it.isNotBlank() }?.trim() ?: "un cliente"
        val montoStr = formatearMonto(monto)

        return when (config.modo) {
            ModoVoz.CAUTELOSO ->
                "Pago confirmado de $nombre."

            ModoVoz.NEGOCIO ->
                "Pago confirmado de $nombre por $montoStr soles."

            ModoVoz.TOKI -> {
                val conMonto = config.subModoToki == SubModoToki.NEGOCIO
                val montoPart = if (conMonto) " por $montoStr soles" else ""
                "¡TOKE! $nombre, pago confirmado$montoPart."
            }
        }
    }

    /**
     * Formatea un monto en soles sin ceros finales innecesarios, p. ej.
     * 25.0 -> "25", 25.5 -> "25.5", 25.55 -> "25.55".
     */
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