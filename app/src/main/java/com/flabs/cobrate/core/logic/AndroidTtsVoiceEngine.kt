package com.flabs.cobrate.core.logic

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.lifecycle.AndroidViewModel
import com.flabs.cobrate.core.data.ConfiguracionDeVozStore
import com.flabs.cobrate.core.model.ConfiguracionDeVoz
import com.flabs.cobrate.core.model.ModoVoz
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

/**
 * Motor de voz (CAPA 14) que reproduce frases vía [TextToSpeech] de Android.
 *
 * Es un [AndroidViewModel] con el [android.app.Application] como context, lo
 * que permite crear el [TextToSpeech] de forma asíncrona (el callback
 * `TextToSpeech(context) { status -> ... }` NO bloquea la UI) y liberarlo con
 * [TextToSpeech.shutdown] en [onCleared].
 *
 * Esta capa SOLO construye el motor y alimenta el botón "Probar voz" de la
 * pantalla de configuración. La integración real (disparar la voz al
 * confirmarse un pago) llega en una capa posterior.
 */
class AndroidTtsVoiceEngine(application: android.app.Application) :
    AndroidViewModel(application) {

    private val _disponible = MutableStateFlow(false)
    /** ¿El TTS está inicializado y listo para hablar? */
    val disponible: StateFlow<Boolean> = _disponible

    private val _hablando = MutableStateFlow(false)
    /** ¿Hay una frase en reproducción? (para p. ej. deshabilitar el botón). */
    val hablando: StateFlow<Boolean> = _hablando

    private var tts: TextToSpeech? = null

    init {
        val context: Context = application.applicationContext
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val engine = tts ?: return@TextToSpeech
                val resultado = engine.setLanguage(Locale.getDefault())
                // El resultado puede ser LANG_MISSING_DATA o LANG_NOT_SUPPORTED:
                // no bloquea la reproducción, seguimos igualmente.
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

            @Deprecated("Deprecated en versions recientes de API")
            override fun onError(utteranceId: String?) {
                _hablando.value = false
            }
        })
    }

    /**
     * Reproduce en voz alta la [frase] usando la configuración vigente de
     * [ConfiguracionDeVozStore] (velocidad y, si está seleccionada, la voz).
     *
     * No hace nada si la configuración está desactivada o el TTS no está listo.
     */
    fun hablar(frase: String) {
        val config = ConfiguracionDeVozStore.configuracion.value
        if (!config.activada) return

        val engine = tts ?: return
        if (!_disponible.value) return

        engine.setSpeechRate(
            ConfiguracionDeVoz.velocidadNormalizada(config.velocidad)
        )

        // Aplica la voz seleccionada si no es la del sistema por defecto.
        config.vozSeleccionada?.let { voz ->
            val soportada = engine
                .voices
                ?.firstOrNull { it.name == voz || it.locale.toLanguageTag() == voz }
            if (soportada != null) {
                engine.voice = soportada
            }
        }

        engine.speak(
            frase,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "cap14_prueba_voz"
        )
    }

    /** Frase de ejemplo usada por el botón "Probar voz". */
    fun fraseDePrueba(): String =
        VoicePhraseBuilder.construirFrase(
            config = ConfiguracionDeVozStore.configuracion.value,
            monto = 25.50,
            cliente = null
        )

    /** Libera los recursos del TTS. Se llama automáticamente al morir el ViewModel. */
    override fun onCleared() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        super.onCleared()
    }
}