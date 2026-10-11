package com.flabs.cobrate.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Diálogo genérico reutilizable para confirmar acciones importantes o destructivas (CAPA 40 & 41).
 */
@Composable
fun ConfirmarAccionDialog(
    titulo: String,
    texto: String,
    textoConfirmar: String,
    textoCancelar: String = "Cancelar",
    destructivo: Boolean = true,
    textoQueHayQueEscribir: String? = null,
    cargando: Boolean = false,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit,
    alVolverAtras: (() -> Unit)? = null
) {
    var textoIngresado by remember { mutableStateOf("") }

    val habilitado = if (textoQueHayQueEscribir != null) {
        textoIngresado.trim().equals(textoQueHayQueEscribir.trim(), ignoreCase = true)
    } else {
        true
    }

    AlertDialog(
        onDismissRequest = {
            if (!cargando) {
                (alVolverAtras ?: onCancelar).invoke()
            }
        },
        title = {
            Text(text = titulo, style = MaterialTheme.typography.titleMedium)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = texto, style = MaterialTheme.typography.bodyMedium)
                if (textoQueHayQueEscribir != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Escribe el nombre del negocio para confirmar:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = textoIngresado,
                        onValueChange = { textoIngresado = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !cargando
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmar,
                enabled = habilitado && !cargando,
                colors = if (destructivo) {
                    ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                } else {
                    ButtonDefaults.buttonColors()
                }
            ) {
                if (cargando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onError
                    )
                } else {
                    Text(text = textoConfirmar)
                }
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onCancelar,
                enabled = !cargando
            ) {
                Text(text = textoCancelar)
            }
        }
    )
}
