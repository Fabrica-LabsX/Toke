package com.flabs.cobrate.features.business

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.flabs.cobrate.R
import com.flabs.cobrate.components.IconoMetodoPago
import com.flabs.cobrate.core.data.MetodoDeCobroStore
import com.flabs.cobrate.core.data.NegocioStore
import com.flabs.cobrate.core.model.MetodoDeCobro
import com.flabs.cobrate.core.model.TipoMetodoCobro
import kotlinx.coroutines.launch

/**
 * Pantalla "Métodos de Cobro" (Mis QRs) conforme a la especificación de Figma (Nodo 10:3888).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodsScreen(
    businessViewModel: BusinessViewModel,
    soloLectura: Boolean = false,
    onBack: (() -> Unit)? = null
) {
    val negocioId by businessViewModel.negocioActualId.collectAsState()
    val metodosPorNegocio by MetodoDeCobroStore.metodosPorNegocio.collectAsState()
    val uploadStageStr by businessViewModel.uploadStage.collectAsState()
    val qrCache by businessViewModel.qrCache.collectAsState()
    val negocios by NegocioStore.negocios.collectAsState()

    val negocioActual = remember(negocioId, negocios) {
        negocios.find { it.id == negocioId }
    }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val metodos = remember(negocioId, metodosPorNegocio) {
        negocioId?.let { metodosPorNegocio[it] } ?: emptyList()
    }

    val uploadStage = remember(uploadStageStr) {
        uploadStageStr?.let {
            runCatching { QrUploadStage.valueOf(it) }.getOrNull()
        }
    }

    var uriSeleccionada by remember { mutableStateOf<Uri?>(null) }
    var metodoEnUpload by remember { mutableStateOf<String?>(null) }

    var mostrarDialogoCrear by remember { mutableStateOf(false) }
    var metodoEnEdicion by remember { mutableStateOf<MetodoDeCobro?>(null) }
    var busquedaTexto by remember { mutableStateOf("") }

    var estaCargando by remember { mutableStateOf(false) }
    var errorCarga by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(negocioId) {
        if (negocioId != null) {
            estaCargando = true
            errorCarga = null
            try {
                businessViewModel.cargarMetodos()
                estaCargando = false
            } catch (e: Exception) {
                errorCarga = "Error al cargar métodos: ${e.message}"
                estaCargando = false
            }
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            uriSeleccionada = uri
        } else {
            metodoEnUpload = null
        }
    }

    val handleConfirmedCrop: (ByteArray) -> Unit = { finalImageData ->
        val metodoId = metodoEnUpload
        if (metodoId != null) {
            businessViewModel.subirQr(metodoId, finalImageData)
            uriSeleccionada = null
            metodoEnUpload = null
        }
    }

    if (uriSeleccionada != null && uploadStage == null) {
        QrEditorScreen(
            imageUri = uriSeleccionada!!,
            onCancel = {
                uriSeleccionada = null
                metodoEnUpload = null
            },
            onConfirmCrop = handleConfirmedCrop
        )
        return
    }

    if (uploadStage != null) {
        QrUploadProgressScreen(
            currentStage = uploadStage,
            errorMessage = null,
            onRetry = { },
            onBack = { }
        )
        return
    }

     Scaffold(
        topBar = {
            // A1. Cabecera conforme a Figma (Nodo 10:3890)
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (onBack != null) {
                            IconButton(onClick = onBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Volver",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                        }

                        Column {
                            Text(
                                text = "Métodos de Cobro",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            // Chip de tienda (Nodo 10:3898)
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.height(22.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Store,
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = negocioActual?.nombre ?: "Sin negocio",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                estaCargando -> {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }

                errorCarga != null -> {
                    Column(
                        modifier = Modifier.weight(1f).fillMaxWidth().padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = errorCarga!!, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                    }
                }

                metodos.isEmpty() -> {
                    Column(
                        modifier = Modifier.weight(1f).fillMaxWidth().padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "Sin métodos de cobro", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Agrega tu primer método para recibir pagos con QR.", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { mostrarDialogoCrear = true },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Vincular método")
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // A1.2. Carrusel Horizontal Protagonista (Nodo 10:3718)
                        item {
                            val pagerState = rememberPagerState(pageCount = { metodos.size })

                            Column(modifier = Modifier.fillMaxWidth()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(430.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    HorizontalPager(
                                        state = pagerState,
                                        modifier = Modifier.fillMaxWidth(),
                                        contentPadding = PaddingValues(horizontal = 32.dp),
                                        pageSpacing = 12.dp
                                    ) { page ->
                                        val metodo = metodos[page]
                                        MetodoCardCarrusel(
                                            metodo = metodo,
                                            qrCache = qrCache,
                                            soloLectura = soloLectura,
                                            onSubirQr = { m ->
                                                metodoEnUpload = m.id
                                                photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                            },
                                            onEditar = { metodoEnEdicion = it },
                                            onObtenerImagen = { path ->
                                                businessViewModel.obtenerImagenQr(path)
                                            }
                                        )
                                    }

                                    // Flechas laterales si hay más de 1 método
                                    if (metodos.size > 1) {
                                        if (pagerState.currentPage > 0) {
                                            IconButton(
                                                onClick = {
                                                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                                                },
                                                modifier = Modifier
                                                    .align(Alignment.CenterStart)
                                                    .padding(start = 4.dp)
                                                    .background(MaterialTheme.colorScheme.surface, CircleShape)
                                                    .size(36.dp)
                                            ) {
                                                Icon(Icons.Default.ChevronLeft, contentDescription = "Anterior")
                                            }
                                        }

                                        if (pagerState.currentPage < metodos.size - 1) {
                                            IconButton(
                                                onClick = {
                                                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                                                },
                                                modifier = Modifier
                                                    .align(Alignment.CenterEnd)
                                                    .padding(end = 4.dp)
                                                    .background(MaterialTheme.colorScheme.surface, CircleShape)
                                                    .size(36.dp)
                                            ) {
                                                Icon(Icons.Default.ChevronRight, contentDescription = "Siguiente")
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Dots de paginación (Nodo 10:3780)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    repeat(metodos.size) { index ->
                                        val activo = pagerState.currentPage == index
                                        Box(
                                            modifier = Modifier
                                                .padding(horizontal = 3.dp)
                                                .height(6.dp)
                                                .width(if (activo) 20.dp else 6.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (activo) MaterialTheme.colorScheme.primary
                                                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                                )
                                        )
                                    }
                                }
                            }
                        }

                        // A1.4. Sección Inferior con Buscador y Chips (Nodo 10:3790)
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            ) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        // Buscador estilo Pill (Nodo 10:3831)
                                        OutlinedTextField(
                                            value = busquedaTexto,
                                            onValueChange = { busquedaTexto = it },
                                            placeholder = { Text("Buscar método o cuenta...", style = MaterialTheme.typography.bodyMedium) },
                                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(24.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                                focusedBorderColor = MaterialTheme.colorScheme.primary
                                            ),
                                            singleLine = true
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))

                                        // Encabezado Métodos Disponibles (Nodo 10:3792)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "MÉTODOS DISPONIBLES (${metodos.size})",
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            TextButton(onClick = { }) {
                                                Text(
                                                    text = "Ver todos",
                                                    style = MaterialTheme.typography.labelMedium.copy(
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
                                                    ),
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Chips Horizontales de Métodos (Nodo 10:3798)
                                        val metodosBasicos = listOf(
                                            TipoMetodoCobro.YAPE,
                                            TipoMetodoCobro.PLIN,
                                            TipoMetodoCobro.BCP,
                                            TipoMetodoCobro.INTERBANK
                                        )

                                        LazyRow(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            items(metodosBasicos) { tipo ->
                                                val existe = metodos.any { it.tipo == tipo }
                                                Surface(
                                                    shape = RoundedCornerShape(12.dp),
                                                    color = if (existe) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                                    border = BorderStroke(
                                                        1.dp,
                                                        if (existe) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                                    ),
                                                    modifier = Modifier.width(75.dp).height(78.dp)
                                                ) {
                                                    Column(
                                                        horizontalAlignment = Alignment.CenterHorizontally,
                                                        verticalArrangement = Arrangement.Center,
                                                        modifier = Modifier.padding(8.dp)
                                                    ) {
                                                        IconoMetodoPago(tipo = tipo, tamano = 28.dp)
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        Text(
                                                            text = tipo.name.lowercase().replaceFirstChar { it.uppercase() },
                                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.onSurface,
                                                            maxLines = 1
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))

                                        // Botón Vincular Nuevo Método (Nodo 10:3825)
                                        if (!soloLectura) {
                                            OutlinedButton(
                                                onClick = { mostrarDialogoCrear = true },
                                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                                shape = RoundedCornerShape(16.dp),
                                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                                            ) {
                                                Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "+ Vincular nuevo QR / método",
                                                    style = MaterialTheme.typography.labelLarge,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (mostrarDialogoCrear && !soloLectura) {
                CrearMetodoDialog(
                    onDismiss = { mostrarDialogoCrear = false },
                    onConfirm = { tipo, dato, nombre ->
                        businessViewModel.crearMetodo(tipo, dato, nombre)
                        mostrarDialogoCrear = false
                    }
                )
            }

            if (metodoEnEdicion != null && !soloLectura) {
                EditarMetodoDialog(
                    metodo = metodoEnEdicion!!,
                    onDismiss = { metodoEnEdicion = null },
                    onConfirm = { id, dato, nombre, oculto ->
                        businessViewModel.actualizarMetodo(id, dato, nombre, oculto = oculto)
                        metodoEnEdicion = null
                    }
                )
            }
        }
    }
}

/**
 * Tarjeta Central Maximizada del Carrusel conforme a Figma (Nodo 10:3724).
 */
