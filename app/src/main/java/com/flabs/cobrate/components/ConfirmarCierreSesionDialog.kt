package com.flabs.cobrate.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.flabs.cobrate.R

/**
 * Diálogo reutilizable para confirmar el cierre de sesión (CAPA 27).
 */
@Composable
fun ConfirmarCierreSesionDialog(
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        title = {
            Text(text = stringResource(R.string.dialogo_cerrar_titulo))
        },
        text = {
            Text(text = stringResource(R.string.dialogo_cerrar_texto))
        },
        confirmButton = {
            Button(
                onClick = onConfirmar,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(text = stringResource(R.string.dialogo_cerrar_confirmar))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onCancelar) {
                Text(text = stringResource(R.string.dialogo_cancelar))
            }
        }
    )
}
