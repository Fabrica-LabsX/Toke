package com.flabs.cobrate.core.logic

import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.exceptions.RestException
import io.ktor.client.plugins.ResponseException
import java.io.IOException

object AuthErrorUtils {

    /**
     * Determina si una excepción ocurrida durante el refresco o autenticación
     * se debe a un problema real de red/conectividad (p. ej., offline, timeout, DNS),
     * o a un rechazo técnico del servidor (p. ej., 400 refresh_token_not_found).
     */
    fun esErrorDeRed(cause: Any?): Boolean {
        if (cause == null) return false

        if ((cause is AuthRestException) ||
            (cause is RestException) ||
            (cause is ResponseException)
        ) {
            return false
        }

        if (cause is IOException) return true

        val str = cause.toString().lowercase()

        if (str.contains("authrestexception") ||
            str.contains("restexception") ||
            str.contains("refresh_token_not_found") ||
            str.contains("invalid_grant")
        ) {
            return false
        }

        return str.contains("network") ||
               str.contains("connect") ||
               str.contains("timeout") ||
               str.contains("host") ||
               str.contains("offline") ||
               str.contains("unreachable") ||
               str.contains("socket") ||
               str.contains("unknownhost") ||
               str.contains("ioexception")
    }
}
