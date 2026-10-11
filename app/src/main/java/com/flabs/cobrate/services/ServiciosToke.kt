package com.flabs.cobrate.services

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.flabs.cobrate.core.data.CuentaStore
import com.flabs.cobrate.core.data.DispositivoRepository
import com.flabs.cobrate.core.data.EstadoOperativoStore
import com.flabs.cobrate.core.data.RealtimeSyncManager
import com.flabs.cobrate.core.logic.AndroidNotifier
import com.flabs.cobrate.core.logic.LogSeguro
import com.flabs.cobrate.core.logic.RegistroDispositivos
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Fachada única para controlar el estado encendido/apagado global de TOKE (CAPA 57).
 */
object ServiciosToke {

    private const val TAG = "TokeServicio"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    private var shutdownGeneration = 0L

    /** Apaga todas las funciones de TOKE en segundo plano y tiempo real de forma atómica en el hilo IO. */
    suspend fun apagarTodo(context: Context) = withContext(Dispatchers.IO) {
        val appContext = context.applicationContext
        synchronized(this@ServiciosToke) {
            ++shutdownGeneration
            EstadoOperativoStore.init(appContext)
            EstadoOperativoStore.setEncendido(appContext, false)
        }

        runCatching {
            TokeNotificationListenerService.desvincular()
            LogSeguro.d(TAG, "1. TokeNotificationListenerService -> desvinculado")
        }

        runCatching {
            appContext.stopService(Intent(appContext, TokeOperandoService::class.java))
            AndroidNotifier.cancelar(appContext, AndroidNotifier.NOTIF_ID_OPERANDO)
            LogSeguro.d(TAG, "2. TokeOperandoService detenido y notificación cancelada")
        }

        runCatching {
            RealtimeSyncManager.detenerTodo()
            LogSeguro.d(TAG, "3. RealtimeSyncManager -> detenido")
        }
    }

    /** Finaliza la tarea de la aplicación usando la Activity disponible o el ActivityManager en el hilo Main. */
    suspend fun cerrarTarea(activity: Activity?, context: Context) = withContext(Dispatchers.Main) {
        if (activity != null && !activity.isFinishing && !activity.isDestroyed) {
            runCatching { activity.finishAndRemoveTask() }
        } else {
            runCatching {
                val am = context.applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
                am?.appTasks?.forEach { task ->
                    runCatching { task.finishAndRemoveTask() }
                }
            }.onFailure { err ->
                LogSeguro.w(TAG, "Error finalizando tareas desde ActivityManager", err)
            }
        }
    }

    /** Apaga todas las funciones de TOKE y finaliza las tareas de la aplicación vía ActivityManager. */
    suspend fun apagarTodoYCerrar(context: Context) {
        val appContext = context.applicationContext
        runCatching { apagarTodo(appContext) }
        cerrarTarea(null, appContext)
    }

    /** Apaga todas las funciones de TOKE y finaliza la actividad indicada directamente. */
    suspend fun apagarYSalir(activity: Activity) {
        val appContext = activity.applicationContext
        runCatching { apagarTodo(appContext) }
        cerrarTarea(activity, appContext)
    }

    /** Enciende todas las funciones de TOKE en segundo plano y tiempo real de forma segura y libre de carreras. */
    suspend fun encenderTodo(context: Context) = withContext(Dispatchers.IO) {
        val appContext = context.applicationContext
        val gen: Long

        synchronized(this@ServiciosToke) {
            gen = shutdownGeneration
            EstadoOperativoStore.init(appContext)
            if (gen != shutdownGeneration) {
                LogSeguro.d(TAG, "encenderTodo cancelado antes de iniciar por apagado concurrente.")
                return@withContext
            }
            EstadoOperativoStore.setEncendido(appContext, true)
        }

        if (!esGeneracionValida(gen)) return@withContext

        runCatching {
            val intent = Intent(appContext, TokeOperandoService::class.java)
            ContextCompat.startForegroundService(appContext, intent)
            LogSeguro.d(TAG, "1. TokeOperandoService -> iniciado")
        }.onFailure { err ->
            LogSeguro.w(TAG, "Excepción al iniciar TokeOperandoService", err)
        }

        if (!esGeneracionValida(gen)) return@withContext

        val uid = CuentaStore.sesionActual.value?.id
        if (uid != null && esGeneracionValida(gen)) {
            runCatching {
                CuentaStore.sincronizarDatos(uid)
                RegistroDispositivos.registrar(FirebaseProveedorDeToken(), DispositivoRepository())
                LogSeguro.d(TAG, "2. Datos y FCM re-sincronizados para UID ${uid.take(6)}")
            }
        }

        if (esGeneracionValida(gen)) {
            runCatching {
                TokeNotificationListenerService.revincular(appContext)
                LogSeguro.d(TAG, "3. TokeNotificationListenerService -> revinculado")
            }
        }
    }

    private fun esGeneracionValida(gen: Long): Boolean {
        return synchronized(this) {
            gen == shutdownGeneration && EstadoOperativoStore.encendido.value
        }
    }

    /** Alterna el estado global de servicios ejecutándolo de forma asíncrona. */
    fun alternar(context: Context) {
        val appContext = context.applicationContext
        EstadoOperativoStore.init(appContext)
        val actual = EstadoOperativoStore.encendido.value
        scope.launch {
            if (actual) {
                apagarTodo(appContext)
            } else {
                encenderTodo(appContext)
            }
        }
    }
}
