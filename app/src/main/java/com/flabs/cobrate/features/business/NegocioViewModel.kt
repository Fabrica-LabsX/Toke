package com.flabs.cobrate.features.business

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flabs.cobrate.core.data.CuentaRepository
import com.flabs.cobrate.core.data.CuentaStore
import com.flabs.cobrate.core.data.EstadoSincronizacionRemota
import com.flabs.cobrate.core.data.NegocioStore
import com.flabs.cobrate.core.data.RelacionCuentaNegocioStore
import com.flabs.cobrate.core.logic.AccesoNegocio
import com.flabs.cobrate.core.logic.LogSeguro
import com.flabs.cobrate.core.logic.RolEfectivo
import com.flabs.cobrate.core.logic.RolResolver
import com.flabs.cobrate.core.model.EstadoRelacion
import com.flabs.cobrate.core.model.Negocio
import com.flabs.cobrate.core.model.RelacionCuentaNegocio
import com.flabs.cobrate.core.model.RolCuentaNegocio
import com.flabs.cobrate.core.model.TipoNegocio
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

private data class AutoCleanupState(
    val esValido: Boolean,
    val seleccionadoId: String?,
    val esExpulsado: Boolean,
    val nombreNegocio: String?
)

/**
 * ViewModel de Negocios y relaciones, usado tanto por el flujo de Dueño como
 * por el de Trabajador.
 */
class NegocioViewModel : ViewModel() {

    private val sesion = CuentaStore.sesionActual
    private val relaciones = RelacionCuentaNegocioStore.relaciones
    private val negocios = NegocioStore.negocios

