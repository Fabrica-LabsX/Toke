package com.flabs.cobrate.features.onboarding

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flabs.cobrate.R
import com.flabs.cobrate.core.data.CuentaStore
import com.flabs.cobrate.core.data.InvitacionRepository
import com.flabs.cobrate.core.data.NegocioStore
import com.flabs.cobrate.core.data.PreferenciasAppStore
import com.flabs.cobrate.core.logic.ErrorMapper
import com.flabs.cobrate.core.logic.FeedbackManager
import com.flabs.cobrate.core.logic.LogSeguro
import com.flabs.cobrate.core.logic.TokeErrorCodes
import com.flabs.cobrate.core.model.InvitacionTrabajador
import com.flabs.cobrate.core.model.PreviewInvitacion
import com.flabs.cobrate.ui.theme.ThemeMode
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Pantalla "Negocio Encontrado" conforme al diseño oficial de Figma (Nodo 14:359).
 *
 * Muestra el nombre real del negocio y la información del dueño antes de aceptar la invitación.
 */
@Composable
fun ConfirmarInvitacionScreen(
    codigo: String,
    invitacion: InvitacionTrabajador? = null,
    onAceptada: () -> Unit,
    onVolver: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val repository = remember { InvitacionRepository() }

    var preview by remember { mutableStateOf<PreviewInvitacion?>(null) }
    var loading by remember { mutableStateOf(true) }
    var errorPreview by remember { mutableStateOf<String?>(null) }
    var errorAceptar by remember { mutableStateOf<String?>(null) }

    val themeMode by PreferenciasAppStore.themeMode.collectAsState()
    val isDark = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    val bgScreen = if (isDark) Color(0xFF0D1117) else Color(0xFFF0FDF4)
    val cardBg = if (isDark) Color(0xFF1A1F26) else Color.White
    val cardBorder = if (isDark) Color(0xFF2D333B) else Color(0xFFE2E8F0)
    val titleColor = if (isDark) Color.White else Color(0xFF131E18)
    val bodyTextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF3D4A3E)

    LaunchedEffect(codigo) {
        loading = true
        errorPreview = null
        repository.obtenerPreview(codigo)
            .onSuccess {
                preview = it
                loading = false
                if (it == null) {
                    Log.w("TokeInvitacion", "preview devolvió null")
                    errorPreview = "Código de invitación no encontrado. Código de ayuda: ${TokeErrorCodes.INVITACION_NO_ENCONTRADA}"
                }
            }
            .onFailure { throwable ->
                LogSeguro.e("TokeInvitacion", "preview falló", throwable)
                errorPreview = ErrorMapper.translateInvitacionError(
                    throwable,
                    TokeErrorCodes.ERROR_VERIFICANDO_INVITACION
                )
                loading = false
            }
    }

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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ---- Header ----
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    onClick = onVolver,
                    shape = CircleShape,
                    color = if (isDark) Color(0xFF1A1F26) else Color.White,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = titleColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Text(
                    text = stringResource(R.string.encontrado_titulo),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = titleColor,
                    letterSpacing = (-0.55).sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ---- Contenido Central ----
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (loading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF00A651))
                    }
                } else if (errorPreview != null) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorPreview!!,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                if (errorAceptar != null) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorAceptar!!,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                if (!loading && errorPreview == null) {
                    val negocioNombreReal = preview?.negocioNombre ?: "Comercio"
                    val duenoNombreReal = remember(preview) {
                        val visible = preview?.duenoNombreVisible?.trim()
                        val username = preview?.duenoUsername?.trim()
                        when {
                            !visible.isNullOrBlank() -> visible
                            !username.isNullOrBlank() && !username.contains("@") -> username
                            !username.isNullOrBlank() && username.contains("@") -> username.substringBefore("@")
                            else -> "El dueño"
                        }
                    }

                    // Módulo 1: Tarjeta Protagonista del Comercio
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = cardBg,
                        border = BorderStroke(1.dp, cardBorder),
                        shadowElevation = if (isDark) 0.dp else 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Ícono de Tienda
                            Surface(
                                shape = CircleShape,
                                color = if (isDark) Color(0x1A00C853) else Color(0xFFEAF7ED),
                                modifier = Modifier.size(96.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Storefront,
                                        contentDescription = null,
                                        tint = if (isDark) Color(0xFF00C853) else Color(0xFF00A651),
                                        modifier = Modifier.size(40.dp)
                                    )
                                }
                            }

                            // Nombre del Negocio Real
                            Text(
                                text = negocioNombreReal,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = titleColor,
                                textAlign = TextAlign.Center,
                                letterSpacing = (-0.7).sp
                            )

                            // Chip de Quién invitó
                            Surface(
                                shape = CircleShape,
                                color = if (isDark) Color(0x1A00C853) else Color(0xFFE4F1E7)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = bodyTextColor,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = stringResource(R.string.encontrado_te_invito, duenoNombreReal),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = bodyTextColor
                                    )
                                }
                            }
                        }
                    }

                    // Módulo 2: Alerta Preventiva ("Verificación importante")
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isDark) Color(0x26F59E0B) else Color(0xFFFEF3C7),
                        border = BorderStroke(1.dp, if (isDark) Color(0x66F59E0B) else Color(0xFFFDE68A)),
                        shadowElevation = if (isDark) 0.dp else 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(17.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isDark) Color(0x4DF59E0B) else Color(0xFFFDE68A),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = if (isDark) Color(0xFFFBBF24) else Color(0xFFB45309),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = stringResource(R.string.encontrado_verificacion_importante),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color(0xFFFBBF24) else Color(0xFF92400E)
                                )
                                Text(
                                    text = stringResource(R.string.encontrado_verificacion_texto),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isDark) Color(0xFFFDE68A) else Color(0xFFB45309),
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }
                }

                // Módulo 3: Botones de Acción al Pie
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Botón Primario: Aceptar invitación
                    Surface(
                        onClick = {
                            scope.launch {
                                errorAceptar = null
                                var cuentaId = CuentaStore.sesionActual.value?.id

                                if (cuentaId == null) {
                                    val cuentaReady = withTimeoutOrNull(8000L) {
                                        CuentaStore.sesionActual.filter { it != null }.first()
                                    }
                                    cuentaId = cuentaReady?.id
                                }

                                if (cuentaId == null) {
                                    errorAceptar = "Sesión del trabajador no disponible. Código de ayuda: ${TokeErrorCodes.SESION_TRABAJADOR_NO_DISPONIBLE}"
                                    return@launch
                                }

                                repository.aceptarInvitacion(codigo)
                                    .onSuccess { resultado ->
                                        if (resultado.exito) {
                                            CuentaStore.sincronizarDatos(cuentaId)
                                            preview?.negocioId?.let { negId ->
                                                NegocioStore.seleccionarNegocio(negId)
                                            }
                                            FeedbackManager.showSuccess(resultado.mensaje)
                                            onAceptada()
                                        } else {
                                            errorAceptar = "${resultado.mensaje} (Código de ayuda: ${TokeErrorCodes.ERROR_ACEPTANDO_INVITACION})"
                                        }
                                    }
                                    .onFailure { throwable ->
                                        LogSeguro.e("TokeInvitacion", "aceptarInvitacion falló", throwable)
                                        errorAceptar = ErrorMapper.translateInvitacionError(
                                            throwable,
                                            TokeErrorCodes.ERROR_ACEPTANDO_INVITACION
                                        )
                                    }
                            }
                        },
                        enabled = !loading && errorPreview == null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = if (isDark) Color(0xFF00C853) else Color(0xFF00A651),
                        shadowElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.encontrado_aceptar),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.Black else Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = if (isDark) Color.Black else Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Botón Secundario: Atrás
                    Surface(
                        onClick = onVolver,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = cardBg,
                        border = BorderStroke(1.dp, cardBorder)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                                tint = if (isDark) Color(0xFF00C853) else Color(0xFF00A651),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.pausado_volver),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color(0xFF00C853) else Color(0xFF00A651)
                            )
                        }
                    }
                }
            }

            // Nota de Pie
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.encontrado_footer_nota),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = bodyTextColor,
                textAlign = TextAlign.Center
            )
        }
    }
}
