package com.flabs.cobrate.components
import com.flabs.cobrate.ui.theme.TokeMedidas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flabs.cobrate.ui.theme.PlusJakartaSans
import com.flabs.cobrate.ui.theme.TokePaleta

private val RojoPausa = TokeRojo

/** Icono grande de pausa en un cuadro rosado, con el punto rojo de alerta. */
@Composable
fun AccesoPausadoIcono(paleta: TokePaleta) {
    Box(modifier = Modifier.size(104.dp)) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = RojoPausa.copy(alpha = 0.12f),
            modifier = Modifier
                .align(Alignment.Center)
                .size(92.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(id = TokeIcons.AccesoPausado),
                    contentDescription = null,
                    tint = RojoPausa,
                    modifier = Modifier.size(48.dp),
                )
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(18.dp)
                .clip(CircleShape)
                .background(RojoPausa),
        )
    }
}

/** Tarjeta "Estado en el negocio": nombre del negocio, estado pausado y qué hacer. */
@Composable
fun AccesoPausadoEstado(
    paleta: TokePaleta,
    negocioNombre: String,
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = paleta.tarjeta,
        border = BorderStroke(1.dp, paleta.borde),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = paleta.seleccionFondo,
                    modifier = Modifier.size(46.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(id = TokeIcons.Tienda),
                            contentDescription = null,
                            tint = paleta.seleccionTexto,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "ESTADO EN EL NEGOCIO",
                        style = TextStyle(
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            letterSpacing = 0.8.sp,
                        ),
                        color = paleta.textoSecundario,
                    )
                    Text(
                        text = "PAUSADO",
                        style = TextStyle(
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                        ),
                        color = paleta.textoPrincipal,
                    )
                    Text(
                        text = negocioNombre,
                        style = TextStyle(
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                        ),
                        color = paleta.textoSecundario,
                        maxLines = 1,
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(paleta.borde),
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Si consideras que es un error, comunícate directamente con el administrador de la tienda.",
                style = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                ),
                color = paleta.textoSecundario,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Surface(shape = RoundedCornerShape(9999.dp), color = paleta.tileFondo) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(id = TokeIcons.CajaRegistradora),
                        contentDescription = null,
                        tint = paleta.activo,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Esperando autorización",
                        style = TextStyle(
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                        ),
                        color = paleta.textoPrincipal,
                    )
                }
            }
        }
    }
}

/** Botón grande del pie. [relleno] lo pinta sólido; si no, lleva solo borde. */
@Composable
fun AccesoPausadoBoton(
    paleta: TokePaleta,
    texto: String,
    icono: Int,
    relleno: Boolean,
    onClick: () -> Unit,
) {
    val fondo = if (relleno) paleta.verde else paleta.tarjeta
    val colorContenido = if (relleno) paleta.verdeTexto else paleta.textoPrincipal
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(9999.dp),
        color = fondo,
        border = if (relleno) null else BorderStroke(1.dp, paleta.bordeControl),
        modifier = Modifier
            .fillMaxWidth()
            .height(TokeMedidas.BotonAltura),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                Icon(
                    painter = painterResource(id = icono),
                    contentDescription = null,
                    tint = if (relleno) Color.White else paleta.textoSecundario,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = texto,
                    style = TextStyle(
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                    ),
                    color = colorContenido,
                    maxLines = 1,
                )
            }
        }
    }
}

/** Acción discreta en texto, para lo que no es lo principal (cerrar sesión, salir, crear propio). */
@Composable
fun AccesoPausadoEnlace(
    paleta: TokePaleta,
    texto: String,
    icono: Int?,
    onClick: () -> Unit,
) {
    Surface(onClick = onClick, color = Color.Transparent, shape = RoundedCornerShape(12.dp)) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icono != null) {
                Icon(
                    painter = painterResource(id = icono),
                    contentDescription = null,
                    tint = paleta.textoSecundario,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = texto,
                style = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                ),
                color = paleta.textoSecundario,
            )
        }
    }
}
