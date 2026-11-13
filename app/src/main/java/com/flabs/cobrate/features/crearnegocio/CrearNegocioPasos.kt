package com.flabs.cobrate.features.crearnegocio

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.flabs.cobrate.components.IconoMetodoPago
import com.flabs.cobrate.core.model.MetodoDeCobro
import com.flabs.cobrate.core.model.TipoMetodoCobro
import com.flabs.cobrate.features.cobros.MetodosCobroLogica
import com.flabs.cobrate.features.cobros.NuevoMetodoHoja
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flabs.cobrate.components.TokeIcons
import com.flabs.cobrate.core.model.TipoNegocio
import com.flabs.cobrate.features.acceso.AccesoFondo
import com.flabs.cobrate.features.negocios.iconoDeNegocio
import com.flabs.cobrate.features.acceso.AvisoAcceso
import com.flabs.cobrate.features.acceso.BotonAcceso
import com.flabs.cobrate.features.acceso.BotonAtras
import com.flabs.cobrate.features.acceso.CampoAcceso
import com.flabs.cobrate.features.acceso.EspacioAcceso
import com.flabs.cobrate.features.acceso.TituloAcceso
import com.flabs.cobrate.ui.theme.PlusJakartaSans
import com.flabs.cobrate.ui.theme.TokePaleta

/** Pantallas del asistente "Crear negocio". Sin estado: quien las llama guarda los datos. */

fun colorDe(hex: String?): Color =
    Color(CrearNegocioLogica.argbDeHex(hex) ?: CrearNegocioLogica.argbDeHex(CrearNegocioLogica.COLOR_POR_DEFECTO)!!)

fun iconoDeTipo(tipo: TipoNegocio): Int = iconoDeNegocio(tipo)

@Composable
private fun CabeceraPaso(paleta: TokePaleta, paso: Int, onAtras: (() -> Unit)?) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        if (onAtras != null) BotonAtras(paleta, onAtras) else Spacer(modifier = Modifier.size(42.dp))
        Spacer(modifier = Modifier.weight(1f))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Paso $paso de 4",
                style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 13.sp),
                color = paleta.textoSecundario,
            )
            Spacer(modifier = Modifier.size(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                (1..4).forEach { n ->
                    Box(
                        modifier = Modifier
                            .size(width = 26.dp, height = 6.dp)
                            .clip(CircleShape)
                            .background(if (n <= paso) paleta.verde else paleta.bordeControl),
                    )
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.size(42.dp))
    }
}

/** Paso 1: nombre del negocio y color de marca, con una vista previa. */
@Composable
fun PasoNombreYColor(
    paleta: TokePaleta,
    nombre: String,
    onNombre: (String) -> Unit,
    colorHex: String?,
    onColor: (String?) -> Unit,
    tipo: TipoNegocio?,
    onTipo: (TipoNegocio) -> Unit,
    onContinuar: () -> Unit,
    onAtras: () -> Unit,
) {
    var hojaIcono by remember { mutableStateOf(false) }
    AccesoFondo(paleta = paleta) {
        CabeceraPaso(paleta, 1, onAtras)
        TituloAcceso(paleta, "¿Cómo se llama tu negocio?", "Elige el nombre y el color que verá tu equipo.")
        CampoAcceso(paleta, "Nombre comercial", nombre, "Ej: Mi Bodeguita", onNombre)

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = paleta.tarjeta,
            border = BorderStroke(1.dp, paleta.borde),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "Color de tu marca",
                    style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 14.sp),
                    color = paleta.textoPrincipal,
                )
                SelectorColorMarca(paleta, colorHex, onColor)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        onClick = { hojaIcono = true },
                        shape = RoundedCornerShape(12.dp),
                        color = colorDe(colorHex),
                        modifier = Modifier.size(44.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(id = iconoDeTipo(CrearNegocioLogica.normalizar(tipo))),
                                contentDescription = "Cambiar el icono del negocio",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Así se verá tu negocio",
                            style = TextStyle(fontFamily = PlusJakartaSans, fontSize = 12.sp),
                            color = paleta.textoSecundario,
                        )
                        Text(
                            text = nombre.ifBlank { "Mi Bodeguita" },
                            style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 16.sp),
                            color = paleta.textoPrincipal,
                        )
                    }
                    Surface(
                        onClick = { hojaIcono = true },
                        shape = RoundedCornerShape(9999.dp),
                        color = paleta.tileFondo,
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = TokeIcons.Editar),
                                contentDescription = null,
                                tint = paleta.textoPrincipal,
                                modifier = Modifier.size(14.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Icono",
                                style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 12.5.sp),
                                color = paleta.textoPrincipal,
                            )
                        }
                    }
                }
            }
        }

        EspacioAcceso(4)
        BotonAcceso(paleta, "Continuar", principal = CrearNegocioLogica.nombreValido(nombre)) {
            if (CrearNegocioLogica.nombreValido(nombre)) onContinuar()
        }
    }
    if (hojaIcono) HojaIconoNegocio(paleta, tipo, onTipo) { hojaIcono = false }
}

