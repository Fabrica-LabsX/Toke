package com.flabs.cobrate.core.model

/**
 * Representa una alerta asociada a una operación.
 *
 * [operacionId] quedó como placeholder del CAPA 1 original (se completará
 * cuando la alerta se genere desde una operación ya creada). [eventoId]
 * referencia el [EventoDeNotificacion] que la originó, ya que en el momento de
 * la detección (CAPA 6c-3) todavía no existe una Operacion.
 */
data class Alerta(
    val id: String,
    val tipo: TipoAlerta,
    val operacionId: String,
    val mensaje: String,
    // Referencia al EventoDeNotificacion que originó esta alerta (CAPA 6c-3).
    val eventoId: String? = null
)

/** Tipos de alerta de ejemplo. */
enum class TipoAlerta {
    MONTO_INCORRECTO,
    METODO_NO_HABILITADO,
    TIEMPO_EXCEDIDO
}