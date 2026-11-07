package com.flabs.cobrate

import com.flabs.cobrate.core.model.Cuenta
import com.flabs.cobrate.core.model.EstadoRelacion
import com.flabs.cobrate.core.model.Negocio
import com.flabs.cobrate.core.model.RelacionCuentaNegocio
import com.flabs.cobrate.core.model.RolCuentaNegocio
import com.flabs.cobrate.core.model.TipoNegocio
import com.flabs.cobrate.features.negocios.EstadoEnNegocio
import com.flabs.cobrate.features.negocios.ListaNegocios
import com.flabs.cobrate.features.negocios.RolEnNegocio
import com.flabs.cobrate.features.negocios.resolverNombreDueno
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ListaNegociosTest {

    private val yo = "yo"

    private fun negocio(id: String, dueno: String) =
        Negocio(id = id, nombre = "Negocio $id", tipo = TipoNegocio.OTRO, dueñoCuentaId = dueno)

    private fun relacion(negocioId: String, estado: EstadoRelacion, rol: RolCuentaNegocio = RolCuentaNegocio.TRABAJADOR) =
        RelacionCuentaNegocio(cuentaId = yo, negocioId = negocioId, rol = rol, estado = estado)

    private val negocios = listOf(
        negocio("propio", yo),
        negocio("activo", "otro"),
        negocio("pausado", "otro"),
        negocio("pendiente", "otro"),
        negocio("expulsado", "otro"),
        negocio("sinRelacion", "otro"),
    )

    private val relaciones = listOf(
        relacion("activo", EstadoRelacion.ACTIVA),
        relacion("pausado", EstadoRelacion.PAUSADA),
        relacion("pendiente", EstadoRelacion.SOLICITADA),
        relacion("expulsado", EstadoRelacion.EXPULSADA),
        // Una relación de trabajador sobre un negocio propio no debe duplicarlo.
        relacion("propio", EstadoRelacion.ACTIVA),
    )

    private fun construir(activo: String? = null) = ListaNegocios.construir(negocios, relaciones, yo, activo)

    @Test
    fun sinCuentaLaListaEstaVacia() {
        assertTrue(ListaNegocios.construir(negocios, relaciones, null, null).isEmpty())
    }

    @Test
    fun primeroLosPropiosYDespuesLasColaboraciones_sinDuplicadosNiExcluidos() {
        val ids = construir().map { it.negocio.id }
        assertEquals(listOf("propio", "activo", "pausado", "pendiente"), ids)
    }

    @Test
    fun rolYEstadoSegunLaRelacion() {
        val porId = construir().associateBy { it.negocio.id }
        assertEquals(RolEnNegocio.DUENO, porId.getValue("propio").rol)
        assertEquals(EstadoEnNegocio.DISPONIBLE, porId.getValue("propio").estado)
        assertEquals(RolEnNegocio.COLABORADOR, porId.getValue("activo").rol)
        assertEquals(EstadoEnNegocio.DISPONIBLE, porId.getValue("activo").estado)
        assertEquals(EstadoEnNegocio.PAUSADO, porId.getValue("pausado").estado)
        assertEquals(EstadoEnNegocio.PENDIENTE, porId.getValue("pendiente").estado)
    }

    @Test
    fun soloSePuedeEntrarAlosDisponibles() {
        val porId = construir().associateBy { it.negocio.id }
        assertTrue(porId.getValue("propio").puedeEntrar)
        assertTrue(porId.getValue("activo").puedeEntrar)
        assertFalse(porId.getValue("pausado").puedeEntrar)
        assertFalse(porId.getValue("pendiente").puedeEntrar)
    }

    @Test
    fun elNegocioActualSeMarcaSoloSiSePuedeEntrar() {
        assertTrue(construir("propio").first { it.negocio.id == "propio" }.esActual)
        assertTrue(construir("activo").first { it.negocio.id == "activo" }.esActual)
        // Un negocio pausado o pendiente nunca es el "actual", aunque esté seleccionado.
        assertFalse(construir("pausado").first { it.negocio.id == "pausado" }.esActual)
        assertFalse(construir("pendiente").first { it.negocio.id == "pendiente" }.esActual)
    }

    @Test
    fun relacionesDeOtraCuentaNoCuentan() {
        val ajenas = listOf(
            RelacionCuentaNegocio(
                cuentaId = "otraCuenta",
                negocioId = "activo",
                rol = RolCuentaNegocio.TRABAJADOR,
                estado = EstadoRelacion.ACTIVA,
            ),
        )
        val ids = ListaNegocios.construir(negocios, ajenas, yo, null).map { it.negocio.id }
        assertEquals(listOf("propio"), ids)
    }

    // ---- nombre del dueño ----

    private fun cuenta(id: String, usuario: String) = Cuenta(id = id, username = usuario)

    @Test
    fun elDuenoSoyYoSeMuestraComoTu() {
        assertEquals("Tú", resolverNombreDueno("yo", "yo", emptyList()))
    }

    @Test
    fun conCuentaCargadaUsaElUsuarioSinElCorreo() {
        val cuentas = listOf(cuenta("d1", "jorge.quispe@correo.com"), cuenta("d2", "luis"))
        assertEquals("jorge.quispe", resolverNombreDueno("d1", "yo", cuentas))
        assertEquals("luis", resolverNombreDueno("d2", "yo", cuentas))
    }

    @Test
    fun sinCuentaCargadaPideCargarlaYUsaTextoGenerico() {
        var pedida: String? = null
        val nombre = resolverNombreDueno("d9", "yo", emptyList()) { pedida = it }
        assertEquals("Dueño del negocio", nombre)
        assertEquals("d9", pedida)
    }

    @Test
    fun usuarioEnBlancoUsaTextoGenerico() {
        assertEquals("Dueño del negocio", resolverNombreDueno("d1", "yo", listOf(cuenta("d1", "   "))))
    }
}
