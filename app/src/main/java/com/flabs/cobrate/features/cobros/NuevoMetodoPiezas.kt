package com.flabs.cobrate.features.cobros
import com.flabs.cobrate.ui.theme.TokeRojo
import com.flabs.cobrate.ui.theme.TokeAmbarTexto

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flabs.cobrate.components.IconoMetodoPago
import com.flabs.cobrate.components.TokeIcons
import com.flabs.cobrate.core.model.TipoMetodoCobro
import com.flabs.cobrate.ui.theme.PlusJakartaSans
import com.flabs.cobrate.ui.theme.TokePaleta

/**
 * Piezas visuales de la ventana "Vincular un método de cobro": una fila por billetera o banco,
 * el aviso de Plin y los campos de datos. Todo con colores sólidos.
 */

/**
 * Fila de una billetera o banco. Si [yaExiste], queda atenuada, no se puede tocar y muestra
 * "(Ya existe)" a su lado.
 */
@Composable
fun OpcionMetodoFila(
    paleta: TokePaleta,
    tipo: TipoMetodoCobro,
    subtitulo: String?,
    habilitada: Boolean,
    yaExiste: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        enabled = habilitada,
        shape = RoundedCornerShape(18.dp),
        color = paleta.tarjeta,
        border = BorderStroke(1.dp, paleta.borde),
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (habilitada) 1f else 0.55f),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconoMetodoPago(tipo = tipo, tamano = 44.dp)
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = MetodosCobroLogica.nombreLegible(tipo),
                        style = TextStyle(
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                        ),
                        color = paleta.textoPrincipal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (yaExiste) {
                        Text(
                            text = " (Ya existe)",
                            style = TextStyle(
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                            ),
                            color = paleta.textoSecundario,
                            maxLines = 1,
                        )
                    }
                }
                if (subtitulo != null && !yaExiste) {
                    Text(
                        text = subtitulo,
                        style = TextStyle(
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                        ),
                        color = paleta.textoSecundario,
                    )
                }
            }
            if (habilitada) {
                Icon(
                    painter = painterResource(id = TokeIcons.ChevronDerecha),
                    contentDescription = null,
                    tint = paleta.textoSecundario,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

/** Título de un grupo de opciones ("BANCOS", "BILLETERAS", ...). */
@Composable
fun TituloGrupo(paleta: TokePaleta, texto: String) {
    Text(
        text = texto.uppercase(),
        style = TextStyle(
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            letterSpacing = 0.8.sp,
        ),
        color = paleta.textoSecundario,
        modifier = Modifier.padding(start = 4.dp, top = 6.dp),
    )
}

/** Aviso previo de Plin: con qué billeteras y bancos trabaja. */
@Composable
fun AvisoPlin(paleta: TokePaleta) {
    val ambar = TokeAmbarTexto
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = paleta.ambar.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, paleta.ambar.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
            Icon(
                painter = painterResource(id = TokeIcons.Advertencia),
                contentDescription = null,
                tint = ambar,
                modifier = Modifier.size(24.dp),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Plin trabaja con estas billeteras y bancos",
                    style = TextStyle(
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                    ),
                    color = paleta.textoPrincipal,
                )
                Text(
                    text = "Elige en cuál recibes tus pagos Plin. Cada billetera o banco se puede vincular una sola vez.",
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
}

/** Campo de texto con etiqueta clara arriba y una ayuda corta debajo. */
@Composable
fun CampoNuevoMetodo(
    paleta: TokePaleta,
    etiqueta: String,
    ayuda: String,
    valor: String,
    placeholder: String,
    teclado: KeyboardType,
    onCambia: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = etiqueta,
            style = TextStyle(
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
            ),
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
            keyboardOptions = KeyboardOptions(keyboardType = teclado),
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
        Text(
            text = ayuda,
            style = TextStyle(
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
            ),
            color = paleta.textoSecundario,
        )
    }
}
