package com.flabs.cobrate.features.business

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flabs.cobrate.components.AvatarIniciales
import com.flabs.cobrate.core.model.EstadoRelacion
import com.flabs.cobrate.core.model.InvitacionTrabajador
import com.flabs.cobrate.ui.theme.TokeGreen
import com.flabs.cobrate.ui.theme.TokeWarning

/**
 * Pantalla de Equipo del Negocio (Nodos Figma 1:156 y 1:356 - TOKE! - Equipo).
 *
 * Incluye pestañas "Mi Equipo", "Códigos" y "Solicitudes", tarjetas de métricas
 * ("Activos" y "En Pausa"), búsqueda local, tarjetas con acciones táctiles
 * (Pausar/Reanudar, Expulsar) y diálogo de confirmación.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerWorkersScreen(
    businessViewModel: BusinessViewModel,
    soloLectura: Boolean = false,
    onRefrescarTab: (() -> Unit)? = null
) {
    val trabajadores by businessViewModel.trabajadoresReales.collectAsState()
    val solicitudes by businessViewModel.solicitudes.collectAsState()
    val cantidadSolicitudes by businessViewModel.cantidadSolicitudes.collectAsState()
    val codigosActivos by businessViewModel.codigosActivos.collectAsState()

    var pestanaSeleccionada by remember { mutableIntStateOf(0) }
    var textoBusqueda by remember { mutableStateOf("") }
    var mostrandoInvitarModal by remember { mutableStateOf(false) }

    // Diálogo de confirmación de expulsión
    var trabajadorAExpulsar by remember { mutableStateOf<TrabajadorRealInfo?>(null) }

    // Conteo real
    val conteoActivos = remember(trabajadores) {
        trabajadores.count { it.estado == EstadoRelacion.ACTIVA }
    }
    val conteoPausados = remember(trabajadores) {
        trabajadores.count { it.estado == EstadoRelacion.PAUSADA }
    }

    // Si la cantidad de solicitudes baja a cero y estaba en Solicitudes, volver a Mi Equipo
    LaunchedEffect(cantidadSolicitudes) {
        if (cantidadSolicitudes == 0 && pestanaSeleccionada == 2) {
            pestanaSeleccionada = 0
        }
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ---- PESTAÑAS DE NAVEGACIÓN DE EQUIPO (Nodo Figma 1:359) ----
            TabRow(
                selectedTabIndex = pestanaSeleccionada,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[pestanaSeleccionada]),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                Tab(
                    selected = pestanaSeleccionada == 0,
                    onClick = { pestanaSeleccionada = 0 },
                    text = {
                        Text(
                            text = "Mi Equipo",
                            fontWeight = if (pestanaSeleccionada == 0) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                )
                Tab(
                    selected = pestanaSeleccionada == 1,
                    onClick = { pestanaSeleccionada = 1 },
                    text = {
                        Text(
                            text = "Códigos",
                            fontWeight = if (pestanaSeleccionada == 1) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                )
                if (cantidadSolicitudes > 0) {
                    Tab(
                        selected = pestanaSeleccionada == 2,
                        onClick = { pestanaSeleccionada = 2 },
                        text = {
                            BadgedBox(
                                badge = {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.error,
                                        contentColor = MaterialTheme.colorScheme.onError
                                    ) {
                                        Text(text = cantidadSolicitudes.toString())
                                    }
                                }
                            ) {
                                Text(
                                    text = "Solicitudes",
                                    fontWeight = if (pestanaSeleccionada == 2) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    )
                }
            }

            // ---- TARJETAS DE MÉTRICAS / METRICS CARDS solo en Mi Equipo (Nodo Figma 1:371) ----
            if (pestanaSeleccionada == 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Tarjeta Activos
                    MetricSummaryCard(
                        titulo = "Activos",
                        valor = conteoActivos.toString(),
                        icono = Icons.Default.Group,
                        accentColor = TokeGreen,
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.weight(1f)
                    )

                    // Tarjeta En Pausa
                    MetricSummaryCard(
                        titulo = "En Pausa",
                        valor = conteoPausados.toString(),
                        icono = Icons.Default.PauseCircle,
                        accentColor = TokeWarning,
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ---- BÚSQUEDA LOCAL (Nodo Figma 1:390) ----
            val placeholderTexto = when (pestanaSeleccionada) {
                1 -> "Buscar código..."
                2 -> "Buscar solicitud..."
                else -> "Buscar en el equipo..."
            }

            OutlinedTextField(
                value = textoBusqueda,
                onValueChange = { textoBusqueda = it },
                placeholder = {
                    Text(
                        text = placeholderTexto,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (textoBusqueda.isNotEmpty()) {
                        IconButton(onClick = { textoBusqueda = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Limpiar"
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // ---- CONTENIDO SEGÚN PESTAÑA SELECCIONADA ----
            when (pestanaSeleccionada) {
                0 -> {
                    // Pestaña: Mi Equipo
                    val listaFiltrada = remember(trabajadores, textoBusqueda) {
                        if (textoBusqueda.isBlank()) trabajadores
                        else trabajadores.filter {
                            it.username.contains(textoBusqueda, ignoreCase = true)
                        }
                    }

                    if (listaFiltrada.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (textoBusqueda.isBlank()) "No hay trabajadores registrados en este equipo."
                                else "No se encontraron trabajadores con '$textoBusqueda'.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(
                                items = listaFiltrada,
                                key = { it.cuentaId }
                            ) { info ->
                                TrabajadorTarjetaEquilibrada(
                                    info = info,
                                    onPausarReanudar = {
                                        val nuevoEstado = if (info.estado == EstadoRelacion.ACTIVA) {
                                            EstadoRelacion.PAUSADA
                                        } else {
                                            EstadoRelacion.ACTIVA
                                        }
                                        businessViewModel.cambiarEstadoTrabajador(info.cuentaId, nuevoEstado)
                                    },
                                    onExpulsar = {
                                        trabajadorAExpulsar = info
                                    }
                                )
                            }
                        }
                    }
                }

                1 -> {
                    // Pestaña: Códigos de Invitación
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { mostrandoInvitarModal = !mostrandoInvitarModal },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (mostrandoInvitarModal) "Ocultar panel de invitación" else "+ Generar nuevo código"
                            )
                        }

                        if (mostrandoInvitarModal) {
                            InvitarTrabajadorScreen(
                                businessViewModel = businessViewModel,
                                onCerrar = { mostrandoInvitarModal = false }
                            )
                        }

                        Text(
                            text = "Códigos vigentes activos",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        val codigosFiltrados = remember(codigosActivos, textoBusqueda) {
                            if (textoBusqueda.isBlank()) codigosActivos
                            else codigosActivos.filter {
                                it.codigo.contains(textoBusqueda, ignoreCase = true)
                            }
                        }

                        if (codigosFiltrados.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No hay códigos de invitación activos.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(
                                    items = codigosFiltrados,
                                    key = { it.codigo }
                                ) { inv ->
                                    CodigoTarjeta(
                                        invitacion = inv,
                                        onRevocar = {
                                            businessViewModel.revocarCodigo(inv.codigo)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // Pestaña: Solicitudes de Ingreso
                    val solicitudesFiltradas = remember(solicitudes, textoBusqueda) {
                        if (textoBusqueda.isBlank()) solicitudes
                        else solicitudes.filter {
                            it.nombre.contains(textoBusqueda, ignoreCase = true) ||
                                    (it.codigoUsado?.contains(textoBusqueda, ignoreCase = true) == true)
                        }
                    }

                    if (solicitudesFiltradas.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No hay solicitudes de ingreso pendientes.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(
                                items = solicitudesFiltradas,
                                key = { it.cuentaId }
                            ) { sol ->
                                SolicitudTarjeta(
                                    solicitud = sol,
                                    onAceptar = {
                                        businessViewModel.responderSolicitud(sol.cuentaId, true)
                                    },
                                    onRechazar = {
                                        businessViewModel.responderSolicitud(sol.cuentaId, false)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Diálogo de confirmación para expulsar trabajador
    trabajadorAExpulsar?.let { info ->
        AlertDialog(
            onDismissRequest = { trabajadorAExpulsar = null },
            title = {
                Text(
                    text = "¿Expulsar a ${info.username}?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Esta acción desvinculará a ${info.username} de este negocio. El usuario ya no podrá ver los pagos recibidos ni operar en este equipo."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        businessViewModel.cambiarEstadoTrabajador(info.cuentaId, EstadoRelacion.EXPULSADA)
                        trabajadorAExpulsar = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Expulsar")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { trabajadorAExpulsar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

/** Tarjeta de Métrica (Activos / En Pausa - Node Figma 1:371) */
@Composable
private fun MetricSummaryCard(
    titulo: String,
    valor: String,
    icono: ImageVector,
    accentColor: Color,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(68.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    modifier = Modifier.size(34.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = accentColor.copy(alpha = 0.12f)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icono,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Text(
                    text = titulo,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = valor,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = accentColor
            )
        }
    }
}

