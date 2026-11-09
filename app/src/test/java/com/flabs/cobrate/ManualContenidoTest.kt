package com.flabs.cobrate

import com.flabs.cobrate.features.soporte.ManualContenido
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ManualContenidoTest {

    @Test
    fun hayLasNueveGuiasPedidasConIdsUnicos() {
        val ids = ManualContenido.articulos.map { it.id }
        assertEquals(9, ids.size)
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun cadaGuiaTienePasosYSuElementoResaltadoExiste() {
        ManualContenido.articulos.forEach { a ->
            assertTrue("${a.id} necesita al menos 3 pasos", a.pasos.size >= 3)
            a.pasos.forEach { p ->
                assertTrue("${a.id}: texto vacío", p.texto.isNotBlank())
                assertTrue("${a.id}: resaltado fuera de rango", p.mock.resaltado in p.mock.elementos.indices)
            }
        }
    }

    @Test
    fun laPrimeraPreguntaEsSobreElAccesoALasBilleteras() {
        assertTrue(ManualContenido.preguntas.first().pregunta.contains("billeteras"))
        ManualContenido.preguntas.forEach {
            assertTrue(it.pregunta.startsWith("¿") && it.pregunta.endsWith("?"))
            assertTrue(it.respuesta.length > 20)
        }
    }
}
