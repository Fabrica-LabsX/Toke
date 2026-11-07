package com.flabs.cobrate.features.acceso

import androidx.compose.foundation.layout.ColumnScope
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
    EspacioAcceso(4)
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
    EspacioAcceso(4)
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
    EnlaceAcceso(paleta, "¿Olvidaste tu contraseña?", onOlvide)
    BotonAcceso(paleta, "Entrar a mi cuenta", principal = true, onClick = onEntrar)
    BotonAcceso(paleta, "Crear una cuenta nueva", principal = false, onClick = onCrear)
}
