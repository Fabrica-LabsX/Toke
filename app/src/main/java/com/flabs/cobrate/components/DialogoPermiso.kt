package com.flabs.cobrate.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.flabs.cobrate.ui.theme.PlusJakartaSans
import com.flabs.cobrate.ui.theme.tokePaleta

/** Ventana para pedir un permiso del sistema, con los colores de la app. */
@Composable
fun DialogoPermiso(
    titulo: String,
    texto: String,
    textoConfirmar: String,
    textoCancelar: String,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit,
) {
    val paleta = tokePaleta()
    AlertDialog(
        onDismissRequest = onCancelar,
        containerColor = paleta.tarjeta,
        title = {
            Text(
                text = titulo,
                style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp),
                color = paleta.textoPrincipal,
            )
        },
        text = {
            Text(
                text = texto,
                style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Medium, fontSize = 15.sp, lineHeight = 22.sp),
                color = paleta.textoSecundario,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirmar) {
                Text(textoConfirmar, fontWeight = FontWeight.Bold, color = paleta.enlaceVerde)
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) {
                Text(textoCancelar, fontWeight = FontWeight.Bold, color = paleta.textoSecundario)
            }
        },
    )
}
