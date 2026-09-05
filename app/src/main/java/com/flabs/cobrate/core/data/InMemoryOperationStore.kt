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

    private val repository = OperacionRepository()

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

    /** 
     * Agrega una operación en Supabase y, si tiene éxito, en la lista local.
     * DECISIÓN CAPA 7e-2: Si falla en Supabase, la operación no se guarda localmente.
     */
    suspend fun agregar(operacion: Operacion): Result<Unit> {
        val result = repository.crear(operacion)
        if (result.isSuccess) {
            val creada = result.getOrThrow()
            _operaciones.value = _operaciones.value + creada
            persistir()
            return Result.success(Unit)
        }
        return Result.failure(result.exceptionOrNull() ?: Exception("Error al crear operación en Supabase"))
    }

    /**
     * Actualiza una operación en Supabase y, si tiene éxito, en la lista local.
     * DECISIÓN CAPA 7e-2: Todo o nada.
     */
    suspend fun actualizar(operacion: Operacion): Result<Unit> {
        val result = repository.actualizar(operacion)
        if (result.isSuccess) {
            val actualizada = result.getOrThrow()
            _operaciones.value = _operaciones.value.map {
                if (it.id == actualizada.id) actualizada else it
            }
            persistir()
            return Result.success(Unit)
        }
        return Result.failure(result.exceptionOrNull() ?: Exception("Error al actualizar operación en Supabase"))
    }

    /**
     * Sincroniza (reemplaza) las operaciones locales con las del servidor,
     * pero solo para el alcance del negocio indicado, preservando las demás.
     * CAPA 7e-3.
     */
    fun sincronizar(negocioIdSincronizado: String, nuevasOperaciones: List<Operacion>) {
        val operacionesDeOtrosNegocios = _operaciones.value.filter { it.negocioId != negocioIdSincronizado }
        _operaciones.value = operacionesDeOtrosNegocios + nuevasOperaciones
        persistir()
    }

    private fun persistir() {
        localStore?.let { store ->
            val snapshot = _operaciones.value
            scope.launch { store.guardar(snapshot) }
        }
    }
}