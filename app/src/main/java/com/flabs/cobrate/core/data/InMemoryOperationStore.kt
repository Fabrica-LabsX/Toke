package com.flabs.cobrate.core.data

import android.content.Context
import com.flabs.cobrate.core.model.Operacion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Fuente de operaciones compartida en memoria (singleton) con persistencia local.
 *
 * Es un puente entre el flujo del trabajador y el del dueño para que ambos lean
 * y actualicen la MISMA lista. Además, cada cambio se persiste en
 * [OperationLocalStore] (DataStore) para que las operaciones sobrevivan a
 * cerrar y reabrir la app.
 *
 * Los ViewModels consumen [operaciones] como StateFlow, igual que antes; solo
 * cambia el mecanismo interno de dónde vive la lista.
 */
object InMemoryOperationStore {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _operaciones = MutableStateFlow<List<Operacion>>(emptyList())
    val operaciones: StateFlow<List<Operacion>> = _operaciones

    // Se inicializa con el contexto de la Application la primera vez que se llama a init().
    private var localStore: OperationLocalStore? = null

    /**
     * Inicializa el store con el contexto de la app y carga las operaciones
     * persistidas. Llamar varias veces es seguro (solo inicializa una vez).
     */
    fun init(context: Context) {
        val appContext = context.applicationContext
        if (localStore == null) {
            val store = OperationLocalStore(appContext)
            localStore = store
            scope.launch {
                val guardadas = store.leerUnaVez()
                if (guardadas.isNotEmpty()) {
                    _operaciones.value = guardadas
                }
            }
        }
    }

    /** Agrega una operación al final de la lista y la persiste. */
    fun agregar(operacion: Operacion) {
        _operaciones.value = _operaciones.value + operacion
        persistir()
    }

    /**
     * Aplica una transformación a la lista actual (p. ej. reemplazar una
     * operación por su versión actualizada) y persiste el resultado.
     */
    fun actualizar(transform: (List<Operacion>) -> List<Operacion>) {
        _operaciones.value = transform(_operaciones.value)
        persistir()
    }

    private fun persistir() {
        localStore?.let { store ->
            val snapshot = _operaciones.value
            scope.launch { store.guardar(snapshot) }
        }
    }
}