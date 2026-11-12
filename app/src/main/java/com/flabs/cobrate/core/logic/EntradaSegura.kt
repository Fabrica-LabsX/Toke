package com.flabs.cobrate.core.logic

/**
 * Filtro de texto que escribe el usuario (nombres, correo, números, titulares).
 *
 * Ojo: la defensa real contra inyección SQL está en el servidor. La app solo habla con Supabase por
 * PostgREST/RPC con parámetros, así que el texto nunca se pega dentro de una consulta. Este filtro es una
 * segunda capa: limpia caracteres invisibles o de control y rechaza lo que claramente es un ataque,
 * sin bloquear nombres reales como "Pollería D'Elia" o "Bodega 3-B".
 */
object EntradaSegura {
    private val CONTROL_O_INVISIBLE = Regex("[\\p{Cc}\\p{Cf}\\u2028\\u2029]")
    private val ESPACIOS = Regex("\\s+")

    private val PATRONES = listOf(
        Regex("(?i)\\bunion\\b.{0,20}\\bselect\\b"),
        Regex("(?i)\\b(drop|alter|truncate)\\s+(table|database|schema|function)\\b"),
        Regex("(?i)\\b(insert\\s+into|delete\\s+from|update\\s+\\w+\\s+set)\\b"),
        Regex("(?i)['\"]\\s*(or|and)\\s+['\"\\d(]"),
        Regex("(?i)\\b(or|and)\\s+\\d+\\s*=\\s*\\d+"),
        Regex("(?i);\\s*(select|drop|insert|update|delete|alter|exec)\\b"),
        Regex("--\\s*$"),
        Regex("/\\*|\\*/"),
        Regex("(?i)\\b(xp_cmdshell|pg_sleep|information_schema|pg_catalog)\\b"),
        Regex("(?i)<\\s*/?\\s*(script|iframe|img|svg|object)\\b"),
        Regex("(?i)javascript\\s*:"),
    )

    /** Quita caracteres de control e invisibles, junta espacios, recorta y limita el largo. */
    fun limpiar(texto: String, max: Int = 80): String =
        texto.replace(CONTROL_O_INVISIBLE, "").replace(ESPACIOS, " ").trim().take(max)

    /** true si el texto parece un intento de inyección (SQL o HTML/script). */
    fun sospechosa(texto: String): Boolean = PATRONES.any { it.containsMatchIn(texto) }

    /** Mensaje de error para un nombre libre (persona, negocio, titular), o null si es aceptable. */
    fun errorNombre(texto: String, min: Int, max: Int): String? {
        val t = limpiar(texto, Int.MAX_VALUE)
        return when {
            t.length < min -> "Escribe al menos $min letras."
            t.length > max -> "Usa como máximo $max caracteres."
            sospechosa(t) -> "Hay caracteres que no están permitidos."
            else -> null
        }
    }

    private val CORREO = Regex("^[A-Za-z0-9._%+\\-]{1,64}@[A-Za-z0-9\\-]+(\\.[A-Za-z0-9\\-]+)*\\.[A-Za-z]{2,}$")

    /** Correo con forma real: sin espacios, comillas, punto y coma ni otros símbolos de ataque. */
    fun correoSeguro(correo: String): Boolean = correo.trim().let { it.length <= 254 && CORREO.matches(it) }

    private val DATO_COBRO = Regex("^[0-9A-Za-z .\\-+/@_]{4,40}$")

    /** Número de celular, cuenta o alias donde se recibe un pago. */
    fun datoDeCobroSeguro(dato: String): Boolean = limpiar(dato, 200).let { DATO_COBRO.matches(it) && !sospechosa(it) }
}
