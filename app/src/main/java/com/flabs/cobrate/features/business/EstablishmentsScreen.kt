package com.flabs.cobrate.features.business

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.flabs.cobrate.components.ProximamenteCard

/**
 * Establecimientos. Pantalla completa "Próximamente":
 * no aplica aún al negocio de un solo local.
 */
@Composable
fun EstablishmentsScreen() {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Establecimientos",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(24.dp))

            ProximamenteCard(
                titulo = "Multi-establecimiento",
                descripcion = "Disponible en el plan MAX",
                mensajeToast = "Disponible en el plan MAX"
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Por ahora tu negocio opera desde un solo local. " +
                    "La gestión multi-establecimiento llegará próximamente.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}