/** Tarjeta de Miembro del Equipo (Variante Táctil Equilibrada - Node Figma 1:398 / 1:417) */
@Composable
private fun TrabajadorTarjetaEquilibrada(
    info: TrabajadorRealInfo,
    onPausarReanudar: () -> Unit,
    onExpulsar: () -> Unit
) {
    val esActivo = info.estado == EstadoRelacion.ACTIVA
    val esPausado = info.estado == EstadoRelacion.PAUSADA

    val badgeColor = when {
        esActivo -> TokeGreen
        esPausado -> TokeWarning
        else -> MaterialTheme.colorScheme.outline
    }

    val estadoTexto = when {
        esActivo -> "Activo"
        esPausado -> "En pausa"
        else -> info.estado.name
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Lado izquierdo: Avatar + Nombre + Estado
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                AvatarIniciales(
                    nombre = info.username,
                    tamano = 44.dp
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = info.username,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(badgeColor)
                        )
                        Text(
                            text = estadoTexto,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = badgeColor
                        )
                    }
                }
            }

            // Lado derecho: Botones rápidos (cuadriculados redondeados en pequeño)
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón Pausar / Reanudar
                IconButton(
                    onClick = onPausarReanudar,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            color = if (esActivo) TokeWarning.copy(alpha = 0.12f) else TokeGreen.copy(alpha = 0.12f)
                        )
                ) {
                    Icon(
                        imageVector = if (esActivo) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (esActivo) "Pausar" else "Reanudar",
                        tint = if (esActivo) TokeWarning else TokeGreen,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Botón Expulsar
                IconButton(
                    onClick = onExpulsar,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonOff,
                        contentDescription = "Expulsar",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/** Tarjeta de Código de Invitación Activo */
@Composable
private fun CodigoTarjeta(
    invitacion: InvitacionTrabajador,
    onRevocar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Key,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = invitacion.codigo,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            OutlinedButton(
                onClick = onRevocar,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(text = "Revocar", fontSize = 12.sp)
            }
        }
    }
}

/** Tarjeta de Solicitud de Ingreso Pendiente */
@Composable
private fun SolicitudTarjeta(
    solicitud: SolicitudUi,
    onAceptar: () -> Unit,
    onRechazar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AvatarIniciales(nombre = solicitud.nombre, tamano = 38.dp)
                Column {
                    Text(
                        text = solicitud.nombre,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    solicitud.codigoUsado?.let { cod ->
                        Text(
                            text = "Código usado: $cod",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onAceptar,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TokeGreen)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Aceptar")
                }

                OutlinedButton(
                    onClick = onRechazar,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Rechazar")
                }
            }
        }
    }
}
