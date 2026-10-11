package com.flabs.cobrate.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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

/**
 * Componente visual de avatar que muestra las iniciales dentro de un círculo.
 * Diseño TOKE! (C11).
 */
@Composable
fun AvatarIniciales(
    nombre: String,
    modifier: Modifier = Modifier,
    tamano: Dp = 40.dp
) {
    Surface(
        modifier = modifier
            .size(tamano)
            .semantics { contentDescription = nombre },
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = inicialesDe(nombre),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                maxLines = 1
            )
        }
    }
}
