package com.flabs.cobrate.features.worker

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flabs.cobrate.components.OperacionCard
import com.flabs.cobrate.components.OperacionCardAccion
import com.flabs.cobrate.core.model.EstadoOperacion
import com.flabs.cobrate.features.acceso.BotonAtras
import com.flabs.cobrate.features.acceso.CampoAcceso
import com.flabs.cobrate.ui.theme.PlusJakartaSans
import com.flabs.cobrate.ui.theme.TokeMedidas
import com.flabs.cobrate.ui.theme.tokePaleta

/**
 * "Encuentra mi pedido": el trabajador escribe el monto que acaba de recibir y se listan los
 * cobros sin asignar con ese monto (tolerancia de ±S/0.50). Tocar "Asignarme" reclama el cobro.
 */
@Composable
fun EncuentraMiPedidoScreen(
    viewModel: WorkerViewModel,
    onBack: () -> Unit
) {
    val paleta = tokePaleta()
    var montoTexto by remember { mutableStateOf("") }
    val monto = montoTexto.toDoubleOrNull()

    val todasLasOperaciones by viewModel.operaciones.collectAsState()
    val operacionesSinAsignar = remember(todasLasOperaciones) {
        todasLasOperaciones.filter { it.estado == EstadoOperacion.SIN_ASIGNAR }
    }
    val candidatas = remember(monto, operacionesSinAsignar) {
        viewModel.buscarCandidatas(monto, operacionesSinAsignar)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(paleta.fondo)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = TokeMedidas.MargenLateral),
        verticalArrangement = Arrangement.spacedBy(TokeMedidas.EntreTituloYContenido),
    ) {
        Row(modifier = Modifier.padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            BotonAtras(paleta, onBack)
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = "Encuentra mi pedido",
                style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp),
                color = paleta.textoPrincipal,
            )
        }

        CampoAcceso(
            paleta = paleta,
            etiqueta = "¿Cuánto te pagaron? (S/)",
            valor = montoTexto,
            placeholder = "Ej: 15.50",
            onCambia = { montoTexto = it },
            teclado = KeyboardType.Decimal,
        )

        if (candidatas.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(top = 24.dp), contentAlignment = Alignment.TopCenter) {
                Text(
                    text = if (montoTexto.isEmpty()) "Escribe un monto para buscar"
                    else "No hay cobros pendientes con ese monto",
                    style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Medium, fontSize = 15.sp),
                    color = paleta.textoSecundario,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(TokeMedidas.EntreGrupos),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(candidatas, key = { it.id }) { operacion ->
                    OperacionCard(
                        operacion = operacion,
                        accion = OperacionCardAccion(
                            etiqueta = "Asignarme",
                            onClick = { viewModel.reclamarOperacion(operacion.id) }
                        )
                    )
                }
            }
        }
    }
}
