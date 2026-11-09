package com.flabs.cobrate.features.operaciones

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.flabs.cobrate.core.data.NegocioStore
import com.flabs.cobrate.core.data.RelacionCuentaNegocioStore
import com.flabs.cobrate.core.model.EstadoRelacion
import com.flabs.cobrate.core.model.RolCuentaNegocio
import com.flabs.cobrate.features.planes.PlanStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Los "pagos por asignar" solo existen cuando el negocio tiene 2 o más trabajadores activos.
 * Con ninguno o con uno, el pago no espera a nadie: es del único trabajador (o del dueño).
 * El dueño puede además apagarlos para un negocio ("Asignación de cobros" en Editar negocio).
 */
object AsignacionLogica {
    const val MIN_TRABAJADORES = 2

    /**
     * @param localesActivos trabajadores activos que este celular conoce del negocio.
     * @param servidor trabajadores activos según el servidor (el celular de un trabajador no ve a los demás).
     * @param activadaPorElDueno interruptor de "Asignación de cobros" del negocio (por defecto encendido).
     */
    fun habilitada(localesActivos: Int, servidor: Int?, activadaPorElDueno: Boolean = true): Boolean =
        activadaPorElDueno && maxOf(localesActivos, servidor ?: 0) >= MIN_TRABAJADORES
}

/** Interruptor "Asignación de cobros" por negocio. Se guarda en este celular. */
object AsignacionPreferencia {
    private var prefs: SharedPreferences? = null
    private val apagados = MutableStateFlow<Set<String>>(emptySet())

    /** Negocios con la asignación apagada; la pantalla se recalcula al cambiar. */
    val negociosApagados: StateFlow<Set<String>> = apagados

    fun init(context: Context) {
        if (prefs != null) return
        val p = context.applicationContext.getSharedPreferences("asignacion_cobros", Context.MODE_PRIVATE)
        prefs = p
        apagados.value = p.all.filter { it.value == false }.keys
    }

    fun activada(negocioId: String): Boolean = negocioId !in apagados.value

    fun cambiar(negocioId: String, activada: Boolean) {
        prefs?.edit()?.putBoolean(negocioId, activada)?.apply()
        apagados.value = if (activada) apagados.value - negocioId else apagados.value + negocioId
    }
}

/** ¿El negocio activo tiene pagos por asignar? Se recalcula solo cuando cambian equipo, plan o el interruptor. */
@Composable
fun rememberAsignacionHabilitada(): Boolean {
    val relaciones by RelacionCuentaNegocioStore.relaciones.collectAsState()
    val negocioId by NegocioStore.negocioActivoId.collectAsState()
    val plan by PlanStore.estado.collectAsState()
    val apagados by AsignacionPreferencia.negociosApagados.collectAsState()
    val locales = relaciones.count {
        it.negocioId == negocioId && it.rol == RolCuentaNegocio.TRABAJADOR && it.estado == EstadoRelacion.ACTIVA
    }
    return AsignacionLogica.habilitada(locales, plan?.trabajadoresActivos, negocioId !in apagados)
}
