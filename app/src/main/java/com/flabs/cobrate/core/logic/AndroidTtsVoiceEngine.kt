package com.flabs.cobrate.core.logic

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.flabs.cobrate.core.data.ConfiguracionDeVozStore
import kotlinx.coroutines.flow.StateFlow

/**
 * Motor de voz (CAPA 14) que reproduce frases vía [TtsSpeaker].
 *
 * Es un [AndroidViewModel] con el [Application] como context, pensado para la
 * pantalla de configuración de voz
 * ([com.flabs.cobrate.features.settings.VoiceSettingsScreen]).
 *
 * La lógica central de TextToSpeech NO vive aquí: se delega a [TtsSpeaker],
 * una clase simple sin dependencia de ViewModel que también puede usar un
 * [android.app.Service] (que no puede instanciar ViewModels fuera de su ciclo
 * de vida). Este ViewModel solo expone esa instancia y la libera en [onCleared].
 *
 * Esta capa SOLO construye el motor y alimenta el botón "Probar voz" de la
 * pantalla de configuración; la integración real en el flujo de confirmación
 * de pago usa [TtsSpeaker] directamente desde el servicio.
 */
class AndroidTtsVoiceEngine(application: Application) : AndroidViewModel(application) {

    /** Speaker subyacente (comparte forma y lógica con el del servicio). */
    private val speaker = TtsSpeaker(application.applicationContext)

    /** ¿El TTS está inicializado y listo para hablar? (reativo, delegado). */
    val disponible: StateFlow<Boolean> = speaker.disponible

    /** ¿Hay una frase en reproducción? (reativo, delegado). */
    val hablando: StateFlow<Boolean> = speaker.hablando

    /**
     * Reproduce en voz alta la [frase] usando la configuración vigente de
     * [ConfiguracionDeVozStore] (velocidad y, si está seleccionada, la voz).
     *
     * No hace nada si la configuración está desactivada o el TTS no está listo.
     */
    fun hablar(frase: String) {
        val config = ConfiguracionDeVozStore.configuracion.value
        if (!config.activada) return
        speaker.hablar(frase, config.velocidad)
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
        speaker.liberar()
        super.onCleared()
    }
}