package com.flabs.cobrate

import com.flabs.cobrate.core.logic.AccesoNegocio
import com.flabs.cobrate.core.model.EstadoRelacion
import com.flabs.cobrate.core.model.RelacionCuentaNegocio
import com.flabs.cobrate.core.model.RolCuentaNegocio
import org.junit.Assert
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AccesoNegocioTest {

    private val cuentaId = "cuenta-123"
    private val negocioId = "negocio-abc"

    @Test
    fun testRelacionActivaDevuelveFalse() {
        val relaciones = listOf(
            RelacionCuentaNegocio(cuentaId, negocioId, RolCuentaNegocio.TRABAJADOR, EstadoRelacion.ACTIVA)
        )
        assertFalse(AccesoNegocio.estaPausado(cuentaId, negocioId, relaciones))
    }

    @Test
    fun testRelacionPausadaDevuelveTrue() {
        val relaciones = listOf(
            RelacionCuentaNegocio(cuentaId, negocioId, RolCuentaNegocio.TRABAJADOR, EstadoRelacion.PAUSADA)
        )
        assertTrue(AccesoNegocio.estaPausado(cuentaId, negocioId, relaciones))
    }

    @Test
    fun testRelacionExpulsadaDevuelveFalse() {
        val relaciones = listOf(
            RelacionCuentaNegocio(cuentaId, negocioId, RolCuentaNegocio.TRABAJADOR, EstadoRelacion.EXPULSADA)
        )
        assertFalse(AccesoNegocio.estaPausado(cuentaId, negocioId, relaciones))
    }

    @Test
    fun testOtroNegocioDevuelveFalse() {
        val relaciones = listOf(
            RelacionCuentaNegocio(cuentaId, "otro-negocio", RolCuentaNegocio.TRABAJADOR, EstadoRelacion.PAUSADA)
        )
        assertFalse(AccesoNegocio.estaPausado(cuentaId, negocioId, relaciones))
    }

    @Test
    fun testNegocioIdNuloDevuelveFalse() {
        val relaciones = listOf(
            RelacionCuentaNegocio(cuentaId, negocioId, RolCuentaNegocio.TRABAJADOR, EstadoRelacion.PAUSADA)
        )
        assertFalse(AccesoNegocio.estaPausado(cuentaId, null, relaciones))
    }

    @Test
    fun testEstadoDeAccesoNormal() {
        val relActiva = listOf(RelacionCuentaNegocio(cuentaId, negocioId, RolCuentaNegocio.TRABAJADOR, EstadoRelacion.ACTIVA))
        val relExpulsada = listOf(RelacionCuentaNegocio(cuentaId, negocioId, RolCuentaNegocio.TRABAJADOR, EstadoRelacion.EXPULSADA))
        val sinRelacion = emptyList<RelacionCuentaNegocio>()

        Assert.assertEquals(AccesoNegocio.EstadoAcceso.NORMAL, AccesoNegocio.estadoDeAcceso(cuentaId, negocioId, relActiva))
        Assert.assertEquals(AccesoNegocio.EstadoAcceso.NORMAL, AccesoNegocio.estadoDeAcceso(cuentaId, negocioId, relExpulsada))
        Assert.assertEquals(AccesoNegocio.EstadoAcceso.NORMAL, AccesoNegocio.estadoDeAcceso(cuentaId, negocioId, sinRelacion))
        Assert.assertEquals(AccesoNegocio.EstadoAcceso.NORMAL, AccesoNegocio.estadoDeAcceso(cuentaId, null, relActiva))
    }

    @Test
    fun testEstadoDeAccesoPausado() {
        val relPausada = listOf(RelacionCuentaNegocio(cuentaId, negocioId, RolCuentaNegocio.TRABAJADOR, EstadoRelacion.PAUSADA))
        Assert.assertEquals(AccesoNegocio.EstadoAcceso.PAUSADO, AccesoNegocio.estadoDeAcceso(cuentaId, negocioId, relPausada))
    }

    @Test
    fun testEstadoDeAccesoSolicitado() {
        val relSolicitada = listOf(RelacionCuentaNegocio(cuentaId, negocioId, RolCuentaNegocio.TRABAJADOR, EstadoRelacion.SOLICITADA))
        Assert.assertEquals(AccesoNegocio.EstadoAcceso.SOLICITADO, AccesoNegocio.estadoDeAcceso(cuentaId, negocioId, relSolicitada))
    }

    @Test
    fun testEstadoDeAccesoRechazado() {
        val relRechazada = listOf(RelacionCuentaNegocio(cuentaId, negocioId, RolCuentaNegocio.TRABAJADOR, EstadoRelacion.RECHAZADA))
        Assert.assertEquals(AccesoNegocio.EstadoAcceso.RECHAZADO, AccesoNegocio.estadoDeAcceso(cuentaId, negocioId, relRechazada))
    }
}
