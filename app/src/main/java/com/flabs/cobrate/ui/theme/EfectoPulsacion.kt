package com.flabs.cobrate.ui.theme

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

/** Fuente de toques y modificador que encoge un poco el botón mientras se pulsa. */
class Pulsacion(val fuente: MutableInteractionSource, val modificador: Modifier)

/**
 * Efecto de pulsación barato: solo cambia la escala en la capa gráfica (90 ms), sin recomponer el
 * contenido, así que va bien incluso en celulares de pocos recursos.
 * Uso: `val pulso = rememberPulsacion()` y pasar `pulso.fuente` al `Surface(interactionSource = …)`
 * y `pulso.modificador` a su `modifier`.
 */
@Composable
fun rememberPulsacion(): Pulsacion {
    val fuente = remember { MutableInteractionSource() }
    val presionado by fuente.collectIsPressedAsState()
    val escala by animateFloatAsState(if (presionado) 0.97f else 1f, tween(90), label = "pulsacion")
    return Pulsacion(fuente, Modifier.graphicsLayer { scaleX = escala; scaleY = escala })
}
