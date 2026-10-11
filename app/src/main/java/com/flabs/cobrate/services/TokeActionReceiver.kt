package com.flabs.cobrate.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.flabs.cobrate.core.data.NegocioStore
import com.flabs.cobrate.core.data.OperacionPendienteStore
import com.flabs.cobrate.core.logic.LogSeguro
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Receptor de acciones desde las notificaciones de TOKE (C11).
 * Permite ejecutar lógica de fondo (como apagar servicios o reintentar envíos) sin abrir la UI.
 */
class TokeActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d(TAG, "Acción recibida: $action")

        when (action) {
            ACTION_APAGAR_SERVICIOS -> {
                Log.i(TAG, "Apagando todos los servicios de TOKE")
                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    val appContext = context.applicationContext
                    try {
                        withTimeoutOrNull(4000L) {
                            ServiciosToke.apagarTodo(appContext)
                        }
                    } catch (e: Exception) {
                        LogSeguro.e(TAG, "Error durante apagarTodo", e)
                    } finally {
                        ServiciosToke.cerrarTarea(null, appContext)
                        pendingResult.finish()
                    }
                }
            }
            ACTION_REINTENTAR_ENVIO_COBRO -> {
                val operacionId = intent.getStringExtra(EXTRA_OPERACION_ID) ?: return
                Log.i(TAG, "Reintentando envío de cobro: $operacionId")
                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        OperacionPendienteStore.reintentarUna(context.applicationContext, operacionId)
                    } catch (e: Exception) {
                        LogSeguro.e(TAG, "Error durante reintento manual de operacion $operacionId", e)
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
        }
    }

    companion object {
        private const val TAG = "TokeActionReceiver"
        const val ACTION_APAGAR_SERVICIOS = "com.flabs.cobrate.ACTION_APAGAR_SERVICIOS"
        const val ACTION_REINTENTAR_ENVIO_COBRO = "com.flabs.cobrate.ACTION_REINTENTAR_ENVIO_COBRO"
        const val EXTRA_OPERACION_ID = "operacion_id"
    }
}
