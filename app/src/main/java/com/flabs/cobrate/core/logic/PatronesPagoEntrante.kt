package com.flabs.cobrate.core.logic

/**
 * Paso final: confirma que el mensaje dice que el usuario RECIBIÓ dinero
 * ("recibiste", "te yapeó", "se acreditó", etc.).
 *
 * Para reconocer una frase nueva de pago entrante se edita solo este archivo.
 */
internal object PatronesPagoEntrante {

    private val patrones = listOf(
        Regex("""\b(recibiste|has recibido|recibio)\b""", RegexOption.IGNORE_CASE),
        Regex("""\bte (envio|yapeo|transfirio|deposito|abono|pago|mando)\b""", RegexOption.IGNORE_CASE),
        Regex("""\bte (han|ha) (enviado|transferido|depositado|abonado|yapeado)\b""", RegexOption.IGNORE_CASE),
        Regex("""\bte enviaron\b""", RegexOption.IGNORE_CASE),
        Regex("""\b(pago|abono|transferencia|deposito|ingreso|yapeo) recibid[oa]\b""", RegexOption.IGNORE_CASE),
        Regex("""\bse (acredito|abono|deposito)\b""", RegexOption.IGNORE_CASE)
    )

    /** [contenido] ya normalizado por [NormalizadorDeTexto]. */
    fun coincide(contenido: String): Boolean =
        patrones.any { it.containsMatchIn(contenido) }
}
