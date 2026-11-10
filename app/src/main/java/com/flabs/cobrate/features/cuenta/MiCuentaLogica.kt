package com.flabs.cobrate.features.cuenta

import com.flabs.cobrate.features.acceso.AccesoLogica

/** Tipo real de cuenta: los trabajadores entran con sesión anónima y no tienen correo ni contraseña. */
enum class TipoCuenta { CON_CORREO, ANONIMA }

enum class ErrorCorreo { VACIO, INVALIDO, IGUAL_AL_ACTUAL }

/** Reglas puras de la pantalla Mi cuenta. */
object MiCuentaLogica {
    fun tipo(correo: String?): TipoCuenta = if (correo.isNullOrBlank()) TipoCuenta.ANONIMA else TipoCuenta.CON_CORREO

    /** null si el correo nuevo es aceptable. */
    fun errorCorreoNuevo(nuevo: String, actual: String?): ErrorCorreo? {
        val n = nuevo.trim()
        return when {
            n.isEmpty() -> ErrorCorreo.VACIO
            !AccesoLogica.correoValido(n) || !n.substringAfter("@").contains(".") || n.contains(" ") -> ErrorCorreo.INVALIDO
            n.equals(actual?.trim(), ignoreCase = true) -> ErrorCorreo.IGUAL_AL_ACTUAL
            else -> null
        }
    }

    /** Cambiar contraseña: exige la actual (reautenticación) y reutiliza la regla de contraseñas nuevas del acceso. */
    fun errorCambioPassword(actual: String, nueva: String, confirmacion: String): String? = when {
        actual.isBlank() -> "Escribe tu contraseña actual."
        nueva == actual -> "La contraseña nueva debe ser distinta a la actual."
        else -> AccesoLogica.errorPasswords(nueva, confirmacion)
    }

    const val FRASE_ELIMINAR = "ELIMINAR"

    /** Eliminar: cuentas con correo confirman con su contraseña; las anónimas escribiendo la frase. */
    fun puedeConfirmarEliminacion(tipo: TipoCuenta, password: String, frase: String): Boolean = when (tipo) {
        TipoCuenta.CON_CORREO -> password.isNotBlank()
        TipoCuenta.ANONIMA -> frase.trim().equals(FRASE_ELIMINAR, ignoreCase = true)
    }
}

/** Resultado de pedir la eliminación al servidor. */
sealed interface ResultadoEliminacion {
    data object Eliminada : ResultadoEliminacion
    data object ContrasenaIncorrecta : ResultadoEliminacion
    data class TieneNegocios(val cantidad: Int) : ResultadoEliminacion
    data object SinRed : ResultadoEliminacion
    data object ErrorServidor : ResultadoEliminacion

    companion object {
        /** Traduce la respuesta HTTP de la Edge Function eliminar-cuenta. */
        fun desdeRespuesta(codigo: Int, cuerpo: String): ResultadoEliminacion = when {
            codigo == 200 -> Eliminada
            codigo == 403 -> ContrasenaIncorrecta
            codigo == 409 && cuerpo.contains("HAS_BUSINESSES") ->
                TieneNegocios(Regex("\"negocios\"\\s*:\\s*(\\d+)").find(cuerpo)?.groupValues?.get(1)?.toIntOrNull() ?: 1)
            else -> ErrorServidor
        }
    }
}
