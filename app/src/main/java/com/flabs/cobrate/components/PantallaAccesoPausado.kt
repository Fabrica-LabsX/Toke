package com.flabs.cobrate.components
import com.flabs.cobrate.ui.theme.TokeMedidas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.flabs.cobrate.ui.theme.PlusJakartaSans
import com.flabs.cobrate.ui.theme.tokePaleta

/**
 * Pantalla "Acceso pausado por el dueño": se muestra cuando la relación de la cuenta con el
 * negocio está pausada. Solo arma las piezas (AccesoPausado*); cada acción aparece únicamente
 * si quien la usa entrega el manejador correspondiente.
 */
@Composable
fun PantallaAccesoPausado(
    negocioNombre: String,
    onVerificarEstado: (() -> Unit)? = null,
    onCrearNegocio: (() -> Unit)? = null,
    onCambiarNegocio: (() -> Unit)? = null,
    onCerrarSesion: (() -> Unit)? = null,
    onSalirDelNegocio: (() -> Unit)? = null,
    onVolver: (() -> Unit)? = null,
) {
    val paleta = tokePaleta()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(paleta.fondo)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        if (onVolver != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Surface(
                    onClick = onVolver,
                    shape = RoundedCornerShape(12.dp),
                    color = paleta.tarjeta,
                    border = BorderStroke(1.dp, paleta.borde),
                    modifier = Modifier.size(TokeMedidas.BotonIcono),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(id = TokeIcons.FlechaAtras),
                            contentDescription = "Volver",
                            tint = paleta.textoPrincipal,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AccesoPausadoIcono(paleta = paleta)
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = "Acceso pausado por el dueño",
                    style = TextStyle(
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                        letterSpacing = (-0.5).sp,
                        textAlign = TextAlign.Center,
                    ),
                    color = paleta.textoPrincipal,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "El dueño ha pausado temporalmente tu rol en este comercio. " +
                        "No escucharás cobros hasta que reactive tu acceso.",
                    style = TextStyle(
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        textAlign = TextAlign.Center,
                    ),
                    color = paleta.textoSecundario,
                    modifier = Modifier.padding(horizontal = 8.dp),
                )
                Spacer(modifier = Modifier.height(20.dp))
                AccesoPausadoEstado(paleta = paleta, negocioNombre = negocioNombre)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (onVerificarEstado != null) {
                AccesoPausadoBoton(
                    paleta = paleta,
                    texto = "VERIFICAR SI YA ME REACTIVÓ",
                    icono = TokeIcons.Sincronizar,
                    relleno = true,
                    onClick = onVerificarEstado,
                )
            }
            if (onCambiarNegocio != null) {
                AccesoPausadoBoton(
                    paleta = paleta,
                    texto = "Cambiar o unirme a otro negocio",
                    icono = TokeIcons.Intercambiar,
                    relleno = false,
                    onClick = onCambiarNegocio,
                )
            }
            if (onCrearNegocio != null) {
                AccesoPausadoEnlace(paleta, "Crear mi propio negocio", null, onCrearNegocio)
            }
            if (onSalirDelNegocio != null) {
                AccesoPausadoEnlace(paleta, "Salir del negocio", null, onSalirDelNegocio)
            }
            if (onCerrarSesion != null) {
                AccesoPausadoEnlace(paleta, "Cerrar sesión", TokeIcons.CerrarSesion, onCerrarSesion)
            }
        }
    }
}
