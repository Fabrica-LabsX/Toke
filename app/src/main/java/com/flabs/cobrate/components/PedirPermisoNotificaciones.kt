package com.flabs.cobrate.components

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.flabs.cobrate.core.logic.PermisoPublicarNotificaciones

@Composable
fun PedirPermisoNotificaciones() {
    val context = LocalContext.current

    if (!PermisoPublicarNotificaciones.requiereSolicitudEnTiempoDeEjecucion()) {
        return
    }

    var yaMostradoEnEstaSesion by rememberSaveable { mutableStateOf(false) }
    var mostrarDialogoExplicativo by rememberSaveable { mutableStateOf(false) }

    val estaConcedido = PermisoPublicarNotificaciones.estaConcedido(context)

    if (!estaConcedido && !yaMostradoEnEstaSesion && !mostrarDialogoExplicativo) {
        mostrarDialogoExplicativo = true
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        yaMostradoEnEstaSesion = true
        mostrarDialogoExplicativo = false
    }

    if (mostrarDialogoExplicativo && !estaConcedido) {
        DialogoPermiso(
            titulo = "Activa los avisos",
            texto = "Para avisarte de cada cobro, Toke necesita permiso para mostrar notificaciones.",
            textoConfirmar = "Activar",
            textoCancelar = "Ahora no",
            onConfirmar = {
                yaMostradoEnEstaSesion = true
                mostrarDialogoExplicativo = false
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            },
            onCancelar = {
                yaMostradoEnEstaSesion = true
                mostrarDialogoExplicativo = false
            },
        )
    }
}
