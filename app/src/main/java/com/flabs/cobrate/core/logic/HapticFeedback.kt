package com.flabs.cobrate.core.logic

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Componente de retroalimentación háptica (CAPA 15).
 *
 * Provee un único punto para hacer vibrar el dispositivo con un patrón corto y
 * no invasivo. En V1 no se construye un sistema complejo de vibraciones: solo
 * existe esta función simple, lista para ser invocada cuando un evento lo
 * requiera (por ejemplo, al confirmar un pago o anunciar una frase por voz en
 * una integración posterior).
 *
 * Escoge la API según el nivel de API:
 *  - API 31+ (Android 12+): usa [VibratorManager] vía [Context.VIBRATOR_MANAGER_SERVICE].
 *  - API < 31: usa el [Vibrator] clásico vía [Context.VIBRATOR_SERVICE].
 *
 * El proyecto tiene minSdk = 24, por lo que el fallback a la API antigua es
 * necesario.
 */

private const val DURACION_VIBRACION_MS = 200L

/**
 * Hace vibrar el dispositivo con un patrón corto de 200 ms.
 *
 * Es una operación "fire and forget": si el dispositivo no tiene vibrador o no
 * lo soporta, no hace nada y no lanza excepción.
 */
fun vibrar(context: Context) {
    val vibrator = obtainVibrator(context) ?: return
    if (!vibrator.hasVibrator()) return

    // API 26+ (minSdk es 24, así que el efecto por duración está disponible en
    // la mayoría de los dispositivos; en API 24-25 no hay VibrationEffect y se
    // cae a vibrate(ms) tradicional).
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(
            VibrationEffect.createOneShot(
                DURACION_VIBRACION_MS,
                VibrationEffect.DEFAULT_AMPLITUDE
            )
        )
    } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(DURACION_VIBRACION_MS)
    }
}

/**
 * Obtiene el [Vibrator] del sistema, usando la API moderna en API 31+ y la
 * clásica en versiones anteriores. Devuelve `null` si el servicio no está
 * disponible.
 */
private fun obtainVibrator(context: Context): Vibrator? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(VibratorManager::class.java) ?: return null
        manager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }
}