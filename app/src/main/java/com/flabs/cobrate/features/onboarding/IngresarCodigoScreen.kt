package com.flabs.cobrate.features.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flabs.cobrate.R
import com.flabs.cobrate.components.TokeIcons
import com.flabs.cobrate.core.model.InvitacionTrabajador
import com.flabs.cobrate.features.acceso.AccesoFondo
import com.flabs.cobrate.features.acceso.BotonAcceso
import com.flabs.cobrate.features.acceso.BotonAtras
import com.flabs.cobrate.features.acceso.IconoAccesoAnimado
import com.flabs.cobrate.features.acceso.TituloAcceso
import com.flabs.cobrate.features.acceso.validarCodigoInvitacion
import com.flabs.cobrate.ui.theme.PlusJakartaSans
import com.flabs.cobrate.ui.theme.TokePaleta
import com.flabs.cobrate.ui.theme.tokePaleta
import kotlinx.coroutines.launch

/**
 * Pantalla "Soy trabajador": pide el código de invitación. Arriba, la llave animada y la explicación;
 * abajo, junto al pulgar, el cuadro grande del código y el botón. Al validarlo se asegura de que
 * exista una sesión anónima en Supabase antes de continuar.
 */
@Composable
fun IngresarCodigoScreen(
    onCodigoValido: (InvitacionTrabajador) -> Unit,
    onVolver: () -> Unit
) {
    var codigo by rememberSaveable { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val paleta = tokePaleta()

    AccesoFondo(paleta = paleta) {
        BotonAtras(paleta, onVolver)
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            IconoAccesoAnimado(paleta, TokeIcons.Llave, tamano = 84, balanceo = true)
            TituloAcceso(
                paleta = paleta,
                titulo = stringResource(R.string.unirse_invitacion_titulo),
                subtitulo = stringResource(R.string.unirse_invitacion_subtitulo),
                centrado = true,
            )
        }
        // El cuadro del código va abajo, al alcance del pulgar.
        Spacer(modifier = Modifier.weight(1f))
        CampoCodigoGrande(
            paleta = paleta,
            etiqueta = stringResource(R.string.label_codigo_invitacion),
            valor = codigo,
            placeholder = stringResource(R.string.empezar_placeholder_codigo),
            onCambia = { codigo = it.uppercase().filter { c -> !c.isWhitespace() }.take(12) },
        )
        BotonAcceso(paleta, stringResource(R.string.empezar_unirme), principal = codigo.isNotBlank()) {
            if (codigo.isNotBlank()) {
                scope.launch { validarCodigoInvitacion(codigo.trim())?.let(onCodigoValido) }
            }
        }
    }
}

/** Cuadro del código: alto, con letras grandes, separadas y centradas para leerlo y copiarlo sin errores. */
@Composable
private fun CampoCodigoGrande(
    paleta: TokePaleta,
    etiqueta: String,
    valor: String,
    placeholder: String,
    onCambia: (String) -> Unit,
) {
    var enfocado by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = etiqueta,
            style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
            color = paleta.textoSecundario,
            modifier = Modifier.padding(start = 4.dp),
        )
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = paleta.tarjeta,
            border = BorderStroke(if (enfocado) 2.5.dp else 1.5.dp, if (enfocado) paleta.verde else paleta.bordeControl),
            modifier = Modifier.fillMaxWidth().height(76.dp),
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 14.dp)) {
                BasicTextField(
                    value = valor,
                    onValueChange = onCambia,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, keyboardType = KeyboardType.Ascii),
                    textStyle = TextStyle(
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 30.sp,
                        letterSpacing = 6.sp,
                        color = paleta.textoPrincipal,
                        textAlign = TextAlign.Center,
                    ),
                    cursorBrush = SolidColor(paleta.verde),
                    modifier = Modifier.fillMaxWidth().onFocusChanged { enfocado = it.isFocused },
                    decorationBox = { campo ->
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                            if (valor.isEmpty()) {
                                Text(
                                    text = placeholder,
                                    style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 22.sp, letterSpacing = 4.sp, textAlign = TextAlign.Center),
                                    color = paleta.textoSecundario,
                                    maxLines = 1,
                                )
                            }
                            campo()
                        }
                    },
                )
            }
        }
    }
}
