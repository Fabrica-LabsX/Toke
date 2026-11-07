package com.flabs.cobrate

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.ui.res.stringResource
import com.flabs.cobrate.features.nav.irADetalle
import com.flabs.cobrate.features.nav.irAPestana
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.Rule
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Shield
import com.flabs.cobrate.components.ConfirmarAccionDialog
import com.flabs.cobrate.core.data.NegocioRepository
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.lifecycle.viewmodel.compose.viewModel
import com.flabs.cobrate.components.ConfirmarCierreSesionDialog
import com.flabs.cobrate.components.FeedbackOverlay
import com.flabs.cobrate.components.ItemMas
import com.flabs.cobrate.components.ItemNav
import com.flabs.cobrate.components.MasSheet
import com.flabs.cobrate.components.OpcionCard
import com.flabs.cobrate.components.PedirExencionBateria
import com.flabs.cobrate.components.PedirPermisoNotificaciones
import com.flabs.cobrate.components.TokeBottomBar
import com.flabs.cobrate.components.TokeIcons
import com.flabs.cobrate.core.data.ConfiguracionDeVozStore
import com.flabs.cobrate.core.data.MetodoDeCobroStore
import com.flabs.cobrate.core.data.CuentaStore
import com.flabs.cobrate.core.data.EstadoSincronizacionRemota
import com.flabs.cobrate.core.data.InMemoryOperationStore
import com.flabs.cobrate.core.data.EstadoServiciosStore
import com.flabs.cobrate.core.data.EstadoOperativoStore
import com.flabs.cobrate.services.TokeOperandoService
import com.flabs.cobrate.services.ServiciosToke
import com.flabs.cobrate.core.data.InvitacionStore
import com.flabs.cobrate.core.data.NegocioStore
import com.flabs.cobrate.core.data.PreferenciasAppStore
import com.flabs.cobrate.core.data.RelacionCuentaNegocioStore
import com.flabs.cobrate.core.data.SupabaseAuthBridge
import com.flabs.cobrate.core.data.DispositivoRepository
import com.flabs.cobrate.core.data.OperacionPendienteStore
import com.flabs.cobrate.core.data.RealtimeSyncManager
import com.flabs.cobrate.core.data.RelacionCuentaNegocioRepository
import com.flabs.cobrate.core.logic.AccesoNegocio
import com.flabs.cobrate.core.logic.FeedbackManager
import com.flabs.cobrate.core.logic.LogSeguro
import com.flabs.cobrate.core.logic.LoginRateLimiter
import com.flabs.cobrate.core.logic.NetworkMonitor
import com.flabs.cobrate.core.logic.RegistroDispositivos
import com.flabs.cobrate.core.logic.RequisitosPermisos
import com.flabs.cobrate.core.logic.RolEfectivo
import com.flabs.cobrate.core.logic.TabRefreshManager
import com.flabs.cobrate.core.logic.TabRefreshState
import com.flabs.cobrate.core.logic.TransicionesRelacion
import com.flabs.cobrate.services.FirebaseProveedorDeToken
import kotlinx.coroutines.withTimeoutOrNull
import com.flabs.cobrate.core.model.EstadoRelacion
import com.flabs.cobrate.core.model.Negocio
import com.flabs.cobrate.core.model.RelacionCuentaNegocio
import com.flabs.cobrate.core.model.RolCuentaNegocio
import com.flabs.cobrate.features.auth.ArranqueScreen
import com.flabs.cobrate.features.auth.AuthViewModel
import com.flabs.cobrate.features.auth.BienvenidaScreen
import com.flabs.cobrate.features.auth.LoginScreen

