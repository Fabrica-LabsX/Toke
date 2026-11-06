package com.flabs.cobrate.features.operaciones

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flabs.cobrate.components.TokeIcons
import com.flabs.cobrate.ui.theme.PlusJakartaSans
import com.flabs.cobrate.ui.theme.TokePaleta

/** Monto protagonista y la píldora de estado ("Recibido" en verde o "Sin asignar" en ámbar). */
@Composable
fun DetalleOperacionResumen(
    paleta: TokePaleta,
    detalle: DetalleOperacion,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = detalle.monto,
            style = TextStyle(
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 40.sp,
                letterSpacing = (-1).sp,
            ),
            color = paleta.textoPrincipal,
            maxLines = 1,
        )
        Spacer(modifier = Modifier.height(12.dp))

        val fondo = if (detalle.recibido) paleta.verde else paleta.ambar
        val colorTexto = if (detalle.recibido) paleta.verdeTexto else paleta.ambarTexto
        Surface(shape = RoundedCornerShape(9999.dp), color = fondo) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(id = if (detalle.recibido) TokeIcons.Check else TokeIcons.Clock),
                    contentDescription = null,
                    tint = colorTexto,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (detalle.recibido) "RECIBIDO" else "SIN ASIGNAR",
                    style = TextStyle(
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        letterSpacing = 0.4.sp,
                    ),
                    color = colorTexto,
                    maxLines = 1,
                )
            }
        }
    }
}
