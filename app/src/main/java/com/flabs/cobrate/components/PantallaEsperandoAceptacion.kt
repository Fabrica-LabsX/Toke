package com.flabs.cobrate.components
import com.flabs.cobrate.ui.theme.TokeMedidas
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                // ---- Tarjeta Central Sólida escalada a 9:16 ----
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = cardBg,
                    border = BorderStroke(1.dp, cardBorder),
                    shadowElevation = 0.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Ícono prominente con reloj y badge sólido
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = paleta.tileFondo,
                                border = BorderStroke(1.dp, paleta.bordeControl),
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Outlined.HourglassTop,
                                        contentDescription = null,
                                        tint = paleta.ambar,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            Surface(
                                shape = CircleShape,
                                color = paleta.tileFondo,
                                border = BorderStroke(1.dp, paleta.bordeControl)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(paleta.ambar)
                                    )
                                    Text(
                                        text = "Pendiente de aprobación",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = paleta.textoPrincipal
                                    )
                                }
                            }
                        }

                        // Título Directo
                        Text(
                            text = stringResource(R.string.espera_titulo),
                            fontSize = 19.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = titleColor,
                            textAlign = TextAlign.Center,
                            letterSpacing = (-0.3).sp,
                            lineHeight = 25.sp
                        )

                        // Tarjeta Resumen del Negocio Solicitado
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = paleta.fondo,
                            border = BorderStroke(1.dp, paleta.bordeControl),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = paleta.verde,
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Storefront,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Column {
                                    Text(
                                        text = "SOLICITUD ENVIADA A",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = paleta.verde,
                                        letterSpacing = 0.5.sp
                                    )
                                    Text(
                                        text = negocioNombre,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = titleColor
                                    )
                                    if (!codigoUsado.isNullOrBlank()) {
                                        Text(
                                            text = "Código: $codigoUsado",
                                            fontSize = 10.5.sp,
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
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = bodyTextColor,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )

                        // Botones de Acción
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // 1. Crear mi propio negocio (si está disponible)
                            if (onCrearNegocio != null) {
                                Surface(
                                    onClick = onCrearNegocio,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(42.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    color = paleta.verde,
                                    shadowElevation = 0.dp
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
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = stringResource(R.string.pausado_crear_propio),
                                            fontSize = 14.sp,
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
                                    .height(42.dp),
                                shape = RoundedCornerShape(14.dp),
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
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = stringResource(R.string.espera_cancelar),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = titleColor
                                    )
                                }
                            }
                        }

                        // Nota de ayuda sutil
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = bodyTextColor,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = stringResource(R.string.espera_nota_ayuda),
                                fontSize = 11.sp,
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
                Surface(
                    onClick = onCerrarSesion,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp),
                    shape = RoundedCornerShape(9999.dp),
                    color = paleta.textoPrincipal,
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = null,
                            tint = paleta.fondo,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.dialogo_cerrar_confirmar),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = paleta.fondo
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
