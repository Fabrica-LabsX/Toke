package com.flabs.cobrate.features.equipo

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flabs.cobrate.components.inicialesDe
import com.flabs.cobrate.features.business.SolicitudUi
import com.flabs.cobrate.ui.theme.PlusJakartaSans
import com.flabs.cobrate.ui.theme.TokePaleta

private val RojoRechazar = Color(0xFFDC2626)

/** Fila de una solicitud de ingreso, con los botones Aceptar y Rechazar. */
@Composable
fun EquipoSolicitudFila(
    paleta: TokePaleta,
    solicitud: SolicitudUi,
    onAceptar: () -> Unit,
    onRechazar: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = paleta.tarjeta,
        border = BorderStroke(1.dp, paleta.borde),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = paleta.tileFondo,
                    border = BorderStroke(1.dp, paleta.bordeControl),
                    modifier = Modifier.size(44.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = inicialesDe(solicitud.nombre),
                            style = TextStyle(
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                            ),
                            color = paleta.textoPrincipal,
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = solicitud.nombre,
                        style = TextStyle(
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                        ),
                        color = paleta.textoPrincipal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    solicitud.codigoUsado?.let { codigo ->
                        Text(
                            text = "Código usado: $codigo",
                            style = TextStyle(
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp,
                            ),
                            color = paleta.textoSecundario,
                            maxLines = 1,
                        )
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Boton(
                    texto = "Aceptar",
                    fondo = paleta.verde,
                    colorTexto = paleta.verdeTexto,
                    borde = null,
                    onClick = onAceptar,
                    modifier = Modifier.weight(1f),
                )
                Boton(
                    texto = "Rechazar",
                    fondo = paleta.tarjeta,
                    colorTexto = RojoRechazar,
                    borde = BorderStroke(1.dp, RojoRechazar),
                    onClick = onRechazar,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun Boton(
    texto: String,
    fondo: Color,
    colorTexto: Color,
    borde: BorderStroke?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = fondo,
        border = borde,
        modifier = modifier.height(44.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = texto,
                style = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                ),
                color = colorTexto,
            )
        }
    }
}
