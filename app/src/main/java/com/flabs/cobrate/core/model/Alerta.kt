package com.flabs.cobrate.core.model

/**
 * Representa una alerta asociada a una operación.
 *
 * Placeholder: el motor de detección de alertas aún no existe
 * ("Próximamente"), pero el modelo debe poder representarlas.
 */
data class Alerta(
    val id: String,
    val tipo: TipoAlerta,
    val operacionId: String,
    val mensaje: String
)

/** Tipos de alerta de ejemplo. */
enum class TipoAlerta {
    MONTO_INCORRECTO,
    METODO_NO_HABILITADO,
    TIEMPO_EXCEDIDO
}