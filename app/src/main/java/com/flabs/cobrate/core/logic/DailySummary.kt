package com.flabs.cobrate.core.logic

import com.flabs.cobrate.core.model.EstadoOperacion
import com.flabs.cobrate.core.model.Operacion

/**
 * Resumen diario calculado a partir de las operaciones.
 * Función pura de agregación; sin efectos secundarios.
 */
object DailySummary {

    /** Total del día: suma de los montos de todas las operaciones. */
    fun totalDelDia(operaciones: List<Operacion>): Double =
        operaciones.sumOf { it.monto }

    /** Cantidad de operaciones del día. */
    fun numeroDeOperaciones(operaciones: List<Operacion>): Int =
        operaciones.size

    /** Cantidad de operaciones pendientes (sin trabajar) — estado SIN_ASIGNAR. */
    fun operacionesPendientes(operaciones: List<Operacion>): Int =
        operaciones.count { it.estado == EstadoOperacion.SIN_ASIGNAR }

    /**
     * Promedio del día: total de montos entre el número de operaciones.
     * Protege ante división por cero (devuelve 0.0 si no hay operaciones).
     */
    fun promedioDelDia(operaciones: List<Operacion>): Double {
        val total = totalDelDia(operaciones)
        val cantidad = numeroDeOperaciones(operaciones)
        return if (cantidad == 0) 0.0 else total / cantidad
    }
}