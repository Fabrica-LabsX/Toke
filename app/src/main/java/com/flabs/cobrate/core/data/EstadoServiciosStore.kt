package com.flabs.cobrate.core.data

import android.content.Context
import com.flabs.cobrate.core.model.EstadoServicios
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Fuente del control "SERVICIOS" compartida en memoria (singleton) con
 * persistencia local.
 *
 * MISMO patrón que [AlertaStore]/[NegocioStore]: todos leen/alternan el MISMO
 * estado, y cada cambio se persiste en [EstadoServiciosLocalStore] (DataStore).
 *
 * Es un control de EJECUCIÓN, no de identidad: desactivarlo NUNCA cierra
 * sesión, NUNCA expulsa trabajadores y NUNCA borra datos históricos.
 */
object EstadoServiciosStore {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _estados = MutableStateFlow<List<EstadoServicios>>(emptyList())
    val estados: StateFlow<List<EstadoServicios>> = _estados

    private var localStore: EstadoServiciosLocalStore? = null

    /**
     * Inicializa el store con el contexto de la app y carga los estados
     * persistidos. Llamar varias veces es seguro (solo inicializa una vez).
     */
    fun init(context: Context) {
        val appContext = context.applicationContext
        if (localStore == null) {
            val store = EstadoServiciosLocalStore(appContext)
            localStore = store
            scope.launch {
                _estados.value = store.leerUnaVez().map { it.aModelo() }
            }
        }
    }

    /** Estado de SERVICIOS de un negocio. Si no existe, devuelve el default (activado). */
    fun estadoDe(negocioId: String): EstadoServicios =
        _estados.value.find { it.negocioId == negocioId }
            ?: EstadoServicios(negocioId = negocioId)

    /**
     * Alterna el estado de SERVICIOS de un negocio.
     *
     * Solo construye el estado/switch local: NO implementa ninguna lógica real
     * de "detener distribución remota" (Supabase en standby).
     *
     * // TODO: cuando exista distribución remota (post-Supabase), verificar
     * // EstadoServiciosStore.activado antes de enviar eventos a otros dispositivos.
     */
    fun alternar(negocioId: String) {
        val actual = estadoDe(negocioId)
        val nuevo = actual.copy(activado = !actual.activado)
        _estados.value = _estados.value.filterNot { it.negocioId == negocioId } + nuevo
        persistir()
    }

    private fun persistir() {
        localStore?.let { store ->
            val snapshot = _estados.value
            scope.launch { store.guardar(snapshot) }
        }
    }
}