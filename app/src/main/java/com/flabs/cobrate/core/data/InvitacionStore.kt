package com.flabs.cobrate.core.data

import android.content.Context
import com.flabs.cobrate.core.model.EstadoInvitacion
import com.flabs.cobrate.core.model.InvitacionTrabajador
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

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
    private const val HORAS_VALIDEZ = 24L

    // Caracteres legibles (sin 0/O, 1/I, etc.) para evitar confusiones al leerlos.
    private val CARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray()

    private val _invitaciones = MutableStateFlow<List<InvitacionTrabajador>>(emptyList())
    val invitaciones: StateFlow<List<InvitacionTrabajador>> = _invitaciones

    private var localStore: InvitacionLocalStore? = null

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
     * Crea una invitación nueva para [negocioId] con un código corto legible
     * (6 caracteres alfanuméricos en mayúsculas) y expiración de ahora + 24 horas.
     */
    fun crear(negocioId: String): InvitacionTrabajador {
        val ahora = System.currentTimeMillis()
        val invitacion = InvitacionTrabajador(
            id = UUID.randomUUID().toString(),
            negocioId = negocioId,
            codigo = generarCodigo(),
            estado = EstadoInvitacion.PENDIENTE,
            fechaCreacion = ahora,
            expiracion = ahora + HORAS_VALIDEZ * 60 * 60 * 1000
        )
        _invitaciones.value = _invitaciones.value + invitacion
        persistir()
        return invitacion
    }

    /** Busca una invitación por su código (case-insensitive). Null si no existe. */
    fun buscarPorCodigo(codigo: String): InvitacionTrabajador? {
        val normalizado = codigo.trim().uppercase()
        return _invitaciones.value.find { it.codigo == normalizado }
    }

    /** Marca una invitación como ACEPTADA dado su código (no-op si no existe). */
    fun marcarAceptada(codigo: String) {
        val normalizado = codigo.trim().uppercase()
        _invitaciones.value = _invitaciones.value.map {
            if (it.codigo == normalizado) it.copy(estado = EstadoInvitacion.ACEPTADA) else it
        }
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