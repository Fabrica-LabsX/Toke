package com.flabs.cobrate.components
import com.flabs.cobrate.ui.theme.TokeAmbarTexto
import com.flabs.cobrate.ui.theme.tokePaleta

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

    val paleta = tokePaleta()
    val bgScreen = paleta.fondo
    val cardBg = paleta.tarjeta
    val cardBorder = paleta.borde
    val titleColor = paleta.textoPrincipal
    val bodyTextColor = paleta.textoSecundario

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
                                color = paleta.ambar.copy(alpha = 0.14f),
                                border = BorderStroke(1.dp, paleta.ambar.copy(alpha = 0.4f)),
                                modifier = Modifier.size(64.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Outlined.HourglassTop,
                                        contentDescription = null,
                                        tint = paleta.ambar,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }

                            Surface(
                                shape = CircleShape,
                                color = paleta.ambar.copy(alpha = 0.14f),
                                border = BorderStroke(1.dp, paleta.ambar.copy(alpha = 0.4f))
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
                                            .background(paleta.ambar)
                                    )
                                    Text(
                                        text = "Pendiente de aprobación",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) paleta.ambar else TokeAmbarTexto
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
                            color = paleta.fondo,
                            border = BorderStroke(1.dp, paleta.bordeControl),
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
                                    color = paleta.verde,
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
                                        color = paleta.verde,
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
                                    color = paleta.verde,
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
                    color = paleta.rojoFondo,
                    border = BorderStroke(1.dp, paleta.rojoBorde)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = null,
                            tint = paleta.rojo,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.dialogo_cerrar_confirmar),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = paleta.rojo
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
