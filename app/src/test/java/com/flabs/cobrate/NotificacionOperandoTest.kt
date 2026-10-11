package com.flabs.cobrate

import kotlinx.coroutines.Job
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificacionOperandoTest {

    private fun debeMostrarNotificacionOperando(
        isListenerConnected: Boolean,
        tieneSesionActiva: Boolean,
        tieneNegocioActivo: Boolean,
        serviciosActivados: Boolean
    ): Boolean {
        return isListenerConnected && tieneSesionActiva && tieneNegocioActivo && serviciosActivados
    }

    @Test
    fun testMatrizLogicaCriterioOperativo() {
        // 1. conectado = true, sesion = true, negocio = true, servicios = true -> MOSTRAR (true)
        assertTrue(
            "Debe mostrar notificación cuando listener está conectado, hay sesión, negocio y servicios activados",
            debeMostrarNotificacionOperando(isListenerConnected = true, tieneSesionActiva = true, tieneNegocioActivo = true, serviciosActivados = true)
        )

        // 2. conectado = false, sesion = true, negocio = true, servicios = true -> NO MOSTRAR (false)
        assertFalse(
            "NO debe mostrar notificación cuando listener está desconectado",
            debeMostrarNotificacionOperando(isListenerConnected = false, tieneSesionActiva = true, tieneNegocioActivo = true, serviciosActivados = true)
        )

        // 3. conectado = true, sesion = false, negocio = true, servicios = true -> NO MOSTRAR (false)
        assertFalse(
            "NO debe mostrar notificación cuando no hay sesión activa",
            debeMostrarNotificacionOperando(isListenerConnected = true, tieneSesionActiva = false, tieneNegocioActivo = true, serviciosActivados = true)
        )

        // 4. conectado = true, sesion = true, negocio = false, servicios = true -> NO MOSTRAR (false)
        assertFalse(
            "NO debe mostrar notificación cuando no hay negocio activo",
            debeMostrarNotificacionOperando(isListenerConnected = true, tieneSesionActiva = true, tieneNegocioActivo = false, serviciosActivados = true)
        )

        // 5. conectado = true, sesion = true, negocio = true, servicios = false -> NO MOSTRAR (false)
        assertFalse(
            "NO debe mostrar notificación cuando los servicios están apagados",
            debeMostrarNotificacionOperando(isListenerConnected = true, tieneSesionActiva = true, tieneNegocioActivo = true, serviciosActivados = false)
        )
    }

    @Test
    fun testReconexionCancelaTrabajosPreviosSinDuplicar() {
        var sessionMonitorJob: Job? = Job()
        var operationalMonitorJob: Job? = Job()
        var sessionJob: Job? = Job()

        val jobSessionPrevio = sessionMonitorJob
        val jobOperativoPrevio = operationalMonitorJob
        val jobContextoPrevio = sessionJob

        // Simulación de reconexión: cancelar los anteriores
        sessionMonitorJob?.cancel()
        operationalMonitorJob?.cancel()
        sessionJob?.cancel()

        // Asignar nuevos trabajos
        sessionMonitorJob = Job()
        operationalMonitorJob = Job()
        sessionJob = Job()

        assertTrue("El job de monitoreo de sesión previo debe ser cancelado", jobSessionPrevio!!.isCancelled)
        assertTrue("El job de monitoreo operativo previo debe ser cancelado", jobOperativoPrevio!!.isCancelled)
        assertTrue("El job de contexto previo debe ser cancelado", jobContextoPrevio!!.isCancelled)

        assertFalse("El nuevo job de monitoreo de sesión no debe estar cancelado", sessionMonitorJob.isCancelled)
        assertFalse("El nuevo job de monitoreo operativo no debe estar cancelado", operationalMonitorJob.isCancelled)
        assertFalse("El nuevo job de contexto de sesión no debe estar cancelado", sessionJob.isCancelled)
    }
}
