package com.flabs.cobrate.features.business

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import com.flabs.cobrate.features.planes.PlanLogica
import com.flabs.cobrate.features.planes.PlanStore
import com.flabs.cobrate.features.planes.TablaComparacionPlanes
import com.flabs.cobrate.ui.theme.PlusJakartaSans
import com.flabs.cobrate.ui.theme.TokeMedidas
import com.flabs.cobrate.ui.theme.tokePaleta

/** "Mi plan": comparación Free y Pro, cuál tienes y cómo activar Pro. */
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

        Text(
            text = if (esPro) "Tu plan Pro está activo. ${PlanLogica.textoVigencia(estado?.proHasta)}."
            else "Estás en el plan Free. Compara lo que incluye cada plan.",
            style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Medium, fontSize = 14.sp),
            color = paleta.textoSecundario,
        )

        TablaComparacionPlanes(paleta = paleta, actual = estado?.plan)

        if (!esPro) {
            Spacer(modifier = Modifier.size(4.dp))
            BotonAcceso(paleta, "Activar plan Pro", principal = true) { verActivacion = true }
        }
    }

    if (verActivacion) ActivarProDialogo(onCerrar = { verActivacion = false })
}
