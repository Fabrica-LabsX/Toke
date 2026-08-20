package com.flabs.cobrate.core.data

import com.flabs.cobrate.core.model.EstadoTrabajador
import com.flabs.cobrate.core.model.ModoRegistro
import com.flabs.cobrate.core.model.Trabajador
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Fuente de trabajadores compartida en memoria (singleton).
 *
 * Es el MISMO patrón que [InMemoryOperationStore]: un puente entre el flujo
 * del Dueño y el del Trabajador para que ambos lean/actualicen la MISMA lista.
 * La lista inicial contiene los 2 trabajadores de demostración.
 *
 * Nota: hoy es solo en memoria (sin persistencia). La persistencia de
 * trabajadores queda para una pasada posterior, fuera del alcance de esta
 * consolidación.
 */
object TrabajadorStore {

    private val _trabajadores = MutableStateFlow<List<Trabajador>>(
        listOf(
            Trabajador("t-001", "Juan Pérez", ModoRegistro.APP, EstadoTrabajador.ACTIVO),
            Trabajador("t-002", "María Gómez", ModoRegistro.WHATSAPP, EstadoTrabajador.ACTIVO)
        )
    )
    val trabajadores: StateFlow<List<Trabajador>> = _trabajadores

    /** Agrega un trabajador nuevo en estado ACTIVO. */
    fun agregar(trabajador: Trabajador) {
        _trabajadores.value = _trabajadores.value + trabajador
    }

    /** Aplica una transformación a la lista actual (p. ej. alternar estado). */
    fun actualizar(transform: (List<Trabajador>) -> List<Trabajador>) {
        _trabajadores.value = transform(_trabajadores.value)
    }
}