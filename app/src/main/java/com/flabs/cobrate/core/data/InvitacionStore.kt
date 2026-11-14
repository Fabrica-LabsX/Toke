package com.flabs.cobrate.core.data

import android.content.Context
import com.flabs.cobrate.core.model.EstadoInvitacion
import com.flabs.cobrate.core.model.InvitacionTrabajador
import com.flabs.cobrate.core.model.toInvitacion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Fuente de invitaciones de trabajador compartida en memoria (singleton) con
 * persistencia local.
 *
 * MISMO patrón que [CuentaStore]/[NegocioStore]: todos leen/actualizan la MISMA
 * lista de invitaciones, y cada cambio se persiste en [InvitacionLocalStore].
 */
object InvitacionStore {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private const val LARGO_CODIGO = 6
    private const val MINUTOS_VALIDEZ = 5L
    const val RETENCION_POST_VENCIMIENTO_MS = 60 * 60 * 1000L // 1 hora como revocado, luego se elimina definitivo

    // Caracteres legibles (sin 0/O, 1/I, etc.) para evitar confusiones al leerlos.
    private val CARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray()

    private val _invitaciones = MutableStateFlow<List<InvitacionTrabajador>>(emptyList())
    val invitaciones: StateFlow<List<InvitacionTrabajador>> = _invitaciones

    private var localStore: InvitacionLocalStore? = null

    private val repository by lazy { InvitacionRepository() }

    /**
     * Inicializa el store con el contexto de la app y carga las invitaciones
     * persistidas. Llamar varias veces es seguro (solo inicializa una vez).
     */
    fun init(context: Context) {
        val appContext = context.applicationContext
        if (localStore == null) {
            val store = InvitacionLocalStore(appContext)
            localStore = store
            scope.launch {
                _invitaciones.value = store.leerUnaVez().map { it.aModelo() }
            }
        }
    }

    /**
     * Crea una invitación nueva en Supabase y localmente.
     * Si falla en Supabase, la operación falla completa.
     * DECISIÓN CAPA 7d-1: El servidor es la fuente de verdad.
     */
    suspend fun crear(negocioId: String, requiereAprobacion: Boolean = true): Result<InvitacionTrabajador> {
        val ahora = System.currentTimeMillis()
        val codigo = generarCodigo()
        val expiracion = ahora + MINUTOS_VALIDEZ * 60 * 1000
        
        val result = repository.crear(negocioId, null, codigo, expiracion, requiereAprobacion)
        if (result.isSuccess) {
            val creada = result.getOrThrow()
            _invitaciones.value = _invitaciones.value + creada
            persistir()
            return Result.success(creada)
        }
        return Result.failure(result.exceptionOrNull() ?: Exception("Error en Supabase"))
    }

    /** Busca una invitación por su código (case-insensitive). Null si no existe. */
    fun buscarPorCodigo(codigo: String): InvitacionTrabajador? {
        val normalizado = codigo.trim().uppercase()
        return _invitaciones.value.find { it.codigo == normalizado }
    }

    /** Agrega una invitación manualmente a la caché (C8). */
    fun agregarManual(invitacion: InvitacionTrabajador) {
        val actual = _invitaciones.value.toMutableList()
        val index = actual.indexOfFirst { it.codigo == invitacion.codigo }
        if (index != -1) {
            actual[index] = invitacion
        } else {
            actual.add(invitacion)
        }
        _invitaciones.value = actual
        persistir()
    }

    /**
     * Busca una invitación remotamente por su código.
     * CAPA 4-FIX: Usa la RPC obtenerPreview en lugar de SELECT directo para 
     * permitir el acceso a trabajadores (sesión anónima) restringido por RLS.
     */
    suspend fun obtenerRemoto(codigo: String): Result<InvitacionTrabajador?> {
        val normalizado = codigo.trim().uppercase()
        return repository.obtenerPreview(normalizado).onSuccess { preview ->
            if (preview != null) {
                val invitacion = preview.toInvitacion()
                val actual = _invitaciones.value.toMutableList()
                val index = actual.indexOfFirst { it.codigo == normalizado }
                if (index != -1) {
                    actual[index] = invitacion
                } else {
                    actual.add(invitacion)
                }
                _invitaciones.value = actual
                persistir()
            }
        }.map { it?.toInvitacion() }
    }

    /** 
     * Marca una invitación como ACEPTADA en Supabase y localmente.
     * Si falla en Supabase, la operación falla completa.
     */
    suspend fun marcarAceptada(codigo: String): Result<Unit> {
        val normalizado = codigo.trim().uppercase()
        val result = repository.marcarAceptada(normalizado)
        if (result.isSuccess) {
            _invitaciones.value = _invitaciones.value.map {
                if (it.codigo == normalizado) it.copy(estado = EstadoInvitacion.ACEPTADA) else it
            }
            persistir()
            return Result.success(Unit)
        }
        return Result.failure(result.exceptionOrNull() ?: Exception("Error en Supabase"))
    }

    /**
     * Limpia la memoria local y el disco (usado en Logout para evitar fuga de datos entre cuentas).
     */
    fun limpiar() {
        _invitaciones.value = emptyList()
        persistir()
    }

    private fun generarCodigo(): String {
        val sb = StringBuilder(LARGO_CODIGO)
        repeat(LARGO_CODIGO) {
            sb.append(CARS.random())
        }
        return sb.toString()
    }

    private fun persistir() {
        localStore?.let { store ->
            val snapshot = _invitaciones.value
            scope.launch { store.guardar(snapshot) }
        }
    }
}