package com.flabs.cobrate.features.soporte

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flabs.cobrate.R
import com.flabs.cobrate.components.TokeIcons
import com.flabs.cobrate.features.acceso.BotonAtras
import com.flabs.cobrate.features.acceso.LogoToke
import com.flabs.cobrate.ui.theme.PlusJakartaSans
import com.flabs.cobrate.ui.theme.TokeMedidas
import com.flabs.cobrate.ui.theme.TokePaleta
import com.flabs.cobrate.ui.theme.tokePaleta

private fun estilo(peso: FontWeight, tam: Int) = TextStyle(fontFamily = PlusJakartaSans, fontWeight = peso, fontSize = tam.sp)

/** "Acerca de y soporte": versión de la app, ayuda por WhatsApp y documentos legales. Igual para dueño y trabajador. */
@Composable
fun AcercaDeScreen(onVolver: () -> Unit, onManual: () -> Unit = {}, onPreguntas: () -> Unit = {}) {
    val paleta = tokePaleta()
    val contexto = LocalContext.current
    val version = remember {
        val info = runCatching { contexto.packageManager.getPackageInfo(contexto.packageName, 0) }.getOrNull()
        AcercaDeLogica.textoVersion(info?.versionName, info?.longVersionCode)
    }
    val telefono = stringResource(R.string.plan_telefono_activacion)
    val urlTerminos = stringResource(R.string.url_terminos)
    val urlPrivacidad = stringResource(R.string.url_privacidad)

    fun abrir(url: String) {
        runCatching {
            contexto.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(paleta.fondo)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = TokeMedidas.MargenLateral, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(TokeMedidas.EntreGrupos),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BotonAtras(paleta, onVolver)
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = stringResource(R.string.nombre_acerca_soporte),
                style = estilo(FontWeight.ExtraBold, 22),
                color = paleta.textoPrincipal,
            )
        }

        // Identidad de la app, centrada, como en la pantalla "Acerca de" de Android.
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            LogoToke(paleta, tamano = 84, descripcion = stringResource(R.string.app_name), animar = false)
            Spacer(modifier = Modifier.height(6.dp))
            Text(stringResource(R.string.app_name), style = estilo(FontWeight.ExtraBold, 24), color = paleta.textoPrincipal)
            Text(version, style = estilo(FontWeight.Medium, 14), color = paleta.textoSecundario, textAlign = TextAlign.Center)
        }

        Seccion(paleta, "Ayuda") {
            Fila(paleta, TokeIcons.Recibo, "Manual", "Guías paso a paso con ejemplos", onManual)
            HorizontalDivider(modifier = Modifier.padding(start = 68.dp), color = paleta.borde)
            Fila(paleta, TokeIcons.Ayuda, "Preguntas frecuentes", "Respuestas cortas y claras", onPreguntas)
        }
        Seccion(paleta, "Soporte") {
            Fila(paleta, TokeIcons.Soporte, "Escribir por WhatsApp", "Habla directamente con nosotros") {
                abrir(AcercaDeLogica.enlaceSoporte(telefono, version))
            }
            HorizontalDivider(modifier = Modifier.padding(start = 68.dp), color = paleta.borde)
            Fila(paleta, TokeIcons.Soporte, "Escribir por correo", "AeroCorpKT@protonmail.com") {
                abrir("mailto:AeroCorpKT@protonmail.com?subject=" + Uri.encode("Soporte TOKE " + version))
            }
        }
        Seccion(paleta, "Información legal") {
            Fila(paleta, TokeIcons.Recibo, "Términos y condiciones", null) { abrir(urlTerminos) }
            HorizontalDivider(modifier = Modifier.padding(start = 68.dp), color = paleta.borde)
            Fila(paleta, TokeIcons.EscudoCandado, "Política de privacidad", null) { abrir(urlPrivacidad) }
            HorizontalDivider(modifier = Modifier.padding(start = 68.dp), color = paleta.borde)
            Fila(paleta, TokeIcons.EscudoCandado, "Seguridad", null) { abrir(contexto.getString(R.string.url_seguridad)) }
            HorizontalDivider(modifier = Modifier.padding(start = 68.dp), color = paleta.borde)
            Fila(paleta, TokeIcons.Ayuda, "Ayuda en la web", null) { abrir(contexto.getString(R.string.url_ayuda)) }
            HorizontalDivider(modifier = Modifier.padding(start = 68.dp), color = paleta.borde)
            Fila(paleta, TokeIcons.Recibo, "Eliminación de cuenta", null) { abrir(contexto.getString(R.string.url_eliminar_cuenta)) }
        }

        Text(
            text = "© 2026 TOKE!",
            style = estilo(FontWeight.Medium, 12),
            color = paleta.textoSecundario,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        )
    }
}

@Composable
private fun Seccion(paleta: TokePaleta, titulo: String, contenido: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = titulo.uppercase(),
            style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 0.8.sp),
            color = paleta.textoSecundario,
            modifier = Modifier.padding(start = 4.dp),
        )
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = paleta.tarjeta,
            border = BorderStroke(1.dp, paleta.borde),
            modifier = Modifier.fillMaxWidth(),
        ) { Column { contenido() } }
    }
}

@Composable
private fun Fila(paleta: TokePaleta, icono: Int, titulo: String, detalle: String?, onClick: () -> Unit) {
    Surface(onClick = onClick, color = androidx.compose.ui.graphics.Color.Transparent, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(shape = RoundedCornerShape(12.dp), color = paleta.tileFondo, modifier = Modifier.size(40.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(painter = painterResource(id = icono), contentDescription = null, tint = paleta.textoPrincipal, modifier = Modifier.size(22.dp))
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(titulo, style = estilo(FontWeight.Bold, 16), color = paleta.textoPrincipal)
                if (detalle != null) Text(detalle, style = estilo(FontWeight.Medium, 13), color = paleta.textoSecundario)
            }
            Icon(
                painter = painterResource(id = TokeIcons.ChevronDerecha),
                contentDescription = null,
                tint = paleta.textoSecundario,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}
