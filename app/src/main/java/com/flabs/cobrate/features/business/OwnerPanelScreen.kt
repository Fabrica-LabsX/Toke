package com.flabs.cobrate.features.business

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import com.flabs.cobrate.core.data.AlertaStore
import com.flabs.cobrate.core.data.CuentaStore
import com.flabs.cobrate.core.data.EstadoServiciosStore
import com.flabs.cobrate.core.data.NegocioStore
import com.flabs.cobrate.core.data.RelacionCuentaNegocioStore
import com.flabs.cobrate.core.model.EstadoRelacion
import com.flabs.cobrate.core.model.RolCuentaNegocio

/**
 * Panel del dueño: resumen del día calculado a partir de las operaciones reales.
 */
@Composable
fun OwnerPanelScreen(
    businessViewModel: BusinessViewModel,
    onVerHistorial: () -> Unit,
    onVerTrabajadores: () -> Unit,
    onVerMetodosDeCobro: () -> Unit,
    onVerEstadisticas: () -> Unit,
    onVerEstablecimientos: () -> Unit,
    onVerPlanFacturacion: () -> Unit,
    onVerPermisos: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    val resumen by businessViewModel.resumenDelDia.collectAsState()
    val alertas by AlertaStore.alertas.collectAsState()
    val negocioActivoId by businessViewModel.negocioActualId.collectAsState()
    val sesionActual by CuentaStore.sesionActual.collectAsState()
    val misRelaciones by RelacionCuentaNegocioStore.relaciones.collectAsState()
    val misNegocios by NegocioStore.negocios.collectAsState()
    var mostrandoAlertas by remember { mutableStateOf(false) }

    val totalDelDia = "S/ %.2f".format(resumen.totalDelDia)
    val numeroOperaciones = resumen.numeroDeOperaciones.toString()
    val operacionesPendientes = resumen.operacionesPendientes.toString()

    val uid = sesionActual?.id

    val esAutorizadoNegocioActivo = remember(negocioActivoId, uid, misRelaciones, misNegocios) {
        if (negocioActivoId == null || uid == null) false
        else {
            val negocio = misNegocios.find { it.id == negocioActivoId }
            val esDueno = negocio?.dueñoCuentaId == uid
            val relacion = misRelaciones.find { it.cuentaId == uid && it.negocioId == negocioActivoId }
            val esAdminActivo = relacion?.rol == RolCuentaNegocio.ADMINISTRADOR && relacion.estado == EstadoRelacion.ACTIVA
            esDueno || esAdminActivo
        }
    }

    val alertasDelNegocio = remember(alertas, negocioActivoId, uid) {
        if (negocioActivoId == null || uid == null) emptyList()
        else alertas.filter { it.negocioId == negocioActivoId && it.cuentaId == uid }
    }

    Scaffold { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Panel del Dueño",
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            // SERVICIOS (CAPA 5b): control de EJECUCIÓN, no de identidad.
            item {
                ControlServicios()
            }

            // Resumen del día.
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        DatoResumen("Total del día", totalDelDia)
                        DatoResumen("Operaciones", numeroOperaciones)
                        DatoResumen("Pendientes", operacionesPendientes)
                    }
                }
            }

            // Indicador de alertas: visible SOLO para Dueño/Admin del negocio activo.
            if (esAutorizadoNegocioActivo && alertasDelNegocio.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { mostrandoAlertas = !mostrandoAlertas }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "⚠️ ${alertasDelNegocio.size} alerta${if (alertasDelNegocio.size == 1) "" else "s"}",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }

                if (mostrandoAlertas) {
                    items(alertasDelNegocio) { alerta ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = alerta.mensaje,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { AlertaStore.descartarAlerta(alerta.id) }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Descartar alerta"
                                )
                            }
                        }
                    }
                }
            }

            // Navegación hacia las secciones del dueño.
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BotonNavegacion("Historial", onVerHistorial)
                    BotonNavegacion("Trabajadores", onVerTrabajadores)
                    BotonNavegacion("Métodos de cobro", onVerMetodosDeCobro)

                    BotonNavegacion("Estadísticas", onVerEstadisticas)
                    BotonNavegacion("Establecimientos", onVerEstablecimientos)
                    BotonNavegacion("Plan y facturación", onVerPlanFacturacion)
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = onCerrarSesion,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Cerrar sesión")
                }
            }
        }
    }
}

@Composable
private fun ControlServicios() {
    val negocioId by NegocioStore.negocioActivoId.collectAsState()
    val id = negocioId ?: return
    val estado = EstadoServiciosStore.estadoDe(id)

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "SERVICIOS",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (estado.activado) "Distribución activa" else "Modo standby",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (estado.activado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                )
            }
            Switch(
                checked = estado.activado,
                onCheckedChange = { EstadoServiciosStore.alternar(id) }
            )
        }
    }
}

@Composable
private fun DatoResumen(etiqueta: String, valor: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = etiqueta, style = MaterialTheme.typography.bodyMedium)
        Text(text = valor, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun BotonNavegacion(texto: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = texto)
    }
}
