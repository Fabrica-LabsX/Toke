package com.flabs.cobrate.core.logic

import android.content.Context
import android.os.PowerManager
import androidx.core.app.NotificationManagerCompat

/**
 * Consultas de solo lectura sobre el estado REAL de permisos del sistema
 * Android (no simulado). CAPA 5 — diagnóstico.
 *
 * Ninguna de estas funciones solicita permisos; solo verifican si ya están
 * concedidos. La solicitud/gestión de permisos vive en la capa de UI.
 */
object PermisosChecker {

    /**
     * ¿La app está habilitada como "notification listener"?
     *
     * Android no permite conceder este acceso mediante un diálogo normal; el
     * usuario debe habilitarlo manualmente en Ajustes. Aquí solo leemos si
     * nuestro paquete figura en la lista de listeners habilitados.
     */
    fun accesoNotificacionesConcedido(context: Context): Boolean {
        val paquetesHabilitados = NotificationManagerCompat.getEnabledListenerPackages(context)
        return paquetesHabilitados.contains(context.packageName)
    }

    /**
     * ¿La app está exenta de las optimizaciones de batería (modo "Sin
     * restricciones" en batería)?
     *
     * Solo lectura. Se trata como una recomendación no bloqueante: no
     * pretendemos obligar a desactivar todas las optimizaciones de forma
     * indiscriminada.
     */
    fun segundoPlanoSinRestricciones(context: Context): Boolean {
        val powerManager =
            context.getSystemService(Context.POWER_SERVICE) as? PowerManager ?: return false
        return powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }
}