@Composable
private fun MetodoCardCarrusel(
    metodo: MetodoDeCobro,
    qrCache: Map<String, ByteArray>,
    soloLectura: Boolean,
    onSubirQr: (MetodoDeCobro) -> Unit,
    onEditar: (MetodoDeCobro) -> Unit,
    onObtenerImagen: (String) -> Unit
) {
    val cardBg = when (metodo.tipo) {
        TipoMetodoCobro.YAPE -> Color(0xFF742284)
        TipoMetodoCobro.PLIN -> Color(0xFF00B4D8)
        TipoMetodoCobro.BCP -> Color(0xFF002A8F)
        TipoMetodoCobro.INTERBANK -> Color(0xFF05BE50)
        else -> MaterialTheme.colorScheme.primaryContainer
    }

    var mostrarAmpliado by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(422.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Encabezado de la Tarjeta (Nodo 10:3727)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconoMetodoPago(tipo = metodo.tipo, tamano = 36.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = metodo.tipo.name.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (metodo.activo) Color(0xFFDCFCE7) else Color(0xFFFEECEB)
                    ) {
                        Text(
                            text = if (metodo.activo) "ACTIVO" else "INACTIVO",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (metodo.activo) Color(0xFF16A34A) else Color(0xFFFF3B30),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // QR Central Maximizado (Nodo 10:3741)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .size(200.dp)
                        .then(
                            if (metodo.qrUrl != null) Modifier.clickable { mostrarAmpliado = true }
                            else Modifier
                        )
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(12.dp)) {
                        if (metodo.qrUrl != null) {
                            QrImageDisplay(
                                qrPath = metodo.qrUrl,
                                modifier = Modifier.fillMaxSize(),
                                qrCache = qrCache,
                                onObtener = onObtenerImagen
                            )
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .then(if (!soloLectura) Modifier.clickable { onSubirQr(metodo) } else Modifier)
                            ) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Sin QR cargado", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            }
                        }
                    }
                }
            }

            // Subtítulo "Toca para ampliar QR" (Nodo 10:3753)
            Text(
                text = "Toca para ampliar QR",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (metodo.qrUrl != null) Modifier.clickable { mostrarAmpliado = true }
                        else Modifier
                    )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Pie de la Tarjeta: Titular y Edición (Nodo 10:3759)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TITULAR / CELULAR",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    val titular = metodo.nombreDestinatario?.trim()
                    val numero = metodo.datoDeCobro.trim()
                    val textoReflejado = when {
                        !titular.isNullOrBlank() && numero.isNotBlank() -> "$titular • $numero"
                        !titular.isNullOrBlank() -> titular
                        numero.isNotBlank() -> numero
                        else -> "Sin configurar"
                    }
                    Text(
                        text = textoReflejado,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }

                if (!soloLectura) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.clickable { onEditar(metodo) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color.White, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Editar", style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (mostrarAmpliado && metodo.qrUrl != null) {
        Dialog(
            onDismissRequest = { mostrarAmpliado = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { mostrarAmpliado = false },
                color = Color.Black.copy(alpha = 0.92f)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().padding(24.dp)) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(onClick = { mostrarAmpliado = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        QrImageDisplay(
                            qrPath = metodo.qrUrl,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(16.dp)),
                            qrCache = qrCache,
                            onObtener = onObtenerImagen
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CrearMetodoDialog(onDismiss: () -> Unit, onConfirm: (TipoMetodoCobro, String, String) -> Unit) {
    var tipoSeleccionado by remember { mutableStateOf<TipoMetodoCobro?>(null) }
    var datoDeCobro by remember { mutableStateOf("") }
    var nombreDestinatario by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(text = "Nuevo método", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp).border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp)).padding(4.dp)) {
                    items(TipoMetodoCobro.entries) { tipo ->
                        Row(modifier = Modifier.fillMaxWidth().clickable { tipoSeleccionado = tipo }.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = tipoSeleccionado == tipo, onClick = { tipoSeleccionado = tipo })
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(tipo.name)
                        }
                    }
                }
                OutlinedTextField(value = datoDeCobro, onValueChange = { datoDeCobro = it }, label = { Text("Dato de cobro *") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = nombreDestinatario, onValueChange = { nombreDestinatario = it }, label = { Text("Titular (opcional)") }, modifier = Modifier.fillMaxWidth())
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancelar") }
                    Button(onClick = { tipoSeleccionado?.let { onConfirm(it, datoDeCobro, nombreDestinatario) } }, modifier = Modifier.weight(1f), enabled = tipoSeleccionado != null && datoDeCobro.isNotBlank()) { Text("Crear") }
                }
            }
        }
    }
}

