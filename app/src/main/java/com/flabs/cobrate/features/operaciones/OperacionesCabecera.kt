package com.flabs.cobrate.features.operaciones

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import com.flabs.cobrate.components.TokeIcons
import com.flabs.cobrate.ui.theme.PlusJakartaSans
import com.flabs.cobrate.ui.theme.TokePaleta

/**
 * Cabecera de Operaciones: botón atrás (solo si hay a dónde volver), título centrado y
 * botón de filtros a la derecha.
 */
@Composable
fun OperacionesCabecera(
    paleta: TokePaleta,
    onBack: (() -> Unit)?,
    onRestablecerFiltros: () -> Unit,
) {
    Surface(
        color = paleta.tarjeta,
        border = BorderStroke(1.dp, paleta.borde),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (onBack != null) {
                BotonCuadrado(
                    paleta = paleta,
                    icono = TokeIcons.FlechaAtras,
                    descripcion = "Volver atrás",
                    onClick = onBack,
                    modifier = Modifier.align(Alignment.CenterStart),
                )
            }
            Text(
                text = "Operaciones",
                style = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    letterSpacing = (-0.5).sp,
                ),
                color = paleta.textoPrincipal,
            )
            BotonCuadrado(
                paleta = paleta,
                icono = TokeIcons.Filtros,
                descripcion = "Restablecer filtros",
                onClick = onRestablecerFiltros,
                modifier = Modifier.align(Alignment.CenterEnd),
            )
        }
    }
}

@Composable
private fun BotonCuadrado(
    paleta: TokePaleta,
    icono: Int,
    descripcion: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = paleta.tarjeta,
        border = BorderStroke(1.dp, paleta.borde),
        modifier = modifier.size(40.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(id = icono),
                contentDescription = descripcion,
                tint = paleta.textoPrincipal,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
