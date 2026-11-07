package com.flabs.cobrate.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flabs.cobrate.R
import com.flabs.cobrate.features.acceso.BotonAcceso
import com.flabs.cobrate.ui.theme.PlusJakartaSans
import com.flabs.cobrate.ui.theme.TokeMedidas
import com.flabs.cobrate.ui.theme.tokePaleta

/** Aviso al trabajador de que el dueño rechazó su solicitud de ingreso. */
@Composable
fun PantallaSolicitudRechazada(
    negocioNombre: String,
    onEntendido: () -> Unit
) {
    val paleta = tokePaleta()
    Box(modifier = Modifier.fillMaxSize()) {
        FondoPanelBorroso()
        Box(
            modifier = Modifier.fillMaxSize().padding(horizontal = TokeMedidas.MargenLateral),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = paleta.tarjeta,
                border = BorderStroke(1.dp, paleta.borde),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        painter = painterResource(id = TokeIcons.Bloqueado),
                        contentDescription = null,
                        tint = androidx.compose.ui.graphics.Color.Unspecified,
                        modifier = Modifier.size(64.dp),
                    )
                    Text(
                        text = stringResource(R.string.rechazo_titulo),
                        style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp),
                        color = paleta.textoPrincipal,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = stringResource(R.string.rechazo_texto, negocioNombre),
                        style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Medium, fontSize = 15.sp, lineHeight = 21.sp),
                        color = paleta.textoSecundario,
                        textAlign = TextAlign.Center,
                    )
                    BotonAcceso(paleta, stringResource(R.string.rechazo_entendido), principal = true, onClick = onEntendido)
                }
            }
        }
    }
}
