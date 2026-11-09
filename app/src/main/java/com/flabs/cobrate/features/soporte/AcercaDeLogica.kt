package com.flabs.cobrate.features.soporte

/** Textos de "Acerca de y soporte" que se arman con datos: se pueden probar sin pantalla. */
object AcercaDeLogica {
    /** "Versión 1.0 (12)" o "Versión 1.0" si no se conoce la compilación. */
    fun textoVersion(nombre: String?, compilacion: Long?): String = when {
        nombre.isNullOrBlank() -> "Versión no disponible"
        compilacion == null || compilacion <= 0L -> "Versión $nombre"
        else -> "Versión $nombre ($compilacion)"
    }

    /** Enlace de WhatsApp para pedir ayuda: deja solo los dígitos del número. */
    fun enlaceSoporte(telefono: String, version: String): String =
        "https://wa.me/${telefono.filter { it.isDigit() }}?text=" +
            java.net.URLEncoder.encode("Hola, necesito ayuda con TOKE. ($version)", "UTF-8").replace("+", "%20")
}
