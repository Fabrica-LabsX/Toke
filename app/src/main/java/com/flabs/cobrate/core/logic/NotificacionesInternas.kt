package com.flabs.cobrate.core.logic

/**
 * Constructores de contenido de notificaciones internas de TOKE (CAPA 13).
 *
 * Funciones PURAS: solo arman el título y el texto de la notificación. NADA
 * aquí dispara la notificación; eso es responsabilidad de
 * [AndroidNotifier] (capa Android/NotificationManager).
 *
 * Regla de privacidad (spec TOKE V1, sección 20): si el modo de
 * voz/notificación es CAUTELOSO no se revela el monto en el contenido de la
 * notificación de Android. La integración con voz (B2) es tarea aparte, pero
 * la notificación en sí respeta esta regla cuando se le pasa [modoCauteloso].
 */
object NotificacionesInternas {

    /** Contenido para "pago asignado" a un trabajador. */
    fun notificacionPagoAsignado(monto: Double, modoCauteloso: Boolean): ContenidoNotificacion {
        val texto = if (modoCauteloso) "Pago confirmado" else "Pago confirmado: S/ $monto"
        return ContenidoNotificacion(titulo = "Nuevo pago asignado", texto = texto)
    }

    /** Contenido para "pago sin asignar" (0 o 2+ trabajadores). */
    fun notificacionPagoSinAsignar(monto: Double, modoCauteloso: Boolean): ContenidoNotificacion {
        val texto = if (modoCauteloso) "Pago confirmado" else "Pago confirmado: S/ $monto"
        return ContenidoNotificacion(titulo = "Pago sin asignar", texto = texto)
    }

    /** Contenido para alerta al Dueño; pasa el mensaje de la Alerta (CAPA 6c-3) tal cual. */
    fun notificacionAlertaDueño(mensaje: String): ContenidoNotificacion {
        return ContenidoNotificacion(titulo = "Alerta de cobro", texto = mensaje)
    }
}

/** Contenido ya construido de una notificación interna de TOKE. */
data class ContenidoNotificacion(
    val titulo: String,
    val texto: String
)