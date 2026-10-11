package com.flabs.cobrate.features.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.flabs.cobrate.R
import com.flabs.cobrate.core.data.ConfiguracionDeVozStore
import com.flabs.cobrate.core.logic.AndroidTtsVoiceEngine
import com.flabs.cobrate.core.model.ConfiguracionDeVoz
import com.flabs.cobrate.core.model.ModoVoz
import com.flabs.cobrate.core.model.SubModoToki

/**
 * Pantalla de configuración de voz (CAPA 14).
 *
 * Expone los controles de la [ConfiguracionDeVoz] (activación, modo, sub-modo
 * Toki, velocidad) y un botón "Probar voz" que reproduce una frase de ejemplo
 * usando el motor [AndroidTtsVoiceEngine].
 *
 * Esta capa NO conecta la voz con la confirmación real de pago: solo construye
 * el motor y la pantalla de configuración. La integración llega en una capa
 * posterior.
 */
@Composable
fun VoiceSettingsScreen(
    ttsEngine: AndroidTtsVoiceEngine = viewModel()
) {
    val config by ConfiguracionDeVozStore.configuracion.collectAsState()
    val ttsDisponible by ttsEngine.disponible.collectAsState()
    val hablando by ttsEngine.hablando.collectAsState()

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.nombre_aviso_voz),
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(16.dp))

            // ---- Activación principal ----
            ActivacionCard(
                activada = config.activada,
                onCambiarActivada = { activa ->
                    ConfiguracionDeVozStore.actualizar { it.copy(activada = activa) }
                }
            )

            if (config.activada) {
                Spacer(modifier = Modifier.height(16.dp))

                // ---- Modo de la frase ----
                ModoCard(
                    modo = config.modo,
                    onCambiarModo = { modo ->
                        // Al cambiar de modo TOKI a otro se limpiar el sub-modo.
                        val subModo = if (modo == ModoVoz.TOKI) config.subModoToki ?: SubModoToki.DISCRETO else null
                        ConfiguracionDeVozStore.actualizar {
                            it.copy(modo = modo, subModoToki = subModo)
                        }
                    }
                )

                // ---- Sub-modo Toki (solo si modo == TOKI) ----
                if (config.modo == ModoVoz.TOKI) {
                    Spacer(modifier = Modifier.height(12.dp))
                    SubModoTokiCard(
                        subModo = config.subModoToki ?: SubModoToki.DISCRETO,
                        onCambiarSubModo = { sub ->
                            ConfiguracionDeVozStore.actualizar { it.copy(subModoToki = sub) }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ---- Velocidad ----
                VelocidadCard(
                    velocidad = config.velocidad,
                    onCambiarVelocidad = { v ->
                        ConfiguracionDeVozStore.actualizar {
                            it.copy(velocidad = ConfiguracionDeVoz.velocidadNormalizada(v))
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // ---- Probar voz ----
                Button(
                    onClick = { ttsEngine.hablar(ttsEngine.fraseDePrueba()) },
                    enabled = ttsDisponible && !hablando,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (hablando) "Reproduciendo…" else "Probar voz")
                }
                if (!ttsDisponible) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "El motor de voz no está disponible en este dispositivo.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun ActivacionCard(
    activada: Boolean,
    onCambiarActivada: (Boolean) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Confirmación de pagos en voz alta",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Anuncia en voz alta cuando un cliente confirma un pago.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Switch(
                checked = activada,
                onCheckedChange = onCambiarActivada
            )
        }
    }
}

@Composable
private fun ModoCard(
    modo: ModoVoz,
    onCambiarModo: (ModoVoz) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "Modo de la frase", style = MaterialTheme.typography.titleMedium)
            Text(
                text = "Cómo se anunciará el pago confirmado.",
                style = MaterialTheme.typography.bodySmall
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = modo == ModoVoz.CAUTELOSO,
                    onClick = { onCambiarModo(ModoVoz.CAUTELOSO) },
                    label = { Text("Cauteloso") }
                )
                FilterChip(
                    selected = modo == ModoVoz.NEGOCIO,
                    onClick = { onCambiarModo(ModoVoz.NEGOCIO) },
                    label = { Text("Negocio") }
                )
                FilterChip(
                    selected = modo == ModoVoz.TOKI,
                    onClick = { onCambiarModo(ModoVoz.TOKI) },
                    label = { Text("Toke") }
                )
            }
        }
    }
}

@Composable
private fun SubModoTokiCard(
    subModo: SubModoToki,
    onCambiarSubModo: (SubModoToki) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "Estilo Toke", style = MaterialTheme.typography.titleMedium)
            Text(
                text = "¿Incluir el monto en la frase?",
                style = MaterialTheme.typography.bodySmall
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = subModo == SubModoToki.DISCRETO,
                    onClick = { onCambiarSubModo(SubModoToki.DISCRETO) },
                    label = { Text("Discreto") }
                )
                FilterChip(
                    selected = subModo == SubModoToki.NEGOCIO,
                    onClick = { onCambiarSubModo(SubModoToki.NEGOCIO) },
                    label = { Text("Con monto") }
                )
            }
        }
    }
}

@Composable
private fun VelocidadCard(
    velocidad: Float,
    onCambiarVelocidad: (Float) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Velocidad", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "%.2fx".format(velocidad),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Slider(
                value = velocidad,
                onValueChange = onCambiarVelocidad,
                valueRange = 0.5f..2.0f,
                steps = 5
            )
        }
    }
}