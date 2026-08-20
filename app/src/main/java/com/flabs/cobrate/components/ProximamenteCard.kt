package com.flabs.cobrate.components

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

/**
 * Card reutilizable que representa un control o sección aún no disponible.
 * Muestra un título (y opcionalmente una descripción) con badge "Próximamente".
 * Al tocarlo muestra un Toast "Disponible próximamente". Sin lógica detrás.
 */
@Composable
fun ProximamenteCard(
    titulo: String,
    descripcion: String? = null,
    mensajeToast: String = "Disponible próximamente"
) {
    val context = LocalContext.current
    OutlinedCard(
        onClick = {
            Toast.makeText(context, mensajeToast, Toast.LENGTH_SHORT).show()
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleMedium
                )
                if (descripcion != null) {
                    Text(
                        text = descripcion,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = "Próximamente",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}