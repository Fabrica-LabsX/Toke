package com.flabs.cobrate.features.cobros

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.flabs.cobrate.core.model.MetodoDeCobro
import com.flabs.cobrate.ui.theme.PlusJakartaSans
import com.flabs.cobrate.ui.theme.TokePaleta

/** Ventana para editar el dato de cobro y el titular de un método, o para ocultarlo. */
@Composable
fun EditarMetodoDialogo(
    paleta: TokePaleta,
    metodo: MetodoDeCobro,
    onDismiss: () -> Unit,
    onConfirm: (id: String, dato: String, titular: String, oculto: Boolean) -> Unit,
) {
    var dato by remember { mutableStateOf(metodo.datoDeCobro) }
    var titular by remember { mutableStateOf(metodo.nombreDestinatario ?: "") }
    var oculto by remember { mutableStateOf(metodo.oculto) }

    Dialogo(paleta = paleta, titulo = "Editar método", onDismiss = onDismiss) {
        Campo(paleta, dato, "Dato de cobro") { dato = it }
        Campo(paleta, titular, "Titular") { titular = it }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { oculto = !oculto },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = oculto,
                onCheckedChange = { oculto = it },
                colors = CheckboxDefaults.colors(checkedColor = paleta.activo),
            )
            Text(text = "Ocultar método", style = textoDialogo(), color = paleta.textoPrincipal)
        }
        Botones(
            paleta = paleta,
            textoConfirmar = "Guardar",
            onCancelar = onDismiss,
            onConfirmar = { onConfirm(metodo.id, dato, titular, oculto) },
        )
    }
}

@Composable
private fun Dialogo(
    paleta: TokePaleta,
    titulo: String,
    onDismiss: () -> Unit,
    contenido: @Composable () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = paleta.tarjeta,
            border = BorderStroke(1.dp, paleta.borde),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    text = titulo,
                    style = TextStyle(
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                    ),
                    color = paleta.textoPrincipal,
                )
                contenido()
            }
        }
    }
}

@Composable
private fun Campo(paleta: TokePaleta, valor: String, etiqueta: String, onCambia: (String) -> Unit) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambia,
        label = { Text(etiqueta, style = textoDialogo()) },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        textStyle = textoDialogo().copy(color = paleta.textoPrincipal),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = paleta.activo,
            unfocusedBorderColor = paleta.bordeControl,
            focusedLabelColor = paleta.activo,
            unfocusedLabelColor = paleta.textoSecundario,
            cursorColor = paleta.activo,
        ),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun Botones(
    paleta: TokePaleta,
    textoConfirmar: String,
    onCancelar: () -> Unit,
    onConfirmar: () -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        TextButton(onClick = onCancelar) {
            Text("Cancelar", style = textoDialogo().copy(fontWeight = FontWeight.Bold), color = paleta.textoSecundario)
        }
        TextButton(onClick = onConfirmar) {
            Text(
                text = textoConfirmar,
                style = textoDialogo().copy(fontWeight = FontWeight.Bold),
                color = paleta.activo,
            )
        }
    }
}

private fun textoDialogo() = TextStyle(
    fontFamily = PlusJakartaSans,
    fontWeight = FontWeight.Medium,
    fontSize = 15.sp,
)

/** Confirmación antes de borrar un método de cobro. Borrar no se puede deshacer. */
@Composable
fun EliminarMetodoDialogo(
    paleta: TokePaleta,
    metodo: MetodoDeCobro,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val rojo = androidx.compose.ui.graphics.Color(0xFFDC2626)
    Dialogo(
        paleta = paleta,
        titulo = "¿Eliminar ${MetodosCobroLogica.nombreLegible(metodo.tipo)}?",
        onDismiss = onDismiss,
    ) {
        Text(
            text = "Se borrará este método de cobro y la imagen de su QR. " +
                "Tus clientes ya no lo verán. Esta acción no se puede deshacer.",
            style = textoDialogo().copy(lineHeight = 21.sp),
            color = paleta.textoSecundario,
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", style = textoDialogo().copy(fontWeight = FontWeight.Bold), color = paleta.textoSecundario)
            }
            TextButton(onClick = onConfirm) {
                Text("Eliminar", style = textoDialogo().copy(fontWeight = FontWeight.Bold), color = rojo)
            }
        }
    }
}
