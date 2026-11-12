package com.flabs.cobrate.ui.theme

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.delay

/** Cuánto espera un elemento según qué tan abajo está: arriba sale primero, abajo después (cascada natural). */
object CascadaEntrada {
    const val MAX_RETRASO_MS = 320L
    private const val MS_POR_100DP = 38L

    fun retrasoMs(yEnDp: Float): Long = (yEnDp / 100f * MS_POR_100DP).toLong().coerceIn(0L, MAX_RETRASO_MS)
}

/**
 * Entrada de pantalla en cascada: cada elemento aparece con un fundido y sube unos dp, empezando por
 * los de arriba. El retraso sale de su posición en pantalla, así que no hay que numerar nada.
 * Solo toca la capa gráfica (alpha y translationY): no recompone y es liviano en gama baja.
 * Corre una vez por elemento; el sistema respeta la escala de animación del usuario.
 */
fun Modifier.entradaEnCascada(subeDp: Int = 14): Modifier = composed {
    val progreso = remember { Animatable(0f) }
    var retraso by remember { mutableLongStateOf(-1L) }
    val densidad = LocalDensity.current.density
    LaunchedEffect(retraso) {
        if (retraso < 0L) return@LaunchedEffect
        delay(retraso)
        progreso.animateTo(1f, tween(260, easing = FastOutSlowInEasing))
    }
    this
        .onGloballyPositioned { if (retraso < 0L) retraso = CascadaEntrada.retrasoMs(it.positionInRoot().y / densidad) }
        .graphicsLayer {
            alpha = progreso.value
            translationY = (1f - progreso.value) * subeDp * density
        }
}
