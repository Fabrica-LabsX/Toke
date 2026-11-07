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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flabs.cobrate.components.TokeIcons
import com.flabs.cobrate.core.model.TipoNegocio
import com.flabs.cobrate.features.acceso.AccesoFondo
import com.flabs.cobrate.features.acceso.AvisoAcceso
import com.flabs.cobrate.features.acceso.BotonAcceso
import com.flabs.cobrate.features.acceso.BotonAtras
import com.flabs.cobrate.features.acceso.CampoAcceso
import com.flabs.cobrate.features.acceso.EspacioAcceso
import com.flabs.cobrate.features.acceso.TituloAcceso
import com.flabs.cobrate.ui.theme.PlusJakartaSans
import com.flabs.cobrate.ui.theme.TokePaleta

/** Pantallas del asistente "Crear negocio". Sin estado: quien las llama guarda los datos. */

private fun colorDe(hex: String?): Color =
    Color(CrearNegocioLogica.argbDeHex(hex) ?: CrearNegocioLogica.argbDeHex(CrearNegocioLogica.COLOR_POR_DEFECTO)!!)

private fun iconoDeTipo(tipo: TipoNegocio): Int = when (tipo) {
    TipoNegocio.ABARROTES -> TokeIcons.Canasta
    TipoNegocio.RESTAURANTE -> TokeIcons.Recibo
    TipoNegocio.MARISQUERIA -> TokeIcons.Efectivo
    TipoNegocio.ROPA -> TokeIcons.Carrito
    TipoNegocio.ELECTRODOMESTICOS -> TokeIcons.Herramienta
    TipoNegocio.SERVICIOS -> TokeIcons.Maletin
    TipoNegocio.OTRO -> TokeIcons.Mas
}

@Composable
private fun CabeceraPaso(paleta: TokePaleta, paso: Int, onAtras: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        BotonAtras(paleta, onAtras)
        Spacer(modifier = Modifier.weight(1f))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Paso $paso de 3",
                style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 13.sp),
                color = paleta.textoSecundario,
            )
            Spacer(modifier = Modifier.size(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                (1..3).forEach { n ->
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
    onContinuar: () -> Unit,
    onAtras: () -> Unit,
) {
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(12.dp), color = colorDe(colorHex), modifier = Modifier.size(44.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(id = TokeIcons.Tienda),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
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
                }
            }
        }

        EspacioAcceso(4)
        BotonAcceso(paleta, "Continuar", principal = CrearNegocioLogica.nombreValido(nombre)) {
            if (CrearNegocioLogica.nombreValido(nombre)) onContinuar()
        }
    }
}

/** Paso 2: rubro del negocio. Al continuar se crea el negocio. */
@Composable
fun PasoTipo(
    paleta: TokePaleta,
    elegido: TipoNegocio?,
    onElegir: (TipoNegocio) -> Unit,
    ocupado: Boolean,
    onCrear: () -> Unit,
    onAtras: () -> Unit,
) {
    AccesoFondo(paleta = paleta) {
        CabeceraPaso(paleta, 2, onAtras)
        TituloAcceso(paleta, "¿Qué tipo de negocio tienes?", "Elige tu rubro para organizar tus cobros y ventas.")
        CrearNegocioLogica.tipos.forEach { op ->
            val activo = elegido == op.tipo
            Surface(
                onClick = { onElegir(op.tipo) },
                shape = RoundedCornerShape(18.dp),
                color = paleta.tarjeta,
                border = BorderStroke(if (activo) 2.dp else 1.dp, if (activo) paleta.verde else paleta.borde),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(12.dp), color = paleta.tileFondo, modifier = Modifier.size(44.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(id = iconoDeTipo(op.tipo)),
                                contentDescription = null,
                                tint = paleta.textoPrincipal,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = op.titulo,
                            style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp),
                            color = paleta.textoPrincipal,
                        )
                        Text(
                            text = op.descripcion,
                            style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Medium, fontSize = 13.sp),
                            color = paleta.textoSecundario,
                        )
                    }
                    if (activo) {
                        Icon(
                            painter = painterResource(id = TokeIcons.CheckVerde),
                            contentDescription = "Elegido",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
            }
        }
        EspacioAcceso(4)
        BotonAcceso(paleta, "Crear mi negocio", principal = elegido != null && !ocupado) {
            if (elegido != null && !ocupado) onCrear()
        }
    }
}

/** Resumen del negocio ya creado: tarjeta con su nombre. */
@Composable
private fun ColumnScope.TarjetaNegocioCreado(paleta: TokePaleta, nombre: String, detalle: String?) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = paleta.tarjeta,
        border = BorderStroke(1.dp, paleta.borde),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(modifier = Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(14.dp), color = paleta.tileFondo, modifier = Modifier.size(52.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(id = TokeIcons.Tienda),
                        contentDescription = null,
                        tint = paleta.textoPrincipal,
                        modifier = Modifier.size(26.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = nombre,
                    style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp),
                    color = paleta.textoPrincipal,
                )
                if (detalle != null) {
                    Text(
                        text = detalle,
                        style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Medium, fontSize = 13.sp),
                        color = paleta.textoSecundario,
                    )
                }
            }
        }
    }
}

/** Conexión exitosa: el negocio ya existe; se elige a dónde ir primero. */
@Composable
fun PasoConexion(
    paleta: TokePaleta,
    nombreNegocio: String,
    onRecibirCobros: () -> Unit,
    onMiNegocio: () -> Unit,
    onEntrar: () -> Unit,
) {
    AccesoFondo(paleta = paleta) {
        EspacioAcceso(24)
        TituloAcceso(paleta, "¡TOKE está listo!", "Tu cuenta ya está conectada a este negocio.")
        TarjetaNegocioCreado(paleta, nombreNegocio, "Ya eres parte de este negocio")
        BotonAcceso(paleta, "Cargar mi QR para recibir cobros", principal = false, onClick = onRecibirCobros)
        BotonAcceso(paleta, "Ver mi negocio", principal = false, onClick = onMiNegocio)
        EspacioAcceso(4)
        BotonAcceso(paleta, "Entrar a TOKE", principal = true, onClick = onEntrar)
    }
}

/** Confirmación final: resumen del negocio y lo que ya queda activo. */
@Composable
fun PasoFinal(
    paleta: TokePaleta,
    nombreNegocio: String,
    tipo: TipoNegocio?,
    onEntrar: () -> Unit,
    onAtras: () -> Unit,
) {
    AccesoFondo(paleta = paleta) {
        CabeceraPaso(paleta, 3, onAtras)
        TituloAcceso(paleta, "¡Tu negocio está listo!", "Ya puedes recibir y organizar tus pagos al instante.")
        TarjetaNegocioCreado(paleta, nombreNegocio, CrearNegocioLogica.tituloDeTipo(tipo))
        AvisoAcceso(
            paleta = paleta,
            titulo = "Ya está activo",
            texto = "Alertas al instante de tus pagos por Yape y Plin, y avisos de voz automáticos.",
        )
        EspacioAcceso(4)
        BotonAcceso(paleta, "Entrar a mi negocio", principal = true, onClick = onEntrar)
    }
}
