package com.flabs.cobrate.features.acceso

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import com.flabs.cobrate.ui.theme.TokePaleta

/**
 * Formularios sin estado de las pantallas de acceso. Quien los llama guarda los valores y hace
 * las llamadas de autenticación; aquí solo se dibuja y se avisa de lo que toca el usuario.
 */

/** Formulario de un solo campo (nombre, correo o código) con su botón y un enlace opcional. */
@Composable
fun ColumnScope.FormUnCampo(
    paleta: TokePaleta,
    etiqueta: String,
    valor: String,
    placeholder: String,
    teclado: KeyboardType,
    onCambia: (String) -> Unit,
    textoBoton: String,
    onAccion: () -> Unit,
    textoEnlace: String? = null,
    onEnlace: () -> Unit = {},
) {
    CampoAcceso(paleta, etiqueta, valor, placeholder, onCambia, teclado)
    Spacer(modifier = Modifier.weight(1f))
    BotonAcceso(paleta, textoBoton, principal = true, onClick = onAccion)
    if (textoEnlace != null) EnlaceAcceso(paleta, textoEnlace, onEnlace)
}

/** Formulario de dos contraseñas (nueva y confirmación) con botón y un enlace opcional. */
@Composable
fun ColumnScope.FormPasswords(
    paleta: TokePaleta,
    etiqueta: String,
    password: String,
    onPassword: (String) -> Unit,
    confirmacion: String,
    onConfirmacion: (String) -> Unit,
    textoBoton: String,
    onAccion: () -> Unit,
    textoEnlace: String? = null,
    onEnlace: () -> Unit = {},
) {
    var visible by rememberSaveable { mutableStateOf(false) }
    CampoAcceso(
        paleta, etiqueta, password, "Mínimo ${AccesoLogica.LARGO_MINIMO_PASSWORD} caracteres", onPassword,
        esPassword = true, visible = visible, onAlternarVisible = { visible = !visible },
    )
    CampoAcceso(
        paleta, "Confirmar contraseña", confirmacion, "Repite la contraseña", onConfirmacion,
        esPassword = true, visible = visible, onAlternarVisible = { visible = !visible },
    )
    Spacer(modifier = Modifier.weight(1f))
    BotonAcceso(paleta, textoBoton, principal = true, onClick = onAccion)
    if (textoEnlace != null) EnlaceAcceso(paleta, textoEnlace, onEnlace)
}

/** Formulario de "Iniciar sesión": correo, contraseña, olvidé mi contraseña y crear cuenta. */
@Composable
fun ColumnScope.FormLogin(
    paleta: TokePaleta,
    email: String,
    onEmail: (String) -> Unit,
    password: String,
    onPassword: (String) -> Unit,
    passwordVisible: Boolean,
    onAlternarVisible: () -> Unit,
    onOlvide: () -> Unit,
    onEntrar: () -> Unit,
    onCrear: () -> Unit,
) {
    CampoAcceso(paleta, "Correo electrónico", email, "tu@correo.com", onEmail, KeyboardType.Email)
    CampoAcceso(
        paleta, "Contraseña", password, "Tu contraseña", onPassword,
        esPassword = true, visible = passwordVisible, onAlternarVisible = onAlternarVisible,
    )
    EnlaceAcceso(paleta, "¿Olvidaste tu contraseña?", onOlvide, sobrio = true)
    Spacer(modifier = Modifier.weight(1f))
    BotonAcceso(paleta, "Entrar a mi cuenta", principal = true, onClick = onEntrar)
    BotonAcceso(paleta, "Crear una cuenta nueva", principal = false, onClick = onCrear)
}

/** Paso 1 del registro: nombre de usuario con su aviso, y el correo. */
@Composable
fun ColumnScope.FormRegistroDatos(
    paleta: TokePaleta,
    nombre: String,
    onNombre: (String) -> Unit,
    email: String,
    onEmail: (String) -> Unit,
    onContinuar: () -> Unit,
) {
    CampoAcceso(paleta, "Nombre de usuario", nombre, "Ej: Carlos", onNombre)
    AvisoAcceso(
        paleta = paleta,
        titulo = "Para qué sirve este nombre",
        texto = "Es solo el nombre con el que tu equipo te va a ver. Por ejemplo, cuando invites a un " +
            "trabajador le aparecerá «Carlos te invitó». Para entrar a la app usarás tu correo.",
    )
    CampoAcceso(paleta, "Correo electrónico", email, "tu@correo.com", onEmail, KeyboardType.Email)
    Spacer(modifier = Modifier.weight(1f))
    BotonAcceso(paleta, "Enviar código a mi correo", principal = true, onClick = onContinuar)
}

/** Paso 2 del registro: el código que llegó al correo y la contraseña, en una sola pantalla. */
@Composable
fun ColumnScope.FormRegistroClave(
    paleta: TokePaleta,
    email: String,
    codigo: String,
    onCodigo: (String) -> Unit,
    password: String,
    onPassword: (String) -> Unit,
    confirmacion: String,
    onConfirmacion: (String) -> Unit,
    onCompletar: () -> Unit,
) {
    var visible by rememberSaveable { mutableStateOf(false) }
    AvisoAcceso(
        paleta = paleta,
        titulo = "Revisa tu correo",
        texto = "Te enviamos un código a $email. Escríbelo aquí y crea tu contraseña.",
    )
    CampoAcceso(paleta, "Código de confirmación", codigo, "Código", onCodigo, KeyboardType.Number)
    CampoAcceso(
        paleta, "Crea tu contraseña", password, "Mínimo ${AccesoLogica.LARGO_MINIMO_PASSWORD} caracteres", onPassword,
        esPassword = true, visible = visible, onAlternarVisible = { visible = !visible },
    )
    CampoAcceso(
        paleta, "Confirmar contraseña", confirmacion, "Repite la contraseña", onConfirmacion,
        esPassword = true, visible = visible, onAlternarVisible = { visible = !visible },
    )
    Spacer(modifier = Modifier.weight(1f))
    BotonAcceso(paleta, "Crear mi cuenta", principal = true, onClick = onCompletar)
}
