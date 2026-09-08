package com.flabs.cobrate.features.worker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flabs.cobrate.core.data.CuentaStore
import com.flabs.cobrate.core.data.InMemoryOperationStore
import com.flabs.cobrate.core.data.OperacionRepository
import com.flabs.cobrate.core.data.RelacionCuentaNegocioStore
import com.flabs.cobrate.core.model.EstadoOperacion
import com.flabs.cobrate.core.model.EstadoRelacion
import com.flabs.cobrate.core.model.Operacion
import com.flabs.cobrate.core.model.OrigenOperacion
import com.flabs.cobrate.core.model.RolCuentaNegocio
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * ViewModel que gestiona la lista de operaciones del trabajador.
 * No contiene lógica de negocio: delega a las funciones puras de core.
 * La fuente de operaciones es compartida ([InMemoryOperationStore]).
 */
class WorkerViewModel : ViewModel() {

    // Lee de la fuente compartida en memoria.
    val operaciones: StateFlow<List<Operacion>> = InMemoryOperationStore.operaciones

    /**
     * Negocio actual gestionado por el trabajador (el primero donde es TRABAJADOR).
     * Si en el futuro un trabajador pertenece a 2+ negocios simultáneamente,
     * esta capa asume el primero.
     */
    val negocioActualId: StateFlow<String?> = combine(
        CuentaStore.sesionActual,
        RelacionCuentaNegocioStore.relaciones
    ) { sesion, relaciones ->
        val cuentaId = sesion?.id ?: return@combine null
        relaciones.find {
            it.cuentaId == cuentaId &&
                it.rol == RolCuentaNegocio.TRABAJADOR &&
                it.estado == EstadoRelacion.ACTIVA
        }?.negocioId
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Id del trabajador autenticado (desde Supabase Auth). */
    val identidadTrabajador: StateFlow<String?> = CuentaStore.sesionActual
        .map { it?.id }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun limpiarError() { _error.value = null }

    /**
     * Crea una nueva operación manual "Va a pagar", asignada al trabajador de
     * la sesión actual (estado ASIGNADA) y con el negocio real detectado.
     */
    fun crearOperacion(monto: Double) {
        val cuentaId = CuentaStore.sesionActual.value?.id
        val negocioId = negocioActualId.value

        if (negocioId == null) {
            _error.value = "No perteneces a ningún negocio todavía"
            return
        }

        val nueva = Operacion(
            id = UUID.randomUUID().toString(),
            negocioId = negocioId,
            trabajadorAsignadoId = cuentaId,
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
     * Confirma la operación indicada por su id. Delegado a la RPC 'confirmar_pago'
     * en Supabase para validación de seguridad (CAPA 13).
     */
    fun confirmarPago(operacionId: String) {
        viewModelScope.launch {
            OperacionRepository().confirmarPago(operacionId)
                .onSuccess { resultado ->
                    if (resultado.exito) {
                        InMemoryOperationStore.refrescarUna(operacionId)
                    } else {
                        _error.value = resultado.mensaje
                    }
                }
                .onFailure {
                    _error.value = "Error de red al confirmar: ${it.message}"
                }
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

    /**
     * Filtra las operaciones SIN_ASIGNAR que podrían corresponder a un monto
     * ingresado por el trabajador.
     *
     * MARGEN DE TOLERANCIA (CAPA 12): se permite un margen de ±S/0.50 para
     * cubrir posibles redondeos de comisiones (p. ej. en Plin/Yape de montos
     * pequeños con céntimos) o errores de dedo mínimos al ingresar el monto.
     * Si el monto es null, devuelve todas las candidatas sin filtrar.
     */
    fun buscarCandidatas(monto: Double?, operacionesSinAsignar: List<Operacion>): List<Operacion> {
        if (monto == null) return operacionesSinAsignar

        val margen = 0.50
        return operacionesSinAsignar.filter { op ->
            val diferencia = Math.abs(op.monto - monto)
            diferencia <= margen
        }.sortedBy { op ->
            // Prioriza coincidencias exactas arriba.
            Math.abs(op.monto - monto)
        }
    }
}