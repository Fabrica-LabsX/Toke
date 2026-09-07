package com.flabs.cobrate.features.business

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.flabs.cobrate.core.data.CuentaStore
import com.flabs.cobrate.core.data.EstadoServiciosStore
import com.flabs.cobrate.core.data.NegocioStore
import com.flabs.cobrate.core.data.RelacionCuentaNegocioStore
import com.flabs.cobrate.core.model.EstadoRelacion
import com.flabs.cobrate.core.model.RolCuentaNegocio

/**
 * Pantalla de trabajadores del dueño.
 * CAPA 9: migración a trabajadores reales vinculados por invitación.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerWorkersScreen(
    businessViewModel: BusinessViewModel
) {
    val trabajadores by businessViewModel.trabajadoresReales.collectAsState()
    var mostrandoInvitar by remember { mutableStateOf(false) }

    // SERVICIOS (CAPA 5b): estado del control de EJECUCIÓN para el negocio actual.
    val sesion by CuentaStore.sesionActual.collectAsState()
    val relaciones by RelacionCuentaNegocioStore.relaciones.collectAsState()
    val negocios by NegocioStore.negocios.collectAsState()
    val estadosServicios by EstadoServiciosStore.estados.collectAsState()

    val negocioActual = remember(sesion?.id, relaciones, negocios) {
        val cuentaId = sesion?.id ?: return@remember null
        relaciones
            .filter {
                it.cuentaId == cuentaId &&
                    it.estado == EstadoRelacion.ACTIVA &&
                    it.rol == RolCuentaNegocio.ADMINISTRADOR
            }
            .mapNotNull { negocio -> negocios.find { it.id == negocio.negocioId } }
            .firstOrNull()
    }
    val servicioActivado = negocioActual?.let { negocio ->
        estadosServicios.firstOrNull { it.negocioId == negocio.id }?.activado ?: true
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Trabajadores",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Invitación por código. Los trabajadores se unen solo por esta vía.
                Button(
                    onClick = { mostrandoInvitar = !mostrandoInvitar },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = if (mostrandoInvitar) "Cerrar invitación" else "Invitar trabajador")
                }
                if (mostrandoInvitar) {
                    Spacer(modifier = Modifier.height(16.dp))
                    InvitarTrabajadorScreen(
                        onCerrar = { mostrandoInvitar = false }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(trabajadores, key = { it.cuentaId }) { info ->
                    TrabajadorRealCard(
                        info = info,
                        servicioActivado = servicioActivado,
                        onAlternarEstado = {
                            val nuevoEstado = if (info.estado == EstadoRelacion.ACTIVA) {
                                EstadoRelacion.EXPULSADA
                            } else {
                                EstadoRelacion.ACTIVA
                            }
                            businessViewModel.cambiarEstadoTrabajador(info.cuentaId, nuevoEstado)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TrabajadorRealCard(
    info: TrabajadorRealInfo,
    servicioActivado: Boolean?,
    onAlternarEstado: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = info.username,
                style = MaterialTheme.typography.titleMedium
            )
            // Indicador SERVICIOS.
            servicioActivado?.let { activado ->
                Text(
                    text = if (activado) "Servicio activado" else "Servicio desactivado",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (activado) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outline
                    }
                )
            }
            Text(
                text = "Estado: ${info.estado.name}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(onClick = onAlternarEstado) {
                    Text(
                        text = if (info.estado == EstadoRelacion.ACTIVA) "Expulsar" else "Reactivar"
                    )
                }
            }
        }
    }
}