/** Paso 2: rubro del negocio, en iconos con su nombre. Al continuar se crea el negocio. */
@Composable
fun PasoTipo(
    paleta: TokePaleta,
    elegido: TipoNegocio?,
    onElegir: (TipoNegocio) -> Unit,
    ocupado: Boolean,
    onCrear: () -> Unit,
    onAtras: () -> Unit,
) {
    AccesoFondo(
        paleta = paleta,
        pie = {
            BotonAcceso(paleta, "Crear mi negocio", principal = elegido != null && !ocupado) {
                if (elegido != null && !ocupado) onCrear()
            }
        },
    ) {
        CabeceraPaso(paleta, 2, onAtras)
        TituloAcceso(paleta, "¿A qué se dedica tu negocio?", "Elige el rubro que más se parezca. Podrás cambiar el icono cuando quieras.")
        SelectorIconoNegocio(paleta, elegido, onElegir)
    }
}

/** Paso 4: el negocio ya está creado. Pantalla para el dueño: su icono y color, lo que quedó hecho y a dónde ir. */
@Composable
fun PasoListo(
    paleta: TokePaleta,
    nombreNegocio: String,
    tipo: TipoNegocio?,
    colorHex: String?,
    metodos: List<MetodoDeCobro>,
    onCargarQr: () -> Unit,
    onEntrar: () -> Unit,
) {
    AccesoFondo(
        paleta = paleta,
        horizontal = Alignment.CenterHorizontally,
        pie = { BotonAcceso(paleta, "Entrar a mi negocio", principal = true, onClick = onEntrar) },
    ) {
        CabeceraPaso(paleta, 4, null)
        Spacer(modifier = Modifier.size(8.dp))
        Surface(shape = RoundedCornerShape(30.dp), color = colorDe(colorHex), modifier = Modifier.size(96.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(id = iconoDeTipo(CrearNegocioLogica.normalizar(tipo))),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(46.dp),
                )
            }
        }
        Text(
            text = "¡Tu negocio está listo!",
            style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp, textAlign = TextAlign.Center),
            color = paleta.textoPrincipal,
        )
        Text(
            text = "$nombreNegocio ya puede recibir cobros y avisar a tu equipo al instante.",
            style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Medium, fontSize = 15.sp, lineHeight = 21.sp, textAlign = TextAlign.Center),
            color = paleta.textoSecundario,
        )
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = paleta.tarjeta,
            border = BorderStroke(1.dp, paleta.borde),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                FilaListo(paleta, "Negocio creado", CrearNegocioLogica.tituloDeTipo(tipo), hecho = true)
                FilaListo(
                    paleta,
                    "Método de cobro",
                    if (metodos.isEmpty()) "Aún no vinculaste ninguno" else metodos.joinToString { MetodosCobroLogica.nombreLegible(it.tipo) },
                    hecho = metodos.isNotEmpty(),
                )
            }
        }
        BotonAcceso(
            paleta,
            if (metodos.isEmpty()) "Vincular un método de cobro" else "Cargar mi QR para recibir cobros",
            principal = false,
            onClick = onCargarQr,
        )
    }
}

