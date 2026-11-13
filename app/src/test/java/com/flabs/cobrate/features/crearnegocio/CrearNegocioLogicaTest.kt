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
        // MARISQUERIA ya no se ofrece: es un rubro antiguo que se muestra como Restaurante.
        assertEquals(TipoNegocio.values().toSet() - TipoNegocio.MARISQUERIA, CrearNegocioLogica.tipos.map { it.tipo }.toSet())
        assertTrue(CrearNegocioLogica.colores.any { it.hex == CrearNegocioLogica.COLOR_POR_DEFECTO })
    }

    @Test
    fun nombreVacioNoEsValido() {
        assertFalse(CrearNegocioLogica.nombreValido("   "))
        assertTrue(CrearNegocioLogica.nombreValido("Mi Bodeguita"))
    }

    @Test
    fun tituloDeTipoNuncaMuestraElNombreInternoDelEnum() {
        assertEquals("Restaurante o comida", CrearNegocioLogica.tituloDeTipo(TipoNegocio.MARISQUERIA))
        assertEquals("Otro negocio", CrearNegocioLogica.tituloDeTipo(null))
        assertEquals("Atuendos o ropa", CrearNegocioLogica.tituloDeTipo(TipoNegocio.ROPA))
    }

    @Test
    fun cadaRubroOfrecidoTieneNombreUnico() {
        val nombres = CrearNegocioLogica.tipos.map { it.titulo }
        assertEquals(nombres.size, nombres.toSet().size)
    }

    @Test
    fun argbDeHexLeeColoresValidos() {
        assertEquals(0xFF007A3EL, CrearNegocioLogica.argbDeHex("#007A3E"))
        assertNull(CrearNegocioLogica.argbDeHex("verde"))
        assertNull(CrearNegocioLogica.argbDeHex(null))
    }
}
