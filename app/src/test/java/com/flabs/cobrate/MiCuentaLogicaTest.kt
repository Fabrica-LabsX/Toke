package com.flabs.cobrate

import com.flabs.cobrate.features.cuenta.ErrorCorreo
import com.flabs.cobrate.features.cuenta.MiCuentaLogica
import com.flabs.cobrate.features.cuenta.ResultadoEliminacion
import com.flabs.cobrate.features.cuenta.TipoCuenta
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MiCuentaLogicaTest {
    @Test
    fun cuentaSinCorreoEsAnonimaYNoSeInventaCorreo() {
        assertEquals(TipoCuenta.ANONIMA, MiCuentaLogica.tipo(null))
        assertEquals(TipoCuenta.ANONIMA, MiCuentaLogica.tipo("  "))
        assertEquals(TipoCuenta.CON_CORREO, MiCuentaLogica.tipo("a@b.com"))
    }

    @Test
    fun validaCorreoNuevo() {
        assertEquals(ErrorCorreo.VACIO, MiCuentaLogica.errorCorreoNuevo(" ", "a@b.com"))
        assertEquals(ErrorCorreo.INVALIDO, MiCuentaLogica.errorCorreoNuevo("sin-arroba", "a@b.com"))
        assertEquals(ErrorCorreo.INVALIDO, MiCuentaLogica.errorCorreoNuevo("a@b", "a@b.com"))
        assertEquals(ErrorCorreo.IGUAL_AL_ACTUAL, MiCuentaLogica.errorCorreoNuevo("A@B.com", "a@b.com"))
        assertNull(MiCuentaLogica.errorCorreoNuevo("nuevo@b.com", "a@b.com"))
    }

    @Test
    fun cambioDePasswordExigeActualYNuevaDistinta() {
        assertNotNull(MiCuentaLogica.errorCambioPassword("", "nueva123", "nueva123"))
        assertNotNull(MiCuentaLogica.errorCambioPassword("igual123", "igual123", "igual123"))
        assertNotNull(MiCuentaLogica.errorCambioPassword("vieja123", "nueva123", "otra1234"))
        assertNull(MiCuentaLogica.errorCambioPassword("vieja123", "nueva123", "nueva123"))
    }

    @Test
    fun confirmarEliminacionSegunTipoDeCuenta() {
        assertTrue(MiCuentaLogica.puedeConfirmarEliminacion(TipoCuenta.CON_CORREO, "clave", ""))
        assertFalse(MiCuentaLogica.puedeConfirmarEliminacion(TipoCuenta.CON_CORREO, "", "ELIMINAR"))
        assertTrue(MiCuentaLogica.puedeConfirmarEliminacion(TipoCuenta.ANONIMA, "", "eliminar"))
        assertFalse(MiCuentaLogica.puedeConfirmarEliminacion(TipoCuenta.ANONIMA, "clave", "borrar"))
    }

    @Test
    fun traduceLaRespuestaDelServidor() {
        assertEquals(ResultadoEliminacion.Eliminada, ResultadoEliminacion.desdeRespuesta(200, "{\"ok\":true}"))
        assertEquals(ResultadoEliminacion.ContrasenaIncorrecta, ResultadoEliminacion.desdeRespuesta(403, "{\"error\":\"REAUTH_FAILED\"}"))
        assertEquals(ResultadoEliminacion.TieneNegocios(2), ResultadoEliminacion.desdeRespuesta(409, "{\"error\":\"HAS_BUSINESSES\",\"negocios\":2}"))
        assertEquals(ResultadoEliminacion.ErrorServidor, ResultadoEliminacion.desdeRespuesta(500, "{}"))
    }
}
