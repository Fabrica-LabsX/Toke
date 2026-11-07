package com.flabs.cobrate.features.equipo
import com.flabs.cobrate.ui.theme.TokeRojo
import com.flabs.cobrate.ui.theme.TokeAmbarTexto

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.flabs.cobrate.features.business.TrabajadorRealInfo
import com.flabs.cobrate.ui.theme.PlusJakartaSans
import com.flabs.cobrate.ui.theme.TokePaleta

private val RojoExpulsar = TokeRojo

/** Confirmación antes de expulsar a un colaborador del negocio. */
@Composable
fun EquipoDialogoExpulsar(
    paleta: TokePaleta,
    trabajador: TrabajadorRealInfo,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        containerColor = paleta.tarjeta,
        title = {
            Text(
                text = "¿Expulsar a ${trabajador.username}?",
                style = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                ),
                color = paleta.textoPrincipal,
            )
        },
        text = {
            Text(
                text = "Esta acción desvinculará a ${trabajador.username} de este negocio. " +
                    "El usuario ya no podrá ver los pagos recibidos ni operar en este equipo.",
                style = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                ),
                color = paleta.textoSecundario,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirmar) {
                Text(
                    text = "Expulsar",
                    style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 15.sp),
                    color = RojoExpulsar,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) {
                Text(
                    text = "Cancelar",
                    style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 15.sp),
                    color = paleta.textoPrincipal,
                )
            }
        },
    )
}
