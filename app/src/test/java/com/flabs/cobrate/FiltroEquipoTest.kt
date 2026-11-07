package com.flabs.cobrate

import com.flabs.cobrate.core.model.EstadoInvitacion
import com.flabs.cobrate.core.model.EstadoRelacion
import com.flabs.cobrate.core.model.InvitacionTrabajador
import com.flabs.cobrate.features.business.SolicitudUi
import com.flabs.cobrate.features.business.TrabajadorRealInfo
import com.flabs.cobrate.features.equipo.FiltroEquipo
import com.flabs.cobrate.features.equipo.PestanaEquipo
import org.junit.Assert.assertEquals
import org.junit.Test

class FiltroEquipoTest {

    private val trabajadores = listOf(
        TrabajadorRealInfo("c1", "Pedro Martínez", EstadoRelacion.ACTIVA),
        TrabajadorRealInfo("c2", "Lucía Santos", EstadoRelacion.PAUSADA),
        TrabajadorRealInfo("c3", "Javier Zamora", EstadoRelacion.ACTIVA),
    )

    private fun invitacion(codigo: String) = InvitacionTrabajador(
        id = "i-$codigo",
        negocioId = "n1",
        codigo = codigo,
        estado = EstadoInvitacion.PENDIENTE,
    )

    @Test
    fun trabajadoresSinTextoDevuelveTodos_yConTextoIgnoraMayusculas() {
        assertEquals(3, FiltroEquipo.trabajadores(trabajadores, "  ").size)
        assertEquals(listOf("c2"), FiltroEquipo.trabajadores(trabajadores, "LUC").map { it.cuentaId })
        assertEquals(emptyList<String>(), FiltroEquipo.trabajadores(trabajadores, "zzz").map { it.cuentaId })
    }

    @Test
    fun codigosSeFiltranPorCodigo() {
        val lista = listOf(invitacion("AB-1234"), invitacion("ZX-9999"))
        assertEquals(listOf("ZX-9999"), FiltroEquipo.codigos(lista, "zx").map { it.codigo })
        assertEquals(2, FiltroEquipo.codigos(lista, "").size)
    }

    @Test
    fun solicitudesSeFiltranPorNombreOPorCodigoUsado() {
        val lista = listOf(
            SolicitudUi("s1", "Ana Morales", "AB-1234", 0L),
            SolicitudUi("s2", "Carlos Rivera", null, 0L),
        )
        assertEquals(listOf("s1"), FiltroEquipo.solicitudes(lista, "ana").map { it.cuentaId })
        assertEquals(listOf("s1"), FiltroEquipo.solicitudes(lista, "ab-12").map { it.cuentaId })
        assertEquals(listOf("s2"), FiltroEquipo.solicitudes(lista, "rivera").map { it.cuentaId })
        assertEquals(2, FiltroEquipo.solicitudes(lista, "").size)
    }

    @Test
    fun sinSolicitudesSeVuelveAMiEquipoSoloSiSeEstabaEnSolicitudes() {
        assertEquals(
            PestanaEquipo.MI_EQUIPO,
            FiltroEquipo.pestanaTrasCambioDeSolicitudes(PestanaEquipo.SOLICITUDES, 0),
        )
        assertEquals(
            PestanaEquipo.SOLICITUDES,
            FiltroEquipo.pestanaTrasCambioDeSolicitudes(PestanaEquipo.SOLICITUDES, 2),
        )
        assertEquals(
            PestanaEquipo.CODIGOS,
            FiltroEquipo.pestanaTrasCambioDeSolicitudes(PestanaEquipo.CODIGOS, 0),
        )
    }

    @Test
    fun pausarYReanudarAlternanElEstado() {
        assertEquals(EstadoRelacion.EXPULSADA, FiltroEquipo.estadoAlPausarOReanudar(EstadoRelacion.EXPULSADA))
        assertEquals(false, FiltroEquipo.apareceEnEquipo(EstadoRelacion.EXPULSADA))
        assertEquals(true, FiltroEquipo.apareceEnEquipo(EstadoRelacion.PAUSADA))
        assertEquals(EstadoRelacion.PAUSADA, FiltroEquipo.estadoAlPausarOReanudar(EstadoRelacion.ACTIVA))
        assertEquals(EstadoRelacion.ACTIVA, FiltroEquipo.estadoAlPausarOReanudar(EstadoRelacion.PAUSADA))
    }

    @Test
    fun cadaPestanaTieneSuPlaceholder() {
        assertEquals("Buscar colaborador...", FiltroEquipo.placeholder(PestanaEquipo.MI_EQUIPO))
        assertEquals("Buscar código...", FiltroEquipo.placeholder(PestanaEquipo.CODIGOS))
        assertEquals("Buscar solicitud...", FiltroEquipo.placeholder(PestanaEquipo.SOLICITUDES))
    }
}
