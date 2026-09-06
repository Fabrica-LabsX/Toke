package com.flabs.cobrate.features.worker

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.flabs.cobrate.components.OperacionCard
import com.flabs.cobrate.components.OperacionCardAccion
import com.flabs.cobrate.core.model.EstadoOperacion

/**
 * Pantalla "Encuentra mi pedido" (CAPA 12).
 *
 * Permite al trabajador buscar una operación SIN_ASIGNAR ingresando el monto
 * que acaba de recibir. Filtra las operaciones con un margen de tolerancia
 * de ±S/0.50 para cubrir redondeos o errores mínimos.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EncuentraMiPedidoScreen(
    viewModel: WorkerViewModel,
    onBack: () -> Unit
) {
    var montoTexto by remember { mutableStateOf("") }
    val monto = montoTexto.toDoubleOrNull()
    
    val todasLasOperaciones by viewModel.operaciones.collectAsState()
    val operacionesSinAsignar = remember(todasLasOperaciones) {
        todasLasOperaciones.filter { it.estado == EstadoOperacion.SIN_ASIGNAR }
    }
    
    val candidatas = remember(monto, operacionesSinAsignar) {
        viewModel.buscarCandidatas(monto, operacionesSinAsignar)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Encuentra mi pedido") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "¿Cuánto te pagaron?",
                style = MaterialTheme.typography.titleMedium
            )
            
            OutlinedTextField(
                value = montoTexto,
                onValueChange = { montoTexto = it },
                label = { Text("Monto (S/)") },
                placeholder = { Text("Ej: 15.50") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider()

            if (candidatas.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (montoTexto.isEmpty()) "Ingresa un monto para buscar"
                               else "No se encontró ninguna operación pendiente con ese monto",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(candidatas) { operacion ->
                        OperacionCard(
                            operacion = operacion,
                            accion = OperacionCardAccion(
                                etiqueta = "Asignarme",
                                onClick = { 
                                    viewModel.reclamarOperacion(operacion.id)
                                }
                            )
                        )
                    }
                }
            }
        }
    }
}