    /** Relaciones de la cuenta con sesión activa (todas, cualquier estado). */
    val misRelaciones: StateFlow<List<RelacionCuentaNegocio>> =
        combine(sesion, relaciones) { cuenta, todas ->
            val id = cuenta?.id ?: return@combine emptyList()
            todas.filter { it.cuentaId == id }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    /** Relaciones VISIBLES de la cuenta con sesión activa. */
    private val misRelacionesVisibles: StateFlow<List<RelacionCuentaNegocio>> =
        combine(sesion, relaciones) { cuenta, todas ->
            val id = cuenta?.id ?: return@combine emptyList()
            todas.filter { it.cuentaId == id && (it.estado == EstadoRelacion.ACTIVA || it.estado == EstadoRelacion.PAUSADA || it.estado == EstadoRelacion.SOLICITADA || it.estado == EstadoRelacion.RECHAZADA) }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    /** Negocios donde la cuenta es dueña directa O tiene una relación visible. */
    val misNegocios: StateFlow<List<Negocio>> =
        combine(sesion, misRelacionesVisibles, negocios) { cuenta, rels, todos ->
            val uid = cuenta?.id ?: return@combine emptyList()
            val idsVisibles = rels.map { it.negocioId }.toSet()
            todos.filter { it.dueñoCuentaId == uid || it.id in idsVisibles }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    /** Rol efectivo de la cuenta activa (para decidir la pantalla de entrada). */
    val rolEfectivo: StateFlow<RolEfectivo> =
        combine(sesion, relaciones) { cuenta, todas ->
            val id = cuenta?.id ?: return@combine RolEfectivo.CARGANDO
            RolResolver.rolEfectivoDe(id, todas)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = RolEfectivo.CARGANDO
        )

    /** ID del negocio seleccionado manualmente. Única fuente de verdad del contexto activo (C9). */
    val negocioSeleccionadoId: StateFlow<String?> = NegocioStore.negocioActivoId

    /** Rol activo en el negocio seleccionado actualmente. */
    val rolActivo: StateFlow<RolCuentaNegocio?> =
        combine(sesion, negocioSeleccionadoId, relaciones, negocios) { cuenta, negId, rels, todosNegocios ->
            val uid = cuenta?.id ?: return@combine null
            if (negId == null) return@combine null

            val negocio = todosNegocios.find { it.id == negId }
            if (negocio != null && negocio.dueñoCuentaId == uid) {
                return@combine RolCuentaNegocio.ADMINISTRADOR
            }

            val rel = rels.find { it.cuentaId == uid && it.negocioId == negId && it.estado == EstadoRelacion.ACTIVA }
            rel?.rol
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    /** Estado de acceso al negocio seleccionado actualmente. */
    val estadoAcceso: StateFlow<AccesoNegocio.EstadoAcceso> =
        combine(sesion, negocioSeleccionadoId, relaciones) { cuenta, negId, rels ->
            val uid = cuenta?.id ?: return@combine AccesoNegocio.EstadoAcceso.NORMAL
            AccesoNegocio.estadoDeAcceso(uid, negId, rels)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AccesoNegocio.EstadoAcceso.NORMAL
        )

    // Estado para la notificación de expulsión (C10)
    private val _negocioExpulsado = MutableStateFlow<String?>(null)
    val negocioExpulsado: StateFlow<String?> = _negocioExpulsado.asStateFlow()

    private val cuentaRepo = CuentaRepository()
    private val idsCuentasEnVuelo = ConcurrentHashMap.newKeySet<String>()

    fun seleccionarNegocio(id: String?) {
        NegocioStore.seleccionarNegocio(id)
    }

    fun cargarCuentasFaltantes(ids: List<String>) {
        val cuentasCheadas = CuentaStore.cuentas.value.map { it.id }.toSet()
        val faltantes = ids.filter { it.isNotBlank() && it !in cuentasCheadas && it !in idsCuentasEnVuelo }
        if (faltantes.isEmpty()) return

        idsCuentasEnVuelo.addAll(faltantes)
        viewModelScope.launch {
            cuentaRepo.obtenerVariosPorId(faltantes).onSuccess { cuentas ->
                cuentas.forEach { CuentaStore.crear(it) }
            }.onFailure {
                idsCuentasEnVuelo.removeAll(faltantes.toSet())
            }
        }
    }

    fun descartarAvisoExpulsion() {
        _negocioExpulsado.value = null
    }

    init {
        viewModelScope.launch {
            negocios.collect { lista ->
                val cuids = lista.map { it.dueñoCuentaId }.distinct()
                cargarCuentasFaltantes(cuids)
            }
        }

        val datosListos = combine(
            RelacionCuentaNegocioStore.listo,
            NegocioStore.listo,
            CuentaStore.estadoSincronizacion
        ) { rL, nL, syncState ->
            rL && nL && syncState != EstadoSincronizacionRemota.SINCRONIZANDO
        }

        // C9: Auto-limpieza protegida frente a sincronizaciones intermedias
        viewModelScope.launch {
            combine(
                sesion,
                relaciones,
                negocioSeleccionadoId,
                negocios,
                datosListos
            ) { cuenta, todas, seleccionadoId, todosNegocios, listos ->
                val cuentaId = cuenta?.id ?: return@combine AutoCleanupState(true, null, false, null)
                if (seleccionadoId == null) return@combine AutoCleanupState(true, null, false, null)

                if (!listos) {
                    return@combine AutoCleanupState(true, seleccionadoId, false, null)
                }

                val negocio = todosNegocios.find { it.id == seleccionadoId }
                val esDueno = negocio != null && negocio.dueñoCuentaId == cuentaId

                val relacion = todas.find { it.cuentaId == cuentaId && it.negocioId == seleccionadoId }
                val esExpulsado = relacion?.estado == EstadoRelacion.EXPULSADA

                val esValido = if (esDueno) {
                    negocio != null
                } else {
                    relacion != null && !esExpulsado
                }

                AutoCleanupState(esValido, seleccionadoId, esExpulsado, negocio?.nombre)
            }.collect { (valido, id, esExpulsado, nombre) ->
                if (!valido && id != null) {
                    LogSeguro.w("NegocioViewModel", "[AUTO-CLEANUP] Des-seleccionando negocio $id por invalidez confirmada (expulsado=$esExpulsado)")
                    if (esExpulsado) {
                        _negocioExpulsado.value = nombre ?: "este negocio"
                    }
                    seleccionarNegocio(null)
                }
            }
        }
    }

    /**
     * Crea un negocio nuevo y lo vincula automáticamente a la cuenta activa
     * con rol ADMINISTRADOR (estado ACTIVA, fechaVinculacion = ahora).
     */
    suspend fun crearNegocio(nombre: String, tipo: TipoNegocio, colorMarca: String? = null): Result<Negocio> {
        val cuentaId = CuentaStore.sesionActual.value?.id 
            ?: return Result.failure(Exception("No hay sesión activa"))
            
        return NegocioStore.crear(nombre, tipo, cuentaId, colorMarca).onSuccess { negocio ->
            RelacionCuentaNegocioStore.crear(
                cuentaId = cuentaId,
                negocioId = negocio.id,
                rol = RolCuentaNegocio.ADMINISTRADOR
            )
            // Seleccionamos automáticamente el nuevo negocio
            seleccionarNegocio(negocio.id)
        }
    }
}
