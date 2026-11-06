package com.flabs.cobrate.features.operaciones

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flabs.cobrate.components.TokeIcons
import com.flabs.cobrate.ui.theme.PlusJakartaSans
import com.flabs.cobrate.ui.theme.TokePaleta

/** Campo de búsqueda de Operaciones. Solo muestra y edita el texto; el filtrado vive en [FiltroOperaciones]. */
@Composable
fun OperacionesBuscador(
    paleta: TokePaleta,
    texto: String,
    onTextoCambia: (String) -> Unit,
) {
    val estilo = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
    )
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = paleta.tarjeta,
        border = BorderStroke(1.dp, paleta.borde),
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(id = TokeIcons.Buscar),
                contentDescription = "Buscar",
                tint = paleta.textoSecundario,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(10.dp))
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                if (texto.isEmpty()) {
                    Text(
                        text = "Buscar cliente o monto...",
                        style = estilo,
                        color = paleta.textoSecundario,
                        maxLines = 1,
                    )
                }
                BasicTextField(
                    value = texto,
                    onValueChange = onTextoCambia,
                    singleLine = true,
                    textStyle = estilo.copy(color = paleta.textoPrincipal),
                    cursorBrush = SolidColor(paleta.textoPrincipal),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
