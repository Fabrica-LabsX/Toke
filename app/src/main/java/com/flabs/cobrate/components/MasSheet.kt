package com.flabs.cobrate.components
import com.flabs.cobrate.ui.theme.TokeRojo
import com.flabs.cobrate.ui.theme.TokeAmbarTexto

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.flabs.cobrate.core.data.CuentaStore
import com.flabs.cobrate.core.data.NegocioStore
import com.flabs.cobrate.core.data.PreferenciasAppStore
import com.flabs.cobrate.core.data.RelacionCuentaNegocioStore
import com.flabs.cobrate.core.model.EstadoRelacion
import com.flabs.cobrate.core.model.RolCuentaNegocio
import com.flabs.cobrate.ui.theme.ThemeMode
import com.flabs.cobrate.ui.theme.tokePaleta
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch

data class ItemMas(
    val label: String,
    val icono: ImageVector,
    val onClick: () -> Unit
)

/**
 * Menú inferior desplegable para opciones adicionales.
 * Separación clara entre destinos secundarios desplazables y pie fijo con acciones inmediatas.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MasSheet(
    items: List<ItemMas>,
    onDismiss: () -> Unit,
    onCerrarSesion: () -> Unit,
    onApagarToke: () -> Unit
) {
    val paleta = tokePaleta()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val scope = rememberCoroutineScope()
    var cerrando by remember { mutableStateOf(false) }

    fun ejecutarConCierre(accion: () -> Unit) {
        if (cerrando) return
        cerrando = true
        scope.launch {
            try {
                sheetState.hide()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // Si hide() falla por otra razón, se continúa con el cierre normal.
            }
            ensureActive()
            onDismiss()
            accion()
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            if (!cerrando) {
                cerrando = true
                onDismiss()
            }
        },
        sheetState = sheetState,
        containerColor = paleta.fondo
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // ---- Zona Central Desplazable ----
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .verticalScroll(
                        state = rememberScrollState(),
                        enabled = sheetState.currentValue == SheetValue.Expanded
                    )
                    .padding(bottom = 8.dp)
            ) {
                // Selector de Tema
                val currentTheme by PreferenciasAppStore.themeMode.collectAsState()

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Aspecto",
                        style = MaterialTheme.typography.labelMedium,
                        color = paleta.textoSecundario,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(paleta.tileFondo),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ThemeOption(
                            label = "Claro",
                            icon = Icons.Default.LightMode,
                            selected = currentTheme == ThemeMode.LIGHT,
                            onClick = { PreferenciasAppStore.setThemeMode(ThemeMode.LIGHT) },
                            modifier = Modifier.weight(1f)
                        )
                        ThemeOption(
                            label = "Oscuro",
                            icon = Icons.Default.DarkMode,
                            selected = currentTheme == ThemeMode.DARK,
                            onClick = { PreferenciasAppStore.setThemeMode(ThemeMode.DARK) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Opción Notificaciones del Dueño
                val sesionActual by CuentaStore.sesionActual.collectAsState()
                val negocioActivoId by NegocioStore.negocioActivoId.collectAsState()
                val relaciones by RelacionCuentaNegocioStore.relaciones.collectAsState()

                val esAdminActivo = remember(sesionActual?.id, negocioActivoId, relaciones) {
                    val uid = sesionActual?.id ?: return@remember false
                    val negId = negocioActivoId ?: return@remember false
                    relaciones.any {
                        it.cuentaId == uid &&
                                it.negocioId == negId &&
                                it.rol == RolCuentaNegocio.ADMINISTRADOR &&
                                it.estado == EstadoRelacion.ACTIVA
                    }
                }

                if (esAdminActivo) {
                    val avisarCobroEnviado by PreferenciasAppStore.avisarCobroEnviado.collectAsState()

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        thickness = 0.5.dp,
                        color = paleta.borde
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Notificaciones del Dueño",
                            style = MaterialTheme.typography.labelMedium,
                            color = paleta.textoSecundario,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                Text(
                                    text = "Avisar cuando un cobro se envía a trabajadores",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = paleta.textoPrincipal
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Te informa cuando TOKE logra enviar el cobro para que quede disponible para los trabajadores.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = paleta.textoSecundario
                                )
                            }
                            Switch(
                                checked = avisarCobroEnviado,
                                onCheckedChange = { enabled ->
                                    PreferenciasAppStore.setAvisarCobroEnviado(enabled)
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    thickness = 0.5.dp,
                    color = paleta.borde
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Rejilla de Destinos Secundarios
                val filas = remember(items) { items.chunked(3) }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    filas.forEach { fila ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            fila.forEach { item ->
                                Box(modifier = Modifier.weight(1f)) {
                                    GridActionCard(
                                        item = item,
                                        onItemClick = { ejecutarConCierre { item.onClick() } }
                                    )
                                }
                            }
                            // Rellenar espacio para mantener ancho consistente si la última fila tiene < 3 elementos
                            repeat(3 - fila.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // ---- Pie Fijo y Siempre Visible ----
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 0.5.dp,
                color = paleta.borde
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { ejecutarConCierre { onCerrarSesion() } },
                    enabled = !cerrando,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, paleta.bordeControl),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = paleta.textoPrincipal)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Cerrar",
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = { ejecutarConCierre { onApagarToke() } },
                    enabled = !cerrando,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TokeRojo,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PowerSettingsNew,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Apagar TOKE",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeOption(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val paleta = tokePaleta()
    val bgColor = if (selected) paleta.seleccionFondo else Color.Transparent
    val contentColor = if (selected) paleta.seleccionTexto else paleta.textoSecundario

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(24.dp))
            .background(bgColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = contentColor
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = contentColor,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
private fun GridActionCard(
    item: ItemMas,
    onItemClick: () -> Unit
) {
    val paleta = tokePaleta()
    Surface(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onItemClick() },
        shape = RoundedCornerShape(16.dp),
        color = paleta.tarjeta,
        border = BorderStroke(1.dp, paleta.borde)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = item.icono,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = paleta.textoPrincipal
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = item.label,
                style = MaterialTheme.typography.labelLarge,
                color = paleta.textoPrincipal,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}
