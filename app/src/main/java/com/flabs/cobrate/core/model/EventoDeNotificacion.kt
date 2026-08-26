package com.flabs.cobrate.core.model

/**
 * Evento de notificación de pago detectado (aviso de Yape/Plin u otro).
 *
 * Es la materia prima de la detección automática de pagos vía NotificationListener.
 * Este archivo solo define el modelo de datos: la implementación del
 * NotificationListenerService que produce estos eventos llega en una capa posterior.
 *
 * [negocioId] identifica el negocio al que corresponde el evento (campo crítico
 * para el filtrado por negocio). [procesado] indica si el evento ya fue
 * consumido para generar una operación.
 *
 * Sin lógica de negocio, solo la estructura de datos.
 */
data class EventoDeNotificacion(
    val id: String,
    val negocioId: String,
    val paqueteOrigen: String,
    val titulo: String,
    val texto: String,
    // Contenido adicional QA de la notificación (subtítulo, big text, líneas
    // múltiples) capturado en CAPA 6c-2. Puede alojar, por ejemplo, el código
    // de seguridad de Yape.
    //
    // REGLA DE PRIVACIDAD: datosAdicionales se persiste y puede usarse
    // internamente (p. ej. verificación antifraude en una capa futura), pero
    // NUNCA debe mostrarse en ninguna pantalla accesible al Trabajador.
    val datosAdicionales: String? = null,
    val fechaHora: Long,
    // Indica si el evento ya fue procesado (consumido) por el sistema.
    val procesado: Boolean = false
)