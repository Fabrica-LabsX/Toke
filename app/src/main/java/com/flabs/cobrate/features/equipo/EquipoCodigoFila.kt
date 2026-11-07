package com.flabs.cobrate.features.equipo
import com.flabs.cobrate.ui.theme.TokeRojo
import com.flabs.cobrate.ui.theme.TokeAmbarTexto

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flabs.cobrate.components.TokeIcons
import com.flabs.cobrate.core.model.InvitacionTrabajador
import com.flabs.cobrate.ui.theme.PlusJakartaSans
import com.flabs.cobrate.ui.theme.TokePaleta

private val RojoRevocar = TokeRojo

/** Fila de un código de invitación vigente, con el botón para revocarlo. */
@Composable
fun EquipoCodigoFila(
    paleta: TokePaleta,
    invitacion: InvitacionTrabajador,
    onRevocar: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = paleta.tarjeta,
        border = BorderStroke(1.dp, paleta.borde),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = paleta.tileFondo,
                border = BorderStroke(1.dp, paleta.bordeControl),
                modifier = Modifier.size(44.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(id = TokeIcons.Llave),
                        contentDescription = null,
                        tint = paleta.textoSecundario,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = invitacion.codigo,
                style = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    letterSpacing = 1.sp,
                ),
                color = paleta.textoPrincipal,
                maxLines = 1,
                modifier = Modifier.weight(1f),
            )
            Surface(
                onClick = onRevocar,
                shape = RoundedCornerShape(12.dp),
                color = paleta.tarjeta,
                border = BorderStroke(1.dp, RojoRevocar),
                modifier = Modifier.height(40.dp),
            ) {
                Box(modifier = Modifier.padding(horizontal = 14.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Revocar",
                        style = TextStyle(
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                        ),
                        color = RojoRevocar,
                    )
                }
            }
        }
    }
}
