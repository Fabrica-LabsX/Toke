package com.flabs.cobrate

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Guardia de regresión: un texto largo con `maxLines = 1` se corta con "…" ("Estás seg…").
 * Revisa el código fuente: ningún `Text` de una sola línea puede llevar un texto de más de [MAX] letras,
 * sea literal o un recurso de strings.xml. Los textos largos deben poder partirse en varias líneas.
 */
class TextoNoSeCortaTest {
    private val MAX = 28
    private val raiz = File("src/main").takeIf { it.exists() } ?: File("app/src/main")

    private fun strings(): Map<String, String> =
        Regex("<string name=\"([^\"]+)\"[^>]*>(.*?)</string>", RegexOption.DOT_MATCHES_ALL)
            .findAll(File(raiz, "res/values/strings.xml").readText())
            .associate { it.groupValues[1] to it.groupValues[2] }

    @Test
    fun ningunTextoLargoVaEnUnaSolaLinea() {
        val textos = strings()
        val problemas = mutableListOf<String>()
        raiz.walkTopDown().filter { it.extension == "kt" }.forEach { archivo ->
            val src = archivo.readText()
            Regex("maxLines = 1").findAll(src).forEach { m ->
                val tramo = src.substring(maxOf(0, m.range.first - 500), m.range.first)
                val llamada = tramo.substring(maxOf(0, tramo.lastIndexOf("Text(")))
                Regex("R\\.string\\.(\\w+)").findAll(llamada).forEach { r ->
                    val v = textos[r.groupValues[1]].orEmpty()
                    if (v.length > MAX) problemas += "${archivo.name}: ${r.groupValues[1]} (${v.length} letras)"
                }
                Regex("\"([^\"$\\\\\\n]{${MAX + 1},})\"").findAll(llamada).forEach { l ->
                    problemas += "${archivo.name}: \"${l.groupValues[1].take(40)}…\""
                }
            }
        }
        assertTrue("Textos largos en una sola línea:\n" + problemas.joinToString("\n"), problemas.isEmpty())
    }
}
