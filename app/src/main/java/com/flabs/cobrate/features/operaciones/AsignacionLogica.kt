package com.flabs.cobrate.features.operaciones

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.flabs.cobrate.core.data.NegocioStore
import com.flabs.cobrate.core.data.RelacionCuentaNegocioStore
import com.flabs.cobrate.core.model.EstadoRelacion
import com.flabs.cobrate.core.model.RolCuentaNegocio
import com.flabs.cobrate.features.planes.PlanStore

/**
 * Los "pagos por asignar" solo existen cuando el negocio tiene 2 o más trabajadores activos.
 * Con ninguno o con uno, el pago no espera a nadie: es del único trabajador (o del dueño).
 */
object AsignacionLogica {
    const val MIN_TRABAJADORES = 2

    /**
     * @param localesActivos trabajadores activos que este celular conoce del negocio.
     * @param servidor trabajadores activos según el servidor (el celular de un trabajador no ve a los demás).
     */
    fun habilitada(localesActivos: Int, servidor: Int?): Boolean =
        maxOf(localesActivos, servidor ?: 0) >= MIN_TRABAJADORES
}

/** ¿El negocio activo tiene pagos por asignar? Se recalcula solo cuando cambian equipo o plan. */
@Composable
fun rememberAsignacionHabilitada(): Boolean {
    val relaciones by RelacionCuentaNegocioStore.relaciones.collectAsState()
    val negocioId by NegocioStore.negocioActivoId.collectAsState()
    val plan by PlanStore.estado.collectAsState()
    val locales = relaciones.count {
        it.negocioId == negocioId && it.rol == RolCuentaNegocio.TRABAJADOR && it.estado == EstadoRelacion.ACTIVA
    }
    return AsignacionLogica.habilitada(locales, plan?.trabajadoresActivos)
}
