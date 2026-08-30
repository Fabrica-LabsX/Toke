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
 * Diagnostica si el permiso de acceso a notificaciones está concedido (este
 * sigue siendo el único chequeo BLOQUEANTE) y muestra información sobre el
 * estado de batería como RECOMENDACIÓN NO BLOQUEANTE.
 *
 * Al regresar de Ajustes (ya sea desde el botón "Solucionar" de notificaciones
 * o el botón "Permitir" de batería), la pantalla se revalida automáticamente
 * vía el observer de lifecycle al volver a RESUMED.
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
    var sinRestriccionesBateria by remember {
        mutableStateOf(PermisosChecker.segundoPlanoSinRestricciones(context))
    }

    // Revalida el estado real cada vez que la pantalla vuelve a primer plano
    // (por ejemplo, al regresar desde los Ajustes de notificaciones o de
    // batería). Mismo patrón que el chequeo de notificaciones.
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                accesoNotificaciones =
                    PermisosChecker.accesoNotificacionesConcedido(context)
                sinRestriccionesBateria =
                    PermisosChecker.segundoPlanoSinRestricciones(context)
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
                EstadoListoCard(
                    sinRestriccionesBateria = sinRestriccionesBateria,
                    onPermitirBateria = {
                        abrirAjustesBateria(context)
                    }
                )
            } else {
                EstadoRequiereAtencionCard(
                    sinRestriccionesBateria = sinRestriccionesBateria,
                    onSolucionar = {
                        abrirAjustesAccesoNotificaciones(context)
                    },
                    onPermitirBateria = {
                        abrirAjustesBateria(context)
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
private fun EstadoListoCard(
    sinRestriccionesBateria: Boolean,
    onPermitirBateria: () -> Unit
) {
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
            SeccionBateria(
                sinRestriccionesBateria = sinRestriccionesBateria,
                onPermitirBateria = onPermitirBateria
            )
        }
    }
}

/** Estado pendiente: acceso a notificaciones no concedido. */
@Composable
private fun EstadoRequiereAtencionCard(
    sinRestriccionesBateria: Boolean,
    onSolucionar: () -> Unit,
    onPermitirBateria: () -> Unit
) {
    // El acceso a notificaciones es el único chequeo bloqueante. La batería se
    // trata como recomendación, pero si ambas fallan a la vez conviene ser
    // preciso sobre cuál es la recomendación pendiente.
    val mensaje = when (sinRestriccionesBateria) {
        true -> "Acceso a notificaciones no concedido"
        false -> "Acceso a notificaciones no concedido · batería sin restricciones pendiente"
    }

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
                text = mensaje,
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
            // Recomendación secundaria (no bloqueante): batería.
            SeccionBateria(
                sinRestriccionesBateria = sinRestriccionesBateria,
                onPermitirBateria = onPermitirBateria
            )
        }
    }
}

/**
 * Sección de batería (recomendación NO BLOQUEANTE).
 *
 * Elección de intent: usamos
 * [Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS] en lugar de
 * [Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS]. La variante directa
 * requiere declarar `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` en el manifiesto,
 * que es un permiso sensible en las políticas de algunas tiendas, y además
 * abriría un diálogo del sistema dirigido específicamente a nuestro paquete.
 * Al ser la batería una recomendación OPCIONAL y no un requisito, preferimos
 * la pantalla genérica de optimización de batería, que no exige el permiso y
 * no presiona al usuario de forma intrusiva.
 */
@Composable
private fun SeccionBateria(
    sinRestriccionesBateria: Boolean,
    onPermitirBateria: () -> Unit
) {
    if (sinRestriccionesBateria) {
        Text(
            text = "✅ Sin restricciones de batería",
            style = MaterialTheme.typography.bodySmall
        )
    } else {
        Text(
            text = "Recomendación opcional: Android podría pausar la detección " +
                "de pagos en segundo plano para ahorrar batería. Puedes permitir " +
                "que TOKE funcione sin restricciones, aunque no es obligatorio.",
            style = MaterialTheme.typography.bodySmall
        )
        OutlinedButton(
            onClick = onPermitirBateria,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Permitir")
        }
    }
}

/** Abre los Ajustes de acceso a notificaciones (listeners). */
private fun abrirAjustesAccesoNotificaciones(context: Context) {
    val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}

/**
 * Abre la pantalla genérica de optimización de batería.
 *
 * Ver [SeccionBateria] para la justificación de esta elección frente a
 * [Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS].
 */
private fun abrirAjustesBateria(context: Context) {
    val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}