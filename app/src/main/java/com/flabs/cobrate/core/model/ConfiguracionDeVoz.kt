package com.flabs.cobrate.core.model

/**
 * Configuración de aviso por voz del trabajador.
 *
 * Placeholder: la reproducción de voz aún no está implementada
 * ("Próximamente"), pero el modelo debe poder representar la configuración.
 */
data class ConfiguracionDeVoz(
    val modo: ModoVoz,
    val modoPrivado: Boolean
)

/** Modo de aviso por voz. */
enum class ModoVoz {
    SILENCIOSO,
    NOTIFICACION,
    VOZ,
    VOZ_Y_NOTIFICACION
}