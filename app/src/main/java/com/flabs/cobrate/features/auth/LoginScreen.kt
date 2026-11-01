package com.flabs.cobrate.features.auth

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flabs.cobrate.R
import com.flabs.cobrate.core.data.PreferenciasAppStore
import com.flabs.cobrate.core.logic.FeedbackManager
import com.flabs.cobrate.ui.theme.ThemeMode
import kotlinx.coroutines.launch

private enum class ModoAuth {
    LOGIN,
    RECUPERACION_EMAIL,
    RECUPERACION_OTP,
    RECUPERACION_PASSWORD,
    REGISTRO_PASOS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    modoInicialCrear: Boolean = false,
    onVolver: () -> Unit = {}
) {
    var modoAuth by rememberSaveable {
        mutableStateOf(if (modoInicialCrear) ModoAuth.REGISTRO_PASOS else ModoAuth.LOGIN)
    }

    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    // Estado Recuperación
    var otpTokenRecuperacion by rememberSaveable { mutableStateOf("") }
    var nuevaPasswordRecuperacion by rememberSaveable { mutableStateOf("") }
    var confirmarPasswordRecuperacion by rememberSaveable { mutableStateOf("") }
    var mostrarDialogoConfirmarCierreOtrasSesiones by rememberSaveable { mutableStateOf(false) }

    // Estado Registro por Pasos (1: Nombre, 2: Correo, 3: OTP, 4: Contraseña)
    var pasoRegistro by rememberSaveable { mutableIntStateOf(1) }
    var nombreRegistro by rememberSaveable { mutableStateOf("") }
    var otpTokenRegistro by rememberSaveable { mutableStateOf("") }
    var passwordRegistro by rememberSaveable { mutableStateOf("") }
    var confirmarPasswordRegistro by rememberSaveable { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    val themeMode by PreferenciasAppStore.themeMode.collectAsState()
    val isDark = themeMode == ThemeMode.DARK
    val logoResource = if (isDark) R.drawable.ic_toke_logo else R.drawable.ic_toke_logo_green

    val handleVolver = {
        when (modoAuth) {
            ModoAuth.LOGIN -> onVolver()
            ModoAuth.RECUPERACION_EMAIL -> modoAuth = ModoAuth.LOGIN
            ModoAuth.RECUPERACION_OTP -> modoAuth = ModoAuth.RECUPERACION_EMAIL
            ModoAuth.RECUPERACION_PASSWORD -> modoAuth = ModoAuth.LOGIN
            ModoAuth.REGISTRO_PASOS -> {
                if (pasoRegistro > 1) {
                    pasoRegistro -= 1
                } else {
                    modoAuth = ModoAuth.LOGIN
                }
            }
        }
    }

    BackHandler(onBack = handleVolver)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = handleVolver) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.login_volver_desc)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = logoResource),
                contentDescription = stringResource(R.string.app_name),
                modifier = Modifier.size(100.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(4.dp))

            when (modoAuth) {
                // ---- MODO 1: LOGIN NORMAL ----
                ModoAuth.LOGIN -> {
                    Text(
                        text = stringResource(R.string.bienvenida_iniciar_sesion),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text(stringResource(R.string.login_email_label)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text(stringResource(R.string.login_password_label)) },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Ver contraseña"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Enlace secundario discreto de recuperación en línea
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { modoAuth = ModoAuth.RECUPERACION_EMAIL }
                        ) {
                            Text(
                                text = "Olvidé mi contraseña",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            if (email.isBlank() || password.isBlank()) {
                                FeedbackManager.showErrorMessage("Por favor ingresa tu correo y contraseña.")
                                return@Button
                            }
                            scope.launch {
                                FeedbackManager.setLoading(true, "Iniciando sesión...")
                                val result = authViewModel.iniciarSesion(email, password)
                                FeedbackManager.setLoading(false)
                                result.onFailure { err ->
                                    FeedbackManager.showError(err)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("Iniciar sesión", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = {
                            modoAuth = ModoAuth.REGISTRO_PASOS
                            pasoRegistro = 1
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("¿No tienes cuenta? Regístrate", fontWeight = FontWeight.SemiBold)
                    }
                }

                // ---- MODO 2: RECUPERACIÓN - EMAIL ----
                ModoAuth.RECUPERACION_EMAIL -> {
                    Text(
                        text = "Recuperar contraseña",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Ingresa tu correo para recibir las instrucciones y tu código de verificación.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Correo electrónico") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            if (email.isBlank() || !email.contains("@")) {
                                FeedbackManager.showErrorMessage("Ingresa un correo electrónico válido.")
                                return@Button
                            }
                            scope.launch {
                                FeedbackManager.setLoading(true, "Enviando código...")
                                val res = authViewModel.solicitarRecuperacionPassword(email)
                                FeedbackManager.setLoading(false)
                                res.onSuccess {
                                    FeedbackManager.showSuccess("Si el correo está registrado en TOKE!, recibirás un código.")
                                    modoAuth = ModoAuth.RECUPERACION_OTP
                                }.onFailure { err ->
                                    FeedbackManager.showError(err)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("Enviar código", fontWeight = FontWeight.Bold)
                    }

                    TextButton(onClick = { modoAuth = ModoAuth.LOGIN }) {
                        Text("Volver al inicio de sesión")
                    }
                }

                // ---- MODO 3: RECUPERACIÓN - OTP ----
                ModoAuth.RECUPERACION_OTP -> {
                    Text(
                        text = "Código de verificación",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Revisa tu bandeja de entrada e ingresa el código enviado a $email",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = otpTokenRecuperacion,
                        onValueChange = { otpTokenRecuperacion = it },
                        label = { Text("Código de verificación") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            if (otpTokenRecuperacion.isBlank()) {
                                FeedbackManager.showErrorMessage("Ingresa el código de verificación.")
                                return@Button
                            }
                            scope.launch {
                                FeedbackManager.setLoading(true, "Verificando código...")
                                val res = authViewModel.verificarOtpRecuperacion(email, otpTokenRecuperacion)
                                FeedbackManager.setLoading(false)
                                res.onSuccess {
                                    FeedbackManager.showSuccess("Código verificado correctamente.")
                                    modoAuth = ModoAuth.RECUPERACION_PASSWORD
                                }.onFailure { err ->
                                    FeedbackManager.showErrorMessage(err.message ?: "Código de verificación inválido o expirado.")
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("Verificar código", fontWeight = FontWeight.Bold)
                    }

                    TextButton(onClick = { modoAuth = ModoAuth.RECUPERACION_EMAIL }) {
                        Text("Cambiar correo o reenviar")
                    }
                }

                // ---- MODO 4: RECUPERACIÓN - NUEVA CONTRASEÑA ----
                ModoAuth.RECUPERACION_PASSWORD -> {
                    Text(
                        text = "Nueva contraseña",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Establece tu nueva contraseña para acceder a TOKE!.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = nuevaPasswordRecuperacion,
                        onValueChange = { nuevaPasswordRecuperacion = it },
                        label = { Text("Nueva contraseña") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = confirmarPasswordRecuperacion,
                        onValueChange = { confirmarPasswordRecuperacion = it },
                        label = { Text("Confirmar contraseña") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            if (nuevaPasswordRecuperacion.length < 6) {
                                FeedbackManager.showErrorMessage("La contraseña debe tener al menos 6 caracteres.")
                                return@Button
                            }
                            if (nuevaPasswordRecuperacion != confirmarPasswordRecuperacion) {
                                FeedbackManager.showErrorMessage("Las contraseñas no coinciden.")
                                return@Button
                            }
                            mostrarDialogoConfirmarCierreOtrasSesiones = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("Aceptar y actualizar", fontWeight = FontWeight.Bold)
                    }

                    TextButton(onClick = {
                        FeedbackManager.showSuccess("Sesión iniciada correctamente.")
                    }) {
                        Text("Continuar sin cambiar")
                    }
                }

                // ---- MODO 5: REGISTRO EN PASOS ----
                ModoAuth.REGISTRO_PASOS -> {
                    Text(
                        text = "Crear tu cuenta en TOKE!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Paso $pasoRegistro de 4",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    when (pasoRegistro) {
                        1 -> {
                            Text(
                                text = "¿Cómo te llamas?",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Tu nombre se mostrará a tu equipo de trabajo.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            OutlinedTextField(
                                value = nombreRegistro,
                                onValueChange = { nombreRegistro = it },
                                label = { Text("Nombre o apodo") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Button(
                                onClick = {
                                    if (nombreRegistro.isBlank()) {
                                        FeedbackManager.showErrorMessage("Ingresa tu nombre.")
                                        return@Button
                                    }
                                    pasoRegistro = 2
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Text("Continuar", fontWeight = FontWeight.Bold)
                            }
                        }

                        2 -> {
                            Text(
                                text = "Tu correo electrónico",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )

                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("Correo electrónico") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Button(
                                onClick = {
                                    if (email.isBlank() || !email.contains("@")) {
                                        FeedbackManager.showErrorMessage("Ingresa un correo electrónico válido.")
                                        return@Button
                                    }
                                    scope.launch {
                                        FeedbackManager.setLoading(true, "Enviando código de confirmación...")
                                        val res = authViewModel.solicitarOtpRegistro(email)
                                        FeedbackManager.setLoading(false)
                                        res.onSuccess {
                                            FeedbackManager.showSuccess("Código enviado a $email")
                                            pasoRegistro = 3
                                        }.onFailure { err ->
                                            FeedbackManager.showError(err)
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Text("Enviar código", fontWeight = FontWeight.Bold)
                            }
                        }

                        3 -> {
                            Text(
                                text = "Verifica tu correo",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Revisa tu bandeja de entrada e ingresa el código enviado a $email",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            OutlinedTextField(
                                value = otpTokenRegistro,
                                onValueChange = { otpTokenRegistro = it },
                                label = { Text("Código de confirmación") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Button(
                                onClick = {
                                    if (otpTokenRegistro.isBlank()) {
                                        FeedbackManager.showErrorMessage("Ingresa el código enviado a tu correo.")
                                        return@Button
                                    }
                                    scope.launch {
                                        FeedbackManager.setLoading(true, "Verificando código...")
                                        val res = authViewModel.verificarOtpRegistro(email, otpTokenRegistro)
                                        FeedbackManager.setLoading(false)
                                        res.onSuccess {
                                            FeedbackManager.showSuccess("Correo verificado correctamente.")
                                            pasoRegistro = 4
                                        }.onFailure { err ->
                                            FeedbackManager.showErrorMessage(err.message ?: "Código incorrecto o expirado.")
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Text("Verificar", fontWeight = FontWeight.Bold)
                            }
                        }

                        4 -> {
                            Text(
                                text = "Crea tu contraseña",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )

                            OutlinedTextField(
                                value = passwordRegistro,
                                onValueChange = { passwordRegistro = it },
                                label = { Text("Contraseña (mínimo 6 caracteres)") },
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = confirmarPasswordRegistro,
                                onValueChange = { confirmarPasswordRegistro = it },
                                label = { Text("Confirmar contraseña") },
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Button(
                                onClick = {
                                    if (passwordRegistro.length < 6) {
                                        FeedbackManager.showErrorMessage("La contraseña debe tener al menos 6 caracteres.")
                                        return@Button
                                    }
                                    if (passwordRegistro != confirmarPasswordRegistro) {
                                        FeedbackManager.showErrorMessage("Las contraseñas no coinciden.")
                                        return@Button
                                    }
                                    scope.launch {
                                        FeedbackManager.setLoading(true, "Completando registro...")
                                        val res = authViewModel.completarRegistro(nombreRegistro, passwordRegistro)
                                        FeedbackManager.setLoading(false)
                                        res.onSuccess {
                                            FeedbackManager.showSuccess("¡Bienvenido a TOKE!, $nombreRegistro")
                                        }.onFailure { err ->
                                            FeedbackManager.showError(err)
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Text("Completar registro", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    TextButton(onClick = { modoAuth = ModoAuth.LOGIN }) {
                        Text("¿Ya tienes cuenta? Inicia sesión")
                    }
                }
            }
        }
    }

    if (mostrarDialogoConfirmarCierreOtrasSesiones) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoConfirmarCierreOtrasSesiones = false },
            title = { Text("Advertencia de seguridad", fontWeight = FontWeight.Bold) },
            text = { Text("Al actualizar tu contraseña, por seguridad se cerrarán las sesiones activas en otros dispositivos.") },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarDialogoConfirmarCierreOtrasSesiones = false
                        scope.launch {
                            FeedbackManager.setLoading(true, "Actualizando contraseña...")
                            val res = authViewModel.actualizarPasswordYRevocarOtros(nuevaPasswordRecuperacion)
                            FeedbackManager.setLoading(false)
                            res.onSuccess {
                                FeedbackManager.showSuccess("Contraseña actualizada. Otras sesiones han sido cerradas.")
                                modoAuth = ModoAuth.LOGIN
                            }.onFailure { err ->
                                FeedbackManager.showError(err)
                            }
                        }
                    }
                ) {
                    Text("Entendido y actualizar")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { mostrarDialogoConfirmarCierreOtrasSesiones = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
