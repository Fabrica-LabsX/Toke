package com.flabs.cobrate.core.logic

import io.github.jan.supabase.exceptions.RestException
import io.ktor.client.plugins.ResponseException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Tipos de feedback visual soportados por la aplicación.
 */
enum class FeedbackType {
    SUCCESS,
    ERROR,
    WARNING,
    INFO,
    OFFLINE,
    LOADING
}

/**
 * Representa un mensaje o estado de feedback para mostrar al usuario.
 */
data class FeedbackState(
    val type: FeedbackType,
    val message: String,
    val durationMillis: Long? = 4000L,
    val action: (() -> Unit)? = null,
    val actionLabel: String? = null
)

/**
 * Catálogo de códigos de ayuda TOKE de 3 dígitos (Glosario local en cliente Android).
 */
object TokeErrorCodes {
    const val INVITACION_NO_ENCONTRADA = 101
    const val INVITACION_YA_UTILIZADA = 102
    const val INVITACION_EXPIRADA = 103
    const val ERROR_VERIFICANDO_INVITACION = 104
    const val RESPUESTA_INVITACION_INVALIDA = 105
    const val ERROR_SESION_TRABAJADOR = 106
    const val SESION_TRABAJADOR_NO_DISPONIBLE = 107
    const val ERROR_ACEPTANDO_INVITACION = 108
    const val ERROR_RED_INVITACION = 109
    const val ERROR_INESPERADO_INVITACION = 110

    /** Glosario explicativo local para soporte y diagnóstico. */
    fun glosario(codigo: Int): String = when (codigo) {
        INVITACION_NO_ENCONTRADA -> "INVITACION_NO_ENCONTRADA"
        INVITACION_YA_UTILIZADA -> "INVITACION_YA_UTILIZADA"
        INVITACION_EXPIRADA -> "INVITACION_EXPIRADA"
        ERROR_VERIFICANDO_INVITACION -> "ERROR_VERIFICANDO_INVITACION"
        RESPUESTA_INVITACION_INVALIDA -> "RESPUESTA_INVITACION_INVALIDA"
        ERROR_SESION_TRABAJADOR -> "ERROR_SESION_TRABAJADOR"
        SESION_TRABAJADOR_NO_DISPONIBLE -> "SESION_TRABAJADOR_NO_DISPONIBLE"
        ERROR_ACEPTANDO_INVITACION -> "ERROR_ACEPTANDO_INVITACION"
        ERROR_RED_INVITACION -> "ERROR_RED_INVITACION"
        else -> "ERROR_INESPERADO_INVITACION"
    }
}

/**
 * Mapper para convertir errores técnicos en mensajes humanos con códigos TOKE de 3 dígitos.
 */
object ErrorMapper {
    fun translate(throwable: Throwable?): String {
        if (throwable == null) return "Ocurrió un error desconocido."
        
        // 1. Errores de Supabase Rest
        if (throwable is RestException) {
            return when (throwable.statusCode) {
                401 -> "Tu sesión expiró. Inicia sesión nuevamente."
                403 -> "No tienes permisos para realizar esta acción."
                409 -> "Este registro ya existe o entró en conflicto."
                400 -> "Los datos enviados no son válidos."
                else -> "Error del servidor (${throwable.statusCode}). Reintenta más tarde."
            }
        }
        
        // 2. Errores de Red / Ktor
        if (throwable is ResponseException) {
            return when (throwable.response.status.value) {
                401 -> "Sesión no válida o expirada."
                403 -> "Acceso denegado (403)."
                else -> "Error de conexión (${throwable.response.status.value})."
            }
        }

        val msg = throwable.message ?: ""
        return when {
            msg.contains("network", ignoreCase = true) || msg.contains("connect", ignoreCase = true) -> 
                "No pudimos conectar con el servidor. Revisa tu conexión."
            msg.contains("timeout", ignoreCase = true) -> 
                "La conexión está tardando demasiado. Inténtalo de nuevo."
            msg.contains("auth", ignoreCase = true) -> 
                "Error de autenticación."
            msg.contains("duplicate", ignoreCase = true) -> 
                "Este registro ya existe."
            else -> "Ocurrió un problema inesperado. Inténtalo nuevamente."
        }
    }

