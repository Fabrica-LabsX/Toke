package com.flabs.cobrate.ui.theme

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

/** Fuente de toques y modificador que hunde un poco el botón mientras se pulsa. */
class Pulsacion(val fuente: MutableInteractionSource, val modificador: Modifier)

/**
 * Efecto de "hundirse" al tocar, como en Waze: el botón se encoge y se apaga un poco al pulsar (70 ms)
 * y vuelve con un pequeño rebote al soltar. Solo cambia la capa gráfica, sin recomponer el contenido,
 * así que va bien incluso en celulares de pocos recursos.
 * Uso: `val pulso = rememberPulsacion()` y pasar `pulso.fuente` al `Surface(interactionSource = …)`
 * y `pulso.modificador` a su `modifier`.
 */
@Composable
fun rememberPulsacion(): Pulsacion {
    val fuente = remember { MutableInteractionSource() }
    val presionado by fuente.collectIsPressedAsState()
    val escala by animateFloatAsState(
        targetValue = if (presionado) ESCALA_HUNDIDO else 1f,
        animationSpec = if (presionado) tween(70) else spring(dampingRatio = 0.45f, stiffness = 650f),
        label = "pulsacion",
    )
    return Pulsacion(fuente, Modifier.graphicsLayer { scaleX = escala; scaleY = escala })
}

/** Cuánto se encoge un botón al pulsarlo (94 %). */
const val ESCALA_HUNDIDO = 0.94f
