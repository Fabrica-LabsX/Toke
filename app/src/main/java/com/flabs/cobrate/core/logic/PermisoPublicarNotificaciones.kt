package com.flabs.cobrate.core.logic

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat

object PermisoPublicarNotificaciones {
    /** True si la app puede publicar notificaciones (cubre API < 33 y el interruptor de Ajustes). */
    fun estaConcedido(context: Context): Boolean =
        NotificationManagerCompat.from(context).areNotificationsEnabled()

    /** True si en este Android se puede mostrar el diálogo del sistema (API 33+). */
    fun requiereSolicitudEnTiempoDeEjecucion(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

}
