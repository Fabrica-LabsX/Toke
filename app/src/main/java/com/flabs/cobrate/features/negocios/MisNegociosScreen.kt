package com.flabs.cobrate.features.negocios

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flabs.cobrate.components.PantallaAccesoPausado
import com.flabs.cobrate.core.data.CuentaStore
import com.flabs.cobrate.core.data.NegocioStore
import com.flabs.cobrate.core.logic.FeedbackManager
import com.flabs.cobrate.core.model.Negocio
import com.flabs.cobrate.features.business.NegocioViewModel
import com.flabs.cobrate.ui.theme.PlusJakartaSans
import com.flabs.cobrate.ui.theme.TokePaleta
import com.flabs.cobrate.ui.theme.tokePaleta
import kotlinx.coroutines.launch

/**
 * Pantalla "Mis Negocios". Reúne los datos y arma las piezas:
 *  - qué negocios salen y en qué estado lo decide [ListaNegocios];
 *  - las acciones con el servidor viven en [NegociosAcciones];
 *  - cómo se ve cada pieza vive en los archivos MisNegocios* y NegocioPiezas.
 */
@Composable
fun MisNegociosScreen(
    negocioViewModel: NegocioViewModel,
    onVolver: () -> Unit,
    onCrearNegocio: () -> Unit,
    onUnirmeNegocio: () -> Unit,
    onBorrarNegocio: (String) -> Unit = {},
) {
    val paleta = tokePaleta()
    val scope = rememberCoroutineScope()

    val todosLosNegocios by NegocioStore.negocios.collectAsState()
    val misRelaciones by negocioViewModel.misRelaciones.collectAsState()
    val negocioActivoId by negocioViewModel.negocioSeleccionadoId.collectAsState()
    val sesionActual by CuentaStore.sesionActual.collectAsState()
    val cuentasCargadas by CuentaStore.cuentas.collectAsState()
    val cuentaId = sesionActual?.id

    var negocioPausado by remember { mutableStateOf<Negocio?>(null) }
    var editando by remember { mutableStateOf<Negocio?>(null) }
    var maxInicial by remember { mutableStateOf<Int?>(null) }

    negocioPausado?.let { negocio ->
        PantallaAccesoPausado(
            negocioNombre = negocio.nombre,
            onVolver = { negocioPausado = null },
            onVerificarEstado = {
                val uid = cuentaId ?: return@PantallaAccesoPausado
                scope.launch {
                    NegociosAcciones.verificarAcceso(
                        negocio = negocio,
                        cuentaId = uid,
                        negocioViewModel = negocioViewModel,
                        alReactivar = {
                            negocioPausado = null
                            onVolver()
                        },
                    )
                }
            },
        )
        return
    }

    val items = remember(todosLosNegocios, misRelaciones, cuentaId, negocioActivoId) {
        ListaNegocios.construir(todosLosNegocios, misRelaciones, cuentaId, negocioActivoId)
    }

    Scaffold(
        containerColor = paleta.fondo,
        topBar = { MisNegociosCabecera(paleta = paleta, onVolver = onVolver) },
        bottomBar = {
            MisNegociosPie(
                paleta = paleta,
                onIngresarCodigo = onUnirmeNegocio,
                onCrearNegocio = onCrearNegocio,
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item { EncabezadoLista(paleta = paleta, total = items.size) }
            items(items, key = { it.negocio.id }) { item ->
                val nombreDueno = remember(item.negocio.dueñoCuentaId, cuentasCargadas, cuentaId) {
                    resolverNombreDueno(
                        duenoCuentaId = item.negocio.dueñoCuentaId,
                        currentUid = cuentaId,
                        cuentasCargadas = cuentasCargadas,
                        onCargarFaltante = { negocioViewModel.cargarCuentasFaltantes(listOf(it)) },
                    )
                }
                MisNegociosTarjeta(
                    paleta = paleta,
                    item = item,
                    nombreDueno = nombreDueno,
                    onEntrar = {
                        negocioViewModel.seleccionarNegocio(item.negocio.id)
                        onVolver()
                    },
                    onVerEstado = { negocioPausado = item.negocio },
                    onCancelarSolicitud = {
                        val uid = cuentaId ?: return@MisNegociosTarjeta
                        scope.launch {
                            NegociosAcciones.cancelarSolicitud(item.negocio.id, uid, negocioViewModel)
                        }
                    },
                    onEditar = if (item.rol == RolEnNegocio.DUENO) {
                        {
                            editando = item.negocio
                            maxInicial = null
                            scope.launch { maxInicial = NegocioStore.maxTrabajadores(item.negocio.id) }
                        }
                    } else {
                        null
                    },
                )
            }
        }
    }
    editando?.let { negocio ->
        EditarNegocioHoja(
            paleta = paleta,
            negocio = negocio,
            maxInicial = maxInicial,
            onDismiss = { editando = null },
            onBorrar = {
                editando = null
                onBorrarNegocio(negocio.id)
            },
            onGuardar = { nombre, tipo, color, max, limpiar ->
                scope.launch {
                    FeedbackManager.setLoading(true, "Guardando cambios...")
                    val resultado = NegocioStore.editar(negocio.id, nombre, tipo, color, max, limpiar)
                    FeedbackManager.setLoading(false)
                    resultado.onSuccess {
                        FeedbackManager.showSuccess("Cambios guardados.")
                        editando = null
                    }.onFailure { FeedbackManager.showError(it) }
                }
            },
        )
    }
}

@Composable
private fun EncabezadoLista(paleta: TokePaleta, total: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "NEGOCIOS VINCULADOS",
            style = TextStyle(
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                letterSpacing = 0.8.sp,
            ),
            color = paleta.textoSecundario,
            modifier = Modifier.weight(1f),
        )
        Surface(shape = RoundedCornerShape(9999.dp), color = paleta.bordeControl) {
            Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)) {
                Text(
                    text = "$total en total",
                    style = TextStyle(
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                    ),
                    color = paleta.textoPrincipal,
                )
            }
        }
    }
}