import com.flabs.cobrate.features.business.CrearNegocioScreen
import com.flabs.cobrate.features.business.NegocioViewModel
import com.flabs.cobrate.features.onboarding.ConfirmarInvitacionScreen
import com.flabs.cobrate.features.onboarding.IngresarCodigoScreen
import com.flabs.cobrate.features.business.BillingPlanScreen
import com.flabs.cobrate.features.business.BusinessStatsScreen
import com.flabs.cobrate.features.business.BusinessViewModel
import com.flabs.cobrate.features.business.EstablishmentsScreen
import com.flabs.cobrate.features.negocios.MisNegociosScreen
import com.flabs.cobrate.features.business.OwnerHistoryScreen
import com.flabs.cobrate.features.equipo.EquipoScreen
import com.flabs.cobrate.features.cobros.MetodosDeCobroScreen
import com.flabs.cobrate.features.permisos.DiagnosticoPermisosScreen
import com.flabs.cobrate.ui.theme.composableToke
import com.flabs.cobrate.features.settings.VoiceSettingsScreen
import com.flabs.cobrate.features.operaciones.DetalleOperacionScreen
import com.flabs.cobrate.features.shared.HomeScreen
import com.flabs.cobrate.features.worker.WorkerOperationsScreen
import com.flabs.cobrate.features.worker.EncuentraMiPedidoScreen
import com.flabs.cobrate.features.worker.WorkerViewModel
import com.flabs.cobrate.ui.theme.CobrateTheme
import com.flabs.cobrate.ui.theme.ThemeMode
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashStartTime = System.currentTimeMillis()
        val splashScreen = installSplashScreen()

        // El splash se mantiene en pantalla mientras Supabase Auth se inicializa (máximo 2.5s)
        splashScreen.setKeepOnScreenCondition {
            SupabaseAuthBridge.estaInicializando.value && (System.currentTimeMillis() - splashStartTime < 2500L)
        }

        super.onCreate(savedInstanceState)

        // Inicialización del monitor de red (CAPA TAREA E-1)
        NetworkMonitor.init(this)
        // Inicialización de la cola offline (CAPA TAREA E-2)
        OperacionPendienteStore.init(this)
        // Carga las operaciones persistidas al iniciar la app.
        InMemoryOperationStore.init(applicationContext)
        // Carga las cuentas persistidas y restaura la última sesión activa.
        CuentaStore.init(applicationContext)
        // Carga negocios y relaciones persistidas (CAPA 3).
        NegocioStore.init(applicationContext)
        RelacionCuentaNegocioStore.init(applicationContext)
        // Carga métodos de cobro persistidos.
        MetodoDeCobroStore.init(applicationContext)
        // Carga invitaciones persistidas (CAPA 4).
        InvitacionStore.init(applicationContext)
        // Carga el estado de SERVICIOS persistido (CAPA 5b).
        EstadoServiciosStore.init(applicationContext)
        // Carga la configuración de voz persistida (CAPA 14).
        ConfiguracionDeVozStore.init(applicationContext)
        // Carga las preferencias de la app (C8 - Tema)
        PreferenciasAppStore.init(applicationContext)
        // Carga el estado operativo global (CAPA 57)
        EstadoOperativoStore.init(applicationContext)
        // Rate Limiter para Auth
        LoginRateLimiter.init(applicationContext)

        setContent {
            val themeMode by PreferenciasAppStore.themeMode.collectAsState()

            CobrateTheme(themeMode = themeMode) {
                Box {
                    CobrateApp()
                    FeedbackOverlay()
                }
            }
        }
    }
}

