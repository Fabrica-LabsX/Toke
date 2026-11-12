package com.flabs.cobrate.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flabs.cobrate.core.logic.DuracionAviso
import com.flabs.cobrate.core.logic.FeedbackManager
import com.flabs.cobrate.core.logic.FeedbackState
import com.flabs.cobrate.core.logic.FeedbackType
import com.flabs.cobrate.ui.theme.PlusJakartaSans
import com.flabs.cobrate.ui.theme.TokePaleta
import com.flabs.cobrate.ui.theme.tokePaleta
import kotlinx.coroutines.delay

/**
 * Avisos globales. Los mensajes cortos salen como una tarjeta **arriba** (nunca tapan la barra de
 * navegación ni el botón de abajo), se van solos y no piden pulsar "OK". Tocarla la cierra.
 * Los estados persistentes (cargando, sin conexión) usan la misma paleta.
 */
@Composable
fun FeedbackOverlay() {
    val feedback by FeedbackManager.currentFeedback.collectAsState()
    val paleta = tokePaleta()

    // Último aviso temporal: se conserva mientras sale para que no parpadee vacío.
    var temporal by remember { mutableStateOf<FeedbackState?>(null) }
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(feedback) {
        val actual = feedback ?: return@LaunchedEffect
        if (actual.durationMillis == null) return@LaunchedEffect
        temporal = actual
        visible = true
        delay(DuracionAviso.milis(actual.message, actual.durationMillis))
        visible = false
        delay(200)
        FeedbackManager.clear()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        temporal?.let { aviso ->
            AnimatedVisibility(
                visible = visible,
                modifier = Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(horizontal = 16.dp, vertical = 10.dp),
                enter = fadeIn(tween(160)) + slideInVertically(tween(180)) { -it / 2 },
                exit = fadeOut(tween(120)) + slideOutVertically(tween(140)) { -it / 2 },
            ) {
                TarjetaAviso(paleta, aviso) {
                    visible = false
                    FeedbackManager.clear()
                }
            }
        }

        feedback?.let { state ->
            if (state.durationMillis == null) {
                when (state.type) {
                    FeedbackType.LOADING -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.35f))
                                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { },
                            contentAlignment = Alignment.Center,
                        ) {
                            Surface(shape = RoundedCornerShape(20.dp), color = paleta.tarjeta, border = BorderStroke(1.dp, paleta.borde)) {
                                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                    CircularProgressIndicator(color = paleta.verde)
                                    Text(state.message, style = estiloAviso(15, FontWeight.SemiBold), color = paleta.textoPrincipal)
                                }
                            }
                        }
                    }
                    FeedbackType.OFFLINE -> {
                        Surface(modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter), color = paleta.rojo) {
                            Row(
                                modifier = Modifier.statusBarsPadding().padding(10.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(state.message, style = estiloAviso(13, FontWeight.Bold), color = Color.White)
                            }
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}

private fun estiloAviso(tam: Int, peso: FontWeight) = TextStyle(fontFamily = PlusJakartaSans, fontWeight = peso, fontSize = tam.sp, lineHeight = (tam + 5).sp)

@Composable
private fun TarjetaAviso(paleta: TokePaleta, aviso: FeedbackState, onCerrar: () -> Unit) {
    val (fondo, texto, borde) = when (aviso.type) {
        FeedbackType.SUCCESS -> Triple(paleta.verde, paleta.verdeTexto, paleta.verde)
        FeedbackType.ERROR -> Triple(paleta.rojo, Color.White, paleta.rojo)
        FeedbackType.WARNING -> Triple(paleta.ambar, paleta.ambarTexto, paleta.ambar)
        else -> Triple(paleta.tarjeta, paleta.textoPrincipal, paleta.bordeControl)
    }
    Surface(
        onClick = onCerrar,
        shape = RoundedCornerShape(16.dp),
        color = fondo,
        border = BorderStroke(1.dp, borde),
        shadowElevation = 6.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = aviso.message,
                style = estiloAviso(14, FontWeight.SemiBold),
                color = texto,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            if (aviso.actionLabel != null && aviso.action != null) {
                Text(
                    text = aviso.actionLabel,
                    style = estiloAviso(14, FontWeight.ExtraBold),
                    color = texto,
                    modifier = Modifier.clickable { aviso.action.invoke(); onCerrar() }.padding(4.dp),
                )
            }
        }
    }
}
