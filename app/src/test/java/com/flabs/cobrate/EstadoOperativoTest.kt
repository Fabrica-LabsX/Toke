package com.flabs.cobrate

import com.flabs.cobrate.core.logic.EstadoOperativoLogic
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EstadoOperativoTest {

    @Test
    fun testDebeOperarEvaluacionDe8Combinaciones() {
        // Combinación 1: encendido = true, haySesion = true, permisoNotificaciones = true -> TRUE
        assertTrue(EstadoOperativoLogic.debeOperar(encendido = true, haySesion = true, permisoNotificaciones = true))

        // Combinación 2: encendido = true, haySesion = true, permisoNotificaciones = false -> FALSE
        assertFalse(EstadoOperativoLogic.debeOperar(encendido = true, haySesion = true, permisoNotificaciones = false))

        // Combinación 3: encendido = true, haySesion = false, permisoNotificaciones = true -> FALSE
        assertFalse(EstadoOperativoLogic.debeOperar(encendido = true, haySesion = false, permisoNotificaciones = true))

        // Combinación 4: encendido = true, haySesion = false, permisoNotificaciones = false -> FALSE
        assertFalse(EstadoOperativoLogic.debeOperar(encendido = true, haySesion = false, permisoNotificaciones = false))

        // Combinación 5: encendido = false, haySesion = true, permisoNotificaciones = true -> FALSE
        assertFalse(EstadoOperativoLogic.debeOperar(encendido = false, haySesion = true, permisoNotificaciones = true))

        // Combinación 6: encendido = false, haySesion = true, permisoNotificaciones = false -> FALSE
        assertFalse(EstadoOperativoLogic.debeOperar(encendido = false, haySesion = true, permisoNotificaciones = false))

        // Combinación 7: encendido = false, haySesion = false, permisoNotificaciones = true -> FALSE
        assertFalse(EstadoOperativoLogic.debeOperar(encendido = false, haySesion = false, permisoNotificaciones = true))

        // Combinación 8: encendido = false, haySesion = false, permisoNotificaciones = false -> FALSE
        assertFalse(EstadoOperativoLogic.debeOperar(encendido = false, haySesion = false, permisoNotificaciones = false))
    }
}
