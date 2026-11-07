package com.flabs.cobrate.features.negocios

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flabs.cobrate.components.TokeIcons
import com.flabs.cobrate.ui.theme.PlusJakartaSans
import com.flabs.cobrate.ui.theme.TokePaleta

/** Fila de un negocio dentro del selector. Solo se puede tocar si se puede entrar al negocio. */
@Composable
fun NegocioFilaSelector(
    paleta: TokePaleta,
    esOscuro: Boolean,
    item: NegocioItem,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        enabled = item.puedeEntrar,
        shape = RoundedCornerShape(18.dp),
        color = if (esOscuro) paleta.tileFondo else paleta.fondo,
        border = BorderStroke(
            width = if (item.esActual) 1.5.dp else 1.dp,
            color = if (item.esActual) paleta.activo else paleta.bordeControl,
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NegocioTile(paleta = paleta, item = item, tamano = 48.dp)
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = item.negocio.nombre,
                    style = TextStyle(
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp,
                    ),
                    color = paleta.textoPrincipal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                NegocioEtiqueta(paleta = paleta, item = item)
            }
            Icon(
                painter = painterResource(id = TokeIcons.ChevronDerecha),
                contentDescription = null,
                tint = paleta.textoSecundario,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}
