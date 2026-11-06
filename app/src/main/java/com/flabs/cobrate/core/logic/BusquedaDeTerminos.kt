package com.flabs.cobrate.core.logic

/**
 * Búsqueda de un término dentro de un texto ya normalizado. La usan los tres filtros
 * (promocional, seguridad y egresos) para que busquen todos de la misma forma.
 */
internal object BusquedaDeTerminos {

    /**
     * Busca [termino] en [texto]. Si contiene '%' o '/', realiza una búsqueda directa
     * con `contains`. De lo contrario, lo busca usando límites de palabra `\b...\b`.
     */
    fun contienePalabra(texto: String, termino: String): Boolean {
        return if (termino.contains("%") || termino.contains("/")) {
            texto.contains(termino)
        } else {
            Regex("\\b" + Regex.escape(termino) + "\\b").containsMatchIn(texto)
        }
    }

    /** ¿El [texto] contiene alguno de los [terminos]? */
    fun contieneAlguno(texto: String, terminos: List<String>): Boolean =
        terminos.any { contienePalabra(texto, it) }
}
