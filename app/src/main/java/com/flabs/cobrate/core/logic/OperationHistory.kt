package com.flabs.cobrate.core.logic

import com.flabs.cobrate.core.model.CambioDeEstado
import com.flabs.cobrate.core.model.EstadoOperacion
import com.flabs.cobrate.core.model.Operacion

/**
 * Regla de registro del historial de cambios de una [Operacion].
 *
 * Funciones puras: no mutan la operación original; devuelven una copia nueva.
 */
object OperationHistory {

    /**
     * Devuelve una copia de [operacion] con una entrada nueva agregada a su
     * [Operacion.historial] (timestamp + estado). No muta la lista
     * original: construye una lista nueva.
     */
    fun registrarCambio(
        operacion: Operacion,
        nuevoEstado: EstadoOperacion,
        timestamp: Long
    ): Operacion {
        val nuevaEntrada = CambioDeEstado(timestamp = timestamp, estado = nuevoEstado)
        val nuevoHistorial = operacion.historial + nuevaEntrada
        return operacion.copy(
            estado = nuevoEstado,
            historial = nuevoHistorial
        )
    }
}