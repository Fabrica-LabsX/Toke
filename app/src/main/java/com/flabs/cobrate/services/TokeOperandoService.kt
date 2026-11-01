package com.flabs.cobrate.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import com.flabs.cobrate.core.data.CuentaStore
import com.flabs.cobrate.core.data.EstadoOperativoStore
import com.flabs.cobrate.core.data.NegocioStore
import com.flabs.cobrate.core.data.RelacionCuentaNegocioStore
import com.flabs.cobrate.core.data.SupabaseAuthBridge
import com.flabs.cobrate.core.logic.AndroidNotifier
import com.flabs.cobrate.core.logic.LogSeguro
import com.flabs.cobrate.core.logic.PermisosChecker
import com.flabs.cobrate.core.model.EstadoRelacion

/**
 * Servicio en primer plano que sostiene la notificación permanente "TOKE está operando" (CAPA 58).
 * Solo opera cuando la sesión es válida, la autenticación ha finalizado, el acceso a notificaciones está concedido y hay un negocio elegible.
 */
class TokeOperandoService : Service() {

    override fun onCreate() {
        super.onCreate()
        EstadoOperativoStore.init(this)
        NegocioStore.init(this)
        RelacionCuentaNegocioStore.init(this)
        CuentaStore.init(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        EstadoOperativoStore.init(this)

        // 1. Promoción INMEDIATA e incondicional a primer plano con notificación transitoria para cumplir el contrato de Android OS
        val notifTransitoria = AndroidNotifier.construirComprobando(this)
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(
                    AndroidNotifier.NOTIF_ID_OPERANDO,
                    notifTransitoria,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } else {
                startForeground(AndroidNotifier.NOTIF_ID_OPERANDO, notifTransitoria)
            }
        }.onFailure { err ->
            LogSeguro.w(TAG, "Excepción al promocionar servicio a primer plano", err)
            stopSelf()
            return START_NOT_STICKY
        }

        // 2. Evaluar la elegibilidad operativa compartida
        if (!esElegibleParaOperar(this)) {
            LogSeguro.d(TAG, "Condiciones de operación no cumplidas. Deteniendo servicio.")
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        // 3. Requisitos cumplidos: Actualizar a notificación operativa real con el nombre del negocio
        val negocioId = NegocioStore.negocioActivoId.value
        val negocio = if (negocioId != null) NegocioStore.buscarPorId(negocioId) else null
        val notifOperativa = AndroidNotifier.construirOperando(this, negocio?.nombre)

        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(
                    AndroidNotifier.NOTIF_ID_OPERANDO,
                    notifOperativa,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } else {
                startForeground(AndroidNotifier.NOTIF_ID_OPERANDO, notifOperativa)
            }
        }.onFailure { err ->
            LogSeguro.w(TAG, "Excepción actualizando notificación a estado operativo", err)
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val TAG = "TokeOperandoService"

        /**
         * Fuente de verdad compartida para validar si la aplicación cumple todos los requisitos
         * técnicos y de dominio para estar en estado realmente operativo.
         */
        fun esElegibleParaOperar(context: Context): Boolean {
            val appContext = context.applicationContext
            EstadoOperativoStore.init(appContext)

            // a) TOKE está encendido según EstadoOperativoStore
            if (!EstadoOperativoStore.encendido.value) return false

            // b) Sesión auténtica disponible y Auth ya no está inicializando
            if (SupabaseAuthBridge.estaInicializando.value) return false
            val sesion = CuentaStore.sesionActual.value ?: return false
            val uid = sesion.id

            // c) Acceso a notificaciones (NotificationListener) concedido
            if (!PermisosChecker.accesoNotificacionesConcedido(appContext)) return false

            // d) Negocio activo y elegible resuelto (Dueño o Trabajador Activo)
            val negocioId = NegocioStore.negocioActivoId.value ?: return false
            val negocio = NegocioStore.buscarPorId(negocioId) ?: return false

            val esDueno = negocio.dueñoCuentaId == uid
            val rel = RelacionCuentaNegocioStore.relaciones.value.find {
                it.cuentaId == uid && it.negocioId == negocioId
            }
            val esTrabajadorActivo = rel?.estado == EstadoRelacion.ACTIVA

            return esDueno || esTrabajadorActivo
        }
    }
}
