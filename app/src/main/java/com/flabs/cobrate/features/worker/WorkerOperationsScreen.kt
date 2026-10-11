package com.flabs.cobrate.features.worker

import androidx.compose.runtime.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.flabs.cobrate.core.model.EstadoOperacion
import com.flabs.cobrate.features.shared.OperacionesScreen

/**
 * Pantalla de Operaciones para Trabajador conforme a Ronda 9.
 */
@Composable
fun WorkerOperationsScreen(
    workerViewModel: WorkerViewModel,
    onEncontrarPedido: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        workerViewModel.notificarReanudacionMirror()
    }

    val operaciones by workerViewModel.operaciones.collectAsState()
    val identidad by workerViewModel.identidadTrabajador.collectAsState()
    val esRelacionActiva by workerViewModel.esRelacionActiva.collectAsState()

    val operacionesVisibles = remember(operaciones, identidad, esRelacionActiva) {
        if (!esRelacionActiva) emptyList()
        else operaciones.filter { it.trabajadorAsignadoId == identidad || it.estado == EstadoOperacion.SIN_ASIGNAR || it.estado == EstadoOperacion.CONFIRMADA }
    }

    OperacionesScreen(
        operaciones = operacionesVisibles,
        onReclamarOperacion = { id ->
            workerViewModel.reclamarOperacion(id)
        }
    )
}
