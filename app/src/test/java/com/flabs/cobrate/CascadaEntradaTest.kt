package com.flabs.cobrate

import com.flabs.cobrate.ui.theme.CascadaEntrada
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CascadaEntradaTest {
    @Test
    fun arribaSaleSinEspera() = assertEquals(0L, CascadaEntrada.retrasoMs(0f))

    @Test
    fun masAbajoEsperaMas() = assertTrue(CascadaEntrada.retrasoMs(500f) > CascadaEntrada.retrasoMs(100f))

    @Test
    fun nuncaPasaDelTope() = assertEquals(CascadaEntrada.MAX_RETRASO_MS, CascadaEntrada.retrasoMs(5000f))
}
