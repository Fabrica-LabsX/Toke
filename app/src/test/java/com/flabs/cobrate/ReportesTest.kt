package com.flabs.cobrate

import com.flabs.cobrate.core.model.EstadoOperacion
import com.flabs.cobrate.core.model.Operacion
import com.flabs.cobrate.core.model.OrigenOperacion
import com.flabs.cobrate.core.model.TipoMetodoCobro
import com.flabs.cobrate.features.reportes.PeriodoReporte
import com.flabs.cobrate.features.reportes.ReportesLogica
import com.flabs.cobrate.features.reportes.XlsxWriter
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.Calendar
import java.util.TimeZone
import java.util.zip.ZipInputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReportesTest {
    private val utc = TimeZone.getTimeZone("UTC")

    private fun ms(y: Int, mes: Int, d: Int, h: Int = 12): Long =
        Calendar.getInstance(utc).apply { clear(); set(y, mes - 1, d, h, 0, 0) }.timeInMillis

    private fun op(id: String, fecha: Long, monto: Double) = Operacion(
        id = id, negocioId = "n", metodoPago = TipoMetodoCobro.YAPE, monto = monto,
        estado = EstadoOperacion.CONFIRMADA, origen = OrigenOperacion.Detectada("e"), fechaHora = fecha,
    )

    // Viernes 6 de noviembre de 2026, 15:00 UTC.
    private val ahora = ms(2026, 11, 6, 15)

    @Test
    fun iniciosDeCadaPeriodo() {
        assertEquals(ms(2026, 11, 6, 0), ReportesLogica.inicio(PeriodoReporte.DIA, ahora, utc))
        assertEquals(ms(2026, 11, 2, 0), ReportesLogica.inicio(PeriodoReporte.SEMANA, ahora, utc))
        assertEquals(ms(2026, 11, 1, 0), ReportesLogica.inicio(PeriodoReporte.MES, ahora, utc))
        assertEquals(ms(2026, 1, 1, 0), ReportesLogica.inicio(PeriodoReporte.ANIO, ahora, utc))
    }

    @Test
    fun laSemanaDeUnDomingoEmpiezaElLunesAnterior() {
        val domingo = ms(2026, 11, 8, 10)
        assertEquals(ms(2026, 11, 2, 0), ReportesLogica.inicio(PeriodoReporte.SEMANA, domingo, utc))
    }

    @Test
    fun filtraPorPeriodoYResume() {
        val ops = listOf(
            op("hoy", ms(2026, 11, 6, 9), 10.0),
            op("lunes", ms(2026, 11, 2, 9), 20.0),
            op("mesPasado", ms(2026, 10, 30, 9), 40.0),
            op("futuro", ms(2026, 11, 7, 9), 99.0),
        )
        assertEquals(listOf("hoy"), ReportesLogica.filtrar(ops, PeriodoReporte.DIA, ahora, utc).map { it.id })
        assertEquals(listOf("hoy", "lunes"), ReportesLogica.filtrar(ops, PeriodoReporte.SEMANA, ahora, utc).map { it.id })
        val resumen = ReportesLogica.resumen(ReportesLogica.filtrar(ops, PeriodoReporte.ANIO, ahora, utc))
        assertEquals(70.0, resumen.total, 0.001)
        assertEquals(3, resumen.cobros)
        assertEquals(0.0, ReportesLogica.resumen(emptyList()).promedio, 0.0)
    }

    @Test
    fun elExcelTieneLasPartesYElContenido() {
        val ops = listOf(op("a", ms(2026, 11, 6, 9), 12.5))
        val filas = ReportesLogica.filasExportacion(ops, emptyMap(), utc)
        assertEquals(3, filas.size) // encabezado + 1 cobro + total

        val salida = ByteArrayOutputStream()
        XlsxWriter.escribir(filas, salida)
        val partes = mutableMapOf<String, String>()
        ZipInputStream(ByteArrayInputStream(salida.toByteArray())).use { zip ->
            generateSequence { zip.nextEntry }.forEach { partes[it.name] = zip.readBytes().toString(Charsets.UTF_8) }
        }
        listOf("[Content_Types].xml", "_rels/.rels", "xl/workbook.xml", "xl/_rels/workbook.xml.rels", "xl/styles.xml", "xl/worksheets/sheet1.xml")
            .forEach { assertTrue("Falta $it", it in partes) }
        val hoja = partes.getValue("xl/worksheets/sheet1.xml")
        assertTrue(hoja.contains("Yape"))
        assertTrue(hoja.contains("<v>12.5</v>"))
        assertTrue(hoja.contains("06/11/2026"))
    }

    @Test
    fun escapaCaracteresDeXmlYNombraColumnas() {
        assertEquals("A &amp; B &lt;c&gt;", XlsxWriter.escapar("A & B <c>"))
        assertEquals("A", XlsxWriter.columna(0))
        assertEquals("Z", XlsxWriter.columna(25))
        assertEquals("AA", XlsxWriter.columna(26))
    }
}
