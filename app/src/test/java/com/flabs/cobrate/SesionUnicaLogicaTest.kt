package com.flabs.cobrate

import com.flabs.cobrate.features.acceso.AccionSesion
import com.flabs.cobrate.features.acceso.RespuestaSesion
import com.flabs.cobrate.features.acceso.SesionUnicaLogica
import java.util.Calendar
import java.util.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SesionUnicaLogicaTest {
    private val utc = TimeZone.getTimeZone("UTC")
    private fun ms(d: Int, h: Int, min: Int = 0) =
        Calendar.getInstance(utc).apply { clear(); set(2026, Calendar.NOVEMBER, d, h, min) }.timeInMillis

    @Test
    fun libreYSinRespuestaContinuan_ocupadaPregunta() {
        assertEquals(AccionSesion.Continuar, SesionUnicaLogica.decidir(RespuestaSesion.Libre))
        assertEquals(AccionSesion.Continuar, SesionUnicaLogica.decidir(RespuestaSesion.SinRespuesta))
        assertEquals(
            AccionSesion.Preguntar("Samsung SM-G973U1", 5L),
            SesionUnicaLogica.decidir(RespuestaSesion.Ocupada("Samsung SM-G973U1", 5L)),
        )
    }

    @Test
    fun meSacaronSoloSiOtroDispositivoEsElActivo() {
        assertTrue(SesionUnicaLogica.meSacaron("mio", "otro"))
        assertFalse(SesionUnicaLogica.meSacaron("mio", "mio"))
        assertFalse(SesionUnicaLogica.meSacaron("mio", null))
    }

    @Test
    fun nombreDelCelularNoRepiteLaMarca() {
        assertEquals("Samsung SM-G973U1", SesionUnicaLogica.nombreDeDispositivo("samsung", "SM-G973U1"))
        assertEquals("Xiaomi M2006C3LG", SesionUnicaLogica.nombreDeDispositivo("Xiaomi", "M2006C3LG"))
        assertEquals("Pixel 8", SesionUnicaLogica.nombreDeDispositivo("Pixel", "Pixel 8"))
        assertEquals("Otro celular", SesionUnicaLogica.nombreDeDispositivo("", ""))
    }

    @Test
    fun cuandoDiceHoyAyerOLosDias() {
        val ahora = ms(7, 10)
        assertEquals("hoy, 8:15 a. m.", SesionUnicaLogica.cuando(ms(7, 8, 15), ahora, utc))
        assertEquals("ayer, 8:15 p. m.", SesionUnicaLogica.cuando(ms(6, 20, 15), ahora, utc))
        assertEquals("hace 3 días, 12:00 a. m.", SesionUnicaLogica.cuando(ms(4, 0), ahora, utc))
    }
}
