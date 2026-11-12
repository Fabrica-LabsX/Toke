package com.flabs.cobrate.features.soporte

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flabs.cobrate.components.TokeIcons
import com.flabs.cobrate.features.acceso.BotonAtras
import com.flabs.cobrate.ui.theme.PlusJakartaSans
import com.flabs.cobrate.ui.theme.TokeMedidas
import com.flabs.cobrate.ui.theme.TokePaleta
import com.flabs.cobrate.ui.theme.rememberPulsacion
import com.flabs.cobrate.ui.theme.tokePaleta

private fun estilo(peso: FontWeight, tam: Int) = TextStyle(fontFamily = PlusJakartaSans, fontWeight = peso, fontSize = tam.sp)

/** Manual: la lista de guías y, al tocar una, sus pasos con una pantalla de ejemplo que se anima. */
@Composable
fun ManualScreen(onVolver: () -> Unit) {
    val paleta = tokePaleta()
    var abierto by remember { mutableStateOf<ArticuloManual?>(null) }
    BackHandler(enabled = abierto != null) { abierto = null }

    Pantalla(paleta, titulo = abierto?.titulo ?: "Manual", onVolver = { if (abierto != null) abierto = null else onVolver() }) {
        val articulo = abierto
        if (articulo == null) {
            Text("Elige una guía. Cada paso muestra dónde tocar.", style = estilo(FontWeight.Medium, 14), color = paleta.textoSecundario)
            ManualContenido.articulos.forEach { TarjetaArticulo(paleta, it) { abierto = it } }
        } else {
            articulo.pasos.forEachIndexed { i, paso -> PasoConEjemplo(paleta, i + 1, paso) }
        }
    }
}

/** Preguntas frecuentes: cada una se abre y se cierra al tocarla. */
@Composable
fun PreguntasFrecuentesScreen(onVolver: () -> Unit) {
    val paleta = tokePaleta()
    Pantalla(paleta, titulo = "Preguntas frecuentes", onVolver = onVolver) {
        ManualContenido.preguntas.forEach { Pregunta(paleta, it) }
    }
}

@Composable
private fun Pantalla(paleta: TokePaleta, titulo: String, onVolver: () -> Unit, contenido: @Composable () -> Unit) {
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
            Text(titulo, style = estilo(FontWeight.ExtraBold, 20), color = paleta.textoPrincipal, modifier = Modifier.weight(1f), maxLines = 2)
        }
        contenido()
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun TarjetaArticulo(paleta: TokePaleta, articulo: ArticuloManual, onClick: () -> Unit) {
    val pulso = rememberPulsacion()
    Surface(
        onClick = onClick,
        interactionSource = pulso.fuente,
        shape = RoundedCornerShape(20.dp),
        color = paleta.tarjeta,
        border = BorderStroke(1.dp, paleta.borde),
        modifier = Modifier.fillMaxWidth().then(pulso.modificador),
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(12.dp), color = paleta.tileFondo, modifier = Modifier.size(44.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(painter = painterResource(id = articulo.icono), contentDescription = null, tint = paleta.textoPrincipal, modifier = Modifier.size(22.dp))
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(articulo.titulo, style = estilo(FontWeight.Bold, 15), color = paleta.textoPrincipal)
                Text(articulo.resumen, style = estilo(FontWeight.Medium, 13), color = paleta.textoSecundario)
            }
            Icon(painter = painterResource(id = TokeIcons.ChevronDerecha), contentDescription = null, tint = paleta.textoSecundario, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun Pregunta(paleta: TokePaleta, p: PreguntaFrecuente) {
    var abierta by remember { mutableStateOf(false) }
    Surface(
        onClick = { abierta = !abierta },
        shape = RoundedCornerShape(18.dp),
        color = paleta.tarjeta,
        border = BorderStroke(1.dp, paleta.borde),
        modifier = Modifier.fillMaxWidth().animateContentSize(),
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(p.pregunta, style = estilo(FontWeight.Bold, 15), color = paleta.textoPrincipal, modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(10.dp))
                Icon(
                    painter = painterResource(id = TokeIcons.ChevronDown),
                    contentDescription = null,
                    tint = paleta.textoSecundario,
                    modifier = Modifier.size(16.dp).graphicsLayer { rotationZ = if (abierta) 180f else 0f },
                )
            }
            if (abierta) Text(p.respuesta, style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp), color = paleta.textoSecundario)
        }
    }
}

@Composable
private fun PasoConEjemplo(paleta: TokePaleta, numero: Int, paso: PasoManual) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.Top) {
            Surface(shape = CircleShape, color = paleta.verde, modifier = Modifier.size(28.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Text("$numero", style = estilo(FontWeight.ExtraBold, 14), color = paleta.verdeTexto)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                paso.texto,
                style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 21.sp),
                color = paleta.textoPrincipal,
                modifier = Modifier.weight(1f).padding(top = 3.dp),
            )
        }
        PantallaDeEjemplo(paleta, paso.mock)
    }
}

