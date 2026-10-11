package com.flabs.cobrate.features.business

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.flabs.cobrate.R
import com.flabs.cobrate.components.ProximamenteCard
import java.util.Locale

/**
 * Estadísticas del negocio.
 * "Hoy" se calcula con datos reales desde [BusinessViewModel.resumenDelDia];
 * "Mes" y "Año" son solo visuales ("Próximamente").
 */
@Composable
fun BusinessStatsScreen(businessViewModel: BusinessViewModel) {
    val resumen by businessViewModel.resumenDelDia.collectAsState()

    val totalHoy = "S/ %.2f".format(Locale.US, resumen.totalDelDia)
    val operacionesHoy = resumen.numeroDeOperaciones.toString()
    val promedioHoy = "S/ %.2f".format(Locale.US, resumen.promedioDelDia)

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.nombre_reportes),
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Sección "Hoy" (funcional visualmente con datos falsos).
            Text(
                text = "Hoy",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    DatoEstadistica("Total", totalHoy)
                    DatoEstadistica("Operaciones", operacionesHoy)
                    DatoEstadistica("Promedio", promedioHoy)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Secciones "Mes" y "Año" (solo visuales, "Próximamente").
            Text(
                text = "Mes",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            ProximamenteCard(
                titulo = stringResource(com.flabs.cobrate.R.string.nombre_este_mes),
                descripcion = "Disponible próximamente"
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Año",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            ProximamenteCard(
                titulo = stringResource(R.string.nombre_este_anio),
                descripcion = "Disponible próximamente"
            )
        }
    }
}

@Composable
private fun DatoEstadistica(etiqueta: String, valor: String) {
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