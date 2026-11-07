package com.flabs.cobrate.features.operaciones

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flabs.cobrate.components.CampoBusqueda
import com.flabs.cobrate.core.model.EstadoOperacion
import com.flabs.cobrate.core.model.Operacion
import com.flabs.cobrate.ui.theme.PlusJakartaSans
import com.flabs.cobrate.ui.theme.TokePaleta
import com.flabs.cobrate.ui.theme.tokeEsOscuro
import com.flabs.cobrate.ui.theme.tokePaleta

/**
 * Pantalla de Operaciones. Solo arma las piezas y guarda los filtros elegidos:
 *  - qué se muestra lo decide [FiltroOperaciones];
 *  - cómo se ve cada pieza vive en los archivos Operaciones* y OperacionFilas.
 */
@Composable
fun OperacionesScreen(
    operaciones: List<Operacion>,
    onBack: (() -> Unit)? = null,
    onReclamarOperacion: ((String) -> Unit)? = null,
    onSeleccionarOperacion: ((String) -> Unit)? = null,
) {
    val paleta = tokePaleta()
    val esOscuro = tokeEsOscuro()
    // Sin 2 trabajadores activos no hay pagos por asignar: no hay pestaña ni sección ni botón de asignar.
    val asignacionHabilitada = rememberAsignacionHabilitada()

    var filtrosElegidos by remember { mutableStateOf(FiltrosOperaciones()) }
    val filtros = if (asignacionHabilitada) filtrosElegidos else filtrosElegidos.copy(pestana = PestanaOperaciones.TODAS)

    val visibles = remember(operaciones, filtros) {
        FiltroOperaciones.aplicar(operaciones, filtros, System.currentTimeMillis())
    }
    val porAsignar = remember(visibles, asignacionHabilitada) {
        if (asignacionHabilitada) visibles.filter { it.estado == EstadoOperacion.SIN_ASIGNAR } else emptyList()
    }
    val recibidas = remember(visibles, asignacionHabilitada) {
        if (asignacionHabilitada) visibles.filter { it.estado != EstadoOperacion.SIN_ASIGNAR } else visibles
    }
    val totalPendientes = remember(operaciones, asignacionHabilitada) {
        if (asignacionHabilitada) FiltroOperaciones.contarPendientes(operaciones) else 0
    }

    Scaffold(
        containerColor = paleta.fondo,
        topBar = {
            OperacionesCabecera(
                paleta = paleta,
                onBack = onBack,
                onRestablecerFiltros = { filtrosElegidos = FiltrosOperaciones() },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (asignacionHabilitada) item {
                OperacionesPestanas(
                    paleta = paleta,
                    seleccionada = filtros.pestana,
                    totalOperaciones = operaciones.size,
                    totalPendientes = totalPendientes,
                    onSeleccionar = { filtrosElegidos = filtrosElegidos.copy(pestana = it) },
                )
            }
            item {
                CampoBusqueda(
                    paleta = paleta,
                    texto = filtros.texto,
                    placeholder = "Buscar cliente o monto...",
                    onTextoCambia = { filtrosElegidos = filtrosElegidos.copy(texto = it) },
                )
            }
            item {
                OperacionesChips(
                    paleta = paleta,
                    esOscuro = esOscuro,
                    rango = filtros.rango,
                    billetera = filtros.billetera,
                    onRango = { filtrosElegidos = filtrosElegidos.copy(rango = it) },
                    onBilletera = { filtrosElegidos = filtrosElegidos.copy(billetera = it) },
                )
            }

            if (visibles.isEmpty()) {
                item {
                    OperacionesVacio(paleta = paleta, hayOperaciones = operaciones.isNotEmpty())
                }
            }

            if (porAsignar.isNotEmpty()) {
                item {
                    EncabezadoSeccion(
                        paleta = paleta,
                        titulo = "Por asignar",
                        insignia = porAsignar.size,
                    )
                }
                items(porAsignar, key = { it.id }) { op ->
                    OperacionPendienteFila(
                        paleta = paleta,
                        operacion = op,
                        onAsignar = { onReclamarOperacion?.invoke(op.id) },
                    )
                }
            }

            if (recibidas.isNotEmpty()) {
                item {
                    EncabezadoSeccion(
                        paleta = paleta,
                        titulo = "Cobros recibidos",
                        textoDerecha = if (recibidas.size == 1) "1 detectado" else "${recibidas.size} detectados",
                    )
                }
                items(recibidas, key = { it.id }) { op ->
                    OperacionRecibidaFila(
                        paleta = paleta,
                        operacion = op,
                        onClick = { onSeleccionarOperacion?.invoke(op.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun EncabezadoSeccion(
    paleta: TokePaleta,
    titulo: String,
    insignia: Int? = null,
    textoDerecha: String? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, start = 2.dp, end = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = titulo,
            style = TextStyle(
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
            ),
            color = paleta.textoPrincipal,
        )
        if (insignia != null) {
            Spacer(modifier = Modifier.width(8.dp))
            Surface(shape = CircleShape, color = paleta.ambar, modifier = Modifier.size(22.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "$insignia",
                        style = TextStyle(
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp,
                        ),
                        color = paleta.ambarTexto,
                    )
                }
            }
        }
        if (textoDerecha != null) {
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = textoDerecha,
                style = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                ),
                color = paleta.enlaceVerde,
            )
        }
    }
}
