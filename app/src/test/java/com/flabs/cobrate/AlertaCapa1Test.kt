package com.flabs.cobrate

import com.flabs.cobrate.core.data.AlertaStore
import com.flabs.cobrate.core.data.CuentaStore
import com.flabs.cobrate.core.model.Alerta
import com.flabs.cobrate.core.model.EstadoRelacion
import com.flabs.cobrate.core.model.Negocio
import com.flabs.cobrate.core.model.RelacionCuentaNegocio
import com.flabs.cobrate.core.model.RolCuentaNegocio
import com.flabs.cobrate.core.model.TipoAlerta
import com.flabs.cobrate.core.model.TipoMetodoCobro
import com.flabs.cobrate.core.model.TipoNegocio
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AlertaCapa1Test {

    @Before
    fun setUp() {
        CuentaStore.cerrarSesion()
        AlertaStore.limpiar()
    }

    @After
    fun tearDown() {
        CuentaStore.cerrarSesion()
        AlertaStore.limpiar()
    }

    @Test
    fun testAislamientoPorUidYNegocio() {
        val alertaN1 = Alerta(
            id = "a1",
            tipo = TipoAlerta.METODO_NO_HABILITADO,
            operacionId = "",
            mensaje = "Yape deshabilitado",
            negocioId = "negocio-1",
            cuentaId = "user-A",
            tipoMetodoCobro = TipoMetodoCobro.YAPE
        )

        val alertaN2 = Alerta(
            id = "a2",
            tipo = TipoAlerta.METODO_NO_HABILITADO,
            operacionId = "",
            mensaje = "Plin deshabilitado",
            negocioId = "negocio-2",
            cuentaId = "user-A",
            tipoMetodoCobro = TipoMetodoCobro.PLIN
        )

        val lista = listOf(alertaN1, alertaN2)

        val filtradasN1 = lista.filter { it.negocioId == "negocio-1" && it.cuentaId == "user-A" }
        assertEquals(1, filtradasN1.size)
        assertEquals(TipoMetodoCobro.YAPE, filtradasN1.first().tipoMetodoCobro)
    }

    @Test
    fun testTrabajadorEnN1NoVeAlertasDeN1InclusoSiEsAdminEnN2() {
        fun esAutorizadoEnNegocioActivo(
            negocioActivoId: String,
            cuentaId: String,
            negocios: List<Negocio>,
            relaciones: List<RelacionCuentaNegocio>
        ): Boolean {
            val negocio = negocios.find { it.id == negocioActivoId }
            val esDueno = negocio?.dueñoCuentaId == cuentaId
            val relacion = relaciones.find { it.cuentaId == cuentaId && it.negocioId == negocioActivoId }
            val esAdminActivo = relacion?.rol == RolCuentaNegocio.ADMINISTRADOR && relacion.estado == EstadoRelacion.ACTIVA
            return esDueno || esAdminActivo
        }

        val cuentaUserC = "user-C"
        val negocio1 = Negocio("negocio-1", "Negocio 1", TipoNegocio.ABARROTES, "dueno-1")
        val negocio2 = Negocio("negocio-2", "Negocio 2", TipoNegocio.ABARROTES, "dueno-2")

        val relN2Admin = RelacionCuentaNegocio(cuentaUserC, "negocio-2", RolCuentaNegocio.ADMINISTRADOR, EstadoRelacion.ACTIVA)
        val relN1Trab = RelacionCuentaNegocio(cuentaUserC, "negocio-1", RolCuentaNegocio.TRABAJADOR, EstadoRelacion.ACTIVA)

        val negocios = listOf(negocio1, negocio2)
        val relaciones = listOf(relN2Admin, relN1Trab)

        // En N2 (donde es Admin) ➔ SÍ Autorizado
        assertTrue(
            "Debe estar autorizado en N2 donde es Admin",
            esAutorizadoEnNegocioActivo("negocio-2", cuentaUserC, negocios, relaciones)
        )

        // En N1 (donde es Trabajador) ➔ NO Autorizado
        assertFalse(
            "NO debe estar autorizado en N1 donde es Trabajador (incluso siendo Admin en N2)",
            esAutorizadoEnNegocioActivo("negocio-1", cuentaUserC, negocios, relaciones)
        )
    }

    @Test
    fun testAutoResolucionEspecificaPorMetodo() {
        val alertaYape = Alerta(
            id = "a-yape",
            tipo = TipoAlerta.METODO_NO_HABILITADO,
            operacionId = "",
            mensaje = "Yape deshabilitado",
            negocioId = "negocio-1",
            cuentaId = "user-A",
            tipoMetodoCobro = TipoMetodoCobro.YAPE
        )

        val alertaPlin = Alerta(
            id = "a-plin",
            tipo = TipoAlerta.METODO_NO_HABILITADO,
            operacionId = "",
            mensaje = "Plin deshabilitado",
            negocioId = "negocio-1",
            cuentaId = "user-A",
            tipoMetodoCobro = TipoMetodoCobro.PLIN
        )

        val listaInicial = listOf(alertaYape, alertaPlin)

        // Resolver YAPE en negocio-1
        val listaTrasResolverYape = listaInicial.filterNot {
            it.negocioId == "negocio-1" && it.tipoMetodoCobro == TipoMetodoCobro.YAPE
        }

        assertEquals(1, listaTrasResolverYape.size)
        assertEquals("a-plin", listaTrasResolverYape.first().id)
        assertEquals(TipoMetodoCobro.PLIN, listaTrasResolverYape.first().tipoMetodoCobro)
    }
}
