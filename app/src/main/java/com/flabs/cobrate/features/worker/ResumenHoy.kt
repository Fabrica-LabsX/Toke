package com.flabs.cobrate.features.worker

import com.flabs.cobrate.core.model.EstadoOperacion
import com.flabs.cobrate.core.model.Operacion
import com.flabs.cobrate.features.business.ResumenDelDia
import com.flabs.cobrate.features.reportes.PeriodoReporte
import com.flabs.cobrate.features.reportes.ReportesLogica
import java.util.TimeZone

/** Resúmenes de "hoy" para el Home. Función pura: solo cuenta lo de hoy. */
object ResumenHoy {

    /** Dueño o administrador: todos los cobros del negocio de hoy. */
    fun delNegocio(operaciones: List<Operacion>, ahora: Long, zona: TimeZone = TimeZone.getDefault()): ResumenDelDia {
        val hoy = ReportesLogica.filtrar(operaciones, PeriodoReporte.DIA, ahora, zona)
        val total = hoy.sumOf { it.monto }
        return ResumenDelDia(
            totalDelDia = total,
            numeroDeOperaciones = hoy.size,
            operacionesPendientes = hoy.count { it.estado == EstadoOperacion.SIN_ASIGNAR },
            promedioDelDia = if (hoy.isEmpty()) 0.0 else total / hoy.size,
            pagosRecibidos = hoy.count { it.estado != EstadoOperacion.SIN_ASIGNAR },
        )
    }

    /**
     * Trabajador: solo lo que le asignaron a él hoy. Los pendientes son los cobros de hoy que
     * todavía nadie reclamó y que puede tomar.
     */
    fun delTrabajador(
        operaciones: List<Operacion>,
        miCuentaId: String?,
        ahora: Long,
        zona: TimeZone = TimeZone.getDefault(),
    ): ResumenDelDia {
        val hoy = ReportesLogica.filtrar(operaciones, PeriodoReporte.DIA, ahora, zona)
        val mios = hoy.filter { it.trabajadorAsignadoId != null && it.trabajadorAsignadoId == miCuentaId }
        val total = mios.sumOf { it.monto }
        return ResumenDelDia(
            totalDelDia = total,
            numeroDeOperaciones = mios.size,
            operacionesPendientes = hoy.count { it.estado == EstadoOperacion.SIN_ASIGNAR },
            promedioDelDia = if (mios.isEmpty()) 0.0 else total / mios.size,
            pagosRecibidos = mios.size,
        )
    }
}
