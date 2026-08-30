package com.flabs.cobrate.core.logic

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

/**
 * Disparador de notificaciones internas de TOKE (CAPA 13).
 *
 * Única responsabilidad: llevar el contenido ya construido (título + texto,
 * vía [NotificacionesInternas]) a la bandeja de notificaciones de Android
 * usando [NotificationManagerCompat]. Este archivo es la única pieza que toca
 * el sistema de notificaciones.
 *
 * Declara un único canal "toke_eventos" (importancia DEFAULT) de forma segura
 * ante llamadas repetidas: solo se crea si todavía no existe.
 */
object AndroidNotifier {

    private const val CHANNEL_ID = "toke_eventos"
    private const val CHANNEL_NOMBRE = "Eventos de TOKE"

    @Volatile
    private var canalDeclarado = false

    /**
     * Dispara una notificación local con el contenido indicado.
     *
     * [notificationId] permite reemplazar/agrupar notificaciones del mismo
     * tipo. El canal se declara en la primera llamada.
     */
    fun mostrar(context: Context, titulo: String, texto: String, notificationId: Int) {
        val appContext = context.applicationContext
        crearCanal(appContext)

        val notificacion = NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_more)
            .setContentTitle(titulo)
            .setContentText(texto)
            .setStyle(NotificationCompat.BigTextStyle().bigText(texto))
            .setAutoCancel(true)
            .build()

        // Llamada segura: POST_NOTIFICATIONS es requerido en Android 13+ y se
        // declara en el Manifest (el flujo de solicitud en runtime es tarea de
        // permisos, pendiente). En versiones anteriores no aplica.
        try {
            NotificationManagerCompat.from(appContext).notify(notificationId, notificacion)
        } catch (e: SecurityException) {
            android.util.Log.w(TAG, "Sin permiso para notificar (POST_NOTIFICATIONS)", e)
        }
    }

    private fun crearCanal(context: Context) {
        // Los canales solo existen desde API 26; en versiones anteriores
        // (minSdk 24/25) no se declaran y el comportamiento de la notificación
        // queda determinado por la importancia del flujo de notificaciones.
        if (canalDeclarado || Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = context.getSystemService(NotificationManager::class.java)
        // Verificación de existencia antes de crear (idempotente).
        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            val canal = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NOMBRE,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            manager.createNotificationChannel(canal)
        }
        canalDeclarado = true
    }

    private const val TAG = "TokeAndroidNotifier"
}