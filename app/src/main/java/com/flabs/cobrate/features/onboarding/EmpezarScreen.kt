package com.flabs.cobrate.features.onboarding

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flabs.cobrate.R
import com.flabs.cobrate.core.data.CuentaStore
import com.flabs.cobrate.core.data.InvitacionStore
import com.flabs.cobrate.core.data.PreferenciasAppStore
import com.flabs.cobrate.core.data.SupabaseAuthBridge
import com.flabs.cobrate.core.logic.ErrorMapper
import com.flabs.cobrate.core.logic.FeedbackManager
import com.flabs.cobrate.core.logic.InvitacionValidation
import com.flabs.cobrate.core.logic.LogSeguro
import com.flabs.cobrate.core.logic.MotivoRechazo
import com.flabs.cobrate.core.logic.TokeErrorCodes
import com.flabs.cobrate.core.model.InvitacionTrabajador
import com.flabs.cobrate.ui.theme.ThemeMode
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Pantalla "Empezar" / Sin Negocio conforme al diseño oficial de Figma (Nodo 14:536).
 *
 * Ofrece dos caminos principales:
 * 1. "Soy dueño" -> Crear un nuevo negocio.
 * 2. "Tengo un código de invitación" -> Unirme con código de 6 dígitos.
 */
@Composable
fun EmpezarScreen(
    onCrearNegocio: () -> Unit,
    onValidarCodigo: (InvitacionTrabajador) -> Unit,
    onCerrarSesion: (() -> Unit)? = null,
    onVolver: (() -> Unit)? = null
) {
    var codigoInput by rememberSaveable { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    val themeMode by PreferenciasAppStore.themeMode.collectAsState()
    val isDark = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    val bgScreen = if (isDark) Color(0xFF0D1117) else Color(0xFFEAF7EE)
    val cardBg = if (isDark) Color(0xFF1A1F26) else Color.White
    val cardBorder = if (isDark) Color(0xFF2D333B) else Color(0xFFE2F0E7)
    val titleColor = if (isDark) Color.White else Color(0xFF0B2316)
    val bodyTextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF4B6354)

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
                    .height(48.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onVolver != null) {
                    Surface(
                        onClick = onVolver,
                        shape = CircleShape,
                        color = if (isDark) Color(0xFF1A1F26) else Color.White,
                        modifier = Modifier.size(40.dp)
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
                }
            }

            // ---- Contenido Principal ----
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Título y Bienvenida
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = stringResource(R.string.empezar_titulo),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = titleColor,
                        textAlign = TextAlign.Center,
                        letterSpacing = (-0.7).sp
                    )
                    Text(
                        text = stringResource(R.string.empezar_subtitulo),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = bodyTextColor,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp,
                        modifier = Modifier.widthIn(max = 320.dp)
                    )
                }

                // ---- Tarjeta 1: Modo Dueño ----
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = cardBg,
                    border = BorderStroke(1.dp, cardBorder),
                    shadowElevation = if (isDark) 0.dp else 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(21.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Header de Tarjeta 1
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (isDark) Color(0x1A00C853) else Color(0xFFE2F4E9),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Storefront,
                                        contentDescription = null,
                                        tint = if (isDark) Color(0xFF00C853) else Color(0xFF00A651),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Surface(
                                shape = CircleShape,
                                color = if (isDark) Color(0x1A00C853) else Color(0xFFE8F7ED)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color(0xFF146C36),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = stringResource(R.string.empezar_modo_dueno),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF146C36)
                                    )
                                }
                            }
                        }

                        // Regla 0.1: "Soy dueño" en lugar de "Soy dueño o administrador"
                        Text(
                            text = stringResource(R.string.empezar_soy_dueno),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = titleColor,
                            letterSpacing = (-0.45).sp
                        )

                        Text(
                            text = stringResource(R.string.empezar_texto_dueno),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = bodyTextColor,
                            lineHeight = 18.sp
                        )

                        Surface(
                            onClick = onCrearNegocio,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isDark) Color(0xFF00C853) else Color(0xFF007A3E),
                            shadowElevation = 1.dp
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.empezar_crear_negocio),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color.Black else Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = if (isDark) Color.Black else Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                // ---- Tarjeta 2: Modo Equipo / Tengo Código ----
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = cardBg,
                    border = BorderStroke(1.dp, cardBorder),
                    shadowElevation = if (isDark) 0.dp else 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(21.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Header de Tarjeta 2
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (isDark) Color(0x1A00C853) else Color(0xFFE2F4E9),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Badge,
                                        contentDescription = null,
                                        tint = if (isDark) Color(0xFF00C853) else Color(0xFF00A651),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Surface(
                                shape = CircleShape,
                                color = if (isDark) Color(0x1A00C853) else Color(0xFFE8F7ED)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Key,
                                        contentDescription = null,
                                        tint = Color(0xFF146C36),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = stringResource(R.string.empezar_modo_equipo),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF146C36)
                                    )
                                }
                            }
                        }

                        Text(
                            text = stringResource(R.string.empezar_tengo_codigo),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = titleColor,
                            letterSpacing = (-0.45).sp
                        )

                        Text(
                            text = stringResource(R.string.empezar_texto_equipo),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = bodyTextColor,
                            lineHeight = 18.sp
                        )

                        // Cajetín interactivo de código
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isDark) Color(0xFF12161A) else Color(0xFFF8FAFC),
                                border = BorderStroke(1.dp, if (isDark) Color(0xFF2D333B) else Color(0xFFE2E8F0)),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Key,
                                        contentDescription = null,
                                        tint = bodyTextColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(modifier = Modifier.weight(1f)) {
                                        if (codigoInput.isEmpty()) {
                                            Text(
                                                text = stringResource(R.string.empezar_placeholder_codigo),
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFF94A3B8),
                                                letterSpacing = 0.7.sp
                                            )
                                        }
                                        BasicTextField(
                                            value = codigoInput,
                                            onValueChange = { codigoInput = it.uppercase() },
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                                            textStyle = LocalTextStyle.current.copy(
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = titleColor,
                                                letterSpacing = 0.7.sp
                                            ),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }

                            // Botón Unirme
                            Surface(
                                onClick = {
                                    if (codigoInput.isNotBlank()) {
                                        val c = codigoInput.trim()
                                        scope.launch {
                                            FeedbackManager.setLoading(true, "Validando invitación...")
                                            val result = InvitacionStore.obtenerRemoto(c)

                                            if (result.isFailure) {
                                                LogSeguro.e("TokeInvitacion", "obtenerRemoto falló", result.exceptionOrNull())
                                                FeedbackManager.setLoading(false)
                                                val errorMsg = ErrorMapper.translateInvitacionError(
                                                    result.exceptionOrNull(),
                                                    TokeErrorCodes.ERROR_VERIFICANDO_INVITACION
                                                )
                                                FeedbackManager.showErrorMessage(errorMsg)
                                                return@launch
                                            }

                                            val invitacion = result.getOrNull()
                                            val motivo = InvitacionValidation.motivoDeRechazo(
                                                invitacion,
                                                System.currentTimeMillis()
                                            )

                                            when (motivo) {
                                                null -> {
                                                    if (CuentaStore.sesionActual.value == null) {
                                                        val anonRes = SupabaseAuthBridge.iniciarSesionTrabajadorAnonimo()
                                                        if (anonRes.isFailure) {
                                                            LogSeguro.e("TokeInvitacion", "sesión anónima falló", anonRes.exceptionOrNull())
                                                            FeedbackManager.setLoading(false)
                                                            val errorMsg = ErrorMapper.translateInvitacionError(
                                                                anonRes.exceptionOrNull(),
                                                                TokeErrorCodes.ERROR_SESION_TRABAJADOR
                                                            )
                                                            FeedbackManager.showErrorMessage(errorMsg)
                                                            return@launch
                                                        }
                                                    }

                                                    val cuentaReady = withTimeoutOrNull(8000L) {
                                                        CuentaStore.sesionActual.filter { it != null }.first()
                                                    }

                                                    if (cuentaReady == null) {
                                                        FeedbackManager.setLoading(false)
                                                        FeedbackManager.showErrorMessage("Sesión del trabajador no disponible. Código de ayuda: ${TokeErrorCodes.SESION_TRABAJADOR_NO_DISPONIBLE}")
                                                        return@launch
                                                    }

                                                    FeedbackManager.setLoading(false)
                                                    onValidarCodigo(invitacion!!)
                                                }
                                                MotivoRechazo.NO_EXISTE -> {
                                                    FeedbackManager.setLoading(false)
                                                    FeedbackManager.showErrorMessage("Código de invitación no encontrado. Código de ayuda: ${TokeErrorCodes.INVITACION_NO_ENCONTRADA}")
                                                }
                                                MotivoRechazo.NO_PENDIENTE -> {
                                                    FeedbackManager.setLoading(false)
                                                    FeedbackManager.showErrorMessage("Esta invitación ya fue utilizada. Código de ayuda: ${TokeErrorCodes.INVITACION_YA_UTILIZADA}")
                                                }
                                                MotivoRechazo.EXPIRADA -> {
                                                    FeedbackManager.setLoading(false)
                                                    FeedbackManager.showErrorMessage("La invitación ha expirado. Código de ayuda: ${TokeErrorCodes.INVITACION_EXPIRADA}")
                                                }
                                            }
                                        }
                                    }
                                },
                                enabled = codigoInput.isNotBlank(),
                                modifier = Modifier.height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = if (codigoInput.isNotBlank()) Color(0xFFDEEFE4) else Color(0xFFE2E8F0)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.empezar_unirme),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (codigoInput.isNotBlank()) Color(0xFF0E6838) else Color(0xFF94A3B8)
                                    )
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = if (codigoInput.isNotBlank()) Color(0xFF0E6838) else Color(0xFF94A3B8),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }

                        // Nota sutil de código
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = bodyTextColor,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = stringResource(R.string.empezar_nota_codigo),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = bodyTextColor,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                // ---- Botón Cerrar Sesión ----
                if (onCerrarSesion != null) {
                    Surface(
                        onClick = onCerrarSesion,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = if (isDark) Color(0x33DC2626) else Color.White,
                        border = BorderStroke(1.dp, if (isDark) Color(0x66DC2626) else Color(0xFFFEE2E2)),
                        shadowElevation = 1.dp
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

            // ---- Footer Notes ----
            Spacer(modifier = Modifier.height(12.dp))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = stringResource(R.string.empezar_nota_footer),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = bodyTextColor,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
