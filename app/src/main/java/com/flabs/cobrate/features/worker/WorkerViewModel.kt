package com.flabs.cobrate.features.worker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flabs.cobrate.core.data.CuentaStore
import com.flabs.cobrate.core.data.InMemoryOperationStore
import com.flabs.cobrate.core.data.NegocioStore
import com.flabs.cobrate.core.data.OperacionRepository
import com.flabs.cobrate.core.data.OperacionRepositoryContract
import com.flabs.cobrate.core.data.RelacionCuentaNegocioStore
import com.flabs.cobrate.core.logic.FeedbackManager
import com.flabs.cobrate.core.model.EstadoOperacion
import com.flabs.cobrate.core.model.EstadoRelacion
import com.flabs.cobrate.core.model.Operacion
import com.flabs.cobrate.core.model.RolCuentaNegocio
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel que gestiona las acciones de consulta, reclamo y confirmación del trabajador sobre operaciones existentes.
 * Un trabajador JAMÁS crea operaciones. La fuente de operaciones es compartida ([InMemoryOperationStore]).
 */
class WorkerViewModel internal constructor(
    internal val repository: OperacionRepositoryContract
) : ViewModel() {

    constructor() : this(OperacionRepository())

    /**
     * Negocio actual gestionado por el trabajador.
     * Única fuente de verdad desde el contexto central (C9).
     */
    val negocioActualId: StateFlow<String?> = NegocioStore.negocioActivoId

    /**
     * Verifica si la relación del trabajador para el negocio activo tiene estado ACTIVA y rol TRABAJADOR.
     * Control preventivo en el cliente para la UI.
     */
    val esRelacionActiva: StateFlow<Boolean> = combine(
        CuentaStore.sesionActual,
        negocioActualId,
        RelacionCuentaNegocioStore.relaciones
    ) { sesion, negId, relaciones ->
        val uid = sesion?.id ?: return@combine false
        val negocioId = negId ?: return@combine false
        relaciones.any {
            it.cuentaId == uid &&
                it.negocioId == negocioId &&
                it.rol == RolCuentaNegocio.TRABAJADOR &&
                it.estado == EstadoRelacion.ACTIVA
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    // Operaciones del negocio activo (C9 - Aislamiento).
    val operaciones: StateFlow<List<Operacion>> = combine(
        InMemoryOperationStore.operaciones,
        negocioActualId,
        esRelacionActiva
    ) { allOps, negId, activa ->
        if (negId == null || !activa) emptyList() else allOps.filter { it.negocioId == negId }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /** Id del trabajador autenticado (desde Supabase Auth). */
    val identidadTrabajador: StateFlow<String?> = CuentaStore.sesionActual
        .map { it?.id }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    init {
        // Iniciar reconciliación reactiva cuando se selecciona un negocio activo válido con relación activa.
        viewModelScope.launch {
            combine(negocioActualId, esRelacionActiva) { negId, activa ->
                if (negId != null && activa) negId else null
            }.collectLatest { negId ->
                if (negId != null) {
                    InMemoryOperationStore.reconciliarMirror(negId, repository)
                }
            }
        }
    }

    /**
     * Notifica el evento ON_RESUME (regreso a foreground real) para solicitar la reconciliación del negocio activo.
     */
    fun notificarReanudacionMirror() {
        val negId = negocioActualId.value
        if (negId != null && esRelacionActiva.value) {
            viewModelScope.launch {
                InMemoryOperationStore.reconciliarMirror(negId, repository)
            }
        }
    }

    /**
     * Confirma la operación indicada por su id. Delegado a la RPC 'confirmar_pago'
     * en Supabase para validación de seguridad (CAPA 13).
     */
    fun confirmarPago(operacionId: String) {
        if (!esRelacionActiva.value) {
            FeedbackManager.showErrorMessage("Tu cuenta no se encuentra activa en este negocio")
            return
        }

        val negId = negocioActualId.value ?: return
        val uid = identidadTrabajador.value
        val operacionValida = operaciones.value.find { 
            it.id == operacionId && 
                it.negocioId == negId && 
                it.estado == EstadoOperacion.ASIGNADA && 
                it.trabajadorAsignadoId == uid 
        }
        if (operacionValida == null) {
            FeedbackManager.showErrorMessage("Operación no válida para confirmación")
            return
        }

        viewModelScope.launch {
            InMemoryOperationStore.invalidarReconciliacion(negId)
            FeedbackManager.setLoading(true, "Confirmando pago...")
            val result = repository.confirmarPago(operacionId)
            FeedbackManager.setLoading(false)
            result.onSuccess { resultado ->
                if (resultado.exito) {
                    InMemoryOperationStore.refrescarUna(operacionId)
                    FeedbackManager.showSuccess("Pago confirmado con éxito.")
                } else {
                    FeedbackManager.showErrorMessage(resultado.mensaje)
                }
            }.onFailure {
                FeedbackManager.showError(it)
            }
        }
    }

    /**
     * Intenta reclamar una operación SIN_ASIGNAR para el trabajador actual.
     * CAPA 9-2.
     */
    fun reclamarOperacion(operacionId: String) {
        if (!esRelacionActiva.value) {
            FeedbackManager.showErrorMessage("Tu cuenta no se encuentra activa en este negocio")
            return
        }

        val negId = negocioActualId.value ?: return
        val operacionValida = operaciones.value.find { 
            it.id == operacionId && 
                it.negocioId == negId && 
                it.estado == EstadoOperacion.SIN_ASIGNAR 
        }
        if (operacionValida == null) {
            FeedbackManager.showErrorMessage("Operación no válida para asignación")
            return
        }

        viewModelScope.launch {
            InMemoryOperationStore.invalidarReconciliacion(negId)
            FeedbackManager.setLoading(true, "Asignando operación...")
            val result = repository.reclamar(operacionId)
            FeedbackManager.setLoading(false)
            result.onSuccess { resultado ->
                if (resultado.exito) {
                    InMemoryOperationStore.refrescarUna(operacionId)
                    FeedbackManager.showSuccess("Operación asignada correctamente.")
                } else {
                    FeedbackManager.showErrorMessage(resultado.mensaje)
                }
            }.onFailure {
                FeedbackManager.showError(it)
            }
        }
    }

    /**
     * Filtra las operaciones SIN_ASIGNAR que podrían corresponder a un monto
     * ingresado por el trabajador.
     */
    fun buscarCandidatas(monto: Double?, operacionesSinAsignar: List<Operacion>): List<Operacion> {
        if (monto == null) return operacionesSinAsignar

        val margen = 0.50
        return operacionesSinAsignar.filter { op ->
            val diferencia = Math.abs(op.monto - monto)
            diferencia <= margen
        }.sortedBy { op ->
            Math.abs(op.monto - monto)
        }
    }
}
