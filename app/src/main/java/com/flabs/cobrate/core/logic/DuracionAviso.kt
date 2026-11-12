package com.flabs.cobrate.core.logic

/** Cuánto tiempo se deja un aviso en pantalla: lo justo para leerlo, sin pedir que lo cierren. */
object DuracionAviso {
    const val MIN_MS = 2500L
    const val MAX_MS = 6000L
    private const val MS_POR_LETRA = 45L

    /** El mayor entre lo pedido y lo que toma leer el texto, siempre entre [MIN_MS] y [MAX_MS]. */
    fun milis(mensaje: String, pedida: Long?): Long {
        val porTexto = MIN_MS + mensaje.length * MS_POR_LETRA
        return maxOf(porTexto, pedida ?: 0L).coerceIn(MIN_MS, MAX_MS)
    }
}
