package com.flabs.cobrate.features.equipo

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flabs.cobrate.ui.theme.PlusJakartaSans
import com.flabs.cobrate.ui.theme.TokePaleta

/** Cabecera de Equipo: título y, debajo, el nombre del negocio activo. */
@Composable
fun EquipoCabecera(
    paleta: TokePaleta,
    nombreNegocio: String?,
) {
    Surface(
        color = paleta.tarjeta,
        border = BorderStroke(1.dp, paleta.borde),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Text(
                text = "Equipo",
                style = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    letterSpacing = (-0.5).sp,
                ),
                color = paleta.textoPrincipal,
            )
            if (!nombreNegocio.isNullOrBlank()) {
                Text(
                    text = nombreNegocio,
                    style = TextStyle(
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                    ),
                    color = paleta.textoSecundario,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
