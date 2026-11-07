package com.flabs.cobrate

import com.flabs.cobrate.features.negocios.NegocioEdicionLogica
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NegocioEdicionLogicaTest {
    @Test
    fun nombreMuyCortoDaError() {
        assertNotNull(NegocioEdicionLogica.errorNombre(" a "))
        assertNull(NegocioEdicionLogica.errorNombre("Mi Bodega"))
    }

    @Test
    fun maxVacioEsSinLimiteYInvalidoDaError() {
        assertEquals(null to null, NegocioEdicionLogica.leerMax("  "))
        assertEquals(5 to null, NegocioEdicionLogica.leerMax("5"))
        assertNotNull(NegocioEdicionLogica.leerMax("0").second)
        assertNotNull(NegocioEdicionLogica.leerMax("501").second)
        assertNotNull(NegocioEdicionLogica.leerMax("abc").second)
    }

    @Test
    fun topeDeTrabajadores() {
        assertTrue(NegocioEdicionLogica.puedeInvitar(null, 99))
        assertTrue(NegocioEdicionLogica.puedeInvitar(3, 2))
        assertFalse(NegocioEdicionLogica.puedeInvitar(3, 3))
    }
}
