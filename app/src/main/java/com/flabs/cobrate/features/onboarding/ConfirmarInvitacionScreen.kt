package com.flabs.cobrate.features.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.flabs.cobrate.core.data.CuentaStore
import com.flabs.cobrate.core.data.InvitacionRepository
import com.flabs.cobrate.core.model.InvitacionTrabajador
import com.flabs.cobrate.core.model.PreviewInvitacion

/**
 * Pantalla de confirmación de la invitación (CAPA 7d-3).
 *
 * Muestra el nombre del negocio y el usuario dueño obtenidos vía RPC seguro
 * antes de aceptar. Esto permite que trabajadores nuevos vean los datos
 * aunque RLS bloquee la lectura directa de las tablas.
 */
@Composable
fun ConfirmarInvitacionScreen(
    invitacion: InvitacionTrabajador,
    onAceptada: () -> Unit,
    onVolver: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val repository = remember { InvitacionRepository() }

    var preview by remember { mutableStateOf<PreviewInvitacion?>(null) }
    var loading by remember { mutableStateOf(true) }
    var errorPreview by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(invitacion.codigo) {
        loading = true
        repository.obtenerPreview(invitacion.codigo)
            .onSuccess {
                preview = it
                loading = false
                if (it == null) errorPreview = "No se pudo verificar esta invitación"
            }
            .onFailure {
                errorPreview = "Error de conexión al verificar"
                loading = false
            }
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Confirmar invitación",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "Vas a unirte al negocio como trabajador. Revisa los datos antes de aceptar.",
                style = MaterialTheme.typography.bodyMedium
            )

            if (loading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (errorPreview != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(
                        text = errorPreview!!,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Negocio",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = preview?.negocioNombre ?: "Desconocido",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Dueño",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = preview?.duenoUsername ?: "Desconocido",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Código: ${invitacion.codigo}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Button(
                onClick = {
                    val cuentaId = CuentaStore.sesionActual.value?.id
                    if (cuentaId != null) {
                        scope.launch {
                            // Acepta la invitación vía RPC (valida y crea la relación en el servidor).
                            repository.aceptarInvitacion(invitacion.codigo).onSuccess {
                                // Sincronizamos datos inmediatamente para el trabajador.
                                CuentaStore.sincronizarDatos(cuentaId)
                                onAceptada()
                            }
                        }
                    }
                },
                enabled = !loading && errorPreview == null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Aceptar y entrar como trabajador")
            }

            OutlinedButton(
                onClick = onVolver,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Volver")
            }
        }
    }
}
