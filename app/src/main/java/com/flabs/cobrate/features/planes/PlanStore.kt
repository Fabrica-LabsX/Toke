package com.flabs.cobrate.features.planes


import com.flabs.cobrate.core.logic.LogSeguro
import android.content.Context
import android.util.Log
import com.flabs.cobrate.core.data.SupabaseClientProvider
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Plan del negocio activo, tal como lo dice el servidor. Si no se puede consultar (sin internet o
 * el servidor aún no tiene los planes) queda en null y no se bloquea nada: la barrera real vive
 * en Supabase, la app solo la muestra.
 */
object PlanStore {
    private const val PREFS = "plan_store"
    private const val CLAVE_PLAN_VISTO = "plan_visto"

    private val _estado = MutableStateFlow<EstadoPlan?>(null)
    val estado: StateFlow<EstadoPlan?> = _estado

    /** Se enciende cuando se detecta que el plan pasó de Free a Pro; la pantalla de éxito lo apaga. */
    private val _activacionPendiente = MutableStateFlow(false)
    val activacionPendiente: StateFlow<Boolean> = _activacionPendiente

    private var contexto: Context? = null

    fun init(context: Context) {
        contexto = context.applicationContext
    }

    /** Plan conocido, o null si todavía no se sabe. */
    val plan: Plan? get() = _estado.value?.plan

    suspend fun refrescar(negocioId: String?) {
        if (negocioId == null) {
            _estado.value = null
            return
        }
        runCatching {
            SupabaseClientProvider.supabase.postgrest.rpc(
                function = "estado_plan",
                parameters = buildJsonObject { put("p_negocio_id", negocioId) },
            ).decodeAs<EstadoPlanDto>()
        }.onSuccess {
            val nuevo = PlanLogica.aEstado(it)
            _estado.value = nuevo
            // La pantalla de "Pro activado" es solo para el plan de tu propia cuenta: un trabajador que abre un
            // negocio cuyo dueño es Pro no activó nada.
            val uid = com.flabs.cobrate.core.data.CuentaStore.sesionActual.value?.id
            val esMiNegocio = uid != null && com.flabs.cobrate.core.data.NegocioStore.negocios.value
                .find { n -> n.id == negocioId }?.dueñoCuentaId == uid
            if (esMiNegocio) registrarPlanVisto(uid!!, nuevo.plan)
        }.onFailure {
            LogSeguro.w("PlanStore", "No se pudo consultar el plan", it)
        }
    }

    /** El último plan visto se guarda por cuenta, para que cambiar de cuenta no parezca una activación. */
    private fun registrarPlanVisto(cuentaId: String, nuevo: Plan) {
        val prefs = contexto?.getSharedPreferences(PREFS, Context.MODE_PRIVATE) ?: return
        val clave = "${CLAVE_PLAN_VISTO}_$cuentaId"
        val anterior = prefs.getString(clave, null)?.let { runCatching { Plan.valueOf(it) }.getOrNull() }
        if (PlanLogica.esActivacionNueva(anterior, nuevo)) _activacionPendiente.value = true
        prefs.edit().putString(clave, nuevo.name).apply()
    }

    fun cerrarActivacion() {
        _activacionPendiente.value = false
    }

    /** Anota que el servidor rechazó un cobro por cuota agotada, para reflejarlo sin esperar la consulta. */
    fun marcarCuotaAgotada() {
        val actual = _estado.value ?: return
        val limite = actual.limite ?: return
        _estado.value = actual.copy(usadas = maxOf(actual.usadas, limite))
    }

    fun limpiar() {
        _estado.value = null
        _activacionPendiente.value = false
    }
}
