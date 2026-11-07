package com.flabs.cobrate.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.flabs.cobrate.ui.theme.PlusJakartaSans
import com.flabs.cobrate.ui.theme.TokeMedidas
import com.flabs.cobrate.ui.theme.TokePaleta
import com.flabs.cobrate.ui.theme.rememberPulsacion
import com.flabs.cobrate.ui.theme.tokePaleta

/**
 * Ventana de confirmación única de la app: título, un texto de hasta 3 líneas y dos botones
 * (cancelar a la izquierda, confirmar a la derecha; en rojo si [destructivo]).
 * Entra con un fundido y una escala corta (120 ms), sin costo para celulares de pocos recursos.
 */
@Composable
fun DialogoToke(
    titulo: String,
    texto: String,
    textoConfirmar: String,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit,
    textoCancelar: String = "Cancelar",
    destructivo: Boolean = false,
    cargando: Boolean = false,
    confirmarHabilitado: Boolean = true,
    contenidoExtra: (@Composable ColumnScope.() -> Unit)? = null,
) {
    val paleta = tokePaleta()
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Dialog(
        onDismissRequest = { if (!cargando) onCancelar() },
        properties = DialogProperties(dismissOnClickOutside = !cargando),
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(120)) + scaleIn(initialScale = 0.96f, animationSpec = tween(120)),
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = paleta.tarjeta,
                border = BorderStroke(1.dp, paleta.borde),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = titulo,
                        style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp),
                        color = paleta.textoPrincipal,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = texto,
                        style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Medium, fontSize = 15.sp, lineHeight = 21.sp),
                        color = paleta.textoSecundario,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                    contenidoExtra?.invoke(this)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        BotonDialogo(paleta, textoCancelar, principal = false, destructivo = false, habilitado = !cargando, Modifier.weight(1f), onCancelar)
                        BotonDialogo(paleta, textoConfirmar, principal = true, destructivo = destructivo, habilitado = confirmarHabilitado && !cargando, Modifier.weight(1f), onConfirmar, cargando)
                    }
                }
            }
        }
    }
}

@Composable
private fun BotonDialogo(
    paleta: TokePaleta,
    texto: String,
    principal: Boolean,
    destructivo: Boolean,
    habilitado: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
    cargando: Boolean = false,
) {
    val pulso = rememberPulsacion()
    val fondo = when {
        !principal -> paleta.tarjeta
        !habilitado -> paleta.bordeControl
        destructivo -> paleta.rojo
        else -> paleta.verde
    }
    val colorTexto = when {
        !principal -> paleta.textoPrincipal
        !habilitado -> paleta.textoSecundario
        destructivo -> androidx.compose.ui.graphics.Color.White
        else -> paleta.verdeTexto
    }
    Surface(
        onClick = onClick,
        enabled = habilitado,
        interactionSource = pulso.fuente,
        shape = RoundedCornerShape(9999.dp),
        color = fondo,
        border = if (principal) null else BorderStroke(1.dp, paleta.bordeControl),
        modifier = modifier.height(TokeMedidas.BotonAltura).then(pulso.modificador),
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (cargando) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = colorTexto)
            } else {
                Text(
                    text = texto,
                    style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 15.sp),
                    color = colorTexto,
                    maxLines = 1,
                )
            }
        }
    }
}
