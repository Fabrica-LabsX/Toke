package com.flabs.cobrate.features.permisos

import android.content.Context
import android.content.Intent
import android.provider.Settings

/** Abre las pantallas de Ajustes del sistema donde el usuario concede cada permiso. */
object PermisosAjustes {

    fun abrirAccesoNotificaciones(context: Context) {
        context.startActivity(
            Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }

    fun abrirBateria(context: Context) {
        context.startActivity(
            Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }
}
