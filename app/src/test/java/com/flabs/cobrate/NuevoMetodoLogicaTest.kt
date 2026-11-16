package com.flabs.cobrate

import com.flabs.cobrate.core.model.MetodoDeCobro
import com.flabs.cobrate.core.model.TipoMetodoCobro
import com.flabs.cobrate.features.cobros.MetodosCobroLogica
import com.flabs.cobrate.features.cobros.NuevoMetodoLogica
import com.flabs.cobrate.features.planes.Plan
import com.flabs.cobrate.features.planes.PlanLogica
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
    fun catalogoContieneTodasLasOpcionesExistentesSinInventarNiEliminar() {
        val principales = NuevoMetodoLogica.gruposPrincipales().flatMap { it.tipos }
        // Todas las 14 opciones del enum están visibles en el catálogo
        assertEquals(TipoMetodoCobro.entries.toSet(), principales.toSet())
        assertEquals(TipoMetodoCobro.entries.size, principales.size)
    }

    @Test
    fun redPlinSeMantieneDefinidaComoReferencia() {
        assertEquals(redPlin, NuevoMetodoLogica.tiposParaPlin().toSet())
        redPlin.forEach { assertTrue(NuevoMetodoLogica.esDeLaRedPlin(it)) }
    }

    @Test
    fun yapeSiguePudiendoElegirseCuandoNoExiste() {
        val vacio = emptyList<MetodoDeCobro>()
        assertTrue(NuevoMetodoLogica.puedeElegir(TipoMetodoCobro.YAPE, vacio))
        assertFalse(NuevoMetodoLogica.esProximamente(TipoMetodoCobro.YAPE))
        assertTrue(NuevoMetodoLogica.esTipoPermitidoParaCrear(TipoMetodoCobro.YAPE))
    }

    @Test
    fun yapeNoSePuedeElegirSiYaExiste() {
        val conYape = listOf(metodo(TipoMetodoCobro.YAPE))
        assertTrue(NuevoMetodoLogica.yaExiste(TipoMetodoCobro.YAPE, conYape))
        assertFalse(NuevoMetodoLogica.puedeElegir(TipoMetodoCobro.YAPE, conYape))
    }

    @Test
    fun plinYTodosLosDemasTiposNoPuedenElegirseYEstanEnProximamente() {
        val vacio = emptyList<MetodoDeCobro>()
        val noYape = TipoMetodoCobro.entries.filter { it != TipoMetodoCobro.YAPE }

        for (tipo in noYape) {
            assertFalse("Tipo $tipo no debe poder elegirse", NuevoMetodoLogica.puedeElegir(tipo, vacio))
            assertTrue("Tipo $tipo debe figurar en Próximamente", NuevoMetodoLogica.esProximamente(tipo))
            assertFalse("Tipo $tipo no debe ser permitido para creación", NuevoMetodoLogica.esTipoPermitidoParaCrear(tipo))
        }
    }

    @Test
    fun plinNoSePuedeElegirBajoNingunaCircunstancia() {
        val vacio = emptyList<MetodoDeCobro>()
        val conInterbank = listOf(metodo(TipoMetodoCobro.INTERBANK))
        val conPlin = listOf(metodo(TipoMetodoCobro.PLIN))

        assertFalse(NuevoMetodoLogica.puedeElegir(TipoMetodoCobro.PLIN, vacio))
        assertFalse(NuevoMetodoLogica.puedeElegir(TipoMetodoCobro.PLIN, conInterbank))
        assertFalse(NuevoMetodoLogica.puedeElegir(TipoMetodoCobro.PLIN, conPlin))
        assertTrue(NuevoMetodoLogica.esProximamente(TipoMetodoCobro.PLIN))
    }

    @Test
    fun noSePuedeCrearUnMetodoNoYapeLlamandoALaLogicaDeConfirmacion() {
        var callbackEjecutado = false
        fun onConfirmSimulado(tipo: TipoMetodoCobro) {
            if (!NuevoMetodoLogica.esTipoPermitidoParaCrear(tipo)) {
                // Rechazo inmediato
                return
            }
            callbackEjecutado = true
        }

        // Intento con Yape: permitido
        onConfirmSimulado(TipoMetodoCobro.YAPE)
        assertTrue(callbackEjecutado)

        // Intento con Plin y otros bancos: rechazados
        val intentosBloqueados = listOf(
            TipoMetodoCobro.PLIN,
            TipoMetodoCobro.BCP,
            TipoMetodoCobro.INTERBANK,
            TipoMetodoCobro.BBVA,
            TipoMetodoCobro.TRANSFERENCIA
        )
        for (tipo in intentosBloqueados) {
            callbackEjecutado = false
            onConfirmSimulado(tipo)
            assertFalse("El callback no debió ejecutarse para $tipo", callbackEjecutado)
        }
    }

    @Test
    fun limitesDePlanesSeMantienenInalterados() {
        assertEquals(1, PlanLogica.MAX_METODOS_FREE)

        fun puedeVincularOtro(plan: Plan, metodosExistentes: Int): Boolean {
            val esPro = plan == Plan.PRO
            return esPro || metodosExistentes < PlanLogica.MAX_METODOS_FREE
        }

        // En plan Free con 0 métodos: puede vincular
        assertTrue(puedeVincularOtro(Plan.FREE, 0))
        // En plan Free con 1 método: bloqueado (máximo 1)
        assertFalse(puedeVincularOtro(Plan.FREE, 1))
        assertFalse(puedeVincularOtro(Plan.FREE, 2))

        // En plan Pro: sin límite
        assertTrue(puedeVincularOtro(Plan.PRO, 0))
        assertTrue(puedeVincularOtro(Plan.PRO, 1))
        assertTrue(puedeVincularOtro(Plan.PRO, 5))
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
        assertNull(MetodosCobroLogica.marcaPlin(metodo(TipoMetodoCobro.PLIN)))
    }
}
