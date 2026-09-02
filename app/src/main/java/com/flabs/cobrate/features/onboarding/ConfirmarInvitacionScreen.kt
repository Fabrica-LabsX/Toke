package com.flabs.cobrate.features.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.flabs.cobrate.core.data.CuentaStore
import com.flabs.cobrate.core.data.InvitacionStore
import com.flabs.cobrate.core.data.NegocioStore
import com.flabs.cobrate.core.data.RelacionCuentaNegocioStore
import com.flabs.cobrate.core.model.InvitacionTrabajador
import com.flabs.cobrate.core.model.RolCuentaNegocio

/**
 * Pantalla de confirmación de la invitación (CAPA 4).
 *
 * Muestra el nombre del negocio y el usuario dueño antes de aceptar. Al
 * aceptar crea la [RelacionCuentaNegocio] con rol TRABAJADOR (ACTIVA), marca
 * la invitación como ACEPTADA y navega al flujo de Trabajador.
 *
 * La cuenta activa ya tiene sesión (CAPA 2 lo garantiza antes de llegar aquí),
 * por lo que [CuentaStore.sesionActual] debería estar poblada.
 */
@Composable
fun ConfirmarInvitacionScreen(
    invitacion: InvitacionTrabajador,
    onAceptada: () -> Unit,
    onVolver: () -> Unit
) {
    val negocios by NegocioStore.negocios.collectAsState()
    val cuentas by CuentaStore.cuentas.collectAsState()
    val scope = rememberCoroutineScope()

    val negocio = negocios.find { it.id == invitacion.negocioId }
    val negocioNombre = negocio?.nombre ?: "Negocio"
    val dueñoUsername = negocio
        ?.let { cuentas.find { c -> c.id == it.dueñoCuentaId }?.username }
        ?: "Dueño"

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
                        text = negocioNombre,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Dueño",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        text = dueñoUsername,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Código: ${invitacion.codigo}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Button(
                onClick = {
                    val cuentaId = CuentaStore.sesionActual.value?.id
                    if (cuentaId != null) {
                        scope.launch {
                            // Crea la relación TRABAJADOR/ACTIVA y marca la invitación como aceptada.
                            val resRel = RelacionCuentaNegocioStore.crear(
                                cuentaId = cuentaId,
                                negocioId = invitacion.negocioId,
                                rol = RolCuentaNegocio.TRABAJADOR
                            )
                            if (resRel.isSuccess) {
                                InvitacionStore.marcarAceptada(invitacion.codigo).onSuccess {
                                    // Sincronizamos datos inmediatamente para el trabajador.
                                    CuentaStore.sincronizarDatos(cuentaId)
                                    onAceptada()
                                }
                            }
                        }
                    }
                },
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