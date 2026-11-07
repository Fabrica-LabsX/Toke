package com.flabs.cobrate.features.crearnegocio

import com.flabs.cobrate.core.model.TipoNegocio
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CrearNegocioLogicaTest {
    @Test
    fun cadaRubroTieneUnaOpcionYElColorPorDefectoEstaEnLaLista() {
        assertEquals(TipoNegocio.values().toSet(), CrearNegocioLogica.tipos.map { it.tipo }.toSet())
        assertTrue(CrearNegocioLogica.colores.any { it.hex == CrearNegocioLogica.COLOR_POR_DEFECTO })
    }

    @Test
    fun nombreVacioNoEsValido() {
        assertFalse(CrearNegocioLogica.nombreValido("   "))
        assertTrue(CrearNegocioLogica.nombreValido("Mi Bodeguita"))
    }

    @Test
    fun tituloDeTipoNuncaMuestraElNombreInternoDelEnum() {
        assertEquals("Marisquería & Cevichería", CrearNegocioLogica.tituloDeTipo(TipoNegocio.MARISQUERIA))
        assertEquals("Otro tipo de negocio", CrearNegocioLogica.tituloDeTipo(null))
    }

    @Test
    fun argbDeHexLeeColoresValidos() {
        assertEquals(0xFF007A3EL, CrearNegocioLogica.argbDeHex("#007A3E"))
        assertNull(CrearNegocioLogica.argbDeHex("verde"))
        assertNull(CrearNegocioLogica.argbDeHex(null))
    }
}
