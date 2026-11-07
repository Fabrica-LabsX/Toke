package com.flabs.cobrate.features.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.flabs.cobrate.R
import com.flabs.cobrate.components.TokeIcons
import com.flabs.cobrate.core.data.ConfiguracionDeVozStore
import com.flabs.cobrate.core.logic.AndroidTtsVoiceEngine
import com.flabs.cobrate.core.model.ConfiguracionDeVoz
import com.flabs.cobrate.core.model.ModoVoz
import com.flabs.cobrate.features.acceso.BotonAcceso
import com.flabs.cobrate.ui.theme.PlusJakartaSans
import com.flabs.cobrate.ui.theme.TokeMedidas
import com.flabs.cobrate.ui.theme.TokePaleta
import com.flabs.cobrate.ui.theme.tokePaleta

/**
 * Aviso de voz: se activa o se apaga, se elige qué tanto dice (Cauteloso o Básico), la
 * velocidad, y se puede probar con una frase de ejemplo.
 */
@Composable
fun VoiceSettingsScreen(
    ttsEngine: AndroidTtsVoiceEngine = viewModel()
) {
    val paleta = tokePaleta()
    val config by ConfiguracionDeVozStore.configuracion.collectAsState()
    val ttsDisponible by ttsEngine.disponible.collectAsState()
    val hablando by ttsEngine.hablando.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(paleta.fondo)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = TokeMedidas.MargenLateral, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(TokeMedidas.EntreGrupos),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(14.dp), color = paleta.tileFondo, modifier = Modifier.size(48.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(id = TokeIcons.VolumenAlto),
                        contentDescription = null,
                        tint = paleta.textoPrincipal,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = stringResource(R.string.nombre_aviso_voz),
                style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp),
                color = paleta.textoPrincipal,
            )
        }

        TarjetaActivacion(paleta, config.activada) { activa ->
            ConfiguracionDeVozStore.actualizar { it.copy(activada = activa) }
        }

        if (config.activada) {
            Text(
                text = "QUÉ TANTO DICE",
                style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 0.8.sp),
                color = paleta.textoSecundario,
                modifier = Modifier.padding(start = 4.dp, top = 6.dp),
            )
            ModoVoz.entries.forEach { modo ->
                TarjetaModo(paleta, modo, elegido = config.modo == modo) {
                    ConfiguracionDeVozStore.actualizar { it.copy(modo = modo) }
                }
            }

            TarjetaVelocidad(paleta, config.velocidad) { v ->
                ConfiguracionDeVozStore.actualizar { it.copy(velocidad = ConfiguracionDeVoz.velocidadNormalizada(v)) }
            }

            BotonAcceso(
                paleta = paleta,
                texto = if (hablando) "Reproduciendo…" else "Probar voz",
                principal = ttsDisponible && !hablando,
            ) {
                if (ttsDisponible && !hablando) ttsEngine.hablar(ttsEngine.fraseDePrueba())
            }
            if (!ttsDisponible) {
                Text(
                    text = "El motor de voz no está disponible en este dispositivo.",
                    style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Medium, fontSize = 13.sp),
                    color = paleta.rojo,
                )
            }
        }
    }
}

@Composable
private fun TarjetaActivacion(paleta: TokePaleta, activada: Boolean, onCambia: (Boolean) -> Unit) {
    Surface(
        onClick = { onCambia(!activada) },
        shape = RoundedCornerShape(20.dp),
        color = paleta.tarjeta,
        border = BorderStroke(1.dp, paleta.borde),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(modifier = Modifier.padding(TokeMedidas.PaddingTarjeta), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Avisar los pagos en voz alta",
                    style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 16.sp),
                    color = paleta.textoPrincipal,
                )
                Text(
                    text = "Cuando llega un pago, el celular lo dice.",
                    style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Medium, fontSize = 13.sp),
                    color = paleta.textoSecundario,
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Switch(
                checked = activada,
                onCheckedChange = onCambia,
                colors = SwitchDefaults.colors(
                    checkedTrackColor = paleta.verde,
                    checkedThumbColor = paleta.verdeTexto,
                    uncheckedTrackColor = paleta.bordeControl,
                    uncheckedBorderColor = paleta.bordeControl,
                    uncheckedThumbColor = paleta.tarjeta,
                ),
            )
        }
    }
}

@Composable
private fun TarjetaModo(paleta: TokePaleta, modo: ModoVoz, elegido: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = paleta.tarjeta,
        border = BorderStroke(if (elegido) 2.dp else 1.dp, if (elegido) paleta.verde else paleta.borde),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(modifier = Modifier.padding(TokeMedidas.PaddingTarjeta), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = modo.titulo,
                    style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp),
                    color = paleta.textoPrincipal,
                )
                Text(
                    text = modo.explicacion,
                    style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
                    color = paleta.textoSecundario,
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Icon(
                painter = painterResource(id = if (elegido) TokeIcons.RadioMarcado else TokeIcons.RadioVacio),
                contentDescription = if (elegido) "Elegido" else null,
                tint = if (elegido) paleta.verde else paleta.bordeControl,
                modifier = Modifier.size(26.dp),
            )
        }
    }
}

@Composable
private fun TarjetaVelocidad(paleta: TokePaleta, velocidad: Float, onCambia: (Float) -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = paleta.tarjeta,
        border = BorderStroke(1.dp, paleta.borde),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(TokeMedidas.PaddingTarjeta)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Velocidad de la voz",
                    style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 16.sp),
                    color = paleta.textoPrincipal,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "%.2fx".format(velocidad),
                    style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 15.sp),
                    color = paleta.enlaceVerde,
                )
            }
            Slider(
                value = velocidad,
                onValueChange = onCambia,
                valueRange = 0.5f..2.0f,
                steps = 5,
                colors = SliderDefaults.colors(
                    thumbColor = paleta.verde,
                    activeTrackColor = paleta.verde,
                    inactiveTrackColor = paleta.bordeControl,
                ),
            )
        }
    }
}
