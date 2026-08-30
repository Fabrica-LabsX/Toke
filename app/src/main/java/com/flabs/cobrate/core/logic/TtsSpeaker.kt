package com.flabs.cobrate.core.logic

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.flabs.cobrate.core.data.ConfiguracionDeVozStore
import com.flabs.cobrate.core.model.ConfiguracionDeVoz
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

/**
 * Hablador de voz (CAPA 14): envuelve [TextToSpeech] de Android en una clase
 * simple SIN dependencia de ViewModel, para poder usarla tanto desde la UI
 * (vía [AndroidTtsVoiceEngine]) como desde un [android.app.Service]
 * (p. ej. [com.flabs.cobrate.services.TokeNotificationListenerService]), que
 * no pueden instanciar ViewModels fuera de su ciclo de vida.
 *
 * Recibe un [Context] en el constructor (el llamador decide si usa
 * applicationContext). El [TextToSpeech] se inicializa de forma asíncrona (su
 * callback NO bloquea); la disponibilidad real queda expuesta en
 * [disponible].
 */
class TtsSpeaker(context: Context) {

    private val _disponible = MutableStateFlow(false)
    /** ¿El TTS está inicializado y listo para hablar? */
    val disponible: StateFlow<Boolean> = _disponible

    private val _hablando = MutableStateFlow(false)
    /** ¿Hay una frase en reproducción? */
    val hablando: StateFlow<Boolean> = _hablando

    private var tts: TextToSpeech? = null

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val engine = tts ?: return@TextToSpeech
                val resultado = engine.setLanguage(Locale.getDefault())
                // LANG_MISSING_DATA / LANG_NOT_SUPPORTED no bloquean la
                // reproducción: no se marcan como no disponible, solo se
                // informa el estado real.
                _hablando.value = false
                _disponible.value =
                    resultado != TextToSpeech.LANG_MISSING_DATA &&
                    resultado != TextToSpeech.LANG_NOT_SUPPORTED
            } else {
                _disponible.value = false
            }
        }
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _hablando.value = true
            }

            override fun onDone(utteranceId: String?) {
                _hablando.value = false
            }

            @Deprecated("Deprecated en versiones recientes de API")
            override fun onError(utteranceId: String?) {
                _hablando.value = false
            }
        })
    }

    /**
     * Reproduce [texto] en voz alta a la [velocidad] indicada.
     *
     * Usa la voz seleccionada en [ConfiguracionDeVozStore] si hay una que no
     * sea la del sistema por defecto. No hace nada si el TTS no está listo
     * (en ese caso la llamada se ignora silenciosamente, igual que en la UI).
     *
     * @param velocidad velocidad de habla (1.0f = normal).
     */
    fun hablar(texto: String, velocidad: Float) {
        val engine = tts ?: return
        if (!_disponible.value) return

        engine.setSpeechRate(ConfiguracionDeVoz.velocidadNormalizada(velocidad))

        // Aplica la voz seleccionada si no es la del sistema por defecto.
        ConfiguracionDeVozStore.configuracion.value.vozSeleccionada?.let { voz ->
            val soportada = engine
                .voices
                ?.firstOrNull { it.name == voz || it.locale.toLanguageTag() == voz }
            if (soportada != null) {
                engine.voice = soportada
            }
        }

        engine.speak(texto, TextToSpeech.QUEUE_FLUSH, null, "cap14_voz_pago")
    }

    /** Libera los recursos del TTS (detiene y apaga el motor). */
    fun liberar() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}