@Composable
fun CobrateApp() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val negocioViewModel: NegocioViewModel = viewModel()
    val workerViewModel: WorkerViewModel = viewModel()
    val businessViewModel: BusinessViewModel = viewModel()

    val sesionActual by CuentaStore.sesionActual.collectAsState()
    val estaInicializando by SupabaseAuthBridge.estaInicializando.collectAsState()

    // --- C9: Estado de navegación y BottomBar ---
    val backStackEntry by navController.currentBackStackEntryAsState()
    val rutaActual = backStackEntry?.destination?.route

    // Timeout de seguridad para evitar congelamiento infinito en Arranque (CAPA 56)
    LaunchedEffect(rutaActual) {
        if (rutaActual == Routes.ARRANQUE) {
            delay(8000L)
            if (navController.currentBackStackEntry?.destination?.route == Routes.ARRANQUE) {
                val sesion = CuentaStore.sesionActual.value
                LogSeguro.w("TokeArranque", "Timeout de 8s en Arranque. Forzando navegación a ${if (sesion != null) "INICIO" else "BIENVENIDA"}")
                val destino = if (sesion != null) Routes.INICIO else Routes.BIENVENIDA
                navController.navigate(destino) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }

    // Lógica de navegación global basada en el estado de la sesión (CAPA 19 & CAPA 54).
    LaunchedEffect(sesionActual, estaInicializando, rutaActual == Routes.ARRANQUE) {
        if (estaInicializando) return@LaunchedEffect

        val intent = (navController.context as? Activity)?.intent
        val routeExtra = intent?.getStringExtra("route")

        val currentRoute = navController.currentBackStackEntry?.destination?.route
        val esRutaLoginOrAuth = currentRoute == Routes.LOGIN ||
                (currentRoute != null && currentRoute.startsWith(Routes.LOGIN)) ||
                currentRoute == Routes.BIENVENIDA ||
                currentRoute == Routes.ARRANQUE ||
                currentRoute == null

        when {
            sesionActual != null -> {
                // Navegamos al hub central (INICIO) limpiando completamente la pila (CAPA 54).
                if (esRutaLoginOrAuth) {
                    navController.navigate(Routes.INICIO) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }

                    // Si venimos de una notificación con ruta profunda (C11)
                    if (routeExtra == "metodos_cobro_dueno") {
                        navController.navigate(Routes.METODOS_COBRO_DUENO)
                        intent.removeExtra("route")
                    }
                }
            }
            // Al cerrar sesión o si no hay sesión activa en ARRANQUE/pantalla autenticada,
            // navegamos a BIENVENIDA limpiando todo el backstack.
            !esRutaLoginOrAuth || currentRoute == Routes.ARRANQUE -> {
                navController.navigate(Routes.BIENVENIDA) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }

    val rolActivo by negocioViewModel.rolActivo.collectAsState()
    val rolEfectivo by negocioViewModel.rolEfectivo.collectAsState()
    val estadoAcceso by negocioViewModel.estadoAcceso.collectAsState()
    val negocioSeleccionadoId by negocioViewModel.negocioSeleccionadoId.collectAsState()
    val negocioExpulsado by negocioViewModel.negocioExpulsado.collectAsState()
    val misNegocios by negocioViewModel.misNegocios.collectAsState()
    val negocioSeleccionado = remember(negocioSeleccionadoId, misNegocios) { misNegocios.find { it.id == negocioSeleccionadoId } }
    val esDuenoReal = negocioSeleccionado != null && sesionActual != null && negocioSeleccionado?.dueñoCuentaId == sesionActual?.id

    val context = LocalContext.current
    val estadoOperativoEncendido by EstadoOperativoStore.encendido.collectAsState()

    var mostrarMas by remember { mutableStateOf(false) }
    var mostrarConfirmarCierre by remember { mutableStateOf(false) }
    var mostrarConfirmarSalir by remember { mutableStateOf(false) }
    var mostrarConfirmarBorrar by remember { mutableStateOf(false) }
    var cargandoBorrar by remember { mutableStateOf(false) }
    var mostrarConfirmarApagarYSalir by remember { mutableStateOf(false) }
    var cargandoApagarYSalir by remember { mutableStateOf(false) }
    val scopeApagarYSalir = rememberCoroutineScope()

    val rutasAutenticadasConSalida = setOf(
        Routes.INICIO,
        Routes.HISTORIAL_DUENO,
        Routes.TRABAJADORES_DUENO,
        Routes.OPERACIONES_TRABAJADOR
    )

    val modalAbierto = mostrarMas ||
            mostrarConfirmarCierre ||
            mostrarConfirmarSalir ||
            mostrarConfirmarBorrar ||
            mostrarConfirmarApagarYSalir ||
            negocioExpulsado != null

    val backHandlerEnabled = sesionActual != null &&
            rutaActual != Routes.LOGIN &&
            rutaActual != Routes.ARRANQUE &&
            (modalAbierto || rutaActual in rutasAutenticadasConSalida)

    BackHandler(enabled = backHandlerEnabled) {
        when {
            mostrarMas -> mostrarMas = false
            mostrarConfirmarCierre -> mostrarConfirmarCierre = false
            mostrarConfirmarSalir -> mostrarConfirmarSalir = false
            mostrarConfirmarBorrar -> mostrarConfirmarBorrar = false
            mostrarConfirmarApagarYSalir -> {
                if (!cargandoApagarYSalir) {
                    mostrarConfirmarApagarYSalir = false
                }
            }
            negocioExpulsado != null -> negocioViewModel.descartarAvisoExpulsion()
            else -> mostrarConfirmarApagarYSalir = true
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    val scopeLifecycle = rememberCoroutineScope()
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val appContext = context.applicationContext
                val uid = CuentaStore.sesionActual.value?.id
                val encendido = EstadoOperativoStore.encendido.value
                if (uid != null && encendido && CuentaStore.estadoSincronizacion.value != EstadoSincronizacionRemota.SINCRONIZANDO) {
                    LogSeguro.d("MainActivity", "[ON_RESUME] Sincronizando datos de ${uid.take(6)}")
                    scopeLifecycle.launch {
                        CuentaStore.sincronizarDatos(uid).onSuccess {
                            LogSeguro.d("MainActivity", "[ON_RESUME] Sincronización exitosa de ${uid.take(6)}")
                        }.onFailure { err ->
                            LogSeguro.w("MainActivity", "[ON_RESUME] Error en sincronización de ${uid.take(6)}", err)
                        }
                    }
                } else {
                    LogSeguro.d("MainActivity", "[ON_RESUME] Omitido por TOKE apagado o sin sesión.")
                }

                // Re-evaluar servicio operativo al volver a primer plano (p.ej. tras otorgar/revocar permisos)
                if (TokeOperandoService.esElegibleParaOperar(appContext)) {
                    runCatching {
                        val intent = Intent(appContext, TokeOperandoService::class.java)
                        ContextCompat.startForegroundService(appContext, intent)
                    }
                } else {
                    runCatching {
                        appContext.stopService(Intent(appContext, TokeOperandoService::class.java))
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(sesionActual?.id, estaInicializando) {
        if (estaInicializando) return@LaunchedEffect
        val uid = sesionActual?.id ?: return@LaunchedEffect
        val appContext = context.applicationContext
        LogSeguro.d("MainActivity", "Sesión activa confirmada tras inicializar Auth (UID ${uid.take(6)}). Activando TOKE...")
        ServiciosToke.encenderTodo(appContext)

        var fotoAnterior: List<RelacionCuentaNegocio>? = null

        RelacionCuentaNegocioStore.relaciones.collect { fotoActual ->
            if (fotoAnterior == null) {
                fotoAnterior = fotoActual
            } else {
                val antes = fotoAnterior!!
                val recienActivadas = TransicionesRelacion.recienActivadas(uid, antes, fotoActual)
                val dejaronDeSerActivas = TransicionesRelacion.dejaronDeSerActivas(uid, antes, fotoActual)

                if (recienActivadas.isNotEmpty()) {
                    LogSeguro.d("TokeSync", "Transición detectada: relaciones recién activadas $recienActivadas. Sincronizando datos...")
                    CuentaStore.sincronizarDatos(uid)
                }

                dejaronDeSerActivas.forEach { id ->
                    val esDueno = NegocioStore.negocios.value.any { it.id == id && it.dueñoCuentaId == uid }
                    if (!esDueno) {
                        LogSeguro.d("TokeSync", "Transición detectada: relación dejó de ser activa en negocio $id. Deteniendo escucha...")
                        RealtimeSyncManager.detenerNegocio(id)
                    }
                }

                fotoAnterior = fotoActual
            }
        }
    }

    // Arranque/Monitoreo de TokeOperandoService (CAPA 58)
    val relaciones by RelacionCuentaNegocioStore.relaciones.collectAsState()

    LaunchedEffect(sesionActual?.id, estadoOperativoEncendido, negocioSeleccionadoId, relaciones) {
        val appContext = context.applicationContext
        if (TokeOperandoService.esElegibleParaOperar(appContext)) {
            runCatching {
                val intent = Intent(appContext, TokeOperandoService::class.java)
                ContextCompat.startForegroundService(appContext, intent)
            }.onFailure { err ->
                LogSeguro.w("TokeSync", "Error iniciando TokeOperandoService", err)
            }
        } else {
            runCatching {
                appContext.stopService(Intent(appContext, TokeOperandoService::class.java))
            }
        }
    }

    // Registro del dispositivo en FCM / Supabase al iniciar sesión (CAPA 52 & CAPA 59)
    LaunchedEffect(sesionActual?.id, estadoOperativoEncendido) {
        if (sesionActual?.id == null || !estadoOperativoEncendido) return@LaunchedEffect
        scopeLifecycle.launch {
            RegistroDispositivos.registrar(FirebaseProveedorDeToken(), DispositivoRepository())
                .onSuccess {
                    LogSeguro.d("TokeSync", "Dispositivo registrado exitosamente para push")
                }
                .onFailure { err ->
                    LogSeguro.w("TokeSync", "No se pudo registrar el dispositivo para push", err)
                }
        }
    }

    LaunchedEffect(estadoAcceso, rutaActual) {
        if (estadoAcceso != AccesoNegocio.EstadoAcceso.NORMAL && rutaActual != null && rutaActual != Routes.INICIO) {
            navController.popBackStack(Routes.INICIO, inclusive = false)
        }
    }

    val rutasConBarra = setOf(
        Routes.INICIO,
        Routes.HISTORIAL_DUENO,
        Routes.TRABAJADORES_DUENO
    )
    val mostrarBarra = estadoAcceso == AccesoNegocio.EstadoAcceso.NORMAL &&
            (rolActivo == RolCuentaNegocio.ADMINISTRADOR || rolActivo == RolCuentaNegocio.TRABAJADOR || esDuenoReal) &&
            negocioSeleccionadoId != null &&
            rutaActual in rutasConBarra

    val itemsNav = listOf(
        ItemNav(Routes.INICIO, stringResource(R.string.nombre_inicio), TokeIcons.NavInicio),
        ItemNav(Routes.HISTORIAL_DUENO, stringResource(R.string.nombre_mis_cobros), TokeIcons.NavOperaciones),
        ItemNav(Routes.TRABAJADORES_DUENO, stringResource(R.string.nombre_mi_equipo), TokeIcons.NavEquipo),
        ItemNav("MAS", stringResource(R.string.nombre_mas), TokeIcons.NavMas)
    )

    val cantidadSolicitudes by businessViewModel.cantidadSolicitudes.collectAsState()
    val refreshStates by TabRefreshManager.refreshStates.collectAsState()
    val scopeRefresco = rememberCoroutineScope()

    val onRefrescarTab: (String) -> Unit = { route ->
        val uid = sesionActual?.id
        if (uid != null) {
            scopeRefresco.launch {
                TabRefreshManager.ejecutarRefresco(route) {
                    when (route) {
                        Routes.INICIO -> {
                            val r1 = CuentaStore.sincronizarDatos(uid)
                            businessViewModel.cargarMetodos()
                            r1
                        }
                        Routes.HISTORIAL_DUENO -> {
                            businessViewModel.refrescarMisCobros()
                        }
                        Routes.TRABAJADORES_DUENO -> {
                            businessViewModel.refrescarEquipo()
                        }
                        else -> Result.success(Unit)
                    }
                }
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (mostrarBarra) {
                TokeBottomBar(
                    items = itemsNav,
                    idSeleccionado = if (rutaActual in rutasConBarra) rutaActual else null,
                    onSeleccionar = { id ->
                        if (id == "MAS") {
                            mostrarMas = true
                        } else {
                            navController.irAPestana(id)
                        }
                    },
                    onRecibir = {
                        navController.irADetalle(Routes.METODOS_COBRO_DUENO)
                    },
                    onRefrescarTab = onRefrescarTab,
                    refreshStates = refreshStates,
                    insigniaEquipo = if (esDuenoReal) cantidadSolicitudes else 0
                )
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.ARRANQUE,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { fadeIn(animationSpec = tween(durationMillis = 140)) },
            exitTransition = { fadeOut(animationSpec = tween(durationMillis = 140)) },
            popEnterTransition = { fadeIn(animationSpec = tween(durationMillis = 140)) },
            popExitTransition = { fadeOut(animationSpec = tween(durationMillis = 140)) }
        ) {
            composable(Routes.ARRANQUE) {
                ArranqueScreen()
            }

            composable(Routes.BIENVENIDA) {
                BienvenidaScreen(
                    onIniciarSesion = {
                        navController.navigate("${Routes.LOGIN}?modo=login")
                    },
                    onCrearCuenta = {
                        navController.navigate("${Routes.LOGIN}?modo=crear")
                    },
                    onIngresarCodigo = {
                        navController.navigate(Routes.INGRESAR_CODIGO)
                    }
                )
            }

            composable(
                route = "${Routes.LOGIN}?modo={modo}",
                arguments = listOf(navArgument("modo") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = "login"
                })
            ) { backStackEntry ->
                val modo = backStackEntry.arguments?.getString("modo") ?: "login"
                LoginScreen(
                    authViewModel = authViewModel,
                    modoInicialCrear = (modo == "crear"),
                    onVolver = { navController.popBackStack() }
                )
            }

            composable(Routes.INICIO) {
                InicioScreen(
                    negocioViewModel = negocioViewModel,
                    workerViewModel = workerViewModel,
                    businessViewModel = businessViewModel,
                    authViewModel = authViewModel,
                    navController = navController,
                    onSalirDelNegocio = { mostrarConfirmarSalir = true },
                    onRefrescarTab = { onRefrescarTab(Routes.INICIO) }
                )
            }

            // ---- Unirse con invitación (CAPA 4) ----
            composable(Routes.INGRESAR_CODIGO) {
                IngresarCodigoScreen(
                    onCodigoValido = { invitacion ->
                        InvitacionStore.agregarManual(invitacion)
                        navController.navigate("${Routes.CONFIRMAR_INVITACION}/${invitacion.codigo}")
                    },
                    onVolver = { navController.popBackStack() }
                )
            }

            composable(
                route = "${Routes.CONFIRMAR_INVITACION}/{codigo}",
                arguments = listOf(navArgument("codigo") { type = NavType.StringType })
            ) { backStackEntry ->
                val codigo = backStackEntry.arguments?.getString("codigo") ?: ""
                val invitacion = InvitacionStore.invitaciones.collectAsState().value.find { it.codigo == codigo }
                ConfirmarInvitacionScreen(
                    codigo = codigo,
                    invitacion = invitacion,
                    onAceptada = {
                        navController.navigate(Routes.INICIO) {
                            popUpTo(Routes.INICIO) { inclusive = true }
                        }
                    },
                    onVolver = { navController.popBackStack() }
                )
            }

            // ---- Crear negocio (CAPA 3) ----
            composable(Routes.CREAR_NEGOCIO) {
                CrearNegocioScreen(
                    negocioViewModel = negocioViewModel,
                    onCreado = {
                        navController.navigate(Routes.INICIO) {
                            popUpTo(Routes.INICIO) { inclusive = true }
                        }
                    }
                )
            }

            // ---- Flujo Trabajador ----
            composableToke(Routes.OPERACIONES_TRABAJADOR) {
                WorkerOperationsScreen(
                    workerViewModel = workerViewModel,
                    onEncontrarPedido = { navController.navigate(Routes.ENCUENTRA_PEDIDO) },
                    onCerrarSesion = { mostrarConfirmarCierre = true }
                )
            }

            composableToke(Routes.ENCUENTRA_PEDIDO) {
                EncuentraMiPedidoScreen(
                    viewModel = workerViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // ---- Sub-pantallas Dueño ----
            composableToke(Routes.HISTORIAL_DUENO) {
                OwnerHistoryScreen(
                    businessViewModel = businessViewModel,
                    onRefrescarTab = { onRefrescarTab(Routes.HISTORIAL_DUENO) },
                    onSeleccionarOperacion = { id -> navController.irADetalle("${Routes.DETALLE_OPERACION}/$id") }
                )
            }

            composableToke(Routes.TRABAJADORES_DUENO) {
                EquipoScreen(
                    businessViewModel = businessViewModel,
                    soloLectura = (rolActivo == RolCuentaNegocio.TRABAJADOR && !esDuenoReal)
                )
            }

            composableToke(Routes.METODOS_COBRO_DUENO) {
                val esTrabajador = (rolActivo == RolCuentaNegocio.TRABAJADOR && !esDuenoReal)
                if (esTrabajador) {
                    MetodosDeCobroScreen(
                        businessViewModel = businessViewModel,
                        soloLectura = true
                    )
                } else {
                    val uid = CuentaStore.sesionActual.collectAsState().value?.id
                    val negocioActivoId by NegocioStore.negocioActivoId.collectAsState()
                    val misRelaciones by RelacionCuentaNegocioStore.relaciones.collectAsState()
                    val misNegocios by NegocioStore.negocios.collectAsState()

                    val esAutorizado = remember(negocioActivoId, uid, misRelaciones, misNegocios) {
                        if (negocioActivoId == null || uid == null) false
                        else {
                            val negocio = misNegocios.find { it.id == negocioActivoId }
                            val esDueno = negocio?.dueñoCuentaId == uid
                            val relacion = misRelaciones.find { it.cuentaId == uid && it.negocioId == negocioActivoId }
                            val esAdminActivo = relacion?.rol == RolCuentaNegocio.ADMINISTRADOR && relacion.estado == EstadoRelacion.ACTIVA
                            esDueno || esAdminActivo
                        }
                    }

                    if (esAutorizado) {
                        MetodosDeCobroScreen(
                            businessViewModel = businessViewModel,
                            soloLectura = false
                        )
                    } else {
                        LaunchedEffect(Unit) {
                            navController.popBackStack()
                        }
                    }
                }
            }



            composableToke(Routes.ESTADISTICAS_DUENO) {
                BusinessStatsScreen(businessViewModel)
            }

            composableToke(Routes.ESTABLECIMIENTOS_DUENO) {
                EstablishmentsScreen(
                    negocioViewModel = negocioViewModel,
                    onNuevoNegocio = {
                        navController.navigate(Routes.CREAR_NEGOCIO)
                    },
                    onUnirmeNegocio = {
                        navController.navigate(Routes.INGRESAR_CODIGO)
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            composableToke(Routes.PLAN_FACTURACION_DUENO) {
                BillingPlanScreen()
            }

            composable(Routes.PERMISOS_DUENO) {
                val cuentaId = CuentaStore.sesionActual.collectAsState().value?.id ?: ""
                val misRelaciones by RelacionCuentaNegocioStore.relaciones.collectAsState()
                val reqs = remember(cuentaId, misRelaciones) {
                    RequisitosPermisos.para(cuentaId, misRelaciones)
                }
                DiagnosticoPermisosScreen(
                    requisitos = reqs,
                    onVolver = {
                        navController.popBackStack()
                    }
                )
            }

            composableToke(Routes.VOZ) {
                VoiceSettingsScreen()
            }

            composable(Routes.MIS_NEGOCIOS) {
                MisNegociosScreen(
                    negocioViewModel = negocioViewModel,
                    onVolver = { navController.popBackStack() },
                    onCrearNegocio = { navController.navigate(Routes.CREAR_NEGOCIO) },
                    onUnirmeNegocio = { navController.navigate(Routes.INGRESAR_CODIGO) }
                )
            }

            composable(
                route = "${Routes.DETALLE_OPERACION}/{operacionId}",
                arguments = listOf(navArgument("operacionId") { type = NavType.StringType })
            ) { backStackEntry ->
                val operacionId = backStackEntry.arguments?.getString("operacionId") ?: ""
                DetalleOperacionScreen(
                    operacionId = operacionId,
                    businessViewModel = businessViewModel,
                    onBack = { navController.popBackStack() },
                    onVolverInicio = { navController.irAPestana(Routes.INICIO) }
                )
            }
        }
    }

    if (mostrarMas) {
        val esTrabajador = (rolActivo == RolCuentaNegocio.TRABAJADOR && !esDuenoReal)
        val itemsMas = if (esTrabajador) {
            val baseItems = mutableListOf(
                ItemMas(stringResource(R.string.nombre_mis_negocios), Icons.Default.Storefront) {
                    navController.irADetalle(Routes.MIS_NEGOCIOS)
                },
                ItemMas(stringResource(R.string.nombre_aviso_voz), Icons.AutoMirrored.Filled.VolumeUp) {
                    navController.irADetalle(Routes.VOZ)
                },
                ItemMas(stringResource(R.string.nombre_permisos), Icons.Default.Shield) {
                    navController.irADetalle(Routes.PERMISOS_DUENO)
                }
            )
            if (negocioSeleccionadoId != null) {
                baseItems.add(
                    ItemMas(stringResource(R.string.accion_salir_negocio), Icons.AutoMirrored.Filled.ExitToApp) {
                        mostrarConfirmarSalir = true
                    }
                )
            }
            baseItems
        } else {
            val baseItems = mutableListOf(
                ItemMas(stringResource(R.string.nombre_mis_negocios), Icons.Default.Storefront) {
                    navController.irADetalle(Routes.MIS_NEGOCIOS)
                },
                ItemMas(stringResource(R.string.nombre_mi_equipo), Icons.Default.Groups) {
                    navController.irAPestana(Routes.TRABAJADORES_DUENO)
                },
                ItemMas(stringResource(R.string.nombre_reportes), Icons.Default.BarChart) {
                    navController.irADetalle(Routes.ESTADISTICAS_DUENO)
                },
                ItemMas(stringResource(R.string.nombre_mis_qr), Icons.Default.Payments) {
                    navController.irADetalle(Routes.METODOS_COBRO_DUENO)
                },
                ItemMas(stringResource(R.string.nombre_mis_locales), Icons.Default.Store) {
                    navController.irADetalle(Routes.ESTABLECIMIENTOS_DUENO)
                },
                ItemMas(stringResource(R.string.nombre_aviso_voz), Icons.AutoMirrored.Filled.VolumeUp) {
                    navController.irADetalle(Routes.VOZ)
                },
                ItemMas(stringResource(R.string.nombre_permisos), Icons.Default.Shield) {
                    navController.irADetalle(Routes.PERMISOS_DUENO)
                },
                ItemMas(stringResource(R.string.nombre_mi_plan), Icons.AutoMirrored.Filled.ReceiptLong) {
                    navController.irADetalle(Routes.PLAN_FACTURACION_DUENO)
                }
            )
            if (esDuenoReal) {
                baseItems.add(
                    ItemMas(stringResource(R.string.accion_borrar_negocio), Icons.Default.DeleteForever) {
                        mostrarConfirmarBorrar = true
                    }
                )
            }
            baseItems
        }
        MasSheet(
            items = itemsMas,
            onDismiss = { mostrarMas = false },
            onCerrarSesion = {
                mostrarMas = false
                mostrarConfirmarCierre = true
            },
            onApagarToke = {
                mostrarMas = false
                val appContext = context.applicationContext
                val activity = context as? Activity
                scopeApagarYSalir.launch {
                    runCatching {
                        withTimeoutOrNull(4000L) {
                            ServiciosToke.apagarTodo(appContext)
                        }
                    }
                    ServiciosToke.cerrarTarea(activity, appContext)
                }
            }
        )
    }

    if (mostrarConfirmarCierre) {
        ConfirmarCierreSesionDialog(
            onConfirmar = {
                mostrarConfirmarCierre = false
                authViewModel.cerrarSesion()
            },
            onCancelar = {
                mostrarConfirmarCierre = false
            }
        )
    }

    if (mostrarConfirmarSalir && negocioSeleccionado != null) {
        val scopeSalir = rememberCoroutineScope()
        ConfirmarAccionDialog(
            titulo = stringResource(R.string.dialogo_salir_titulo, negocioSeleccionado.nombre),
            texto = stringResource(R.string.dialogo_salir_texto),
            textoConfirmar = stringResource(R.string.dialogo_confirmar_salir),
            destructivo = true,
            onConfirmar = {
                val cuentaId = sesionActual?.id
                val negocioId = negocioSeleccionado.id
                scopeSalir.launch {
                    RelacionCuentaNegocioRepository().salirDelNegocio(negocioId).onSuccess {
                        mostrarConfirmarSalir = false
                        if (cuentaId != null) {
                            RelacionCuentaNegocioStore.quitarRelacion(negocioId, cuentaId)
                        }
                        negocioViewModel.seleccionarNegocio(null)
                        navController.irAPestana(Routes.INICIO)
                        FeedbackManager.showSuccess("Saliste del negocio.")
                    }.onFailure { err ->
                        mostrarConfirmarSalir = false
                        FeedbackManager.showErrorMessage(err.message ?: "Error al salir del negocio")
                    }
                }
            },
            onCancelar = { mostrarConfirmarSalir = false }
        )
    }

    if (mostrarConfirmarBorrar && negocioSeleccionado != null) {
        val scopeBorrar = rememberCoroutineScope()
        ConfirmarAccionDialog(
            titulo = stringResource(R.string.dialogo_borrar_titulo, negocioSeleccionado.nombre),
            texto = stringResource(R.string.dialogo_borrar_texto),
            textoConfirmar = stringResource(R.string.dialogo_confirmar_borrar),
            textoQueHayQueEscribir = negocioSeleccionado.nombre,
            cargando = cargandoBorrar,
            destructivo = true,
            onConfirmar = {
                val negocioId = negocioSeleccionado.id
                cargandoBorrar = true
                scopeBorrar.launch {
                    NegocioRepository().eliminarNegocio(negocioId).onSuccess {
                        cargandoBorrar = false
                        mostrarConfirmarBorrar = false
                        NegocioStore.quitarNegocio(negocioId)
                        RelacionCuentaNegocioStore.quitarPorNegocio(negocioId)
                        negocioViewModel.seleccionarNegocio(null)
                        navController.irAPestana(Routes.INICIO)
                        FeedbackManager.showSuccess("Negocio borrado.")
                    }.onFailure { err ->
                        cargandoBorrar = false
                        FeedbackManager.showErrorMessage(err.message ?: "Error al borrar el negocio")
                    }
                }
            },
            onCancelar = { mostrarConfirmarBorrar = false }
        )
    }

    if (mostrarConfirmarApagarYSalir) {
        ConfirmarAccionDialog(
            titulo = stringResource(R.string.dialogo_apagar_titulo),
            texto = stringResource(R.string.dialogo_apagar_texto),
            textoConfirmar = stringResource(R.string.dialogo_apagar_confirmar),
            textoCancelar = stringResource(R.string.dialogo_cancelar),
            destructivo = true,
            cargando = cargandoApagarYSalir,
            onConfirmar = {
                cargandoApagarYSalir = true
                val appContext = context.applicationContext
                val activity = context as? Activity
                scopeApagarYSalir.launch {
                    runCatching {
                        withTimeoutOrNull(4000L) {
                            ServiciosToke.apagarTodo(appContext)
                        }
                    }
                    ServiciosToke.cerrarTarea(activity, appContext)
                }
            },
            onCancelar = {
                if (!cargandoApagarYSalir) {
                    mostrarConfirmarApagarYSalir = false
                }
            }
        )
    }

    if (negocioExpulsado != null) {
        AlertDialog(
            onDismissRequest = { negocioViewModel.descartarAvisoExpulsion() },
            title = {
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Lo sentimos", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Te han expulsado de este negocio.", textAlign = TextAlign.Center)
                }
            },
            confirmButton = {
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    TextButton(onClick = { negocioViewModel.descartarAvisoExpulsion() }) {
                        Text("Entendido")
                    }
                }
            }
        )
    }
}

@Composable
private fun InicioScreen(
    negocioViewModel: NegocioViewModel,
    workerViewModel: WorkerViewModel,
    businessViewModel: BusinessViewModel,
    authViewModel: AuthViewModel,
    navController: NavController,
    onSalirDelNegocio: () -> Unit = {},
    onRefrescarTab: (() -> Unit)? = null
) {
    PedirPermisoNotificaciones()
    PedirExencionBateria()

    val misNegocios by negocioViewModel.misNegocios.collectAsState()
    val seleccionadoId by negocioViewModel.negocioSeleccionadoId.collectAsState()

    // Sincronizar el negocio seleccionado con el BusinessViewModel para que los datos
    // del Home correspondan siempre al negocio elegido en el Hub.
    // DECISIÓN C9: Ya no se requiere sync manual, BusinessViewModel observa NegocioStore.negocioActivoId.

    val negocioActual = misNegocios.find { it.id == seleccionadoId }

    HomeScreen(
        negocio = negocioActual,
        businessViewModel = businessViewModel,
        negocioViewModel = negocioViewModel,
        authViewModel = authViewModel,
        workerViewModel = workerViewModel,
        onVerTodas = { navController.irAPestana(Routes.HISTORIAL_DUENO) },
        onMisQRs = { navController.irADetalle(Routes.METODOS_COBRO_DUENO) },
        onAnadirTrabajador = { navController.irAPestana(Routes.TRABAJADORES_DUENO) },
        onReclamarPagos = {},
        onCrearNegocio = { navController.navigate(Routes.CREAR_NEGOCIO) },
        onUnirmeNegocio = { navController.navigate(Routes.INGRESAR_CODIGO) },
        onSalirDelNegocio = onSalirDelNegocio,
        onRefrescarTab = onRefrescarTab,
        onSeleccionarOperacion = { id -> navController.irADetalle("${Routes.DETALLE_OPERACION}/$id") },
        onAbrirPermisos = { navController.irADetalle(Routes.PERMISOS_DUENO) }
    )
}

object Routes {
    const val ARRANQUE = "arranque"
    const val BIENVENIDA = "bienvenida"
    const val LOGIN = "login"
    const val INICIO = "inicio"
    const val CREAR_NEGOCIO = "crear_negocio"
    const val INGRESAR_CODIGO = "ingresar_codigo"
    const val CONFIRMAR_INVITACION = "confirmar_invitacion"
    const val DETALLE_OPERACION = "detalle_operacion"
    const val MIS_NEGOCIOS = "mis_negocios"

    // Trabajador
    const val OPERACIONES_TRABAJADOR = "operaciones_trabajador"
    const val ENCUENTRA_PEDIDO = "encuentra_pedido"

    // Dueño
    const val HISTORIAL_DUENO = "historial_dueno"
    const val TRABAJADORES_DUENO = "trabajadores_dueno"
    const val METODOS_COBRO_DUENO = "metodos_cobro_dueno"
    const val ESTADISTICAS_DUENO = "estadisticas_dueno"
    const val ESTABLECIMIENTOS_DUENO = "establecimientos_dueno"
    const val PLAN_FACTURACION_DUENO = "plan_facturacion_dueno"
    const val PERMISOS_DUENO = "permisos_dueno"
    const val VOZ = "voz"
}
