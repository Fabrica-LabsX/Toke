package com.flabs.cobrate.core.data

import android.content.Context
import com.flabs.cobrate.core.model.EstadoRelacion
import com.flabs.cobrate.core.model.RelacionCuentaNegocio
import com.flabs.cobrate.core.model.RolCuentaNegocio
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Fuente de relaciones Cuenta↔Negocio compartida en memoria (singleton) con
 * persistencia local.
 *
 * MISMO patrón que [CuentaStore]: todos leen/actualizan la MISMA lista de
 * relaciones, y cada cambio se persiste en [RelacionCuentaNegocioLocalStore].
 */
object RelacionCuentaNegocioStore {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _relaciones = MutableStateFlow<List<RelacionCuentaNegocio>>(emptyList())
    val relaciones: StateFlow<List<RelacionCuentaNegocio>> = _relaciones

    private var localStore: RelacionCuentaNegocioLocalStore? = null

    private val repository = RelacionCuentaNegocioRepository()

    /**
     * Inicializa el store con el contexto de la app y carga las relaciones
     * persistidas. Llamar varias veces es seguro (solo inicializa una vez).
     */
    fun init(context: Context) {
        val appContext = context.applicationContext
        if (localStore == null) {
            val store = RelacionCuentaNegocioLocalStore(appContext)
            localStore = store
            scope.launch {
                _relaciones.value = store.leerUnaVez().map { it.aModelo() }
            }
        }
    }

    /** 
     * Crea (agrega) una relación nueva en Supabase y localmente. 
     * Si falla en Supabase, la operación falla completa.
     * DECISIÓN CAPA 7d-1: El servidor es la fuente de verdad.
     */
    suspend fun crear(cuentaId: String, negocioId: String, rol: RolCuentaNegocio): Result<RelacionCuentaNegocio> {
        val relacion = RelacionCuentaNegocio(
            cuentaId = cuentaId,
            negocioId = negocioId,
            rol = rol,
            estado = EstadoRelacion.ACTIVA,
            fechaVinculacion = System.currentTimeMillis()
        )
        
        val result = repository.crear(relacion)
        if (result.isSuccess) {
            val creada = result.getOrThrow()
            _relaciones.value = _relaciones.value + creada
            persistir()
            return Result.success(creada)
        }
        return Result.failure(result.exceptionOrNull() ?: Exception("Error en Supabase"))
    }

    /** Relaciones de una cuenta (todas, independientemente del estado). */
    fun relacionesDe(cuentaId: String): List<RelacionCuentaNegocio> =
        _relaciones.value.filter { it.cuentaId == cuentaId }

    /**
     * Sincroniza (reemplaza) las relaciones locales con las del servidor.
     * CAPA 7d-2.
     */
    fun sincronizar(nuevasRelaciones: List<RelacionCuentaNegocio>) {
        _relaciones.value = nuevasRelaciones
        persistir()
    }

    private fun persistir() {
        localStore?.let { store ->
            val snapshot = _relaciones.value
            scope.launch { store.guardar(snapshot) }
        }
    }
}