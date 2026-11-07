package com.flabs.cobrate.features.acceso
import com.flabs.cobrate.ui.theme.TokeMedidas
import com.flabs.cobrate.ui.theme.rememberPulsacion

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flabs.cobrate.R
import com.flabs.cobrate.components.TokeIcons
import com.flabs.cobrate.ui.theme.PlusJakartaSans
import com.flabs.cobrate.ui.theme.TokePaleta

/** Piezas comunes de las pantallas de acceso. Colores sólidos, paleta global de la app. */

/**
 * Fondo de pantalla completa con desplazamiento. Si el contenido es corto, ocupa toda la altura:
 * un `Spacer(Modifier.weight(1f))` dentro de [contenido] manda lo que va debajo al pie de la pantalla.
 */
@Composable
fun AccesoFondo(
    paleta: TokePaleta,
    horizontal: Alignment.Horizontal = Alignment.Start,
    contenido: @Composable ColumnScope.() -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(paleta.fondo)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
    ) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = this@BoxWithConstraints.maxHeight)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = horizontal,
                verticalArrangement = Arrangement.spacedBy(14.dp),
                content = contenido,
            )
        }
    }
}

/** Logo oficial de TOKE!: el `ic_toke_logo` blanco y transparente sobre una loseta verde redondeada. */
@Composable
fun LogoToke(paleta: TokePaleta, tamano: Int, descripcion: String, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape((tamano * 0.3f).dp),
        color = paleta.verde,
        modifier = modifier.size(tamano.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(id = R.drawable.ic_toke_logo),
                contentDescription = descripcion,
                modifier = Modifier.size((tamano * 1.35f).dp),
            )
        }
    }
}

@Composable
fun BotonAtras(paleta: TokePaleta, onClick: () -> Unit) {
    val pulso = rememberPulsacion()
    Surface(
        onClick = onClick,
        interactionSource = pulso.fuente,
        shape = RoundedCornerShape(12.dp),
        color = paleta.tarjeta,
        border = BorderStroke(1.dp, paleta.borde),
        modifier = Modifier.size(TokeMedidas.BotonIcono).then(pulso.modificador),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(id = TokeIcons.FlechaAtras),
                contentDescription = "Volver",
                tint = paleta.textoPrincipal,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
fun TituloAcceso(
    paleta: TokePaleta,
    titulo: String,
    subtitulo: String? = null,
    centrado: Boolean = false,
) {
    val alineacion = if (centrado) TextAlign.Center else TextAlign.Start
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = if (centrado) Alignment.CenterHorizontally else Alignment.Start,
    ) {
        Text(
            text = titulo,
            style = TextStyle(
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 26.sp,
                letterSpacing = (-0.5).sp,
            ),
            color = paleta.textoPrincipal,
            textAlign = alineacion,
        )
        if (subtitulo != null) {
            Text(
                text = subtitulo,
                style = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                ),
                color = paleta.textoSecundario,
                textAlign = alineacion,
            )
        }
    }
}

/** Campo con la etiqueta arriba. Si [esPassword], lleva la píldora "Mostrar / Ocultar". */
@Composable
fun CampoAcceso(
    paleta: TokePaleta,
    etiqueta: String,
    valor: String,
    placeholder: String,
    onCambia: (String) -> Unit,
    teclado: KeyboardType = KeyboardType.Text,
    esPassword: Boolean = false,
    visible: Boolean = false,
    onAlternarVisible: () -> Unit = {},
) {
    var enfocado by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = etiqueta,
            style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 13.sp),
            color = paleta.textoSecundario,
        )
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = paleta.tarjeta,
            border = BorderStroke(if (enfocado) 2.dp else 1.dp, if (enfocado) paleta.verde else paleta.bordeControl),
            modifier = Modifier.fillMaxWidth().height(51.dp),
        ) {
            Row14(paleta = paleta, esPassword = esPassword, visible = visible, onAlternarVisible = onAlternarVisible) {
                BasicTextField(
                    value = valor,
                    onValueChange = onCambia,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = if (esPassword) KeyboardType.Password else teclado),
                    visualTransformation = if (esPassword && !visible) PasswordVisualTransformation() else VisualTransformation.None,
                    textStyle = TextStyle(
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = paleta.textoPrincipal,
                    ),
                    cursorBrush = SolidColor(paleta.verde),
                    modifier = Modifier.fillMaxWidth().onFocusChanged { enfocado = it.isFocused },
                    decorationBox = { campo ->
                        if (valor.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = TextStyle(fontFamily = PlusJakartaSans, fontSize = 16.sp),
                                color = paleta.textoSecundario.copy(alpha = 0.75f),
                                maxLines = 1,
                            )
                        }
                        campo()
                    },
                )
            }
        }
    }
}

/** Fila interior del campo: el texto a la izquierda y, en contraseñas, la píldora a la derecha. */
@Composable
private fun Row14(
    paleta: TokePaleta,
    esPassword: Boolean,
    visible: Boolean,
    onAlternarVisible: () -> Unit,
    campo: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxSize().padding(start = 14.dp, end = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.weight(1f)) { campo() }
        if (esPassword) {
            Surface(
                onClick = onAlternarVisible,
                shape = RoundedCornerShape(9999.dp),
                color = paleta.tileFondo,
                border = BorderStroke(1.dp, paleta.borde),
            ) {
                Text(
                    text = if (visible) "Ocultar" else "Mostrar",
                    style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 12.sp),
                    color = paleta.textoPrincipal,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                )
            }
        }
    }
}

/** Botón en píldora: [principal] va en verde; si no, blanco con borde. */
@Composable
fun BotonAcceso(paleta: TokePaleta, texto: String, principal: Boolean, onClick: () -> Unit) {
    val pulso = rememberPulsacion()
    Surface(
        onClick = onClick,
        interactionSource = pulso.fuente,
        shape = RoundedCornerShape(9999.dp),
        color = if (principal) paleta.verde else paleta.tarjeta,
        border = if (principal) null else BorderStroke(1.dp, paleta.bordeControl),
        modifier = Modifier
            .fillMaxWidth()
            .height(TokeMedidas.BotonAltura)
            .then(pulso.modificador),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = texto,
                style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 16.sp),
                color = if (principal) paleta.verdeTexto else paleta.textoPrincipal,
                maxLines = 1,
            )
        }
    }
}

/** Enlace de texto. [sobrio] lo pinta en el color del texto (como "¿Olvidaste tu contraseña?"). */
@Composable
fun EnlaceAcceso(paleta: TokePaleta, texto: String, onClick: () -> Unit, sobrio: Boolean = false) {
    TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Text(
            text = texto,
            style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 14.sp),
            color = if (sobrio) paleta.textoPrincipal else paleta.enlaceVerde,
        )
    }
}

@Composable
fun EspacioAcceso(alto: Int) = Spacer(modifier = Modifier.height(alto.dp))

/** Aviso con título y texto corto, para explicar algo antes de que la persona siga. */
@Composable
fun AvisoAcceso(paleta: TokePaleta, titulo: String, texto: String) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = paleta.tarjeta,
        border = BorderStroke(1.dp, paleta.borde),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = titulo,
                style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp),
                color = paleta.textoPrincipal,
            )
            Text(
                text = texto,
                style = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.5.sp,
                    lineHeight = 19.sp,
                ),
                color = paleta.textoSecundario,
            )
        }
    }
}
