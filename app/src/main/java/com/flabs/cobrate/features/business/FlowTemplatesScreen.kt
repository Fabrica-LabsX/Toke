package com.flabs.cobrate.features.business

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.flabs.cobrate.core.model.PlantillaDeFlujo
import com.flabs.cobrate.core.model.PlantillasPredefinidas

/**
 * Plantillas de flujo disponibles. Solo lectura por ahora.
 * Lee de la fuente única [PlantillasPredefinidas] (compartida con el trabajador).
 */
@Composable
fun FlowTemplatesScreen() {
    // Plantillas predefinidas del producto (solo lectura) desde la fuente única.
    val plantillas = PlantillasPredefinidas.todas

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = "Plantillas de flujo",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Secuencia de estados por plantilla (solo lectura).",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(plantillas, key = { it.nombre }) { plantilla ->
                    PlantillaCard(plantilla)
                }
            }
        }
    }
}

@Composable
private fun PlantillaCard(plantilla: PlantillaDeFlujo) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = plantilla.nombre,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = plantilla.estados.joinToString(" → "),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}