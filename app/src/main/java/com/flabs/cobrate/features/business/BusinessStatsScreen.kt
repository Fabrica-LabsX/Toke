package com.flabs.cobrate.features.business

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flabs.cobrate.R
import com.flabs.cobrate.components.TokeIcons
import com.flabs.cobrate.features.acceso.BotonAcceso
import com.flabs.cobrate.features.reportes.PeriodoReporte
import com.flabs.cobrate.features.planes.ActivarProDialogo
import com.flabs.cobrate.features.planes.PlanLogica
import com.flabs.cobrate.features.planes.PlanStore
import com.flabs.cobrate.features.reportes.ReportesAcciones
import com.flabs.cobrate.features.reportes.ReportesLogica
import com.flabs.cobrate.ui.theme.PlusJakartaSans
import com.flabs.cobrate.ui.theme.TokeMedidas
import com.flabs.cobrate.ui.theme.TokePaleta
import com.flabs.cobrate.ui.theme.tokePaleta
import java.util.Locale
import kotlinx.coroutines.launch

/**
 * Reportes del negocio: totales por día, semana, mes o año con los cobros reales, una
 * recomendación para cerrar el día y la exportación a Excel.
 */
@Composable
fun BusinessStatsScreen(businessViewModel: BusinessViewModel) {
    val paleta = tokePaleta()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val operaciones by businessViewModel.operaciones.collectAsState()
    val trabajadores by businessViewModel.trabajadoresReales.collectAsState()

    var periodo by remember { mutableStateOf(PeriodoReporte.DIA) }
    var elegirParaExportar by remember { mutableStateOf(false) }
    val planEstado by PlanStore.estado.collectAsState()
    var verActivacion by remember { mutableStateOf(false) }
    // En Free solo se ve el día; el resto lleva al aviso del plan Pro.
    val puede = { p: PeriodoReporte -> PlanLogica.puedeVerPeriodo(planEstado?.plan, p) }
    if (verActivacion) ActivarProDialogo(onCerrar = { verActivacion = false })

    val resumen = remember(operaciones, periodo) {
        ReportesLogica.resumen(ReportesLogica.filtrar(operaciones, periodo, System.currentTimeMillis()))
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
            Surface(shape = RoundedCornerShape(14.dp), color = paleta.tileFondo, modifier = Modifier.size(48.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(id = TokeIcons.Reportes),
                        contentDescription = null,
                        tint = paleta.textoPrincipal,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = stringResource(R.string.nombre_reportes),
                style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp),
                color = paleta.textoPrincipal,
            )
        }

        SelectorPeriodo(paleta, periodo, bloqueado = { !puede(it) }) { if (puede(it)) periodo = it else verActivacion = true }

        Surface(
            shape = RoundedCornerShape(24.dp),
            color = paleta.tarjeta,
            border = BorderStroke(1.dp, paleta.borde),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "Total ${periodo.titulo.lowercase()}",
                    style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Medium, fontSize = 14.sp),
                    color = paleta.textoSecundario,
                )
                Text(
                    text = "S/ %.2f".format(Locale.US, resumen.total),
                    style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.ExtraBold, fontSize = 36.sp, letterSpacing = (-1).sp),
                    color = paleta.textoPrincipal,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Dato(paleta, "Cobros", resumen.cobros.toString(), Modifier.weight(1f))
                    Dato(paleta, "Promedio", "S/ %.2f".format(Locale.US, resumen.promedio), Modifier.weight(1f))
                }
            }
        }

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = paleta.tarjeta,
            border = BorderStroke(1.dp, paleta.borde),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(TokeMedidas.PaddingTarjeta), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Recomendación",
                    style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp),
                    color = paleta.textoPrincipal,
                )
                Text(
                    text = "Antes de exportar, asegúrate de terminar el día. Así tu cuenta de efectivo cuadra con tus cobros.",
                    style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Medium, fontSize = 13.5.sp, lineHeight = 19.sp),
                    color = paleta.textoSecundario,
                )
            }
        }

        BotonAcceso(paleta, "Exportar en Excel", principal = true) { elegirParaExportar = true }
    }

    if (elegirParaExportar) {
        ElegirPeriodoHoja(
            paleta = paleta,
            inicial = periodo,
            onDismiss = { elegirParaExportar = false },
            onExportar = { elegido ->
                elegirParaExportar = false
                val nombres = trabajadores.associate { it.cuentaId to it.username }
                if (!puede(elegido)) verActivacion = true
                else scope.launch { ReportesAcciones.exportarExcel(context, operaciones, nombres, elegido) }
            },
        )
    }
}

