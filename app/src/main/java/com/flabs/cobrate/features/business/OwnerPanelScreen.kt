package com.flabs.cobrate.features.business

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.ui.unit.dp
import com.flabs.cobrate.components.ProximamenteCard
import com.flabs.cobrate.core.data.AlertaStore
import com.flabs.cobrate.core.data.CuentaStore
import com.flabs.cobrate.core.data.EstadoServiciosStore
import com.flabs.cobrate.core.data.NegocioStore
import com.flabs.cobrate.core.data.RelacionCuentaNegocioStore
import com.flabs.cobrate.core.model.EstadoRelacion
import com.flabs.cobrate.core.model.RolCuentaNegocio

/**
 * Panel del dueño: resumen del día calculado a partir de las operaciones reales.
 * Los controles "Próximamente" son solo visuales; al tocarlos muestran un Toast.
 */
@Composable
fun OwnerPanelScreen(
    businessViewModel: BusinessViewModel,
    onVerHistorial: () -> Unit,
    onVerTrabajadores: () -> Unit,
    onVerMetodosDeCobro: () -> Unit,
    onVerPlantillasFlujo: () -> Unit,
    onVerEstadisticas: () -> Unit,
    onVerEstablecimientos: () -> Unit,
    onVerPlanFacturacion: () -> Unit,
    onVerPermisos: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    val resumen by businessViewModel.resumenDelDia.collectAsState()
    val alertas by AlertaStore.alertas.collectAsState()
    var mostrandoAlertas by remember { mutableStateOf(false) }
    val totalDelDia = "S/ %.2f".format(resumen.totalDelDia)
    val numeroOperaciones = resumen.numeroDeOperaciones.toString()
    val operacionesPendientes = resumen.operacionesPendientes.toString()

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = "Panel del Dueño",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(16.dp))

            // SERVICIOS (CAPA 5b): control de EJECUCIÓN, no de identidad.
            // Desactivarlo NUNCA cierra sesión, NUNCA expulsa trabajadores y NUNCA
            // borra datos históricos. Solo construye el switch y su estado local;
            // la distribución remota real (Supabase en standby) queda fuera de alcance.
            ControlServicios()

            Spacer(modifier = Modifier.height(16.dp))

            // Resumen del día.
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    DatoResumen("Total del día", totalDelDia)
                    DatoResumen("Operaciones", numeroOperaciones)
                    DatoResumen("Pendientes", operacionesPendientes)
                }
            }

            // Indicador de alertas (CAPA 6c-3): visible SOLO para el Dueño.
            // No se implementa resolución/descarte todavía (pendiente CAPA 7+).
            if (alertas.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { mostrandoAlertas = !mostrandoAlertas }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "⚠️ ${alertas.size} alerta${if (alertas.size == 1) "" else "s"}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (mostrandoAlertas) {
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(alertas) { alerta ->
                                    Text(
                                        text = alerta.mensaje,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Navegación hacia las secciones del dueño.
            BotonNavegacion("Historial", onVerHistorial)
            BotonNavegacion("Trabajadores", onVerTrabajadores)
            BotonNavegacion("Métodos de cobro", onVerMetodosDeCobro)
            BotonNavegacion("Plantillas de flujo", onVerPlantillasFlujo)
            BotonNavegacion("Estadísticas", onVerEstadisticas)
            BotonNavegacion("Establecimientos", onVerEstablecimientos)
            BotonNavegacion("Plan y facturación", onVerPlanFacturacion)

            Spacer(modifier = Modifier.height(24.dp))

            // Permisos (CAPA 5): acceso a notificaciones, batería.
            BotonNavegacion("Permisos", onVerPermisos)

            // Controles "Próximamente" (solo visuales, muestran un Toast).
            Text(
                text = "Herramientas",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            ProximamenteCard("Cuadratura de caja")
            Spacer(modifier = Modifier.height(8.dp))
            ProximamenteCard("Estado del sistema")

            Spacer(modifier = Modifier.height(24.dp))

            // Cerrar sesión (alcance local único, sin backend).
            OutlinedButton(
                onClick = onCerrarSesion,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Cerrar sesión")
            }
        }
    }
}

@Composable
private fun BotonNavegacion(etiqueta: String, onClick: () -> Unit) {
    Spacer(modifier = Modifier.height(8.dp))
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = etiqueta)
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
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = valor,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

/**
 * Control "SERVICIOS" (CAPA 5b): switch de EJECUCIÓN, no de identidad.
 *
 * Apagarlo NUNCA cierra sesión, NUNCA expulsa trabajadores y NUNCA borra datos
 * históricos. Aquí solo se construye el switch y su estado local; la
 * distribución REMOTA real (Supabase en standby) queda fuera de alcance.
 *
 * // TODO: cuando exista distribución remota (post-Supabase), verificar
 * // EstadoServiciosStore.activado antes de enviar eventos a otros dispositivos.
 */
@Composable
private fun ControlServicios() {
    // Negocio actual: el primer negocio donde la cuenta activa es ADMINISTRADOR
    // (misma lógica que en la pantalla de invitación).
    val sesion by CuentaStore.sesionActual.collectAsState()
    val relaciones by RelacionCuentaNegocioStore.relaciones.collectAsState()
    val negocios by NegocioStore.negocios.collectAsState()

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

    val negocio = negocioActual

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "SERVICIOS",
                style = MaterialTheme.typography.titleLarge
            )
            if (negocio == null) {
                Text(
                    text = "Sin negocio asociado",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                val estado by EstadoServiciosStore.estados.collectAsState()
                val activado = estado.firstOrNull { it.negocioId == negocio.id }?.activado ?: true
                val colorEstado =
                    if (activado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (activado) "ACTIVADO" else "APAGADO",
                        style = MaterialTheme.typography.titleMedium,
                        color = colorEstado
                    )
                    Switch(
                        checked = activado,
                        onCheckedChange = {
                            EstadoServiciosStore.alternar(negocio.id)
                        }
                    )
                }
                Text(
                    text = "Los eventos de pago se detectan localmente; la distribución remota a otros dispositivos respetará este estado cuando exista (post-Supabase).",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

