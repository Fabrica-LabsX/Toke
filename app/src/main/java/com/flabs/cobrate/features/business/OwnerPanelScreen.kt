package com.flabs.cobrate.features.business

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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.flabs.cobrate.components.ProximamenteCard

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

            // Resumen del día.
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    DatoResumen("Total del día", totalDelDia)
                    DatoResumen("Operaciones", numeroOperaciones)
                    DatoResumen("Pendientes", operacionesPendientes)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

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

