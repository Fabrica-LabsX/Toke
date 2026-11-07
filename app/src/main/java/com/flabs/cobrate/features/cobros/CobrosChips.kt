package com.flabs.cobrate.features.cobros

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flabs.cobrate.components.BilleteraColores
import com.flabs.cobrate.components.IconoMetodoPago
import com.flabs.cobrate.components.colorBilletera
import com.flabs.cobrate.core.model.MetodoDeCobro
import com.flabs.cobrate.ui.theme.PlusJakartaSans
import com.flabs.cobrate.ui.theme.ThemeMode
import com.flabs.cobrate.ui.theme.TokePaleta

/**
 * Fila de chips con los métodos de cobro del negocio. El elegido se pinta con el color de su
 * billetera y lleva el logo; los demás llevan un punto de color.
 */
@Composable
fun CobrosChips(
    paleta: TokePaleta,
    esOscuro: Boolean,
    metodos: List<MetodoDeCobro>,
    seleccionadoId: String?,
    onSeleccionar: (MetodoDeCobro) -> Unit,
) {
    val aspecto = if (esOscuro) ThemeMode.DARK else ThemeMode.LIGHT
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        metodos.forEach { metodo ->
            val elegido = metodo.id == seleccionadoId
            val colorMarca = colorBilletera(metodo.tipo, aspecto) ?: paleta.seleccionFondo
            Chip(
                paleta = paleta,
                metodo = metodo,
                elegido = elegido,
                colorMarca = colorMarca,
                colorPlin = if (esOscuro) BilleteraColores.Plin.oscuro else BilleteraColores.Plin.claro,
                onClick = { onSeleccionar(metodo) },
            )
        }
    }
}

@Composable
private fun Chip(
    paleta: TokePaleta,
    metodo: MetodoDeCobro,
    elegido: Boolean,
    colorMarca: Color,
    colorPlin: Color,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(9999.dp),
        color = if (elegido) colorMarca else paleta.tarjeta,
        border = if (elegido) null else BorderStroke(1.dp, paleta.bordeControl),
        modifier = Modifier.height(44.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (elegido) {
                IconoMetodoPago(tipo = metodo.tipo, tamano = 26.dp)
            } else {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(colorMarca),
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            NombreMetodo(
                metodo = metodo,
                estilo = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                ),
                colorNombre = if (elegido) Color.White else paleta.textoPrincipal,
                colorMarca = if (elegido) Color.White.copy(alpha = 0.9f) else colorPlin,
                colorBarrera = if (elegido) Color.White.copy(alpha = 0.55f) else paleta.bordeControl,
            )
        }
    }
}
