package com.flabs.cobrate.features.auth

import androidx.activity.compose.BackHandler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import com.flabs.cobrate.R
import com.flabs.cobrate.core.logic.FeedbackManager
import com.flabs.cobrate.features.acceso.AccesoFondo
import com.flabs.cobrate.features.acceso.AccesoLogica
import com.flabs.cobrate.features.acceso.BotonAtras
import com.flabs.cobrate.features.acceso.EnlaceAcceso
import com.flabs.cobrate.features.acceso.EspacioAcceso
import com.flabs.cobrate.features.acceso.FormLogin
import com.flabs.cobrate.features.acceso.FormPasswords
import com.flabs.cobrate.features.acceso.FormRegistroClave
import com.flabs.cobrate.features.acceso.FormRegistroDatos
import com.flabs.cobrate.features.acceso.FormUnCampo
import com.flabs.cobrate.features.acceso.LogoToke
import com.flabs.cobrate.features.acceso.TituloAcceso
import com.flabs.cobrate.ui.theme.tokeEsOscuro
import com.flabs.cobrate.ui.theme.tokePaleta
import kotlinx.coroutines.launch

private enum class ModoAuth {
    LOGIN,
    RECUPERACION_EMAIL,
    RECUPERACION_OTP,
    RECUPERACION_PASSWORD,
    REGISTRO_PASOS
}

