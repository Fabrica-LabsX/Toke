package com.flabs.cobrate.features.shared

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flabs.cobrate.components.TokeIcons
import com.flabs.cobrate.components.colorBilletera
import com.flabs.cobrate.core.data.PreferenciasAppStore
import com.flabs.cobrate.core.model.Operacion
import com.flabs.cobrate.features.worker.PanelTrabajadorLogica
import com.flabs.cobrate.ui.theme.PlusJakartaSans
import com.flabs.cobrate.ui.theme.TokePaleta
import com.flabs.cobrate.ui.theme.tokePaleta
import java.util.Locale

private fun estilo(peso: FontWeight, tam: Int, espaciado: Float = 0f) =
    TextStyle(fontFamily = PlusJakartaSans, fontWeight = peso, fontSize = tam.sp, letterSpacing = espaciado.sp)

/** Botón de tres puntos de la cabecera del trabajador: abre el menú Más. */
@Composable
internal fun BotonMasOpciones(onClick: () -> Unit) {
    val p = tokePaleta()
    Surface(
        modifier = Modifier.size(36.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = p.tarjeta,
        border = BorderStroke(1.dp, p.borde),
    ) {
        Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            repeat(3) { i ->
                if (i > 0) Spacer(Modifier.width(3.dp))
                Box(Modifier.size(4.dp).background(p.textoPrincipal, CircleShape))
            }
        }
    }
}

/** "Último cobro recibido": monto grande y tres píldoras (billetera, hace cuánto, quién). */
@Composable
internal fun UltimoCobroCard(ultimo: Operacion?, nombreTrabajador: String?) {
    val p = tokePaleta()
    val aspecto by PreferenciasAppStore.themeMode.collectAsState()
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = p.tarjeta,
        border = BorderStroke(1.dp, p.borde),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(17.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Último cobro recibido", style = estilo(FontWeight.Bold, 13), color = p.textoSecundario)
            Text(
                text = if (ultimo == null) "S/ 0.00" else String.format(Locale.US, "+ S/%,.2f", ultimo.monto),
                style = estilo(FontWeight.ExtraBold, 34, -0.85f),
                color = p.textoPrincipal,
            )
            if (ultimo == null) {
                Text("Aún no tienes cobros", style = estilo(FontWeight.Medium, 12), color = p.textoSecundario)
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    val metodo = ultimo.metodoPago?.name
                    if (metodo != null) {
                        Pildora(
                            texto = metodo.uppercase(),
                            fondo = colorBilletera(ultimo.metodoPago, aspecto) ?: p.tileFondo,
                            color = Color.White,
                            peso = FontWeight.ExtraBold,
                        )
                    }
                    Pildora(
                        texto = PanelTrabajadorLogica.textoHace(ultimo.fechaHora, System.currentTimeMillis()),
                        fondo = p.tileFondo,
                        color = p.textoPrincipal,
                    )
                    if (!nombreTrabajador.isNullOrBlank()) {
                        Pildora(texto = nombreTrabajador, fondo = p.verde, color = p.verdeTexto)
                    }
                }
            }
        }
    }
}

@Composable
private fun Pildora(texto: String, fondo: Color, color: Color, peso: FontWeight = FontWeight.Bold) {
    Surface(shape = RoundedCornerShape(9999.dp), color = fondo) {
        Text(
            text = texto,
            style = estilo(peso, 11),
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

/** Dos fichas iguales: Mis QRs y, a la derecha, pagos por reclamar o, si no hay, Mis Negocios. */
@Composable
internal fun FichasTrabajador(
    pendientes: List<Operacion>,
    onMisQRs: () -> Unit,
    onReclamar: () -> Unit,
    onMisNegocios: () -> Unit,
) {
    val p = tokePaleta()
    Row(
        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        FichaSimple(
            p = p,
            icono = TokeIcons.Qr,
            titulo = "Mis QRs",
            subtitulo = "Yape, Plin y bancos",
            onClick = onMisQRs,
            modifier = Modifier.weight(1f).fillMaxHeight(),
        )
        when (PanelTrabajadorLogica.fichaDerecha(pendientes.size)) {
            PanelTrabajadorLogica.FichaDerecha.POR_RECLAMAR -> FichaPorReclamar(
                p = p,
                pendientes = pendientes,
                onClick = onReclamar,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
            PanelTrabajadorLogica.FichaDerecha.MIS_NEGOCIOS -> FichaSimple(
                p = p,
                icono = TokeIcons.Maletin,
                titulo = "Mis Negocios",
                subtitulo = "Cambia o únete a uno",
                onClick = onMisNegocios,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
        }
    }
}

@Composable
private fun FichaSimple(
    p: TokePaleta,
    icono: Int,
    titulo: String,
    subtitulo: String,
    onClick: () -> Unit,
    modifier: Modifier,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = p.tarjeta,
        border = BorderStroke(1.dp, p.borde),
        modifier = modifier,
    ) {
        Column(modifier = Modifier.padding(15.dp)) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = p.tileFondo,
                border = BorderStroke(1.dp, p.borde),
                modifier = Modifier.size(42.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(painterResource(icono), contentDescription = null, tint = p.textoPrincipal, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(Modifier.height(14.dp))
            Text(titulo, style = estilo(FontWeight.Bold, 14), color = p.textoPrincipal, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(4.dp))
            Text(subtitulo, style = estilo(FontWeight.Medium, 11), color = p.textoSecundario, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun FichaPorReclamar(p: TokePaleta, pendientes: List<Operacion>, onClick: () -> Unit, modifier: Modifier) {
    val total = pendientes.sumOf { it.monto }
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = p.ambar,
        modifier = modifier,
    ) {
        Column(modifier = Modifier.padding(15.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = p.ambarTexto,
                    modifier = Modifier.size(36.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(painterResource(TokeIcons.Billetera), contentDescription = null, tint = p.ambar, modifier = Modifier.size(16.dp))
                    }
                }
                Spacer(Modifier.weight(1f))
                Surface(shape = RoundedCornerShape(9999.dp), color = Color.Black) {
                    Text(
                        text = "${pendientes.size} ${if (pendientes.size == 1) "PENDIENTE" else "PENDIENTES"}",
                        style = estilo(FontWeight.ExtraBold, 10),
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text("Por reclamar", style = estilo(FontWeight.Bold, 13), color = p.ambarTexto)
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = String.format(Locale.US, "S/ %,.2f", total),
                    style = estilo(FontWeight.ExtraBold, 15, -0.375f),
                    color = p.ambarTexto,
                    modifier = Modifier.weight(1f),
                )
                Surface(shape = RoundedCornerShape(9999.dp), color = Color.Black) {
                    Text(
                        text = "Reclamar →",
                        style = estilo(FontWeight.Bold, 11),
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    )
                }
            }
        }
    }
}
