package com.flabs.cobrate.features.crearnegocio

import com.flabs.cobrate.ui.theme.scrimSolido
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flabs.cobrate.core.model.TipoNegocio
import com.flabs.cobrate.ui.theme.PlusJakartaSans
import com.flabs.cobrate.ui.theme.TokeMedidas
import com.flabs.cobrate.ui.theme.TokePaleta

private const val COLUMNAS = 3

/**
 * Iconos del negocio en círculos, cada uno con su nombre debajo. Sirve para elegir el rubro al crear
 * el negocio y para cambiar el icono al editarlo (el icono es el del rubro). Tocar uno lo elige.
 */
@Composable
fun SelectorIconoNegocio(paleta: TokePaleta, elegido: TipoNegocio?, onElegir: (TipoNegocio) -> Unit) {
    val actual = CrearNegocioLogica.normalizar(elegido).takeIf { elegido != null }
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        CrearNegocioLogica.tipos.chunked(COLUMNAS).forEach { fila ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                fila.forEach { op ->
                    val activo = op.tipo == actual
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Surface(
                            onClick = { onElegir(op.tipo) },
                            shape = CircleShape,
                            color = if (activo) paleta.seleccionFondo else paleta.tarjeta,
                            border = BorderStroke(if (activo) 2.dp else 1.dp, if (activo) paleta.verde else paleta.bordeControl),
                            modifier = Modifier.size(68.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    painter = painterResource(id = iconoDeTipo(op.tipo)),
                                    contentDescription = op.titulo,
                                    tint = if (activo) paleta.seleccionTexto else paleta.textoPrincipal,
                                    modifier = Modifier.size(30.dp),
                                )
                            }
                        }
                        Text(
                            text = op.titulo,
                            style = TextStyle(
                                fontFamily = PlusJakartaSans,
                                fontWeight = if (activo) FontWeight.ExtraBold else FontWeight.SemiBold,
                                fontSize = 12.sp,
                                lineHeight = 15.sp,
                                textAlign = TextAlign.Center,
                            ),
                            color = paleta.textoPrincipal,
                            maxLines = 2,
                        )
                    }
                }
                // Completa la última fila para que los círculos queden alineados con los de arriba.
                repeat(COLUMNAS - fila.size) { Spacer(modifier = Modifier.weight(1f)) }
            }
        }
    }
}

/** Hoja para cambiar el icono del negocio desde su vista previa (Quick setup). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HojaIconoNegocio(paleta: TokePaleta, elegido: TipoNegocio?, onElegir: (TipoNegocio) -> Unit, onDismiss: () -> Unit) {
    ModalBottomSheet(
        scrimColor = com.flabs.cobrate.ui.theme.tokePaleta().let { it.scrimSolido() },
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = paleta.fondo,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = TokeMedidas.MargenLateral),
            verticalArrangement = Arrangement.spacedBy(TokeMedidas.EntreGrupos),
        ) {
            Text(
                text = "Icono de tu negocio",
                style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.ExtraBold, fontSize = 21.sp),
                color = paleta.textoPrincipal,
            )
            Text(
                text = "Elige el que mejor represente a tu negocio.",
                style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Medium, fontSize = 14.sp),
                color = paleta.textoSecundario,
            )
            SelectorIconoNegocio(paleta, elegido) { onElegir(it); onDismiss() }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
