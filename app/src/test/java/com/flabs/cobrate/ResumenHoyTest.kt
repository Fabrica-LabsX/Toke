package com.flabs.cobrate

import com.flabs.cobrate.core.model.EstadoOperacion
import com.flabs.cobrate.core.model.Operacion
import com.flabs.cobrate.core.model.OrigenOperacion
import com.flabs.cobrate.core.model.TipoMetodoCobro
import com.flabs.cobrate.features.worker.ResumenHoy
import java.util.Calendar
import java.util.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Test

class ResumenHoyTest {
    private val utc = TimeZone.getTimeZone("UTC")
    private fun ms(d: Int, h: Int) = Calendar.getInstance(utc).apply { clear(); set(2026, Calendar.NOVEMBER, d, h, 0) }.timeInMillis
    private val ahora = ms(7, 15)

    private fun op(id: String, d: Int, monto: Double, estado: EstadoOperacion, trabajador: String? = null) = Operacion(
        id = id, negocioId = "n", trabajadorAsignadoId = trabajador, metodoPago = TipoMetodoCobro.YAPE,
        monto = monto, estado = estado, origen = OrigenOperacion.Detectada("e"), fechaHora = ms(d, 9),
    )

    private val ops = listOf(
        op("hoy1", 7, 10.0, EstadoOperacion.ASIGNADA, "yo"),
        op("hoy2", 7, 20.0, EstadoOperacion.CONFIRMADA, "otro"),
        op("hoy3", 7, 5.0, EstadoOperacion.SIN_ASIGNAR),
        op("ayer", 6, 100.0, EstadoOperacion.CONFIRMADA, "yo"),
    )

    @Test
    fun elDuenoSoloVeLoDeHoy_noTodoElHistorial() {
        val r = ResumenHoy.delNegocio(ops, ahora, utc)
        assertEquals(35.0, r.totalDelDia, 0.001)
        assertEquals(3, r.numeroDeOperaciones)
        assertEquals(1, r.operacionesPendientes)
    }

    @Test
    fun elTrabajadorSoloVeLoQueLeAsignaronHoyYLosPendientesParaTomar() {
        val r = ResumenHoy.delTrabajador(ops, "yo", ahora, utc)
        assertEquals(10.0, r.totalDelDia, 0.001)
        assertEquals(1, r.numeroDeOperaciones)
        assertEquals(1, r.operacionesPendientes)
        assertEquals(0, ResumenHoy.delTrabajador(ops, null, ahora, utc).numeroDeOperaciones)
    }
}
