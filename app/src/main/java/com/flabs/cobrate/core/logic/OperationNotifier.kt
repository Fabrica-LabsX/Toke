package com.flabs.cobrate.core.logic

import android.content.Context
import android.util.Log
import com.flabs.cobrate.core.data.ConfiguracionDeVozStore
import com.flabs.cobrate.core.model.EstadoOperacion
import com.flabs.cobrate.core.model.ModoVoz
import com.flabs.cobrate.core.model.Operacion

/**
 * Orquestador de avisos al usuario (Android, Voz, Vibración) tras una operación exitosa.
 * CAPA-REALTIME paso 2.
 *
 * Centraliza la lógica que antes vivía solo en TokeNotificationListenerService
 * para que pueda ser invocada también por RealtimeSyncManager ante eventos remotos.
 */
object OperationNotifier {
    private const val TAG = "OperationNotifier"

    // Ids de notificaciones internas (CAPA 13).
    private const val NOTIF_PAGO_ASIGNADO = 1001
    private const val NOTIF_PAGO_SIN_ASIGNAR = 1002

    private var ttsSpeaker: TtsSpeaker? = null

    /**
     * Dispara los avisos correspondientes para una operación.
     * Evita duplicar lógica entre el detector local y el listener remoto.
     */
    fun notificar(context: Context, operacion: Operacion) {
        val monto = operacion.monto
        val modoCauteloso = ConfiguracionDeVozStore.configuracion.value.modo == ModoVoz.CAUTELOSO

        // 1. Notificación de Android (CAPA 13)
        when (operacion.estado) {
            EstadoOperacion.ASIGNADA -> {
                val contenido = NotificacionesInternas.notificacionPagoAsignado(monto, modoCauteloso)
                AndroidNotifier.mostrar(context, contenido.titulo, contenido.texto, NOTIF_PAGO_ASIGNADO)
            }
            EstadoOperacion.SIN_ASIGNAR -> {
                val contenido = NotificacionesInternas.notificacionPagoSinAsignar(monto, modoCauteloso)
                AndroidNotifier.mostrar(context, contenido.titulo, contenido.texto, NOTIF_PAGO_SIN_ASIGNAR)
            }
            else -> return
        }

        // 2. Voz y Vibración (CAPA 14+)
        val config = ConfiguracionDeVozStore.configuracion.value
        if (config.vibrarAlAnunciar) {
            vibrar(context)
        }
        
        if (config.activada) {
            val frase = VoicePhraseBuilder.construirFrase(config, monto)
            obtenerSpeaker(context).hablar(frase, config.velocidad)
        }
    }

    private fun obtenerSpeaker(context: Context): TtsSpeaker {
        if (ttsSpeaker == null) {
            ttsSpeaker = TtsSpeaker(context.applicationContext)
        }
        return ttsSpeaker!!
    }

    /** Libera recursos si es necesario (ej. al cerrar la app). */
    fun liberar() {
        ttsSpeaker?.liberar()
        ttsSpeaker = null
    }
}
