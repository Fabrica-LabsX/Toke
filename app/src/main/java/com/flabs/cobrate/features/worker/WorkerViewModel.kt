package com.flabs.cobrate.features.worker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flabs.cobrate.core.data.InMemoryOperationStore
import com.flabs.cobrate.core.data.OperacionRepository
import com.flabs.cobrate.core.logic.PaymentConfirmation
import com.flabs.cobrate.core.model.EstadoOperacion
import com.flabs.cobrate.core.model.Operacion
import com.flabs.cobrate.core.model.OrigenOperacion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * ViewModel que gestiona la lista de operaciones del trabajador.
 * No contiene lógica de negocio: delega a las funciones puras de core.
 * La fuente de operaciones es compartida ([InMemoryOperationStore]).
 */
class WorkerViewModel : ViewModel() {

    /**
     * PLACEHOLDER CAPA 1: TOKE V1 es multi-negocio, pero aún no hay selección de
     * negocio (ese flujo llega en una capa posterior). Por ahora toda operación
     * creada se anota en un negocio por defecto claramente marcado.
     * TODO(CAPA posterior): resolver el negocio activo desde la relación Cuenta↔Negocio.
     */
    private val negocioPlaceholder = "NEGOCIO_DEFAULT"

    // Lee de la fuente compartida en memoria.
    val operaciones: StateFlow<List<Operacion>> = InMemoryOperationStore.operaciones

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun limpiarError() { _error.value = null }

    // ---------------------------------------------------------------------
    // Identidad simulada de la sesión del trabajador.
    //
    // IMPORTANTE: esto es una SIMPLIFICACIÓN TEMPORAL para poder filtrar
    // operaciones por trabajador dentro de la app. NO es autenticación real:
    // el trabajador se elige manualmente al entrar. La autenticación real
    // (login) llega en la CAPA 9.
    // ---------------------------------------------------------------------
    private val _identidadTrabajador = MutableStateFlow<String?>(null)

    /** Id del trabajador que identifica la sesión actual (null si no hay). */
    val identidadTrabajador: StateFlow<String?> = _identidadTrabajador

    /** Establece la identidad simulada de la sesión actual. */
    fun seleccionarIdentidad(trabajadorId: String) {
        _identidadTrabajador.value = trabajadorId
    }

    /**
     * Crea una nueva operación manual "Va a pagar", asignada al trabajador de
     * la sesión actual (estado ASIGNADA) y con el negocio placeholder.
     *
     * NOTA CAPA 1: el método de pago ya no vive en [Operacion] (el flujo de
     * confirmación/detección se rediseña en una capa posterior); por eso esta
     * función ya no lo recibe.
     */
    fun crearOperacion(monto: Double) {
        val trabajador = _identidadTrabajador.value
        val nueva = Operacion(
            id = UUID.randomUUID().toString(),
            negocioId = negocioPlaceholder,
            trabajadorAsignadoId = trabajador,
            monto = monto,
            estado = EstadoOperacion.ASIGNADA,
            origen = OrigenOperacion.VaAPagar,
            fechaHora = System.currentTimeMillis()
        )
        viewModelScope.launch {
            InMemoryOperationStore.agregar(nueva)
                .onFailure { _error.value = "Error al crear: ${it.message}" }
        }
    }

    /**
     * Confirma la operación indicada por su id.  Delegado a
     * PaymentConfirmation.confirmarPagoManualmente.
     */
    fun confirmarPago(operacionId: String) {
        val op = InMemoryOperationStore.operaciones.value.find { it.id == operacionId } ?: return

        val confirmado = PaymentConfirmation.confirmarPagoManualmente(
            operacion = op,
            timestamp = System.currentTimeMillis()
        )
        viewModelScope.launch {
            InMemoryOperationStore.actualizar(confirmado)
                .onFailure { _error.value = "Error al confirmar: ${it.message}" }
        }
    }

    /**
     * Intenta reclamar una operación SIN_ASIGNAR para el trabajador actual.
     * CAPA 9-2.
     */
    fun reclamarOperacion(operacionId: String) {
        viewModelScope.launch {
            OperacionRepository().reclamar(operacionId)
                .onSuccess { resultado ->
                    if (resultado.exito) {
                        InMemoryOperationStore.refrescarUna(operacionId)
                    } else {
                        _error.value = resultado.mensaje
                    }
                }
                .onFailure {
                    _error.value = "Error de red al reclamar: ${it.message}"
                }
        }
    }
}