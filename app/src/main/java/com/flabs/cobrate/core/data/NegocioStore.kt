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
import kotlinx.coroutines.flow.asStateFlow
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

    // ---- CONTEXTO ACTIVO (C9) ----
    private val _negocioActivoId = MutableStateFlow<String?>(null)
    val negocioActivoId: StateFlow<String?> = _negocioActivoId

    // ---- SEÑAL DATOS LISTOS (CAPA 55) ----
    private val _listo = MutableStateFlow<Boolean>(true)
    val listo: StateFlow<Boolean> = _listo.asStateFlow()

    /** Establece el negocio activo para toda la aplicación. */
    fun seleccionarNegocio(id: String?) {
        _negocioActivoId.value = id
    }

    // CAPA 8-SANEAMIENTO: Identidad de la sesión actual para aislamiento.
    private var currentSessionUid: String? = null

    private var localStore: NegocioLocalStore? = null

    private val repository by lazy { NegocioRepository() }

    /**
     * Inicializa el store y comienza a observar la sesión para cargar la caché local (C8).
     */
    fun init(context: Context) {
        val appContext = context.applicationContext
        if (localStore == null) {
            localStore = NegocioLocalStore(appContext)
            
            // Observar cambios de sesión
            scope.launch {
                CuentaStore.sesionActual.collect { cuenta ->
                    val newUid = cuenta?.id
                    if (newUid != currentSessionUid) {
                        currentSessionUid = newUid
                        if (newUid != null) {
                            _listo.value = false
                            try {
                                cargarDeDisco(newUid)
                            } finally {
                                _listo.value = true
                            }
                        } else {
                            _negocios.value = emptyList()
                            _listo.value = true
                        }
                    }
                }
            }
        }
    }

    private suspend fun cargarDeDisco(uid: String) {
        val guardadas = localStore?.leerUnaVez(uid) ?: emptyList()
        _negocios.value = guardadas.map { it.aModelo() }
    }

    /**
     * Crea un negocio nuevo y lo persiste en Supabase y localmente.
     * Si la escritura en Supabase falla, la operación falla completa.
     * DECISIÓN CAPA 7d-1: No hay cola de sincronización; el servidor es fuente de verdad.
     */
    suspend fun crear(nombre: String, tipo: TipoNegocio, dueñoCuentaId: String, colorMarca: String? = null): Result<Negocio> {
        val negocio = Negocio(
            id = UUID.randomUUID().toString(),
            nombre = nombre.trim(),
            tipo = tipo,
            dueñoCuentaId = dueñoCuentaId,
            colorMarca = colorMarca?.trim()?.ifBlank { null }
        )
        
        val result = repository.crear(negocio)
        if (result.isSuccess) {
            val creado = result.getOrThrow()
            _negocios.value = _negocios.value + creado
            persistir()
            return Result.success(creado)
        }
        return Result.failure(result.exceptionOrNull() ?: Exception("Error desconocido en Supabase"))
    }

    /** Tope de trabajadores del negocio (null = sin límite). */
    suspend fun maxTrabajadores(negocioId: String): Int? =
        repository.obtenerMaxTrabajadores(negocioId).getOrNull()

    /** Edita nombre y tope de trabajadores en el servidor y, si sale bien, el nombre local. */
    suspend fun editar(negocioId: String, nombre: String?, tipo: TipoNegocio?, colorMarca: String?, max: Int?, limpiarMax: Boolean): Result<Unit> {
        val limpio = nombre?.trim()?.ifBlank { null }
        val resultado = repository.actualizarNegocio(negocioId, limpio, tipo, colorMarca, max, limpiarMax)
        if (resultado.isSuccess) {
            _negocios.value = _negocios.value.map {
                if (it.id == negocioId) it.copy(nombre = limpio ?: it.nombre, tipo = tipo ?: it.tipo, colorMarca = colorMarca ?: it.colorMarca) else it
            }
            persistir()
        }
        return resultado
    }

    /** Busca un negocio por su id. Null si no existe. */
    fun buscarPorId(negocioId: String): Negocio? =
        _negocios.value.find { it.id == negocioId }

    /**
     * Sincroniza (reemplaza) los negocios locales con los del servidor.
     * CAPA 7d-2.
     */
    fun sincronizar(nuevosNegocios: List<Negocio>) {
        _negocios.value = nuevosNegocios
        scope.launch { persistir() }
    }

    /** Quita un negocio de la memoria y el disco (CAPA 41). */
    fun quitarNegocio(negocioId: String) {
        _negocios.value = _negocios.value.filterNot { it.id == negocioId }
        if (_negocioActivoId.value == negocioId) {
            _negocioActivoId.value = null
        }
        scope.launch { persistir() }
    }

    /**
     * Limpia la memoria local (usado en Logout para evitar fuga de datos).
     */
    fun limpiar() {
        _negocios.value = emptyList()
        _negocioActivoId.value = null
        currentSessionUid = null
        _listo.value = true
    }

    /**
     * CAPA 8-SANEAMIENTO: Persistencia suspendida y aislada.
     */
    private suspend fun persistir() {
        val uid = currentSessionUid ?: return
        val store = localStore ?: return
        val snapshot = _negocios.value
        store.guardar(uid, snapshot)
    }
}