/**
 * Pantalla de acceso: iniciar sesión, recuperar la contraseña y crear la cuenta en pasos.
 * Guarda el estado y hace las llamadas de autenticación; el dibujo vive en `features/acceso`.
 */
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
    val paleta = tokePaleta()
    val esOscuro = tokeEsOscuro()

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

    AccesoFondo(paleta = paleta) {
        BotonAtras(paleta, handleVolver)
        LogoToke(esOscuro, tamano = 72, descripcion = stringResource(R.string.app_name))

        when (modoAuth) {
            // ---- MODO 1: LOGIN NORMAL ----
            ModoAuth.LOGIN -> {
                TituloAcceso(paleta, "Ingresa a tu cuenta", "¡Bienvenido de vuelta!")
                FormLogin(
                    paleta = paleta,
                    email = email,
                    onEmail = { email = it },
                    password = password,
                    onPassword = { password = it },
                    passwordVisible = passwordVisible,
                    onAlternarVisible = { passwordVisible = !passwordVisible },
                    onOlvide = { modoAuth = ModoAuth.RECUPERACION_EMAIL },
                    onEntrar = {
                        if (email.isBlank() || password.isBlank()) {
                            FeedbackManager.showErrorMessage("Por favor ingresa tu correo y contraseña.")
                        } else {
                            scope.launch {
                                FeedbackManager.setLoading(true, "Iniciando sesión...")
                                val result = authViewModel.iniciarSesion(email, password)
                                FeedbackManager.setLoading(false)
                                result.onFailure { err -> FeedbackManager.showError(err) }
                            }
                        }
                    },
                    onCrear = {
                        modoAuth = ModoAuth.REGISTRO_PASOS
                        pasoRegistro = 1
                    },
                )
            }

            // ---- MODO 2: RECUPERACIÓN - EMAIL ----
            ModoAuth.RECUPERACION_EMAIL -> {
                TituloAcceso(paleta, "Recuperar acceso", "Ingresa tu correo para recibir las instrucciones y tu código de verificación.")
                FormUnCampo(
                    paleta = paleta,
                    etiqueta = "Correo electrónico",
                    valor = email,
                    placeholder = "tu@correo.com",
                    teclado = KeyboardType.Email,
                    onCambia = { email = it },
                    textoBoton = "Enviar código",
                    onAccion = {
                        if (!AccesoLogica.correoValido(email)) {
                            FeedbackManager.showErrorMessage("Ingresa un correo electrónico válido.")
                        } else {
                            scope.launch {
                                FeedbackManager.setLoading(true, "Enviando código...")
                                val res = authViewModel.solicitarRecuperacionPassword(email)
                                FeedbackManager.setLoading(false)
                                res.onSuccess {
                                    FeedbackManager.showSuccess("Si el correo está registrado en TOKE!, recibirás un código.")
                                    modoAuth = ModoAuth.RECUPERACION_OTP
                                }.onFailure { err -> FeedbackManager.showError(err) }
                            }
                        }
                    },
                    textoEnlace = "Volver al inicio de sesión",
                    onEnlace = { modoAuth = ModoAuth.LOGIN },
                )
            }

            // ---- MODO 3: RECUPERACIÓN - OTP ----
            ModoAuth.RECUPERACION_OTP -> {
                TituloAcceso(paleta, "Verifica tu código", "Revisa tu bandeja de entrada e ingresa el código enviado a $email")
                FormUnCampo(
                    paleta = paleta,
                    etiqueta = "Código de verificación",
                    valor = otpTokenRecuperacion,
                    placeholder = "Código",
                    teclado = KeyboardType.Number,
                    onCambia = { otpTokenRecuperacion = it },
                    textoBoton = "Verificar código",
                    onAccion = {
                        if (otpTokenRecuperacion.isBlank()) {
                            FeedbackManager.showErrorMessage("Ingresa el código de verificación.")
                        } else {
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
                        }
                    },
                    textoEnlace = "Cambiar correo o reenviar",
                    onEnlace = { modoAuth = ModoAuth.RECUPERACION_EMAIL },
                )
            }

            // ---- MODO 4: RECUPERACIÓN - NUEVA CONTRASEÑA ----
            ModoAuth.RECUPERACION_PASSWORD -> {
                TituloAcceso(paleta, "Nueva contraseña", "Establece tu nueva contraseña para acceder a TOKE!.")
                FormPasswords(
                    paleta = paleta,
                    etiqueta = "Nueva contraseña",
                    password = nuevaPasswordRecuperacion,
                    onPassword = { nuevaPasswordRecuperacion = it },
                    confirmacion = confirmarPasswordRecuperacion,
                    onConfirmacion = { confirmarPasswordRecuperacion = it },
                    textoBoton = "Aceptar y actualizar",
                    onAccion = {
                        val error = AccesoLogica.errorPasswords(nuevaPasswordRecuperacion, confirmarPasswordRecuperacion)
                        if (error != null) {
                            FeedbackManager.showErrorMessage(error)
                        } else {
                            mostrarDialogoConfirmarCierreOtrasSesiones = true
                        }
                    },
                    textoEnlace = "Continuar sin cambiar",
                    onEnlace = { FeedbackManager.showSuccess("Sesión iniciada correctamente.") },
                )
            }

            // ---- MODO 5: REGISTRO EN 2 PASOS ----
            ModoAuth.REGISTRO_PASOS -> {
                TituloAcceso(
                    paleta,
                    "Crea tu cuenta",
                    if (pasoRegistro <= 1) "Paso 1 de 2 · Tus datos" else "Paso 2 de 2 · Confirma tu correo",
                )
                if (pasoRegistro <= 1) {
                    FormRegistroDatos(
                        paleta = paleta,
                        nombre = nombreRegistro,
                        onNombre = { nombreRegistro = it },
                        email = email,
                        onEmail = { email = it },
                        onContinuar = {
                            if (nombreRegistro.isBlank()) {
                                FeedbackManager.showErrorMessage("Ingresa tu nombre de usuario.")
                            } else if (!AccesoLogica.correoValido(email)) {
                                FeedbackManager.showErrorMessage("Ingresa un correo electrónico válido.")
                            } else {
                                scope.launch {
                                    FeedbackManager.setLoading(true, "Enviando código de confirmación...")
                                    val res = authViewModel.solicitarOtpRegistro(email)
                                    FeedbackManager.setLoading(false)
                                    res.onSuccess {
                                        FeedbackManager.showSuccess("Código enviado a $email")
                                        pasoRegistro = 2
                                    }.onFailure { err -> FeedbackManager.showError(err) }
                                }
                            }
                        },
                    )
                } else {
                    FormRegistroClave(
                        paleta = paleta,
                        email = email,
                        codigo = otpTokenRegistro,
                        onCodigo = { otpTokenRegistro = it },
                        password = passwordRegistro,
                        onPassword = { passwordRegistro = it },
                        confirmacion = confirmarPasswordRegistro,
                        onConfirmacion = { confirmarPasswordRegistro = it },
                        onCompletar = {
                            val error = AccesoLogica.errorPasswords(passwordRegistro, confirmarPasswordRegistro)
                            if (otpTokenRegistro.isBlank()) {
                                FeedbackManager.showErrorMessage("Ingresa el código enviado a tu correo.")
                            } else if (error != null) {
                                FeedbackManager.showErrorMessage(error)
                            } else {
                                scope.launch {
                                    FeedbackManager.setLoading(true, "Verificando código...")
                                    val verif = authViewModel.verificarOtpRegistro(email, otpTokenRegistro)
                                    if (verif.isFailure) {
                                        FeedbackManager.setLoading(false)
                                        FeedbackManager.showErrorMessage(
                                            verif.exceptionOrNull()?.message ?: "Código incorrecto o expirado."
                                        )
                                        return@launch
                                    }
                                    FeedbackManager.setLoading(true, "Completando registro...")
                                    val res = authViewModel.completarRegistro(nombreRegistro, passwordRegistro)
                                    FeedbackManager.setLoading(false)
                                    res.onSuccess {
                                        FeedbackManager.showSuccess("¡Bienvenido a TOKE!, $nombreRegistro")
                                    }.onFailure { err -> FeedbackManager.showError(err) }
                                }
                            }
                        },
                    )
                }
                EnlaceAcceso(paleta, "¿Ya tienes cuenta? Inicia sesión") {
                    modoAuth = ModoAuth.LOGIN
                }
            }
        }
        EspacioAcceso(8)
    }

    if (mostrarDialogoConfirmarCierreOtrasSesiones) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoConfirmarCierreOtrasSesiones = false },
            containerColor = paleta.tarjeta,
            title = { Text("Advertencia de seguridad", fontWeight = FontWeight.Bold, color = paleta.textoPrincipal) },
            text = {
                Text(
                    "Al actualizar tu contraseña, por seguridad se cerrarán las sesiones activas en otros dispositivos.",
                    color = paleta.textoSecundario,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        mostrarDialogoConfirmarCierreOtrasSesiones = false
                        scope.launch {
                            FeedbackManager.setLoading(true, "Actualizando contraseña...")
                            val res = authViewModel.actualizarPasswordYRevocarOtros(nuevaPasswordRecuperacion)
                            FeedbackManager.setLoading(false)
                            res.onSuccess {
                                FeedbackManager.showSuccess("Contraseña actualizada. Otras sesiones han sido cerradas.")
                                modoAuth = ModoAuth.LOGIN
                            }.onFailure { err -> FeedbackManager.showError(err) }
                        }
                    }
                ) {
                    Text("Entendido y actualizar", fontWeight = FontWeight.Bold, color = paleta.enlaceVerde)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoConfirmarCierreOtrasSesiones = false }) {
                    Text("Cancelar", color = paleta.textoSecundario)
                }
            }
        )
    }
}
