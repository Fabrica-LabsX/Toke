package com.flabs.cobrate.features.operaciones

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flabs.cobrate.components.IconoMetodoPago
import com.flabs.cobrate.components.TokeIcons
import com.flabs.cobrate.core.model.Operacion
import com.flabs.cobrate.core.model.TipoMetodoCobro
import com.flabs.cobrate.ui.theme.PlusJakartaSans
import com.flabs.cobrate.ui.theme.TokePaleta
import java.text.SimpleDateFormat
import java.util.Locale

/** Fila de una operación por asignar: logo, referencia, monto y botón "Asignar Cobro". */
@Composable
fun OperacionPendienteFila(
    paleta: TokePaleta,
    operacion: Operacion,
    onAsignar: () -> Unit,
) {
    TarjetaFila(paleta = paleta, onClick = null) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IdentidadOperacion(
                paleta = paleta,
                operacion = operacion,
                detalle = "${nombreMetodo(operacion.metodoPago)} · ${tiempoRelativo(operacion.fechaHora)}",
                modifier = Modifier.weight(1f),
            )
            Column(horizontalAlignment = Alignment.End) {
                MontoTexto(paleta = paleta, operacion = operacion)
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    onClick = onAsignar,
                    shape = RoundedCornerShape(9999.dp),
                    color = paleta.ambar,
                ) {
                    Text(
                        text = "Asignar Cobro",
                        style = TextStyle(
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                        ),
                        color = paleta.ambarTexto,
                        maxLines = 1,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    )
                }
            }
        }
    }
}

/** Fila de un cobro recibido: logo, referencia, hora, monto y flecha de detalle. */
@Composable
fun OperacionRecibidaFila(
    paleta: TokePaleta,
    operacion: Operacion,
    onClick: () -> Unit,
) {
    TarjetaFila(paleta = paleta, onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IdentidadOperacion(
                paleta = paleta,
                operacion = operacion,
                detalle = "${nombreMetodo(operacion.metodoPago)} · ${horaDelDia(operacion.fechaHora)}",
                modifier = Modifier.weight(1f),
            )
            MontoTexto(paleta = paleta, operacion = operacion)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                painter = painterResource(id = TokeIcons.ChevronDerecha),
                contentDescription = null,
                tint = paleta.textoSecundario,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}

@Composable
private fun TarjetaFila(
    paleta: TokePaleta,
    onClick: (() -> Unit)?,
    contenido: @Composable () -> Unit,
) {
    val forma = RoundedCornerShape(14.dp)
    val borde = BorderStroke(1.dp, paleta.borde)
    if (onClick != null) {
        Surface(
            onClick = onClick,
            shape = forma,
            color = paleta.tarjeta,
            border = borde,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(12.dp)) { contenido() }
        }
    } else {
        Surface(
            shape = forma,
            color = paleta.tarjeta,
            border = borde,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(12.dp)) { contenido() }
        }
    }
}

@Composable
private fun IdentidadOperacion(
    paleta: TokePaleta,
    operacion: Operacion,
    detalle: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        IconoMetodoPago(
            tipo = operacion.metodoPago ?: TipoMetodoCobro.TRANSFERENCIA,
            tamano = 44.dp,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = TextosCobro.titulo(operacion.pagador),
                style = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                ),
                color = paleta.textoPrincipal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = detalle,
                style = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                ),
                color = paleta.textoSecundario,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun MontoTexto(paleta: TokePaleta, operacion: Operacion) {
    Text(
        text = "S/ %.2f".format(Locale.US, operacion.monto),
        style = TextStyle(
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 16.sp,
        ),
        color = paleta.textoPrincipal,
        maxLines = 1,
    )
}

private fun nombreMetodo(metodo: TipoMetodoCobro?): String =
    metodo?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Pago"

private fun horaDelDia(fechaHora: Long): String =
    SimpleDateFormat("hh:mm a", Locale.forLanguageTag("es-PE")).format(java.util.Date(fechaHora))

private fun tiempoRelativo(fechaHora: Long): String {
    val minutos = ((System.currentTimeMillis() - fechaHora) / (1000 * 60)).coerceAtLeast(1)
    return if (minutos < 60) "Hace $minutos min" else "Hace ${minutos / 60} h"
}
