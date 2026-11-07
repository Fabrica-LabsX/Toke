package com.flabs.cobrate.ui.theme

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

/** Las alturas de botones salen de TokeMedidas; no se fijan números sueltos de 46 a 56 dp. */
class MedidasBotonesTest {
    private val prohibidas = Regex("""\.height\((46|52|54|56)\.dp\)""")

    @Test
    fun noHayAlturasDeBotonSueltas() {
        val raiz = File("src/main/java")
        assertTrue("No se encontró ${raiz.absolutePath}", raiz.isDirectory)
        val sueltas = raiz.walkTopDown()
            .filter { it.extension == "kt" && it.name != "TokeMedidas.kt" }
            .flatMap { f -> f.readLines().withIndex().filter { prohibidas.containsMatchIn(it.value) }.map { "${f.name}:${it.index + 1}" } }
            .toList()
        assertTrue("Alturas fuera de TokeMedidas: $sueltas", sueltas.isEmpty())
    }
}
