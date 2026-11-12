package com.flabs.cobrate.features.operaciones
import com.flabs.cobrate.ui.theme.TokeMedidas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flabs.cobrate.core.data.InMemoryOperationStore
import com.flabs.cobrate.core.data.NegocioStore
import com.flabs.cobrate.core.data.RelacionCuentaNegocioStore
import com.flabs.cobrate.core.model.EstadoRelacion
import com.flabs.cobrate.core.model.OrigenOperacion
import com.flabs.cobrate.core.model.RolCuentaNegocio
import com.flabs.cobrate.features.business.BusinessViewModel
import com.flabs.cobrate.ui.theme.PlusJakartaSans
import com.flabs.cobrate.ui.theme.TokePaleta
import com.flabs.cobrate.ui.theme.tokeEsOscuro
import com.flabs.cobrate.ui.theme.tokePaleta

/**
 * Detalle de una operación. Reúne los datos de las tiendas de la app y arma las piezas:
 *  - qué se muestra lo decide [ConstructorDetalleOperacion];
 *  - cómo se ve cada pieza vive en los archivos DetalleOperacion*.
 */
@Composable
fun DetalleOperacionScreen(
    operacionId: String,
    businessViewModel: BusinessViewModel,
    onBack: () -> Unit,
    onVolverInicio: () -> Unit,
) {
    val paleta = tokePaleta()
    val esOscuro = tokeEsOscuro()

    val operaciones by InMemoryOperationStore.operaciones.collectAsState()
    val negocios by NegocioStore.negocios.collectAsState()
    val trabajadoresReales by businessViewModel.trabajadoresReales.collectAsState()
    val relaciones by RelacionCuentaNegocioStore.relaciones.collectAsState()

    val operacion = remember(operaciones, operacionId) { operaciones.find { it.id == operacionId } }

    Scaffold(
        containerColor = paleta.fondo,
        topBar = { DetalleOperacionCabecera(paleta = paleta, onBack = onBack) },
        bottomBar = { BotonVolverInicio(paleta = paleta, onClick = onVolverInicio) },
    ) { innerPadding ->
        if (operacion == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Operación no encontrada",
                    style = TextStyle(
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp,
                    ),
                    color = paleta.textoSecundario,
                )
            }
            return@Scaffold
        }

        val tituloNotificacion: String? = null // PRI-6: el texto original no se guarda; el pagador viene normalizado en la operación
        val trabajadoresActivos = remember(relaciones, operacion.negocioId) {
            relaciones.count {
                it.negocioId == operacion.negocioId &&
                    it.rol == RolCuentaNegocio.TRABAJADOR &&
                    it.estado == EstadoRelacion.ACTIVA
            }
        }
        val detalle = remember(
            operacion, negocios, tituloNotificacion, trabajadoresActivos, trabajadoresReales,
        ) {
            ConstructorDetalleOperacion.construir(
                operacion = operacion,
                nombreNegocio = negocios.find { it.id == operacion.negocioId }?.nombre,
                tituloNotificacion = tituloNotificacion,
                trabajadoresActivos = trabajadoresActivos,
                nombreTrabajador = trabajadoresReales
                    .find { it.cuentaId == operacion.trabajadorAsignadoId }?.username,
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top,
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            DetalleOperacionResumen(paleta = paleta, detalle = detalle)
            Spacer(modifier = Modifier.height(24.dp))
            DetalleOperacionMetodo(paleta = paleta, detalle = detalle)
            Spacer(modifier = Modifier.height(16.dp))
            DetalleOperacionInfo(paleta = paleta, esOscuro = esOscuro, detalle = detalle)
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun BotonVolverInicio(paleta: TokePaleta, onClick: () -> Unit) {
    Surface(color = paleta.fondo, modifier = Modifier.fillMaxWidth()) {
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(16.dp),
            color = paleta.verde,
            modifier = Modifier
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth()
                .height(TokeMedidas.BotonAltura),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "Volver al inicio",
                    style = TextStyle(
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                    ),
                    color = paleta.verdeTexto,
                )
            }
        }
    }
}