@Composable
private fun SelectorPeriodo(paleta: TokePaleta, actual: PeriodoReporte, bloqueado: (PeriodoReporte) -> Boolean, onElegir: (PeriodoReporte) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(TokeMedidas.BotonAltura)
            .background(paleta.tileFondo, RoundedCornerShape(9999.dp))
            .padding(4.dp),
    ) {
        PeriodoReporte.entries.forEach { p ->
            val elegido = p == actual
            Surface(
                onClick = { onElegir(p) },
                shape = RoundedCornerShape(9999.dp),
                color = if (elegido) paleta.seleccionFondo else androidx.compose.ui.graphics.Color.Transparent,
                modifier = Modifier.weight(1f).height(TokeMedidas.BotonAltura - 8.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = corto(p),
                        style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = if (elegido) FontWeight.Bold else FontWeight.Medium, fontSize = 14.sp),
                        color = if (elegido) paleta.seleccionTexto else if (bloqueado(p)) paleta.textoSecundario.copy(alpha = 0.55f) else paleta.textoSecundario,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

private fun corto(p: PeriodoReporte) = when (p) {
    PeriodoReporte.DIA -> "Día"
    PeriodoReporte.SEMANA -> "Semana"
    PeriodoReporte.MES -> "Mes"
    PeriodoReporte.ANIO -> "Año"
}

@Composable
private fun Dato(paleta: TokePaleta, etiqueta: String, valor: String, modifier: Modifier) {
    Surface(shape = RoundedCornerShape(14.dp), color = paleta.tileFondo, modifier = modifier) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = etiqueta,
                style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Medium, fontSize = 12.sp),
                color = paleta.textoSecundario,
            )
            Text(
                text = valor,
                style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp),
                color = paleta.textoPrincipal,
            )
        }
    }
}

/** Hoja para elegir qué periodo se exporta a Excel. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ElegirPeriodoHoja(
    paleta: TokePaleta,
    inicial: PeriodoReporte,
    onDismiss: () -> Unit,
    onExportar: (PeriodoReporte) -> Unit,
) {
    var elegido by remember { mutableStateOf(inicial) }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = paleta.fondo,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = TokeMedidas.MargenLateral),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "¿Qué periodo exportas?",
                style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.ExtraBold, fontSize = 21.sp),
                color = paleta.textoPrincipal,
            )
            Text(
                text = "Se crea un Excel con todos los cobros de ese periodo.",
                style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Medium, fontSize = 14.sp),
                color = paleta.textoSecundario,
            )
            PeriodoReporte.entries.forEach { p ->
                val activo = p == elegido
                Surface(
                    onClick = { elegido = p },
                    shape = RoundedCornerShape(16.dp),
                    color = paleta.tarjeta,
                    border = BorderStroke(if (activo) 2.dp else 1.dp, if (activo) paleta.verde else paleta.borde),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = p.titulo,
                                style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 16.sp),
                                color = paleta.textoPrincipal,
                            )
                            Text(
                                text = p.detalle,
                                style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Medium, fontSize = 13.sp),
                                color = paleta.textoSecundario,
                            )
                        }
                        Icon(
                            painter = painterResource(id = if (activo) TokeIcons.RadioMarcado else TokeIcons.RadioVacio),
                            contentDescription = null,
                            tint = if (activo) paleta.verde else paleta.bordeControl,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            BotonAcceso(paleta, "Exportar en Excel", principal = true) { onExportar(elegido) }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
