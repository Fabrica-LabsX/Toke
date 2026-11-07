package com.flabs.cobrate.features.cobros

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.flabs.cobrate.core.model.MetodoDeCobro

/**
 * Nombre del método de cobro. Si es de Plin, al lado del nombre de la billetera va una barrera
 * fina y la marca: "Interbank | Plin".
 */
@Composable
fun NombreMetodo(
    metodo: MetodoDeCobro,
    estilo: TextStyle,
    colorNombre: Color,
    colorMarca: Color,
    colorBarrera: Color,
    modifier: Modifier = Modifier,
) {
    val marca = MetodosCobroLogica.marcaPlin(metodo)
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = MetodosCobroLogica.nombreLegible(metodo.tipo),
            style = estilo,
            color = colorNombre,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (marca != null) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .width(1.5.dp)
                    .height(14.dp)
                    .background(colorBarrera, RoundedCornerShape(1.dp)),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = marca,
                style = estilo.copy(fontWeight = FontWeight.SemiBold),
                color = colorMarca,
                maxLines = 1,
            )
        }
    }
}
