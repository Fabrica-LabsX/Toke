package com.flabs.cobrate

import com.flabs.cobrate.core.logic.NivelConfianzaParseo
import com.flabs.cobrate.core.logic.ResultadoParseo
import java.text.Normalizer
import java.util.Locale

/**
 * Copia congelada del parser ANTES de separarlo por responsabilidades.
 * Solo se usa en pruebas para comprobar que el parser nuevo se comporta igual.
 * No tocar ni borrar hasta cerrar la separación.
 */
internal object NotificationParserLegacy {

    // Lista de palabras y frases promocionales que descartan la notificación como NoEsPago.
    private val vetoPromocional = listOf(
        "gana", "ganar", "ganate", "gane", "sorteo", "sortea", "premio", "cashback",
        "cupon", "regalo", "bono", "participa", "canjea", "obten", "aprovecha",
        "oferta", "promo", "promocion", "promociones", "descuento", "dscto",
        "%off", "% off", "hasta s/", "desde s/", "por solo s/", "solo por s/",
        "paga tu", "pagando tu", "pagar tu", "paga con", "pagando con", "solicita",
        "prestamo", "credito", "preaprobado", "pre aprobado", "tarjeta de credito",
        "invita", "refiere", "no te lo pierdas", "ultimos dias", "soat",
        "recibo de luz", "recibo de agua", "vence", "vencimiento"
    )

    // Lista de palabras/frases de seguridad o notificaciones no transaccionales.
    private val vetoSeguridad = listOf(
        "codigo de verificacion", "clave", "token", "no compartas",
        "nunca compartas", "actualiza tu app", "inicio de sesion"
    )

    // Lista de palabras/frases que indican transacciones salientes (egresos).
    private val vetoSaliente = listOf(
        "pagaste", "enviaste", "yapeaste", "transferiste", "realizaste",
        "retiraste", "compraste", "has pagado", "pago realizado",
        "pago de servicio", "consumo", "cargo", "debito", "se debito",
        "se desconto", "retiro"
    )

    // Lista de patrones regex para identificar pagos entrantes legítimos.
    private val patronesPagoEntrante = listOf(
        Regex("""\b(recibiste|has recibido|recibio)\b""", RegexOption.IGNORE_CASE),
        Regex("""\bte (envio|yapeo|transfirio|deposito|abono|pago|mando)\b""", RegexOption.IGNORE_CASE),
        Regex("""\bte (han|ha) (enviado|transferido|depositado|abonado|yapeado)\b""", RegexOption.IGNORE_CASE),
        Regex("""\bte enviaron\b""", RegexOption.IGNORE_CASE),
        Regex("""\b(pago|abono|transferencia|deposito|ingreso|yapeo) recibid[oa]\b""", RegexOption.IGNORE_CASE),
        Regex("""\bse (acredito|abono|deposito)\b""", RegexOption.IGNORE_CASE)
    )

    // Patrón de monto en soles: "S/" o "S/." seguido de un número con punto
    // o coma decimal (p. ej. "S/ 35.00", "S/35", "S/ 1,250.50").
    private val regexMonto = Regex("""S/\.?\s*([\d,]+\.?\d*)""", RegexOption.IGNORE_CASE)

    /**
     * Parsea [titulo]/[texto]/[datosAdicionales] de un evento y devuelve:
     *  - [ResultadoParseo.MontoDetectado] con confianza ALTA (PAGO REAL) si hay
     *    un monto válido, un patrón de pago entrante y no activa ningún veto.
     *  - [ResultadoParseo.NoEsPago] si la notificación es promocional (PROMO / NO PAGO)
     *    o si no cumple los criterios transaccionales entrantes (NI LO UNO NI LO OTRO).
     */
    fun parsear(
        titulo: String,
        texto: String,
        paqueteOrigen: String,
        datosAdicionales: String? = null
    ): ResultadoParseo {
        // Paso 0 — Normalización
        val contenidoUnificado = buildString {
            append(titulo)
            append(" \n ")
            append(texto)
            if (!datosAdicionales.isNullOrBlank()) {
                append(" \n ")
                append(datosAdicionales)
            }
        }

        val contenido = Normalizer.normalize(contenidoUnificado, Normalizer.Form.NFD)
            .replace(Regex("""\p{M}"""), "")
            .lowercase(Locale.ROOT)
            .replace(Regex("""\s+"""), " ")
            .trim()

        // Paso 1 — VETO PROMOCIONAL
        if (vetoPromocional.any { contienePalabra(contenido, it) }) {
            return ResultadoParseo.NoEsPago
        }

        // Paso 2 — VETO SEGURIDAD / NO TRANSACCIONAL
        if (vetoSeguridad.any { contienePalabra(contenido, it) }) {
            return ResultadoParseo.NoEsPago
        }

        // Paso 3 — VETO SALIENTE
        if (vetoSaliente.any { contienePalabra(contenido, it) }) {
            return ResultadoParseo.NoEsPago
        }

        // Paso 4 — Monto
        val matchMonto = regexMonto.find(contenido) ?: return ResultadoParseo.NoEsPago
        val monto = convertirAMonto(matchMonto.groupValues[1]) ?: return ResultadoParseo.NoEsPago

        // Paso 5 — PATRÓN DE PAGO ENTRANTE
        val esPagoEntrante = patronesPagoEntrante.any { it.containsMatchIn(contenido) }
        if (!esPagoEntrante) {
            return ResultadoParseo.NoEsPago
        }

        // Paso 6 — Veredicto
        return ResultadoParseo.MontoDetectado(monto, NivelConfianzaParseo.ALTA)
    }

    /**
     * Busca [termino] en [texto]. Si contiene '%' o '/', realiza una búsqueda directa
     * con `contains`. De lo contrario, lo busca usando límites de palabra `\b...\b`.
     */
    private fun contienePalabra(texto: String, termino: String): Boolean {
        return if (termino.contains("%") || termino.contains("/")) {
            texto.contains(termino)
        } else {
            Regex("\\b" + Regex.escape(termino) + "\\b").containsMatchIn(texto)
        }
    }

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
