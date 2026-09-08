package com.flabs.cobrate.core.logic

import com.flabs.cobrate.core.model.EstadoOperacion
import com.flabs.cobrate.core.model.Operacion

/**
 * Regla de confirmación MANUAL de pago.
 *
 * MIGRACIÓN: La lógica de confirmación se ha migrado a la función 'confirmar_pago'
 * de Supabase para validación centralizada de seguridad. Esta clase queda pendiente
 * de limpieza en una tarea posterior.
 *
 * Estado de transición (CAPA 1): la versión previa de esta regla avanzaba la
 * operación por una plantilla de flujo (usando pasoActual/plantillaId), que
 * ya NO viven en [Operacion]. Mientras se rediseña el flujo de confirmación,
 * esta función se reduce a su mínimo: mover la operación a CONFIRMADA.
 *
 * TODO(CAPA posterior): rediseñar la confirmación según el nuevo modelo
 * (detección automática, origen, asignaciones).
 */
object PaymentConfirmation {

    /**
     * Confirma manualmente una operación [operacion], moviéndola a
     * [EstadoOperacion.CONFIRMADA] (una sola vez; no re-confirma operaciones
     * ya confirmadas o cerradas).
     *
     * Devuelve una copia nueva; no muta la operación original.
     */
    fun confirmarPagoManualmente(
        operacion: Operacion,
        timestamp: Long
    ): Operacion {
        // Una operación solo se confirma una vez.
        if (operacion.estado == EstadoOperacion.CONFIRMADA ||
            operacion.estado == EstadoOperacion.CERRADA
        ) {
            return operacion
        }

        // Registrar la confirmación en el historial (delega en OperationHistory).
        return OperationHistory.registrarCambio(
            operacion = operacion,
            nuevoEstado = EstadoOperacion.CONFIRMADA,
            timestamp = timestamp
        )
    }
}