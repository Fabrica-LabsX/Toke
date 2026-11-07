package com.flabs.cobrate.features.cobros

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flabs.cobrate.components.BilleteraColores
import com.flabs.cobrate.components.IconoMetodoPago
import com.flabs.cobrate.components.TokeIcons
import com.flabs.cobrate.core.model.MetodoDeCobro
import com.flabs.cobrate.ui.theme.PlusJakartaSans
import com.flabs.cobrate.ui.theme.TokePaleta
import com.flabs.cobrate.ui.theme.tokeEsOscuro

/**
 * Tarjeta del método elegido: nombre, botón Editar, el QR, titular y número con su botón Copiar.
 * [onEditar] y [onCargarQr] solo se entregan si quien mira puede modificar el método.
 */
@Composable
fun CobrosTarjeta(
    paleta: TokePaleta,
    metodo: MetodoDeCobro,
    qrCache: Map<String, ByteArray>,
    onObtenerQr: (String) -> Unit,
    onAmpliarQr: () -> Unit,
    onCopiar: (String) -> Unit,
    onEditar: (() -> Unit)?,
    onCargarQr: (() -> Unit)?,
) {
    Surface(
        shape = RoundedCornerShape(26.dp),
        color = paleta.tarjeta,
        border = BorderStroke(1.dp, paleta.borde),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconoMetodoPago(tipo = metodo.tipo, tamano = 48.dp)
                Spacer(modifier = Modifier.width(14.dp))
                NombreMetodo(
                    metodo = metodo,
                    estilo = TextStyle(
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 19.sp,
                        letterSpacing = (-0.3).sp,
                    ),
                    colorNombre = paleta.textoPrincipal,
                    colorMarca = if (tokeEsOscuro()) BilleteraColores.Plin.oscuro else BilleteraColores.Plin.claro,
                    colorBarrera = paleta.bordeControl,
                    modifier = Modifier.weight(1f),
                )
                if (onEditar != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    ChipAccion(paleta = paleta, texto = "Editar", icono = TokeIcons.Editar, onClick = onEditar)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                QrCuadro(
                    paleta = paleta,
                    qrPath = metodo.qrUrl,
                    qrCache = qrCache,
                    puedeCargar = onCargarQr != null,
                    onObtener = onObtenerQr,
                    onAmpliar = onAmpliarQr,
                    onCargar = { onCargarQr?.invoke() },
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(paleta.borde),
            )
            Spacer(modifier = Modifier.height(16.dp))

            Etiqueta(paleta, "TITULAR")
            Text(
                text = MetodosCobroLogica.titular(metodo),
                style = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp,
                ),
                color = paleta.textoPrincipal,
                maxLines = 2,
            )

            Spacer(modifier = Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Etiqueta(paleta, "NÚMERO ASOCIADO")
                    Text(
                        text = MetodosCobroLogica.numero(metodo),
                        style = TextStyle(
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 28.sp,
                            letterSpacing = 0.5.sp,
                        ),
                        color = paleta.textoPrincipal,
                        maxLines = 1,
                    )
                }
                val copiable = MetodosCobroLogica.numeroCopiable(metodo)
                if (copiable != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    ChipAccion(paleta = paleta, texto = "Copiar", icono = TokeIcons.Copiar, onClick = { onCopiar(copiable) })
                }
            }
        }
    }
}

@Composable
private fun Etiqueta(paleta: TokePaleta, texto: String) {
    Text(
        text = texto,
        style = TextStyle(
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            letterSpacing = 0.8.sp,
        ),
        color = paleta.textoSecundario,
    )
}

@Composable
private fun ChipAccion(paleta: TokePaleta, texto: String, icono: Int, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(9999.dp),
        color = paleta.tileFondo,
        border = BorderStroke(1.dp, paleta.bordeControl),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(id = icono),
                contentDescription = null,
                tint = paleta.textoPrincipal,
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = texto,
                style = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                ),
                color = paleta.textoPrincipal,
            )
        }
    }
}
