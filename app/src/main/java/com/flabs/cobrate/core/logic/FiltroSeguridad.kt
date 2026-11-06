package com.flabs.cobrate.core.logic

/**
 * Veto 2: descarta avisos de seguridad o no transaccionales (códigos, claves,
 * inicio de sesión, actualizaciones). Nunca son un pago recibido.
 *
 * Para agregar o quitar un término de seguridad se edita solo este archivo.
 */
internal object FiltroSeguridad {

    private val terminos = listOf(
        "codigo de verificacion", "clave", "token", "no compartas",
        "nunca compartas", "actualiza tu app", "inicio de sesion"
    )

    /** [contenido] ya normalizado por [NormalizadorDeTexto]. */
    fun detecta(contenido: String): Boolean =
        BusquedaDeTerminos.contieneAlguno(contenido, terminos)
}
