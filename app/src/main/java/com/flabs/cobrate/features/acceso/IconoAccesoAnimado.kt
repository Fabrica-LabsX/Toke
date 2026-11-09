package com.flabs.cobrate.features.acceso

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.flabs.cobrate.ui.theme.TokePaleta

/**
 * Icono de una pantalla de acceso: loseta verde con el dibujo en blanco. Entra con un rebote y un aro
 * que se expande en bucle; con [balanceo] el dibujo se mece suavemente (la llave del código de trabajador).
 * Todo ocurre en la capa gráfica: no recompone y es barato en celulares modestos.
 */
@Composable
fun IconoAccesoAnimado(
    paleta: TokePaleta,
    @DrawableRes icono: Int,
    modifier: Modifier = Modifier,
    tamano: Int = 88,
    balanceo: Boolean = false,
) {
    val entrada = remember { Animatable(0.6f) }
    LaunchedEffect(Unit) { entrada.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = 260f)) }
    val bucle = rememberInfiniteTransition(label = "icono-acceso")
    val aro by bucle.animateFloat(0f, 1f, infiniteRepeatable(tween(2200, easing = FastOutSlowInEasing)), label = "aro")
    val giro by bucle.animateFloat(
        -9f, 9f, infiniteRepeatable(tween(1500, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "giro",
    )

    Box(
        modifier = modifier
            .size((tamano * 1.7f).dp)
            .drawBehind {
                // Aro que sale de la loseta y se desvanece.
                drawCircle(
                    color = paleta.verde.copy(alpha = 0.22f * (1f - aro)),
                    radius = size.minDimension / 2f * (0.55f + 0.45f * aro),
                    center = Offset(size.width / 2f, size.height / 2f),
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            shape = RoundedCornerShape((tamano * 0.3f).dp),
            color = paleta.verde,
            modifier = Modifier.size(tamano.dp).graphicsLayer { scaleX = entrada.value; scaleY = entrada.value },
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(id = icono),
                    contentDescription = null,
                    tint = paleta.verdeTexto,
                    modifier = Modifier
                        .size((tamano * 0.5f).dp)
                        .graphicsLayer { if (balanceo) rotationZ = giro },
                )
            }
        }
    }
}
