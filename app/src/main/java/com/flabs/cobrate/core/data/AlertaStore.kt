package com.flabs.cobrate.core.data

import android.content.Context
import com.flabs.cobrate.core.model.Alerta
import com.flabs.cobrate.core.model.TipoAlerta
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

/**
 * Fuente de alertas compartida en memoria (singleton) con persistencia local.
 *
 * MISMO patrón que [InvitacionStore]/[EventoDeNotificacionStore]: todos leen
 * la MISMA lista, y cada cambio se persiste en [AlertaLocalStore].
 */
object AlertaStore {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _alertas = MutableStateFlow<List<Alerta>>(emptyList())
    val alertas: StateFlow<List<Alerta>> = _alertas

    private var localStore: AlertaLocalStore? = null

    /**
     * Inicializa el store con el contexto de la app y carga las alertas
     * persistidas. Llamar varias veces es seguro (solo inicializa una vez).
     */
    fun init(context: Context) {
        val appContext = context.applicationContext
        if (localStore == null) {
            val store = AlertaLocalStore(appContext)
            localStore = store
            scope.launch {
                val guardadas = store.leerUnaVez()
                if (guardadas.isNotEmpty()) {
                    _alertas.value = guardadas.map { it.aModelo() }
                }
            }
        }
    }

    /**
     * Crea una alerta para el Dueño y la persiste.
     *
     * [eventoId] referencia el evento de notificación (CAPA 6c-3) que la
     * originó, ya que al momento de la detección todavía no existe una
     * operación. [operacionId] queda vacío (placeholder, se puebla en capas
     * posteriores cuando exista la operación).
     */
    fun crear(mensaje: String, eventoId: String?): Alerta {
        val alerta = Alerta(
            id = UUID.randomUUID().toString(),
            tipo = TipoAlerta.METODO_NO_HABILITADO,
            operacionId = "",
            mensaje = mensaje,
            eventoId = eventoId
        )
        _alertas.value = _alertas.value + alerta
        persistir()
        return alerta
    }

    private fun persistir() {
        localStore?.let { store ->
            val snapshot = _alertas.value
            scope.launch { store.guardar(snapshot) }
        }
    }
}