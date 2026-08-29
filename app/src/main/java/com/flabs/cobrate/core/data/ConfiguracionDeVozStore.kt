package com.flabs.cobrate.core.data

import android.content.Context
import com.flabs.cobrate.core.model.ConfiguracionDeVoz
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Fuente única de configuración de voz compartida en memoria (singleton) con
 * persistencia local.
 *
 * MISMO patrón que [AlertaStore]/[InvitacionStore]: la UI (pantalla de
 * configuración) y, en una capa posterior, el flujo de confirmación de pago
 * leen la MISMA [StateFlow] y cada cambio se persiste en
 * [ConfiguracionDeVozLocalStore].
 */
object ConfiguracionDeVozStore {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _configuracion =
        MutableStateFlow<ConfiguracionDeVoz>(ConfiguracionDeVoz.POR_DEFECTO)
    val configuracion: StateFlow<ConfiguracionDeVoz> = _configuracion

    private var localStore: ConfiguracionDeVozLocalStore? = null

    /**
     * Inicializa el store con el contexto de la app y carga la configuración
     * persistida. Llamar varias veces es seguro (solo inicializa una vez).
     */
    fun init(context: Context) {
        val appContext = context.applicationContext
        if (localStore == null) {
            val store = ConfiguracionDeVozLocalStore(appContext)
            localStore = store
            scope.launch {
                store.leerUnaVez()?.let { dto ->
                    _configuracion.value = dto.aModelo()
                }
            }
        }
    }

    /**
     * Actualiza la configuración completa (reemplaza el estado actual),
     * persistiendo el nuevo valor de forma asíncrona.
     */
    fun actualizar(config: ConfiguracionDeVoz) {
        _configuracion.value = config
        persistir(config)
    }

    /** Actualiza solo los campos indicados vía [transform]. */
    fun actualizar(transform: (ConfiguracionDeVoz) -> ConfiguracionDeVoz) {
        val nueva = transform(_configuracion.value)
        _configuracion.value = nueva
        persistir(nueva)
    }

    private fun persistir(config: ConfiguracionDeVoz) {
        localStore?.let { store ->
            scope.launch { store.guardar(config) }
        }
    }
}