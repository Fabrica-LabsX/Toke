package com.flabs.cobrate.features.business

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.flabs.cobrate.components.OperacionCard
import com.flabs.cobrate.core.model.Operacion

/**
 * Historial del dueño: lista de operaciones reales desde la fuente compartida.
 */
@Composable
fun OwnerHistoryScreen(
    businessViewModel: BusinessViewModel
) {
    val operaciones by businessViewModel.operaciones.collectAsState()

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = "Historial",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (operaciones.isEmpty()) {
                Text(text = "No hay operaciones todavía.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(operaciones, key = { it.id }) { operacion: Operacion ->
                        OperacionCard(
                            operacion = operacion,
                            mostrarId = true
                        )
                    }
                }
            }
        }
    }
}