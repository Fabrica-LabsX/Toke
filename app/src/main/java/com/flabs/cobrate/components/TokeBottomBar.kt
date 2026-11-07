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
import androidx.compose.ui.unit.sp
import com.flabs.cobrate.ui.theme.PlusJakartaSans
import androidx.compose.runtime.collectAsState
import com.flabs.cobrate.core.data.PreferenciasAppStore
import com.flabs.cobrate.ui.theme.ThemeMode
import com.flabs.cobrate.core.logic.TabRefreshState
import com.flabs.cobrate.ui.theme.tokeEsOscuro
import com.flabs.cobrate.ui.theme.tokePaleta

private class NavPalette(
    val Fondo: Color,
    val Borde: Color,
    val Activo: Color,
    val Inactivo: Color,
    val FabFondo: Color,
    val FabContenido: Color
)

/** Colores de la barra inferior: salen de la paleta única de la app. */
@Composable
private fun navColors(): NavPalette {
    val p = tokePaleta()
    return NavPalette(
        Fondo = if (tokeEsOscuro()) p.fondo else p.tarjeta,
        Borde = p.borde,
        Activo = p.textoPrincipal,
        Inactivo = p.textoSecundario,
        FabFondo = p.verde,
        FabContenido = p.verdeTexto
    )
}

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

    val colors = navColors()

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
            color = colors.Fondo,
            shadowElevation = 0.dp,
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                HorizontalDivider(
                    thickness = 1.dp,
                    color = colors.Borde,
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
                    ambientColor = colors.FabFondo,
                    spotColor = colors.FabFondo.copy(alpha = 0.35f),
                )
                .border(2.dp, colors.Fondo, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .clickable(
                    interactionSource = interactionSource,
                    indication = ripple(bounded = false),
                    onClick = onRecibir,
                ),
            shape = RoundedCornerShape(12.dp),
            color = colors.FabFondo,
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
                    tint = colors.FabContenido,
                )
                Text(
                    text = "Recibir",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.FabContenido,
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
    val colors = navColors()
    val color = if (isSeleccionado) colors.Activo else colors.Inactivo

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
            style = androidx.compose.ui.text.TextStyle(
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                lineHeight = 16.5.sp,
                letterSpacing = (-0.275).sp
            ),
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
                tint = MaterialTheme.colorScheme.primary
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
