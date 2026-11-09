package com.flabs.cobrate.debug

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.flabs.cobrate.core.data.PreferenciasAppStore
import com.flabs.cobrate.features.auth.AuthViewModel
import com.flabs.cobrate.features.auth.BienvenidaScreen
import com.flabs.cobrate.features.auth.LoginScreen
import com.flabs.cobrate.features.soporte.AcercaDeScreen
import com.flabs.cobrate.ui.theme.CobrateTheme

/**
 * SOLO DEBUG. Abre una pantalla sin iniciar sesión para revisarla en el celular:
 *   adb shell am start -n com.flabs.cobrate/.debug.VistaPreviaActivity --es pantalla bienvenida|login|registro|acerca
 */
class VistaPreviaActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PreferenciasAppStore.init(applicationContext)
        val pantalla = intent.getStringExtra("pantalla") ?: "bienvenida"
        setContent {
            val modo by PreferenciasAppStore.themeMode.collectAsState()
            CobrateTheme(themeMode = modo) {
                when (pantalla) {
                    "login" -> LoginScreen(viewModel<AuthViewModel>(), modoInicialCrear = false)
                    "registro" -> LoginScreen(viewModel<AuthViewModel>(), modoInicialCrear = true)
                    "acerca" -> AcercaDeScreen(onVolver = { finish() })
                    "manual" -> com.flabs.cobrate.features.soporte.ManualScreen(onVolver = { finish() })
                    "preguntas" -> com.flabs.cobrate.features.soporte.PreguntasFrecuentesScreen(onVolver = { finish() })
                    "codigo" -> com.flabs.cobrate.features.onboarding.IngresarCodigoScreen(onCodigoValido = {}, onVolver = { finish() })
                    else -> BienvenidaScreen({}, {}, {})
                }
            }
        }
    }
}
