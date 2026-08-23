package com.flabs.cobrate.services

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.flabs.cobrate.core.data.EventoDeNotificacionStore
import com.flabs.cobrate.core.model.EventoDeNotificacion
import java.util.UUID

/**
 * NotificationListenerService de TOKE (CAPA 6b).
 *
 * CAPA 6a declaró el servicio; esta capa añade la captura y persistencia del
 * evento crudo: extrae el paquete de origen y el texto legible de cada
 * notificación entrante (sin interpretar su significado) y lo guarda en
 * [EventoDeNotificacionStore] (memoria + persistencia local).
 *
 * NO se hace parsing de montos, NO se generan operaciones todavía — eso llega
 * en capas posteriores (6c+).
 */
class TokeNotificationListenerService : NotificationListenerService() {

    override fun onListenerConnected() {
        super.onListenerConnected()
        // Inicializa los stores con el contexto del servicio (mismo patrón que
        // InMemoryOperationStore.init(context)).
        EventoDeNotificacionStore.init(this)
        Log.d(TAG, "Listener conectado y stores inicializados.")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        // LOG TEMPORAL DE CALIBRACIÓN (se retirará en el siguiente paso):
        // muestra SIEMPRE el paqueteOrigen exacto de cada notificación, sin
        // ningún filtro de contenido, para identificar los paquetes reales
        // que emiten los avisos de pago en el dispositivo.
        Log.d(TAG_CALIBRACION, "Paquete: ${sbn.packageName}")

        // Filtro base de ruido: ignorar nuestras propias notificaciones y las
        // que no tienen contenido legible (título o texto). NO intentamos
        // decidir aquí si es un pago o no — eso es CAPA 6c.
        val paquete = sbn.packageName
        if (paquete == applicationContext.packageName) {
            Log.d(TAG, "Notificación propia ignorada ($paquete)")
            return
        }

        val titulo = sbn.notification.extras
            .getCharSequence(Notification.EXTRA_TITLE)
            ?.toString()
            ?.trim()
            .orEmpty()
        val texto = sbn.notification.extras
            .getCharSequence(Notification.EXTRA_TEXT)
            ?.toString()
            ?.trim()
            .orEmpty()

        if (titulo.isEmpty() && texto.isEmpty()) {
            Log.d(TAG, "Notificación sin contenido legible ignorada ($paquete)")
            return
        }

        // PLACEHOLDER CAPA 1: mismo negocio por defecto que WorkerViewModel.
        // TODO(CAPA posterior): resolver el negocio activo cuando exista
        // selección real de negocio en segundo plano.
        val evento = EventoDeNotificacion(
            id = UUID.randomUUID().toString(),
            negocioId = NEGOCIO_PLACEHOLDER,
            paqueteOrigen = paquete,
            titulo = titulo,
            texto = texto,
            fechaHora = sbn.postTime,
            procesado = false
        )

        EventoDeNotificacionStore.agregar(evento)
        Log.d(TAG, "Evento capturado de: $paquete -> \"$titulo: $texto\"")
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        Log.d(TAG, "Notificación eliminada de: ${sbn.packageName}")
    }

    companion object {
        private const val TAG = "TokeListener"

        // Tag exclusivo para el log temporal de calibración.
        private const val TAG_CALIBRACION = "TokeCalibracion"

        // Mismo placeholder de negocio único que WorkerViewModel
        // ("NEGOCIO_DEFAULT"), pendiente de resolverse cuando exista selección
        // real de negocio activo en segundo plano.
        private const val NEGOCIO_PLACEHOLDER = "NEGOCIO_DEFAULT"
    }
}