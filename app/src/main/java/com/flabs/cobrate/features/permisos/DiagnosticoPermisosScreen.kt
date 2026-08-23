package com.flabs.cobrate.features.permisos

import android.content.Context
import android.content.Intent
import android.provider.Settings
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.flabs.cobrate.core.logic.PermisosChecker

/**
 * Diagnóstico de permisos (CAPA 5).
 *
 * Solo diagnostica si el permiso de acceso a notificaciones está concedido;
 * no implementa el NotificationListenerService (eso es CAPA 6). El chequeo de
 * batería se muestra como recomendación secundaria no bloqueante.
 *
 * Al regresar de Ajustes (que se abren con el botón "Solucionar"), la pantalla
 * se revalida automáticamente vía el observer de lifecycle al volver a RESUMED.
 */
@Composable
fun DiagnosticoPermisosScreen(
    onVolver: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var accesoNotificaciones by remember {
        mutableStateOf(PermisosChecker.accesoNotificacionesConcedido(context))
    }

    // Revalida el estado real cada vez que la pantalla vuelve a primer plano
    // (por ejemplo, al regresar de los Ajustes de acceso a notificaciones).
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                accesoNotificaciones =
                    PermisosChecker.accesoNotificacionesConcedido(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = "Permisos",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (accesoNotificaciones) {
                EstadoListoCard()
            } else {
                EstadoRequiereAtencionCard(
                    onSolucionar = {
                        abrirAjustesAccesoNotificaciones(context)
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButton(
                onClick = onVolver,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Volver")
            }
        }
    }
}

/** Estado positivo: la app está lista para la detección automática. */
@Composable
private fun EstadoListoCard() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "✅ TOKE está listo",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "El acceso a notificaciones está concedido. La detección " +
                    "automática de pagos podrá funcionar en segundo plano.",
                style = MaterialTheme.typography.bodyMedium
            )
            // Recomendación secundaria (no bloqueante): batería.
            Text(
                text = "Recomendación opcional: permite que TOKE funcione sin " +
                    "restricciones de batería para una detección más fiable.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

/** Estado pendiente: acceso a notificaciones no concedido. */
@Composable
private fun EstadoRequiereAtencionCard(
    onSolucionar: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "⚠️ TOKE necesita tu atención",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Acceso a notificaciones no concedido",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Start
            )
            Text(
                text = "Para detectar los pagos automáticamente, TOKE necesita " +
                    "acceso a las notificaciones. Android no permite conceder " +
                    "este acceso con un diálogo normal; deberás habilitarlo " +
                    "desde los Ajustes del sistema.",
                style = MaterialTheme.typography.bodyMedium
            )
            Button(
                onClick = onSolucionar,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Solucionar")
            }
        }
    }
}

/** Abre los Ajustes de acceso a notificaciones (listeners). */
private fun abrirAjustesAccesoNotificaciones(context: Context) {
    val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}