package com.flabs.cobrate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.lifecycle.viewmodel.compose.viewModel
import com.flabs.cobrate.core.data.ConfiguracionDeVozStore
import com.flabs.cobrate.core.data.CuentaStore
import com.flabs.cobrate.core.data.InMemoryOperationStore
import com.flabs.cobrate.core.data.EstadoServiciosStore
import com.flabs.cobrate.core.data.InvitacionStore
import com.flabs.cobrate.core.data.NegocioStore
import com.flabs.cobrate.core.data.RelacionCuentaNegocioStore
import com.flabs.cobrate.core.data.TrabajadorStore
import com.flabs.cobrate.core.logic.RolEfectivo
import com.flabs.cobrate.core.model.EstadoTrabajador
import com.flabs.cobrate.core.model.Negocio
import com.flabs.cobrate.core.model.RolCuentaNegocio
import com.flabs.cobrate.features.auth.AuthViewModel
import com.flabs.cobrate.features.auth.LoginScreen
import com.flabs.cobrate.features.business.CrearNegocioScreen
import com.flabs.cobrate.features.business.NegocioViewModel
import com.flabs.cobrate.features.onboarding.ConfirmarInvitacionScreen
import com.flabs.cobrate.features.onboarding.IngresarCodigoScreen
import com.flabs.cobrate.features.business.BillingPlanScreen
import com.flabs.cobrate.features.business.BusinessStatsScreen
import com.flabs.cobrate.features.business.BusinessViewModel
import com.flabs.cobrate.features.business.EstablishmentsScreen
import com.flabs.cobrate.features.business.FlowTemplatesScreen
import com.flabs.cobrate.features.business.OwnerHistoryScreen
import com.flabs.cobrate.features.business.OwnerPanelScreen
import com.flabs.cobrate.features.business.OwnerWorkersScreen
import com.flabs.cobrate.features.business.PaymentMethodsScreen
import com.flabs.cobrate.features.permisos.DiagnosticoPermisosScreen
import com.flabs.cobrate.features.worker.PayScreen
import com.flabs.cobrate.features.worker.WorkerOperationsScreen
import com.flabs.cobrate.features.worker.WorkerViewModel
import com.flabs.cobrate.ui.theme.CobrateTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Carga las operaciones persistidas al iniciar la app.
        InMemoryOperationStore.init(applicationContext)
        // Carga las cuentas persistidas y restaura la última sesión activa.
        CuentaStore.init(applicationContext)
        // Carga negocios y relaciones persistidas (CAPA 3).
        NegocioStore.init(applicationContext)
        RelacionCuentaNegocioStore.init(applicationContext)
        // Carga invitaciones persistidas (CAPA 4).
        InvitacionStore.init(applicationContext)
        // Carga el estado de SERVICIOS persistido (CAPA 5b).
        EstadoServiciosStore.init(applicationContext)
        // Carga la configuración de voz persistida (CAPA 14).
        ConfiguracionDeVozStore.init(applicationContext)
        setContent {
            CobrateTheme {
                CobrateApp()
            }
        }
    }
}

// Rutas de navegación.
private object Routes {
    const val LOGIN = "login"
    const val INICIO = "inicio"

    // Flujo Trabajador / unirse con invitación (CAPA 4).
    const val INGRESAR_CODIGO = "ingresar_codigo"
    const val CONFIRMAR_INVITACION = "confirmar_invitacion"

    // Flujo Trabajador.
    const val OPERACIONES_TRABAJADOR = "operaciones_trabajador"
    const val SELECCION_IDENTIDAD = "seleccion_identidad"
    const val PAGAR = "pagar"

