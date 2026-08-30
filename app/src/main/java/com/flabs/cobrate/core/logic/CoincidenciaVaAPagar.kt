package com.flabs.cobrate.core.logic

import com.flabs.cobrate.core.model.EstadoOperacion
import com.flabs.cobrate.core.model.Operacion
import com.flabs.cobrate.core.model.OrigenOperacion

/**
 * CAPA 11: asocia una operación "Va a pagar" con la detección real del pago.
 *
 * Regla de producto (spec TOKE V1, secciones 10-11): "Va a pagar" permite
 * identificar anticipadamente una operación. Si existe información previa por
 * ese flujo, TOKE debe intentar asignar directamente en lugar de tratar el
 * pago como inesperado (evitando crear una operación duplicada). Si NO existe
 * una "Va a pagar" pendiente que coincida, la detección sigue el flujo normal.
 *
 * Funciones puras: no mutan ni escriben en stores; solo inspeccionan la lista
 * recibida y devuelven una referencia (o null).
 */
object CoincidenciaVaAPagar {

    /**
     * Busca, entre [operacionesPendientes] (lista ya filtrada por el negocio
     * del evento), una operación que:
     *  - tenga origen [OrigenOperacion.VaAPagar],
     *  - siga "a la espera de confirmación",
     *  - y cuyo [Operacion.monto] coincida EXACTAMENTE con [eventoMonto].
     *
     * El estado que indica "esperando confirmación" es
     * [EstadoOperacion.ASIGNADA]: es el estado que WorkerViewModel.crearOperacion()
     * asigna hoy a una "Va a pagar" recién creada (tiene trabajador asignado,
     * pendiente de cobro).
     *
     * @return la operación coincidente, o null si no hay coincidencia.
     *
     * DECISIÓN (múltiples coincidencias con el mismo monto): si dos o más
     * "Va a pagar" pendientes comparten el mismo monto, no hay forma
     * determinista de saber cuál corresponde a este evento. En lugar de elegir
     * arbitrariamente (riesgo de asociar el pago a la operación equivocada),
     * se devuelve null y TOKE trata el pago como inesperado (flujo normal),
     * sin inventar la coincidencia.
     */
    fun buscarCoincidencia(
        eventoMonto: Double,
        operacionesPendientes: List<Operacion>
    ): Operacion? {
        val coincidencias = operacionesPendientes.filter { op ->
            op.origen is OrigenOperacion.VaAPagar &&
                op.estado == EstadoOperacion.ASIGNADA &&
                op.monto == eventoMonto
        }

        // 0 coincidencias → pago inesperado. >1 coincidencias → ambiguo: no se
        // elige arbitrariamente, se trata como pago inesperado. Solo hay
        // coincidencia inequívoca cuando hay exactamente 1.
        if (coincidencias.size != 1) return null
        return coincidencias.first()
    }
}