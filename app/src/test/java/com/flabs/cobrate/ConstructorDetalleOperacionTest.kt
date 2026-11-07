package com.flabs.cobrate

import com.flabs.cobrate.core.model.EstadoOperacion
import com.flabs.cobrate.core.model.Operacion
import com.flabs.cobrate.core.model.OrigenOperacion
import com.flabs.cobrate.core.model.TipoMetodoCobro
import com.flabs.cobrate.features.operaciones.ConstructorDetalleOperacion
import com.flabs.cobrate.features.operaciones.IconoFila
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ConstructorDetalleOperacionTest {

    private fun op(
        id: String = "op-1234567890abcdef",
        estado: EstadoOperacion = EstadoOperacion.CONFIRMADA,
        trabajador: String? = null,
        metodo: TipoMetodoCobro? = TipoMetodoCobro.PLIN,
        monto: Double = 80.0,
    ) = Operacion(
        id = id,
        negocioId = "n1",
        trabajadorAsignadoId = trabajador,
        metodoPago = metodo,
        monto = monto,
        estado = estado,
        origen = OrigenOperacion.Detectada(),
        fechaHora = 1_700_000_000_000L,
    )

    private fun iconos(d: com.flabs.cobrate.features.operaciones.DetalleOperacion) = d.filas.map { it.icono }

    @Test
    fun montoSeFormateaConDosDecimalesYPunto() {
        val d = ConstructorDetalleOperacion.construir(op(monto = 1250.5), "Mi Negocio", null, 0, null)
        assertEquals("S/ 1250.50", d.monto)
    }

    @Test
    fun recibidoDependeDelEstado() {
        assertTrue(ConstructorDetalleOperacion.construir(op(), null, null, 0, null).recibido)
        assertFalse(
            ConstructorDetalleOperacion.construir(op(estado = EstadoOperacion.SIN_ASIGNAR), null, null, 0, null).recibido,
        )
    }

    @Test
    fun metodoSinIdentificarTieneNombreGenerico() {
        val d = ConstructorDetalleOperacion.construir(op(metodo = null), null, null, 0, null)
        assertEquals("Pago sin identificar", d.metodoNombre)
        assertEquals("Plin", ConstructorDetalleOperacion.construir(op(), null, null, 0, null).metodoNombre)
    }

    @Test
    fun filasEnOrdenYSinNegocioSiNoSeConoce() {
        val conNegocio = ConstructorDetalleOperacion.construir(op(), "Mi Negocio", null, 0, null)
        assertEquals(
            listOf(IconoFila.NEGOCIO, IconoFila.FECHA, IconoFila.HORA, IconoFila.PAGADOR, IconoFila.ID),
            iconos(conNegocio),
        )
        val sinNegocio = ConstructorDetalleOperacion.construir(op(), "  ", null, 0, null)
        assertEquals(
            listOf(IconoFila.FECHA, IconoFila.HORA, IconoFila.PAGADOR, IconoFila.ID),
            iconos(sinNegocio),
        )
    }

    @Test
    fun pagadorSaleDelTituloOUsaNombreGenerico() {
        assertEquals("Juan P.", ConstructorDetalleOperacion.nombrePagador("  Juan P.  "))
        assertEquals("Cliente Toke", ConstructorDetalleOperacion.nombrePagador("   "))
        assertEquals("Cliente Toke", ConstructorDetalleOperacion.nombrePagador(null))
    }

    @Test
    fun trabajadorSoloSeMuestraConUnoAsignadoYMasDeUnoActivo() {
        val soloUnTrabajador = ConstructorDetalleOperacion.construir(op(trabajador = "t1"), null, null, 1, "Ana")
        assertFalse(iconos(soloUnTrabajador).contains(IconoFila.TRABAJADOR))

        val sinAsignado = ConstructorDetalleOperacion.construir(op(trabajador = null), null, null, 3, "Ana")
        assertFalse(iconos(sinAsignado).contains(IconoFila.TRABAJADOR))

        val visible = ConstructorDetalleOperacion.construir(op(trabajador = "t1"), null, null, 2, "Ana")
        val fila = visible.filas.first { it.icono == IconoFila.TRABAJADOR }
        assertEquals("Ana", fila.valor)

        val sinNombre = ConstructorDetalleOperacion.construir(op(trabajador = "t1"), null, null, 2, null)
        assertEquals("Trabajador asignado", sinNombre.filas.first { it.icono == IconoFila.TRABAJADOR }.valor)
    }

    @Test
    fun idMuestraLosUltimosSeisCaracteresComoEnLaLista() {
        val d = ConstructorDetalleOperacion.construir(op(id = "abcdefghijklmnopqrstuvwxyz"), null, null, 0, null)
        assertEquals("#uvwxyz", d.filas.last { it.icono == IconoFila.ID }.valor)
    }
}
