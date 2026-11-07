package com.flabs.cobrate.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.flabs.cobrate.ui.theme.tokePaleta

/**
 * Badge del estado del servicio de TOKE, junto al de conexión (mismo tamaño y forma):
 *  - [operativo]: fondo verde con el icono de sincronización y el check verde;
 *  - si no: fondo rojo suave con el icono de sincronización en rojo, sin check.
 * Al tocarlo se abre la pantalla de permisos ([onClick]).
 */
@Composable
fun BadgeSincronizacion(
    operativo: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val paleta = tokePaleta()
    val fondo = when {
        operativo -> paleta.verde
        else -> paleta.rojoFondo
    }
    val borde = when {
        operativo -> paleta.verde
        else -> paleta.rojoBorde
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = fondo,
        border = BorderStroke(1.dp, borde),
        modifier = modifier.size(36.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(id = if (operativo) TokeIcons.SyncOk else TokeIcons.SyncError),
                contentDescription = if (operativo) {
                    "Servicio activo. Ver permisos"
                } else {
                    "Servicio con problemas. Ver permisos"
                },
                tint = Color.Unspecified,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
