package com.flabs.cobrate.ui.theme

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

/** Los colores salen solo de TokePaleta.kt; los de cada billetera viven en BilleteraColores.kt. */
class PaletaUnicaTest {
    private val permitidos = setOf("TokePaleta.kt", "BilleteraColores.kt")

    @Test
    fun noHayColoresHexSueltosFueraDeLaPaleta() {
        val raiz = File("src/main/java")
        assertTrue("No se encontró ${raiz.absolutePath}", raiz.isDirectory)
        val sueltos = raiz.walkTopDown()
            .filter { it.extension == "kt" && it.name !in permitidos }
            .flatMap { f -> f.readLines().withIndex().filter { "Color(0x" in it.value }.map { "${f.name}:${it.index + 1}" } }
            .toList()
        assertTrue("Colores fuera de la paleta: $sueltos", sueltos.isEmpty())
    }
}
