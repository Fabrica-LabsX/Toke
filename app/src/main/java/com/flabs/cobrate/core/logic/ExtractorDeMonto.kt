package com.flabs.cobrate.core.logic

/**
 * Extrae el monto en soles de un texto ya normalizado.
 *
 * Solo lee el número. No decide si el mensaje es un pago.
 */
internal object ExtractorDeMonto {

    // Patrón de monto en soles: "S/" o "S/." seguido de un número con punto
    // o coma decimal (p. ej. "S/ 35.00", "S/35", "S/ 1,250.50").
    private val regexMonto = Regex("""S/\.?\s*([\d,]+\.?\d*)""", RegexOption.IGNORE_CASE)

    /** Devuelve el monto, o null si no hay un monto en soles válido. */
    fun extraer(contenido: String): Double? {
        val crudo = regexMonto.find(contenido)?.groupValues?.get(1)
            ?: regexSoles.find(contenido)?.groupValues?.get(1)
            ?: return null
        return convertirAMonto(crudo)
    }

    // Algunas billeteras escriben "22.00 soles" o "22.00 PEN" en vez de "S/ 22.00".
    private val regexSoles = Regex("""\b(\d[\d,]*(?:\.\d+)?)\s*(?:soles|pen)\b""", RegexOption.IGNORE_CASE)

    /**
     * Convierte la cadena numérica extraída (que puede incluir comas como
     * separador de miles y un punto como decimal) a [Double].
     *
     * Ejemplos: "35" -> 35.0; "35.00" -> 35.0; "1,250.50" -> 1250.5.
     * Devuelve null si la cadena no es un número válido.
     */
    private fun convertirAMonto(raw: String): Double? {
        val normalizado = raw.replace(",", "")
        return normalizado.toDoubleOrNull()
    }
}
