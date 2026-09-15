package com.flabs.cobrate.core.data

import android.content.Context
import com.flabs.cobrate.core.model.EventoDeNotificacion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Fuente de eventos de notificación de pago compartida en memoria (singleton)
 * con persistencia local.
 *
 * MISMO patrón que [InvitacionStore]/[InMemoryOperationStore]: todos leen la
 * MISMA lista, y cada cambio se persiste en [EventoDeNotificacionLocalStore].
 *
 * Esta capa guarda el evento tal cual llega (crudo), sin interpretar su
 * significado. El parsing de montos y la generación de operaciones llegan en
 * capas posteriores (6c+).
 */
object EventoDeNotificacionStore {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _eventos = MutableStateFlow<List<EventoDeNotificacion>>(emptyList())
    val eventos: StateFlow<List<EventoDeNotificacion>> = _eventos

    private var localStore: EventoDeNotificacionLocalStore? = null

    /**
     * Inicializa el store con el contexto de la app y carga los eventos
     * persistidos. Llamar varias veces es seguro (solo inicializa una vez).
     */
    fun init(context: Context) {
        val appContext = context.applicationContext
        if (localStore == null) {
            val store = EventoDeNotificacionLocalStore(appContext)
            localStore = store
            scope.launch {
                val guardados = store.leerUnaVez()
                if (guardados.isNotEmpty()) {
                    _eventos.value = guardados.map { it.aModelo() }
                }
            }
        }
    }

    /** Agrega un evento crudo al final de la lista y lo persiste. */
    fun agregar(evento: EventoDeNotificacion) {
        _eventos.value = _eventos.value + evento
        persistir()
    }

    /**
     * Marca un evento como procesado (procesado = true). No-op si el id no
     * existe. Persiste el cambio.
     */
    fun marcarProcesado(id: String) {
        _eventos.value = _eventos.value.map { evento ->
            if (evento.id == id) evento.copy(procesado = true) else evento
        }
        persistir()
    }

    /**
     * Limpia la memoria local y el disco (usado en Logout para evitar fuga de datos entre cuentas).
     */
    fun limpiar() {
        _eventos.value = emptyList()
        persistir()
    }

    private fun persistir() {
        localStore?.let { store ->
            val snapshot = _eventos.value
            scope.launch { store.guardar(snapshot) }
        }
    }
}