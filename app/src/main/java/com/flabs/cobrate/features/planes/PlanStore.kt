package com.flabs.cobrate.features.planes

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
    private val _estado = MutableStateFlow<EstadoPlan?>(null)
    val estado: StateFlow<EstadoPlan?> = _estado

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
            _estado.value = PlanLogica.aEstado(it)
        }.onFailure {
            Log.w("PlanStore", "No se pudo consultar el plan: ${it.message}")
        }
    }

    /** Anota que el servidor rechazó un cobro por cuota agotada, para reflejarlo sin esperar la consulta. */
    fun marcarCuotaAgotada() {
        val actual = _estado.value ?: return
        val limite = actual.limite ?: return
        _estado.value = actual.copy(usadas = maxOf(actual.usadas, limite))
    }

    fun limpiar() {
        _estado.value = null
    }
}
