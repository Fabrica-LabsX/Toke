package com.flabs.cobrate.services

import android.content.Intent
import androidx.core.content.ContextCompat
import com.flabs.cobrate.R
import com.flabs.cobrate.core.data.CuentaStore
import com.flabs.cobrate.core.data.DispositivoRepository
import com.flabs.cobrate.core.data.EstadoOperativoStore
import com.flabs.cobrate.core.data.InMemoryOperationStore
import com.flabs.cobrate.core.data.OperacionRepository
import com.flabs.cobrate.core.data.RelacionCuentaNegocioRepository
import com.flabs.cobrate.core.data.RelacionCuentaNegocioStore
import com.flabs.cobrate.core.logic.AndroidNotifier
import com.flabs.cobrate.core.logic.LogSeguro
import com.flabs.cobrate.core.logic.OperationNotifier
import com.flabs.cobrate.core.logic.RegistroAvisos
import com.flabs.cobrate.core.logic.RegistroDispositivos
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Servicio de mensajes push de Firebase para TOKE (CAPA 51, 52 y 53).
 */
class TokeMessagingService : FirebaseMessagingService() {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(serviceJob + Dispatchers.IO)
    private val dispositivoRepo = DispositivoRepository()
    private val operacionRepo = OperacionRepository()
    private val relacionRepo = RelacionCuentaNegocioRepository()

    override fun onCreate() {
        super.onCreate()
        EstadoOperativoStore.init(this)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        LogSeguro.d(TAG, "Nuevo token FCM recibido: ${token.take(6)}...")
        val sesion = CuentaStore.sesionActual.value
        if (sesion != null) {
            serviceScope.launch {
                RegistroDispositivos.registrar(FirebaseProveedorDeToken(), dispositivoRepo)
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        EstadoOperativoStore.init(this)
        val tokeOperativo = EstadoOperativoStore.encendido.value

        // Si TOKE es elegible para operar, aseguramos/iniciamos el servicio operativo foreground
        if (TokeOperandoService.esElegibleParaOperar(applicationContext)) {
            runCatching {
                val intent = Intent(applicationContext, TokeOperandoService::class.java)
                ContextCompat.startForegroundService(applicationContext, intent)
            }
        } else {
            LogSeguro.d(TAG, "Mensaje push recibido sin elegibilidad operativa. Se procesará la notificación funcional sin activar servicio de primer plano.")
        }

        val data = remoteMessage.data
        if (data.isEmpty()) return

        val tipo = data["tipo"] ?: return
        val sesion = CuentaStore.sesionActual.value ?: return

        LogSeguro.d(TAG, "Mensaje push recibido. Tipo: $tipo (operativo=$tokeOperativo)")

        serviceScope.launch {
            when (tipo) {
                "cobro", "asignado" -> {
                    val operacionId = data["operacion_id"] ?: return@launch
                    if (RegistroAvisos.yaNotificadoOAgregar(operacionId)) {
                        LogSeguro.d(TAG, "Cobro $operacionId ya fue notificado previamente. Ignorando push.")
                        return@launch
                    }

                    operacionRepo.obtenerPorId(operacionId).onSuccess { operacion ->
                        if (operacion != null) {
                            InMemoryOperationStore.aplicarCambioRemotoRealtime(operacion)
                            OperationNotifier.notificar(applicationContext, operacion)
                        }
                    }
                }

                "solicitud" -> {
                    val cuentaId = data["cuenta_id"] ?: ""
                    val negocioId = data["negocio_id"] ?: ""

                    val titulo = getString(R.string.push_solicitud_titulo)
                    val texto = getString(R.string.push_solicitud_texto)

                    AndroidNotifier.mostrarAvisoSimple(
                        context = applicationContext,
                        tag = "solicitud-$cuentaId",
                        titulo = titulo,
                        texto = texto
                    )

                    if (negocioId.isNotEmpty()) {
                        relacionRepo.relacionesDelNegocio(negocioId).onSuccess { relaciones ->
                            RelacionCuentaNegocioStore.sincronizarNegocio(negocioId, relaciones)
                        }
                    }
                }

                "respuesta_solicitud" -> {
                    val estado = data["estado"]
                    val negocioId = data["negocio_id"] ?: ""

                    if (estado == "ACTIVA") {
                        AndroidNotifier.mostrarAvisoSimple(
                            context = applicationContext,
                            tag = "respuesta_solicitud-$negocioId",
                            titulo = getString(R.string.push_aceptada_titulo),
                            texto = getString(R.string.push_aceptada_texto)
                        )
                    } else if (estado == "RECHAZADA") {
                        AndroidNotifier.mostrarAvisoSimple(
                            context = applicationContext,
                            tag = "respuesta_solicitud-$negocioId",
                            titulo = getString(R.string.push_rechazada_titulo),
                            texto = getString(R.string.push_rechazada_texto)
                        )
                    }

                    val uid = sesion.id
                    relacionRepo.relacionesDe(uid).onSuccess { relaciones ->
                        RelacionCuentaNegocioStore.sincronizar(uid, relaciones)
                    }
                }

                else -> {
                    LogSeguro.d(TAG, "Tipo de mensaje push no reconocido: $tipo")
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }

    private companion object {
        const val TAG = "TokeMessagingService"
    }
}
