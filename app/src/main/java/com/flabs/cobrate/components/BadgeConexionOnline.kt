package com.flabs.cobrate.components
import com.flabs.cobrate.ui.theme.tokePaleta

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

/**
 * Badge de conexión en línea (cabecera del Home del dueño y del trabajador).
 * Mismos valores en claro y en oscuro: cuadro de 36 dp, radio 10 dp, relleno verde de la paleta,
 * borde del mismo verde y glifo de 20 dp centrado.
 *
 * Solo dibuja el estado verde. Quién lo muestra y cuándo lo decide quien lo llama,
 * a partir de NetworkMonitor.disponible (única fuente de red).
 */
@Composable
fun BadgeConexionOnline(modifier: Modifier = Modifier) {
    val paleta = tokePaleta()
    Surface(
        modifier = modifier.size(36.dp),
        shape = RoundedCornerShape(10.dp),
        color = paleta.verdeVivo,
        border = BorderStroke(1.dp, paleta.verdeVivoBorde),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(id = TokeIcons.WifiActivo),
                contentDescription = "Conectado a internet",
                tint = Color.Unspecified,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
