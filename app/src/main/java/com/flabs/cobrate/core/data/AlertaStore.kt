package com.flabs.cobrate.core.data

import android.content.Context
import android.util.Log
import com.flabs.cobrate.core.model.Alerta
import com.flabs.cobrate.core.model.TipoAlerta
import com.flabs.cobrate.core.model.TipoMetodoCobro
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Fuente de alertas compartida en memoria (singleton) con persistencia local aislada por cuenta.
 */
object AlertaStore {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _alertas = MutableStateFlow<List<Alerta>>(emptyList())
    val alertas: StateFlow<List<Alerta>> = _alertas

    private var currentSessionUid: String? = null
    private var localStore: AlertaLocalStore? = null

    /**
     * Inicializa el store con el contexto de la app y gestiona la caché por sesión.
     */
    fun init(context: Context) {
        val appContext = context.applicationContext
        if (localStore == null) {
            val store = AlertaLocalStore(appContext)
            localStore = store
            
            scope.launch {
                CuentaStore.sesionActual.collect { cuenta ->
                    val newUid = cuenta?.id
                    if (newUid != currentSessionUid) {
                        currentSessionUid = newUid
                        if (newUid != null) {
                            val guardadas = store.leerUnaVez(newUid)
                            _alertas.value = guardadas
                        } else {
                            _alertas.value = emptyList()
                        }
                    }
                }
            }
        }
    }

    /**
     * Crea una alerta para el Dueño/Admin y la persiste de forma deduplicada.
     */
    fun crear(
        mensaje: String,
        eventoId: String?,
        negocioId: String,
        cuentaId: String,
        tipoMetodoCobro: TipoMetodoCobro? = null
    ): Alerta {
        val existente = _alertas.value.find { 
            it.negocioId == negocioId && 
                it.cuentaId == cuentaId && 
                it.tipoMetodoCobro == tipoMetodoCobro && 
                it.tipo == TipoAlerta.METODO_NO_HABILITADO 
        }
        if (existente != null) return existente

        val alerta = Alerta(
            id = UUID.randomUUID().toString(),
            tipo = TipoAlerta.METODO_NO_HABILITADO,
            operacionId = "",
            mensaje = mensaje,
            eventoId = eventoId,
            negocioId = negocioId,
            cuentaId = cuentaId,
            tipoMetodoCobro = tipoMetodoCobro
        )
        _alertas.value = _alertas.value + alerta
        persistir()
        return alerta
    }

    /**
     * Descarta/elimina una alerta individualmente de memoria y disco.
     */
    fun descartarAlerta(alertaId: String) {
        _alertas.value = _alertas.value.filterNot { it.id == alertaId }
        persistir()
    }

    /**
     * Resuelve/elimina de memoria y disco únicamente las alertas asociadas al negocioId y tipoMetodoCobro indicado.
     */
    fun resolverPorMetodo(negocioId: String, tipoMetodo: TipoMetodoCobro) {
        val antes = _alertas.value
        val filtradas = antes.filterNot { 
            it.negocioId == negocioId && it.tipoMetodoCobro == tipoMetodo 
        }
        if (filtradas.size != antes.size) {
            _alertas.value = filtradas
            persistir()
            Log.d("AlertaStore", "Resueltas ${antes.size - filtradas.size} alerta(s) para $tipoMetodo en negocio $negocioId")
        }
    }

    /**
     * Limpia la memoria local (usado en Logout).
     */
    fun limpiar() {
        _alertas.value = emptyList()
        currentSessionUid = null
    }

    private fun persistir() {
        val uid = currentSessionUid ?: return
        localStore?.let { store ->
            val snapshot = _alertas.value
            scope.launch { store.guardar(uid, snapshot) }
        }
    }
}
