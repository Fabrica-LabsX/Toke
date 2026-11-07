package com.flabs.cobrate.features.reportes

import com.flabs.cobrate.core.model.EstadoOperacion
import com.flabs.cobrate.core.model.Operacion
import com.flabs.cobrate.core.model.TipoMetodoCobro
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** Periodos que el dueño puede ver y exportar. */
enum class PeriodoReporte(val titulo: String, val detalle: String) {
    DIA("Hoy", "Desde las 12:00 a. m. de hoy"),
    SEMANA("Esta semana", "Desde el lunes"),
    MES("Este mes", "Desde el día 1"),
    ANIO("Este año", "Desde el 1 de enero"),
}

/** Resumen de un periodo. */
data class ResumenPeriodo(val total: Double, val cobros: Int, val promedio: Double)

/** Reglas de los reportes: qué cobros entran en un periodo y cómo se arma la tabla a exportar. */
object ReportesLogica {

    /** Inicio (inclusive) del periodo que contiene a [ahora]. La semana empieza el lunes. */
    fun inicio(periodo: PeriodoReporte, ahora: Long, zona: TimeZone = TimeZone.getDefault()): Long {
        val c = Calendar.getInstance(zona).apply {
            timeInMillis = ahora
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        when (periodo) {
            PeriodoReporte.DIA -> Unit
            PeriodoReporte.SEMANA -> c.add(Calendar.DAY_OF_YEAR, -((c.get(Calendar.DAY_OF_WEEK) + 5) % 7))
            PeriodoReporte.MES -> c.set(Calendar.DAY_OF_MONTH, 1)
            PeriodoReporte.ANIO -> c.set(Calendar.DAY_OF_YEAR, 1)
        }
        return c.timeInMillis
    }

    /** Cobros del periodo, del más reciente al más antiguo. */
    fun filtrar(
        operaciones: List<Operacion>,
        periodo: PeriodoReporte,
        ahora: Long,
        zona: TimeZone = TimeZone.getDefault(),
    ): List<Operacion> {
        val desde = inicio(periodo, ahora, zona)
        return operaciones.filter { it.fechaHora in desde..ahora }.sortedByDescending { it.fechaHora }
    }

    fun resumen(operaciones: List<Operacion>): ResumenPeriodo {
        val total = operaciones.sumOf { it.monto }
        val n = operaciones.size
        return ResumenPeriodo(total, n, if (n == 0) 0.0 else total / n)
    }

    /** Filas de la hoja de Excel: encabezado y un cobro por fila. [nombres] traduce el id del trabajador a su nombre. */
    fun filasExportacion(
        operaciones: List<Operacion>,
        nombres: Map<String, String>,
        zona: TimeZone = TimeZone.getDefault(),
    ): List<List<Celda>> {
        val fecha = SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("es-PE")).apply { timeZone = zona }
        val hora = SimpleDateFormat("HH:mm", Locale.forLanguageTag("es-PE")).apply { timeZone = zona }
        val encabezado = listOf("Fecha", "Hora", "Billetera", "Monto (S/)", "Trabajador", "Estado").map { Celda.Encabezado(it) }
        val filas = operaciones.sortedBy { it.fechaHora }.map { op ->
            val d = Date(op.fechaHora)
            listOf(
                Celda.Texto(fecha.format(d)),
                Celda.Texto(hora.format(d)),
                Celda.Texto(nombreBilletera(op.metodoPago)),
                Celda.Numero(op.monto),
                Celda.Texto(op.trabajadorAsignadoId?.let { nombres[it] } ?: "Sin asignar"),
                Celda.Texto(nombreEstado(op.estado)),
            )
        }
        val total = listOf(
            Celda.Encabezado("Total"), Celda.Texto(""), Celda.Texto(""),
            Celda.Numero(operaciones.sumOf { it.monto }), Celda.Texto(""), Celda.Texto(""),
        )
        return listOf(encabezado) + filas + listOf(total)
    }

    fun nombreArchivo(periodo: PeriodoReporte, ahora: Long, zona: TimeZone = TimeZone.getDefault()): String {
        val f = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { timeZone = zona }
        return "toke-cobros-${periodo.name.lowercase()}-${f.format(Date(ahora))}.xlsx"
    }

    private fun nombreBilletera(m: TipoMetodoCobro?): String =
        m?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Otro"

    private fun nombreEstado(e: EstadoOperacion): String = when (e) {
        EstadoOperacion.SIN_ASIGNAR -> "Pendiente"
        EstadoOperacion.ASIGNADA -> "Asignado"
        EstadoOperacion.CONFIRMADA -> "Confirmado"
        EstadoOperacion.CERRADA -> "Cerrado"
    }
}
