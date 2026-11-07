package com.flabs.cobrate.features.negocios

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
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
 * Los dos botones grandes: uno principal (oscuro en claro, blanco en oscuro) y otro
 * con borde. Los usan Mis Negocios y el selector "Cambiar de negocio".
 */
@Composable
fun NegociosBotonesPie(
    paleta: TokePaleta,
    textoPrincipal: String,
    onPrincipal: () -> Unit,
    textoSecundario: String,
    onSecundario: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Boton(
            texto = textoPrincipal,
            fondo = paleta.seleccionFondo,
            colorTexto = paleta.seleccionTexto,
            borde = null,
            onClick = onPrincipal,
        )
        Boton(
            texto = textoSecundario,
            fondo = paleta.tarjeta,
            colorTexto = paleta.textoPrincipal,
            borde = BorderStroke(1.dp, paleta.bordeControl),
            onClick = onSecundario,
        )
    }
}

@Composable
private fun Boton(
    texto: String,
    fondo: Color,
    colorTexto: Color,
    borde: BorderStroke?,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(9999.dp),
        color = fondo,
        border = borde,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = texto,
                style = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                ),
                color = colorTexto,
                maxLines = 1,
            )
        }
    }
}

/** Contenedor del pie de Mis Negocios: fondo, línea superior y espacio de la barra del sistema. */
@Composable
fun MisNegociosPie(
    paleta: TokePaleta,
    onIngresarCodigo: () -> Unit,
    onCrearNegocio: () -> Unit,
) {
    Surface(
        color = paleta.tarjeta,
        border = BorderStroke(1.dp, paleta.borde),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(modifier = Modifier.navigationBarsPadding()) {
            NegociosBotonesPie(
                paleta = paleta,
                textoPrincipal = "Ingresar código de invitación",
                onPrincipal = onIngresarCodigo,
                textoSecundario = "+ Crear Negocio",
                onSecundario = onCrearNegocio,
            )
        }
    }
}
