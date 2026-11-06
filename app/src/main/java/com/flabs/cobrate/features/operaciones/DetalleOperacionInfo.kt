package com.flabs.cobrate.features.operaciones

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flabs.cobrate.components.TokeIcons
import com.flabs.cobrate.ui.theme.PlusJakartaSans
import com.flabs.cobrate.ui.theme.TokePaleta

/**
 * Tarjeta "Información de la operación". En el aspecto claro el título va dentro de la
 * tarjeta; en el oscuro va encima, como en el diseño.
 */
@Composable
fun DetalleOperacionInfo(
    paleta: TokePaleta,
    esOscuro: Boolean,
    detalle: DetalleOperacion,
) {
    if (esOscuro) {
        TituloInfo(paleta, Modifier.padding(start = 4.dp, bottom = 10.dp))
    }
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = paleta.tarjeta,
        border = BorderStroke(1.dp, paleta.borde),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            if (!esOscuro) {
                TituloInfo(paleta, Modifier.padding(top = 4.dp, bottom = 8.dp))
            }
            detalle.filas.forEachIndexed { indice, fila ->
                if (indice > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(paleta.borde),
                    )
                }
                FilaInformacion(paleta, fila)
            }
        }
    }
}

@Composable
private fun TituloInfo(paleta: TokePaleta, modifier: Modifier) {
    Text(
        text = "INFORMACIÓN DE LA OPERACIÓN",
        style = TextStyle(
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            letterSpacing = 0.8.sp,
        ),
        color = paleta.textoSecundario,
        modifier = modifier,
    )
}

@Composable
private fun FilaInformacion(paleta: TokePaleta, fila: FilaDetalle) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = paleta.tileFondo,
            border = BorderStroke(1.dp, paleta.borde),
            modifier = Modifier.size(30.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(id = iconoDe(fila.icono)),
                    contentDescription = null,
                    tint = paleta.textoSecundario,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = fila.etiqueta,
            style = TextStyle(
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
            ),
            color = paleta.textoSecundario,
            maxLines = 1,
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = fila.valor,
            style = TextStyle(
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                textAlign = TextAlign.End,
            ),
            color = paleta.textoPrincipal,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
    }
}

private fun iconoDe(icono: IconoFila): Int = when (icono) {
    IconoFila.NEGOCIO -> TokeIcons.Tienda
    IconoFila.FECHA -> TokeIcons.Calendario
    IconoFila.HORA -> TokeIcons.Clock
    IconoFila.PAGADOR, IconoFila.TRABAJADOR -> TokeIcons.Persona
    IconoFila.ID -> TokeIcons.Recibo
}
