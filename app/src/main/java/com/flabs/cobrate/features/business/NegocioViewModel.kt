package com.flabs.cobrate.features.business

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flabs.cobrate.core.data.CuentaStore
import com.flabs.cobrate.core.data.NegocioStore
import com.flabs.cobrate.core.data.RelacionCuentaNegocioStore
import com.flabs.cobrate.core.logic.RolResolver
import com.flabs.cobrate.core.logic.RolEfectivo
import com.flabs.cobrate.core.model.EstadoRelacion
import com.flabs.cobrate.core.model.Negocio
import com.flabs.cobrate.core.model.RelacionCuentaNegocio
import com.flabs.cobrate.core.model.RolCuentaNegocio
import com.flabs.cobrate.core.model.TipoNegocio
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel de Negocios y relaciones, usado tanto por el flujo de Dueño como
 * por el de Trabajador (por eso está en features/business/, que es donde vive
 * la lógica de negocio, y se consume desde la pantalla de entrada).
 *
 * Se basa en la sesión activa de [CuentaStore] y en las fuentes compartidas
 * [RelacionCuentaNegocioStore] y [NegocioStore].
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

    /** Relaciones ACTIVAS de la cuenta con sesión activa. */
    private val misRelacionesActivas: StateFlow<List<RelacionCuentaNegocio>> =
        combine(sesion, relaciones) { cuenta, todas ->
            val id = cuenta?.id ?: return@combine emptyList()
            todas.filter { it.cuentaId == id && it.estado == EstadoRelacion.ACTIVA }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    /** Negocios donde la cuenta tiene una relación activa. */
    val misNegocios: StateFlow<List<Negocio>> =
        combine(misRelacionesActivas, negocios) { rels, todos ->
            val idsActivos = rels.map { it.negocioId }.toSet()
            todos.filter { it.id in idsActivos }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    /** Rol efectivo de la cuenta activa (para decidir la pantalla de entrada). */
    val rolEfectivo: StateFlow<RolEfectivo> =
        combine(sesion, relaciones) { cuenta, todas ->
            val id = cuenta?.id ?: return@combine RolEfectivo.SIN_NEGOCIOS
            RolResolver.rolEfectivoDe(id, todas)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = RolEfectivo.SIN_NEGOCIOS
        )

    /**
     * Crea un negocio nuevo y lo vincula automáticamente a la cuenta activa
     * con rol ADMINISTRADOR (estado ACTIVA, fechaVinculacion = ahora).
     * No-op si no hay sesión activa.
     */
    fun crearNegocio(nombre: String, tipo: TipoNegocio) {
        val cuentaId = CuentaStore.sesionActual.value?.id ?: return
        viewModelScope.launch {
            NegocioStore.crear(nombre, tipo, cuentaId)
                .onSuccess { negocio ->
                    RelacionCuentaNegocioStore.crear(
                        cuentaId = cuentaId,
                        negocioId = negocio.id,
                        rol = RolCuentaNegocio.ADMINISTRADOR
                    )
                }
        }
    }
}