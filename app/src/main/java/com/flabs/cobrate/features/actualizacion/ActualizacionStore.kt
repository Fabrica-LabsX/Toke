package com.flabs.cobrate.features.actualizacion

import android.content.Context
import android.content.SharedPreferences
import com.flabs.cobrate.BuildConfig
import com.flabs.cobrate.core.logic.LogSeguro
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL

/**
 * Comprueba al abrir la app si hay una versión nueva. Corre en segundo plano (nunca en el hilo principal),
 * guarda la última respuesta verificada y, si el servidor no responde, decide con lo guardado.
 */
object ActualizacionStore {
    private const val PREFS = "toke_actualizacion"
    private const val CLAVE_MANIFIESTO = "manifiesto"
    private const val CLAVE_FECHA = "verificado_en"
    private val json = Json { ignoreUnknownKeys = true }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _estado = MutableStateFlow<EstadoActualizacion>(EstadoActualizacion.AlDia)
    val estado: StateFlow<EstadoActualizacion> = _estado

    private val _opcionalDescartada = MutableStateFlow(false)
    val opcionalDescartada: StateFlow<Boolean> = _opcionalDescartada

    fun descartarOpcional() { _opcionalDescartada.value = true }

    fun versionInstalada(c: Context): Int =
        runCatching { c.packageManager.getPackageInfo(c.packageName, 0).longVersionCode.toInt() }.getOrDefault(0)

    /** Decide al instante con lo guardado (si ya se sabe que es obligatoria, se bloquea sin esperar a la red) y luego consulta. */
    fun iniciar(contexto: Context) {
        val c = contexto.applicationContext
        val prefs = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val guardado = leerGuardado(prefs, c.packageName)
        if (guardado != null) {
            _estado.value = ActualizacionLogica.decidir(versionInstalada(c), guardado.first, guardado.second, System.currentTimeMillis())
        }
        scope.launch { consultar(c, prefs) }
    }

    /** Repite la consulta (al reintentar o al volver a la app). */
    fun reconsultar(contexto: Context) {
        val c = contexto.applicationContext
        scope.launch { consultar(c, c.getSharedPreferences(PREFS, Context.MODE_PRIVATE)) }
    }

    private suspend fun consultar(c: Context, prefs: SharedPreferences) {
        val m = descargarManifiesto() ?: return // servidor caído o sin red: se conserva lo guardado
        if (!ActualizacionLogica.manifiestoValido(m, c.packageName)) {
            LogSeguro.w("Actualizacion", "Respuesta de versión descartada: no es válida para este paquete")
            return
        }
        val ahora = System.currentTimeMillis()
        prefs.edit()
            .putString(CLAVE_MANIFIESTO, json.encodeToString(ManifiestoVersion.serializer(), m))
            .putLong(CLAVE_FECHA, ahora)
            .apply()
        _estado.value = ActualizacionLogica.decidir(versionInstalada(c), m, ahora, ahora)
    }

    private fun leerGuardado(prefs: SharedPreferences, paquete: String): Pair<ManifiestoVersion, Long>? {
        val txt = prefs.getString(CLAVE_MANIFIESTO, null) ?: return null
        val m = runCatching { json.decodeFromString(ManifiestoVersion.serializer(), txt) }.getOrNull() ?: return null
        return if (ActualizacionLogica.manifiestoValido(m, paquete)) m to prefs.getLong(CLAVE_FECHA, 0L) else null
    }

    private suspend fun descargarManifiesto(): ManifiestoVersion? = withContext(Dispatchers.IO) {
        if (BuildConfig.SUPABASE_URL.isBlank()) return@withContext null
        var con: HttpURLConnection? = null
        try {
            val url = "${BuildConfig.SUPABASE_URL.trimEnd('/')}/functions/v1/check-app-release?channel=stable"
            con = (URL(url).openConnection() as HttpURLConnection).apply {
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("apikey", BuildConfig.SUPABASE_ANON_KEY)
                setRequestProperty("Accept", "application/json")
            }
            if (con.responseCode != 200) return@withContext null
            json.decodeFromString(ManifiestoVersion.serializer(), con.inputStream.bufferedReader().use { it.readText() })
        } catch (e: Exception) {
            null
        } finally {
            con?.disconnect()
        }
    }
}
