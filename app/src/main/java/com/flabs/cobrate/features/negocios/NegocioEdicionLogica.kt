package com.flabs.cobrate.features.negocios

/** Reglas para editar un negocio y para el tope de trabajadores. Solo lógica: sin pantallas ni red. */
object NegocioEdicionLogica {
    const val LARGO_MINIMO_NOMBRE = 2
    const val MAX_TRABAJADORES_PERMITIDO = 500

    /** Mensaje de error del nombre, o null si es válido. */
    fun errorNombre(nombre: String): String? =
        if (nombre.trim().length < LARGO_MINIMO_NOMBRE) "El nombre debe tener al menos $LARGO_MINIMO_NOMBRE letras." else null

    /** Texto vacío = sin límite (null). Devuelve error si no es un entero entre 1 y 500. */
    fun leerMax(texto: String): Pair<Int?, String?> {
        val limpio = texto.trim()
        if (limpio.isEmpty()) return null to null
        val n = limpio.toIntOrNull()
        return if (n == null || n !in 1..MAX_TRABAJADORES_PERMITIDO) {
            null to "Escribe un número entre 1 y $MAX_TRABAJADORES_PERMITIDO, o déjalo vacío para no poner límite."
        } else {
            n to null
        }
    }

    /** ¿Se puede invitar a otra persona? Sin tope, siempre. */
    fun puedeInvitar(max: Int?, actuales: Int): Boolean = max == null || actuales < max
}
