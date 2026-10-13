package com.flabs.cobrate.features.business

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.flabs.cobrate.R
import com.flabs.cobrate.core.data.CuentaStore
import com.flabs.cobrate.core.data.NegocioStore
import com.flabs.cobrate.core.data.RelacionCuentaNegocioStore
import com.flabs.cobrate.core.logic.FeedbackManager
import com.flabs.cobrate.core.model.EstadoRelacion
import com.flabs.cobrate.core.model.InvitacionTrabajador
import com.flabs.cobrate.core.model.RolCuentaNegocio
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Componente "Invitar trabajador" del lado del dueño (CAPA 4 & CAPA 35).
 */
@Composable
fun InvitarTrabajadorScreen(
    businessViewModel: BusinessViewModel,
    onCerrar: () -> Unit
) {
    var invitacionActiva by remember { mutableStateOf<InvitacionTrabajador?>(null) }
    var tiempoRestanteMs by remember { mutableStateOf(0L) }
    var creandoInvitacion by remember { mutableStateOf(false) }

    val sesion by CuentaStore.sesionActual.collectAsState()
    val relaciones by RelacionCuentaNegocioStore.relaciones.collectAsState()
    val negocios by NegocioStore.negocios.collectAsState()
    val negocioActivoId by NegocioStore.negocioActivoId.collectAsState()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val cuentaId = sesion?.id
    // Negocio actual: estrictamente el negocio activo seleccionado con validación de permisos.
    val negocioActual = remember(negocioActivoId, cuentaId, relaciones, negocios) {
        val activeId = negocioActivoId ?: return@remember null
        val uid = cuentaId ?: return@remember null
        val negocio = negocios.find { it.id == activeId } ?: return@remember null
        val esDueno = negocio.dueñoCuentaId == uid
        val esAdminActivo = relaciones.any {
            it.cuentaId == uid && it.negocioId == activeId &&
                    it.estado == EstadoRelacion.ACTIVA && it.rol == RolCuentaNegocio.ADMINISTRADOR
        }
        if (esDueno || esAdminActivo) negocio else null
    }

    LaunchedEffect(invitacionActiva) {
        val inv = invitacionActiva ?: return@LaunchedEffect
        while (true) {
            val restante = inv.expiracion - System.currentTimeMillis()
            tiempoRestanteMs = if (restante > 0) restante else 0L
            if (restante <= 0) break
            delay(1000)
        }
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Invitar trabajador",
                style = MaterialTheme.typography.titleMedium
            )

            val negocio = negocioActual
            if (negocio != null) {
                Text(
                    text = "Negocio: ${negocio.nombre}",
                    style = MaterialTheme.typography.bodyMedium
                )

                // Casilla con aprobación previa
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { requiereAprobacion = !requiereAprobacion }
                        .padding(vertical = 2.dp)
                ) {
                    Checkbox(
                        checked = requiereAprobacion,
                        onCheckedChange = { requiereAprobacion = it }
                    )
                    Column(modifier = Modifier.padding(start = 8.dp)) {
                        Text(
                            text = "Con aprobación previa",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (requiereAprobacion) {
                            Text(
                                text = "Tendrás que aprobar a quien use este código.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        if (creandoInvitacion) return@Button
                        creandoInvitacion = true
                        scope.launch {
                            try {
                                businessViewModel.crearInvitacion(negocio.id, requiereAprobacion).onSuccess {
                                    invitacionActiva = it
                                    if (requiereAprobacion) {
                                        FeedbackManager.showSuccess("Código creado. Quien lo use deberá esperar tu aprobación.")
                                    }
                                }.onFailure { err ->
                                    FeedbackManager.showErrorMessage(err.message ?: "Error al crear invitación")
                                }
                            } finally {
                                creandoInvitacion = false
                            }
                        }
                    },
                    enabled = !creandoInvitacion,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (creandoInvitacion) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                        Text(text = "Generando...")
                    } else {
                        Text(
                            text = if (invitacionActiva == null) "Generar invitación"
                            else "Generar otro código"
                        )
                    }
                }

                invitacionActiva?.let { inv ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Comparte este código con tu trabajador:",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    if (tiempoRestanteMs > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            SelectionContainer {
                                Text(
                                    text = inv.codigo,
                                    style = MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }
                            IconButton(onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Código TOKE", inv.codigo)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Código copiado", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copiar código"
                                )
                            }
                        }

                        val minutos = (tiempoRestanteMs / 1000) / 60
                        val segundos = (tiempoRestanteMs / 1000) % 60
                        Text(
                            text = "${stringResource(R.string.invitar_vence)} (%02d:%02d)".format(minutos, segundos),
                            style = MaterialTheme.typography.bodySmall
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = {
                                businessViewModel.revocarCodigo(inv.codigo)
                                invitacionActiva = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Revocar código")
                        }
                    } else {
                        Text(
                            text = inv.codigo,
                            style = MaterialTheme.typography.displaySmall.copy(color = MaterialTheme.colorScheme.outline),
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = stringResource(R.string.invitar_vencido),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            } else {
                Text(
                    text = "No se encontró un negocio activo que administres.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            OutlinedButton(
                onClick = onCerrar,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Cerrar")
            }
        }
    }
}
