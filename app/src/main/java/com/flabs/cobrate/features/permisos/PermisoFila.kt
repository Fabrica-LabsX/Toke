package com.flabs.cobrate.features.permisos

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flabs.cobrate.components.TokeIcons
import com.flabs.cobrate.ui.theme.PlusJakartaSans
import com.flabs.cobrate.ui.theme.TokePaleta

/** Un permiso de la lista: lo que es, para qué sirve y su estado ("Listo" o el botón "Activar"). */
data class PermisoUi(
    val icono: Int,
    val titulo: String,
    val descripcion: String,
    val concedido: Boolean,
    /** Resalta el icono en ámbar (permiso recomendado que falta). */
    val resaltado: Boolean,
    val onActivar: () -> Unit,
)

/** Tarjeta con la lista de permisos, separados por una línea. */
@Composable
fun PermisosLista(
    paleta: TokePaleta,
    permisos: List<PermisoUi>,
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = paleta.tarjeta,
        border = BorderStroke(1.dp, paleta.borde),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column {
            permisos.forEachIndexed { indice, permiso ->
                if (indice > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(paleta.borde),
                    )
                }
                PermisoFila(paleta, permiso)
            }
        }
    }
}

@Composable
private fun PermisoFila(paleta: TokePaleta, permiso: PermisoUi) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = if (permiso.resaltado) paleta.ambar.copy(alpha = 0.18f) else paleta.tileFondo,
            modifier = Modifier.size(48.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(id = permiso.icono),
                    contentDescription = null,
                    tint = if (permiso.resaltado) Color(0xFFB45309) else paleta.textoPrincipal,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = permiso.titulo,
                style = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                ),
                color = paleta.textoPrincipal,
            )
            Text(
                text = permiso.descripcion,
                style = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.5.sp,
                    lineHeight = 19.sp,
                ),
                color = paleta.textoSecundario,
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        if (permiso.concedido) {
            ChipListo(paleta)
        } else {
            BotonActivar(paleta, permiso.onActivar)
        }
    }
}

@Composable
private fun ChipListo(paleta: TokePaleta) {
    Surface(shape = RoundedCornerShape(9999.dp), color = paleta.activo.copy(alpha = 0.14f)) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(id = TokeIcons.CheckVerde),
                contentDescription = null,
                tint = paleta.activo,
                modifier = Modifier.size(14.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Listo",
                style = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                ),
                color = paleta.activo,
            )
        }
    }
}

@Composable
private fun BotonActivar(paleta: TokePaleta, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(9999.dp),
        color = paleta.seleccionFondo,
    ) {
        Text(
            text = "Activar",
            style = TextStyle(
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
            ),
            color = paleta.seleccionTexto,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
        )
    }
}

/** Nota de privacidad al pie. Con [conTitulo] muestra "Aviso Previo" sobre el texto. */
@Composable
fun PermisosAviso(
    paleta: TokePaleta,
    conTitulo: Boolean,
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = paleta.tarjeta,
        border = BorderStroke(1.dp, paleta.borde),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = paleta.tileFondo,
                modifier = Modifier.size(36.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(id = TokeIcons.EscudoVerificado),
                        contentDescription = null,
                        tint = paleta.textoPrincipal,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                if (conTitulo) {
                    Text(
                        text = "Aviso Previo",
                        style = TextStyle(
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                        ),
                        color = paleta.textoPrincipal,
                    )
                }
                Text(
                    text = "Solo se analizan avisos de pago legítimos. TOKE nunca lee chats personales ni contraseñas.",
                    style = TextStyle(
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.5.sp,
                        lineHeight = 19.sp,
                    ),
                    color = paleta.textoSecundario,
                )
            }
        }
    }
}
