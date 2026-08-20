package com.flabs.cobrate.core.model

/**
 * Representa un establecimiento (local) del negocio.
 *
 * Por ahora hay uno solo por negocio; el campo negocioId queda preparado
 * para el multi-local futuro del plan MAX.
 */
data class Establecimiento(
    val id: String,
    val negocioId: String,
    val nombre: String
)