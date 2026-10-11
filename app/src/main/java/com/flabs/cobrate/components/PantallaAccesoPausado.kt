package com.flabs.cobrate.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flabs.cobrate.R
import com.flabs.cobrate.core.data.PreferenciasAppStore
import com.flabs.cobrate.ui.theme.ThemeMode

/**
 * Pantalla "Acceso Pausado" conforme al diseño oficial de Figma (Nodo 14:410).
 *
 * Se muestra cuando la relación de la cuenta con el negocio activo está en estado PAUSADA.
 */
@Composable
fun PantallaAccesoPausado(
    negocioNombre: String,
    onVerificarEstado: (() -> Unit)? = null,
    onCrearNegocio: (() -> Unit)? = null,
    onCambiarNegocio: (() -> Unit)? = null,
    onCerrarSesion: (() -> Unit)? = null,
    onSalirDelNegocio: (() -> Unit)? = null
) {
    val themeMode by PreferenciasAppStore.themeMode.collectAsState()
    val isDark = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    val bgScreen = if (isDark) Color(0xFF0D1117) else Color(0xFFF4FBF6)
    val cardBg = if (isDark) Color(0xFF1A1F26) else Color.White
    val cardBorder = if (isDark) Color(0xFF2D333B) else Color(0xFFE2E8F0)
    val titleColor = if (isDark) Color.White else Color(0xFF0F172A)
    val subtitleColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgScreen)
    ) {
        FondoPanelBorroso()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ---- Header: NEGOCIO ACTUAL ----
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isDark) Color(0x1A00C853) else Color(0x1A006B32),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = null,
                                tint = if (isDark) Color(0xFF00C853) else Color(0xFF00A651),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = "NEGOCIO ACTUAL",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = subtitleColor,
                            letterSpacing = 0.55.sp
                        )
                        Text(
                            text = negocioNombre,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = titleColor
                        )
                    }
                }

                // ---- Tarjeta Central de Acceso Pausado ----
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = cardBg,
                    border = BorderStroke(1.dp, cardBorder),
                    shadowElevation = if (isDark) 0.dp else 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(21.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Ícono prominente y badge de pausa
                        Box(contentAlignment = Alignment.BottomEnd) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (isDark) Color(0x26F59E0B) else Color(0xFFFFFBEB),
                                border = BorderStroke(1.dp, if (isDark) Color(0x66F59E0B) else Color(0xFFFDE68A)),
                                modifier = Modifier.size(64.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.PauseCircle,
                                        contentDescription = null,
                                        tint = Color(0xFFF59E0B),
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = stringResource(R.string.pausado_titulo),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = titleColor,
                            textAlign = TextAlign.Center,
                            letterSpacing = (-0.5).sp
                        )

                        Text(
                            text = stringResource(R.string.pausado_texto, negocioNombre),
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = subtitleColor,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )

                        // Botón Primario: Verificar si ya me reactivó
                        if (onVerificarEstado != null) {
                            Surface(
                                onClick = onVerificarEstado,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = if (isDark) Color(0xFF00C853) else Color(0xFF00A651),
                                shadowElevation = 2.dp
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        tint = if (isDark) Color.Black else Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = stringResource(R.string.pausado_verificar),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) Color.Black else Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                // ---- Opciones Alternativas ----
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = stringResource(R.string.pausado_opciones_alternativas),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = subtitleColor,
                        letterSpacing = 0.6.sp,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    // Opción 1: Crear mi propio negocio
                    if (onCrearNegocio != null) {
                        Surface(
                            onClick = onCrearNegocio,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(20.dp),
                            color = cardBg,
                            border = BorderStroke(1.dp, cardBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 17.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isDark) Color(0x1A00C853) else Color(0xFFE8F8EE),
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = null,
                                                tint = Color(0xFF00A651),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = stringResource(R.string.pausado_crear_propio),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = titleColor
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = subtitleColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    // Opción 2: Cambiar o unirme a otro negocio
                    if (onCambiarNegocio != null) {
                        Surface(
                            onClick = onCambiarNegocio,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(20.dp),
                            color = cardBg,
                            border = BorderStroke(1.dp, cardBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 17.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isDark) Color(0x1A00C853) else Color(0xFFE4F1E7),
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.SwapHoriz,
                                                contentDescription = null,
                                                tint = Color(0xFF00A651),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = stringResource(R.string.pausado_cambiar_unirme),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = titleColor
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = subtitleColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    // Opción opcional: Salir del negocio
                    if (onSalirDelNegocio != null) {
                        Surface(
                            onClick = onSalirDelNegocio,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            shape = RoundedCornerShape(20.dp),
                            color = cardBg,
                            border = BorderStroke(1.dp, cardBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 17.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = stringResource(R.string.accion_salir_negocio),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }

                // ---- Botón de Cerrar Sesión ----
                if (onCerrarSesion != null) {
                    Surface(
                        onClick = onCerrarSesion,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = if (isDark) Color(0x33DC2626) else Color(0xFFFEF2F2),
                        border = BorderStroke(1.dp, if (isDark) Color(0x66DC2626) else Color(0xFFFECACA))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = null,
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.dialogo_cerrar_confirmar),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFDC2626)
                            )
                        }
                    }
                }
            }

            // Nota de Ayuda al pie
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.pausado_nota_ayuda),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = subtitleColor,
                textAlign = TextAlign.Center
            )
        }
    }
}
