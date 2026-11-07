package com.flabs.cobrate

import com.flabs.cobrate.core.model.MetodoDeCobro
import com.flabs.cobrate.core.model.TipoMetodoCobro
import com.flabs.cobrate.features.cobros.MetodosCobroLogica
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MetodosCobroLogicaTest {

    private fun metodo(id: String, tipo: TipoMetodoCobro, dato: String = "", titular: String? = null) =
        MetodoDeCobro(id = id, tipo = tipo, datoDeCobro = dato, nombreDestinatario = titular)

    @Test
    fun todosLosTiposTienenUnNombreLegible() {
        TipoMetodoCobro.entries.forEach {
            assertTrue("Falta nombre para $it", MetodosCobroLogica.nombreLegible(it).isNotBlank())
        }
        assertEquals("Yape", MetodosCobroLogica.nombreLegible(TipoMetodoCobro.YAPE))
        assertEquals("BCP", MetodosCobroLogica.nombreLegible(TipoMetodoCobro.BCP))
        assertEquals("Lemon Cash", MetodosCobroLogica.nombreLegible(TipoMetodoCobro.LEMON_CASH))
    }

    @Test
    fun seleccionadoUsaElElegidoOElPrimeroSiNoExiste() {
        val lista = listOf(metodo("a", TipoMetodoCobro.YAPE), metodo("b", TipoMetodoCobro.PLIN))
        assertEquals("b", MetodosCobroLogica.seleccionado(lista, "b")?.id)
        assertEquals("a", MetodosCobroLogica.seleccionado(lista, "zzz")?.id)
        assertEquals("a", MetodosCobroLogica.seleccionado(lista, null)?.id)
        assertNull(MetodosCobroLogica.seleccionado(emptyList(), "a"))
    }

    @Test
    fun titularYNumeroSinConfigurarMuestranElTextoPorDefecto() {
        val vacio = metodo("a", TipoMetodoCobro.YAPE, dato = "   ", titular = "  ")
        assertEquals(MetodosCobroLogica.SIN_CONFIGURAR, MetodosCobroLogica.titular(vacio))
        assertEquals(MetodosCobroLogica.SIN_CONFIGURAR, MetodosCobroLogica.numero(vacio))
        assertNull(MetodosCobroLogica.numeroCopiable(vacio))

        val completo = metodo("b", TipoMetodoCobro.YAPE, dato = " 987654321 ", titular = " Ana Ruiz ")
        assertEquals("Ana Ruiz", MetodosCobroLogica.titular(completo))
        assertEquals("987654321", MetodosCobroLogica.numero(completo))
        assertEquals("987654321", MetodosCobroLogica.numeroCopiable(completo))
    }

    @Test
    fun laExtensionDeLaImagenSaleDeSusPrimerosBytes() {
        val png = byteArrayOf(0x89.toByte(), 'P'.code.toByte(), 'N'.code.toByte(), 'G'.code.toByte(), 0, 0, 0, 0, 0, 0, 0, 0)
        val webp = ByteArray(12).also { it[8] = 'W'.code.toByte(); it[9] = 'E'.code.toByte() }
        val jpg = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xE0.toByte(), 0, 0, 0, 0, 0, 0, 0, 0)
        assertEquals("png", MetodosCobroLogica.extensionDeImagen(png))
        assertEquals("webp", MetodosCobroLogica.extensionDeImagen(webp))
        assertEquals("jpg", MetodosCobroLogica.extensionDeImagen(jpg))
        assertEquals("jpg", MetodosCobroLogica.extensionDeImagen(ByteArray(0)))
    }
}
