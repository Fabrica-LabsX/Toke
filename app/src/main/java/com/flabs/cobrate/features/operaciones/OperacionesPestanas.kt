package com.flabs.cobrate.features.operaciones
import com.flabs.cobrate.ui.theme.TokeMedidas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flabs.cobrate.components.TokeIcons
import com.flabs.cobrate.ui.theme.PlusJakartaSans
import com.flabs.cobrate.ui.theme.TokePaleta

/**
 * Píldoras "Todas (N)" y "Pendientes (M)". "Pendientes" se pinta en ámbar mientras haya
 * pendientes o esté seleccionada; sin pendientes y sin seleccionar queda como píldora neutra.
 */
@Composable
fun OperacionesPestanas(
    paleta: TokePaleta,
    seleccionada: PestanaOperaciones,
    totalOperaciones: Int,
    totalPendientes: Int,
    onSeleccionar: (PestanaOperaciones) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        val todasActiva = seleccionada == PestanaOperaciones.TODAS
        Pildora(
            texto = "Todas ($totalOperaciones)",
            fondo = if (todasActiva) paleta.seleccionFondo else paleta.tarjeta,
            colorTexto = if (todasActiva) paleta.seleccionTexto else paleta.textoSecundario,
            borde = if (todasActiva) null else paleta.borde,
            onClick = { onSeleccionar(PestanaOperaciones.TODAS) },
            modifier = Modifier.weight(1f),
        )

        val pendientesActiva = seleccionada == PestanaOperaciones.PENDIENTES
        val pendientesAmbar = pendientesActiva || totalPendientes > 0
        Pildora(
            texto = "Pendientes ($totalPendientes)",
            fondo = if (pendientesAmbar) paleta.ambar else paleta.tarjeta,
            colorTexto = if (pendientesAmbar) paleta.ambarTexto else paleta.textoSecundario,
            borde = if (pendientesAmbar) null else paleta.borde,
            iconoInicial = if (pendientesAmbar) TokeIcons.Clock else null,
            onClick = { onSeleccionar(PestanaOperaciones.PENDIENTES) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun Pildora(
    texto: String,
    fondo: Color,
    colorTexto: Color,
    borde: Color?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconoInicial: Int? = null,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(9999.dp),
        color = fondo,
        border = borde?.let { BorderStroke(1.dp, it) },
        modifier = modifier.height(TokeMedidas.ChipAltura),
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (iconoInicial != null) {
                    Icon(
                        painter = painterResource(id = iconoInicial),
                        contentDescription = null,
                        tint = colorTexto,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(
                    text = texto,
                    style = TextStyle(
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                    ),
                    color = colorTexto,
                    maxLines = 1,
                )
            }
        }
    }
}
