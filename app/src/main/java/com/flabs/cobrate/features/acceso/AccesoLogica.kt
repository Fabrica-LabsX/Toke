package com.flabs.cobrate.features.acceso

/** Reglas de validación de los formularios de acceso. Solo lógica: sin pantallas ni red. */
object AccesoLogica {
    const val LARGO_MINIMO_PASSWORD = 6

    fun correoValido(correo: String): Boolean = com.flabs.cobrate.core.logic.EntradaSegura.correoSeguro(correo)

    /** Nombre de usuario al crear la cuenta: null si es aceptable. */
    fun errorNombreUsuario(nombre: String): String? = com.flabs.cobrate.core.logic.EntradaSegura.errorNombre(nombre, 2, 40)

    /** Mensaje de error de dos contraseñas nuevas, o null si son aceptables. */
    fun errorPasswords(password: String, confirmacion: String): String? = when {
        password.length < LARGO_MINIMO_PASSWORD -> "La contraseña debe tener al menos $LARGO_MINIMO_PASSWORD caracteres."
        password != confirmacion -> "Las contraseñas no coinciden."
        else -> null
    }

    /** Nombre del dueño que se muestra en la invitación: el visible, o el usuario sin el dominio. */
    fun nombreDueno(visible: String?, usuario: String?): String {
        val nombre = visible?.trim()
        val user = usuario?.trim()
        return when {
            !nombre.isNullOrBlank() -> nombre
            !user.isNullOrBlank() -> user.substringBefore("@")
            else -> "El dueño"
        }
    }
}
