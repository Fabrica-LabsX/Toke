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
import com.flabs.cobrate.components.colorBilletera
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
import com.flabs.cobrate.ui.theme.PlusJakartaSans
import com.flabs.cobrate.core.model.TipoMetodoCobro
import com.flabs.cobrate.core.data.PreferenciasAppStore
import com.flabs.cobrate.ui.theme.ThemeMode
import com.flabs.cobrate.ui.theme.tokeEsOscuro
import com.flabs.cobrate.ui.theme.tokePaleta
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


private class HomePalette(
    val Fondo: Color,
    val TarjetaFondo: Color,
    val TarjetaBorde: Color,
    val TileIconoFondo: Color,
    val TextoPrincipal: Color,
    val TextoSecundario: Color,
    val EnlaceVerTodas: Color,
    val Chip1Fondo: Color,
    val Chip1Dot: Color,
    val Chip1Texto: Color,
    val Chip2Fondo: Color,
    val Chip2Texto: Color,
    val OfflineFondo: Color,
    val OfflineBorde: Color,
    val IconoTile: Color,
    val IconoCabecera: Color
)

/** Colores del Home: salen de la paleta única de la app; solo los iconos tiñen distinto en oscuro. */
@Composable
private fun homeColors(): HomePalette {
    val p = tokePaleta()
    val oscuro = tokeEsOscuro()
    return HomePalette(
        Fondo = p.fondo,
        TarjetaFondo = p.tarjeta,
        TarjetaBorde = p.borde,
        TileIconoFondo = p.tileFondo,
        TextoPrincipal = p.textoPrincipal,
        TextoSecundario = p.textoSecundario,
        EnlaceVerTodas = p.enlaceVerde,
        Chip1Fondo = p.tileFondo,
        Chip1Dot = p.textoPrincipal,
        Chip1Texto = p.textoPrincipal,
        Chip2Fondo = p.ambar,
        Chip2Texto = p.ambarTexto,
        OfflineFondo = p.rojoFondo,
        OfflineBorde = p.rojoBorde,
        IconoTile = if (oscuro) Color.White else Color.Unspecified,
        IconoCabecera = if (oscuro) Color.White else Color.Unspecified
    )
}


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
    onAbrirPermisos: () -> Unit = {},
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
        onAbrirPermisos = onAbrirPermisos,
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
    onAbrirPermisos: () -> Unit = {},
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
    var mostrarOffline by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(3000)
        mostrarOffline = true
    }

    val disponible by com.flabs.cobrate.core.logic.NetworkMonitor.disponible.collectAsState()

    val estaCargandoGlobal = rolEfectivo == RolEfectivo.CARGANDO ||
            !negocioStoreListo ||
            !relacionStoreListo ||
            (estadoSync == EstadoSincronizacionRemota.SINCRONIZANDO && negocio == null && misNegocios.isEmpty())

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


    Scaffold(
        topBar = {
            if (negocio != null && !estaCargandoGlobal && estadoAcceso == EstadoAcceso.NORMAL) {
                HomeTopBarCard(
                    negocio = negocio,
                    onCambiarNegocio = { mostrarSelectorNegocio = true },
                    mostrarBadgeOffline = mostrarOffline && !disponible,
                    mostrarBadgeOnline = disponible,
                    onAbrirPermisos = onAbrirPermisos
                )
            }
        },
        modifier = Modifier.fillMaxSize(),
        containerColor = homeColors().Fondo,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
        ) {
            if (estaCargandoGlobal) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
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
                        }
                    }
                }
            } else if (negocio == null) {
                val relSolicitada = misRelaciones.find { it.estado == EstadoRelacion.SOLICITADA }
                val relPausada = misRelaciones.find { it.estado == EstadoRelacion.PAUSADA }
                val relRechazada = misRelaciones.find { it.estado == EstadoRelacion.RECHAZADA }

                when {
                    relSolicitada != null -> {
                        val negNombre = misNegocios.find { it.id == relSolicitada.negocioId }?.nombre ?: "este negocio"
                        PantallaEsperandoAceptacion(
                            negocioNombre = negNombre,
                            codigoUsado = relSolicitada.codigoUsado,
                            onCrearNegocio = onCrearNegocio,
                            onCerrarSesion = { mostrarConfirmarCierre = true },
                            onCancelarSolicitud = {
                                val negId = relSolicitada.negocioId
                                val uidActual = sesionActual?.id ?: return@PantallaEsperandoAceptacion
                                scope.launch {
                                    RelacionCuentaNegocioRepository().salirDelNegocio(negId).onSuccess { res ->
                                        RelacionCuentaNegocioStore.quitarRelacion(negId, uidActual)
                                        negocioViewModel.seleccionarNegocio(null)
                                        FeedbackManager.showSuccess(res.mensaje)
                                    }.onFailure { err ->
                                        FeedbackManager.showErrorMessage(err.message ?: "Error al cancelar solicitud")
                                    }
                                }
                            }
                        )
                    }
                    relPausada != null -> {
                        val negNombre = misNegocios.find { it.id == relPausada.negocioId }?.nombre ?: "este negocio"
                        PantallaAccesoPausado(
                            negocioNombre = negNombre,
                            onVerificarEstado = {
                                val negId = relPausada.negocioId
                                val uidActual = sesionActual?.id ?: return@PantallaAccesoPausado
                                scope.launch {
                                    FeedbackManager.setLoading(true, "Verificando acceso...")
                                    RelacionCuentaNegocioRepository().relacionesDe(uidActual).onSuccess { relaciones ->
                                        FeedbackManager.setLoading(false)
                                        RelacionCuentaNegocioStore.sincronizar(uidActual, relaciones)
                                        val relActual = relaciones.find { it.negocioId == negId }
                                        if (relActual?.estado == EstadoRelacion.ACTIVA) {
                                            negocioViewModel.seleccionarNegocio(negId)
                                            FeedbackManager.showSuccess("¡Acceso reactivado por el dueño!")
                                        } else {
                                            FeedbackManager.showSuccess("Tu acceso a «$negNombre» sigue pausado por el dueño.")
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
                            onSalirDelNegocio = onSalirDelNegocio
                        )
                    }
                    relRechazada != null -> {
                        val negNombre = misNegocios.find { it.id == relRechazada.negocioId }?.nombre ?: "este negocio"
                        PantallaSolicitudRechazada(
                            negocioNombre = negNombre,
                            onEntendido = {
                                val negId = relRechazada.negocioId
                                val uidActual = sesionActual?.id ?: return@PantallaSolicitudRechazada
                                scope.launch {
                                    RelacionCuentaNegocioRepository().salirDelNegocio(negId).onSuccess {
                                        RelacionCuentaNegocioStore.quitarRelacion(negId, uidActual)
                                        negocioViewModel.seleccionarNegocio(null)
                                    }.onFailure {
                                        RelacionCuentaNegocioStore.quitarRelacion(negId, uidActual)
                                        negocioViewModel.seleccionarNegocio(null)
                                    }
                                }
                            }
                        )
                    }
                    else -> {
                        EmpezarScreen(
                            onCrearNegocio = onCrearNegocio,
                            onValidarCodigo = { _ -> onUnirmeNegocio() },
                            onCerrarSesion = { mostrarConfirmarCierre = true }
                        )
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

@Composable
private fun HomeTopBarCard(
    negocio: Negocio?,
    onCambiarNegocio: () -> Unit,
    mostrarBadgeOffline: Boolean = false,
    mostrarBadgeOnline: Boolean = false,
    onAbrirPermisos: () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onCambiarNegocio)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = negocio?.nombre ?: "Sin negocio",
                    style = androidx.compose.ui.text.TextStyle(
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        letterSpacing = (-0.45).sp
                    ),
                    color = homeColors().TextoPrincipal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    painter = androidx.compose.ui.res.painterResource(id = com.flabs.cobrate.components.TokeIcons.ChevronDown),
                    contentDescription = "Cambiar negocio",
                    tint = homeColors().IconoCabecera,
                    modifier = Modifier.size(16.dp),
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = stringResource(R.string.home_resumen_hoy),
                style = androidx.compose.ui.text.TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                ),
                color = homeColors().TextoSecundario,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        com.flabs.cobrate.components.BadgeSincronizacion(
            operativo = com.flabs.cobrate.features.permisos.rememberEstadoServicio() ==
                com.flabs.cobrate.features.permisos.EstadoServicio.OPERATIVO,
            onClick = onAbrirPermisos,
        )
        Spacer(modifier = Modifier.width(8.dp))
        if (mostrarBadgeOnline && !mostrarBadgeOffline) {
            com.flabs.cobrate.components.BadgeConexionOnline()
        }
        if (mostrarBadgeOffline) {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = RoundedCornerShape(10.dp),
                color = homeColors().OfflineFondo,
                border = BorderStroke(1.dp, homeColors().OfflineBorde)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = androidx.compose.ui.res.painterResource(id = com.flabs.cobrate.components.TokeIcons.Offline),
                        contentDescription = "Sin conexión a internet",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ResumenCard(resumen: ResumenDelDia, esTrabajador: Boolean) {
    if (esTrabajador) return
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = homeColors().TarjetaFondo),
        border = BorderStroke(1.dp, homeColors().TarjetaBorde),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(17.dp)) {
            Text(
                text = stringResource(R.string.home_cobros_hoy),
                style = androidx.compose.ui.text.TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                ),
                color = homeColors().TextoSecundario,
            )
            Spacer(modifier = Modifier.height(2.5.dp))
            Text(
                text = String.format(Locale.US, "S/ %,.2f", resumen.totalDelDia),
                style = androidx.compose.ui.text.TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 34.sp,
                    letterSpacing = (-0.85).sp
                ),
                color = homeColors().TextoPrincipal,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Chip 1
                Surface(
                    shape = RoundedCornerShape(9999.dp),
                    color = homeColors().Chip1Fondo,
                    border = BorderStroke(1.dp, homeColors().TarjetaBorde)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 13.dp, vertical = 5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(homeColors().Chip1Dot, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${resumen.numeroDeOperaciones} cobros",
                            style = androidx.compose.ui.text.TextStyle(
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                            ),
                            color = homeColors().Chip1Texto,
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                // Chip 2
                Surface(
                    shape = RoundedCornerShape(9999.dp),
                    color = homeColors().Chip2Fondo,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            painter = androidx.compose.ui.res.painterResource(id = com.flabs.cobrate.components.TokeIcons.Clock),
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${resumen.operacionesPendientes} pendientes",
                            style = androidx.compose.ui.text.TextStyle(
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                            ),
                            color = homeColors().Chip2Texto,
                        )
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
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Tile 1
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = homeColors().TarjetaFondo),
            border = BorderStroke(1.dp, homeColors().TarjetaBorde),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            onClick = onMisQRs
        ) {
            Column(modifier = Modifier.padding(15.dp)) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = homeColors().TileIconoFondo,
                    border = BorderStroke(1.dp, homeColors().TarjetaBorde),
                    modifier = Modifier.size(42.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = androidx.compose.ui.res.painterResource(id = com.flabs.cobrate.components.TokeIcons.Qr),
                            contentDescription = null,
                            tint = homeColors().IconoTile,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = stringResource(R.string.nombre_mis_qr),
                    style = androidx.compose.ui.text.TextStyle(
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                    ),
                    color = homeColors().TextoPrincipal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.home_yape_plin_bancos),
                    style = androidx.compose.ui.text.TextStyle(
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp,
                        lineHeight = 13.75.sp
                    ),
                    color = homeColors().TextoSecundario,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Tile 2
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = homeColors().TarjetaFondo),
            border = BorderStroke(1.dp, homeColors().TarjetaBorde),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            onClick = onAnadirTrabajador
        ) {
            Column(modifier = Modifier.padding(15.dp)) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = homeColors().TileIconoFondo,
                    border = BorderStroke(1.dp, homeColors().TarjetaBorde),
                    modifier = Modifier.size(42.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = androidx.compose.ui.res.painterResource(id = com.flabs.cobrate.components.TokeIcons.PersonAdd),
                            contentDescription = null,
                            tint = homeColors().IconoTile,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = stringResource(R.string.home_mi_personal),
                    style = androidx.compose.ui.text.TextStyle(
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                    ),
                    color = homeColors().TextoPrincipal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.home_ver_colaboradores),
                    style = androidx.compose.ui.text.TextStyle(
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp,
                        lineHeight = 13.75.sp
                    ),
                    color = homeColors().TextoSecundario,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun BannerPagosPendientes(
    cantidadPendientes: Int,
    pendientes: List<Operacion>,
    onClick: () -> Unit
) {
    val sumaPendientes = pendientes.sumOf { it.monto }

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = homeColors().TarjetaFondo),
        border = BorderStroke(1.dp, homeColors().TarjetaBorde),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {
                Surface(
                    shape = RoundedCornerShape(9999.dp),
                    color = homeColors().Chip2Fondo,
                    modifier = Modifier.size(28.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = cantidadPendientes.toString(),
                            style = androidx.compose.ui.text.TextStyle(
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                            ),
                            color = homeColors().Chip2Texto,
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = stringResource(R.string.home_pagos_por_asignar),
                        style = androidx.compose.ui.text.TextStyle(
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                        ),
                        color = homeColors().TextoPrincipal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.home_hay_cobros_esperando),
                        style = androidx.compose.ui.text.TextStyle(
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                        ),
                        color = homeColors().TextoSecundario,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = String.format(Locale.US, "S/ %.2f", sumaPendientes),
                    style = androidx.compose.ui.text.TextStyle(
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                    ),
                    color = homeColors().TextoPrincipal,
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "›",
                    style = androidx.compose.ui.text.TextStyle(
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                    ),
                    color = homeColors().TextoSecundario,
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

@Composable
private fun RecentOpsSection(
    operaciones: List<Operacion>,
    onVerTodas: () -> Unit,
    resolverNombreAsignado: (Operacion) -> String? = { null },
    onSeleccionarOperacion: ((String) -> Unit)? = null,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.home_operaciones_recientes),
                style = androidx.compose.ui.text.TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    letterSpacing = (-0.375).sp
                ),
                color = homeColors().TextoPrincipal,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable(onClick = onVerTodas)
            ) {
                Text(
                    text = stringResource(R.string.home_ver_todas),
                    style = androidx.compose.ui.text.TextStyle(
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                    ),
                    color = homeColors().EnlaceVerTodas,
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = "›",
                    style = androidx.compose.ui.text.TextStyle(
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                    ),
                    color = homeColors().EnlaceVerTodas,
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (operaciones.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = homeColors().TarjetaFondo),
                border = BorderStroke(1.dp, homeColors().TarjetaBorde),
            ) {
                Text(
                    text = stringResource(R.string.home_sin_operaciones),
                    style = androidx.compose.ui.text.TextStyle(
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                    ),
                    color = homeColors().TextoSecundario,
                    modifier = Modifier.padding(20.dp),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                operaciones.forEach { operacion ->
                    HomeOperacionRow(
                        operacion = operacion,
                        asignadoPorNombre = resolverNombreAsignado(operacion),
                        onClick = { onSeleccionarOperacion?.invoke(operacion.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeOperacionRow(
    operacion: Operacion,
    asignadoPorNombre: String?,
    onClick: () -> Unit
) {
    val themeMode by PreferenciasAppStore.themeMode.collectAsState()
    val methodName = operacion.metodoPago?.name?.lowercase()
    val colorMarca = colorBilletera(operacion.metodoPago, themeMode) ?: homeColors().TileIconoFondo

    val isWhitePlate = methodName == "yape" || methodName == "plin" || methodName == "bcp"
    val colorFondoPlaca = if (isWhitePlate) com.flabs.cobrate.components.BilleteraColores.PlacaLogo else colorMarca
    val borderMetodo = if (isWhitePlate) BorderStroke(2.dp, colorMarca) else BorderStroke(1.dp, homeColors().TarjetaBorde)

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = homeColors().TarjetaFondo),
        border = BorderStroke(1.dp, homeColors().TarjetaBorde),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = colorFondoPlaca,
                    border = borderMetodo,
                    modifier = Modifier.size(44.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (operacion.metodoPago != null) {
                            if (isWhitePlate) {
                                com.flabs.cobrate.components.IconoMetodoPago(
                                    tipo = operacion.metodoPago,
                                    tamano = 34.dp
                                )
                            } else {
                                com.flabs.cobrate.components.IconoMetodoPago(
                                    tipo = operacion.metodoPago,
                                    tamano = 26.dp
                                )
                            }
                        } else {
                            Text(
                                text = "?",
                                style = androidx.compose.ui.text.TextStyle(
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp,
                                ),
                                color = homeColors().TextoSecundario
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = asignadoPorNombre ?: "Desconocido",
                        style = androidx.compose.ui.text.TextStyle(
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                        ),
                        color = homeColors().TextoPrincipal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val metodoTexto = operacion.metodoPago?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Pago"
                    Text(
                        text = "$metodoTexto · ${formatearHoraLocal(operacion.fechaHora)}",
                        style = androidx.compose.ui.text.TextStyle(
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                        ),
                        color = homeColors().TextoSecundario,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = String.format(Locale.US, "S/ %.2f", operacion.monto),
                style = androidx.compose.ui.text.TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                ),
                color = homeColors().TextoPrincipal,
                textAlign = TextAlign.Right
            )
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
