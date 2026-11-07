package com.flabs.cobrate.features.permisos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flabs.cobrate.components.TokeIcons
import com.flabs.cobrate.ui.theme.PlusJakartaSans
import com.flabs.cobrate.ui.theme.TokePaleta

private val Rojo = Color(0xFFDC2626)

/** Tarjeta grande de estado del servicio: verde (operativo), ámbar (parcial) o roja (desactivado). */
@Composable
fun PermisosBanner(
    paleta: TokePaleta,
    estado: EstadoServicio,
) {
    val datos = when (estado) {
        EstadoServicio.OPERATIVO -> DatosBanner(
            color = paleta.activo,
            icono = TokeIcons.Check,
            titulo = "Sincronización activa",
            mensaje = "¡Genial!, TOKE está listo para recibir cobros y avisar a tu equipo en tiempo real.",
            etiqueta = null,
        )
        EstadoServicio.PARCIAL -> DatosBanner(
            color = paleta.ambar,
            icono = TokeIcons.Advertencia,
            titulo = "Avisos en pausa al bloquear",
            mensaje = "TOKE funciona, pero dejará de enviar alertas cuando la pantalla se apague.",
            etiqueta = "Atención",
        )
        EstadoServicio.DESACTIVADO -> DatosBanner(
            color = Rojo,
            icono = TokeIcons.Bloqueado,
            titulo = "Sincronización desactivada",
            mensaje = "TOKE no está funcionando correctamente por los siguientes problemas.",
            etiqueta = "Detenido",
        )
    }

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = datos.color.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, datos.color.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = datos.color,
                modifier = Modifier.size(48.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(id = datos.icono),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(26.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = datos.titulo,
                    style = TextStyle(
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 19.sp,
                    ),
                    color = paleta.textoPrincipal,
                )
                Text(
                    text = datos.mensaje,
                    style = TextStyle(
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                    ),
                    color = paleta.textoPrincipal,
                )
            }
            if (datos.etiqueta != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(9999.dp),
                    color = paleta.tarjeta,
                    border = BorderStroke(1.dp, datos.color.copy(alpha = 0.5f)),
                ) {
                    Text(
                        text = datos.etiqueta,
                        style = TextStyle(
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                        ),
                        color = if (estado == EstadoServicio.PARCIAL) Color(0xFFB45309) else Rojo,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
            }
        }
    }
}

private data class DatosBanner(
    val color: Color,
    val icono: Int,
    val titulo: String,
    val mensaje: String,
    val etiqueta: String?,
)
