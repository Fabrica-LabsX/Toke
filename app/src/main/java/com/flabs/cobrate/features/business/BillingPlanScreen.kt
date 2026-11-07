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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flabs.cobrate.R
import com.flabs.cobrate.components.TokeIcons
import com.flabs.cobrate.features.acceso.BotonAcceso
import com.flabs.cobrate.ui.theme.PlusJakartaSans
import com.flabs.cobrate.ui.theme.TokeMedidas
import com.flabs.cobrate.ui.theme.TokePaleta
import com.flabs.cobrate.ui.theme.tokePaleta

/** "Mi plan": el plan actual y el siguiente. La mejora de plan todavía no está disponible. */
@Composable
fun BillingPlanScreen() {
    val paleta = tokePaleta()
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
        Spacer(modifier = Modifier.size(4.dp))

        PlanCard(paleta, "LOCAL", "S/ 20/mes", "Para un solo local. Incluye cobros y reportes básicos.", actual = true)
        PlanCard(paleta, "MAX", "S/ 40/mes", "Varios establecimientos, estadísticas avanzadas y más.", actual = false)

        Spacer(modifier = Modifier.size(8.dp))
        BotonAcceso(paleta, "Actualizar a MAX (próximamente)", principal = false) { /* Aún no disponible. */ }
    }
}

@Composable
private fun PlanCard(paleta: TokePaleta, nombre: String, precio: String, descripcion: String, actual: Boolean) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = paleta.tarjeta,
        border = BorderStroke(if (actual) 2.dp else 1.dp, if (actual) paleta.verde else paleta.borde),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(TokeMedidas.PaddingTarjeta), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = nombre,
                    style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp),
                    color = paleta.textoPrincipal,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = precio,
                    style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 16.sp),
                    color = paleta.enlaceVerde,
                )
            }
            Text(
                text = descripcion,
                style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
                color = paleta.textoSecundario,
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
    }
}
