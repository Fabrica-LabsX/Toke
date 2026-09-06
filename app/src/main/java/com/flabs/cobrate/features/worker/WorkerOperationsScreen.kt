package com.flabs.cobrate.features.worker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.flabs.cobrate.components.OperacionCard
import com.flabs.cobrate.components.OperacionCardAccion
import com.flabs.cobrate.core.model.EstadoOperacion

@Composable
fun WorkerOperationsScreen(
    workerViewModel: WorkerViewModel,
    onVaAPagar: () -> Unit,
    onEncontrarPedido: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    val operaciones by workerViewModel.operaciones.collectAsState()
    val identidad by workerViewModel.identidadTrabajador.collectAsState()
    val error by workerViewModel.error.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            workerViewModel.limpiarError()
        }
    }

    // Solo muestra las operaciones de la sesión actual O las que no tienen dueño (SIN_ASIGNAR).
    val operacionesDelTrabajador = remember(operaciones, identidad) {
        operaciones.filter { it.trabajadorAsignadoId == identidad || it.estado == EstadoOperacion.SIN_ASIGNAR }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = "Operaciones activas",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (operacionesDelTrabajador.isEmpty()) {
                Text(text = "No hay operaciones activas.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(operacionesDelTrabajador) { operacion ->
                        OperacionCard(
                            operacion = operacion,
                            accion = when (operacion.estado) {
                                EstadoOperacion.ASIGNADA -> OperacionCardAccion(
                                    etiqueta = "Marcar como pagado",
                                    onClick = { workerViewModel.confirmarPago(operacion.id) }
                                )
                                EstadoOperacion.SIN_ASIGNAR -> OperacionCardAccion(
                                    etiqueta = "Asignarme",
                                    onClick = { workerViewModel.reclamarOperacion(operacion.id) }
                                )
                                else -> null
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onVaAPagar,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "¡Voy a cobrar!")
            }

            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onEncontrarPedido,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Encuentra mi pedido")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Cerrar sesión (alcance local único, sin backend).
            OutlinedButton(
                onClick = onCerrarSesion,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Cerrar sesión")
            }
        }
    }
}
