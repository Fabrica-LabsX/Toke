package com.flabs.cobrate

import com.flabs.cobrate.core.model.MetodoDeCobro
import com.flabs.cobrate.core.model.TipoMetodoCobro
import com.flabs.cobrate.features.cobros.MetodosCobroLogica
import com.flabs.cobrate.features.cobros.NuevoMetodoLogica
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NuevoMetodoLogicaTest {

    private fun metodo(tipo: TipoMetodoCobro) =
        MetodoDeCobro(id = "id-$tipo", tipo = tipo)

    private val redPlin = setOf(
        TipoMetodoCobro.INTERBANK,
        TipoMetodoCobro.BBVA,
        TipoMetodoCobro.SCOTIABANK,
        TipoMetodoCobro.BANBIF,
        TipoMetodoCobro.AGORA_TUNKI,
    )

    @Test
    fun cadaTipoSeOfreceEnUnSoloLugar_laPrimeraPantallaOLaDePlin() {
        val principales = NuevoMetodoLogica.gruposPrincipales().flatMap { it.tipos }
        val dePlin = NuevoMetodoLogica.tiposParaPlin()
        assertEquals(TipoMetodoCobro.entries.toSet(), (principales + dePlin).toSet())
        assertEquals(TipoMetodoCobro.entries.size, principales.size + dePlin.size)
        assertTrue(principales.intersect(dePlin.toSet()).isEmpty())
    }

    @Test
    fun dentroDePlinSoloEstaLaRedPlin() {
        assertEquals(redPlin, NuevoMetodoLogica.tiposParaPlin().toSet())
        redPlin.forEach { assertTrue(NuevoMetodoLogica.esDeLaRedPlin(it)) }
    }

    @Test
    fun lasEntidadesDeLaRedPlinNoSalenEnLaPrimeraPantalla() {
        val principales = NuevoMetodoLogica.gruposPrincipales().flatMap { it.tipos }
        redPlin.forEach { assertFalse("$it no debe salir en la primera pantalla", it in principales) }
    }

    @Test
    fun lasQueNoSonDePlinSiguenDondeSiempre() {
        val principales = NuevoMetodoLogica.gruposPrincipales()
        val bancos = principales.first { it.titulo == "Bancos" }.tipos
        val billeteras = principales.first { it.titulo == "Billeteras" }.tipos
        assertTrue(TipoMetodoCobro.BCP in bancos)
        assertTrue(TipoMetodoCobro.PICHINCHA in bancos)
        assertEquals(listOf(TipoMetodoCobro.YAPE, TipoMetodoCobro.PLIN), billeteras.take(2))
        assertTrue(TipoMetodoCobro.BIM in billeteras)
        assertFalse(NuevoMetodoLogica.esDeLaRedPlin(TipoMetodoCobro.BCP))
        assertFalse(NuevoMetodoLogica.esDeLaRedPlin(TipoMetodoCobro.YAPE))
        assertFalse(NuevoMetodoLogica.esDeLaRedPlin(TipoMetodoCobro.PLIN))
    }

    @Test
    fun unaBilleteraQueYaExisteNoSePuedeElegir() {
        val existentes = listOf(metodo(TipoMetodoCobro.YAPE), metodo(TipoMetodoCobro.INTERBANK))
        assertTrue(NuevoMetodoLogica.yaExiste(TipoMetodoCobro.YAPE, existentes))
        assertFalse(NuevoMetodoLogica.puedeElegir(TipoMetodoCobro.YAPE, existentes))
        assertFalse(NuevoMetodoLogica.puedeElegir(TipoMetodoCobro.INTERBANK, existentes))
        assertTrue(NuevoMetodoLogica.puedeElegir(TipoMetodoCobro.BCP, existentes))
    }

    @Test
    fun plinSiempreSePuedeElegirMientrasQuedeUnaBilleteraLibre() {
        val conInterbank = listOf(metodo(TipoMetodoCobro.INTERBANK))
        assertTrue(NuevoMetodoLogica.puedeElegir(TipoMetodoCobro.PLIN, conInterbank))
        // Un Plin heredado (tipo PLIN) no ocupa ninguna billetera.
        assertTrue(NuevoMetodoLogica.puedeElegir(TipoMetodoCobro.PLIN, listOf(metodo(TipoMetodoCobro.PLIN))))
    }

    @Test
    fun plinNoSePuedeElegirSiTodasSusBilleterasYaExisten() {
        val todas = NuevoMetodoLogica.tiposParaPlin().map { metodo(it) }
        assertFalse(NuevoMetodoLogica.puedeElegir(TipoMetodoCobro.PLIN, todas))
    }

    @Test
    fun soloElNumeroOCuentaEsObligatorio() {
        assertFalse(NuevoMetodoLogica.datosValidos("   "))
        assertTrue(NuevoMetodoLogica.datosValidos("987654321"))
    }

    @Test
    fun laMarcaPlinEsSoloVisualYSeDeduceDeLaBilletera() {
        redPlin.forEach { assertEquals("Plin", MetodosCobroLogica.marcaPlin(metodo(it))) }
        assertNull(MetodosCobroLogica.marcaPlin(metodo(TipoMetodoCobro.BCP)))
        assertNull(MetodosCobroLogica.marcaPlin(metodo(TipoMetodoCobro.YAPE)))
        assertNull(MetodosCobroLogica.marcaPlin(metodo(TipoMetodoCobro.BIM)))
        // Un Plin antiguo (tipo PLIN) no cambia: se muestra solo como Plin.
        assertNull(MetodosCobroLogica.marcaPlin(metodo(TipoMetodoCobro.PLIN)))
    }
}
