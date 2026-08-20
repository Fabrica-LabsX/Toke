package com.flabs.cobrate.features.business

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.flabs.cobrate.core.data.MetodoDeCobroStore
import com.flabs.cobrate.core.model.MetodoDeCobro

/**
 * Métodos de cobro del dueño.
 * Lee/escribe la fuente única [MetodoDeCobroStore] (sin ViewModel).
 */
@Composable
fun PaymentMethodsScreen() {
    // Lee la fuente única de métodos de cobro compartida con el trabajador.
    val metodos by MetodoDeCobroStore.metodos.collectAsState()

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = "Métodos de cobro",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(metodos) { metodo ->
                    MetodoCard(
                        metodo = metodo,
                        onCambiarActivo = { activo ->
                            MetodoDeCobroStore.cambiarActivo(metodo.tipo, activo)
                        },
                        onCambiarDato = { dato ->
                            MetodoDeCobroStore.cambiarDato(metodo.tipo, dato)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MetodoCard(
    metodo: MetodoDeCobro,
    onCambiarActivo: (Boolean) -> Unit,
    onCambiarDato: (String) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = metodo.tipo.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Switch(
                    checked = metodo.activo,
                    onCheckedChange = onCambiarActivo
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = metodo.datoDeCobro,
                onValueChange = onCambiarDato,
                label = { Text("Dato de cobro") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}