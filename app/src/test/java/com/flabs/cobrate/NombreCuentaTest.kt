package com.flabs.cobrate

import com.flabs.cobrate.features.cuenta.MiCuentaLogica
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NombreCuentaTest {
    @Test
    fun aceptaUnNombreNormal() = assertTrue(MiCuentaLogica.nombreValido("  Rosa Pérez ", "aociikt"))

    @Test
    fun rechazaVacioCortoOLargo() {
        assertFalse(MiCuentaLogica.nombreValido("   ", "x"))
        assertFalse(MiCuentaLogica.nombreValido("a", "x"))
        assertFalse(MiCuentaLogica.nombreValido("a".repeat(41), "x"))
    }

    @Test
    fun rechazaElMismoNombre() = assertFalse(MiCuentaLogica.nombreValido(" Rosa ", "Rosa"))
}
