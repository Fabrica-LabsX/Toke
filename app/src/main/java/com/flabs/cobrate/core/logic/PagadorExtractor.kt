package com.flabs.cobrate.core.logic

/**
 * Saca de la notificación de la billetera el nombre de quien paga, tal como lo muestra la
 * billetera (por ejemplo "Juan P*" o "Ana R."). No cambia la detección del pago: solo lee el texto.
 */
object PagadorExtractor {

    // Un nombre: empieza con letra y puede llevar espacios, punto, asterisco (censura), apóstrofo y guion.
    private const val NOMBRE = """\p{L}[\p{L}\s.*'’-]{1,38}?"""
    private const val FIN = """(?=\s+(?:por|via|vía|con|en|el|al|desde)\b|\s*$)"""
    private const val MONTO = """(?:s/\.?\s*[\d.,]+|\d[\d.,]*\s*(?:soles|pen))"""
    private const val VERBO = """envi[oó]|yape[oó]|transfiri[oó]|deposit[oó]|abon[oó]|pag[oó]|mand[oó]|acredit[oó]|""" +
        """ha\s+(?:enviado|transferido|depositado|abonado|yapeado|pagado|mandado)|""" +
        """han\s+(?:enviado|transferido|depositado|abonado|yapeado|pagado|mandado)|""" +
        """enviaron|transfirieron|depositaron|abonaron|pagaron|yapearon|mandaron|acreditaron"""

    // "Juan P* te envió un pago por S/ 5", "Ana R. te yapeó S/ 10", "Luis M te transfirió S/ 8"
    private val antesDeTe = Regex("""^($NOMBRE)\s+te\s+(?:$VERBO)\b""", RegexOption.IGNORE_CASE)

    // "Te transfirieron S/ 100.00 de JUAN PEREZ", "Te enviaron S/ 30 desde Juan P."
    private val despuesDelMonto = Regex("""\b$MONTO\s+(?:de|desde)\s+($NOMBRE)$FIN""", RegexOption.IGNORE_CASE)

    // "Recibiste un Plin de Juan P. por S/ 20.00", "Has recibido S/ 10.00 de Ana R*"
    private val recibisteDe = Regex(
        """\b(?:recibiste|has\s+recibido|recibi[oó])\b.*?\b(?:de|desde)\s+($NOMBRE)$FIN""",
        RegexOption.IGNORE_CASE,
    )

    private val prefijos = Regex("""^[¡!\s]*(yape|plin|lukita|bim|tunki|agora)\s*[!¡:.\-]*\s*""", RegexOption.IGNORE_CASE)

    fun extraer(titulo: String?, texto: String?): String? {
        val cuerpo = texto.orEmpty().trim()
        val intentos = listOf(cuerpo, "${titulo.orEmpty().trim()} $cuerpo".trim())
        for (candidato in intentos) {
            val limpio = candidato.replace(prefijos, "").trim()
            for (regex in listOf(antesDeTe, despuesDelMonto, recibisteDe)) {
                regex.find(limpio)?.groupValues?.get(1)?.let { n -> validar(n)?.let { return it } }
            }
        }
        return null
    }

    private fun validar(nombre: String): String? {
        var n = nombre.replace(prefijos, "").trim().trimEnd(',', '!', ';')
        // "Juan Perez." termina una frase; "Juan P." es una inicial y se conserva.
        if (n.endsWith(".") && n.dropLast(1).substringAfterLast(' ').length >= 3) n = n.dropLast(1)
        if (n.length < 2 || n.any { it.isDigit() }) return null
        return n
    }
}
