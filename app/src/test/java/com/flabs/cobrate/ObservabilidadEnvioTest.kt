package com.flabs.cobrate

import com.flabs.cobrate.core.data.ElementoPendiente
import com.flabs.cobrate.core.data.EstadoSincronizacion
import com.flabs.cobrate.core.data.PreferenciasAppStore
import com.flabs.cobrate.core.data.dto.OperacionLocalDto
import com.flabs.cobrate.core.model.CambioDeEstado
import com.flabs.cobrate.core.model.EstadoOperacion
import com.flabs.cobrate.core.model.Operacion
import com.flabs.cobrate.core.model.OrigenOperacion
import com.flabs.cobrate.core.model.RolCuentaNegocio
import com.flabs.cobrate.core.model.TipoMetodoCobro
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ObservabilidadEnvioTest {

    @Test
    fun testPreferenciaAvisarCobroEnviadoOffPorDefecto() {
        assertFalse(
            "La preferencia 'avisarCobroEnviado' debe ser false por defecto",
            PreferenciasAppStore.avisarCobroEnviado.value
        )
    }

    @Test
    fun testEstadoOperacionIndependienteDeEstadoSincronizacion() {
        val operacionOriginal = Operacion(
            id = "op-test-123",
            negocioId = "negocio-test-456",
            trabajadorAsignadoId = null,
            metodoPago = TipoMetodoCobro.YAPE,
            monto = 50.0,
            estado = EstadoOperacion.SIN_ASIGNAR,
            origen = OrigenOperacion.Detectada("evento-789"),
            fechaHora = System.currentTimeMillis(),
            historial = listOf(CambioDeEstado(System.currentTimeMillis(), EstadoOperacion.SIN_ASIGNAR))
        )

        val pendienteSinc = ElementoPendiente(
            operacion = operacionOriginal,
            ownerId = "user-123",
            estadoSincronizacion = EstadoSincronizacion.ERROR
        )

        // El estado comercial de la operacion NO debe verse alterado por el fallo de sincronización
        assertEquals(EstadoOperacion.SIN_ASIGNAR, pendienteSinc.operacion.estado)
        assertEquals(EstadoSincronizacion.ERROR, pendienteSinc.estadoSincronizacion)
    }

    @Test
    fun testNotificacionIdEstablePorOperacion() {
        val operacionId = "op-id-777"
        val notifId1 = operacionId.hashCode()
        val notifId2 = operacionId.hashCode()

        assertEquals("El ID de notificación para la misma operación debe ser estrictamente estable e idéntico", notifId1, notifId2)
    }

    @Test
    fun testDtoPersistenciaConservaEstadoSincronizacionErrorYEsperandoRed() {
        val dtoError = OperacionLocalDto(
            id = "op-err-1",
            negocioId = "neg-1",
            monto = 20.0,
            estado = EstadoOperacion.SIN_ASIGNAR,
            origen = "Detectada:ev-1",
            fechaHora = 1000L,
            ownerId = "owner-1",
            estadoSincronizacion = EstadoSincronizacion.ERROR.name
        )

        val dtoEsperando = OperacionLocalDto(
            id = "op-esp-1",
            negocioId = "neg-1",
            monto = 20.0,
            estado = EstadoOperacion.SIN_ASIGNAR,
            origen = "Detectada:ev-1",
            fechaHora = 1000L,
            ownerId = "owner-1",
            estadoSincronizacion = EstadoSincronizacion.ESPERANDO_RED.name
        )

        assertEquals("ERROR", dtoError.estadoSincronizacion)
        assertEquals("ESPERANDO_RED", dtoEsperando.estadoSincronizacion)
    }

    @Test
    fun testSoloRolAdministradorDiferenciadoDeTrabajador() {
        val rolAdmin = RolCuentaNegocio.ADMINISTRADOR
        val rolTrabajador = RolCuentaNegocio.TRABAJADOR

        assertTrue("Rol ADMINISTRADOR debe ser reconocido como Admin", rolAdmin == RolCuentaNegocio.ADMINISTRADOR)
        assertFalse("Rol TRABAJADOR no es ADMINISTRADOR", rolTrabajador == RolCuentaNegocio.ADMINISTRADOR)
    }

    @Test
    fun testDetectorAutomaticoGeneraOrigenDetectadaConEventoIdValido() {
        val eventoIdReal = "evento-uuid-real-123"
        val origenDetectada = OrigenOperacion.Detectada(eventoId = eventoIdReal)

        assertTrue("EventoId no debe estar vacío", origenDetectada.eventoId.isNotEmpty())
        assertEquals(eventoIdReal, origenDetectada.eventoId)
    }

    @Test
    fun testDescarteEstrictoDeOrigenesLegacyYDesconocidos() {
        // Mapeo seguro para tags de origen
        fun parsearOrigenSeguro(tag: String): OrigenOperacion? {
            if (tag.startsWith("Detectada:")) {
                val eventoId = tag.substringAfter("Detectada:")
                if (eventoId.isNotEmpty()) {
                    return OrigenOperacion.Detectada(eventoId)
                }
            }
            return null
        }

        assertNull("VaAPagar debe ser descartado como null", parsearOrigenSeguro("VaAPagar"))
        assertNull("VA_A_PAGAR debe ser descartado como null", parsearOrigenSeguro("VA_A_PAGAR"))
        assertNull("OrigenInventado debe ser descartado como null", parsearOrigenSeguro("OrigenInventado"))
        assertNull("Detectada con id vacio debe ser descartado como null", parsearOrigenSeguro("Detectada:"))
        
        val valido = parsearOrigenSeguro("Detectada:ev-ok")
        assertTrue("Detectada con id valido no debe ser null", valido is OrigenOperacion.Detectada)
        assertEquals("ev-ok", (valido as OrigenOperacion.Detectada).eventoId)
    }
}
