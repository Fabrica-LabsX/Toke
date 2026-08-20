package com.flabs.cobrate.core.data

import android.content.Context
import com.flabs.cobrate.core.model.Negocio
import com.flabs.cobrate.core.model.TipoNegocio
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

/**
 * Fuente de negocios compartida en memoria (singleton) con persistencia local.
 *
 * MISMO patrón que [CuentaStore]: puente entre flujos para que todos lean la
 * MISMA lista de negocios, y cada cambio se persiste en [NegocioLocalStore].
 */
object NegocioStore {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _negocios = MutableStateFlow<List<Negocio>>(emptyList())
    val negocios: StateFlow<List<Negocio>> = _negocios

    private var localStore: NegocioLocalStore? = null

    /**
     * Inicializa el store con el contexto de la app y carga los negocios
     * persistidos. Llamar varias veces es seguro (solo inicializa una vez).
     */
    fun init(context: Context) {
        val appContext = context.applicationContext
        if (localStore == null) {
            val store = NegocioLocalStore(appContext)
            localStore = store
            scope.launch {
                _negocios.value = store.leerUnaVez().map { it.aModelo() }
            }
        }
    }

    /**
     * Crea un negocio nuevo (id autogenerado) y lo persiste.
     * Devuelve el [Negocio] creado.
     */
    fun crear(nombre: String, tipo: TipoNegocio, dueñoCuentaId: String): Negocio {
        val negocio = Negocio(
            id = UUID.randomUUID().toString(),
            nombre = nombre.trim(),
            tipo = tipo,
            dueñoCuentaId = dueñoCuentaId
        )
        _negocios.value = _negocios.value + negocio
        persistir()
        return negocio
    }

    /** Busca un negocio por su id. Null si no existe. */
    fun buscarPorId(negocioId: String): Negocio? =
        _negocios.value.find { it.id == negocioId }

    private fun persistir() {
        localStore?.let { store ->
            val snapshot = _negocios.value
            scope.launch { store.guardar(snapshot) }
        }
    }
}