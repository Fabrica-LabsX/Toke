package com.flabs.cobrate.features.auth

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import com.flabs.cobrate.R
import com.flabs.cobrate.features.acceso.AccesoFondo
import com.flabs.cobrate.features.acceso.BotonAcceso
import com.flabs.cobrate.features.acceso.EnlaceAcceso
import com.flabs.cobrate.features.acceso.EspacioAcceso
import com.flabs.cobrate.features.acceso.LogoToke
import com.flabs.cobrate.features.acceso.TituloAcceso
import com.flabs.cobrate.ui.theme.tokeEsOscuro
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
    AccesoFondo(paleta = paleta, horizontal = Alignment.CenterHorizontally) {
        EspacioAcceso(48)
        LogoToke(tokeEsOscuro(), tamano = 96, descripcion = stringResource(R.string.app_name))
        EspacioAcceso(16)
        TituloAcceso(
            paleta = paleta,
            titulo = "¡Tus cobros ahora llegan Al-Toke!",
            subtitulo = "Notificaciones de pago al instante para tu negocio.",
            centrado = true,
        )
        EspacioAcceso(32)
        BotonAcceso(paleta, stringResource(R.string.bienvenida_iniciar_sesion), principal = false, onClick = onIniciarSesion)
        BotonAcceso(paleta, "+ " + stringResource(R.string.bienvenida_crear_cuenta), principal = true, onClick = onCrearCuenta)
        EnlaceAcceso(paleta, "Tengo un código de trabajador", onIngresarCodigo)
    }
}
