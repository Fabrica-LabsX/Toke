package com.flabs.cobrate.features.acceso

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

/** Fondo de pantalla completa con desplazamiento y márgenes de las barras del sistema. */
@Composable
fun AccesoFondo(
    paleta: TokePaleta,
    horizontal: Alignment.Horizontal = Alignment.Start,
    contenido: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(paleta.fondo)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = horizontal,
        verticalArrangement = Arrangement.spacedBy(14.dp),
        content = contenido,
    )
}

/** Logo oficial de TOKE!: la versión verde en claro y la blanca en oscuro. */
@Composable
fun LogoToke(esOscuro: Boolean, tamano: Int, descripcion: String) {
    Image(
        painter = painterResource(id = if (esOscuro) R.drawable.ic_toke_logo else R.drawable.ic_toke_logo_green),
        contentDescription = descripcion,
        modifier = Modifier.size(tamano.dp),
    )
}

@Composable
fun BotonAtras(paleta: TokePaleta, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = paleta.tarjeta,
        border = BorderStroke(1.dp, paleta.borde),
        modifier = Modifier.size(42.dp),
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

/** Campo con la etiqueta arriba. Si [esPassword], lleva el enlace "Mostrar / Ocultar". */
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
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = etiqueta,
            style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 14.sp),
            color = paleta.textoPrincipal,
        )
        OutlinedTextField(
            value = valor,
            onValueChange = onCambia,
            placeholder = {
                Text(
                    text = placeholder,
                    style = TextStyle(fontFamily = PlusJakartaSans, fontSize = 16.sp),
                    color = paleta.textoSecundario,
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = if (esPassword) KeyboardType.Password else teclado),
            visualTransformation = if (esPassword && !visible) PasswordVisualTransformation() else VisualTransformation.None,
            trailingIcon = if (esPassword) {
                {
                    TextButton(onClick = onAlternarVisible) {
                        Text(
                            text = if (visible) "Ocultar" else "Mostrar",
                            style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 13.sp),
                            color = paleta.enlaceVerde,
                        )
                    }
                }
            } else {
                null
            },
            shape = RoundedCornerShape(16.dp),
            textStyle = TextStyle(
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.SemiBold,
                fontSize = 17.sp,
                color = paleta.textoPrincipal,
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = paleta.activo,
                unfocusedBorderColor = paleta.bordeControl,
                focusedContainerColor = paleta.tarjeta,
                unfocusedContainerColor = paleta.tarjeta,
                cursorColor = paleta.activo,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** Botón en píldora: [principal] va en verde; si no, con borde. */
@Composable
fun BotonAcceso(paleta: TokePaleta, texto: String, principal: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(9999.dp),
        color = if (principal) paleta.verde else paleta.tarjeta,
        border = if (principal) null else BorderStroke(1.dp, paleta.bordeControl),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = texto,
                style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 17.sp),
                color = if (principal) paleta.verdeTexto else paleta.textoPrincipal,
                maxLines = 1,
            )
        }
    }
}

@Composable
fun EnlaceAcceso(paleta: TokePaleta, texto: String, onClick: () -> Unit) {
    TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Text(
            text = texto,
            style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 14.sp),
            color = paleta.enlaceVerde,
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
