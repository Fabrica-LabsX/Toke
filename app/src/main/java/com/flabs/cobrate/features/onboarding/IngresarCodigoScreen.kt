package com.flabs.cobrate.features.onboarding

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
 * Pantalla "Soy trabajador" — captura el código de invitación (CAPA 4).
 * Se asegura de que exista una sesión anónima en Supabase antes de continuar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngresarCodigoScreen(
    onCodigoValido: (InvitacionTrabajador) -> Unit,
    onVolver: () -> Unit
) {
    var codigo by rememberSaveable { mutableStateOf("") }
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
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = onVolver,
                    shape = CircleShape,
                    color = cardBg,
                    border = BorderStroke(1.dp, cardBorder),
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

            Spacer(modifier = Modifier.height(16.dp))

            // Card Central
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
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (isDark) Color(0x1A00C853) else Color(0xFFE2F4E9),
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = null,
                                tint = if (isDark) Color(0xFF00C853) else Color(0xFF00A651),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Text(
                        text = stringResource(R.string.unirse_invitacion_titulo),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = titleColor,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = stringResource(R.string.unirse_invitacion_subtitulo),
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = bodyTextColor,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    OutlinedTextField(
                        value = codigo,
                        onValueChange = { codigo = it.uppercase() },
                        label = { Text(stringResource(R.string.label_codigo_invitacion)) },
                        placeholder = { Text("EJ: TK-8492") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00A651),
                            unfocusedBorderColor = cardBorder,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            scope.launch {
                                FeedbackManager.setLoading(true, "Validando invitación...")

                                val result = InvitacionStore.obtenerRemoto(codigo)

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
                                Log.d("TokeInvitacion", "motivoRechazo=$motivo")

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
                                            Log.w("TokeInvitacion", "sesión no disponible tras 8s")
                                            FeedbackManager.setLoading(false)
                                            FeedbackManager.showErrorMessage("Sesión del trabajador no disponible. Código de ayuda: ${TokeErrorCodes.SESION_TRABAJADOR_NO_DISPONIBLE}")
                                            return@launch
                                        }

                                        FeedbackManager.setLoading(false)
                                        onCodigoValido(invitacion!!)
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
                        },
                        enabled = codigo.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDark) Color(0xFF00C853) else Color(0xFF00A651),
                            contentColor = if (isDark) Color.Black else Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.empezar_unirme),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
