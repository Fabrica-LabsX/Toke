package com.flabs.cobrate.core.data

import com.flabs.cobrate.core.model.MetodoDeCobro
import com.flabs.cobrate.core.model.TipoMetodoCobro
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Fuente única de métodos de cobro compartida en memoria (singleton).
 *
 * Es el MISMO patrón que [TrabajadorStore] / [InMemoryOperationStore]: un
 * puente entre el flujo del Dueño (que configura los métodos) y el del
 * Trabajador (que los consume), para que ambos lean/actualicen la MISMA lista.
 *
 * Nota: esta pasada resuelve tener una SOLA fuente en memoria. La persistencia
 * (sobrevivir al cierre de la app) queda pendiente para una revisión posterior.
 */
object MetodoDeCobroStore {

    private val _metodos = MutableStateFlow<List<MetodoDeCobro>>(
        listOf(
            MetodoDeCobro(TipoMetodoCobro.YAPE, "987 654 321", true),
            MetodoDeCobro(TipoMetodoCobro.PLIN, "912 345 678", true),
            MetodoDeCobro(TipoMetodoCobro.TRANSFERENCIA, "191-2345678-0-12", false)
        )
    )
    val metodos: StateFlow<List<MetodoDeCobro>> = _metodos

    /** Aplica una transformación a la lista actual (p. ej. editar un método). */
    fun actualizar(transform: (List<MetodoDeCobro>) -> List<MetodoDeCobro>) {
        _metodos.value = transform(_metodos.value)
    }

    /**
     * ¿El método [tipo] está activo en el negocio? Devuelve null si el tipo no
     * está configurado en absoluto (p. ej. no se ha añadido aún).
     */
    fun estaActivo(tipo: TipoMetodoCobro): Boolean? =
        _metodos.value.find { it.tipo == tipo }?.activo

    /** Cambia el flag [MetodoDeCobro.activo] de un método por su tipo. */
    fun cambiarActivo(tipo: TipoMetodoCobro, activo: Boolean) {
        actualizar { lista ->
            lista.map {
                if (it.tipo == tipo) it.copy(activo = activo) else it
            }
        }
    }

    /** Actualiza el dato de cobro de un método por su tipo. */
    fun cambiarDato(tipo: TipoMetodoCobro, dato: String) {
        actualizar { lista ->
            lista.map {
                if (it.tipo == tipo) it.copy(datoDeCobro = dato) else it
            }
        }
    }
}