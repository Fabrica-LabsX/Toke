package com.flabs.cobrate.core.logic

import java.text.Normalizer
import java.util.Locale

/**
 * Paso 0 del parseo de notificaciones: une título, texto y datos extra en un solo
 * texto comparable (sin tildes, en minúsculas y con espacios simples).
 *
 * Solo prepara el texto. No decide si es pago ni si es promoción.
 */
internal object NormalizadorDeTexto {

    fun unificar(titulo: String, texto: String, datosAdicionales: String?): String {
        val contenidoUnificado = buildString {
            append(titulo)
            append(" \n ")
            append(texto)
            if (!datosAdicionales.isNullOrBlank()) {
                append(" \n ")
                append(datosAdicionales)
            }
        }

        return Normalizer.normalize(contenidoUnificado, Normalizer.Form.NFD)
            .replace(Regex("""\p{M}"""), "")
            .lowercase(Locale.ROOT)
            .replace(Regex("""\s+"""), " ")
            .trim()
    }
}