@Composable
private fun EditarMetodoDialog(metodo: MetodoDeCobro, onDismiss: () -> Unit, onConfirm: (String, String, String, Boolean) -> Unit) {
    var datoDeCobro by remember { mutableStateOf(metodo.datoDeCobro) }
    var nombreDestinatario by remember { mutableStateOf(metodo.nombreDestinatario ?: "") }
    var oculto by remember { mutableStateOf(metodo.oculto) }

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(text = "Editar método", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                OutlinedTextField(value = datoDeCobro, onValueChange = { datoDeCobro = it }, label = { Text("Dato de cobro") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = nombreDestinatario, onValueChange = { nombreDestinatario = it }, label = { Text("Titular") }, modifier = Modifier.fillMaxWidth())
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = oculto, onCheckedChange = { oculto = it })
                    Text("Ocultar método")
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancelar") }
                    Button(onClick = { onConfirm(metodo.id, datoDeCobro, nombreDestinatario, oculto) }, modifier = Modifier.weight(1f)) { Text("Guardar") }
                }
            }
        }
    }
}

@Composable
private fun QrImageDisplay(qrPath: String, modifier: Modifier = Modifier, qrCache: Map<String, ByteArray>, onObtener: (String) -> Unit) {
    val context = LocalContext.current
    val qrBytes = qrCache[qrPath]

    LaunchedEffect(qrPath) {
        if (qrBytes == null) onObtener(qrPath)
    }

    Box(modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)).clip(RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
        if (qrBytes == null) {
            CircularProgressIndicator(modifier = Modifier.size(40.dp))
        } else {
            AsyncImage(model = ImageRequest.Builder(context).data(qrBytes).crossfade(true).build(), contentDescription = "QR", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
        }
    }
}
