package com.flabs.cobrate.components

import androidx.compose.runtime.Composable

/** Ventana para pedir un permiso del sistema, con el mismo estilo que el resto de confirmaciones. */
@Composable
fun DialogoPermiso(
    titulo: String,
    texto: String,
    textoConfirmar: String,
    textoCancelar: String,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit,
) {
    DialogoToke(
        titulo = titulo,
        texto = texto,
        textoConfirmar = textoConfirmar,
        textoCancelar = textoCancelar,
        onConfirmar = onConfirmar,
        onCancelar = onCancelar,
    )
}
