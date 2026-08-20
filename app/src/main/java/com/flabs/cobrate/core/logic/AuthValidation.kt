package com.flabs.cobrate.core.logic

import java.security.MessageDigest

/**
 * Validaciones y utilidades puras de autenticación local (CAPA 2).
 *
 * Funciones puras: no acceden a Context ni a Stores; solo reciben datos y
 * devuelven datos, para poder probarse de forma aislada.
 *
 * IMPORTANTE (seguridad): esto NO es un sistema de seguridad de producción.
 * El hash vía SHA-256 sin sal ni iteraciones es una protección MÍNIMA para
 * desarrollo local (evitar guardar la contraseña en texto plano en disco).
 * La seguridad real (hash con sal, KDF como argon2/bcrypt, etc.) es CAPA 24
 * y queda fuera del alcance de V1.
 */
object AuthValidation {

    /** Longitud mínima de contraseña para esta capa (desarrollo local). */
    const val MIN_LONGITUD_PASSWORD = 6

    /** Longitud mínima de username. */
    private const val MIN_LONGITUD_USERNAME = 3

    /**
     * Un username es válido si no está vacío, no contiene espacios y tiene una
     * longitud mínima razonable.
     */
    fun usernameValido(username: String): Boolean =
        username.isNotBlank() &&
            !username.contains(" ") &&
            username.length >= MIN_LONGITUD_USERNAME

    /** Una contraseña es válida si alcanza la longitud mínima razonable. */
    fun passwordValida(password: String): Boolean =
        password.length >= MIN_LONGITUD_PASSWORD

    /**
     * Calcula el hash SHA-256 (hexadecimal) de [password].
     *
     * Protección MÍNIMA para no guardar la contraseña en texto plano en disco
     * durante el desarrollo local. No es un estándar de seguridad de producción
     * (sin sal ni iteraciones) — ver nota de clase.
     */
    fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256")
            .digest(password.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}