package com.flabs.cobrate.features.shared

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flabs.cobrate.components.IconoMetodoPago
import com.flabs.cobrate.core.data.EventoDeNotificacionStore
import com.flabs.cobrate.core.data.InMemoryOperationStore
import com.flabs.cobrate.core.data.RelacionCuentaNegocioStore
import com.flabs.cobrate.core.model.EstadoOperacion
import com.flabs.cobrate.core.model.EstadoRelacion
import com.flabs.cobrate.core.model.Operacion
import com.flabs.cobrate.core.model.OrigenOperacion
import com.flabs.cobrate.core.model.RolCuentaNegocio
import com.flabs.cobrate.core.model.TipoMetodoCobro
import com.flabs.cobrate.features.business.BusinessViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Pantalla de Detalle de Operación conforme al contrato visual de Figma (Nodo 15:4199 claro / 15:4080 oscuro).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleOperacionScreen(
    operacionId: String,
    businessViewModel: BusinessViewModel,
    onBack: () -> Unit,
    onVolverInicio: () -> Unit,
) {
    val operaciones by InMemoryOperationStore.operaciones.collectAsState()
    val trabajadoresReales by businessViewModel.trabajadoresReales.collectAsState()
    val relaciones by RelacionCuentaNegocioStore.relaciones.collectAsState()
    val eventosNotificacion by EventoDeNotificacionStore.eventos.collectAsState()

    val operacion = remember(operaciones, operacionId) {
        operaciones.find { it.id == operacionId }
    }

    if (operacion == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Detalle de la operación", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                        }
                    }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Operación no encontrada",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    // Regla 7: Nombre del pagador desde la notificación
    val eventoNotif = remember(operacion, eventosNotificacion) {
        (operacion.origen as? OrigenOperacion.Detectada)?.eventoId?.let { id ->
            eventosNotificacion.find { it.id == id }
        }
    }

    val nombrePagador = remember(eventoNotif) {
        eventoNotif?.titulo?.trim()?.takeIf { it.isNotBlank() } ?: "Cliente Toke"
    }

    // Regla 9: Contar trabajadores ACTIVOS del negocio
    val trabajadoresActivos = remember(relaciones, operacion.negocioId) {
        relaciones.filter {
            it.negocioId == operacion.negocioId &&
                it.rol == RolCuentaNegocio.TRABAJADOR &&
                it.estado == EstadoRelacion.ACTIVA
        }
    }

    val mostrarFilaTrabajador = remember(operacion.trabajadorAsignadoId, trabajadoresActivos) {
        operacion.trabajadorAsignadoId != null && trabajadoresActivos.size > 1
    }

    val nombreTrabajadorAsignado = remember(operacion.trabajadorAsignadoId, trabajadoresReales) {
        trabajadoresReales.find { it.cuentaId == operacion.trabajadorAsignadoId }?.username
            ?: "Trabajador asignado"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de la operación", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                color = MaterialTheme.colorScheme.background
            ) {
                Button(
                    onClick = onVolverInicio,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = "Volver al inicio",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // 1. Sección de Monto Protagonista + Badge
            Text(
                text = "S/ %.2f".format(Locale.US, operacion.monto),
                style = MaterialTheme.typography.displayMedium.copy(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Badge de Estado "Recibido"
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (operacion.estado == EstadoOperacion.SIN_ASIGNAR) "Sin asignar" else "Recibido",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Tarjeta de Origen / Método de Pago (Card 1)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val avatarColor = when (operacion.metodoPago) {
                        TipoMetodoCobro.YAPE -> Color(0xFF742284)
                        TipoMetodoCobro.PLIN -> Color(0xFF00B4D8)
                        TipoMetodoCobro.BCP -> Color(0xFF002A8F)
                        TipoMetodoCobro.INTERBANK -> Color(0xFF05BE50)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }

                    Surface(
                        modifier = Modifier.size(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = avatarColor
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            if (operacion.metodoPago != null) {
                                IconoMetodoPago(tipo = operacion.metodoPago, tamano = 28.dp)
                            } else {
                                Text(
                                    text = "?",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Información de pago",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        val metodoNombre = operacion.metodoPago?.name?.lowercase()
                            ?.replaceFirstChar { it.uppercase() } ?: "Pago sin identificar"
                        Text(
                            text = "Método de pago: $metodoNombre",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "A nombre de: $nombrePagador",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Tarjeta de Metadatos de la Operación (Card 2)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "INFORMACIÓN DE LA OPERACIÓN",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Fila Fecha
                    MetadatoRow(
                        icon = Icons.Default.CalendarToday,
                        label = "Fecha",
                        value = formatearFechaDetalle(operacion.fechaHora)
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                    )

                    // Fila Hora
                    MetadatoRow(
                        icon = Icons.Default.AccessTime,
                        label = "Hora",
                        value = formatearHoraDetalle(operacion.fechaHora)
                    )

                    // Fila Trabajador Asociado (CONDICIONAL - Regla 7)
                    if (mostrarFilaTrabajador) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                        )
                        MetadatoRow(
                            icon = Icons.Default.Person,
                            label = "Trabajador",
                            value = nombreTrabajadorAsignado
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                    )

                    // Fila ID Operación (Regla 5)
                    MetadatoRow(
                        icon = Icons.AutoMirrored.Filled.ReceiptLong,
                        label = "ID Operación",
                        value = operacion.id.take(12)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun MetadatoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun formatearFechaDetalle(fechaHora: Long): String {
    val cal = Calendar.getInstance().apply { timeInMillis = fechaHora }
    val sdf = SimpleDateFormat("dd 'de' MMMM 'de' yyyy", Locale("es", "PE"))
    return sdf.format(cal.time)
}

private fun formatearHoraDetalle(fechaHora: Long): String {
    val cal = Calendar.getInstance().apply { timeInMillis = fechaHora }
    val sdf = SimpleDateFormat("hh:mm a", Locale("es", "PE"))
    return sdf.format(cal.time)
}
