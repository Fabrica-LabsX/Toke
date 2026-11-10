package com.flabs.cobrate

import com.flabs.cobrate.core.logic.FechaCobro
import java.util.Calendar
import java.util.TimeZone
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FechaCobroTest {
    private val utc = TimeZone.getTimeZone("UTC")
    private fun ms(d: Int, h: Int) = Calendar.getInstance(utc).apply { clear(); set(2026, Calendar.NOVEMBER, d, h, 0) }.timeInMillis

    @Test
    fun deHoySoloMuestraLaHora() {
        val t = FechaCobro.texto(ms(10, 13), ms(10, 18), utc)
        assertFalse(t.contains("·"))
        assertTrue(t.contains("01:00"))
    }

    @Test
    fun deOtroDiaMuestraElDia() {
        val t = FechaCobro.texto(ms(7, 13), ms(10, 18), utc)
        assertTrue(t, t.startsWith("7 "))
        assertTrue(t, t.contains("·"))
    }
}
