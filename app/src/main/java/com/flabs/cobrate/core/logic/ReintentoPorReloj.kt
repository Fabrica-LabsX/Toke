package com.flabs.cobrate.core.logic

import kotlinx.coroutines.delay

/**
 * Si el servidor rechaza el token por desfase de reloj (PGRST303 / "JWT issued at future"),
 * espera y reintenta UNA vez (CAPA 31.2).
 */
suspend fun <T> conReintentoPorReloj(esperaMs: Long = 3000, bloque: suspend () -> Result<T>): Result<T> {
    val primerResultado = bloque()
    if (primerResultado.isSuccess) {
        return primerResultado
    }

    val error = primerResultado.exceptionOrNull()
    val mensaje = error?.message ?: ""
    val esErrorReloj = mensaje.contains("PGRST303", ignoreCase = true) ||
            mensaje.contains("JWT issued at future", ignoreCase = true)

    if (esErrorReloj) {
        LogSeguro.w("TokeReloj", "Desfase de reloj detectado (PGRST303). Reintentando en ${esperaMs}ms...", error)
        delay(esperaMs)
        return bloque()
    }

    return primerResultado
}
