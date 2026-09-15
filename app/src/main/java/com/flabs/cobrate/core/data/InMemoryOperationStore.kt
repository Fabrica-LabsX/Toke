package com.flabs.cobrate.core.data

import android.content.Context
import com.flabs.cobrate.core.logic.NetworkMonitor
import com.flabs.cobrate.core.model.Operacion
import com.flabs.cobrate.core.model.EstadoOperacion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Collections

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

    // Conjunto de IDs de operaciones que están siendo enviadas al servidor.
    // Durante la sincronización, estas operaciones no serán sobrescritas para
    // evitar pérdida de progreso local "en vuelo".
    private val idsEnVuelo = Collections.synchronizedSet(mutableSetOf<String>())

    // Se inicializa con el contexto de la Application la primera vez que se llama a init().
    private var localStore: OperationLocalStore? = null

    private val repository = OperacionRepository()

    private var appContext: Context? = null

    /**
     * Inicializa el store con el contexto de la app y carga las operaciones
     * persistidas. Llamar varias veces es seguro (solo inicializa una vez).
     */
    fun init(context: Context) {
        val ctx = context.applicationContext
        appContext = ctx
        if (localStore == null) {
            val store = OperationLocalStore(ctx)
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
     * CAPA 7e-2 / E-2: Si falla y no hay red, la guarda en la cola offline.
     */
    suspend fun agregar(operacion: Operacion): Result<Unit> {
        idsEnVuelo.add(operacion.id)
        try {
            val result = repository.crear(operacion)
            if (result.isSuccess) {
                val creada = result.getOrThrow()
                _operaciones.value = _operaciones.value + creada
                persistir()
                return Result.success(Unit)
            }
            
            // CAPA TAREA E-2: Cola Offline
            val error = result.exceptionOrNull()
            if (!NetworkMonitor.disponible.value) {
                appContext?.let { ctx ->
                    OperacionPendienteStore.agregar(ctx, operacion)
                    // Se agrega localmente para que el usuario/trabajador la vea de inmediato.
                    if (_operaciones.value.none { it.id == operacion.id }) {
                        _operaciones.value = _operaciones.value + operacion
                        persistir()
                    }
                    return Result.success(Unit)
                }
            }
            
            return Result.failure(error ?: Exception("Error al crear operación en Supabase"))
        } finally {
            idsEnVuelo.remove(operacion.id)
        }
    }

    /**
     * Actualiza una operación en Supabase y, si tiene éxito, en la lista local.
     * DECISIÓN CAPA 7e-2: Todo o nada.
     */
    suspend fun actualizar(operacion: Operacion): Result<Unit> {
        idsEnVuelo.add(operacion.id)
        try {
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
        } finally {
            idsEnVuelo.remove(operacion.id)
        }
    }

    /**
     * Sincroniza las operaciones locales con las del servidor usando una política
     * de merge para evitar sobrescribir cambios locales "más nuevos" o "en vuelo".
     * CAPA 7e-3 + FIX INTEGRIDAD.
     */
    fun sincronizar(negocioIdSincronizado: String, nuevasOperaciones: List<Operacion>) {
        val operacionLocales = _operaciones.value
        val idsOffline = OperacionPendienteStore.operacionesPendientes.value.map { it.id }.toSet()

        // 1. Identificar operaciones de otros negocios (se mantienen intactas)
        val deOtrosNegocios = operacionLocales.filter { it.negocioId != negocioIdSincronizado }

        // 2. Realizar el merge para el negocio sincronizado
        val resultadoMerge = nuevasOperaciones.map { remota ->
            val local = operacionLocales.find { it.id == remota.id }
            
            when {
                // REGLA 1: Si está en la cola offline, la versión local manda.
                remota.id in idsOffline -> local ?: remota
                
                // REGLA 2: Si está "en vuelo" (enviándose), protegemos el estado local.
                remota.id in idsEnVuelo -> local ?: remota
                
                // REGLA 3: Si el estado local es más avanzado que el remoto, conservamos local.
                // (Ej: local está CONFIRMADA pero el servidor todavía reporta ASIGNADA por latencia)
                local != null && local.estado.ordinal > remota.estado.ordinal -> local
                
                // REGLA 4: Por defecto, el servidor es la fuente de verdad.
                else -> remota
            }
        }

        // 3. Añadir operaciones locales que no están en la lista del servidor 
        // (ej. creadas offline que aún no aparecen en el fetch remoto)
        val idsRemotos = nuevasOperaciones.map { it.id }.toSet()
        val soloLocalesDelNegocio = operacionLocales.filter { 
            it.negocioId == negocioIdSincronizado && it.id !in idsRemotos 
        }

        _operaciones.value = deOtrosNegocios + resultadoMerge + soloLocalesDelNegocio
        persistir()
    }

    /**
     * Refresca la información de UNA sola operación desde el servidor.
     * Útil tras una operación atómica en el backend (ej. RPC reclamar).
     * CAPA 9-2.
     */
    suspend fun refrescarUna(operacionId: String) {
        repository.obtenerPorId(operacionId).onSuccess { actualizada ->
            if (actualizada != null) {
                _operaciones.value = _operaciones.value.map { 
                    if (it.id == operacionId) actualizada else it 
                }
                persistir()
            }
        }
    }

    /**
     * Limpia la memoria local y el disco (usado en Logout para evitar fuga de datos entre cuentas).
     */
    fun limpiar() {
        _operaciones.value = emptyList()
        persistir()
    }

    private fun persistir() {
        localStore?.let { store ->
            val snapshot = _operaciones.value
            scope.launch { store.guardar(snapshot) }
        }
    }
}