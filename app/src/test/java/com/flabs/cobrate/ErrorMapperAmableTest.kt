package com.flabs.cobrate

import com.flabs.cobrate.core.logic.ErrorMapper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ErrorMapperAmableTest {

    @Test
    fun sinInternetMuestraMensajeCortoConCodigoYNuncaElTextoDelServidor() {
        val crudo = UnknownHostException("Unable to resolve host \"ianiulmdyzsqtrqpujkx.supabase.co\": No address associated with hostname")
        val texto = ErrorMapper.translate(crudo)
        assertEquals(ErrorMapper.MENSAJE_SIN_RED, texto)
        assertFalse(texto.contains("supabase", ignoreCase = true))
    }

    @Test
    fun laRedTambienSeDetectaDentroDeUnaCausa() {
        val envuelto = RuntimeException("falló", UnknownHostException("host"))
        assertEquals(ErrorMapper.MENSAJE_SIN_RED, ErrorMapper.translate(envuelto))
        assertEquals(ErrorMapper.MENSAJE_SIN_RED, ErrorMapper.amable(envuelto, "No se pudo guardar"))
    }

    @Test
    fun tiempoAgotadoTieneSuPropioMensaje() {
        assertEquals(ErrorMapper.MENSAJE_TIEMPO, ErrorMapper.translate(SocketTimeoutException("timeout")))
    }

    @Test
    fun textoTecnicoSeReemplazaPorLaAlternativaConCodigo() {
        val t = IllegalStateException("io.ktor.client.HttpClient: {\"code\":\"PGRST301\"}")
        val texto = ErrorMapper.amable(t, "Error al guardar")
        assertTrue(texto.startsWith("Error al guardar"))
        assertTrue(texto.contains("Código"))
        assertFalse(texto.contains("ktor"))
    }

    @Test
    fun unMensajeLegibleDeLaAppSeRespeta() {
        assertEquals("Tu acceso a este negocio está pausado.", ErrorMapper.amable(Exception("Tu acceso a este negocio está pausado."), "x"))
    }

    @Test
    fun credencialesInvalidasSonClaras() {
        assertEquals("Correo o contraseña incorrectos.", ErrorMapper.translate(Exception("Invalid login credentials")))
    }
}
