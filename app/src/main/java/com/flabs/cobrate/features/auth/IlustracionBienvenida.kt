package com.flabs.cobrate.features.auth

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flabs.cobrate.components.IconoMetodoPago
import com.flabs.cobrate.components.TokeIcons
import com.flabs.cobrate.core.model.TipoMetodoCobro
import com.flabs.cobrate.features.acceso.LogoToke
import com.flabs.cobrate.ui.theme.PlusJakartaSans
import com.flabs.cobrate.ui.theme.TokePaleta
import kotlin.math.cos
import kotlin.math.sin

/** Algo que gira alrededor del logo: una etiqueta con icono, un icono suelto o el logo de una billetera. */
private sealed class Satelite {
    data class Etiqueta(val texto: String, val icono: Int) : Satelite()
    data class Icono(val icono: Int) : Satelite()
    data class Billetera(val tipo: TipoMetodoCobro) : Satelite()
}

// Orden alrededor del anillo: cada 60°. Las etiquetas van enfrentadas (0° y 180°) para que nunca se choquen.
private val satelites = listOf(
    Satelite.Etiqueta("En voz alta", TokeIcons.VolumenAlto),
    Satelite.Icono(TokeIcons.Qr),
    Satelite.Billetera(TipoMetodoCobro.YAPE),
    Satelite.Etiqueta("Al equipo", TokeIcons.Check),
    Satelite.Billetera(TipoMetodoCobro.PLIN),
    Satelite.Icono(TokeIcons.Campana),
)

/**
 * Ilustración de la bienvenida: el logo de TOKE al centro, con sus funciones girando a su alrededor como
 * planetas (voz alta, equipo, QR, Yape, Plin, avisos). Los satélites van en un solo anillo rígido y giran
 * despacio (36 s por vuelta) siempre derechos; una onda sale del logo como si "avisara". Todo se mueve en la
 * capa gráfica, sin recomponer, para que sea liviano en celulares modestos.
 */
@Composable
fun IlustracionBienvenida(paleta: TokePaleta, modifier: Modifier = Modifier) {
    val bucle = rememberInfiniteTransition(label = "orbita")
    val giro by bucle.animateFloat(0f, 360f, infiniteRepeatable(tween(36_000, easing = LinearEasing)), label = "giro")
    val onda by bucle.animateFloat(0f, 1f, infiniteRepeatable(tween(2600, easing = FastOutSlowInEasing)), label = "onda")

    Box(modifier = modifier.fillMaxWidth().height(310.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val centro = Offset(size.width / 2f, size.height / 2f)
            listOf(64.dp to 0.10f, RADIO_ORBITA to 0.16f, 150.dp to 0.06f).forEach { (radio, alfa) ->
                drawCircle(paleta.verde.copy(alpha = alfa), radius = radio.toPx(), center = centro, style = Stroke(1.5.dp.toPx()))
            }
            // Onda que sale del logo y se desvanece.
            drawCircle(
                color = paleta.verde.copy(alpha = 0.20f * (1f - onda)),
                radius = (46.dp.toPx()) + (RADIO_ORBITA.toPx() - 46.dp.toPx()) * onda,
                center = centro,
            )
        }

        LogoToke(paleta, tamano = 84, descripcion = "TOKE")

        satelites.forEachIndexed { i, satelite ->
            Box(
                modifier = Modifier.graphicsLayer {
                    val angulo = Math.toRadians((giro + i * 60f).toDouble())
                    val radio = RADIO_ORBITA.toPx()
                    translationX = (radio * cos(angulo)).toFloat()
                    translationY = (radio * sin(angulo)).toFloat()
                },
            ) { ContenidoSatelite(paleta, satelite) }
        }
    }
}

private val RADIO_ORBITA = 104.dp

@Composable
private fun ContenidoSatelite(paleta: TokePaleta, satelite: Satelite) {
    when (satelite) {
        is Satelite.Etiqueta -> Etiqueta(paleta, satelite.texto, satelite.icono)
        is Satelite.Icono -> Burbuja(paleta) {
            Icon(painter = painterResource(id = satelite.icono), contentDescription = null, tint = paleta.textoPrincipal, modifier = Modifier.size(20.dp))
        }
        is Satelite.Billetera -> Burbuja(paleta) { IconoMetodoPago(tipo = satelite.tipo, tamano = 34.dp) }
    }
}

@Composable
private fun Burbuja(paleta: TokePaleta, contenido: @Composable () -> Unit) {
    Surface(
        shape = CircleShape,
        color = paleta.tarjeta,
        border = BorderStroke(1.dp, paleta.borde),
        shadowElevation = 3.dp,
        modifier = Modifier.size(40.dp),
    ) { Box(contentAlignment = Alignment.Center) { contenido() } }
}

@Composable
private fun Etiqueta(paleta: TokePaleta, texto: String, icono: Int) {
    Surface(
        shape = RoundedCornerShape(9999.dp),
        color = paleta.tarjeta,
        border = BorderStroke(1.dp, paleta.borde),
        shadowElevation = 3.dp,
    ) {
        Row(modifier = Modifier.padding(start = 6.dp, end = 12.dp, top = 6.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = paleta.verde, modifier = Modifier.size(24.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(painter = painterResource(id = icono), contentDescription = null, tint = paleta.verdeTexto, modifier = Modifier.size(13.dp))
                }
            }
            Spacer(modifier = Modifier.width(7.dp))
            Text(
                text = texto,
                style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 11.sp),
                color = paleta.textoPrincipal,
                maxLines = 1,
            )
        }
    }
}
