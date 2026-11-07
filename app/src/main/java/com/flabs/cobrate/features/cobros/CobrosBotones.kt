package com.flabs.cobrate.features.cobros
import com.flabs.cobrate.ui.theme.TokeMedidas
import com.flabs.cobrate.ui.theme.rememberPulsacion

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.flabs.cobrate.ui.theme.PlusJakartaSans
import com.flabs.cobrate.ui.theme.TokePaleta

/** Botón grande de la pantalla de cobros. [principal] lo pinta sólido; si no, lleva solo borde. */
@Composable
fun CobrosBoton(
    paleta: TokePaleta,
    texto: String,
    principal: Boolean,
    onClick: () -> Unit,
    icono: Int? = null,
) {
    val fondo = if (principal) paleta.seleccionFondo else paleta.tarjeta
    val colorContenido = if (principal) paleta.seleccionTexto else paleta.textoPrincipal
    val pulso = rememberPulsacion()
    Surface(
        onClick = onClick,
        interactionSource = pulso.fuente,
        shape = RoundedCornerShape(9999.dp),
        color = fondo,
        border = if (principal) null else BorderStroke(1.dp, paleta.bordeControl),
        modifier = Modifier
            .fillMaxWidth()
            .height(TokeMedidas.BotonAltura)
            .then(pulso.modificador),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icono != null) {
                    Icon(
                        painter = painterResource(id = icono),
                        contentDescription = null,
                        tint = colorContenido,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                }
                Text(
                    text = texto,
                    style = TextStyle(
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                    ),
                    color = colorContenido,
                    maxLines = 1,
                )
            }
        }
    }
}

