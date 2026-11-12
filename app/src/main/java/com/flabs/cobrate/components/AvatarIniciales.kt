package com.flabs.cobrate.components

import java.text.Normalizer
import java.util.Locale

/**
 * Función pura que deriva las iniciales a partir de un nombre o identificador.
 * Sigue la regla R1: Inicial del apellido (último fragmento) + Inicial del nombre (primer fragmento).
 *
 * Casos de prueba:
 * | Entrada                     | Salida |
 * |-----------------------------|--------|
 * | "Angel Zamora"              | ZA     |
 * | "angel zamora"              | ZA     |
 * | "Angel de la Cruz Zamora"   | ZA     |
 * | "angel.zamora@gmail.com"    | ZA     |
 * | "angelzamora"               | AN     |
 * | "A"                         | A      |
 * | "  "                        | ?      |
 * | "123"                       | ?      |
 */
fun inicialesDe(nombre: String?): String {
    if (nombre.isNullOrBlank()) return "?"

    // 1. Manejo de correos: quedarse con la parte anterior al @
    var texto = nombre.split("@").first()

    // 2. Reemplazar separadores por espacios
    texto = texto.replace(".", " ")
        .replace("_", " ")
        .replace("-", " ")
        .replace("+", " ")

    // 3. Quitar dígitos
    texto = texto.replace(Regex("\\d"), "")

    // 4. Normalizar acentos
    texto = Normalizer.normalize(texto, Normalizer.Form.NFD)
    texto = texto.replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")

    // 5. Partir por espacios y filtrar vacíos
    val fragmentos = texto.split(" ").filter { it.isNotBlank() }

    return when {
        fragmentos.size >= 2 -> {
            // Regla R1: Inicial del último + Inicial del primero
            val primera = fragmentos.first().first().toString()
            val ultima = fragmentos.last().first().toString()
            (ultima + primera).uppercase(Locale("es"))
        }
        fragmentos.size == 1 -> {
            val f = fragmentos.first()
            if (f.length >= 2) {
                f.substring(0, 2).uppercase(Locale("es"))
            } else {
                f.uppercase(Locale("es"))
            }
        }
        else -> "?"
    }
}
