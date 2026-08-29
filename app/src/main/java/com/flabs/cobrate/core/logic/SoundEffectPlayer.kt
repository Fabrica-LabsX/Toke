package com.flabs.cobrate.core.logic

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool

/**
 * Componente de reproducción de efectos de sonido (CAPA 15).
 *
 * Provee un único punto para reproducir efectos cortos usando [SoundPool], que
 * es la opción recomendada para clips breves (a diferencia de [android.media.MediaPlayer],
 * orientado a reproducción más larga).
 *
 * NOTA DE ASSETS: esta tarea NO agrega archivos de audio al proyecto. La función
 * [reproducir] está lista para recibir un `resourceId` de un recurso raw
 * (R.raw.xxx). Los archivos .mp3/.ogg reales se agregan en una tarea de assets
 * aparte; hasta entonces no debe invocarse con ids que aún no existen.
 *
 * En V1 la spec pide explícitamente no construir un sistema excesivamente
 * complejo de paquetes de sonidos, por lo que solo hay una función simple.
 */

private const val MAX_STREAMS = 1

private var soundPool: SoundPool? = null

/**
 * Reproduce el efecto de sonido identificado por [resourceId] si está cargado.
 *
 * [SoundPool.load] es asíncrono: se registra un listener que reproduce el clip
 * en cuanto termina de cargar. Es una operación "fire and forget": no lanza
 * excepción si el efecto falla.
 */
fun reproducir(context: Context, resourceId: Int) {
    val pool = getOrCreateSoundPool()
    val sampleId = pool.load(context, resourceId, 1)
    if (sampleId == 0) return

    pool.setOnLoadCompleteListener { _, loadedId, status ->
        if ((loadedId == sampleId) && (status == 0)) {
            pool.play(sampleId, 1f, 1f, 1, 0, 1f)
        }
    }
}

private fun getOrCreateSoundPool(): SoundPool {
    soundPool?.let { return it }

    val attributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

    return SoundPool.Builder()
        .setMaxStreams(MAX_STREAMS)
        .setAudioAttributes(attributes)
        .build()
        .also { soundPool = it }
}