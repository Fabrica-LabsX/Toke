package com.flabs.cobrate.features.business

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.flabs.cobrate.core.data.CuentaStore
import com.flabs.cobrate.core.data.InvitacionStore
import com.flabs.cobrate.core.data.NegocioStore
import com.flabs.cobrate.core.data.RelacionCuentaNegocioStore
import com.flabs.cobrate.core.model.EstadoRelacion
import com.flabs.cobrate.core.model.RolCuentaNegocio

/**
 * Componente "Invitar trabajador" del lado del dueño (CAPA 4).
 *
 * Resuelve el negocio actual como el primer negocio donde la cuenta activa es
 * ADMINISTRADOR (aún no hay un selector de negocio activo global), genera una
 * invitación vía [InvitacionStore.crear] y muestra el código en grande,
 * seleccionable para copiar/compartir. No implementa envío por WhatsApp/SMS
 * (fuera de alcance).
 */
@Composable
fun InvitarTrabajadorScreen(
    onCerrar: () -> Unit
) {
    var codigoGenerado by remember { mutableStateOf<String?>(null) }
    var negocioNombre by remember { mutableStateOf<String?>(null) }

    val sesion by CuentaStore.sesionActual.collectAsState()
    val relaciones by RelacionCuentaNegocioStore.relaciones.collectAsState()
    val negocios by NegocioStore.negocios.collectAsState()

    val scope = rememberCoroutineScope()

    val cuentaId = sesion?.id
    // Negocio actual: el primer negocio donde la cuenta activa es ADMINISTRADOR.
    val negocioActual = remember(cuentaId, relaciones, negocios) {
        if (cuentaId == null) null
        else relaciones
            .filter { it.cuentaId == cuentaId && it.estado == EstadoRelacion.ACTIVA && it.rol == RolCuentaNegocio.ADMINISTRADOR }
            .mapNotNull { negocio -> negocios.find { it.id == negocio.negocioId } }
            .firstOrNull()
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

                Button(
                    onClick = {
                        scope.launch {
                            InvitacionStore.crear(negocio.id).onSuccess {
                                codigoGenerado = it.codigo
                                negocioNombre = negocio.nombre
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (codigoGenerado == null) "Generar invitación"
                        else "Generar otro código"
                    )
                }

                codigoGenerado?.let { codigo ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Comparte este código con tu trabajador:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    SelectionContainer {
                        Text(
                            text = codigo,
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Text(
                        text = "Toca y copia el código. Vence en 24 horas.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            } else {
                Text(
                    text = "No se encontró un negocio que administres.",
                    style = MaterialTheme.typography.bodyMedium
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