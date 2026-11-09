package com.flabs.cobrate.features.acceso

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flabs.cobrate.R
import com.flabs.cobrate.ui.theme.PlusJakartaSans
import com.flabs.cobrate.ui.theme.TokePaleta

/** Línea corta al pie del inicio de sesión y del registro. "términos y condiciones" va en azul y abre la página. */
@Composable
fun AvisoTerminos(paleta: TokePaleta, modifier: Modifier = Modifier) {
    val url = stringResource(R.string.url_terminos)
    val texto = buildAnnotatedString {
        append("Al continuar aceptas los ")
        withLink(
            LinkAnnotation.Url(
                url,
                TextLinkStyles(SpanStyle(color = paleta.enlaceAzul, fontWeight = FontWeight.SemiBold, textDecoration = TextDecoration.Underline)),
            ),
        ) { append("términos y condiciones") }
    }
    Text(
        text = texto,
        style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 17.sp),
        color = paleta.textoSecundario,
        textAlign = TextAlign.Center,
        modifier = modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp),
    )
}
