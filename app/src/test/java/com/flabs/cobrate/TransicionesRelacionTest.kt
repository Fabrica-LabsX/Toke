package com.flabs.cobrate

import com.flabs.cobrate.core.logic.TransicionesRelacion
import com.flabs.cobrate.core.model.EstadoRelacion
import com.flabs.cobrate.core.model.RelacionCuentaNegocio
import com.flabs.cobrate.core.model.RolCuentaNegocio
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TransicionesRelacionTest {

    private val cuentaId = "trabajador-1"
    private val otraCuentaId = "trabajador-2"
    private val negocioId = "negocio-a"

    @Test
    fun testSolicitadaAActivaEsRecienActivada() {
        val antes = listOf(
            RelacionCuentaNegocio(cuentaId, negocioId, RolCuentaNegocio.TRABAJADOR, EstadoRelacion.SOLICITADA)
        )
        val ahora = listOf(
            RelacionCuentaNegocio(cuentaId, negocioId, RolCuentaNegocio.TRABAJADOR, EstadoRelacion.ACTIVA)
        )

        val activadas = TransicionesRelacion.recienActivadas(cuentaId, antes, ahora)
        val desactivadas = TransicionesRelacion.dejaronDeSerActivas(cuentaId, antes, ahora)

        assertEquals(listOf(negocioId), activadas)
        assertTrue(desactivadas.isEmpty())
    }

    @Test
    fun testPausadaAActivaEsRecienActivada() {
        val antes = listOf(
            RelacionCuentaNegocio(cuentaId, negocioId, RolCuentaNegocio.TRABAJADOR, EstadoRelacion.PAUSADA)
        )
        val ahora = listOf(
            RelacionCuentaNegocio(cuentaId, negocioId, RolCuentaNegocio.TRABAJADOR, EstadoRelacion.ACTIVA)
        )

        val activadas = TransicionesRelacion.recienActivadas(cuentaId, antes, ahora)
        val desactivadas = TransicionesRelacion.dejaronDeSerActivas(cuentaId, antes, ahora)

        assertEquals(listOf(negocioId), activadas)
        assertTrue(desactivadas.isEmpty())
    }

    @Test
    fun testNuevaRelacionActivaEsRecienActivada() {
        val antes = emptyList<RelacionCuentaNegocio>()
        val ahora = listOf(
            RelacionCuentaNegocio(cuentaId, negocioId, RolCuentaNegocio.TRABAJADOR, EstadoRelacion.ACTIVA)
        )

        val activadas = TransicionesRelacion.recienActivadas(cuentaId, antes, ahora)
        assertEquals(listOf(negocioId), activadas)
    }

    @Test
    fun testActivaAActivaNoGeneraTransicion() {
        val relaciones = listOf(
            RelacionCuentaNegocio(cuentaId, negocioId, RolCuentaNegocio.TRABAJADOR, EstadoRelacion.ACTIVA)
        )

        val activadas = TransicionesRelacion.recienActivadas(cuentaId, relaciones, relaciones)
        val desactivadas = TransicionesRelacion.dejaronDeSerActivas(cuentaId, relaciones, relaciones)

        assertTrue(activadas.isEmpty())
        assertTrue(desactivadas.isEmpty())
    }

    @Test
    fun testActivaAPausadaDejoDeSerActiva() {
        val antes = listOf(
            RelacionCuentaNegocio(cuentaId, negocioId, RolCuentaNegocio.TRABAJADOR, EstadoRelacion.ACTIVA)
        )
        val ahora = listOf(
            RelacionCuentaNegocio(cuentaId, negocioId, RolCuentaNegocio.TRABAJADOR, EstadoRelacion.PAUSADA)
        )

        val activadas = TransicionesRelacion.recienActivadas(cuentaId, antes, ahora)
        val desactivadas = TransicionesRelacion.dejaronDeSerActivas(cuentaId, antes, ahora)

        assertTrue(activadas.isEmpty())
        assertEquals(listOf(negocioId), desactivadas)
    }

    @Test
    fun testActivaASinRelacionDejoDeSerActiva() {
        val antes = listOf(
            RelacionCuentaNegocio(cuentaId, negocioId, RolCuentaNegocio.TRABAJADOR, EstadoRelacion.ACTIVA)
        )
        val ahora = emptyList<RelacionCuentaNegocio>()

        val activadas = TransicionesRelacion.recienActivadas(cuentaId, antes, ahora)
        val desactivadas = TransicionesRelacion.dejaronDeSerActivas(cuentaId, antes, ahora)

        assertTrue(activadas.isEmpty())
        assertEquals(listOf(negocioId), desactivadas)
    }

    @Test
    fun testRelacionesDeOtraCuentaSeIgnoran() {
        val antes = listOf(
            RelacionCuentaNegocio(otraCuentaId, negocioId, RolCuentaNegocio.TRABAJADOR, EstadoRelacion.SOLICITADA)
        )
        val ahora = listOf(
            RelacionCuentaNegocio(otraCuentaId, negocioId, RolCuentaNegocio.TRABAJADOR, EstadoRelacion.ACTIVA)
        )

        val activadas = TransicionesRelacion.recienActivadas(cuentaId, antes, ahora)
        val desactivadas = TransicionesRelacion.dejaronDeSerActivas(cuentaId, antes, ahora)

        assertTrue(activadas.isEmpty())
        assertTrue(desactivadas.isEmpty())
    }
}