    fun translate(technicalMessage: String?): String {
        if (technicalMessage == null) return "Ocurrió un error desconocido."
        return translate(Exception(technicalMessage))
    }

    /**
     * Mapeo de errores específico para el flujo de invitaciones asociando un código TOKE de 3 dígitos.
     * Garantiza que NUNCA se expongan excepciones o cadenas de error técnicas en la UI.
     */
    fun translateInvitacionError(throwable: Throwable?, defaultCode: Int = TokeErrorCodes.ERROR_INESPERADO_INVITACION): String {
        if (throwable == null) {
            return "No pudimos procesar la invitación. Código de ayuda: $defaultCode"
        }

        val msg = throwable.message ?: ""

        // 1. Red / Transporte / Timeout real
        if (throwable is ConnectException ||
            throwable is SocketTimeoutException ||
            throwable is UnknownHostException ||
            throwable is IOException ||
            msg.contains("network", ignoreCase = true) || 
            msg.contains("connect", ignoreCase = true) || 
            msg.contains("timeout", ignoreCase = true)
        ) {
            return "Error de conexión al procesar la invitación. Código de ayuda: ${TokeErrorCodes.ERROR_RED_INVITACION}"
        }

        // 2. Excepción de Supabase REST
        if (throwable is RestException) {
            return when (throwable.statusCode) {
                401 -> "Sesión del trabajador no disponible. Código de ayuda: ${TokeErrorCodes.SESION_TRABAJADOR_NO_DISPONIBLE}"
                403 -> "No tienes permisos para esta invitación. Código de ayuda: $defaultCode"
                404 -> "Código de invitación no encontrado. Código de ayuda: ${TokeErrorCodes.INVITACION_NO_ENCONTRADA}"
                409 -> "Esta invitación ya fue utilizada. Código de ayuda: ${TokeErrorCodes.INVITACION_YA_UTILIZADA}"
                else -> "Error del servidor al verificar invitación. Código de ayuda: $defaultCode"
            }
        }

        // 3. Excepción HTTP de Ktor (Separada de transporte de red)
        if (throwable is ResponseException) {
            return when (throwable.response.status.value) {
                401 -> "Sesión de trabajador no disponible. Código de ayuda: ${TokeErrorCodes.SESION_TRABAJADOR_NO_DISPONIBLE}"
                403 -> "Acceso denegado a la invitación. Código de ayuda: $defaultCode"
                404 -> "Código de invitación no encontrado. Código de ayuda: ${TokeErrorCodes.INVITACION_NO_ENCONTRADA}"
                else -> "Error del servidor al procesar la invitación. Código de ayuda: $defaultCode"
            }
        }

        // 4. Mapeo por código por defecto (Nunca exponemos throwable.message directamente)
        return when (defaultCode) {
            TokeErrorCodes.INVITACION_NO_ENCONTRADA -> "Código de invitación no encontrado. Código de ayuda: 101"
            TokeErrorCodes.INVITACION_YA_UTILIZADA -> "Esta invitación ya fue utilizada. Código de ayuda: 102"
            TokeErrorCodes.INVITACION_EXPIRADA -> "La invitación ha expirado. Código de ayuda: 103"
            TokeErrorCodes.ERROR_VERIFICANDO_INVITACION -> "No pudimos verificar la invitación. Código de ayuda: 104"
            TokeErrorCodes.RESPUESTA_INVITACION_INVALIDA -> "Respuesta de invitación inválida. Código de ayuda: 105"
            TokeErrorCodes.ERROR_SESION_TRABAJADOR -> "Falló el intento de establecer la sesión del trabajador. Código de ayuda: 106"
            TokeErrorCodes.SESION_TRABAJADOR_NO_DISPONIBLE -> "Sesión del trabajador no disponible. Código de ayuda: 107"
            TokeErrorCodes.ERROR_ACEPTANDO_INVITACION -> "No se pudo aceptar la invitación. Código de ayuda: 108"
            else -> "Ocurrió un problema inesperado. Código de ayuda: $defaultCode"
        }
    }
}
