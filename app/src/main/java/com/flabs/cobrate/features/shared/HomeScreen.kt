package com.flabs.cobrate.features.shared

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flabs.cobrate.MainActivity
import com.flabs.cobrate.R
import com.flabs.cobrate.components.CambiarNegocioBottomSheet
import com.flabs.cobrate.components.ConfirmarCierreSesionDialog
import com.flabs.cobrate.components.OperacionCard
import com.flabs.cobrate.components.PantallaAccesoPausado
import com.flabs.cobrate.components.PantallaEsperandoAceptacion
import com.flabs.cobrate.components.PantallaSolicitudRechazada
import com.flabs.cobrate.components.rememberSwipeUpRefreshConnection
import com.flabs.cobrate.features.onboarding.EmpezarScreen
import com.flabs.cobrate.core.data.*
import com.flabs.cobrate.core.logic.AccesoNegocio.EstadoAcceso
import com.flabs.cobrate.core.logic.FeedbackManager
import com.flabs.cobrate.core.logic.LogSeguro
import com.flabs.cobrate.core.logic.RolEfectivo
import com.flabs.cobrate.core.model.Alerta
import com.flabs.cobrate.core.model.EstadoOperacion
import com.flabs.cobrate.core.model.EstadoRelacion
import com.flabs.cobrate.core.model.Negocio
import com.flabs.cobrate.core.model.Operacion
import com.flabs.cobrate.core.model.RolCuentaNegocio
import com.flabs.cobrate.features.auth.AuthViewModel
import com.flabs.cobrate.features.business.BusinessViewModel
import com.flabs.cobrate.features.business.NegocioViewModel
import com.flabs.cobrate.features.business.ResumenDelDia
import com.flabs.cobrate.features.worker.WorkerViewModel
import com.flabs.cobrate.ui.theme.TokeBorder
import com.flabs.cobrate.ui.theme.TokeSurfaceMuted
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    negocio: Negocio?,
    businessViewModel: BusinessViewModel,
    negocioViewModel: NegocioViewModel,
    authViewModel: AuthViewModel,
    workerViewModel: WorkerViewModel? = null,
    onVerTodas: () -> Unit,
    onMisQRs: () -> Unit,
    onAnadirTrabajador: () -> Unit,
    onReclamarPagos: () -> Unit,
    onCrearNegocio: () -> Unit,
    onUnirmeNegocio: () -> Unit,
    onSalirDelNegocio: (() -> Unit)? = null,
    onRefrescarTab: (() -> Unit)? = null,
    onSeleccionarOperacion: ((String) -> Unit)? = null,
) {
    HomeScreenContent(
        negocio = negocio,
        businessViewModel = businessViewModel,
        negocioViewModel = negocioViewModel,
        authViewModel = authViewModel,
        workerViewModel = workerViewModel,
        onVerTodas = onVerTodas,
        onMisQRs = onMisQRs,
        onAnadirTrabajador = onAnadirTrabajador,
        onReclamarPagos = onReclamarPagos,
        onCrearNegocio = onCrearNegocio,
        onUnirmeNegocio = onUnirmeNegocio,
        onSalirDelNegocio = onSalirDelNegocio,
        onRefrescarTab = onRefrescarTab,
        onSeleccionarOperacion = onSeleccionarOperacion,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreenContent(
    negocio: Negocio?,
    businessViewModel: BusinessViewModel,
    negocioViewModel: NegocioViewModel,
    authViewModel: AuthViewModel,
    workerViewModel: WorkerViewModel?,
    onVerTodas: () -> Unit,
    onMisQRs: () -> Unit,
    onAnadirTrabajador: () -> Unit,
    onReclamarPagos: () -> Unit,
    onCrearNegocio: () -> Unit,
    onUnirmeNegocio: () -> Unit,
    onSalirDelNegocio: (() -> Unit)? = null,
    onRefrescarTab: (() -> Unit)? = null,
    onSeleccionarOperacion: ((String) -> Unit)? = null,
) {
    val resumen by businessViewModel.resumenDelDia.collectAsState()
    val operaciones by InMemoryOperationStore.operaciones.collectAsState()
    val alertas by AlertaStore.alertas.collectAsState()
    val sesionActual by CuentaStore.sesionActual.collectAsState()
    val trabajadoresReales by businessViewModel.trabajadoresReales.collectAsState()

    val rolActivo by negocioViewModel.rolActivo.collectAsState()
    val rolEfectivo by negocioViewModel.rolEfectivo.collectAsState()
    val estadoAcceso by negocioViewModel.estadoAcceso.collectAsState()
    val misNegocios by negocioViewModel.misNegocios.collectAsState()
    val misRelaciones by negocioViewModel.misRelaciones.collectAsState()

    val estadoSync by CuentaStore.estadoSincronizacion.collectAsState()
    val negocioStoreListo by NegocioStore.listo.collectAsState()
    val relacionStoreListo by RelacionCuentaNegocioStore.listo.collectAsState()

    val scope = rememberCoroutineScope()
    var mostrarSelectorNegocio by remember { mutableStateOf(false) }
    var mostrarSheetReclamar by remember { mutableStateOf(false) }
    var mostrarConfirmarCierre by remember { mutableStateOf(false) }

    val estaCargandoGlobal = rolEfectivo == RolEfectivo.CARGANDO ||
            !negocioStoreListo ||
            !relacionStoreListo ||
            estadoSync == EstadoSincronizacionRemota.SINCRONIZANDO

    val operacionesRecientes = remember(operaciones) {
        operaciones.sortedByDescending { it.fechaHora }.take(5)
    }

    val uid = sesionActual?.id
    val esAutorizadoNegocioActivo = remember(negocio, uid, misRelaciones) {
        if (negocio == null || uid == null) false
        else {
            val esDueno = negocio.dueñoCuentaId == uid
            val relacion = misRelaciones.find { it.cuentaId == uid && it.negocioId == negocio.id }
            val esAdminActivo = relacion?.rol == RolCuentaNegocio.ADMINISTRADOR && relacion.estado == EstadoRelacion.ACTIVA
            esDueno || esAdminActivo
        }
    }

    val esTrabajador = rolActivo == RolCuentaNegocio.TRABAJADOR && negocio?.dueñoCuentaId != uid
    val hayPendientes = resumen.operacionesPendientes > 0

    val pendientes = remember(operaciones, negocio) {
        if (negocio == null) emptyList()
        else operaciones.filter { it.negocioId == negocio.id && it.estado == EstadoOperacion.SIN_ASIGNAR }
    }

    val resolverNombreAsignado = remember(esTrabajador, sesionActual, trabajadoresReales) {
        { op: Operacion ->
            if (op.estado == EstadoOperacion.SIN_ASIGNAR) {
                null
            } else if (esTrabajador) {
                if (op.trabajadorAsignadoId != null && op.trabajadorAsignadoId == sesionActual?.id) {
                    sesionActual?.username
                } else {
                    null
                }
            } else {
                trabajadoresReales.find { it.cuentaId == op.trabajadorAsignadoId }?.username
            }
        }
    }

    val alertasNegocioActivo = remember(alertas, negocio, uid) {
        if (negocio == null || uid == null) emptyList()
        else alertas.filter { it.negocioId == negocio.id && it.cuentaId == uid }
    }

    val onAccionReclamar: () -> Unit = {
        if (esTrabajador) {
            when {
                pendientes.size == 1 -> workerViewModel?.reclamarOperacion(pendientes.first().id)
                pendientes.size >= 2 -> mostrarSheetReclamar = true
                else -> { /* 0 pendientes */ }
            }
        } else {
            onReclamarPagos()
        }
    }

    val swipeConnection = rememberSwipeUpRefreshConnection(enabled = onRefrescarTab != null) {
        onRefrescarTab?.invoke()
    }

    Scaffold(
        topBar = {
            if (negocio != null && !estaCargandoGlobal && estadoAcceso == EstadoAcceso.NORMAL) {
                HomeTopBarCard(
                    negocio = negocio,
                    onCambiarNegocio = { mostrarSelectorNegocio = true },
                )
            }
        },
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .nestedScroll(swipeConnection)
        ) {
            if (estaCargandoGlobal) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Sincronizando negocios...", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else if (negocio == null && misNegocios.isEmpty()) {
                if (estadoSync == EstadoSincronizacionRemota.ERROR) {
                    LogSeguro.w("HomeScreen", "[UI] Mostrando pantalla de error de red/consulta")
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudOff,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No se pudo sincronizar la información",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Ocurrió un problema de red o consulta. Revisa tu conexión e intenta nuevamente.",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = {
                                    val idActual = sesionActual?.id
                                    if (idActual != null) {
                                        scope.launch {
                                            CuentaStore.sincronizarDatos(idActual)
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Text("Reintentar")
                            }
                        }
                    }
                } else if (estadoSync == EstadoSincronizacionRemota.EXITO) {
                    EmpezarScreen(
                        onCrearNegocio = onCrearNegocio,
                        onValidarCodigo = { _ ->
                            onUnirmeNegocio()
                        },
                        onCerrarSesion = { mostrarConfirmarCierre = true }
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Comprobando información...", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            } else if (negocio == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                        Text("Tienes ${misNegocios.size} negocios vinculados.", style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
                        Text("Por favor, selecciona uno para continuar.", style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { mostrarSelectorNegocio = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                        ) {
                            Text("Elegir negocio", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            } else when (estadoAcceso) {
                EstadoAcceso.PAUSADO -> {
                    PantallaAccesoPausado(
                        negocioNombre = negocio.nombre,
                        onVerificarEstado = {
                            val negId = negocio.id
                            val uid = sesionActual?.id ?: return@PantallaAccesoPausado
                            scope.launch {
                                FeedbackManager.setLoading(true, "Verificando acceso...")
                                RelacionCuentaNegocioRepository().relacionesDe(uid).onSuccess { relaciones ->
                                    FeedbackManager.setLoading(false)
                                    RelacionCuentaNegocioStore.sincronizar(uid, relaciones)
                                    val relActual = relaciones.find { it.negocioId == negId }
                                    if (relActual?.estado == EstadoRelacion.ACTIVA) {
                                        negocioViewModel.seleccionarNegocio(negId)
                                        FeedbackManager.showSuccess("¡Acceso reactivado por el dueño!")
                                    } else {
                                        FeedbackManager.showSuccess("Tu acceso a «${negocio.nombre}» sigue pausado por el dueño.")
                                    }
                                }.onFailure { err ->
                                    FeedbackManager.setLoading(false)
                                    FeedbackManager.showErrorMessage(err.message ?: "Error al verificar estado con el servidor")
                                }
                            }
                        },
                        onCrearNegocio = onCrearNegocio,
                        onCambiarNegocio = { mostrarSelectorNegocio = true },
                        onCerrarSesion = { mostrarConfirmarCierre = true },
                        onSalirDelNegocio = onSalirDelNegocio,
                    )
                }
                EstadoAcceso.SOLICITADO -> {
                    val relSolicitada = misRelaciones.find { it.negocioId == negocio.id }
                    PantallaEsperandoAceptacion(
                        negocioNombre = negocio.nombre,
                        codigoUsado = relSolicitada?.codigoUsado,
                        onCrearNegocio = onCrearNegocio,
                        onCerrarSesion = { mostrarConfirmarCierre = true },
                        onCancelarSolicitud = {
                            val negId = negocio.id
                            val uid = sesionActual?.id ?: return@PantallaEsperandoAceptacion
                            scope.launch {
                                RelacionCuentaNegocioRepository().salirDelNegocio(negId).onSuccess { res ->
                                    RelacionCuentaNegocioStore.quitarRelacion(negId, uid)
                                    negocioViewModel.seleccionarNegocio(null)
                                    FeedbackManager.showSuccess(res.mensaje)
                                }.onFailure { err ->
                                    FeedbackManager.showErrorMessage(err.message ?: "Error al cancelar solicitud")
                                }
                            }
                        },
                    )
                }
                EstadoAcceso.RECHAZADO -> {
                    PantallaSolicitudRechazada(
                        negocioNombre = negocio.nombre,
                        onEntendido = {
                            val negId = negocio.id
                            val uid = sesionActual?.id ?: return@PantallaSolicitudRechazada
                            scope.launch {
                                RelacionCuentaNegocioRepository().salirDelNegocio(negId).onSuccess {
                                    RelacionCuentaNegocioStore.quitarRelacion(negId, uid)
                                    negocioViewModel.seleccionarNegocio(null)
                                }.onFailure {
                                    RelacionCuentaNegocioStore.quitarRelacion(negId, uid)
                                    negocioViewModel.seleccionarNegocio(null)
                                }
                            }
                        },
                    )
                }
                EstadoAcceso.NORMAL -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 12.dp),
                    ) {
                        // 1. Resumen Principal del Día (Hero Balance Card)
                        item {
                            ResumenCard(
                                resumen = resumen,
                                esTrabajador = esTrabajador,
                            )
                        }

                        // 3. Acciones Rápidas (Grid 2 columnas: Mis QRs / Mi equipo)
                        item {
                            QuickActionsRow(
                                onMisQRs = onMisQRs,
                                onAnadirTrabajador = onAnadirTrabajador,
                            )
                        }

                        // 4. Banner de Pagos Pendientes (si existen pendientes)
                        if (hayPendientes) {
                            item {
                                BannerPagosPendientes(
                                    cantidadPendientes = resumen.operacionesPendientes,
                                    pendientes = pendientes,
                                    onClick = onAccionReclamar,
                                )
                            }
                        }

                        // 5. Alertas Existentes (Conservado según Decisión 10)
                        if (esAutorizadoNegocioActivo && alertasNegocioActivo.isNotEmpty()) {
                            item {
                                AlertasCard(alertasNegocioActivo.first())
                            }
                        }

                        // 6. Operaciones Recientes
                        item {
                            RecentOpsSection(
                                operaciones = operacionesRecientes,
                                onVerTodas = onVerTodas,
                                resolverNombreAsignado = resolverNombreAsignado,
                                onSeleccionarOperacion = onSeleccionarOperacion,
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }
        }

        if (mostrarConfirmarCierre) {
            ConfirmarCierreSesionDialog(
                onConfirmar = {
                    mostrarConfirmarCierre = false
                    authViewModel.cerrarSesion()
                },
                onCancelar = { mostrarConfirmarCierre = false },
            )
        }

        if (mostrarSelectorNegocio) {
            CambiarNegocioBottomSheet(
                negocioViewModel = negocioViewModel,
                onDismiss = { mostrarSelectorNegocio = false },
                onCrearNegocio = {
                    mostrarSelectorNegocio = false
                    onCrearNegocio()
                },
                onUnirmeNegocio = {
                    mostrarSelectorNegocio = false
                    onUnirmeNegocio()
                }
            )
        }

        if (mostrarSheetReclamar && esTrabajador) {
            ModalBottomSheet(
                onDismissRequest = { mostrarSheetReclamar = false },
                containerColor = MaterialTheme.colorScheme.surface,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = "Elige el pago a reclamar",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 16.dp),
                    )

                    if (pendientes.isEmpty()) {
                        Text(
                            text = "No hay pagos pendientes para reclamar.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 16.dp),
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 24.dp),
                        ) {
                            items(pendientes) { op ->
                                ItemReclamarSheet(
                                    operacion = op,
                                    onReclamar = {
                                        workerViewModel?.reclamarOperacion(op.id)
                                        mostrarSheetReclamar = false
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ItemReclamarSheet(
    operacion: Operacion,
    onReclamar: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "S/ %.2f".format(operacion.monto),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(2.dp))
                val metodoNombre = operacion.metodoPago?.name ?: "Pago"
                Text(
                    text = "$metodoNombre • ${formatearHoraLocal(operacion.fechaHora)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Button(
                onClick = onReclamar,
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("Reclamar", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

private fun formatearHoraLocal(fechaHora: Long): String {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = fechaHora
    }
    val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return formatter.format(calendar.time)
}

/**
 * TopBar conforme a Figma (Nodo 7:2891 / Decisión 5 & 6):
 * Sin botón hamburguesa lateral. Selector con ChevronDown.
 */
@Composable
private fun HomeTopBarCard(
    negocio: Negocio?,
    onCambiarNegocio: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onCambiarNegocio)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = if (negocio != null) "Hoy en ${negocio.nombre}" else "Sin Negocio",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Cambiar negocio",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

/**
 * Hero Balance Card conforme a Figma (Nodo 7:2905 / Decisión 7 & 9):
 * 358x146, radio 16, borde 2px. Pills con puntos sólidos `#16A34A` y `#D97706`.
 */
@Composable
private fun ResumenCard(resumen: ResumenDelDia, esTrabajador: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(22.dp)) {
            Text(
                text = if (esTrabajador) "RESUMEN DE HOY" else "TOTAL DE HOY",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(6.dp))

            if (esTrabajador) {
                // Conservar vista diferenciada para trabajador (Decisión 9)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = resumen.pagosRecibidos.toString(),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Pagos recibidos",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }

                    VerticalDivider(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(vertical = 4.dp),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = resumen.operacionesPendientes.toString(),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = if (resumen.operacionesPendientes > 0) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Pendientes",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            } else {
                Text(
                    text = String.format(Locale.US, "S/ %,.2f", resumen.totalDelDia),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Pills estilo Figma: punto verde / punto naranja sin íconos vectoriales (Decisión 7)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Pill Operaciones (verde #16A34A)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = TokeSurfaceMuted,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 13.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF16A34A)),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${resumen.numeroDeOperaciones} operaciones",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }

                    // Pill Pendientes (naranja #D97706)
                    if (resumen.operacionesPendientes > 0) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 13.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFD97706)),
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${resumen.operacionesPendientes} pendientes",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionsRow(
    onMisQRs: () -> Unit,
    onAnadirTrabajador: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        QuickActionButton(
            modifier = Modifier.weight(1f),
            title = stringResource(R.string.nombre_mis_qr),
            subtitle = "Gestiona códigos",
            icon = Icons.Default.QrCode,
            onClick = onMisQRs,
        )
        QuickActionButton(
            modifier = Modifier.weight(1f),
            title = stringResource(R.string.nombre_mi_equipo),
            subtitle = "Invita a tu equipo",
            icon = Icons.Default.People,
            onClick = onAnadirTrabajador,
        )
    }
}

/**
 * Botón Bubbly conforme a Figma (Nodo 7:2922 / 7:2941):
 * Borde 2px, icono contenedor 40x40 fondo `#ECFDF5` con borde `#A7F3D0`.
 */
@Composable
private fun QuickActionButton(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1.0f,
        animationSpec = tween(durationMillis = 100),
        label = "quick_action_scale",
    )

    Card(
        onClick = onClick,
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                    modifier = Modifier.size(40.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

/**
 * Banner de pagos pendientes (Decisión 4):
 * Mantiene la lógica dinámica de la app.
 */
@Composable
private fun BannerPagosPendientes(
    cantidadPendientes: Int,
    pendientes: List<Operacion>,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1.0f,
        animationSpec = tween(durationMillis = 100),
        label = "banner_scale",
    )

    val primeraOp = pendientes.firstOrNull()

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)),
                    modifier = Modifier.size(36.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Tienes $cantidadPendientes pago(s) pendiente(s)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.tertiary,
                        ) {
                            Text(
                                text = cantidadPendientes.toString(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                ),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (primeraOp != null) {
                            val metodo = primeraOp.metodoPago?.name ?: "Pago"
                            "Monto: S/ %.2f • $metodo".format(Locale.US, primeraOp.monto)
                        } else {
                            "Toca para reclamar y asignar a tu cuenta"
                        },
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Reclamar",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.tertiary,
                )
                Spacer(modifier = Modifier.width(2.dp))
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Reclamar",
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@Composable
private fun AlertasCard(alerta: Alerta) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f)),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                modifier = Modifier.size(36.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = "Alerta",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = alerta.mensaje,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
        }
    }
}

/**
 * Sección de Operaciones Recientes conforme a Figma (Nodo 7:2981 / Decisión 1):
 * Título "Operaciones recientes". Enlace "Ver todas ›".
 */
@Composable
private fun RecentOpsSection(
    operaciones: List<Operacion>,
    onVerTodas: () -> Unit,
    resolverNombreAsignado: (Operacion) -> String? = { null },
    onSeleccionarOperacion: ((String) -> Unit)? = null,
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.nombre_cobros_recientes),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )
            TextButton(onClick = onVerTodas) {
                Text(
                    text = "Ver todas ›",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = Color(0xFF15803D),
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (operaciones.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
            ) {
                Text(
                    text = "No hay operaciones registradas hoy.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(20.dp),
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                operaciones.forEach { operacion ->
                    OperacionCard(
                        operacion = operacion,
                        asignadoPorNombre = resolverNombreAsignado(operacion),
                        onClick = { onSeleccionarOperacion?.invoke(operacion.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun NoBusinessHub(
    onCrearNegocio: () -> Unit,
    onUnirmeNegocio: () -> Unit,
    onCerrarSesion: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(96.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Storefront,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "¡Bienvenido a TOKE!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Para comenzar a gestionar cobros, crea un nuevo negocio o únete a uno existente con un código de invitación.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onCrearNegocio,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
        ) {
            Text("Crear un negocio", style = MaterialTheme.typography.labelLarge)
        }

        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = onUnirmeNegocio,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
        ) {
            Text("Tengo un código de invitación", style = MaterialTheme.typography.labelLarge)
        }

        Spacer(modifier = Modifier.height(24.dp))
        TextButton(onClick = onCerrarSesion) {
            Text("Cerrar sesión", style = MaterialTheme.typography.labelMedium)
        }
    }
}
