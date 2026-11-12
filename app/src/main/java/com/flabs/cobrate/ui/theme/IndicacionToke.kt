package com.flabs.cobrate.ui.theme

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.invalidateDraw
import kotlinx.coroutines.launch

/** Cuánto se encoge lo que se toca mientras está pulsado (96 %). */
const val ESCALA_HUNDIDO = 0.96f

/**
 * Respuesta al toque de TODA la app: sin la onda de Material. Lo pulsado se hunde un poco (70 ms)
 * y vuelve con un rebote corto al soltar. Se dibuja encogiendo el contenido, así que respeta cualquier forma
 * (tarjetas, píldoras, íconos) y no agrega capas ni transparencias: es liviano en gama baja.
 * Se instala una sola vez en el tema (`LocalIndication`).
 */
object IndicacionToke : IndicationNodeFactory {
    override fun create(interactionSource: InteractionSource): DelegatableNode = NodoPulsacion(interactionSource)
    override fun equals(other: Any?) = other === IndicacionToke
    override fun hashCode() = -1
}

private class NodoPulsacion(private val fuente: InteractionSource) : Modifier.Node(), DrawModifierNode {
    private val escala = Animatable(1f)

    override fun onAttach() {
        coroutineScope.launch {
            fuente.interactions.collect { interaccion ->
                when (interaccion) {
                    is PressInteraction.Press -> launch { escala.animateTo(ESCALA_HUNDIDO, tween(70)) { invalidateDraw() } }
                    is PressInteraction.Release, is PressInteraction.Cancel ->
                        launch { escala.animateTo(1f, spring(dampingRatio = 0.55f, stiffness = 650f)) { invalidateDraw() } }
                }
            }
        }
    }

    override fun ContentDrawScope.draw() {
        val e = escala.value
        if (e == 1f) drawContent() else scale(e, e) { this@draw.drawContent() }
    }
}
