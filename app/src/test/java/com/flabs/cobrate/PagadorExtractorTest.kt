package com.flabs.cobrate

import com.flabs.cobrate.core.logic.PagadorExtractor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PagadorExtractorTest {
    @Test
    fun yapeConNombreCensurado() {
        assertEquals("Juan P*", PagadorExtractor.extraer("Yape", "Juan P* te envió un pago por S/ 5.00"))
    }

    @Test
    fun prefijoDeLaAppNoEsParteDelNombre() {
        assertEquals("Ana R.", PagadorExtractor.extraer("", "Yape! Ana R. te envió un pago por S/ 12.50"))
    }

    @Test
    fun nombreEnElTituloYMontoEnElTexto() {
        assertEquals("Carlos Perez", PagadorExtractor.extraer("Carlos Perez", "te yapeó S/ 15.50"))
    }

    @Test
    fun recibisteDeAlguien() {
        assertEquals("Juan P.", PagadorExtractor.extraer("", "Recibiste S/ 35.00 de Juan P."))
        assertEquals("Ana R*", PagadorExtractor.extraer(null, "Has recibido S/ 10.00 de Ana R*"))
    }

    @Test
    fun sinNombreDevuelveNulo() {
        assertNull(PagadorExtractor.extraer("", "Te yapeó S/ 1,250.50"))
        assertNull(PagadorExtractor.extraer(null, null))
        assertNull(PagadorExtractor.extraer("Yape", "Recibiste un pago"))
    }
}
