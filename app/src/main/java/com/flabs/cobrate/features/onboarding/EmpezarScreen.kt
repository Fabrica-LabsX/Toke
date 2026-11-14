package com.flabs.cobrate.features.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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
import com.flabs.cobrate.features.acceso.TarjetaOpcionAcceso
import com.flabs.cobrate.features.acceso.validarCodigoInvitacion
import com.flabs.cobrate.ui.theme.PlusJakartaSans
import com.flabs.cobrate.ui.theme.tokePaleta
import kotlinx.coroutines.launch

/**
 * Pantalla "Selección de Inicio" expandida y llena, perfectamente escalada a 9:16 sin scroll.
 * Ambas opciones se muestran completamente expandidas con sus explicaciones, campos y botones,
 * aprovechando el 100% de la pantalla sin requerir deslizamientos molestos.
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
    val focusManager = LocalFocusManager.current

    AccesoFondo(paleta = paleta, esDesplazable = false) {
        if (onVolver != null) {
            BotonAtras(paleta, onVolver)
        }

        // Cabecera compacta
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = stringResource(R.string.empezar_titulo),
                style = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    lineHeight = 24.sp,
                ),
                color = paleta.textoPrincipal,
            )
            Text(
                text = stringResource(R.string.empezar_subtitulo),
                style = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    lineHeight = 15.sp,
                ),
                color = paleta.textoSecundario,
            )
        }

        // Opción 1: Modo Dueño (Expandida)
        TarjetaOpcionAcceso(
            paleta = paleta,
            icono = TokeIcons.Tienda,
            etiqueta = stringResource(R.string.empezar_modo_dueno),
            titulo = stringResource(R.string.empezar_soy_dueno),
            texto = stringResource(R.string.empezar_texto_dueno),
        ) {
            BotonAcceso(
                paleta = paleta,
                texto = stringResource(R.string.empezar_crear_negocio),
                principal = true,
                altura = 39.dp,
                onClick = onCrearNegocio,
            )
        }

        // Opción 2: Modo Equipo (Expandida con campo y botón propios)
        TarjetaOpcionAcceso(
            paleta = paleta,
            icono = TokeIcons.Llave,
            etiqueta = stringResource(R.string.empezar_modo_equipo),
            titulo = stringResource(R.string.empezar_tengo_codigo),
            texto = stringResource(R.string.empezar_texto_equipo),
        ) {
            // Cuadro de código expandido
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = paleta.fondo,
                border = BorderStroke(
                    width = 1.dp,
                    color = if (codigoInput.isNotBlank()) paleta.verde else paleta.bordeControl,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (codigoInput.isEmpty()) {
                        Text(
                            text = stringResource(R.string.empezar_placeholder_codigo),
                            style = TextStyle(
                                fontFamily = PlusJakartaSans,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Normal,
                            ),
                            color = paleta.textoSecundario,
                        )
                    }
                    BasicTextField(
                        value = codigoInput,
                        onValueChange = { nuevo ->
                            codigoInput = nuevo.uppercase().filter { !it.isWhitespace() }.take(10)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Ascii,
                            capitalization = KeyboardCapitalization.Characters,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                if (codigoInput.isNotBlank()) {
                                    scope.launch {
                                        validarCodigoInvitacion(codigoInput.trim())?.let(onValidarCodigo)
                                    }
                                }
                            }
                        ),
                        textStyle = TextStyle(
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = paleta.textoPrincipal,
                        ),
                        cursorBrush = SolidColor(paleta.verde),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            // Botón de Unirme a pantalla completa
            BotonAcceso(
                paleta = paleta,
                texto = stringResource(R.string.empezar_unirme),
                principal = codigoInput.isNotBlank(),
                altura = 39.dp,
                onClick = {
                    focusManager.clearFocus()
                    if (codigoInput.isNotBlank()) {
                        scope.launch {
                            validarCodigoInvitacion(codigoInput.trim())?.let(onValidarCodigo)
                        }
                    }
                },
            )

            Text(
                text = "El código expira a los 5 minutos y es para un solo uso.",
                style = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontSize = 10.5.sp,
                    lineHeight = 13.sp,
                ),
                color = paleta.textoSecundario,
            )
        }

        // Espaciador flexible para equilibrar altura según proporción de pantalla
        Spacer(modifier = Modifier.weight(1f))

        // Pie de pantalla: Botón de Cerrar Sesión y Nota legal/informativa
        if (onCerrarSesion != null) {
            Surface(
                onClick = onCerrarSesion,
                shape = RoundedCornerShape(9999.dp),
                color = paleta.textoPrincipal,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(39.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(id = TokeIcons.CerrarSesion),
                        contentDescription = null,
                        tint = paleta.fondo,
                        modifier = Modifier.size(15.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.dialogo_cerrar_confirmar),
                        color = paleta.fondo,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        Text(
            text = stringResource(R.string.empezar_nota_footer),
            style = TextStyle(
                fontFamily = PlusJakartaSans,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
            ),
            color = paleta.textoSecundario,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
