package com.flabs.cobrate.features.negocios
import com.flabs.cobrate.ui.theme.TokeRojo
import com.flabs.cobrate.ui.theme.TokeAmbarTexto

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flabs.cobrate.components.TokeIcons
import com.flabs.cobrate.ui.theme.PlusJakartaSans
import com.flabs.cobrate.ui.theme.TokePaleta

/**
 * Piezas visuales que comparten "Mis Negocios" y "Cambiar de negocio": el cuadro con el
 * icono del rubro y la etiqueta de rol/estado.
 */

/** Cuadro con el icono del negocio. El negocio actual lleva el fondo verde. */
@Composable
fun NegocioTile(
    paleta: TokePaleta,
    item: NegocioItem,
    tamano: Dp,
    modifier: Modifier = Modifier,
) {
    val esActual = item.esActual
    // El color de marca que eligió el dueño: fondo del negocio actual e icono de los demás.
    val colorMarca = item.negocio.colorMarca?.let {
        com.flabs.cobrate.features.crearnegocio.CrearNegocioLogica.argbDeHex(it)?.let { argb -> androidx.compose.ui.graphics.Color(argb) }
    }
    val fondo = if (esActual) (colorMarca ?: paleta.activo) else paleta.tileFondo
    val colorIcono = when {
        esActual -> if (colorMarca != null) androidx.compose.ui.graphics.Color.White else paleta.verdeTexto
        item.estado == EstadoEnNegocio.PENDIENTE -> TokeAmbarTexto
        else -> colorMarca ?: paleta.textoPrincipal
    }
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = fondo,
        border = if (esActual) null else BorderStroke(1.dp, paleta.bordeControl),
        modifier = modifier.size(tamano),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(id = iconoDeNegocio(item.negocio.tipo)),
                contentDescription = null,
                tint = colorIcono,
                modifier = Modifier.size(tamano * 0.5f),
            )
        }
    }
}

/** Etiqueta de rol o de estado: "Dueño", "Colaborador", "Pausado" o "Pendiente de aprobación". */
@Composable
fun NegocioEtiqueta(
    paleta: TokePaleta,
    item: NegocioItem,
    modifier: Modifier = Modifier,
) {
    val (fondo, colorTexto, texto) = when {
        item.estado == EstadoEnNegocio.PENDIENTE ->
            Triple(paleta.ambar, paleta.ambarTexto, "Pendiente de aprobación")
        item.estado == EstadoEnNegocio.PAUSADO ->
            Triple(paleta.ambar, paleta.ambarTexto, "Pausado")
        item.rol == RolEnNegocio.DUENO ->
            Triple(paleta.verde, paleta.verdeTexto, "Dueño")
        else ->
            Triple(paleta.bordeControl, paleta.textoPrincipal, "Colaborador")
    }
    Surface(shape = RoundedCornerShape(9999.dp), color = fondo, modifier = modifier) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = texto,
                style = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                ),
                color = colorTexto,
                maxLines = 1,
            )
        }
    }
}

/** Píldora de acción de una tarjeta ("Actual", "Entrar ›", "Ver estado", "Cancelar"). */
@Composable
fun NegocioAccion(
    texto: String,
    fondo: Color,
    colorTexto: Color,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val contenido: @Composable () -> Unit = {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
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
    if (onClick != null) {
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(9999.dp),
            color = fondo,
            modifier = modifier,
        ) { contenido() }
    } else {
        Surface(shape = RoundedCornerShape(9999.dp), color = fondo, modifier = modifier) { contenido() }
    }
}
