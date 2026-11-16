package com.flabs.cobrate.components

import android.content.Intent
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
 * Se muestra 1 de cada 5 veces que abre la app si el usuario es solo trabajador,
 * no tiene exención de batería concedida y no hay un diálogo de aviso de notificaciones activo.
 */
@Composable
fun PedirExencionBateria() {
    val context = LocalContext.current
    val sesionActual by CuentaStore.sesionActual.collectAsState()
    val relaciones by RelacionCuentaNegocioStore.relaciones.collectAsState()
    val conteoAperturas by PreferenciasAppStore.conteoAperturas.collectAsState()
    var descartadoEnEstaSesion by rememberSaveable { mutableStateOf(false) }

    val uid = sesionActual?.id ?: return
    if (descartadoEnEstaSesion) return

    // Solo 1 de cada 5 veces que abre la app
    if (conteoAperturas <= 0 || conteoAperturas % 5 != 1) return

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

    DialogoPermiso(
        titulo = "Para no perder cobros",
        texto = "Permite que Toke funcione sin restricciones de batería. Si no, el celular puede dormir la app y los avisos llegarán tarde.",
        textoConfirmar = "Permitir",
        textoCancelar = "Ahora no",
        onConfirmar = {
            descartadoEnEstaSesion = true
            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        },
        onCancelar = { descartadoEnEstaSesion = true },
    )
}
