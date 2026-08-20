package com.flabs.cobrate.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.flabs.cobrate.core.model.Operacion

/**
 * Card reutilizable que muestra una [Operacion].
 * Se usa tanto en el historial del dueño como en las operaciones del trabajador.
 *
 * @param mostrarId si se debe mostrar el id de la operación (historial del dueño).
 * @param accion acción opcional con su etiqueta (p. ej. "Marcar como pagado").
 */
@Composable
fun OperacionCard(
    operacion: Operacion,
    modifier: Modifier = Modifier,
    mostrarId: Boolean = false,
    accion: OperacionCardAccion? = null
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (mostrarId) {
                Text(
                    text = "Id: ${operacion.id}",
                    style = MaterialTheme.typography.labelMedium
                )
            }
            Text(
                text = "Monto: S/ ${"%.2f".format(operacion.monto)}",
                style = MaterialTheme.typography.titleMedium
            )
            // NOTA CAPA 1: la línea que mostraba "Método" se eliminó porque
            // [Operacion] ya no almacena el método de pago (ver PayScreen).
            Text(
                text = "Estado: ${operacion.estado.name}",
                style = MaterialTheme.typography.bodyMedium
            )

            if (accion != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(onClick = accion.onClick) {
                        Text(text = accion.etiqueta)
                    }
                }
            }
        }
    }
}

/** Acción opcional que se muestra dentro de la card. */
data class OperacionCardAccion(
    val etiqueta: String,
    val onClick: () -> Unit
)