package com.flabs.cobrate

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Guardia de privacidad (PRI-6 y SEG-3), revisa el código fuente:
 *  - Nadie llama a `android.util.Log` directamente: todo pasa por `LogSeguro` (que apaga `d` en la versión pública
 *    y no imprime el detalle de las excepciones).
 *  - El servicio de notificaciones no imprime ni guarda el título/texto originales.
 *  - Las copias automáticas de Android son una lista blanca (solo `include`, nada de `exclude` como único control).
 */
class PrivacidadCodigoTest {
    private val raiz = File("src/main").takeIf { it.exists() } ?: File("app/src/main")

    @Test
    fun nadieUsaLogDirectamente() {
        val directo = Regex("(?<![\\w.])Log\\.[diwev]\\(")
        val problemas = raiz.walkTopDown()
            .filter { it.extension == "kt" && it.name != "LogSeguro.kt" }
            .filter { directo.containsMatchIn(it.readText()) }
            .map { it.name }.toList()
        assertEquals("Usa LogSeguro en vez de Log: $problemas", emptyList<String>(), problemas)
    }

    @Test
    fun elServicioNoGuardaNiImprimeElTextoOriginal() {
        val src = File(raiz, "java/com/flabs/cobrate/services/TokeNotificationListenerService.kt").readText()
        assertTrue("No se debe persistir el evento", !src.contains("EventoDeNotificacionStore.agregar"))
        assertTrue("No se debe imprimir el título/texto", !Regex("LogSeguro\\.[dwe]\\([^\\n]*\\$(titulo|texto)\\b").containsMatchIn(src))
    }

    @Test
    fun lasCopiasAutomaticasSonListaBlanca() {
        listOf("res/xml/backup_rules.xml", "res/xml/data_extraction_rules.xml").forEach { ruta ->
            val xml = File(raiz, ruta).readText()
            assertTrue("$ruta debe usar include", xml.contains("<include"))
            val incluidos = Regex("<include[^>]*path=\"([^\"]+)\"").findAll(xml).map { it.groupValues[1] }.toList()
            assertTrue("$ruta solo debe incluir preferencias no sensibles: $incluidos",
                incluidos.isNotEmpty() && incluidos.all { it.endsWith("preferencias_app.preferences_pb") || it.endsWith("configuracion_voz.preferences_pb") })
        }
    }
}
