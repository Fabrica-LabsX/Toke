package com.flabs.cobrate.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.flabs.cobrate.R
import com.flabs.cobrate.core.model.TipoMetodoCobro

/**
 * Componente único y reutilizable que muestra el logo oficial de un método de cobro (C10).
 * Si el logo no existe, muestra un respaldo tipográfico neutro.
 */
@Composable
fun IconoMetodoPago(
    tipo: TipoMetodoCobro,
    modifier: Modifier = Modifier,
    tamano: Dp = 40.dp
) {
    val resId = when (tipo) {
        TipoMetodoCobro.YAPE -> R.drawable.ic_pago_yape
        TipoMetodoCobro.PLIN -> R.drawable.ic_pago_plin
        TipoMetodoCobro.BCP -> R.drawable.ic_pago_bcp
        TipoMetodoCobro.INTERBANK -> R.drawable.ic_pago_interbank
        TipoMetodoCobro.TRANSFERENCIA,
        TipoMetodoCobro.BIM,
        TipoMetodoCobro.AGORA_TUNKI,
        TipoMetodoCobro.BBVA,
        TipoMetodoCobro.BANBIF,
        TipoMetodoCobro.LIGO,
        TipoMetodoCobro.SCOTIABANK,
        TipoMetodoCobro.PICHINCHA,
        TipoMetodoCobro.SIP,
        TipoMetodoCobro.LEMON_CASH -> null
    }

    Box(
        modifier = modifier.size(tamano),
        contentAlignment = Alignment.Center
    ) {
        if (resId != null) {
            Image(
                painter = painterResource(id = resId),
                contentDescription = tipo.name.lowercase().replaceFirstChar { it.uppercase() },
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Respaldo tipográfico (ausencia declarada)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tipo.name.take(1),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
