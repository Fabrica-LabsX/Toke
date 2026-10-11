package com.flabs.cobrate.components

import android.content.Intent
import android.provider.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.flabs.cobrate.core.data.CuentaStore
import com.flabs.cobrate.core.data.PreferenciasAppStore
import com.flabs.cobrate.core.data.RelacionCuentaNegocioStore
import com.flabs.cobrate.core.logic.PermisoPublicarNotificaciones
import com.flabs.cobrate.core.logic.PermisosChecker
import com.flabs.cobrate.core.logic.Requisito
import com.flabs.cobrate.core.logic.RequisitosPermisos

/**
 * Diálogo de aviso de optimización de batería para el trabajador (CAPA 63).
 * Se muestra una sola vez si el usuario es solo trabajador, no tiene exención de batería
 * y no hay un diálogo de aviso de notificaciones activo.
 */
@Composable
fun PedirExencionBateria() {
    val context = LocalContext.current
    val sesionActual by CuentaStore.sesionActual.collectAsState()
    val relaciones by RelacionCuentaNegocioStore.relaciones.collectAsState()
    val avisoBateriaMostrado by PreferenciasAppStore.avisoBateriaMostrado.collectAsState()

    val uid = sesionActual?.id ?: return
    if (avisoBateriaMostrado) return

    // Evita solaparse con el diálogo de aviso de notificaciones
    val notifDialogoActivo = PermisoPublicarNotificaciones.requiereSolicitudEnTiempoDeEjecucion() &&
            !PermisoPublicarNotificaciones.estaConcedido(context)
    if (notifDialogoActivo) return

    val reqs = remember(uid, relaciones) {
        RequisitosPermisos.para(uid, relaciones)
    }
    val esSoloTrabajador = Requisito.ACCESO_A_NOTIFICACIONES !in reqs
    if (!esSoloTrabajador) return

    val sinRestricciones = PermisosChecker.segundoPlanoSinRestricciones(context)
    if (sinRestricciones) return

    AlertDialog(
        onDismissRequest = {
            PreferenciasAppStore.marcarAvisoBateriaMostrado()
        },
        title = { Text("Para no perder cobros") },
        text = { Text("Permite que Toke funcione sin restricciones de batería. Si no, el celular puede dormir la app y los avisos llegarán tarde.") },
        confirmButton = {
            Button(
                onClick = {
                    PreferenciasAppStore.marcarAvisoBateriaMostrado()
                    val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                }
            ) {
                Text("Permitir")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = {
                    PreferenciasAppStore.marcarAvisoBateriaMostrado()
                }
            ) {
                Text("Ahora no")
            }
        }
    )
}
