package com.flabs.cobrate

import com.flabs.cobrate.core.logic.TabRefreshManager
import com.flabs.cobrate.core.logic.TabRefreshState
import com.flabs.cobrate.core.model.EstadoInvitacion
import com.flabs.cobrate.core.model.InvitacionTrabajador
import com.flabs.cobrate.core.logic.SolicitudesIngreso
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TabRefreshAndInvitationTest {

    @Test
    fun testTabRefreshManagerDeduplicationAndStateFlow() = runTest {
        val route = "inicio"
        assertEquals(TabRefreshState.IDLE, TabRefreshManager.getState(route))

        var executionCount = 0
        TabRefreshManager.ejecutarRefresco(route) {
            executionCount++
            delay(100)
            Result.success(Unit)
        }

        assertEquals(1, executionCount)
        assertEquals(TabRefreshState.IDLE, TabRefreshManager.getState(route))
    }

    @Test
    fun testCodigosVigentesSortingByExpiration() {
        val ahora = System.currentTimeMillis()
        val inv1 = InvitacionTrabajador(
            id = "COD1",
            negocioId = "NEG1",
            codigo = "COD1",
            estado = EstadoInvitacion.PENDIENTE,
            fechaCreacion = ahora,
            expiracion = ahora + 100000L
        )
        val inv2 = InvitacionTrabajador(
            id = "COD2",
            negocioId = "NEG1",
            codigo = "COD2",
            estado = EstadoInvitacion.PENDIENTE,
            fechaCreacion = ahora,
            expiracion = ahora + 50000L
        )

        val lista = listOf(inv1, inv2)
        val vigentes = SolicitudesIngreso.codigosVigentes(lista, ahora).sortedBy { it.expiracion }

        assertEquals(2, vigentes.size)
        assertEquals("COD2", vigentes[0].codigo)
        assertEquals("COD1", vigentes[1].codigo)
    }
}
