package com.flabs.cobrate.features.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.flabs.cobrate.core.data.InvitacionStore
import com.flabs.cobrate.core.logic.InvitacionValidation
import com.flabs.cobrate.core.logic.MotivoRechazo
import com.flabs.cobrate.core.model.InvitacionTrabajador

/**
 * Pantalla "Soy trabajador" — captura el código de invitación (CAPA 4).
 *
 * Valida la invitación con [InvitacionValidation]. Si es válida, navega a la
 * confirmación; si no, muestra el motivo de rechazo.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngresarCodigoScreen(
    onCodigoValido: (InvitacionTrabajador) -> Unit,
    onVolver: () -> Unit
) {
    var codigo by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Unirse con invitación",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "Ingresa el código de invitación que te dio el dueño del negocio.",
                style = MaterialTheme.typography.bodyMedium
            )

            OutlinedTextField(
                value = codigo,
                onValueChange = { codigo = it.uppercase(); error = null },
                label = { Text("Código de invitación") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                modifier = Modifier.fillMaxWidth()
            )

            error?.let { mensaje ->
                Text(
                    text = mensaje,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Button(
                onClick = {
                    val invitacion = InvitacionStore.buscarPorCodigo(codigo)
                    val motivo = InvitacionValidation.motivoDeRechazo(
                        invitacion,
                        System.currentTimeMillis()
                    )
                    when (motivo) {
                        null -> onCodigoValido(invitacion!!)
                        MotivoRechazo.NO_EXISTE -> error = "Código incorrecto. Intenta de nuevo."
                        MotivoRechazo.NO_PENDIENTE -> error = "Esta invitación ya fue usada."
                        MotivoRechazo.EXPIRADA -> error = "La invitación ha expirado."
                    }
                },
                enabled = codigo.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Continuar")
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onVolver,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Volver")
            }
        }
    }
}