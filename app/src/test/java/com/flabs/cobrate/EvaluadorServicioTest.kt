package com.flabs.cobrate

import com.flabs.cobrate.core.logic.Requisito
import com.flabs.cobrate.features.permisos.EstadoServicio
import com.flabs.cobrate.features.permisos.EvaluadorServicio
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EvaluadorServicioTest {

    private fun evaluar(
        acceso: Boolean,
        bateria: Boolean,
        requiereAcceso: Boolean = true,
        requiereBateria: Boolean = true,
    ) = EvaluadorServicio.evaluar(requiereAcceso, acceso, requiereBateria, bateria)

    @Test
    fun sinAccesoANotificacionesElServicioEstaDesactivado_aunqueLaBateriaEsteBien() {
        assertEquals(EstadoServicio.DESACTIVADO, evaluar(acceso = false, bateria = true))
        assertEquals(EstadoServicio.DESACTIVADO, evaluar(acceso = false, bateria = false))
    }

    @Test
    fun conAccesoPeroSinBateriaEsParcial() {
        assertEquals(EstadoServicio.PARCIAL, evaluar(acceso = true, bateria = false))
    }

    @Test
    fun conTodoConcedidoEstaOperativo() {
        assertEquals(EstadoServicio.OPERATIVO, evaluar(acceso = true, bateria = true))
    }

    @Test
    fun unPermisoQueNoSeRequiereNoCuenta() {
        // Un trabajador no necesita el acceso a notificaciones del sistema.
        assertEquals(
            EstadoServicio.OPERATIVO,
            evaluar(acceso = false, bateria = true, requiereAcceso = false),
        )
        assertEquals(
            EstadoServicio.OPERATIVO,
            evaluar(acceso = true, bateria = false, requiereBateria = false),
        )
    }

    @Test
    fun requisitosDeDuenoYDeTrabajador() {
        val dueno = setOf(Requisito.AVISOS, Requisito.BATERIA, Requisito.ACCESO_A_NOTIFICACIONES)
        val trabajador = setOf(Requisito.AVISOS, Requisito.BATERIA)
        assertTrue(EvaluadorServicio.requiereAccesoNotificaciones(dueno))
        assertFalse(EvaluadorServicio.requiereAccesoNotificaciones(trabajador))
        assertTrue(EvaluadorServicio.requiereBateria(trabajador))
    }

    @Test
    fun sinInformacionDeRequisitosSeExigeTodo() {
        assertTrue(EvaluadorServicio.requiereAccesoNotificaciones(emptySet()))
        assertTrue(EvaluadorServicio.requiereBateria(emptySet()))
    }
}
