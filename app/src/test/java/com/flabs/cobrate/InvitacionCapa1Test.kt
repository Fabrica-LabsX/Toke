package com.flabs.cobrate

import com.flabs.cobrate.core.data.InvitacionRepository
import com.flabs.cobrate.core.logic.ErrorMapper
import com.flabs.cobrate.core.logic.TokeErrorCodes
import java.net.ConnectException
import java.net.SocketTimeoutException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InvitacionCapa1Test {

    @Test
    fun testGlosarioCodigosToke101a110() {
        assertEquals("INVITACION_NO_ENCONTRADA", TokeErrorCodes.glosario(101))
        assertEquals("INVITACION_YA_UTILIZADA", TokeErrorCodes.glosario(102))
        assertEquals("INVITACION_EXPIRADA", TokeErrorCodes.glosario(103))
        assertEquals("ERROR_VERIFICANDO_INVITACION", TokeErrorCodes.glosario(104))
        assertEquals("RESPUESTA_INVITACION_INVALIDA", TokeErrorCodes.glosario(105))
        assertEquals("ERROR_SESION_TRABAJADOR", TokeErrorCodes.glosario(106))
        assertEquals("SESION_TRABAJADOR_NO_DISPONIBLE", TokeErrorCodes.glosario(107))
        assertEquals("ERROR_ACEPTANDO_INVITACION", TokeErrorCodes.glosario(108))
        assertEquals("ERROR_RED_INVITACION", TokeErrorCodes.glosario(109))
        assertEquals("ERROR_INESPERADO_INVITACION", TokeErrorCodes.glosario(110))
    }

    @Test
    fun testNoExposicionDeCadenasTecnicasPGRSToHttp500() {
        val errPgrst = Exception("PGRST202: internal database exception in postgres function SQLSTATE 23505")
        val translatedPgrst = ErrorMapper.translateInvitacionError(errPgrst, TokeErrorCodes.ERROR_VERIFICANDO_INVITACION)
        
        assertFalse("No debe exponer PGRST202", translatedPgrst.contains("PGRST202"))
        assertFalse("No debe exponer SQLSTATE", translatedPgrst.contains("SQLSTATE"))
        assertFalse("No debe exponer 23505", translatedPgrst.contains("23505"))
        assertTrue("Debe mostrar código de ayuda TOKE 104", translatedPgrst.contains("104"))
    }

    @Test
    fun testErrorRedYTimeoutMapeanACodigo109() {
        val errConnect = ConnectException("Failed to connect to 192.168.1.1:443")
        val translatedConnect = ErrorMapper.translateInvitacionError(errConnect, TokeErrorCodes.ERROR_VERIFICANDO_INVITACION)
        assertTrue("Error de conexión debe mapear a TOKE 109", translatedConnect.contains("109"))

        val errTimeout = SocketTimeoutException("Read timed out")
        val translatedTimeout = ErrorMapper.translateInvitacionError(errTimeout, TokeErrorCodes.ERROR_VERIFICANDO_INVITACION)
        assertTrue("Timeout debe mapear a TOKE 109", translatedTimeout.contains("109"))
    }

    @Test
    fun testPruebaFallbackInesperadoMapeaAMensajeHumanoSinExponerCadenasInternas() {
        val errServidor = Exception("Server response 500")
        val translated = ErrorMapper.translateInvitacionError(errServidor, TokeErrorCodes.ERROR_ACEPTANDO_INVITACION)
        
        assertTrue("Error de aceptación debe incluir código de ayuda 108", translated.contains("108"))
        assertFalse("No debe filtrar la cadena técnica 'Server response 500'", translated.contains("Server response 500"))
    }

    @Test
    fun testResultadoAceptarInvitacionExitoYRechazoNegocio() {
        val exitoObj = InvitacionRepository.ResultadoAceptarInvitacion(true, "Unión exitosa")
        assertTrue(exitoObj.exito)
        assertEquals("Unión exitosa", exitoObj.mensaje)

        val rechazoPropioNegocio = InvitacionRepository.ResultadoAceptarInvitacion(false, "No puedes unirte a tu propio negocio.")
        assertFalse(rechazoPropioNegocio.exito)
        assertEquals("No puedes unirte a tu propio negocio.", rechazoPropioNegocio.mensaje)
    }
}