    // Flujo Dueño.
    const val PANEL_DUENO = "panel_dueno"
    const val HISTORIAL_DUENO = "historial_dueno"
    const val TRABAJADORES_DUENO = "trabajadores_dueno"
    const val METODOS_COBRO_DUENO = "metodos_cobro_dueno"
    const val PLANTILLAS_FLUJO_DUENO = "plantillas_flujo_dueno"
    const val ESTADISTICAS_DUENO = "estadisticas_dueno"
    const val ESTABLECIMIENTOS_DUENO = "establecimientos_dueno"
    const val PLAN_FACTURACION_DUENO = "plan_facturacion_dueno"
    const val PERMISOS_DUENO = "permisos_dueno"
}

@Composable
fun CobrateApp() {
    val navController = rememberNavController()

    // Instancias de ViewModel compartidas en la navegación.
    val workerViewModel: WorkerViewModel = viewModel()
    val businessViewModel: BusinessViewModel = viewModel()
    val authViewModel: AuthViewModel = viewModel()
    val negocioViewModel: NegocioViewModel = viewModel()

    // Observa la sesión: arranca en LOGIN, salta a INICIO en cuanto hay una
    // sesión activa (tras autenticarse o al restaurarse al abrir la app), y
    // vuelve a LOGIN cuando la sesión se cierra (cerrarSesion).
    val sesionActual by authViewModel.sesionActual.collectAsState()
    LaunchedEffect(sesionActual) {
        when {
            sesionActual != null -> {
                navController.navigate(Routes.INICIO) {
                    popUpTo(Routes.LOGIN) { inclusive = true }
                }
            }
            // Al cerrar sesión (null tras haber estado logueado) volvemos a
            // LOGIN limpiando todo el backstack. Evitamos el arranque en frío
            // (ya estamos en LOGIN y no ha habido sesión).
            navController.currentDestination?.route != Routes.LOGIN -> {
                navController.navigate(Routes.LOGIN) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(authViewModel = authViewModel)
        }

        composable(Routes.INICIO) {
            val rol by negocioViewModel.rolEfectivo.collectAsState()
            val misRelaciones by negocioViewModel.misRelaciones.collectAsState()
            val misNegocios by negocioViewModel.misNegocios.collectAsState()

            // Negocios donde la cuenta es ADMINISTRADOR (para el selector).
            val adminNegocios = misNegocios.filter { negocio ->
                misRelaciones.any {
                    it.negocioId == negocio.id && it.rol == RolCuentaNegocio.ADMINISTRADOR
                }
            }

            InicioScreen(
                rol = rol,
                adminNegocios = adminNegocios,
                negocioViewModel = negocioViewModel,
                onEntrarComoDueno = { negocio ->
                    navController.navigate(Routes.PANEL_DUENO)
                },
                onEntrarComoTrabajador = {
                    val activos = TrabajadorStore.trabajadores.value
                        .filter { it.estado == EstadoTrabajador.ACTIVO }
                    when {
                        activos.size == 1 -> {
                            workerViewModel.seleccionarIdentidad(activos.first().id)
                            navController.navigate(Routes.OPERACIONES_TRABAJADOR)
                        }
                        activos.size > 1 -> navController.navigate(Routes.SELECCION_IDENTIDAD)
                        else -> navController.navigate(Routes.OPERACIONES_TRABAJADOR)
                    }
                },
                onSoyTrabajador = {
                    navController.navigate(Routes.INGRESAR_CODIGO)
                }
            )
        }

        // ---- Unirse con invitación (CAPA 4) ----
        composable(Routes.INGRESAR_CODIGO) {
            IngresarCodigoScreen(
                onCodigoValido = { invitacion ->
                    navController.navigate("${Routes.CONFIRMAR_INVITACION}/${invitacion.codigo}")
                },
                onVolver = { navController.popBackStack() }
            )
        }

        composable(
            route = "${Routes.CONFIRMAR_INVITACION}/{codigo}",
            arguments = listOf(navArgument("codigo") { type = NavType.StringType })
        ) { backStackEntry ->
            val codigo = backStackEntry.arguments?.getString("codigo").orEmpty()
            val invitacion = InvitacionStore.buscarPorCodigo(codigo)
            if (invitacion == null) {
                LaunchedEffect(Unit) { navController.popBackStack() }
            } else {
                ConfirmarInvitacionScreen(
                    invitacion = invitacion,
                    onAceptada = {
                        navController.navigate(Routes.OPERACIONES_TRABAJADOR) {
                            popUpTo(Routes.INICIO) { inclusive = true }
                        }
                    },
                    onVolver = { navController.popBackStack() }
                )
            }
        }

        // ---- Flujo Trabajador ----
        composable(Routes.SELECCION_IDENTIDAD) {
            SeleccionIdentidadScreen(
                onSeleccionar = { id ->
                    workerViewModel.seleccionarIdentidad(id)
                    navController.navigate(Routes.OPERACIONES_TRABAJADOR) {
                        popUpTo(Routes.SELECCION_IDENTIDAD) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.OPERACIONES_TRABAJADOR) {
            WorkerOperationsScreen(
                workerViewModel = workerViewModel,
                onVaAPagar = {
                    navController.navigate(Routes.PAGAR)
                },
                onCerrarSesion = { authViewModel.cerrarSesion() }
            )
        }

        composable(Routes.PAGAR) {
            PayScreen(
                workerViewModel = workerViewModel,
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // ---- Flujo Dueño ----
        composable(Routes.PANEL_DUENO) {
            OwnerPanelScreen(
                businessViewModel = businessViewModel,
                onVerHistorial = {
                    navController.navigate(Routes.HISTORIAL_DUENO)
                },
                onVerTrabajadores = {
                    navController.navigate(Routes.TRABAJADORES_DUENO)
                },
                onVerMetodosDeCobro = {
                    navController.navigate(Routes.METODOS_COBRO_DUENO)
                },
                onVerPlantillasFlujo = {
                    navController.navigate(Routes.PLANTILLAS_FLUJO_DUENO)
                },
                onVerEstadisticas = {
                    navController.navigate(Routes.ESTADISTICAS_DUENO)
                },
                onVerEstablecimientos = {
                    navController.navigate(Routes.ESTABLECIMIENTOS_DUENO)
                },
                onVerPlanFacturacion = {
                    navController.navigate(Routes.PLAN_FACTURACION_DUENO)
                },
                onVerPermisos = {
                    navController.navigate(Routes.PERMISOS_DUENO)
                },
                onCerrarSesion = { authViewModel.cerrarSesion() }
            )
        }

        composable(Routes.HISTORIAL_DUENO) {
            OwnerHistoryScreen(businessViewModel)
        }

        composable(Routes.TRABAJADORES_DUENO) {
            OwnerWorkersScreen(businessViewModel)
        }

        composable(Routes.METODOS_COBRO_DUENO) {
            PaymentMethodsScreen()
        }

        composable(Routes.PLANTILLAS_FLUJO_DUENO) {
            FlowTemplatesScreen()
        }

        composable(Routes.ESTADISTICAS_DUENO) {
            BusinessStatsScreen(businessViewModel)
        }

        composable(Routes.ESTABLECIMIENTOS_DUENO) {
            EstablishmentsScreen()
        }

        composable(Routes.PLAN_FACTURACION_DUENO) {
            BillingPlanScreen()
        }

        composable(Routes.PERMISOS_DUENO) {
            DiagnosticoPermisosScreen(
                onVolver = {
                    navController.popBackStack()
                }
            )
        }
    }
}

@Composable
private fun InicioScreen(
    rol: RolEfectivo,
    adminNegocios: List<Negocio>,
    negocioViewModel: NegocioViewModel,
    onEntrarComoDueno: (Negocio?) -> Unit,
    onEntrarComoTrabajador: () -> Unit,
    onSoyTrabajador: () -> Unit
) {
    // En las ramas que no requieren más datos (un solo negocio admin, o
    // trabajador), se navega automáticamente al flujo correspondiente.
    LaunchedEffect(rol, adminNegocios.size) {
        when (rol) {
            // Navegación directa al flujo de Dueño si hay un único negocio admin.
            RolEfectivo.ADMINISTRADOR -> if (adminNegocios.size == 1) {
                onEntrarComoDueno(adminNegocios.first())
            }
            // Navegación directa al flujo de Trabajador.
            RolEfectivo.TRABAJADOR -> onEntrarComoTrabajador()
            RolEfectivo.SIN_NEGOCIOS -> Unit
        }
    }

    when (rol) {
        RolEfectivo.SIN_NEGOCIOS -> {
            // Sin relaciones activas: ofrecer crear el primer negocio o unirse
            // con una invitación. "Crear tu negocio" muestra el formulario
            // inline; "Soy trabajador" lleva a ingresar el código (CAPA 4).
            SinNegociosScreen(
                negocioViewModel = negocioViewModel,
                onCrearNegocio = {},
                onSoyTrabajador = onSoyTrabajador
            )
        }

        RolEfectivo.ADMINISTRADOR -> {
            if (adminNegocios.size > 1) {
                // Varios negocios admin: selector simple antes de entrar.
                SelectorNegocioScreen(
                    negocios = adminNegocios,
                    onSeleccionar = { negocio -> onEntrarComoDueno(negocio) }
                )
            } else {
                // Un único negocio (o ninguno aún): esperando la navegación por LaunchedEffect.
                Box(modifier = Modifier.fillMaxSize())
            }
        }

        RolEfectivo.TRABAJADOR -> {
            // Esperando la navegación por LaunchedEffect.
            Box(modifier = Modifier.fillMaxSize())
        }
    }
}

/** Selector simple de negocio (usado cuando la cuenta administra varios). */
@Composable
private fun SelectorNegocioScreen(
    negocios: List<Negocio>,
    onSeleccionar: (Negocio) -> Unit
) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
        ) {
            Text(
                text = "¿Qué negocio?",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            negocios.forEach { negocio ->
                Button(
                    onClick = { onSeleccionar(negocio) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(negocio.nombre)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

/**
 * Pantalla para cuentas sin relaciones activas (SIN_NEGOCIOS).
 * Ofrece crear el primer negocio o unirse mediante invitación (CAPA 4).
 */
@Composable
private fun SinNegociosScreen(
    negocioViewModel: NegocioViewModel,
    onCrearNegocio: () -> Unit,
    onSoyTrabajador: () -> Unit
) {
    var creandoNegocio by remember { mutableStateOf(false) }

    if (creandoNegocio) {
        CrearNegocioScreen(
            negocioViewModel = negocioViewModel,
            onCreado = onCrearNegocio
        )
    } else {
        Scaffold { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Todavía no perteneces a ningún negocio",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { creandoNegocio = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Crear tu negocio")
                }

                OutlinedButton(
                    onClick = onSoyTrabajador,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Soy trabajador — tengo una invitación")
                }
            }
        }
    }
}

/**
 * Pantalla de selección de identidad simulada del trabajador.
 *
 * Se muestra al entrar como Trabajador cuando hay más de un trabajador ACTIVO.
 * IMPORTANTE: es una simplificación temporal — el trabajador se elige a mano
 * para poder filtrar operaciones por trabajadorId. No es autenticación real;
 * el login llega en la CAPA 9.
 */
@Composable
private fun SeleccionIdentidadScreen(
    onSeleccionar: (String) -> Unit
) {
    val trabajadores by TrabajadorStore.trabajadores.collectAsState()
    val activos = trabajadores.filter { it.estado == EstadoTrabajador.ACTIVO }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
        ) {
            Text(
                text = "¿Cuál trabajador eres?",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            activos.forEach { trabajador ->
                Button(
                    onClick = { onSeleccionar(trabajador.id) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(trabajador.nombre)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
