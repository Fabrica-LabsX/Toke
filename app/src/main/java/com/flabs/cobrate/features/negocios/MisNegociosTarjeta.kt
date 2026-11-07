package com.flabs.cobrate.features.negocios
import com.flabs.cobrate.ui.theme.TokeRojo
import com.flabs.cobrate.ui.theme.TokeAmbarTexto

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flabs.cobrate.components.TokeIcons
import com.flabs.cobrate.ui.theme.PlusJakartaSans
import com.flabs.cobrate.ui.theme.TokePaleta

private val RojoCancelar = TokeRojo
private val AmbarAviso = TokeAmbarTexto

/**
 * Tarjeta de un negocio en Mis Negocios. Según el estado muestra "Actual", "Entrar",
 * "Ver estado" (acceso pausado) o el aviso de espera con el botón "Cancelar".
 */
@Composable
fun MisNegociosTarjeta(
    paleta: TokePaleta,
    item: NegocioItem,
    nombreDueno: String,
    onEntrar: () -> Unit,
    onVerEstado: () -> Unit,
    onCancelarSolicitud: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = paleta.tarjeta,
        border = BorderStroke(
            width = if (item.esActual) 1.5.dp else 1.dp,
            color = if (item.esActual) paleta.activo else paleta.borde,
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                NegocioTile(paleta = paleta, item = item, tamano = 56.dp)
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = item.negocio.nombre,
                        style = TextStyle(
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 19.sp,
                        ),
                        color = paleta.textoPrincipal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = if (item.rol == RolEnNegocio.DUENO) "Tu negocio" else "Dueño: $nombreDueno",
                        style = TextStyle(
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                        ),
                        color = paleta.textoSecundario,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    NegocioEtiqueta(paleta = paleta, item = item)
                }
                AccionPrincipal(paleta, item, onEntrar, onVerEstado)
            }

            when (item.estado) {
                EstadoEnNegocio.PENDIENTE -> PieDeEstado(
                    paleta = paleta,
                    icono = TokeIcons.Clock,
                    texto = "Esperando aprobación del dueño",
                    botonTexto = "Cancelar",
                    botonFondo = RojoCancelar,
                    onBoton = onCancelarSolicitud,
                )
                EstadoEnNegocio.PAUSADO -> PieDeEstado(
                    paleta = paleta,
                    icono = TokeIcons.Pausa,
                    texto = "Pausado por el dueño",
                    botonTexto = null,
                    botonFondo = paleta.ambar,
                    onBoton = {},
                )
                EstadoEnNegocio.DISPONIBLE -> Unit
            }
        }
    }
}

@Composable
private fun AccionPrincipal(
    paleta: TokePaleta,
    item: NegocioItem,
    onEntrar: () -> Unit,
    onVerEstado: () -> Unit,
) {
    when {
        item.esActual -> NegocioAccion(
            texto = "Actual",
            fondo = paleta.seleccionFondo,
            colorTexto = paleta.seleccionTexto,
            onClick = null,
        )
        item.estado == EstadoEnNegocio.DISPONIBLE -> NegocioAccion(
            texto = "Entrar ›",
            fondo = paleta.verde,
            colorTexto = paleta.verdeTexto,
            onClick = onEntrar,
        )
        item.estado == EstadoEnNegocio.PAUSADO -> NegocioAccion(
            texto = "Ver estado",
            fondo = paleta.ambar,
            colorTexto = paleta.ambarTexto,
            onClick = onVerEstado,
        )
        else -> Unit
    }
}

@Composable
private fun PieDeEstado(
    paleta: TokePaleta,
    icono: Int,
    texto: String,
    botonTexto: String?,
    botonFondo: Color,
    onBoton: () -> Unit,
) {
    Spacer(modifier = Modifier.height(14.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(paleta.borde),
    )
    Spacer(modifier = Modifier.height(12.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(id = icono),
            contentDescription = null,
            tint = AmbarAviso,
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = texto,
            style = TextStyle(
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
            ),
            color = AmbarAviso,
            modifier = Modifier.weight(1f),
        )
        if (botonTexto != null) {
            Spacer(modifier = Modifier.width(10.dp))
            NegocioAccion(
                texto = botonTexto,
                fondo = botonFondo,
                colorTexto = Color.White,
                onClick = onBoton,
            )
        }
    }
}
