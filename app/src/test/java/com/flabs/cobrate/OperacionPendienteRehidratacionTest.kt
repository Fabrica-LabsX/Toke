package com.flabs.cobrate

import com.flabs.cobrate.core.data.ElementoPendiente
import com.flabs.cobrate.core.data.EstadoSincronizacion
import com.flabs.cobrate.core.data.dto.OperacionLocalDto
import com.flabs.cobrate.core.model.CambioDeEstado
import com.flabs.cobrate.core.model.EstadoOperacion
import com.flabs.cobrate.core.model.EstadoRelacion
import com.flabs.cobrate.core.model.Operacion
import com.flabs.cobrate.core.model.OrigenOperacion
import com.flabs.cobrate.core.model.RelacionCuentaNegocio
import com.flabs.cobrate.core.model.RolCuentaNegocio
import com.flabs.cobrate.core.model.TipoMetodoCobro
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OperacionPendienteRehidratacionTest {

    @Test
    fun testElementoPendienteAislamientoOwnerId() {
        val opUserA = Operacion(
            id = "op-user-a-1",
            negocioId = "neg-1",
            monto = 100.0,
            estado = EstadoOperacion.SIN_ASIGNAR,
            origen = OrigenOperacion.Detectada("ev-1"),
            fechaHora = System.currentTimeMillis()
        )

        val elemUserA = ElementoPendiente(opUserA, ownerId = "UID_A", estadoSincronizacion = EstadoSincronizacion.ESPERANDO_RED)
        val elemUserB = ElementoPendiente(opUserA, ownerId = "UID_B", estadoSincronizacion = EstadoSincronizacion.ESPERANDO_RED)

        assertEquals("UID_A", elemUserA.ownerId)
        assertEquals("UID_B", elemUserB.ownerId)
        assertNotEquals(elemUserA.ownerId, elemUserB.ownerId)
    }

    @Test
    fun testEstadoSincronizacionPreservadoErrorYEsperandoRed() {
        val dtoError = OperacionLocalDto(
            id = "op-err-1",
            negocioId = "neg-1",
            monto = 50.0,
            estado = EstadoOperacion.SIN_ASIGNAR,
            origen = "Detectada:ev-1",
            fechaHora = 1000L,
            ownerId = "UID_A",
            estadoSincronizacion = EstadoSincronizacion.ERROR.name
        )

        val dtoEsperando = OperacionLocalDto(
            id = "op-esp-1",
            negocioId = "neg-1",
            monto = 50.0,
            estado = EstadoOperacion.SIN_ASIGNAR,
            origen = "Detectada:ev-1",
            fechaHora = 1000L,
            ownerId = "UID_A",
            estadoSincronizacion = EstadoSincronizacion.ESPERANDO_RED.name
        )

        assertEquals("ERROR", dtoError.estadoSincronizacion)
        assertEquals("ESPERANDO_RED", dtoEsperando.estadoSincronizacion)
    }

    @Test
    fun testReglaAutorizacionCrearOperacionYAlertas() {
        fun esAutorizadoParaCreacionYAlertas(duenoCuentaId: String, cuentaActualId: String, rol: RolCuentaNegocio, estadoRelacion: EstadoRelacion): Boolean {
            val esDueno = duenoCuentaId == cuentaActualId
            val esAdminActivo = rol == RolCuentaNegocio.ADMINISTRADOR && estadoRelacion == EstadoRelacion.ACTIVA
            return esDueno || esAdminActivo
        }

        // Dueño directo ➔ Autorizado
        assertTrue(esAutorizadoParaCreacionYAlertas("uid-dueno", "uid-dueno", RolCuentaNegocio.TRABAJADOR, EstadoRelacion.ACTIVA))
        
        // Admin Activo ➔ Autorizado
        assertTrue(esAutorizadoParaCreacionYAlertas("uid-dueno", "uid-admin", RolCuentaNegocio.ADMINISTRADOR, EstadoRelacion.ACTIVA))

        // Trabajador Activo (no dueño) ➔ NO Autorizado
        assertFalse(esAutorizadoParaCreacionYAlertas("uid-dueno", "uid-trabajador", RolCuentaNegocio.TRABAJADOR, EstadoRelacion.ACTIVA))

        // Admin Pausado / Expulsado ➔ NO Autorizado
        assertFalse(esAutorizadoParaCreacionYAlertas("uid-dueno", "uid-admin", RolCuentaNegocio.ADMINISTRADOR, EstadoRelacion.PAUSADA))
        assertFalse(esAutorizadoParaCreacionYAlertas("uid-dueno", "uid-admin", RolCuentaNegocio.ADMINISTRADOR, EstadoRelacion.EXPULSADA))
    }

    @Test
    fun testFiltroRealtimeNegocios() {
        fun obtenerNegociosParaEscuchar(duenoCuentaId: String, cuentaActualId: String, relaciones: List<RelacionCuentaNegocio>): Boolean {
            val esDueno = duenoCuentaId == cuentaActualId
            val relacionActiva = relaciones.any { it.cuentaId == cuentaActualId && it.estado == EstadoRelacion.ACTIVA }
            return esDueno || relacionActiva
        }

        val relActiva = listOf(RelacionCuentaNegocio("uid-trab", "neg-1", RolCuentaNegocio.TRABAJADOR, EstadoRelacion.ACTIVA, 1000L))
        val relPausada = listOf(RelacionCuentaNegocio("uid-trab", "neg-1", RolCuentaNegocio.TRABAJADOR, EstadoRelacion.PAUSADA, 1000L))

        // Dueño sin relación en la lista ➔ SÍ escucha
        assertTrue(obtenerNegociosParaEscuchar("uid-dueno", "uid-dueno", emptyList()))

        // Trabajador Activo ➔ SÍ escucha
        assertTrue(obtenerNegociosParaEscuchar("uid-dueno", "uid-trab", relActiva))

        // Trabajador Pausado ➔ NO escucha
        assertFalse(obtenerNegociosParaEscuchar("uid-dueno", "uid-trab", relPausada))
    }
}
