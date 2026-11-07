package com.flabs.cobrate.features.planes

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.flabs.cobrate.components.TokeIcons
import com.flabs.cobrate.features.acceso.BotonAcceso
import com.flabs.cobrate.ui.theme.PlusJakartaSans
import com.flabs.cobrate.ui.theme.TokeMedidas
import com.flabs.cobrate.ui.theme.tokePaleta

private val Desbloqueado = listOf(
    "Notificaciones sin límite",
    "Varios negocios y más colaboradores",
    "Aviso de voz Básico",
    "Reportes de semana, mes y año",
    "Historial completo y Excel",
)

/** Pantalla de éxito: se muestra una vez, cuando el plan pasa de Free a Pro. */
@Composable
fun ProActivadoPantalla(proHasta: Long?, onEmpezar: () -> Unit) {
    val paleta = tokePaleta()
    Dialog(
        onDismissRequest = onEmpezar,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(paleta.fondo)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = TokeMedidas.MargenLateral, vertical = 24.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Box(
                    modifier = Modifier.size(96.dp).background(paleta.verde, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(TokeIcons.Check),
                        contentDescription = null,
                        tint = paleta.verdeTexto,
                        modifier = Modifier.size(48.dp),
                    )
                }
                Spacer(Modifier.height(24.dp))
                Text(
                    "¡Plan Pro activado!",
                    style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.ExtraBold, fontSize = 28.sp),
                    color = paleta.textoPrincipal,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    PlanLogica.textoVigencia(proHasta),
                    style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 15.sp),
                    color = paleta.enlaceVerde,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(28.dp))
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Desbloqueado.forEach { texto ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(TokeIcons.CheckVerde),
                                contentDescription = null,
                                tint = Color.Unspecified,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                texto,
                                style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Medium, fontSize = 15.sp),
                                color = paleta.textoPrincipal,
                            )
                        }
                    }
                }
            }
            BotonAcceso(paleta, "Empezar", principal = true) { onEmpezar() }
        }
    }
}
