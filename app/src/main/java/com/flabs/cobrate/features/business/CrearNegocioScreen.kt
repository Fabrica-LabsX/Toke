package com.flabs.cobrate.features.business

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
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.flabs.cobrate.core.model.TipoNegocio

/**
 * Formulario para crear el primer negocio del dueño (CAPA 3).
 *
 * Captura el nombre y el tipo (selector con los valores de [TipoNegocio]) y
 * delega en [NegocioViewModel.crearNegocio], que además vincula la cuenta como
 * ADMINISTRADOR del negocio recién creado.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearNegocioScreen(
    negocioViewModel: NegocioViewModel,
    onCreado: () -> Unit
) {
    var nombre by rememberSaveable { mutableStateOf("") }
    var tipo by rememberSaveable { mutableStateOf<TipoNegocio?>(null) }
    var menuAbierto by remember { mutableStateOf(false) }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Crea tu primer negocio",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "Aún no perteneces a ningún negocio. Crea uno para empezar a cobrar.",
                style = MaterialTheme.typography.bodyMedium
            )

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre del negocio") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Column {
                OutlinedButton(onClick = { menuAbierto = true }) {
                    Text(text = tipo?.let { "Tipo: ${it.name}" } ?: "Selecciona el tipo")
                }
                DropdownMenu(
                    expanded = menuAbierto,
                    onDismissRequest = { menuAbierto = false }
                ) {
                    TipoNegocio.entries.forEach { valor ->
                        DropdownMenuItem(
                            text = { Text(valor.name) },
                            onClick = {
                                tipo = valor
                                menuAbierto = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val tipoElegido = tipo
                    if (nombre.isNotBlank() && tipoElegido != null) {
                        negocioViewModel.crearNegocio(nombre, tipoElegido)
                        onCreado()
                    }
                },
                enabled = nombre.isNotBlank() && tipo != null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Crear negocio")
            }
        }
    }
}