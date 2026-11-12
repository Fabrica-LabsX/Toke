package com.flabs.cobrate.features.cobros

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.flabs.cobrate.components.TokeIcons
import com.flabs.cobrate.core.data.MetodoDeCobroStore
import com.flabs.cobrate.core.model.MetodoDeCobro
import com.flabs.cobrate.features.business.BusinessViewModel
import com.flabs.cobrate.features.business.QrEditorScreen
import com.flabs.cobrate.features.business.QrUploadProgressScreen
import com.flabs.cobrate.features.business.QrUploadStage
import com.flabs.cobrate.ui.theme.tokeEsOscuro
import com.flabs.cobrate.ui.theme.tokePaleta

/**
 * Pantalla "Métodos de Cobro" (Cobrar con QR). Reúne los datos y arma las piezas:
 *  - qué método se ve, nombres, titular y número lo decide [MetodosCobroLogica];
 *  - copiar y compartir viven en [CobrosAcciones];
 *  - cómo se ve cada pieza vive en los archivos Cobros*.
 *
 * Con [soloLectura] se ve el método y su QR, pero no se puede editar, cargar ni vincular.
 */
@Composable
fun MetodosDeCobroScreen(
    businessViewModel: BusinessViewModel,
    soloLectura: Boolean = false,
    onBack: (() -> Unit)? = null,
) {
    val paleta = tokePaleta()
    val esOscuro = tokeEsOscuro()
    val context = LocalContext.current
    val puedeEditar = !soloLectura

    val negocioId by businessViewModel.negocioActualId.collectAsState()
    val metodosPorNegocio by MetodoDeCobroStore.metodosPorNegocio.collectAsState()
    val etapaSubidaTexto by businessViewModel.uploadStage.collectAsState()
    val qrCache by businessViewModel.qrCache.collectAsState()

    val metodos = remember(negocioId, metodosPorNegocio) {
        negocioId?.let { metodosPorNegocio[it] } ?: emptyList()
    }
    val etapaSubida = remember(etapaSubidaTexto) {
        etapaSubidaTexto?.let { runCatching { QrUploadStage.valueOf(it) }.getOrNull() }
    }

    var seleccionadoId by remember { mutableStateOf<String?>(null) }
    var uriSeleccionada by remember { mutableStateOf<Uri?>(null) }
    var metodoEnSubida by remember { mutableStateOf<String?>(null) }
    var mostrarCrear by remember { mutableStateOf(false) }
    var metodoEnEdicion by remember { mutableStateOf<MetodoDeCobro?>(null) }
    var metodoAEliminar by remember { mutableStateOf<MetodoDeCobro?>(null) }
    var qrAmpliado by remember { mutableStateOf(false) }
    var cargando by remember { mutableStateOf(false) }
    var errorCarga by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(negocioId) {
        if (negocioId != null) {
            cargando = true
            errorCarga = null
            try {
                businessViewModel.cargarMetodos()
            } catch (e: Exception) {
                errorCarga = com.flabs.cobrate.core.logic.ErrorMapper.amable(e, "No pudimos cargar tus métodos de cobro.")
            }
            cargando = false
        }
    }

    val selectorDeFoto = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) uriSeleccionada = uri else metodoEnSubida = null
    }

    // Flujo de carga del QR: primero se recorta la foto y luego se muestra el progreso.
    val imagen = uriSeleccionada
    if (imagen != null && etapaSubida == null) {
        QrEditorScreen(
            imageUri = imagen,
            onCancel = {
                uriSeleccionada = null
                metodoEnSubida = null
            },
            onConfirmCrop = { recorte ->
                metodoEnSubida?.let { businessViewModel.subirQr(it, recorte) }
                uriSeleccionada = null
                metodoEnSubida = null
            },
        )
        return
    }
    if (etapaSubida != null) {
        QrUploadProgressScreen(currentStage = etapaSubida, errorMessage = null, onRetry = { }, onBack = { })
        return
    }

    val seleccionado = MetodosCobroLogica.seleccionado(metodos, seleccionadoId)
    val bytesQr = seleccionado?.qrUrl?.let { qrCache[it] }

    Scaffold(
        containerColor = paleta.fondo,
        topBar = { CobrosCabecera(paleta = paleta, onBack = onBack) },
    ) { innerPadding ->
        when {
            cargando -> CobrosCargando(paleta, Modifier.padding(innerPadding))
            errorCarga != null -> CobrosError(paleta, errorCarga ?: "", Modifier.padding(innerPadding))
            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                if (seleccionado == null) {
                    item { CobrosVacio(paleta) }
                } else {
                    item {
                        CobrosChips(
                            paleta = paleta,
                            esOscuro = esOscuro,
                            metodos = metodos,
                            seleccionadoId = seleccionado.id,
                            onSeleccionar = { seleccionadoId = it.id },
                        )
                    }
                    item {
                        CobrosTarjeta(
                            paleta = paleta,
                            metodo = seleccionado,
                            qrCache = qrCache,
                            onObtenerQr = { businessViewModel.obtenerImagenQr(it) },
                            onAmpliarQr = { qrAmpliado = true },
                            onCopiar = { CobrosAcciones.copiarNumero(context, it) },
                            onEditar = if (puedeEditar) ({ metodoEnEdicion = seleccionado }) else null,
                            onCargarQr = if (puedeEditar) {
                                {
                                    metodoEnSubida = seleccionado.id
                                    selectorDeFoto.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                                    )
                                }
                            } else {
                                null
                            },
                        )
                    }
                    if (bytesQr != null) {
                        item {
                            CobrosBoton(
                                paleta = paleta,
                                texto = "Compartir QR con cliente",
                                principal = true,
                                icono = TokeIcons.Compartir,
                                onClick = { CobrosAcciones.compartirQr(context, bytesQr) },
                            )
                        }
                    }
                }
                if (puedeEditar) {
                    item {
                        CobrosBoton(
                            paleta = paleta,
                            texto = "+ Vincular otro método de cobro",
                            principal = false,
                            onClick = { mostrarCrear = true },
                        )
                    }
                }
            }
        }
    }

    if (qrAmpliado && seleccionado?.qrUrl != null) {
        QrAmpliadoDialogo(
            qrPath = seleccionado.qrUrl,
            qrCache = qrCache,
            onObtener = { businessViewModel.obtenerImagenQr(it) },
            onCerrar = { qrAmpliado = false },
        )
    }

    if (mostrarCrear && puedeEditar) {
        NuevoMetodoHoja(
            paleta = paleta,
            existentes = metodos,
            onDismiss = { mostrarCrear = false },
            onConfirm = { tipo, dato, titular ->
                businessViewModel.crearMetodo(tipo, dato, titular)
                mostrarCrear = false
            },
        )
    }

    metodoAEliminar?.let { metodo ->
        if (puedeEditar) {
            EliminarMetodoDialogo(
                paleta = paleta,
                metodo = metodo,
                onDismiss = { metodoAEliminar = null },
                onConfirm = {
                    businessViewModel.eliminarMetodo(metodo.id)
                    seleccionadoId = null
                    metodoAEliminar = null
                },
            )
        }
    }

    metodoEnEdicion?.let { metodo ->
        if (puedeEditar) {
            EditarMetodoHoja(
                paleta = paleta,
                metodo = metodo,
                onDismiss = { metodoEnEdicion = null },
                onEliminar = {
                    metodoAEliminar = metodo
                    metodoEnEdicion = null
                },
                onConfirm = { id, dato, titular, oculto ->
                    businessViewModel.actualizarMetodo(id, dato, titular, oculto = oculto)
                    metodoEnEdicion = null
                },
            )
        }
    }
}
