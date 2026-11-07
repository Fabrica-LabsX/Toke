package com.flabs.cobrate

import com.flabs.cobrate.features.worker.PanelTrabajadorLogica
import com.flabs.cobrate.features.worker.PanelTrabajadorLogica.FichaDerecha
import org.junit.Assert.assertEquals
import org.junit.Test

class PanelTrabajadorLogicaTest {
    private val min = 60_000L

    @Test
    fun textoHace() {
        val ahora = 10_000_000_000L
        assertEquals("Ahora", PanelTrabajadorLogica.textoHace(ahora - 20_000, ahora))
        assertEquals("Hace 5 min", PanelTrabajadorLogica.textoHace(ahora - 5 * min, ahora))
        assertEquals("Hace 2 h", PanelTrabajadorLogica.textoHace(ahora - 125 * min, ahora))
        assertEquals("Hace 3 d", PanelTrabajadorLogica.textoHace(ahora - 3 * 24 * 60 * min, ahora))
        assertEquals("Ahora", PanelTrabajadorLogica.textoHace(ahora + 5 * min, ahora))
    }

    @Test
    fun fichaDerechaSegunPendientes() {
        assertEquals(FichaDerecha.POR_RECLAMAR, PanelTrabajadorLogica.fichaDerecha(1))
        assertEquals(FichaDerecha.MIS_NEGOCIOS, PanelTrabajadorLogica.fichaDerecha(0))
    }
}
