package com.flabs.cobrate.features.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
        // En pantallas altas la ilustración se centra en el espacio libre, en vez de quedar pegada arriba.
        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            IlustracionBienvenida(paleta)
        }
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
