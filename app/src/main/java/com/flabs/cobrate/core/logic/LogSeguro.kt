package com.flabs.cobrate.core.logic

import android.util.Log
import com.flabs.cobrate.BuildConfig

/**
 * Utilidad de logging seguro que desinfecta mensajes de excepción para prevenir
 * la fuga de tokens de autenticación o claves en Logcat (CAPA 31).
 */
object LogSeguro {

    /** Quita secretos de un texto: tokens JWT, 'Bearer ...', apikey y el bloque 'Headers: [...]'. */
    internal fun sanitizar(texto: String?): String {
        if (texto.isNullOrEmpty()) return ""

        var resultado = texto

        // 1. Elimina todo desde Headers: [ hasta el final de esa línea o cierre y reemplázalo por Headers: [omitidos]
        resultado = resultado.replace(Regex("""Headers:\s*\[[^]\n]*\]?""", RegexOption.IGNORE_CASE), "Headers: [omitidos]")

        // 2. Reemplaza Bearer\s+\S+ por Bearer ***
        resultado = resultado.replace(Regex("""Bearer\s+\S+""", RegexOption.IGNORE_CASE), "Bearer ***")

        // 3. Reemplaza cualquier texto con forma JWT (eyJ[\w-]+\.[\w-]+\.[\w-]*) por ***JWT***
        resultado = resultado.replace(Regex("""eyJ[\w-]+\.[\w-]+\.[\w-]*"""), "***JWT***")

        // 4. Reemplaza apikey=\[?[^\],\s]+\]? por apikey=*** y sb_publishable_\w+ / sb_secret_\w+ por ***
        resultado = resultado.replace(Regex("""apikey=\[?[^\],\s]+\]?""", RegexOption.IGNORE_CASE), "apikey=***")
        resultado = resultado.replace(Regex("""sb_(publishable|secret)_\w+""", RegexOption.IGNORE_CASE), "***")

        // 5. Recorta el resultado a 300 caracteres
        if (resultado.length > 300) {
            resultado = resultado.substring(0, 300)
        }

        return resultado
    }

    /** El mensaje de una excepción puede traer datos del usuario (filas, textos): solo se imprime en la versión de depuración. */
    private fun detalle(error: Throwable): String = if (BuildConfig.DEBUG) ": ${sanitizar(error.message)}" else ""

    fun e(tag: String, mensaje: String, error: Throwable? = null) {
        val errSimple = if (error != null) "${error.javaClass.simpleName}${detalle(error)}" else ""
        val textoFinal = if (errSimple.isNotEmpty()) "$mensaje | $errSimple" else mensaje
        try {
            Log.e(tag, textoFinal)
        } catch (e: Throwable) {
            println("[$tag] ERROR: $textoFinal")
        }
    }

    fun w(tag: String, mensaje: String, error: Throwable? = null) {
        val errSimple = if (error != null) "${error.javaClass.simpleName}${detalle(error)}" else ""
        val textoFinal = if (errSimple.isNotEmpty()) "$mensaje | $errSimple" else mensaje
        try {
            Log.w(tag, textoFinal)
        } catch (e: Throwable) {
            println("[$tag] WARN: $textoFinal")
        }
    }

    fun d(tag: String, mensaje: String, error: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            val errSimple = if (error != null) "${error.javaClass.simpleName}${detalle(error)}" else ""
            val textoFinal = if (errSimple.isNotEmpty()) "$mensaje | $errSimple" else mensaje
            try {
                Log.d(tag, textoFinal)
            } catch (e: Throwable) {
                println("[$tag] DEBUG: $textoFinal")
            }
        }
    }
}
