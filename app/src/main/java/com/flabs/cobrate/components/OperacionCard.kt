package com.flabs.cobrate.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import com.flabs.cobrate.ui.theme.ThemeMode
import com.flabs.cobrate.ui.theme.tokeEsOscuro
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flabs.cobrate.core.model.EstadoOperacion
import com.flabs.cobrate.core.model.Operacion
import com.flabs.cobrate.core.model.TipoMetodoCobro
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Card reutilizable de Operaciones conforme a la especificación visual TOKE! en Figma (Nodo 7:2990).
 *
 * @param operacion Datos de la operación.
 * @param mostrarId Si se debe incluir la referencia/ID de la operación.
 * @param accion Acción opcional contenida en la tarjeta.
 * @param asignadoPorNombre Nombre asignado o concepto de la transacción.
 */
@Composable
fun OperacionCard(
    operacion: Operacion,
    modifier: Modifier = Modifier,
    mostrarId: Boolean = false,
    accion: OperacionCardAccion? = null,
    asignadoPorNombre: String? = null,
    onClick: (() -> Unit)? = null,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 94.dp)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                // Sección Izquierda: Avatar (40x40) + Textos + Badge
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Avatar de 40x40 con fondo según medio de pago y logo oficial
                    AvatarMedioPago(metodo = operacion.metodoPago)

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        val nombreMostrar = when {
                            !asignadoPorNombre.isNullOrBlank() -> asignadoPorNombre
                            else -> com.flabs.cobrate.features.operaciones.TextosCobro.titulo(operacion.pagador)
                        }

                        Text(
                            text = nombreMostrar,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        val metodoTexto = operacion.metodoPago?.name?.lowercase()
                            ?.replaceFirstChar { it.uppercase() } ?: "Pago"
                        val horaTexto = formatearHora(operacion.fechaHora)

                        Text(
                            text = "$metodoTexto · $horaTexto",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        EstadoBadge(estado = operacion.estado)
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Sección Derecha: Monto + Chevron
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "S/ %.2f".format(Locale.US, operacion.monto),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.outline,
                    )
                }
            }

            // Acción opcional
            if (accion != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                        onClick = accion.onClick,
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text(text = accion.etiqueta, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

/**
 * Avatar de 40x40dp con color de fondo oficial según medio de pago (Figma Sección 4)
 * y el logotipo oficial centrado.
 */
@Composable
private fun AvatarMedioPago(metodo: TipoMetodoCobro?) {
    val aspecto = if (tokeEsOscuro()) ThemeMode.DARK else ThemeMode.LIGHT
    val fondoColor = colorBilletera(metodo, aspecto) ?: when (metodo) {
        TipoMetodoCobro.INTERBANK -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.primaryContainer
    }

    Surface(
        modifier = Modifier.size(40.dp),
        shape = RoundedCornerShape(4.dp),
        color = fondoColor,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize(),
        ) {
            if (metodo != null) {
                IconoMetodoPago(
                    tipo = metodo,
                    tamano = 24.dp,
                )
            } else {
                Text(
                    text = "OP",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}

/**
 * Badge de estado adaptativo conforme a Figma (Nodo 7:3000 claro / 7:3209 oscuro).
 */
@Composable
private fun EstadoBadge(estado: EstadoOperacion) {
    val (bgColor, borderColor, textColor, label) = when (estado) {
        EstadoOperacion.SIN_ASIGNAR -> Quadruple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.outline,
            MaterialTheme.colorScheme.onSurfaceVariant,
            "Sin asignar",
        )
        else -> Quadruple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.primary,
            "Recibido",
        )
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
            ),
            color = textColor,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 2.dp),
        )
    }
}

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
)

private fun formatearHora(fechaHora: Long): String {
    // Con el día si el cobro no es de hoy, para que un cobro viejo no parezca de hoy.
    return com.flabs.cobrate.core.logic.FechaCobro.texto(fechaHora, System.currentTimeMillis())
}

/** Acción opcional dentro de la card. */
data class OperacionCardAccion(
    val etiqueta: String,
    val onClick: () -> Unit,
)
