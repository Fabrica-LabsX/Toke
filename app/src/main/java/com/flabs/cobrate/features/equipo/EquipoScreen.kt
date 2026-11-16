package com.flabs.cobrate.features.equipo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import com.flabs.cobrate.core.data.NegocioStore
import com.flabs.cobrate.core.model.EstadoRelacion
import com.flabs.cobrate.features.business.BusinessViewModel
import com.flabs.cobrate.features.business.InvitarTrabajadorScreen
import com.flabs.cobrate.features.business.TrabajadorRealInfo
import com.flabs.cobrate.ui.theme.PlusJakartaSans
import com.flabs.cobrate.ui.theme.TokePaleta
import com.flabs.cobrate.ui.theme.tokePaleta

/**
 * Pantalla de Equipo. Solo reúne los datos y arma las piezas:
 *  - qué lista se ve y qué dice el buscador lo decide [FiltroEquipo];
 *  - cómo se ve cada pieza vive en los archivos Equipo*.
 *
 * Con [soloLectura] se ve la lista del equipo sin poder pausar, expulsar ni invitar.
 */
@Composable
fun EquipoScreen(
    businessViewModel: BusinessViewModel,
    soloLectura: Boolean = false,
    onIrAPlanes: () -> Unit = {},
) {
    val paleta = tokePaleta()
    val puedeAdministrar = !soloLectura

    val trabajadores by businessViewModel.trabajadoresReales.collectAsState()
    val solicitudes by businessViewModel.solicitudes.collectAsState()
    val cantidadSolicitudes by businessViewModel.cantidadSolicitudes.collectAsState()
    val codigosActivos by businessViewModel.codigosActivos.collectAsState()
    val negocioId by businessViewModel.negocioActualId.collectAsState()
    val negocios by NegocioStore.negocios.collectAsState()
    val nombreNegocio = remember(negocios, negocioId) { negocios.find { it.id == negocioId }?.nombre }

    var pestana by remember { mutableStateOf(PestanaEquipo.MI_EQUIPO) }
    var texto by remember { mutableStateOf("") }
    var mostrandoInvitar by remember { mutableStateOf(false) }
    var trabajadorAExpulsar by remember { mutableStateOf<TrabajadorRealInfo?>(null) }

    LaunchedEffect(negocioId) {
        businessViewModel.cargarEquipoDelNegocio()
        businessViewModel.cargarEquipoVisible()
    }

    LaunchedEffect(cantidadSolicitudes) {
        pestana = FiltroEquipo.pestanaTrasCambioDeSolicitudes(pestana, cantidadSolicitudes)
    }

    Scaffold(
        containerColor = paleta.fondo,
        topBar = { EquipoCabecera(paleta = paleta, nombreNegocio = nombreNegocio) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            EquipoPestanas(
                paleta = paleta,
                seleccionada = pestana,
                totalTrabajadores = trabajadores.size,
                cantidadSolicitudes = cantidadSolicitudes,
                puedeAdministrar = puedeAdministrar,
                onSeleccionar = { pestana = it },
            )

            val mostrarBusqueda = when (pestana) {
                PestanaEquipo.MI_EQUIPO -> true
                PestanaEquipo.CODIGOS -> !mostrandoInvitar && (codigosActivos.size > 1 || texto.isNotEmpty())
                PestanaEquipo.SOLICITUDES -> solicitudes.size > 2 || texto.isNotEmpty()
            }
            if (mostrarBusqueda) {
                CampoBusqueda(
                    paleta = paleta,
                    texto = texto,
                    placeholder = FiltroEquipo.placeholder(pestana),
                    onTextoCambia = { texto = it },
                )
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                when (pestana) {
                    PestanaEquipo.MI_EQUIPO -> {
                        val lista = FiltroEquipo.trabajadores(trabajadores, texto)
                        if (lista.isEmpty()) {
                            item {
                                MensajeVacio(
                                    paleta = paleta,
                                    mensaje = if (texto.isBlank()) {
                                        "No hay trabajadores registrados en este equipo."
                                    } else {
                                        "No se encontraron trabajadores con '$texto'."
                                    },
                                )
                            }
                        }
                        items(lista, key = { it.cuentaId }) { info ->
                            EquipoTrabajadorFila(
                                paleta = paleta,
                                info = info,
                                puedeAdministrar = puedeAdministrar,
                                onPausarReanudar = {
                                    businessViewModel.cambiarEstadoTrabajador(
                                        info.cuentaId,
                                        FiltroEquipo.estadoAlPausarOReanudar(info.estado),
                                    )
                                },
                                onExpulsar = { trabajadorAExpulsar = info },
                            )
                        }
                    }

                    PestanaEquipo.CODIGOS -> {
                        if (mostrandoInvitar) {
                            item {
                                InvitarTrabajadorScreen(
                                    businessViewModel = businessViewModel,
                                    onCerrar = { mostrandoInvitar = false },
                                    onIrAPlanes = onIrAPlanes,
                                )
                            }
                        }
                        val lista = FiltroEquipo.codigos(codigosActivos, texto)
                        item {
                            Text(
                                text = "Códigos vigentes activos",
                                style = TextStyle(
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                ),
                                color = paleta.textoPrincipal,
                            )
                        }
                        if (lista.isEmpty()) {
                            item { MensajeVacio(paleta, "No hay códigos de invitación activos.") }
                        }
                        items(lista, key = { it.codigo }) { invitacion ->
                            EquipoCodigoFila(
                                paleta = paleta,
                                invitacion = invitacion,
                                onRevocar = { businessViewModel.revocarCodigo(invitacion.codigo) },
                            )
                        }
                    }

                    PestanaEquipo.SOLICITUDES -> {
                        val lista = FiltroEquipo.solicitudes(solicitudes, texto)
                        if (lista.isEmpty()) {
                            item { MensajeVacio(paleta, "No hay solicitudes de ingreso pendientes.") }
                        }
                        items(lista, key = { it.cuentaId }) { solicitud ->
                            EquipoSolicitudFila(
                                paleta = paleta,
                                solicitud = solicitud,
                                onAceptar = { businessViewModel.responderSolicitud(solicitud.cuentaId, true) },
                                onRechazar = { businessViewModel.responderSolicitud(solicitud.cuentaId, false) },
                            )
                        }
                    }
                }
            }

            if (puedeAdministrar) {
                val planEstado by com.flabs.cobrate.features.planes.PlanStore.estado.collectAsState()
                val esPro = planEstado?.plan == com.flabs.cobrate.features.planes.Plan.PRO
                val puedeGenerarMas = esPro || (trabajadores.size < com.flabs.cobrate.features.planes.PlanLogica.MAX_TRABAJADORES_FREE && codigosActivos.size < com.flabs.cobrate.features.planes.PlanLogica.MAX_CODIGOS_ACTIVOS_FREE)
                val aforoLleno = codigosActivos.size >= com.flabs.cobrate.features.planes.PlanLogica.MAX_CODIGOS_ACTIVOS_GLOBAL

                when (pestana) {
                    PestanaEquipo.MI_EQUIPO -> {
                        if (puedeGenerarMas) {
                            EquipoBotonPrincipal(
                                paleta = paleta,
                                texto = "+ Invitar nuevo colaborador",
                                onClick = {
                                    pestana = PestanaEquipo.CODIGOS
                                    mostrandoInvitar = true
                                },
                            )
                        } else {
                            com.flabs.cobrate.components.BotonProBloqueado(
                                paleta = paleta,
                                texto = "Invitar colaborador (Máx 1 en Free)",
                                onClick = onIrAPlanes,
                            )
                        }
                    }
                    PestanaEquipo.CODIGOS -> {
                        if (aforoLleno) {
                            com.flabs.cobrate.features.acceso.BotonAcceso(
                                paleta = paleta,
                                texto = "Aforo máximo de 5 códigos alcanzado",
                                principal = false,
                                onClick = {},
                            )
                        } else if (!puedeGenerarMas) {
                            com.flabs.cobrate.components.BotonProBloqueado(
                                paleta = paleta,
                                texto = if (trabajadores.size >= 1) "Máx 1 colaborador en Free (Ver planes)" else "Máx 1 código en Free (Ver planes)",
                                onClick = onIrAPlanes,
                            )
                        } else {
                            EquipoBotonPrincipal(
                                paleta = paleta,
                                texto = if (mostrandoInvitar) "Ocultar panel de invitación" else "+ Generar nuevo código",
                                onClick = { mostrandoInvitar = !mostrandoInvitar },
                            )
                        }
                    }
                    PestanaEquipo.SOLICITUDES -> Unit
                }
            }
        }
    }

    trabajadorAExpulsar?.let { info ->
        EquipoDialogoExpulsar(
            paleta = paleta,
            trabajador = info,
            onConfirmar = {
                businessViewModel.cambiarEstadoTrabajador(info.cuentaId, EstadoRelacion.EXPULSADA)
                trabajadorAExpulsar = null
            },
            onCancelar = { trabajadorAExpulsar = null },
        )
    }
}

@Composable
private fun MensajeVacio(paleta: TokePaleta, mensaje: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = mensaje,
            style = TextStyle(
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
            ),
            color = paleta.textoSecundario,
        )
    }
}
