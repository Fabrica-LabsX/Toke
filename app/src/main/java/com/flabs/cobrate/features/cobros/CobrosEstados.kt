package com.flabs.cobrate.features.cobros

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flabs.cobrate.components.TokeIcons
import com.flabs.cobrate.ui.theme.PlusJakartaSans
import com.flabs.cobrate.ui.theme.TokePaleta

/** Indicador mientras se cargan los métodos. */
@Composable
fun CobrosCargando(paleta: TokePaleta, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = paleta.activo)
    }
}

/** Aviso cuando falló la carga. */
@Composable
fun CobrosError(paleta: TokePaleta, mensaje: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            painter = painterResource(id = TokeIcons.Advertencia),
            contentDescription = null,
            tint = paleta.ambar,
            modifier = Modifier.size(44.dp),
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = mensaje,
            style = TextStyle(
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
            ),
            color = paleta.textoPrincipal,
        )
    }
}

/** Tarjeta cuando el negocio todavía no tiene ningún método de cobro. */
@Composable
fun CobrosVacio(paleta: TokePaleta) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = paleta.tarjeta,
        border = androidx.compose.foundation.BorderStroke(1.dp, paleta.borde),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Surface(shape = RoundedCornerShape(16.dp), color = paleta.tileFondo, modifier = Modifier.size(64.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(id = TokeIcons.Tarjeta),
                        contentDescription = null,
                        tint = paleta.textoSecundario,
                        modifier = Modifier.size(30.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Sin métodos de cobro",
                style = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                ),
                color = paleta.textoPrincipal,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Agrega tu primer método para recibir pagos con QR.",
                style = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                ),
                color = paleta.textoSecundario,
            )
        }
    }
}
