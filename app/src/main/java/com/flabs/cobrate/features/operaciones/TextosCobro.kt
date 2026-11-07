package com.flabs.cobrate.features.operaciones

/**
 * Textos de un cobro cuando la billetera no entrega datos. Un solo lugar para que Home,
 * Operaciones y el detalle digan lo mismo y nunca muestren palabras técnicas ("Desconocido",
 * "Cliente Toke", códigos de operación).
 */
object TextosCobro {
    /** Título de una fila de cobro. El trabajador y la hora van en la línea de abajo. */
    const val TITULO = "Pago recibido"

    /** Título de la fila: el nombre que mostró la billetera ("Juan P*") o, si no vino, [TITULO]. */
    fun titulo(pagador: String?): String = pagador?.trim()?.takeIf { it.isNotEmpty() } ?: TITULO

    /** Pagador cuando el aviso de la billetera no trae su nombre. */
    const val PAGADOR_SIN_NOMBRE = "No indicado"
}
