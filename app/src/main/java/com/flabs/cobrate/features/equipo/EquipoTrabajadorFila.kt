package com.flabs.cobrate.features.equipo
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flabs.cobrate.components.TokeIcons
import com.flabs.cobrate.components.inicialesDe
import com.flabs.cobrate.core.model.EstadoRelacion
import com.flabs.cobrate.features.business.TrabajadorRealInfo
import com.flabs.cobrate.ui.theme.PlusJakartaSans
import com.flabs.cobrate.ui.theme.TokePaleta

private val RojoExpulsar = TokeRojo

/**
 * Fila de un colaborador: iniciales, nombre, estado y, si quien mira puede administrar,
 * el botón Pausar/Reanudar y el menú con "Expulsar".
 */
@Composable
fun EquipoTrabajadorFila(
    paleta: TokePaleta,
    info: TrabajadorRealInfo,
    puedeAdministrar: Boolean,
    onPausarReanudar: () -> Unit,
    onExpulsar: () -> Unit,
) {
    val esActivo = info.estado == EstadoRelacion.ACTIVA

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = paleta.tarjeta,
        border = BorderStroke(1.dp, paleta.borde),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Iniciales(paleta = paleta, nombre = info.username)
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = info.username,
                    style = TextStyle(
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp,
                    ),
                    color = paleta.textoPrincipal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (info.estado == EstadoRelacion.PAUSADA) {
                    EstadoChip(paleta = paleta, estado = info.estado)
                }
            }
            if (puedeAdministrar) {
                Spacer(modifier = Modifier.width(10.dp))
                BotonPausarReanudar(paleta = paleta, esActivo = esActivo, onClick = onPausarReanudar)
                Spacer(modifier = Modifier.width(8.dp))
                MenuMas(paleta = paleta, onExpulsar = onExpulsar)
            }
        }
    }
}

@Composable
private fun Iniciales(paleta: TokePaleta, nombre: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = paleta.tileFondo,
        border = BorderStroke(1.dp, paleta.bordeControl),
        modifier = Modifier.size(46.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = inicialesDe(nombre),
                style = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                ),
                color = paleta.textoPrincipal,
            )
        }
    }
}

@Composable
private fun EstadoChip(paleta: TokePaleta, estado: EstadoRelacion) {
    val (fondo, colorTexto, texto) = when (estado) {
        EstadoRelacion.PAUSADA -> Triple(paleta.ambar, paleta.ambarTexto, "En pausa")
        else -> Triple(paleta.tileFondo, paleta.textoSecundario, estado.name)
    }
    Surface(shape = RoundedCornerShape(9999.dp), color = fondo) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (estado == EstadoRelacion.PAUSADA) {
                Icon(
                    painter = painterResource(id = TokeIcons.Pausa),
                    contentDescription = null,
                    tint = colorTexto,
                    modifier = Modifier.size(11.dp),
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = texto,
                style = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                ),
                color = colorTexto,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun BotonPausarReanudar(paleta: TokePaleta, esActivo: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (esActivo) paleta.tileFondo else paleta.ambar,
        border = if (esActivo) BorderStroke(1.dp, paleta.bordeControl) else null,
        modifier = Modifier.height(40.dp),
    ) {
        Box(modifier = Modifier.padding(horizontal = 14.dp), contentAlignment = Alignment.Center) {
            Text(
                text = if (esActivo) "Pausar" else "Reanudar",
                style = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                ),
                color = if (esActivo) paleta.textoPrincipal else paleta.ambarTexto,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun MenuMas(paleta: TokePaleta, onExpulsar: () -> Unit) {
    var abierto by remember { mutableStateOf(false) }
    Box {
        Surface(
            onClick = { abierto = true },
            shape = RoundedCornerShape(12.dp),
            color = paleta.tileFondo,
            border = BorderStroke(1.dp, paleta.bordeControl),
            modifier = Modifier.size(40.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(id = TokeIcons.MenuVertical),
                    contentDescription = "Más acciones",
                    tint = paleta.textoSecundario,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
        DropdownMenu(
            expanded = abierto,
            onDismissRequest = { abierto = false },
            containerColor = paleta.tarjeta,
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        text = "Expulsar",
                        style = TextStyle(
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                        ),
                        color = RojoExpulsar,
                    )
                },
                onClick = {
                    abierto = false
                    onExpulsar()
                },
            )
        }
    }
}
