package com.flabs.cobrate.core.logic

import android.R
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.flabs.cobrate.MainActivity
import com.flabs.cobrate.core.model.TipoMetodoCobro
import com.flabs.cobrate.services.TokeActionReceiver

/**
 * Estados posibles para la observabilidad de envío de un cobro hacia Supabase.
 */
enum class EstadoObservabilidadEnvio {
    ESPERANDO_RED,
    ERROR,
    ENVIADO
}

/**
 * Disparador de notificaciones internas de TOKE (CAPA 13).
 * Actualizado para soporte de estado "Operando", Alertas de Configuración y Observabilidad de Envío.
 */
object AndroidNotifier {

    private const val CHANNEL_EVENTOS_ID = "toke_eventos"
    private const val CHANNEL_EVENTOS_NOMBRE = "Eventos de TOKE"

    private const val CHANNEL_RUNNING_ID = "toke_running"
    private const val CHANNEL_RUNNING_NOMBRE = "TOKE en ejecución"

    private const val CHANNEL_OBSERVABILIDAD_ID = "toke_observabilidad_envio"
    private const val CHANNEL_OBSERVABILIDAD_NOMBRE = "Estado de envío a trabajadores"

    // IDs estables de notificación (C11 & CAPA 30)
    const val NOTIF_ID_OPERANDO = 5001
    const val NOTIF_ID_ALERTA_METODO_BASE = 6000 // Se suma el ordinal del tipo

    @Volatile
    private var canalesDeclarados = false

    /**
     * Dispara una notificación local estándar.
     */
    fun mostrar(context: Context, titulo: String, texto: String, notificationId: Int) {
        val appContext = context.applicationContext
        declararCanales(appContext)

        val notificacion = NotificationCompat.Builder(appContext, CHANNEL_EVENTOS_ID)
            .setSmallIcon(R.drawable.stat_notify_more)
            .setContentTitle(titulo)
            .setContentText(texto)
            .setStyle(NotificationCompat.BigTextStyle().bigText(texto))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificarSeguro(appContext, notificationId, notificacion)
    }

