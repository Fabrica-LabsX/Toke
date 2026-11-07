package com.flabs.cobrate.features.planes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flabs.cobrate.ui.theme.PlusJakartaSans
import com.flabs.cobrate.ui.theme.TokePaleta

/** Color de la cuota: texto normal (negro en claro, blanco en oscuro), amarillo, rojo. */
fun colorDeCuota(paleta: TokePaleta, nivel: NivelCuota): Color = when (nivel) {
    NivelCuota.NORMAL -> paleta.textoPrincipal
    NivelCuota.AMARILLO -> paleta.ambar
    NivelCuota.ROJO, NivelCuota.AGOTADA -> paleta.rojo
}

/** Mensaje "50 notificaciones restantes" que va arriba del monto. */
@Composable
fun CuotaMensaje(paleta: TokePaleta, cuota: CuotaUi) {
    Text(
        text = PlanLogica.textoRestantes(cuota.restantes),
        style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 13.sp),
        color = colorDeCuota(paleta, cuota.nivel),
    )
}

/** Barrita que va debajo del monto: se vacía y cambia de color a medida que baja la cuota. */
@Composable
fun CuotaBarra(paleta: TokePaleta, cuota: CuotaUi) {
    val progreso = PlanLogica.progreso(cuota.limite, cuota.restantes)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(CircleShape)
            .background(paleta.bordeControl),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progreso)
                .fillMaxHeight()
                .clip(CircleShape)
                .background(colorDeCuota(paleta, cuota.nivel)),
        )
    }
}
