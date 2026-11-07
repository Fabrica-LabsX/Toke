package com.flabs.cobrate

import com.flabs.cobrate.core.model.EstadoOperacion
import com.flabs.cobrate.core.model.Operacion
import com.flabs.cobrate.core.model.OrigenOperacion
import com.flabs.cobrate.core.model.TipoMetodoCobro
import com.flabs.cobrate.features.operaciones.FiltroBilletera
import com.flabs.cobrate.features.operaciones.FiltroOperaciones
import com.flabs.cobrate.features.operaciones.FiltrosOperaciones
import com.flabs.cobrate.features.operaciones.PestanaOperaciones
import com.flabs.cobrate.features.operaciones.RangoTiempo
import org.junit.Assert.assertEquals
import org.junit.Test

class FiltroOperacionesTest {

    private val ahora = 10_000_000_000L
    private val minuto = 60_000L

    private fun op(
        id: String,
        haceMin: Long,
        metodo: TipoMetodoCobro? = TipoMetodoCobro.YAPE,
        estado: EstadoOperacion = EstadoOperacion.CONFIRMADA,
        monto: Double = 10.0,
    ) = Operacion(
        id = id,
        negocioId = "n1",
        metodoPago = metodo,
        monto = monto,
        estado = estado,
        origen = OrigenOperacion.Detectada(),
        fechaHora = ahora - haceMin * minuto,
    )

    private val operaciones = listOf(
        op("a-yape-5", 5, TipoMetodoCobro.YAPE, EstadoOperacion.SIN_ASIGNAR, 80.0),
        op("b-plin-20", 20, TipoMetodoCobro.PLIN, EstadoOperacion.CONFIRMADA, 35.5),
        op("c-bcp-45", 45, TipoMetodoCobro.BCP, EstadoOperacion.SIN_ASIGNAR, 4.0),
        op("d-interbank-90", 90, TipoMetodoCobro.INTERBANK, EstadoOperacion.CONFIRMADA, 120.0),
        op("e-sin-metodo-3", 3, null, EstadoOperacion.CONFIRMADA, 12.0),
    )

    private fun ids(f: FiltrosOperaciones) =
        FiltroOperaciones.aplicar(operaciones, f, ahora).map { it.id }

    @Test
    fun rangoDeDiezMinutos_ordenadoDeNuevoAAntiguo() {
        assertEquals(listOf("e-sin-metodo-3", "a-yape-5"), ids(FiltrosOperaciones(rango = RangoTiempo.DIEZ_MIN)))
    }

    @Test
    fun personalizadoNoLimitaElTiempo() {
        val todos = ids(FiltrosOperaciones(rango = RangoTiempo.PERSONALIZADO))
        assertEquals(5, todos.size)
        assertEquals("d-interbank-90", todos.last())
    }

    @Test
    fun treintaMinutosIncluyeLaDeVeinteYNoLaDeCuarentaYCinco() {
        val r = ids(FiltrosOperaciones(rango = RangoTiempo.TREINTA_MIN))
        assertEquals(listOf("e-sin-metodo-3", "a-yape-5", "b-plin-20"), r)
    }

    @Test
    fun unaHoraEquivaleASesentaMinutos() {
        assertEquals(
            ids(FiltrosOperaciones(rango = RangoTiempo.SESENTA_MIN)),
            ids(FiltrosOperaciones(rango = RangoTiempo.UNA_HORA)),
        )
    }

    @Test
    fun pendientesSoloMuestraLasSinAsignar() {
        val r = ids(FiltrosOperaciones(pestana = PestanaOperaciones.PENDIENTES, rango = RangoTiempo.PERSONALIZADO))
        assertEquals(listOf("a-yape-5", "c-bcp-45"), r)
    }

    @Test
    fun yapeYPlinFiltranPorSuMetodo() {
        assertEquals(
            listOf("a-yape-5"),
            ids(FiltrosOperaciones(rango = RangoTiempo.PERSONALIZADO, billetera = FiltroBilletera.YAPE)),
        )
        assertEquals(
            listOf("b-plin-20"),
            ids(FiltrosOperaciones(rango = RangoTiempo.PERSONALIZADO, billetera = FiltroBilletera.PLIN)),
        )
    }

    @Test
    fun bancosAgrupaTodoLoQueNoEsYapeNiPlin_ySinMetodoNoEntra() {
        val r = ids(FiltrosOperaciones(rango = RangoTiempo.PERSONALIZADO, billetera = FiltroBilletera.BANCOS))
        assertEquals(listOf("c-bcp-45", "d-interbank-90"), r)
    }

    @Test
    fun busquedaPorMontoYPorIdentificador() {
        assertEquals(
            listOf("b-plin-20"),
            ids(FiltrosOperaciones(rango = RangoTiempo.PERSONALIZADO, texto = "35.50")),
        )
        assertEquals(
            listOf("d-interbank-90"),
            ids(FiltrosOperaciones(rango = RangoTiempo.PERSONALIZADO, texto = "  INTERBANK ")),
        )
    }

    @Test
    fun busquedaEnBlancoNoFiltra() {
        assertEquals(
            5,
            ids(FiltrosOperaciones(rango = RangoTiempo.PERSONALIZADO, texto = "   ")).size,
        )
    }

    @Test
    fun contarPendientesCuentaSoloSinAsignar() {
        assertEquals(2, FiltroOperaciones.contarPendientes(operaciones))
    }
}

class FiltroOperacionesHoyTest {
    @org.junit.Test
    fun hoyMuestraLoDeHoyYNoLoDeAyer() {
        val ahora = System.currentTimeMillis()
        val inicio = FiltroOperaciones.inicioDelDia(ahora)
        org.junit.Assert.assertTrue(inicio <= ahora)
        org.junit.Assert.assertTrue(ahora - inicio < 24L * 60 * 60 * 1000)
        org.junit.Assert.assertEquals(RangoTiempo.HOY, FiltrosOperaciones().rango)
    }
}
