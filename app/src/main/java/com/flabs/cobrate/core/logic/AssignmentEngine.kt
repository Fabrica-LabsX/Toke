package com.flabs.cobrate.core.logic

import com.flabs.cobrate.core.model.EstadoOperacion
import com.flabs.cobrate.core.model.Operacion
import com.flabs.cobrate.core.model.RelacionCuentaNegocio

/**
 * Regla de asignación automática de operaciones a trabajadores (CAPA 8).
 *
 * Función pura: no muta la operación original; devuelve una copia nueva.
 *
 * "1 trabajador activo" se cuenta sobre [RelacionCuentaNegocio] con
 * rol = TRABAJADOR y estado = ACTIVA. La lista ya debe llegar filtrada desde
 * el llamador; esta función solo decide si corresponde asignar.
 */
object AssignmentEngine {

    /**
     * Si hay exactamente 1 trabajador activo, asigna la operación a ese único
     * trabajador (estado [EstadoOperacion.ASIGNADA]) registrando el cambio en
     * el historial. Si hay 0 o 2+ trabajadores activos, devuelve la operación
     * sin cambios (SIN_ASIGNAR); el caso 2+ es CAPA 9 y queda fuera de alcance.
     */
    fun asignarSiCorresponde(
        operacion: Operacion,
        trabajadoresActivos: List<RelacionCuentaNegocio>
    ): Operacion {
        if (trabajadoresActivos.size != 1) {
            return operacion
        }

        val unico = trabajadoresActivos.first()

        // Asigna el trabajador y registra el cambio de estado en el historial
        // (mismo patrón que OperationHistory).
        val asignada = OperationHistory.registrarCambio(
            operacion = operacion,
            nuevoEstado = EstadoOperacion.ASIGNADA,
            timestamp = System.currentTimeMillis()
        )
        return asignada.copy(trabajadorAsignadoId = unico.cuentaId)
    }
}