    /**
     * Construye la notificación persistente de "TOKE está operando" (C11 & CAPA 58).
     */
    fun construirOperando(context: Context, negocioNombre: String?): Notification {
        val appContext = context.applicationContext
        declararCanales(appContext)

        val texto = if (negocioNombre != null) {
            "Detectando pagos en segundo plano · $negocioNombre"
        } else {
            "Detectando pagos en segundo plano."
        }

        // Acción de Apagar
        val intentApagar = Intent(appContext, TokeActionReceiver::class.java).apply {
            action = TokeActionReceiver.ACTION_APAGAR_SERVICIOS
        }
        val pendingApagar = PendingIntent.getBroadcast(
            appContext, 0, intentApagar,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(appContext, CHANNEL_RUNNING_ID)
            .setSmallIcon(R.drawable.stat_notify_sync)
            .setContentTitle("TOKE está operando")
            .setContentText(texto)
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(0, "Apagar", pendingApagar)
            .build()
    }

    /**
     * Muestra la notificación persistente de "TOKE está operando" (C11).
     */
    fun mostrarOperando(context: Context, negocioNombre: String?) {
        val appContext = context.applicationContext
        val notificacion = construirOperando(appContext, negocioNombre)
        notificarSeguro(appContext, NOTIF_ID_OPERANDO, notificacion)
    }

    /**
     * Muestra alerta de método no configurado o inactivo (C11). Solo Dueños.
     */
    fun mostrarMetodoNoConfigurado(
        context: Context,
        tipo: TipoMetodoCobro,
        existePeroInactivo: Boolean
    ) {
        val appContext = context.applicationContext
        declararCanales(appContext)

        val titulo = "${tipo.name} detectado"
        val texto = if (existePeroInactivo) {
            "${tipo.name} está desactivado en este negocio."
        } else {
            "No tienes ${tipo.name} configurado en este negocio. ¿Quieres agregarlo?"
        }

        // Acción Agregar -> Abre la app en Métodos de Cobro
        val intentAgregar = Intent(appContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("route", "metodos_cobro_dueno") 
        }
        val pendingAgregar = PendingIntent.getActivity(
            appContext, tipo.ordinal, intentAgregar,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificacion = NotificationCompat.Builder(appContext, CHANNEL_EVENTOS_ID)
            .setSmallIcon(R.drawable.stat_sys_warning)
            .setContentTitle(titulo)
            .setContentText(texto)
            .setAutoCancel(true)
            .addAction(0, "Configurar", pendingAgregar)
            .build()

        notificarSeguro(appContext, NOTIF_ID_ALERTA_METODO_BASE + tipo.ordinal, notificacion)
    }

    /**
     * Muestra una notificación discreta de observabilidad sobre el estado de transmisión
     * de un cobro hacia Supabase ("Esperando red", "Hubo un error", "Cobro enviado a trabajadores").
     * Canal exclusivo: toke_observabilidad_envio (IMPORTANCE_LOW, silencioso).
     * ID Estable por operación: operacionId.hashCode() para actualizar/reemplazar transiciones.
     */
    fun mostrarObservabilidadEnvio(
        context: Context,
        operacionId: String,
        monto: Double,
        metodoNombre: String?,
        estado: EstadoObservabilidadEnvio
    ) {
        val appContext = context.applicationContext
        declararCanales(appContext)

        val titulo = if (!metodoNombre.isNullOrBlank()) {
            "Cobro $metodoNombre · S/ ${"%.2f".format(monto)}"
        } else {
            "Cobro · S/ ${"%.2f".format(monto)}"
        }

        val texto = when (estado) {
            EstadoObservabilidadEnvio.ESPERANDO_RED -> "Esperando red..."
            EstadoObservabilidadEnvio.ERROR -> "Hubo un error al enviar el cobro."
            EstadoObservabilidadEnvio.ENVIADO -> "Cobro enviado a trabajadores."
        }

        val notifId = operacionId.hashCode()
        val builder = NotificationCompat.Builder(appContext, CHANNEL_OBSERVABILIDAD_ID)
            .setSmallIcon(R.drawable.stat_notify_sync)
            .setContentTitle(titulo)
            .setContentText(texto)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setSilent(true)
            .setAutoCancel(true)

        if (estado == EstadoObservabilidadEnvio.ENVIADO) {
            // Notificación ENVIADO es discreta y auto-expira tras 10s para evitar acumulación
            builder.setTimeoutAfter(10000L)
        } else if (estado == EstadoObservabilidadEnvio.ERROR) {
            val intentRetry = Intent(appContext, TokeActionReceiver::class.java).apply {
                action = TokeActionReceiver.ACTION_REINTENTAR_ENVIO_COBRO
                putExtra(TokeActionReceiver.EXTRA_OPERACION_ID, operacionId)
            }
            val pendingRetry = PendingIntent.getBroadcast(
                appContext,
                notifId,
                intentRetry,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(0, "Reintentar", pendingRetry)
        }

        notificarSeguro(appContext, notifId, builder.build())
    }

    /** Cancela una notificación activa. */
    fun cancelar(context: Context, notificationId: Int) {
        NotificationManagerCompat.from(context.applicationContext).cancel(notificationId)
    }

    const val NOTIF_ID_AVISO_SIMPLE = 7000

    /**
     * Muestra una notificación simple de evento con prioridad alta (CAPA 53).
     */
    fun mostrarAvisoSimple(context: Context, tag: String, titulo: String, texto: String) {
        val appContext = context.applicationContext
        declararCanales(appContext)

        val intent = Intent(appContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            appContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificacion = NotificationCompat.Builder(appContext, CHANNEL_EVENTOS_ID)
            .setSmallIcon(R.drawable.stat_notify_more)
            .setContentTitle(titulo)
            .setContentText(texto)
            .setStyle(NotificationCompat.BigTextStyle().bigText(texto))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificarSeguro(appContext, tag, NOTIF_ID_AVISO_SIMPLE, notificacion)
    }

    private fun notificarSeguro(context: Context, tag: String, id: Int, notification: Notification) {
        try {
            NotificationManagerCompat.from(context).notify(tag, id, notification)
        } catch (e: SecurityException) {
            Log.w(TAG, "Sin permiso POST_NOTIFICATIONS", e)
        }
    }

    private fun notificarSeguro(context: Context, id: Int, notification: Notification) {
        try {
            NotificationManagerCompat.from(context).notify(id, notification)
        } catch (e: SecurityException) {
            Log.w(TAG, "Sin permiso POST_NOTIFICATIONS", e)
        }
    }

    private fun declararCanales(context: Context) {
        if (canalesDeclarados || Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = context.getSystemService(NotificationManager::class.java) ?: return

        // 1. Canal de Eventos (Default)
        if (manager.getNotificationChannel(CHANNEL_EVENTOS_ID) == null) {
            val canal = NotificationChannel(
                CHANNEL_EVENTOS_ID,
                CHANNEL_EVENTOS_NOMBRE,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            manager.createNotificationChannel(canal)
        }

        // 2. Canal de Estado (Low/Silent)
        if (manager.getNotificationChannel(CHANNEL_RUNNING_ID) == null) {
            val canal = NotificationChannel(
                CHANNEL_RUNNING_ID,
                CHANNEL_RUNNING_NOMBRE,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                setShowBadge(false)
                enableVibration(false)
                enableLights(false)
            }
            manager.createNotificationChannel(canal)
        }

        // 3. Canal de Observabilidad de Envío (Low/Silent)
        if (manager.getNotificationChannel(CHANNEL_OBSERVABILIDAD_ID) == null) {
            val canal = NotificationChannel(
                CHANNEL_OBSERVABILIDAD_ID,
                CHANNEL_OBSERVABILIDAD_NOMBRE,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                setShowBadge(false)
                enableVibration(false)
                enableLights(false)
            }
            manager.createNotificationChannel(canal)
        }
        
        canalesDeclarados = true
    }

    private const val TAG = "TokeAndroidNotifier"
}
