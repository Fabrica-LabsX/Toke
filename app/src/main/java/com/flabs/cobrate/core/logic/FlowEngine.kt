package com.flabs.cobrate.core.logic

import com.flabs.cobrate.core.model.PlantillaDeFlujo

/**
 * Reglas de avance de estado según una [PlantillaDeFlujo].
 *
 * Funciones puras: reciben datos y devuelven datos, sin efectos secundarios.
 */
object FlowEngine {

    /**
     * Devuelve el nombre del siguiente estado válido de la plantilla según el
     * [pasoActual], o null si la operación ya está en el último estado.
     */
    fun siguienteEstado(plantilla: PlantillaDeFlujo, pasoActual: Int): String? =
        plantilla.estados.getOrNull(pasoActual + 1)

    /**
     * Indica si el [pasoActual] corresponde al último estado de la plantilla.
     */
    fun esUltimoPaso(plantilla: PlantillaDeFlujo, pasoActual: Int): Boolean =
        pasoActual >= plantilla.estados.lastIndex

    /**
     * Valida que [estado] pertenezca a la plantilla, para evitar que una
     * operación "salte" a un estado que no existe en su flujo.
     */
    fun estadoPerteneceAPlantilla(plantilla: PlantillaDeFlujo, estado: String): Boolean =
        plantilla.estados.contains(estado)
}