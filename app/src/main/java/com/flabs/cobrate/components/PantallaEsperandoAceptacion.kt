package com.flabs.cobrate.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.outlined.HourglassTop
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
 * Pantalla "Esperando Aprobación del Dueño" conforme al diseño oficial de Figma (Nodo 14:483).
 *
 * Se muestra cuando la relación del trabajador para el negocio está en estado SOLICITADA.
 */
@Composable
fun PantallaEsperandoAceptacion(
    negocioNombre: String,
    codigoUsado: String? = null,
    onCrearNegocio: (() -> Unit)? = null,
    onCancelarSolicitud: () -> Unit,
    onCerrarSesion: (() -> Unit)? = null,
    onVolver: (() -> Unit)? = null
) {
    var mostrarConfirmarCancelar by remember { mutableStateOf(false) }

    val themeMode by PreferenciasAppStore.themeMode.collectAsState()
    val isDark = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    val bgScreen = if (isDark) Color(0xFF0D1117) else Color(0xFFF4FBF6)
    val cardBg = if (isDark) Color(0xFF1A1F26) else Color.White
    val cardBorder = if (isDark) Color(0xFF2D333B) else Color(0xFFE2E8F0)
    val titleColor = if (isDark) Color.White else Color(0xFF131E18)
    val bodyTextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF3D4A3E)

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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                // ---- Tarjeta Central Modal Flotante ----
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
                            .padding(25.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Ícono prominente animado con reloj y badge
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (isDark) Color(0x26F59E0B) else Color(0xFFFEF3C7),
                                border = BorderStroke(1.dp, if (isDark) Color(0x66F59E0B) else Color(0xFFFDE68A)),
                                modifier = Modifier.size(64.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Outlined.HourglassTop,
                                        contentDescription = null,
                                        tint = Color(0xFFF59E0B),
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }

                            Surface(
                                shape = CircleShape,
                                color = if (isDark) Color(0x26F59E0B) else Color(0xFFFEF3C7),
                                border = BorderStroke(1.dp, if (isDark) Color(0x66F59E0B) else Color(0xFFFDE68A))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 13.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFF59E0B))
                                    )
                                    Text(
                                        text = "Pendiente de aprobación",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) Color(0xFFF59E0B) else Color(0xFFB45309)
                                    )
                                }
                            }
                        }

                        // Título Directo
                        Text(
                            text = stringResource(R.string.espera_titulo),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = titleColor,
                            textAlign = TextAlign.Center,
                            letterSpacing = (-0.55).sp,
                            lineHeight = 30.sp
                        )

                        // Tarjeta Resumen del Negocio Solicitado
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isDark) Color(0xFF12161A) else Color(0xFFF8FAFC),
                            border = BorderStroke(1.dp, if (isDark) Color(0xFF2D333B) else Color(0xFFE2E8F0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(13.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF00A651),
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Storefront,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                Column {
                                    Text(
                                        text = "SOLICITUD ENVIADA A",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF00A651),
                                        letterSpacing = 0.55.sp
                                    )
                                    Text(
                                        text = negocioNombre,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = titleColor
                                    )
                                    if (!codigoUsado.isNullOrBlank()) {
                                        Text(
                                            text = "Código: $codigoUsado",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = bodyTextColor
                                        )
                                    }
                                }
                            }
                        }

                        // Mensaje Explicativo
                        Text(
                            text = stringResource(R.string.espera_texto, negocioNombre),
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = bodyTextColor,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )

                        // Botones de Acción
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // 1. Crear mi propio negocio (si está disponible)
                            if (onCrearNegocio != null) {
                                Surface(
                                    onClick = onCrearNegocio,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (isDark) Color(0xFF00C853) else Color(0xFF00A651),
                                    shadowElevation = 2.dp
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            tint = if (isDark) Color.Black else Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = stringResource(R.string.pausado_crear_propio),
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDark) Color.Black else Color.White
                                        )
                                    }
                                }
                            }

                            // 2. Cancelar y cambiar código
                            Surface(
                                onClick = { mostrarConfirmarCancelar = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(20.dp),
                                color = cardBg,
                                border = BorderStroke(1.dp, cardBorder)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        tint = titleColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = stringResource(R.string.espera_cancelar),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = titleColor
                                    )
                                }
                            }
                        }

                        // Nota de ayuda sutil
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = bodyTextColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = stringResource(R.string.espera_nota_ayuda),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = bodyTextColor,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // ---- Opción Cerrar Sesión ----
            if (onCerrarSesion != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    onClick = onCerrarSesion,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDark) Color(0x33DC2626) else Color(0xEEFEF2F2),
                    border = BorderStroke(1.dp, if (isDark) Color(0x66DC2626) else Color(0xCCFECACA))
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
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFDC2626)
                        )
                    }
                }
            }
        }
    }

    if (mostrarConfirmarCancelar) {
        ConfirmarAccionDialog(
            titulo = stringResource(R.string.dialogo_cancelar_solicitud_titulo),
            texto = stringResource(R.string.dialogo_cancelar_solicitud_texto, negocioNombre),
            textoConfirmar = stringResource(R.string.dialogo_cancelar_solicitud_confirmar),
            textoCancelar = stringResource(R.string.dialogo_cancelar_solicitud_cancelar),
            destructivo = true,
            onConfirmar = {
                mostrarConfirmarCancelar = false
                onCancelarSolicitud()
            },
            onCancelar = { mostrarConfirmarCancelar = false }
        )
    }
}
