package com.flabs.cobrate.features.acceso

/** Reglas de validación de los formularios de acceso. Solo lógica: sin pantallas ni red. */
object AccesoLogica {
    const val LARGO_MINIMO_PASSWORD = 6

    fun correoValido(correo: String): Boolean = correo.isNotBlank() && correo.contains("@")

    /** Mensaje de error de dos contraseñas nuevas, o null si son aceptables. */
    fun errorPasswords(password: String, confirmacion: String): String? = when {
        password.length < LARGO_MINIMO_PASSWORD -> "La contraseña debe tener al menos $LARGO_MINIMO_PASSWORD caracteres."
        password != confirmacion -> "Las contraseñas no coinciden."
        else -> null
    }
}
