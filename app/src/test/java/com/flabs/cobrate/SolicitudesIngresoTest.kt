package com.flabs.cobrate

import com.flabs.cobrate.core.logic.SolicitudesIngreso
import com.flabs.cobrate.core.model.EstadoInvitacion
import com.flabs.cobrate.core.model.EstadoRelacion
import com.flabs.cobrate.core.model.InvitacionTrabajador
import com.flabs.cobrate.core.model.RelacionCuentaNegocio
import com.flabs.cobrate.core.model.RolCuentaNegocio
import org.junit.Assert.assertEquals
import org.junit.Test

class SolicitudesIngresoTest {

    private val negocioId = "negocio-1"
    private val otroNegocioId = "negocio-2"

    @Test
    fun testPendientesFiltraSoloSolicitadaDelNegocioYOrdenaPorFecha() {
        val rel1 = RelacionCuentaNegocio("c1", negocioId, RolCuentaNegocio.TRABAJADOR, EstadoRelacion.SOLICITADA, fechaVinculacion = 1000L)
        val rel2 = RelacionCuentaNegocio("c2", negocioId, RolCuentaNegocio.TRABAJADOR, EstadoRelacion.SOLICITADA, fechaVinculacion = 500L)
        val relActiva = RelacionCuentaNegocio("c3", negocioId, RolCuentaNegocio.TRABAJADOR, EstadoRelacion.ACTIVA, fechaVinculacion = 100L)
        val relAdmin = RelacionCuentaNegocio("c4", negocioId, RolCuentaNegocio.ADMINISTRADOR, EstadoRelacion.SOLICITADA, fechaVinculacion = 200L)
        val relOtroNegocio = RelacionCuentaNegocio("c5", otroNegocioId, RolCuentaNegocio.TRABAJADOR, EstadoRelacion.SOLICITADA, fechaVinculacion = 300L)
        val relRechazada = RelacionCuentaNegocio("c6", negocioId, RolCuentaNegocio.TRABAJADOR, EstadoRelacion.RECHAZADA, fechaVinculacion = 400L)

        val relaciones = listOf(rel1, rel2, relActiva, relAdmin, relOtroNegocio, relRechazada)

        val resultado = SolicitudesIngreso.pendientes(relaciones, negocioId)

        assertEquals(2, resultado.size)
        // La más antigua primero (fechaVinculacion 500L -> c2, luego 1000L -> c1)
        assertEquals("c2", resultado[0].cuentaId)
        assertEquals("c1", resultado[1].cuentaId)
    }

    @Test
    fun testCodigosVigentesExcluyeNoPendientesYVencidosYOrdenaPorExpiracion() {
        val ahora = 10_000L
        val invVigente2 = InvitacionTrabajador("i2", negocioId, "COD2", EstadoInvitacion.PENDIENTE, fechaCreacion = 1000L, expiracion = 20_000L)
        val invVigente1 = InvitacionTrabajador("i1", negocioId, "COD1", EstadoInvitacion.PENDIENTE, fechaCreacion = 1000L, expiracion = 15_000L)
        val invVencida = InvitacionTrabajador("i3", negocioId, "COD3", EstadoInvitacion.PENDIENTE, fechaCreacion = 1000L, expiracion = 10_000L) // vence justo ahora
        val invAceptada = InvitacionTrabajador("i4", negocioId, "COD4", EstadoInvitacion.ACEPTADA, fechaCreacion = 1000L, expiracion = 30_000L)
        val invRevocada = InvitacionTrabajador("i5", negocioId, "COD5", EstadoInvitacion.REVOCADA, fechaCreacion = 1000L, expiracion = 30_000L)
        val invExpirada = InvitacionTrabajador("i6", negocioId, "COD6", EstadoInvitacion.EXPIRADA, fechaCreacion = 1000L, expiracion = 30_000L)

        val invitaciones = listOf(invVigente2, invVigente1, invVencida, invAceptada, invRevocada, invExpirada)

        val resultado = SolicitudesIngreso.codigosVigentes(invitaciones, ahora)

        assertEquals(2, resultado.size)
        // Ordena por expiracion (15_000L -> i1, luego 20_000L -> i2)
        assertEquals("COD1", resultado[0].codigo)
        assertEquals("COD2", resultado[1].codigo)
    }

    @Test
    fun testSegundosRestantesNuncaEsNegativo() {
        val ahora = 10_000L
        val invVigente = InvitacionTrabajador("i1", negocioId, "COD1", EstadoInvitacion.PENDIENTE, fechaCreacion = 0L, expiracion = 25_000L)
        val invVencida = InvitacionTrabajador("i2", negocioId, "COD2", EstadoInvitacion.PENDIENTE, fechaCreacion = 0L, expiracion = 5_000L)

        assertEquals(15L, SolicitudesIngreso.segundosRestantes(invVigente, ahora))
        assertEquals(0L, SolicitudesIngreso.segundosRestantes(invVencida, ahora))
    }

    @Test
    fun testFormatearRestante() {
        assertEquals("02:05", SolicitudesIngreso.formatearRestante(125L))
        assertEquals("00:00", SolicitudesIngreso.formatearRestante(0L))
        assertEquals("00:00", SolicitudesIngreso.formatearRestante(-10L))
        assertEquals("10:00", SolicitudesIngreso.formatearRestante(600L))
    }
}
