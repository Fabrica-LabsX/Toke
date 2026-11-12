package com.flabs.cobrate

import com.flabs.cobrate.core.logic.DuracionAviso
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DuracionAvisoTest {
    @Test
    fun mensajeCortoNuncaBajaDelMinimo() = assertTrue(DuracionAviso.milis("Listo", 1000L) >= DuracionAviso.MIN_MS)

    @Test
    fun mensajeLargoTopeaEnElMaximo() = assertEquals(DuracionAviso.MAX_MS, DuracionAviso.milis("a".repeat(500), 4000L))

    @Test
    fun masTextoDuraMas() = assertTrue(DuracionAviso.milis("a".repeat(80), 4000L) > DuracionAviso.milis("a".repeat(10), 4000L))
}
