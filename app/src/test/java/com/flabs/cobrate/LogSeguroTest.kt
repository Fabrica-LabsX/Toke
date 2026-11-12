package com.flabs.cobrate

import com.flabs.cobrate.core.logic.LogSeguro
import com.flabs.cobrate.core.logic.conReintentoPorReloj
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LogSeguroTest {

    @Test
    fun testSanitizarHeadersYTokens() {
        val raw = "Error en Supabase: RestException Headers: [Authorization=[Bearer eyJabc.def.ghi], apikey=[sb_publishable_FAKE123]]"
        val sanitizado = LogSeguro.sanitizar(raw)

        assertFalse("No debe contener eyJabc", sanitizado.contains("eyJabc"))
        assertFalse("No debe contener Bearer eyJ", sanitizado.contains("Bearer eyJ"))
        assertFalse("No debe contener sb_publishable_FAKE123", sanitizado.contains("sb_publishable_FAKE123"))
        assertTrue("Debe contener Headers: [omitidos]", sanitizado.contains("Headers: [omitidos]"))
    }

    @Test
    fun testSanitizarJwtSuelto() {
        val raw = "Token recibido eyJ123.456.789 en la respuesta"
        val sanitizado = LogSeguro.sanitizar(raw)

        assertFalse("No debe contener el JWT crudo", sanitizado.contains("eyJ123.456.789"))
        assertTrue("Debe reemplazar por ***JWT***", sanitizado.contains("***JWT***"))
    }

    @Test
    fun testSanitizarTextoNormal() {
        val raw = "Error de red"
        val sanitizado = LogSeguro.sanitizar(raw)

        assertEquals("Error de red", sanitizado)
    }

    @Test
    fun testSanitizarRecorteMaximo300Caracteres() {
        val largo = "A".repeat(400)
        val sanitizado = LogSeguro.sanitizar(largo)

        assertTrue("El resultado debe ser de longitud <= 300", sanitizado.length <= 300)
    }

    @Test
    fun testReintentoPorRelojExitoAlSegundoIntento() = runBlocking {
        var llamadas = 0
        val resultado = conReintentoPorReloj(esperaMs = 10) {
            llamadas++
            if (llamadas == 1) {
                Result.failure(Exception("PGRST303: JWT issued at future"))
            } else {
                Result.success("OK")
            }
        }

        assertEquals(2, llamadas)
        assertTrue("Debe ser exito", resultado.isSuccess)
        assertEquals("OK", resultado.getOrNull())
    }

    @Test
    fun testReintentoPorRelojFalloDiferenteSinReintento() = runBlocking {
        var llamadas = 0
        val resultado = conReintentoPorReloj<String>(esperaMs = 10) {
            llamadas++
            Result.failure(Exception("Error de red normal"))
        }

        assertEquals(1, llamadas)
        assertTrue("Debe ser fallo", resultado.isFailure)
    }

    @Test
    fun testReintentoPorRelojFallaSiemprePGRST303() = runBlocking {
        var llamadas = 0
        val resultado = conReintentoPorReloj<String>(esperaMs = 10) {
            llamadas++
            Result.failure(Exception("PGRST303: JWT issued at future"))
        }

        assertEquals(2, llamadas)
        assertTrue("Debe ser fallo", resultado.isFailure)
    }

    @Test
    fun testCodigoDeRegistroEsCortoYSinNombres() {
        val c = LogSeguro.codigo("MetodoDeCobroRealtimeListener")
        assertTrue(Regex("E-[0-9A-F]{4}").matches(c))
        assertFalse(c.contains("Metodo", ignoreCase = true))
        assertEquals(c, LogSeguro.codigo("MetodoDeCobroRealtimeListener"))
    }
}
