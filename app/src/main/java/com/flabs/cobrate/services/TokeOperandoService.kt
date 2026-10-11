package com.flabs.cobrate.services

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import com.flabs.cobrate.core.data.CuentaStore
import com.flabs.cobrate.core.data.EstadoOperativoStore
import com.flabs.cobrate.core.data.NegocioStore
import com.flabs.cobrate.core.data.SupabaseAuthBridge
import com.flabs.cobrate.core.logic.AndroidNotifier
import com.flabs.cobrate.core.logic.LogSeguro

/**
 * Servicio en primer plano que sostiene la notificación permanente "TOKE está operando" (CAPA 58).
 */
class TokeOperandoService : Service() {

    override fun onCreate() {
        super.onCreate()
        EstadoOperativoStore.init(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        EstadoOperativoStore.init(this)

        val encendido = EstadoOperativoStore.encendido.value
        val sesion = CuentaStore.sesionActual.value
        val estaInicializando = SupabaseAuthBridge.estaInicializando.value

        if (!encendido || (sesion == null && !estaInicializando)) {
            LogSeguro.d(TAG, "Servicio TokeOperandoService detenido (encendido=$encendido, sesionPresente=${sesion != null}, inicializando=$estaInicializando)")
            stopSelf()
            return START_NOT_STICKY
        }

        val negocioId = NegocioStore.negocioActivoId.value
        val negocio = if (negocioId != null) NegocioStore.buscarPorId(negocioId) else null
        val negocioNombre = negocio?.nombre

        val notificacion = AndroidNotifier.construirOperando(this, negocioNombre)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            } else 0
            startForeground(AndroidNotifier.NOTIF_ID_OPERANDO, notificacion, type)
        } else {
            startForeground(AndroidNotifier.NOTIF_ID_OPERANDO, notificacion)
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private companion object {
        const val TAG = "TokeOperandoService"
    }
}
