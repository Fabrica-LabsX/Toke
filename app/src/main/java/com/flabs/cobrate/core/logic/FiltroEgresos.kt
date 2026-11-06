package com.flabs.cobrate.core.logic

/**
 * Veto 3: descarta transacciones salientes (cuando el usuario paga, envía o le
 * debitan dinero). Solo importan los ingresos.
 *
 * Para agregar o quitar una frase de egreso se edita solo este archivo.
 */
internal object FiltroEgresos {

    private val terminos = listOf(
        "pagaste", "enviaste", "yapeaste", "transferiste", "realizaste",
        "retiraste", "compraste", "has pagado", "pago realizado",
        "pago de servicio", "consumo", "cargo", "debito", "se debito",
        "se desconto", "retiro"
    )

    /** [contenido] ya normalizado por [NormalizadorDeTexto]. */
    fun detecta(contenido: String): Boolean =
        BusquedaDeTerminos.contieneAlguno(contenido, terminos)
}
