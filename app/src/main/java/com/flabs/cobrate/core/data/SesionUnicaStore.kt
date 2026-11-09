package com.flabs.cobrate.core.data

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import com.flabs.cobrate.core.logic.LogSeguro
import com.flabs.cobrate.features.acceso.AccionSesion
import com.flabs.cobrate.features.acceso.RespuestaSesion
import com.flabs.cobrate.features.acceso.SesionUnicaLogica
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put
import java.util.UUID

/** Aviso que la app debe mostrar por la sesión única. */
sealed class AvisoSesion {
    /** La cuenta ya está abierta en otro celular: preguntar si se usa aquí. */
    data class Ocupada(val dispositivoNombre: String, val iniciadaEn: Long) : AvisoSesion()

    /** Otro celular tomó la cuenta: este se cierra. */
    data object Desplazada : AvisoSesion()
}

/**
 * Sesión única: una cuenta solo puede estar abierta en un celular. Habla con `reclamar_sesion` y
 * `liberar_sesion` (migración 20261107150000). Si el servidor no responde o la función no existe, no
 * bloquea nada: los pagos detectados no pueden interrumpirse.
 */
object SesionUnicaStore {
    private const val TAG = "SesionUnica"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _aviso = MutableStateFlow<AvisoSesion?>(null)
    val aviso: StateFlow<AvisoSesion?> = _aviso

    private var prefs: SharedPreferences? = null
    private var canal: RealtimeChannel? = null
    private var uidEscuchado: String? = null

    fun init(context: Context) {
        if (prefs == null) prefs = context.applicationContext.getSharedPreferences("sesion_unica", Context.MODE_PRIVATE)
    }

    private fun dispositivoId(): String {
        val p = prefs ?: return "sin-inicializar-0000"
        return p.getString("dispositivo_id", null) ?: UUID.randomUUID().toString().also {
            p.edit().putString("dispositivo_id", it).apply()
        }
    }

    private fun nombreDispositivo() = SesionUnicaLogica.nombreDeDispositivo(Build.MANUFACTURER.orEmpty(), Build.MODEL.orEmpty())

    private fun claveReclamada(uid: String) = "reclamada_$uid"

    private suspend fun llamar(forzar: Boolean): RespuestaSesion = try {
        val json = SupabaseClientProvider.supabase.postgrest.rpc(
            function = "reclamar_sesion",
            parameters = buildJsonObject {
                put("p_dispositivo_id", dispositivoId())
                put("p_dispositivo_nombre", nombreDispositivo())
                put("p_forzar", forzar)
            },
        ).decodeAs<JsonObject>()
        if (json["estado"]?.jsonPrimitive?.contentOrNull == "OCUPADA") {
            RespuestaSesion.Ocupada(
                json["dispositivo_nombre"]?.jsonPrimitive?.contentOrNull ?: "Otro celular",
                json["iniciada_en"]?.jsonPrimitive?.longOrNull ?: System.currentTimeMillis(),
            )
        } else RespuestaSesion.Libre
    } catch (e: Exception) {
        LogSeguro.w(TAG, "No se pudo consultar la sesión única; se continúa sin bloquear", e)
        RespuestaSesion.SinRespuesta
    }

    /** Pregunta al servidor si este celular puede usar la cuenta abierta. Llamar al iniciar sesión y al volver a la app. */
    suspend fun verificar() {
        val uid = CuentaStore.sesionActual.value?.id ?: return
        if (_aviso.value != null) return
        when (val accion = SesionUnicaLogica.decidir(llamar(forzar = false))) {
            AccionSesion.Continuar -> {
                prefs?.edit()?.putBoolean(claveReclamada(uid), true)?.apply()
                escuchar(uid)
            }
            is AccionSesion.Preguntar -> {
                // Si yo tenía la cuenta y ahora está ocupada, otro celular me la quitó.
                _aviso.value = if (prefs?.getBoolean(claveReclamada(uid), false) == true) AvisoSesion.Desplazada
                else AvisoSesion.Ocupada(accion.dispositivoNombre, accion.iniciadaEn)
            }
        }
    }

    /** "Usar aquí": este celular pasa a ser el activo. */
    suspend fun usarAqui() {
        val uid = CuentaStore.sesionActual.value?.id ?: return
        llamar(forzar = true)
        prefs?.edit()?.putBoolean(claveReclamada(uid), true)?.apply()
        _aviso.value = null
        escuchar(uid)
    }

    /** Se cierra la sesión (a propósito o desplazada): libera la cuenta si era de este celular. */
    suspend fun liberar(uid: String?) {
        detener()
        _aviso.value = null
        if (uid != null) prefs?.edit()?.remove(claveReclamada(uid))?.apply()
        withTimeoutOrNull(3000L) {
            runCatching {
                SupabaseClientProvider.supabase.postgrest.rpc(
                    function = "liberar_sesion",
                    parameters = buildJsonObject { put("p_dispositivo_id", dispositivoId()) },
                )
            }
        }
    }

    private fun detener() {
        val c = canal
        canal = null
        uidEscuchado = null
        if (c != null) scope.launch { runCatching { c.unsubscribe() } }
    }

    /** Escucha en tiempo real si otro celular toma la cuenta. */
    private fun escuchar(uid: String) {
        if (uidEscuchado == uid) return
        detener()
        uidEscuchado = uid
        val ch = SupabaseClientProvider.supabase.realtime.channel("sesion_unica_$uid") {}
        canal = ch
        scope.launch {
            launch {
                runCatching {
                    ch.postgresChangeFlow<PostgresAction>(schema = "public") {
                        table = "sesiones_activas"
                        filter("cuenta_id", FilterOperator.EQ, uid)
                    }.collect { accion ->
                        val record = when (accion) {
                            is PostgresAction.Insert -> accion.record
                            is PostgresAction.Update -> accion.record
                            else -> null
                        }
                        val activo = record?.get("dispositivo_id")?.jsonPrimitive?.contentOrNull
                        if (SesionUnicaLogica.meSacaron(dispositivoId(), activo)) {
                            _aviso.value = AvisoSesion.Desplazada
                        }
                    }
                }.onFailure { LogSeguro.w(TAG, "Error en la escucha de la sesión única", it) }
            }
            runCatching { ch.subscribe() }.onFailure { LogSeguro.w(TAG, "No se pudo suscribir a la sesión única", it) }
        }
    }
}
