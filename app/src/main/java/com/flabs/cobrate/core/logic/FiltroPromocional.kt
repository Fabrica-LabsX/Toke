package com.flabs.cobrate.core.logic

/**
 * Veto 1: descarta publicidad, sorteos, ofertas y avisos de cobro de servicios.
 * Si el mensaje contiene alguno de estos términos, NO es un pago recibido.
 *
 * Para agregar o quitar una palabra promocional se edita solo este archivo.
 */
internal object FiltroPromocional {

    private val terminos = listOf(
        "gana", "ganar", "ganate", "gane", "sorteo", "sortea", "premio", "cashback",
        "cupon", "regalo", "bono", "participa", "canjea", "obten", "aprovecha",
        "oferta", "promo", "promocion", "promociones", "descuento", "dscto",
        "%off", "% off", "hasta s/", "desde s/", "por solo s/", "solo por s/",
        "paga tu", "pagando tu", "pagar tu", "paga con", "pagando con", "solicita",
        "prestamo", "credito", "preaprobado", "pre aprobado", "tarjeta de credito",
        "invita", "refiere", "no te lo pierdas", "ultimos dias", "soat",
        "recibo de luz", "recibo de agua", "vence", "vencimiento"
    )

    /** [contenido] ya normalizado por [NormalizadorDeTexto]. */
    fun detecta(contenido: String): Boolean =
        BusquedaDeTerminos.contieneAlguno(contenido, terminos)
}
