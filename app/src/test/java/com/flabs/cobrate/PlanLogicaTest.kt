package com.flabs.cobrate

import com.flabs.cobrate.features.planes.EstadoPlanDto
import com.flabs.cobrate.features.planes.NivelCuota
import com.flabs.cobrate.features.planes.Plan
import com.flabs.cobrate.features.planes.PlanLogica
import com.flabs.cobrate.features.reportes.PeriodoReporte
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlanLogicaTest {
    @Test
    fun nivelesDeLaCuota() {
        assertEquals(NivelCuota.NORMAL, PlanLogica.nivel(50))
        assertEquals(NivelCuota.NORMAL, PlanLogica.nivel(21))
        assertEquals(NivelCuota.AMARILLO, PlanLogica.nivel(20))
        assertEquals(NivelCuota.AMARILLO, PlanLogica.nivel(17))
        assertEquals(NivelCuota.ROJO, PlanLogica.nivel(16))
        assertEquals(NivelCuota.ROJO, PlanLogica.nivel(1))
        assertEquals(NivelCuota.AGOTADA, PlanLogica.nivel(0))
        assertNull(PlanLogica.nivel(null))
    }

    @Test
    fun restantesYBarra() {
        assertEquals(30, PlanLogica.restantes(50, 20))
        assertEquals(0, PlanLogica.restantes(50, 80))
        assertNull(PlanLogica.restantes(null, 5))
        assertEquals(0.6f, PlanLogica.progreso(50, 30), 0.001f)
        assertEquals(0f, PlanLogica.progreso(null, null), 0f)
        assertEquals(12, PlanLogica.usadas(servidor = 12, localesHoy = 7))
        assertEquals(12, PlanLogica.usadas(servidor = 3, localesHoy = 12))
    }

    @Test
    fun textos() {
        assertEquals("50 notificaciones restantes", PlanLogica.textoRestantes(50))
        assertEquals("1 notificación restante", PlanLogica.textoRestantes(1))
        assertEquals("Cuota de hoy agotada", PlanLogica.textoRestantes(0))
    }

    @Test
    fun freeLimitaYSinPlanConocidoNoBloquea() {
        assertFalse(PlanLogica.puedeVozBasica(Plan.FREE))
        assertTrue(PlanLogica.puedeVozBasica(Plan.PRO))
        assertTrue(PlanLogica.puedeVozBasica(null))
        assertTrue(PlanLogica.puedeVerPeriodo(Plan.FREE, PeriodoReporte.DIA))
        assertFalse(PlanLogica.puedeVerPeriodo(Plan.FREE, PeriodoReporte.MES))
        assertTrue(PlanLogica.puedeVerPeriodo(Plan.PRO, PeriodoReporte.ANIO))
        assertFalse(PlanLogica.puedeCrearOtroNegocio(Plan.FREE, 1))
        assertTrue(PlanLogica.puedeCrearOtroNegocio(Plan.FREE, 0))
        assertTrue(PlanLogica.puedeCrearOtroNegocio(Plan.PRO, 5))
    }

    @Test
    fun historialDe30DiasEnFree() {
        val ahora = 100L * 24 * 60 * 60 * 1000
        assertEquals(70L * 24 * 60 * 60 * 1000, PlanLogica.desdeHistorial(Plan.FREE, ahora))
        assertEquals(Long.MIN_VALUE, PlanLogica.desdeHistorial(Plan.PRO, ahora))
        assertEquals(Long.MIN_VALUE, PlanLogica.desdeHistorial(null, ahora))
    }

    @Test
    fun leeLaRespuestaDelServidorYReconoceLaCuotaAgotada() {
        val free = PlanLogica.aEstado(EstadoPlanDto(plan = "FREE", limite = 50, usadas = 4, maxColaboradores = 2))
        assertEquals(Plan.FREE, free.plan)
        assertEquals(46, PlanLogica.restantes(free.limite, free.usadas))
        assertEquals(Plan.PRO, PlanLogica.aEstado(EstadoPlanDto(plan = "pro")).plan)
        assertTrue(PlanLogica.esCuotaAgotada("CUOTA_AGOTADA: se acabó la cuota de hoy del plan Free"))
        assertFalse(PlanLogica.esCuotaAgotada("otro error"))
        assertFalse(PlanLogica.esCuotaAgotada(null))
        assertTrue(PlanLogica.mensajeDeLimite("LIMITE_NEGOCIOS: el plan Free permite 1 negocio(s)")!!.contains("1 negocio"))
        assertTrue(PlanLogica.mensajeDeLimite("x LIMITE_COLABORADORES x")!!.contains("2 colaboradores"))
        assertNull(PlanLogica.mensajeDeLimite("otro error"))
    }
}
