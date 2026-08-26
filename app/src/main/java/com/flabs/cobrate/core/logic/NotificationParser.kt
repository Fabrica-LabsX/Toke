package com.flabs.cobrate.core.logic

import java.util.Locale

/**
 * Resultado del parsing de una notificación.
 */
sealed class ResultadoParseo {
    /** Se detectó un monto en soles, con un nivel de confianza asociado. */
    data class MontoDetectado(
        val monto: Double,
        val confianza: NivelConfianzaParseo
    ) : ResultadoParseo()

    /** No se detectó un patrón de monto: no parece un pago. */
    object NoEsPago : ResultadoParseo()
}

/** Nivel de confianza de la detección de monto. */
enum class NivelConfianzaParseo { ALTA, MEDIA }

/**
 * Parser/normalizador de eventos de notificación de pago (CAPA 6c).
 *
 * Función pura: recibe el título, el texto y el paquete de origen de una
 * notificación y devuelve si contiene un monto en soles (y con qué
 * confianza). No tiene acceso a Context ni a ningún Store; no genera
 * operaciones ni muta estado.
 *
 * Sin librerías de terceros: solo Regex de Kotlin.
 */
object NotificationParser {

    // Patrón de monto en soles: "S/" o "S/." seguido de un número con punto
    // o coma decimal (p. ej. "S/ 35.00", "S/35", "S/ 1,250.50").
    private val regexMonto = Regex("""S/\.?\s*([\d,]+\.?\d*)""", RegexOption.IGNORE_CASE)

    // Palabras clave que asocian el contenido a un pago recibido. Sin
    // distinguir mayúsculas/minúsculas.
    private val palabrasClavePago = listOf(
        "pago", "recibiste", "te enviaron", "abono", "yape",
        "plin", "transferencia", "recibido"
    )

    // Palabras/frases que indican contenido PROMOCIONAL y no un pago real
    // (CAPA 6c-2). Si aparecen, el contenido se descarta como NoEsPago sin
    // intentar extraer monto, incluso si por coincidencia contuviera un "S/".
    // Sin distinguir mayúsculas/minúsculas.
    private val palabrasPromocionales = listOf(
        "aprovecha", "oferta", "promoción", "descuento", "%off"
    )

    /**
     * Parsea [titulo]/[texto]/[datosAdicionales] de un evento y devuelve:
     *  - [ResultadoParseo.MontoDetectado] si hay un patrón de monto en soles,
     *    con confianza ALTA (si también hay palabra clave de pago) o MEDIA
     *    (si solo hay monto).
     *  - [ResultadoParseo.NoEsPago] si el contenido es promocional o no hay
     *    ningún patrón de monto.
     */
    fun parsear(
        titulo: String,
        texto: String,
        paqueteOrigen: String,
        datosAdicionales: String? = null
    ): ResultadoParseo {
        // [paqueteOrigen] se recibe para futuras reglas por app; la whitelist
        // se aplica ANTES en el servicio (CAPA 6c-2).

        // Se incluyen los extras enriquecidos por si el monto o la palabra
        // clave solo aparece en ellos (p. ej. el detalle completo en
        // EXTRA_BIG_TEXT / EXTRA_TEXT_LINES).
        val contenido = buildString {
            append(titulo)
            append(" \n ")
            append(texto)
            if (!datosAdicionales.isNullOrBlank()) {
                append(" \n ")
                append(datosAdicionales)
            }
        }.lowercase(Locale.ROOT)

        // FILTRO PROMOCIONAL: antes de intentar detectar un monto.
        if (palabrasPromocionales.any { contenido.contains(it) }) {
            return ResultadoParseo.NoEsPago
        }

        val matchMonto = regexMonto.find(contenido) ?: return ResultadoParseo.NoEsPago
        val monto = convertirAMonto(matchMonto.groupValues[1]) ?: return ResultadoParseo.NoEsPago

        val tienePalabraClave = palabrasClavePago.any { contenido.contains(it) }
        val confianza = if (tienePalabraClave) {
            NivelConfianzaParseo.ALTA
        } else {
            NivelConfianzaParseo.MEDIA
        }

        return ResultadoParseo.MontoDetectado(monto, confianza)
    }

    /**
     * Convierte la cadena numérica extraída (que puede incluir comas como
     * separador de miles y un punto como decimal) a [Double].
     *
     * Ejemplos: "35" -> 35.0; "35.00" -> 35.0; "1,250.50" -> 1250.5.
     * Devuelve null si la cadena no es un número válido.
     */
    private fun convertirAMonto(raw: String): Double? {
        // Elimina las comas de separación de miles: quedan solo dígitos y el
        // punto decimal (si existe).
        val normalizado = raw.replace(",", "")
        return normalizado.toDoubleOrNull()
    }
}