package com.flabs.cobrate.features.business

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.flabs.cobrate.R

/**
 * Plan y facturación. Los planes son contenido estático de solo lectura;
 * la acción "Actualizar a MAX" está deshabilitada ("Próximamente").
 */
@Composable
fun BillingPlanScreen() {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.nombre_mi_plan),
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(16.dp))

            PlanCard(
                nombre = "LOCAL",
                precio = "S/ 20/mes",
                descripcion = "Para un solo local. Incluye cobros y reportes básicos.",
                actual = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            PlanCard(
                nombre = "MAX",
                precio = "S/ 40/mes",
                descripcion = "Multi-establecimiento, estadísticas avanzadas y más.",
                actual = false
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { /* Acción deshabilitada "Próximamente". */ },
                enabled = false,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Actualizar a MAX — Próximamente")
            }
        }
    }
}

@Composable
private fun PlanCard(
    nombre: String,
    precio: String,
    descripcion: String,
    actual: Boolean
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = nombre,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = precio,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = descripcion,
                style = MaterialTheme.typography.bodyMedium
            )
            if (actual) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Plan actual",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}