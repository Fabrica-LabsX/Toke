package com.flabs.cobrate.features.business

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flabs.cobrate.core.data.InMemoryOperationStore
import com.flabs.cobrate.core.data.TrabajadorStore
import com.flabs.cobrate.core.logic.DailySummary
import com.flabs.cobrate.core.model.EstadoTrabajador
import com.flabs.cobrate.core.model.ModoRegistro
import com.flabs.cobrate.core.model.Operacion
import com.flabs.cobrate.core.model.Trabajador
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.UUID

/**
 * Estado de la aplicación para el lado del Dueño.
 * Las operaciones se leen de la fuente compartida [InMemoryOperationStore];
 * los trabajadores se leen de la fuente compartida [TrabajadorStore].
 */
class BusinessViewModel : ViewModel() {

    // Operaciones reales compartidas (mismo flujo que usa WorkerViewModel).
    val operaciones: StateFlow<List<Operacion>> = InMemoryOperationStore.operaciones

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

    // Lista de trabajadores, leída de la fuente compartida [TrabajadorStore]
    // (mismo patrón que InMemoryOperationStore), accesible tanto para el Dueño
    // como para el Trabajador. Ver TrabajadorStore para la lista inicial.
    val trabajadores: StateFlow<List<Trabajador>> = TrabajadorStore.trabajadores

    /** Agrega un trabajador nuevo en estado ACTIVO. */
    fun agregarTrabajador(nombre: String, modo: ModoRegistro) {
        val nuevo = Trabajador(
            id = UUID.randomUUID().toString(),
            nombre = nombre.trim(),
            modo = modo,
            estado = EstadoTrabajador.ACTIVO
        )
        TrabajadorStore.agregar(nuevo)
    }

    /** Cambia el estado de un trabajador por su id. */
    fun cambiarEstado(trabajadorId: String, nuevoEstado: EstadoTrabajador) {
        TrabajadorStore.actualizar { lista ->
            lista.map {
                if (it.id == trabajadorId) it.copy(estado = nuevoEstado) else it
            }
        }
    }
}

/** Resumen del día expuesto a la UI. */
data class ResumenDelDia(
    val totalDelDia: Double,
    val numeroDeOperaciones: Int,
    val operacionesPendientes: Int,
    val promedioDelDia: Double
)