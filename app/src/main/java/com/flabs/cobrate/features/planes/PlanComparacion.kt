package com.flabs.cobrate.features.planes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flabs.cobrate.ui.theme.PlusJakartaSans
import com.flabs.cobrate.ui.theme.TokePaleta

/** Una fila de la comparación: qué ofrece cada plan. */
data class FilaPlan(val funcion: String, val free: String, val pro: String)

/** Lo que cambia entre Free y Pro. Una sola lista para Mi plan y para la pantalla de activación. */
object PlanComparacion {
    val filas = listOf(
        FilaPlan("Notificaciones al día", "20", "Sin límite"),
        FilaPlan("Negocios", "1", "Varios"),
        FilaPlan("Colaboradores", "Hasta 2", "Sin tope"),
        FilaPlan("Métodos de cobro", "Hasta 4", "Sin tope"),
        FilaPlan("Aviso de voz", "Cauteloso", "Cauteloso y Básico"),
        FilaPlan("Reportes", "Hoy", "Semana, mes y año"),
        FilaPlan("Historial", "30 días", "Completo"),
        FilaPlan("Exportar a Excel", "No", "Sí"),
    )
}

private fun estilo(peso: FontWeight, tam: Int) =
    TextStyle(fontFamily = PlusJakartaSans, fontWeight = peso, fontSize = tam.sp)

/** Tabla Free vs Pro. [actual] resalta la columna del plan que tiene la cuenta. */
@Composable
fun TablaComparacionPlanes(paleta: TokePaleta, actual: Plan?) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = paleta.tarjeta,
        border = BorderStroke(1.dp, paleta.borde),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                Box(modifier = Modifier.weight(1.25f))
                EncabezadoPlan(paleta, "Free", actual == Plan.FREE, Modifier.weight(1f))
                EncabezadoPlan(paleta, "Pro", actual == Plan.PRO, Modifier.weight(1.1f))
            }
            PlanComparacion.filas.forEachIndexed { i, fila ->
                if (i > 0) HorizontalDivider(color = paleta.borde)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
                    Text(fila.funcion, style = estilo(FontWeight.SemiBold, 13), color = paleta.textoPrincipal, modifier = Modifier.weight(1.25f))
                    Text(
                        fila.free,
                        style = estilo(FontWeight.Medium, 12),
                        color = if (fila.free == "No") paleta.textoSecundario else paleta.textoPrincipal,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        fila.pro,
                        style = estilo(FontWeight.Bold, 12),
                        color = paleta.enlaceVerde,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1.1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun EncabezadoPlan(paleta: TokePaleta, nombre: String, actual: Boolean, modifier: Modifier) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Text(nombre, style = estilo(FontWeight.ExtraBold, 17), color = paleta.textoPrincipal)
        if (actual) {
            Surface(shape = RoundedCornerShape(9999.dp), color = paleta.tileFondo, modifier = Modifier.padding(top = 4.dp)) {
                Text(
                    "Tu plan",
                    style = estilo(FontWeight.Bold, 11),
                    color = paleta.enlaceVerde,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                )
            }
        }
    }
}
