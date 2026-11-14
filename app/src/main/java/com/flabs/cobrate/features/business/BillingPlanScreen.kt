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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flabs.cobrate.R
import com.flabs.cobrate.components.TokeIcons
import com.flabs.cobrate.features.acceso.BotonAcceso
import com.flabs.cobrate.features.planes.ActivarProDialogo
import com.flabs.cobrate.features.planes.Plan
import com.flabs.cobrate.features.planes.PlanComparacion
import com.flabs.cobrate.features.planes.PlanLogica
import com.flabs.cobrate.features.planes.PlanStore
import com.flabs.cobrate.ui.theme.PlusJakartaSans
import com.flabs.cobrate.ui.theme.TokeMedidas
import com.flabs.cobrate.ui.theme.TokePaleta
import com.flabs.cobrate.ui.theme.tokePaleta

private fun estilo(peso: FontWeight, tam: Int) =
    TextStyle(fontFamily = PlusJakartaSans, fontWeight = peso, fontSize = tam.sp)

/** "Mi plan": tu plan de hoy, cuánto llevas usado y qué cambia con Pro. */
@Composable
fun BillingPlanScreen() {
    val paleta = tokePaleta()
    val estado by PlanStore.estado.collectAsState()
    var verActivacion by remember { mutableStateOf(false) }
    val esPro = estado?.plan == Plan.PRO

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
        Text(
            text = stringResource(R.string.nombre_mi_plan),
            style = estilo(FontWeight.ExtraBold, 26),
            color = paleta.textoPrincipal,
        )

        TarjetaPlanActual(paleta, esPro, estado?.proHasta, estado?.usadas ?: 0, estado?.limite)

        Text(
            text = if (esPro) "Lo que tienes con Pro" else "Lo que te da Pro",
            style = estilo(FontWeight.ExtraBold, 18),
            color = paleta.textoPrincipal,
            modifier = Modifier.padding(top = 4.dp),
        )
        ListaBeneficios(paleta, esPro)

        if (!esPro) {
            Spacer(modifier = Modifier.size(4.dp))
            BotonAcceso(paleta, "Activar plan Pro", principal = true) { verActivacion = true }
            Text(
                text = "Escríbenos por WhatsApp y lo activamos en tu cuenta.",
                style = estilo(FontWeight.Medium, 13),
                color = paleta.textoSecundario,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
    }

    if (verActivacion) ActivarProDialogo(onCerrar = { verActivacion = false })
}

/** Tarjeta grande: el plan que tienes, y el uso de hoy (Free) o la vigencia (Pro). */
@Composable
private fun TarjetaPlanActual(paleta: TokePaleta, esPro: Boolean, proHasta: Long?, usadas: Int, limite: Int?) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = if (esPro) paleta.verde else paleta.tarjeta,
        border = if (esPro) null else BorderStroke(1.dp, paleta.borde),
        modifier = Modifier.fillMaxWidth(),
    ) {
        val textoFuerte = if (esPro) paleta.verdeTexto else paleta.textoPrincipal
        val textoSuave = if (esPro) paleta.verdeTexto else paleta.textoSecundario
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Tu plan",
                    style = estilo(FontWeight.SemiBold, 13),
                    color = textoSuave,
                    modifier = Modifier.weight(1f),
                )
                Surface(
                    shape = RoundedCornerShape(9999.dp),
                    color = if (esPro) paleta.verdeFondo else paleta.tileFondo,
                    border = BorderStroke(1.dp, if (esPro) paleta.verdeBorde else paleta.bordeControl),
                ) {
                    Text(
                        text = if (esPro) "Activo" else "Gratis",
                        style = estilo(FontWeight.Bold, 12),
                        color = if (esPro) paleta.activo else textoFuerte,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
            }
            Text(
                text = if (esPro) "Pro" else "Free",
                style = estilo(FontWeight.ExtraBold, 40),
                color = textoFuerte,
            )
            if (esPro) {
                Text(
                    text = "${PlanLogica.textoVigencia(proHasta)}. Avisos sin límite en todos tus negocios.",
                    style = estilo(FontWeight.Medium, 14),
                    color = textoSuave,
                )
            } else if (limite != null) {
                val progreso = (usadas.toFloat() / limite).coerceIn(0f, 1f)
                Text(
                    text = "Hoy llevas $usadas de $limite avisos",
                    style = estilo(FontWeight.Bold, 15),
                    color = textoFuerte,
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(paleta.tileFondo),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progreso)
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(paleta.verde),
                    )
                }
                Text(
                    text = "Se reinicia cada día y se comparte entre todos tus negocios.",
                    style = estilo(FontWeight.Medium, 13),
                    color = textoSuave,
                )
            }
        }
    }
}

/** Una tarjeta con las mejoras de Pro; en Free cada fila recuerda lo que hoy tienes. */
@Composable
private fun ListaBeneficios(paleta: TokePaleta, esPro: Boolean) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = paleta.tarjeta,
        border = BorderStroke(1.dp, paleta.borde),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(vertical = 6.dp)) {
            PlanComparacion.filas.forEachIndexed { i, fila ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                ) {
                    Surface(shape = CircleShape, color = paleta.verde, modifier = Modifier.size(26.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(id = TokeIcons.Check),
                                contentDescription = null,
                                tint = paleta.verdeTexto,
                                modifier = Modifier.size(14.dp),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(fila.funcion, style = estilo(FontWeight.Bold, 15), color = paleta.textoPrincipal)
                        Text(
                            text = if (esPro) fila.pro else "${fila.pro}  ·  Free: ${fila.free}",
                            style = estilo(FontWeight.Medium, 13),
                            color = paleta.textoSecundario,
                        )
                    }
                }
                if (i < PlanComparacion.filas.lastIndex) {
                    Box(modifier = Modifier.fillMaxWidth().padding(start = 54.dp).height(1.dp).background(paleta.borde))
                }
            }
        }
    }
}