/** Pantalla de ejemplo con los mismos estilos de la app. El elemento a tocar late y un dedo "pulsa" sobre él. */
@Composable
private fun PantallaDeEjemplo(paleta: TokePaleta, mock: MockPantalla) {
    val bucle = rememberInfiniteTransition(label = "toque")
    val latido by bucle.animateFloat(0f, 1f, infiniteRepeatable(tween(1100, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "latido")
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = paleta.fondo,
        border = BorderStroke(1.5.dp, paleta.bordeControl),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Text(mock.titulo, style = estilo(FontWeight.ExtraBold, 14), color = paleta.textoPrincipal)
            mock.elementos.forEachIndexed { i, el ->
                val activo = i == mock.resaltado
                Box(
                    modifier = Modifier.graphicsLayer {
                        if (activo) { val e = 1f - 0.04f * latido; scaleX = e; scaleY = e }
                    },
                ) {
                    Elemento(paleta, el, activo, latido)
                    if (activo) Dedo(paleta, latido, Modifier.align(Alignment.CenterEnd).offset(x = (-14).dp, y = 8.dp))
                }
            }
        }
    }
}

@Composable
private fun Dedo(paleta: TokePaleta, latido: Float, modifier: Modifier) {
    Box(
        modifier = modifier.size(30.dp).graphicsLayer { scaleX = 1.15f - 0.35f * latido; scaleY = 1.15f - 0.35f * latido },
        contentAlignment = Alignment.Center,
    ) {
        Box(modifier = Modifier.size(30.dp).clip(CircleShape).background(paleta.borde))
        Box(modifier = Modifier.size(14.dp).clip(CircleShape).background(paleta.textoSecundario))
    }
}

@Composable
private fun Elemento(paleta: TokePaleta, el: ElementoMock, activo: Boolean, latido: Float) {
    val borde = if (activo) BorderStroke(2.dp, paleta.verde) else BorderStroke(1.dp, paleta.borde)
    when (el) {
        is ElementoMock.Fila -> Surface(shape = RoundedCornerShape(14.dp), color = paleta.tarjeta, border = borde, modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(10.dp), color = paleta.tileFondo, modifier = Modifier.size(30.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(painter = painterResource(id = el.icono), contentDescription = null, tint = paleta.textoPrincipal, modifier = Modifier.size(17.dp))
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(el.texto, style = estilo(FontWeight.Bold, 14), color = paleta.textoPrincipal, modifier = Modifier.weight(1f))
                Icon(painter = painterResource(id = TokeIcons.ChevronDerecha), contentDescription = null, tint = paleta.textoSecundario, modifier = Modifier.size(13.dp))
            }
        }
        is ElementoMock.Boton -> Surface(
            shape = RoundedCornerShape(9999.dp),
            color = if (el.principal) paleta.verde else paleta.tarjeta,
            border = if (activo) borde else if (el.principal) null else BorderStroke(1.dp, paleta.bordeControl),
            modifier = Modifier.fillMaxWidth().height(40.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(el.texto, style = estilo(FontWeight.Bold, 14), color = if (el.principal) paleta.verdeTexto else paleta.textoPrincipal, maxLines = 1)
            }
        }
        is ElementoMock.Campo -> Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(el.etiqueta, style = estilo(FontWeight.SemiBold, 11), color = paleta.textoSecundario)
            Surface(shape = RoundedCornerShape(10.dp), color = paleta.tarjeta, border = borde, modifier = Modifier.fillMaxWidth().height(38.dp)) {
                Box(contentAlignment = Alignment.CenterStart, modifier = Modifier.padding(horizontal = 12.dp)) {
                    Text(el.valor, style = estilo(FontWeight.SemiBold, 14), color = paleta.textoPrincipal, maxLines = 1)
                }
            }
        }
        is ElementoMock.Interruptor -> Surface(shape = RoundedCornerShape(14.dp), color = paleta.tarjeta, border = borde, modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(el.texto, style = estilo(FontWeight.Bold, 14), color = paleta.textoPrincipal, modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier.width(40.dp).height(22.dp).clip(CircleShape).background(if (el.activo) paleta.verde else paleta.bordeControl),
                    contentAlignment = if (el.activo) Alignment.CenterEnd else Alignment.CenterStart,
                ) {
                    Box(modifier = Modifier.padding(3.dp).size(16.dp).clip(CircleShape).background(if (el.activo) paleta.verdeTexto else paleta.tarjeta))
                }
            }
        }
    }
}
