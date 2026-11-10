package com.flabs.cobrate.features.onboarding
import com.flabs.cobrate.ui.theme.solidoSobre
import com.flabs.cobrate.ui.theme.TokeMedidas
import com.flabs.cobrate.ui.theme.TokeRojo
import com.flabs.cobrate.ui.theme.TokeAmbarTexto

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.flabs.cobrate.ui.theme.PlusJakartaSans
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.flabs.cobrate.R
import com.flabs.cobrate.components.TokeIcons
import com.flabs.cobrate.core.model.InvitacionTrabajador
import com.flabs.cobrate.features.acceso.AccesoFondo
import com.flabs.cobrate.features.acceso.BotonAcceso
import com.flabs.cobrate.features.acceso.BotonAtras
import com.flabs.cobrate.features.acceso.CampoAcceso
import com.flabs.cobrate.features.acceso.EspacioAcceso
import com.flabs.cobrate.features.acceso.TarjetaOpcionAcceso
import com.flabs.cobrate.features.acceso.TituloAcceso
import com.flabs.cobrate.features.acceso.validarCodigoInvitacion
import com.flabs.cobrate.ui.theme.tokePaleta
import kotlinx.coroutines.launch

/**
 * Pantalla "Selección de Inicio": quien entra sin negocio elige entre crear el suyo o unirse
 * con el código de invitación que le dio un dueño.
 */
@Composable
fun EmpezarScreen(
    onCrearNegocio: () -> Unit,
    onValidarCodigo: (InvitacionTrabajador) -> Unit,
    onCerrarSesion: (() -> Unit)? = null,
    onVolver: (() -> Unit)? = null
) {
    var codigoInput by rememberSaveable { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val paleta = tokePaleta()

    AccesoFondo(paleta = paleta) {
        if (onVolver != null) BotonAtras(paleta, onVolver)
        TituloAcceso(
            paleta = paleta,
            titulo = stringResource(R.string.empezar_titulo),
            subtitulo = stringResource(R.string.empezar_subtitulo),
        )

        TarjetaOpcionAcceso(
            paleta = paleta,
            icono = TokeIcons.Tienda,
            etiqueta = stringResource(R.string.empezar_modo_dueno),
            titulo = stringResource(R.string.empezar_soy_dueno),
            texto = stringResource(R.string.empezar_texto_dueno),
        ) {
            BotonAcceso(paleta, stringResource(R.string.empezar_crear_negocio), principal = true, onClick = onCrearNegocio)
        }

        TarjetaOpcionAcceso(
            paleta = paleta,
            icono = TokeIcons.Llave,
            etiqueta = stringResource(R.string.empezar_modo_equipo),
            titulo = stringResource(R.string.empezar_tengo_codigo),
            texto = stringResource(R.string.empezar_texto_equipo),
        ) {
            CampoAcceso(
                paleta = paleta,
                etiqueta = stringResource(R.string.label_codigo_invitacion),
                valor = codigoInput,
                placeholder = stringResource(R.string.empezar_placeholder_codigo),
                onCambia = { codigoInput = it.uppercase() },
                teclado = KeyboardType.Ascii,
            )
            BotonAcceso(paleta, stringResource(R.string.empezar_unirme), principal = codigoInput.isNotBlank()) {
                if (codigoInput.isNotBlank()) {
                    scope.launch { validarCodigoInvitacion(codigoInput.trim())?.let(onValidarCodigo) }
                }
            }
            Text(
                text = stringResource(R.string.empezar_nota_codigo),
                style = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontSize = 12.5.sp,
                    lineHeight = 17.sp,
                ),
                color = paleta.textoSecundario,
            )
        }

        if (onCerrarSesion != null) {
            val rojo = TokeRojo
            Surface(
                onClick = onCerrarSesion,
                shape = RoundedCornerShape(9999.dp),
                color = paleta.tarjeta,
                border = BorderStroke(1.dp, rojo.solidoSobre(paleta.fondo, 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(TokeMedidas.BotonAltura),
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(id = TokeIcons.CerrarSesion),
                        contentDescription = null,
                        tint = rojo,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.dialogo_cerrar_confirmar),
                        color = rojo,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        Text(
            text = stringResource(R.string.empezar_nota_footer),
            style = TextStyle(fontFamily = PlusJakartaSans, fontSize = 12.5.sp),
            color = paleta.textoSecundario,
        )
        EspacioAcceso(8)
    }
}
