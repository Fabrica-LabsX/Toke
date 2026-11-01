package com.flabs.cobrate.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.annotation.DrawableRes
import com.flabs.cobrate.core.logic.TabRefreshState
import com.flabs.cobrate.ui.theme.TokeBorder
import com.flabs.cobrate.ui.theme.TokeGreen

data class ItemNav(
    val id: String,
    val label: String,
    @DrawableRes val icono: Int,
)

/**
 * Barra de navegación inferior persistente con FAB central.
 * Lenguaje Visual TOKE! (Estilo Waze / Stitch - Capa 1).
 */
@Composable
fun TokeBottomBar(
    items: List<ItemNav>,
    idSeleccionado: String?,
    onSeleccionar: (String) -> Unit,
    onRecibir: () -> Unit,
    onRefrescarTab: (String) -> Unit = {},
    refreshStates: Map<String, TabRefreshState> = emptyMap(),
    modifier: Modifier = Modifier,
    insigniaEquipo: Int = 0,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val fabScale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1.0f,
        animationSpec = tween(durationMillis = 100),
        label = "fab_press_scale",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .height(82.dp),
    ) {
        // Fondo de la barra con borde superior sutil
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(64.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 0.dp,
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                HorizontalDivider(
                    thickness = 1.dp,
                    color = TokeBorder,
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Primeros dos items (Inicio, Mis Cobros)
                    items.take(2).forEach { item ->
                        val state = refreshStates[item.id] ?: TabRefreshState.IDLE
                        BottomNavItem(
                            item = item,
                            isSeleccionado = item.id == idSeleccionado,
                            refreshState = state,
                            onClick = {
                                if (item.id == idSeleccionado && item.id != "MAS") {
                                    onRefrescarTab(item.id)
                                } else {
                                    onSeleccionar(item.id)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            insignia = if (item.id.contains("TRABAJADORES", ignoreCase = true)) insigniaEquipo else 0,
                        )
                    }

                    // Espacio central para el botón FAB "Recibir"
                    Spacer(modifier = Modifier.width(72.dp))

                    // Últimos dos items (Equipo, Más)
                    items.takeLast(2).forEach { item ->
                        val state = refreshStates[item.id] ?: TabRefreshState.IDLE
                        BottomNavItem(
                            item = item,
                            isSeleccionado = item.id == idSeleccionado,
                            refreshState = state,
                            onClick = {
                                if (item.id == idSeleccionado && item.id != "MAS") {
                                    onRefrescarTab(item.id)
                                } else {
                                    onSeleccionar(item.id)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            insignia = if (item.id.contains("TRABAJADORES", ignoreCase = true)) insigniaEquipo else 0,
                        )
                    }
                }
            }
        }

        // Botón FAB Central "Recibir" (Conforme a Figma 64x64, radio 12dp)
        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(64.dp)
                .graphicsLayer {
                    scaleX = fabScale
                    scaleY = fabScale
                }
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(12.dp),
                    ambientColor = TokeGreen,
                    spotColor = TokeGreen.copy(alpha = 0.35f),
                )
                .border(2.dp, MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .clickable(
                    interactionSource = interactionSource,
                    indication = ripple(bounded = false),
                    onClick = onRecibir,
                ),
            shape = RoundedCornerShape(12.dp),
            color = TokeGreen,
            shadowElevation = 0.dp,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize(),
            ) {
                Icon(
                    imageVector = Icons.Default.QrCode2,
                    contentDescription = "Recibir",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
                Text(
                    text = "Recibir",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BottomNavItem(
    item: ItemNav,
    isSeleccionado: Boolean,
    refreshState: TabRefreshState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    insignia: Int = 0,
) {
    val color = if (isSeleccionado) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false),
                onClick = onClick,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (insignia > 0) {
            BadgedBox(
                badge = {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                    ) {
                        Text(text = if (insignia > 99) "99+" else insignia.toString())
                    }
                },
            ) {
                RenderIconContent(item = item, refreshState = refreshState, color = color)
            }
        } else {
            RenderIconContent(item = item, refreshState = refreshState, color = color)
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = item.label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSeleccionado) FontWeight.Bold else FontWeight.Medium,
            color = color,
            maxLines = 1,
        )
    }
}

@Composable
private fun RenderIconContent(
    item: ItemNav,
    refreshState: TabRefreshState,
    color: Color
) {
    when (refreshState) {
        TabRefreshState.REFRESHING -> {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary
            )
        }
        TabRefreshState.SUCCESS -> {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Éxito",
                modifier = Modifier.size(20.dp),
                tint = TokeGreen
            )
        }
        TabRefreshState.IDLE -> {
            Icon(
                painter = painterResource(id = item.icono),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = color,
            )
        }
    }
}
