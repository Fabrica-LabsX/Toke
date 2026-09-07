package com.flabs.cobrate.features.business

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flabs.cobrate.core.data.CuentaRepository
import com.flabs.cobrate.core.data.CuentaStore
import com.flabs.cobrate.core.data.InMemoryOperationStore
import com.flabs.cobrate.core.data.RelacionCuentaNegocioRepository
import com.flabs.cobrate.core.data.RelacionCuentaNegocioStore
import com.flabs.cobrate.core.logic.DailySummary
import com.flabs.cobrate.core.model.EstadoRelacion
import com.flabs.cobrate.core.model.Operacion
import com.flabs.cobrate.core.model.RelacionCuentaNegocio
import com.flabs.cobrate.core.model.RolCuentaNegocio
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Estado de la aplicación para el lado del Dueño.
 */
class BusinessViewModel : ViewModel() {

    private val relacionRepo = RelacionCuentaNegocioRepository()
    private val cuentaRepo = CuentaRepository()

    // Operaciones reales compartidas (mismo flujo que usa WorkerViewModel).
    val operaciones: StateFlow<List<Operacion>> = InMemoryOperationStore.operaciones

    /** 
     * Negocio actual gestionado por el dueño (el primero donde es ADMINISTRADOR). 
     * Mismo criterio que en el resto de la app.
     */
    val negocioActualId: StateFlow<String?> = combine(
        CuentaStore.sesionActual,
        RelacionCuentaNegocioStore.relaciones
    ) { sesion, relaciones ->
        val cuentaId = sesion?.id ?: return@combine null
        relaciones.find {
            it.cuentaId == cuentaId &&
                it.rol == RolCuentaNegocio.ADMINISTRADOR &&
                it.estado == EstadoRelacion.ACTIVA
        }?.negocioId
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    // Resumen del día calculado a partir de las operaciones reales.
    val resumenDelDia: StateFlow<ResumenDelDia> = InMemoryOperationStore.operaciones
        .map { ops ->
            ResumenDelDia(
                totalDelDia = DailySummary.totalDelDia(ops),
                numeroDeOperaciones = DailySummary.numeroDeOperaciones(ops),
                operacionesPendientes = DailySummary.operacionesPendientes(ops),
                promedioDelDia = DailySummary.promedioDelDia(ops)
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ResumenDelDia(
                totalDelDia = 0.0,
                numeroDeOperaciones = 0,
                operacionesPendientes = 0,
                promedioDelDia = 0.0
            )
        )

    /**
     * Lista de trabajadores reales del negocio actual (vinculados por invitación).
     * CAPA 9: migración a RelacionCuentaNegocio real.
     */
    val trabajadoresReales: StateFlow<List<TrabajadorRealInfo>> = combine(
        negocioActualId,
        RelacionCuentaNegocioStore.relaciones,
        CuentaStore.cuentas
    ) { negocioId, todasLasRelaciones, cuentasCheadas ->
        if (negocioId == null) return@combine emptyList()

        val relacionesTrabajador = todasLasRelaciones.filter {
            it.negocioId == negocioId && it.rol == RolCuentaNegocio.TRABAJADOR
        }

        relacionesTrabajador.map { rel ->
            val cuenta = cuentasCheadas.find { it.id == rel.cuentaId }
            if (cuenta == null) {
                // Si no está en caché, disparamos carga asíncrona.
                cargarCuenta(rel.cuentaId)
            }
            TrabajadorRealInfo(
                cuentaId = rel.cuentaId,
                username = cuenta?.username ?: "Cuenta sin nombre",
                estado = rel.estado
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private fun cargarCuenta(id: String) {
        viewModelScope.launch {
            cuentaRepo.obtenerPorId(id).onSuccess { cuenta ->
                if (cuenta != null) {
                    // Cacheamos en CuentaStore para que el StateFlow reaccione.
                    CuentaStore.crear(cuenta)
                }
            }
        }
    }

    /** Cambia el estado de un trabajador (vínculo con el negocio). */
    fun cambiarEstadoTrabajador(cuentaId: String, nuevoEstado: EstadoRelacion) {
        val negocioId = negocioActualId.value ?: return
        viewModelScope.launch {
            relacionRepo.actualizarEstado(cuentaId, negocioId, nuevoEstado).onSuccess {
                // Actualizamos el Store local (todo o nada).
                val relacionesActuales = RelacionCuentaNegocioStore.relaciones.value
                val nuevasRelaciones = relacionesActuales.map {
                    if (it.cuentaId == cuentaId && it.negocioId == negocioId) {
                        it.copy(estado = nuevoEstado)
                    } else it
                }
                RelacionCuentaNegocioStore.sincronizar(cuentaId, nuevasRelaciones)
            }
        }
    }
}

/** Información de un trabajador real vinculada a una cuenta. */
data class TrabajadorRealInfo(
    val cuentaId: String,
    val username: String,
    val estado: EstadoRelacion
)

/** Resumen del día expuesto a la UI. */
data class ResumenDelDia(
    val totalDelDia: Double,
    val numeroDeOperaciones: Int,
    val operacionesPendientes: Int,
    val promedioDelDia: Double
)
