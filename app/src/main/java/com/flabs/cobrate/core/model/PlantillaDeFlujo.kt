package com.flabs.cobrate.core.model

/**
 * Plantilla de flujo de una operación: define la secuencia de estados.
 * Sin lógica de negocio, solo el modelo de datos.
 *
 * Nota: no incluye `id` propio; las plantillas son solo lectura por ahora
 * (las custom se agregan en una extensión futura). No se agrega para no
 * adelantarse a capas futuras.
 */
data class PlantillaDeFlujo(
    val nombre: String,
    val estados: List<String>
)