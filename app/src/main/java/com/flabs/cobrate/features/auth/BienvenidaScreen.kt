package com.flabs.cobrate.features.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flabs.cobrate.R
import com.flabs.cobrate.components.TokeIcons
import com.flabs.cobrate.features.acceso.AccesoFondo
import com.flabs.cobrate.features.acceso.BotonAcceso
import com.flabs.cobrate.features.acceso.EnlaceAcceso
import com.flabs.cobrate.features.acceso.LogoToke
import com.flabs.cobrate.features.acceso.TituloAcceso
import com.flabs.cobrate.ui.theme.PlusJakartaSans
import com.flabs.cobrate.ui.theme.TokePaleta
import com.flabs.cobrate.ui.theme.TokePaletaClara
import com.flabs.cobrate.ui.theme.TokePaletaOscura
import com.flabs.cobrate.ui.theme.tokePaleta

/**
 * Pantalla de bienvenida: se muestra al abrir la app sin sesión o al cerrarla.
 * Ofrece iniciar sesión, crear la cuenta o entrar con un código de trabajador.
 */
@Composable
fun BienvenidaScreen(
    onIniciarSesion: () -> Unit,
    onCrearCuenta: () -> Unit,
    onIngresarCodigo: () -> Unit = {}
) {
    val paleta = tokePaleta()
    AccesoFondo(paleta = paleta) {
        ComposicionFlotante(paleta)
        Spacer(modifier = Modifier.weight(1f))
        TituloAcceso(
            paleta = paleta,
            titulo = "¡Tus cobros ahora llegan Al-Toke!",
            subtitulo = "Notificaciones de pago al instante para tu negocio.",
        )
        Spacer(modifier = Modifier.height(10.dp))
        BotonAcceso(paleta, stringResource(R.string.bienvenida_iniciar_sesion), principal = false, onClick = onIniciarSesion)
        BotonAcceso(paleta, "+ " + stringResource(R.string.bienvenida_crear_cuenta), principal = true, onClick = onCrearCuenta)
        EnlaceAcceso(paleta, "Tengo un código de trabajador", onIngresarCodigo)
    }
}

/** Parte alta de la bienvenida: logo, tarjeta del celular y aviso de pago, ligeramente girados. */
@Composable
private fun ComposicionFlotante(paleta: TokePaleta) {
    Box(modifier = Modifier.fillMaxWidth().height(270.dp)) {
        LogoToke(
            paleta = paleta,
            tamano = 104,
            descripcion = stringResource(R.string.app_name),
            modifier = Modifier.offset(x = 12.dp, y = 24.dp).rotate(-3f).shadow(10.dp, RoundedCornerShape(31.dp)),
        )
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = paleta.tarjeta,
            border = BorderStroke(1.dp, paleta.borde),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(y = 4.dp)
                .size(92.dp)
                .rotate(2f)
                .shadow(8.dp, RoundedCornerShape(22.dp)),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(id = TokeIcons.Celular),
                    contentDescription = null,
                    tint = paleta.textoPrincipal,
                    modifier = Modifier.size(38.dp),
                )
            }
        }
        // La tarjeta de aviso va siempre oscura, como en el diseño, en claro y en oscuro.
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = TokePaletaClara.textoPrincipal,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(72.dp)
                .rotate(1f)
                .shadow(14.dp, RoundedCornerShape(22.dp)),
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(13.dp), color = paleta.verde, modifier = Modifier.size(40.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(id = TokeIcons.VolumenAlto),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Pago recibido",
                        style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp),
                        color = Color.White,
                    )
                    Text(
                        text = "Te avisamos al instante",
                        style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Medium, fontSize = 12.sp),
                        color = TokePaletaOscura.textoSecundario,
                    )
                }
            }
        }
    }
}