@Composable
private fun FilaListo(paleta: TokePaleta, titulo: String, detalle: String, hecho: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(id = if (hecho) TokeIcons.CheckVerde else TokeIcons.Advertencia),
            contentDescription = if (hecho) "Hecho" else "Pendiente",
            tint = if (hecho) Color.Unspecified else paleta.ambar,
            modifier = Modifier.size(24.dp),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = titulo,
                style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp),
                color = paleta.textoPrincipal,
            )
            Text(
                text = detalle,
                style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Medium, fontSize = 13.sp),
                color = paleta.textoSecundario,
            )
        }
    }
}

/** Paso 3: medios de cobro. El negocio ya existe; se pueden vincular ahora o dejarlo para después. */
@Composable
fun PasoMediosCobro(
    paleta: TokePaleta,
    existentes: List<MetodoDeCobro>,
    onVincular: (TipoMetodoCobro, String, String) -> Unit,
    onContinuar: () -> Unit,
) {
    var mostrarHoja by remember { mutableStateOf(false) }
    AccesoFondo(paleta = paleta) {
        CabeceraPaso(paleta, 3, null)
        TituloAcceso(
            paleta,
            "¿Cómo te pagan tus clientes?",
            "Vincula Yape, Plin o tu banco para que TOKE te avise de cada cobro. También puedes hacerlo después.",
        )
        existentes.forEach { metodo ->
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = paleta.tarjeta,
                border = BorderStroke(1.dp, paleta.borde),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconoMetodoPago(tipo = metodo.tipo, tamano = 44.dp)
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = MetodosCobroLogica.nombreLegible(metodo.tipo),
                            style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp),
                            color = paleta.textoPrincipal,
                        )
                        Text(
                            text = metodo.datoDeCobro,
                            style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Medium, fontSize = 13.sp),
                            color = paleta.textoSecundario,
                            maxLines = 1,
                        )
                    }
                    Icon(
                        painter = painterResource(id = TokeIcons.CheckVerde),
                        contentDescription = "Vinculado",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        }
        BotonAcceso(paleta, if (existentes.isEmpty()) "Vincular un método" else "Vincular otro método", principal = false) {
            mostrarHoja = true
        }
        Spacer(modifier = Modifier.weight(1f))
        BotonAcceso(paleta, if (existentes.isEmpty()) "Lo haré después" else "Continuar", principal = existentes.isNotEmpty(), onClick = onContinuar)
    }
    if (mostrarHoja) {
        NuevoMetodoHoja(
            paleta = paleta,
            existentes = existentes,
            onDismiss = { mostrarHoja = false },
            onConfirm = { tipo, dato, titular ->
                onVincular(tipo, dato, titular)
                mostrarHoja = false
            },
        )
    }
}

/** Fila de colores de marca. Tocar el color elegido lo quita; [onColor] recibe null. */
@Composable
fun SelectorColorMarca(paleta: TokePaleta, colorHex: String?, onColor: (String?) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        CrearNegocioLogica.colores.forEach { c ->
            val elegido = colorHex == c.hex
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(colorDe(c.hex))
                    .then(if (elegido) Modifier.border(3.dp, paleta.textoPrincipal, CircleShape) else Modifier)
                    .clickable { onColor(if (elegido) null else c.hex) },
                contentAlignment = Alignment.Center,
            ) {
                if (elegido) {
                    Icon(
                        painter = painterResource(id = TokeIcons.Check),
                        contentDescription = c.nombre,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
    }
}
