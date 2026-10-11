package com.flabs.cobrate.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flabs.cobrate.core.data.CuentaStore
import com.flabs.cobrate.core.data.PreferenciasAppStore
import com.flabs.cobrate.core.model.Cuenta
import com.flabs.cobrate.core.model.EstadoRelacion
import com.flabs.cobrate.core.model.Negocio
import com.flabs.cobrate.core.model.RelacionCuentaNegocio
import com.flabs.cobrate.features.business.NegocioViewModel
import com.flabs.cobrate.ui.theme.ThemeMode

/**
 * Bottom Sheet "Cambiar de Negocio" (Figma Nodos 1:3, 1:113).
 *
 * Muestra la lista unificada de negocios (dueño y trabajador) permitiendo
 * cambiar de negocio activo de forma rápida con el diseño oficial TOKE!,
 * adaptable automáticamente a Modo Claro y Oscuro.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CambiarNegocioBottomSheet(
    negocioViewModel: NegocioViewModel,
    onDismiss: () -> Unit,
    onCrearNegocio: () -> Unit,
    onUnirmeNegocio: () -> Unit
) {
    val misNegocios by negocioViewModel.misNegocios.collectAsState()
    val misRelaciones by negocioViewModel.misRelaciones.collectAsState()
    val negocioActivoId by negocioViewModel.negocioSeleccionadoId.collectAsState()
    val sesionActual by CuentaStore.sesionActual.collectAsState()
    val cuentasCheadas by CuentaStore.cuentas.collectAsState()
    val currentUid = sesionActual?.id

    val themeMode by PreferenciasAppStore.themeMode.collectAsState()
    val isDark = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    // Colores según tema
    val sheetBg = if (isDark) Color(0xFF14161B) else Color.White
    val handleColor = if (isDark) Color(0xEC525252) else Color(0xFFCBD5E1)
    val titleColor = if (isDark) Color.White else Color(0xFF0F172A)
    val subtitleColor = if (isDark) Color(0xFFA3A3A3) else Color(0xFF64748B)
    val closeBg = if (isDark) Color(0xFF1E222B) else Color(0xFFF1F5F9)
    val closeBorder = if (isDark) Color(0x99404040) else Color(0xFFE2E8F0)
    val dividerColor = if (isDark) Color(0xCC262626) else Color(0xFFF1F5F9)
    val footerDividerColor = if (isDark) Color(0xCC383838) else Color(0xFFE2E8F0)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = sheetBg,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(handleColor)
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            // ---- Encabezado ----
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isDark) "Seleccionar negocio" else "Cambiar de Negocio",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = titleColor
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isDark) "Tus cuentas activas en este dispositivo" else "${misNegocios.size} comercios asociados",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = subtitleColor
                    )
                }
                Surface(
                    onClick = onDismiss,
                    shape = CircleShape,
                    color = closeBg,
                    border = BorderStroke(1.dp, closeBorder),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = subtitleColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = dividerColor, thickness = 1.dp)

            // ---- Lista de Negocios ----
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(misNegocios, key = { it.id }) { negocio ->
                    val esDueno = negocio.dueñoCuentaId == currentUid
                    val relacion = misRelaciones.find { it.negocioId == negocio.id }
                    val esActivo = negocio.id == negocioActivoId

                    val nombreDueno = remember(negocio.dueñoCuentaId, cuentasCheadas, currentUid) {
                        resolverNombreDueno(
                            duenoCuentaId = negocio.dueñoCuentaId,
                            currentUid = currentUid,
                            cuentasCheadas = cuentasCheadas,
                            onCargarFaltante = { negocioViewModel.cargarCuentasFaltantes(listOf(it)) }
                        )
                    }

                    ItemNegocioSheet(
                        negocio = negocio,
                        esDueno = esDueno,
                        relacion = relacion,
                        esActivo = esActivo,
                        nombreDueno = nombreDueno,
                        isDark = isDark,
                        onClick = {
                            if (esDueno || relacion?.estado == EstadoRelacion.ACTIVA) {
                                negocioViewModel.seleccionarNegocio(negocio.id)
                                onDismiss()
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = footerDividerColor, thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // ---- Pie / Botones de Acción ----
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isDark) {
                    // Botón único unificado en tema oscuro según Figma 1:113
                    Surface(
                        onClick = {
                            onDismiss()
                            onCrearNegocio()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0x991A1D24),
                        border = BorderStroke(1.dp, Color(0xFF525252))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = Color(0xFF10B968),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Agregar otro negocio o código",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                } else {
                    // Acciones separadas en tema claro
                    Surface(
                        onClick = {
                            onDismiss()
                            onCrearNegocio()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFF00A651)),
                        shadowElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = Color(0xFF00A651),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "+ Vincular nuevo negocio",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00A651)
                            )
                        }
                    }

                    Surface(
                        onClick = {
                            onDismiss()
                            onUnirmeNegocio()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = null,
                                tint = Color(0xFF334155),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "+ Tengo un código de invitación",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF334155)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ItemNegocioSheet(
    negocio: Negocio,
    esDueno: Boolean,
    relacion: RelacionCuentaNegocio?,
    esActivo: Boolean,
    nombreDueno: String,
    isDark: Boolean,
    onClick: () -> Unit
) {
    val esPausado = !esDueno && relacion?.estado == EstadoRelacion.PAUSADA
    val esSolicitado = !esDueno && relacion?.estado == EstadoRelacion.SOLICITADA
    val esClicable = esDueno || relacion?.estado == EstadoRelacion.ACTIVA

    // Tokens adaptativos por tema
    val cardBg = when {
        isDark && esActivo -> Color(0xFF1B221C)
        isDark -> Color(0xFF1A1D24)
        else -> Color.White
    }

    val cardBorder = when {
        esActivo -> BorderStroke(2.dp, if (isDark) Color(0xFF10B968) else Color(0xFF00A651))
        isDark -> BorderStroke(1.dp, Color(0xCC404040))
        else -> BorderStroke(1.dp, Color(0xFFE2E8F0))
    }

    val textPrimary = if (isDark) Color.White else Color(0xFF0F172A)

    Surface(
        onClick = onClick,
        enabled = esClicable,
        shape = RoundedCornerShape(16.dp),
        color = cardBg,
        border = cardBorder,
        shadowElevation = if (isDark) 0.dp else 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Ícono de negocio
                val (iconBgColor, iconBorderColor, iconTint) = when {
                    esActivo -> Triple(
                        if (isDark) Color(0xFF008740) else Color(0xFF00A651),
                        Color.Transparent,
                        Color.White
                    )
                    esSolicitado || esPausado -> Triple(
                        if (isDark) Color(0x26F59E0B) else Color(0xFFFFFBEB),
                        if (isDark) Color(0x66F59E0B) else Color(0xFFFDE68A),
                        Color(0xFFD97706)
                    )
                    else -> Triple(
                        if (isDark) Color(0xFF262A34) else Color(0xFFF1F5F9),
                        if (isDark) Color(0xFF404040) else Color(0xFFE2E8F0),
                        if (isDark) Color(0xFFD4D4D4) else Color(0xFF334155)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = iconBgColor,
                    border = BorderStroke(1.dp, iconBorderColor),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (esSolicitado || esPausado) Icons.Default.ShoppingBasket else if (esDueno) Icons.Default.Storefront else Icons.Default.Badge,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = negocio.nombre,
                        fontSize = 15.sp,
                        fontWeight = if (esActivo) FontWeight.ExtraBold else FontWeight.Bold,
                        color = textPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    if (esActivo) {
                        val rolTexto = if (esDueno) "Dueño" else "Trabajador"
                        val badgeBg = if (isDark) Color(0xFF004F24) else Color(0xFFECFDF5)
                        val badgeBorder = if (isDark) Color(0x8010B968) else Color(0xFFA7F3D0)
                        val badgeText = if (isDark) Color(0xFF5ADF82) else Color(0xFF008740)

                        Surface(
                            shape = CircleShape,
                            color = badgeBg,
                            border = BorderStroke(1.dp, badgeBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (isDark) Color(0xFF5ADF82) else Color(0xFF00A651))
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isDark) rolTexto else "ACTUAL · $rolTexto",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = badgeText
                                )
                            }
                        }
                    } else if (esSolicitado) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = if (isDark) Color(0x26F59E0B) else Color(0xFFFEF3C7),
                                border = BorderStroke(1.dp, if (isDark) Color(0x66F59E0B) else Color(0xFFFDE68A))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFD97706))
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Pendiente de aprobación",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) Color(0xFFF59E0B) else Color(0xFFB45309)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Trabajador",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isDark) Color(0xFFA3A3A3) else Color(0xFF64748B)
                            )
                        }
                    } else if (esPausado) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (isDark) Color(0x26F59E0B) else Color(0xFFFEF3C7),
                                    border = BorderStroke(1.dp, if (isDark) Color(0x66F59E0B) else Color(0xFFFDE68A))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFD97706))
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "PAUSADO",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDark) Color(0xFFF59E0B) else Color(0xFFB45309)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Trabajador",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isDark) Color(0xFFA3A3A3) else Color(0xFF64748B)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Pausado por el dueño",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFD97706)
                            )
                        }
                    } else {
                        // Estado ACTIVO inactivo
                        Surface(
                            shape = CircleShape,
                            color = if (isDark) Color(0xFF252932) else Color(0xFFF1F5F9),
                            border = BorderStroke(1.dp, if (isDark) Color(0xFF404040) else Color(0xFFE2E8F0))
                        ) {
                            Text(
                                text = if (esDueno) "Dueño" else "Trabajador",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color(0xFFD4D4D4) else Color(0xFF334155),
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                            )
                        }
                        if (!esDueno) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Dueño: $nombreDueno",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isDark) Color(0xFFA3A3A3) else Color(0xFF64748B)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Acción Derecha
            if (esActivo) {
                Surface(
                    shape = CircleShape,
                    color = if (isDark) Color(0xFF10B968) else Color(0xFF00A651),
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Activo",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            } else if (esClicable) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isDark) Color(0xFF252932) else Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, if (isDark) Color(0xFF404040) else Color(0xFFE2E8F0))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Entrar",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color(0xFFD4D4D4) else Color(0xFF334155)
                        )
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = if (isDark) Color(0xFFD4D4D4) else Color(0xFF334155),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            } else {
                Surface(
                    shape = CircleShape,
                    color = if (isDark) Color(0xFF1E222B) else Color(0xFFF8FAFC),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = if (isDark) Color(0xFF525252) else Color(0xFF94A3B8),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

internal fun resolverNombreDueno(
    duenoCuentaId: String,
    currentUid: String?,
    cuentasCheadas: List<Cuenta>,
    onCargarFaltante: ((String) -> Unit)? = null
): String {
    if (currentUid != null && duenoCuentaId == currentUid) {
        return "Tú"
    }

    val cuenta = cuentasCheadas.find { it.id == duenoCuentaId }
    if (cuenta == null) {
        onCargarFaltante?.invoke(duenoCuentaId)
        return "Cargando…"
    }

    val username = cuenta.username.trim()
    val correo = cuenta.correoVerificado?.trim()

    return when {
        username.isNotBlank() && username != "Usuario anónimo" && !username.contains("anon") -> username
        !correo.isNullOrBlank() -> correo
        username.isNotBlank() -> username
        else -> "Cargando…"
    }
}
