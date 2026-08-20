package com.flabs.cobrate.features.worker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.flabs.cobrate.core.data.MetodoDeCobroStore

/**
 * Pantalla "Va a pagar": captura el monto y el método de pago,
 * y crea una operación nueva (estado ASIGNADA).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayScreen(
    workerViewModel: WorkerViewModel,
    onBack: () -> Unit
) {
    var montoText by remember { mutableStateOf("") }
    var menuAbierto by remember { mutableStateOf(false) }

    // Lee los métodos ACTIVOS de la fuente única compartida con el Dueño.
    val metodosActivos by MetodoDeCobroStore.metodos.collectAsState()
    val nombresMetodos = metodosActivos.filter { it.activo }.map { it.tipo.name }
    var metodoSeleccionado by remember { mutableStateOf(nombresMetodos.firstOrNull() ?: "") }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Va a pagar",
                style = MaterialTheme.typography.headlineMedium
            )

            OutlinedTextField(
                value = montoText,
                onValueChange = { montoText = it },
                label = { Text("Monto") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Column {
                OutlinedButton(onClick = { menuAbierto = true }) {
                    Text(text = "Método: $metodoSeleccionado")
                }
                DropdownMenu(
                    expanded = menuAbierto,
                    onDismissRequest = { menuAbierto = false }
                ) {
                    nombresMetodos.forEach { nombre ->
                        DropdownMenuItem(
                            text = { Text(nombre) },
                            onClick = {
                                metodoSeleccionado = nombre
                                menuAbierto = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val monto = montoText.toDoubleOrNull()
                    if (montoText.isNotBlank() && monto != null) {
                        // NOTA CAPA 1: el método seleccionado ya no se persiste en
                        // [Operacion] (ver WorkerViewModel.crearOperacion); el selector
                        // queda por ahora sin efecto sobre el modelo.
                        // TODO(CAPA posterior): reintroducir el método de pago.
                        workerViewModel.crearOperacion(monto)
                        onBack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Confirmar")
            }
        }
    }
}
