package com.flabs.cobrate.core.logic

/**
 * Saca de la notificación de la billetera el nombre de quien paga, tal como lo muestra la
 * billetera (por ejemplo "Juan P*" o "Ana R."). No cambia la detección del pago: solo lee el texto.
 */
object PagadorExtractor {

    // "Juan P* te envió un pago por S/ 5", "Ana R. te yapeó S/ 10", "Luis M te transfirió S/ 8"
    private val antesDeTe = Regex(
        """^(.{2,40}?)\s+te\s+(envi[oó]|yape[oó]|transfiri[oó]|deposit[oó]|abon[oó]|pag[oó]|mand[oó]|ha\s+enviado|han\s+yapeado|enviaron)\b""",
        RegexOption.IGNORE_CASE,
    )

    // "Recibiste S/ 35.00 de Juan P.", "Has recibido S/ 10 de Ana R*"
    private val despuesDeDe = Regex(
        """\b(?:recibiste|has\s+recibido|recibi[oó])\b.*?\bde\s+(.{2,40})$""",
        RegexOption.IGNORE_CASE,
    )

    private val prefijos = Regex("""^(yape|plin|lukita|bim|tunki|agora)\s*[!¡:.\-]*\s*""", RegexOption.IGNORE_CASE)

    fun extraer(titulo: String?, texto: String?): String? {
        val cuerpo = texto.orEmpty().trim()
        val intentos = listOf(cuerpo, "${titulo.orEmpty().trim()} $cuerpo".trim())
        for (candidato in intentos) {
            val limpio = candidato.replace(prefijos, "").trim()
            antesDeTe.find(limpio)?.groupValues?.get(1)?.let { return validar(it) }
            despuesDeDe.find(limpio)?.groupValues?.get(1)?.let { return validar(it) }
        }
        return null
    }

    private fun validar(nombre: String): String? {
        val n = nombre.replace(prefijos, "").trim().trimEnd(',', '!')
        if (n.isBlank() || n.any { it.isDigit() }) return null
        return n
    }
}
