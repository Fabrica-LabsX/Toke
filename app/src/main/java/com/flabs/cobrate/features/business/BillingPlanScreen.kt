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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Color
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
import com.flabs.cobrate.features.planes.PlanStore
import com.flabs.cobrate.ui.theme.PlusJakartaSans
import com.flabs.cobrate.ui.theme.TokeMedidas
import com.flabs.cobrate.ui.theme.TokePaleta
import com.flabs.cobrate.ui.theme.tokePaleta
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val VentajasFree = listOf(
    "50 notificaciones al día",
    "1 negocio",
    "Hasta 2 colaboradores",
    "Aviso de voz Cauteloso",
    "Reportes de hoy",
    "Historial de 30 días",
)

private val VentajasPro = listOf(
    "Notificaciones sin límite",
    "Varios negocios",
    "Más colaboradores",
    "Aviso de voz Básico (dice billetera y monto)",
    "Reportes de la semana, el mes y el año",
    "Historial completo",
)

/** "Mi plan": los dos planes (Free y Pro), cuál tienes y cómo activar Pro. */
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
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(14.dp), color = paleta.tileFondo, modifier = Modifier.size(48.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(id = TokeIcons.Recibo),
                        contentDescription = null,
                        tint = paleta.textoPrincipal,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = stringResource(R.string.nombre_mi_plan),
                style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp),
                color = paleta.textoPrincipal,
            )
        }

        PlanCard(
            paleta = paleta,
            nombre = "Free",
            descripcion = "Para empezar a recibir tus cobros.",
            ventajas = VentajasFree,
            actual = estado?.plan == Plan.FREE,
            detalle = null,
        )
        PlanCard(
            paleta = paleta,
            nombre = "Pro",
            descripcion = "Para negocios que crecen.",
            ventajas = VentajasPro,
            actual = esPro,
            detalle = estado?.proHasta?.let { "Activo hasta ${formatoFecha(it)}" },
        )

        if (!esPro) {
            Spacer(modifier = Modifier.size(4.dp))
            BotonAcceso(paleta, "Activar plan Pro", principal = true) { verActivacion = true }
        }
    }

    if (verActivacion) ActivarProDialogo(onCerrar = { verActivacion = false })
}

private fun formatoFecha(milis: Long): String =
    SimpleDateFormat("d 'de' MMMM", Locale.forLanguageTag("es-PE")).format(Date(milis))

@Composable
private fun PlanCard(
    paleta: TokePaleta,
    nombre: String,
    descripcion: String,
    ventajas: List<String>,
    actual: Boolean,
    detalle: String?,
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = paleta.tarjeta,
        border = BorderStroke(if (actual) 2.dp else 1.dp, if (actual) paleta.verde else paleta.borde),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(TokeMedidas.PaddingTarjeta), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = nombre,
                    style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp),
                    color = paleta.textoPrincipal,
                    modifier = Modifier.weight(1f),
                )
                if (actual) {
                    Surface(shape = RoundedCornerShape(9999.dp), color = paleta.tileFondo) {
                        Text(
                            text = "Tu plan actual",
                            style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 12.sp),
                            color = paleta.enlaceVerde,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                        )
                    }
                }
            }
            Text(
                text = descripcion,
                style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Medium, fontSize = 14.sp),
                color = paleta.textoSecundario,
            )
            if (detalle != null) {
                Text(
                    text = detalle,
                    style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 13.sp),
                    color = paleta.enlaceVerde,
                )
            }
            ventajas.forEach { ventaja ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = TokeIcons.CheckVerde),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = ventaja,
                        style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Medium, fontSize = 15.sp),
                        color = paleta.textoPrincipal,
                    )
                }
            }
        }
    }
}
