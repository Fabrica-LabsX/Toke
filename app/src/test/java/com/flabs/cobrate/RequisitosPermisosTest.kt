package com.flabs.cobrate

import com.flabs.cobrate.core.logic.Requisito
import com.flabs.cobrate.core.logic.RequisitosPermisos
import com.flabs.cobrate.core.model.EstadoRelacion
import com.flabs.cobrate.core.model.RelacionCuentaNegocio
import com.flabs.cobrate.core.model.RolCuentaNegocio
import org.junit.Assert.assertEquals
import org.junit.Test

class RequisitosPermisosTest {

    private val uid = "usr123"

    @Test
    fun testSoloTrabajadorActivo() {
        val relaciones = listOf(
            RelacionCuentaNegocio(
                cuentaId = uid,
                negocioId = "neg1",
                rol = RolCuentaNegocio.TRABAJADOR,
                estado = EstadoRelacion.ACTIVA
            )
        )
        val res = RequisitosPermisos.para(uid, relaciones)
        assertEquals(setOf(Requisito.AVISOS, Requisito.BATERIA), res)
    }

    @Test
    fun testAdministradorActivo() {
        val relaciones = listOf(
            RelacionCuentaNegocio(
                cuentaId = uid,
                negocioId = "neg1",
                rol = RolCuentaNegocio.ADMINISTRADOR,
                estado = EstadoRelacion.ACTIVA
            )
        )
        val res = RequisitosPermisos.para(uid, relaciones)
        assertEquals(setOf(Requisito.AVISOS, Requisito.BATERIA, Requisito.ACCESO_A_NOTIFICACIONES), res)
    }

    @Test
    fun testDuenoEnUnNegocioYTrabajadorEnOtro() {
        val relaciones = listOf(
            RelacionCuentaNegocio(
                cuentaId = uid,
                negocioId = "neg1",
                rol = RolCuentaNegocio.TRABAJADOR,
                estado = EstadoRelacion.ACTIVA
            ),
            RelacionCuentaNegocio(
                cuentaId = uid,
                negocioId = "neg2",
                rol = RolCuentaNegocio.ADMINISTRADOR,
                estado = EstadoRelacion.ACTIVA
            )
        )
        val res = RequisitosPermisos.para(uid, relaciones)
        assertEquals(setOf(Requisito.AVISOS, Requisito.BATERIA, Requisito.ACCESO_A_NOTIFICACIONES), res)
    }

    @Test
    fun testAdministradorPausadoOTrabajadorSolicitante() {
        val relaciones = listOf(
            RelacionCuentaNegocio(
                cuentaId = uid,
                negocioId = "neg1",
                rol = RolCuentaNegocio.ADMINISTRADOR,
                estado = EstadoRelacion.PAUSADA
            ),
            RelacionCuentaNegocio(
                cuentaId = uid,
                negocioId = "neg2",
                rol = RolCuentaNegocio.TRABAJADOR,
                estado = EstadoRelacion.SOLICITADA
            )
        )
        val res = RequisitosPermisos.para(uid, relaciones)
        assertEquals(setOf(Requisito.AVISOS, Requisito.BATERIA), res)
    }

    @Test
    fun testSinRelaciones() {
        val res = RequisitosPermisos.para(uid, emptyList())
        assertEquals(setOf(Requisito.AVISOS, Requisito.BATERIA), res)
    }
}
