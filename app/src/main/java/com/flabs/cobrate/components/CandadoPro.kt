package com.flabs.cobrate.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flabs.cobrate.ui.theme.PlusJakartaSans
import com.flabs.cobrate.ui.theme.TokePaleta
import com.flabs.cobrate.ui.theme.tokePaleta

/**
 * Candadito negro con fondo sutil circular alrededor del icono.
 * Al tocarse, ejecuta la acción para navegar a Planes y Pago.
 */
@Composable
fun CandadoProBadge(
    modifier: Modifier = Modifier,
    paleta: TokePaleta = tokePaleta(),
    tamano: Dp = 28.dp,
    tamanoIcono: Dp = 16.dp,
    onClick: (() -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .size(tamano)
            .clip(CircleShape)
            .background(paleta.candadoBadgeFondo)
            .border(BorderStroke(1.dp, paleta.candadoBadgeBorde), CircleShape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(id = TokeIcons.Candado),
            contentDescription = "Bloqueado con Plan PRO",
            tint = Color.Black,
            modifier = Modifier.size(tamanoIcono),
        )
    }
}

/**
 * Botón bloqueado por Plan PRO con candado negro y llamada a la pantalla de planes y pago.
 */
@Composable
fun BotonProBloqueado(
    paleta: TokePaleta,
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = paleta.tileFondo,
        border = BorderStroke(1.dp, paleta.borde),
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = texto,
                style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 14.sp),
                color = paleta.textoSecundario,
                modifier = Modifier.weight(1f),
            )
            Spacer(modifier = Modifier.width(8.dp))
            CandadoProBadge(paleta = paleta, onClick = onClick)
        }
    }
}
