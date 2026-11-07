package com.flabs.cobrate.features.equipo
import com.flabs.cobrate.ui.theme.TokeMedidas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flabs.cobrate.ui.theme.PlusJakartaSans
import com.flabs.cobrate.ui.theme.TokePaleta

/**
 * Píldoras "Mi Equipo (N)", "Códigos" y "Solicitudes (M)". Las dos últimas solo existen
 * para quien puede administrar el equipo; "Solicitudes" solo aparece si hay solicitudes.
 */
@Composable
fun EquipoPestanas(
    paleta: TokePaleta,
    seleccionada: PestanaEquipo,
    totalTrabajadores: Int,
    cantidadSolicitudes: Int,
    puedeAdministrar: Boolean,
    onSeleccionar: (PestanaEquipo) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        val equipoActivo = seleccionada == PestanaEquipo.MI_EQUIPO
        Pildora(
            texto = "Mi Equipo ($totalTrabajadores)",
            fondo = if (equipoActivo) paleta.seleccionFondo else paleta.tarjeta,
            colorTexto = if (equipoActivo) paleta.seleccionTexto else paleta.textoPrincipal,
            borde = if (equipoActivo) null else paleta.bordeControl,
            onClick = { onSeleccionar(PestanaEquipo.MI_EQUIPO) },
            modifier = Modifier.weight(1f),
        )
        if (puedeAdministrar) {
            val codigosActiva = seleccionada == PestanaEquipo.CODIGOS
            Pildora(
                texto = "Códigos",
                fondo = if (codigosActiva) paleta.seleccionFondo else paleta.tarjeta,
                colorTexto = if (codigosActiva) paleta.seleccionTexto else paleta.textoPrincipal,
                borde = if (codigosActiva) null else paleta.bordeControl,
                onClick = { onSeleccionar(PestanaEquipo.CODIGOS) },
                modifier = Modifier.weight(1f),
            )
            if (cantidadSolicitudes > 0) {
                Pildora(
                    texto = "Solicitudes ($cantidadSolicitudes)",
                    fondo = paleta.ambar,
                    colorTexto = paleta.ambarTexto,
                    borde = null,
                    onClick = { onSeleccionar(PestanaEquipo.SOLICITUDES) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
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
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = fondo,
        border = borde?.let { BorderStroke(1.dp, it) },
        modifier = modifier.height(TokeMedidas.ChipAltura),
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = texto,
                style = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                ),
                color = colorTexto,
                maxLines = 1,
            )
        }
    }
}
