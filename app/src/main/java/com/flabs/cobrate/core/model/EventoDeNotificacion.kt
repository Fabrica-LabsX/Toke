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
    // Contenido adicional de la notificación (subtítulo, big text, líneas). Solo en memoria mientras se procesa:
    // NUNCA se persiste ni se imprime (PRI-6) y nunca se muestra al Trabajador.
    val datosAdicionales: String? = null,
    val fechaHora: Long,
    // Indica si el evento ya fue procesado (consumido) por el sistema.
    val procesado: Boolean = false
)