package com.flabs.cobrate.core.model

/**
 * Representa una alerta asociada a un negocio y cuenta para el Dueño/Administrador.
 */
data class Alerta(
    val id: String,
    val tipo: TipoAlerta,
    val operacionId: String,
    val mensaje: String,
    val eventoId: String? = null,
    val negocioId: String = "",
    val cuentaId: String = "",
    val tipoMetodoCobro: TipoMetodoCobro? = null
)

/** Tipos de alerta. */
enum class TipoAlerta {
    MONTO_INCORRECTO,
    METODO_NO_HABILITADO,
    TIEMPO_EXCEDIDO
}
