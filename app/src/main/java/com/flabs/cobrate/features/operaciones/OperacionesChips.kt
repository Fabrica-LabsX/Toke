package com.flabs.cobrate.features.operaciones

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flabs.cobrate.components.BilleteraColores
import com.flabs.cobrate.components.TokeIcons
import com.flabs.cobrate.ui.theme.PlusJakartaSans
import com.flabs.cobrate.ui.theme.TokePaleta

/**
 * Fila de chips bajo el buscador: selector de rango de tiempo (con menú) y los
 * filtros de billetera (Yape, Plin, BCP / Bancos). Tocar el chip activo lo desactiva.
 */
@Composable
fun OperacionesChips(
    paleta: TokePaleta,
    esOscuro: Boolean,
    rango: RangoTiempo,
    billetera: FiltroBilletera?,
    onRango: (RangoTiempo) -> Unit,
    onBilletera: (FiltroBilletera?) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ChipRango(paleta = paleta, rango = rango, onRango = onRango)

        FiltroBilletera.entries.forEach { filtro ->
            val activo = billetera == filtro
            ChipBilletera(
                paleta = paleta,
                texto = filtro.etiqueta,
                colorPunto = colorPuntoDe(filtro, esOscuro),
                activo = activo,
                onClick = { onBilletera(if (activo) null else filtro) },
            )
        }
    }
}

private fun colorPuntoDe(filtro: FiltroBilletera, esOscuro: Boolean): Color {
    val par = when (filtro) {
        FiltroBilletera.YAPE -> BilleteraColores.Yape
        FiltroBilletera.PLIN -> BilleteraColores.Plin
        FiltroBilletera.BANCOS -> BilleteraColores.BCP
    }
    return if (esOscuro) par.oscuro else par.claro
}

@Composable
private fun ChipRango(
    paleta: TokePaleta,
    rango: RangoTiempo,
    onRango: (RangoTiempo) -> Unit,
) {
    var menuAbierto by remember { mutableStateOf(false) }
    Box {
        Surface(
            onClick = { menuAbierto = true },
            shape = RoundedCornerShape(9999.dp),
            color = paleta.tarjeta,
            border = BorderStroke(1.dp, paleta.borde),
            modifier = Modifier.height(36.dp),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = rango.etiqueta,
                    style = textoChip(),
                    color = paleta.textoPrincipal,
                    maxLines = 1,
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    painter = painterResource(id = TokeIcons.ChevronDown),
                    contentDescription = "Cambiar rango de tiempo",
                    tint = paleta.textoSecundario,
                    modifier = Modifier.size(12.dp),
                )
            }
        }
        DropdownMenu(
            expanded = menuAbierto,
            onDismissRequest = { menuAbierto = false },
            containerColor = paleta.tarjeta,
        ) {
            RangoTiempo.entries.filter { it != RangoTiempo.SESENTA_MIN }.forEach { opcion ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = opcion.etiqueta,
                            style = textoChip(),
                            color = if (opcion == rango) paleta.enlaceVerde else paleta.textoPrincipal,
                        )
                    },
                    onClick = {
                        menuAbierto = false
                        onRango(opcion)
                    },
                )
            }
        }
    }
}

@Composable
private fun ChipBilletera(
    paleta: TokePaleta,
    texto: String,
    colorPunto: Color,
    activo: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(9999.dp),
        color = if (activo) paleta.seleccionFondo else paleta.tarjeta,
        border = if (activo) null else BorderStroke(1.dp, paleta.borde),
        modifier = Modifier.height(36.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(colorPunto),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = texto,
                style = textoChip(),
                color = if (activo) paleta.seleccionTexto else paleta.textoPrincipal,
                maxLines = 1,
            )
        }
    }
}

private fun textoChip() = TextStyle(
    fontFamily = PlusJakartaSans,
    fontWeight = FontWeight.SemiBold,
    fontSize = 14.sp,
)
