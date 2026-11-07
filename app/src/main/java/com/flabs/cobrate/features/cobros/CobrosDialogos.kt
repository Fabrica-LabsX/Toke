package com.flabs.cobrate.features.cobros

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.flabs.cobrate.core.model.MetodoDeCobro
import com.flabs.cobrate.ui.theme.PlusJakartaSans
import com.flabs.cobrate.ui.theme.TokePaleta

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
