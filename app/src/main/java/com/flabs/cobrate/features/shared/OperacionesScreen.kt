package com.flabs.cobrate.features.shared

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flabs.cobrate.R
import com.flabs.cobrate.core.data.PreferenciasAppStore
import com.flabs.cobrate.core.model.EstadoOperacion
import com.flabs.cobrate.core.model.Operacion
import com.flabs.cobrate.core.model.TipoMetodoCobro
import com.flabs.cobrate.ui.theme.ThemeMode
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Pantalla unificada de "Operaciones" (Figma Nodos 8:2, 8:119, 8:235, 8:494).
 *
 * Muestra el feed de operaciones con filtros por estado, búsqueda,
 * rango de tiempo y billetera/método de cobro.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OperacionesScreen(
    operaciones: List<Operacion>,
    onBack: (() -> Unit)? = null,
    onReclamarOperacion: ((String) -> Unit)? = null,
    onSeleccionarOperacion: ((String) -> Unit)? = null
) {
    val themeMode by PreferenciasAppStore.themeMode.collectAsState()
    val isDark = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    // Punto 8: Por defecto "Todas" seleccionada (pestanaSeleccionada = 0)
    var pestanaSeleccionada by remember { mutableIntStateOf(0) } // 0: Todas, 1: Pendientes
    var textoBusqueda by remember { mutableStateOf("") }

    // Punto 5: Por defecto "10 min" seleccionado (rangoSeleccionado = 0)
    var rangoSeleccionado by remember { mutableIntStateOf(0) } // 0: 10 min, 1: 30 min, 2: 60 min, 3: 1h, 4: Personalizado

    // Punto 9: Sin selección por defecto en la billetera (billeteraFiltro = null)
    var billeteraFiltro by remember { mutableStateOf<TipoMetodoCobro?>(null) } // null = todas

    // Conteo de pendientes
    val pendientesTotales = remember(operaciones) {
        operaciones.count { it.estado == EstadoOperacion.SIN_ASIGNAR }
    }

    // Punto 4: Identificar cuáles medios tienen operaciones el día de hoy
    val medosPresentesHoy = remember(operaciones) {
        val inicioHoy = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        operaciones
            .filter { it.fechaHora >= inicioHoy }
            .mapNotNull { it.metodoPago }
            .toSet()
    }

    val billeterasTodas = listOf(
        Triple(TipoMetodoCobro.YAPE, "Yape", R.drawable.ic_pago_yape),
        Triple(TipoMetodoCobro.PLIN, "Plin", R.drawable.ic_pago_plin),
        Triple(TipoMetodoCobro.BCP, "BCP", R.drawable.ic_pago_bcp),
        Triple(TipoMetodoCobro.INTERBANK, "Interbank", R.drawable.ic_pago_interbank)
    )

    // Punto 4: Solo medios con operaciones del día. Si no hay ninguno, queda vacía.
    val billeterasVisibles = remember(medosPresentesHoy) {
        billeterasTodas.filter { (tipo, _, _) -> tipo in medosPresentesHoy }
    }

    // Filtrado de operaciones
    val operacionesFiltradas = remember(
        operaciones,
        pestanaSeleccionada,
        textoBusqueda,
        rangoSeleccionado,
        billeteraFiltro
    ) {
        val ahora = System.currentTimeMillis()
        val limiteTiempo = when (rangoSeleccionado) {
            0 -> ahora - (10 * 60 * 1000L)
            1 -> ahora - (30 * 60 * 1000L)
            2 -> ahora - (60 * 60 * 1000L)
            3 -> ahora - (60 * 60 * 1000L)
            else -> 0L
        }

        operaciones.filter { op ->
            // Filtro por pestaña
            val cumplePestana = if (pestanaSeleccionada == 1) {
                op.estado == EstadoOperacion.SIN_ASIGNAR
            } else true

            // Filtro por búsqueda
            val cumpleBusqueda = if (textoBusqueda.isBlank()) true else {
                val query = textoBusqueda.trim().lowercase()
                val montoStr = "%.2f".format(Locale.US, op.monto)
                val idStr = op.id.lowercase()
                montoStr.contains(query) || idStr.contains(query)
            }

            // Filtro por tiempo
            val cumpleTiempo = if (limiteTiempo > 0) op.fechaHora >= limiteTiempo else true

            // Filtro por billetera
            val cumpleBilletera = if (billeteraFiltro != null) op.metodoPago == billeteraFiltro else true

            cumplePestana && cumpleBusqueda && cumpleTiempo && cumpleBilletera
        }.sortedByDescending { it.fechaHora }
    }

    val listaPendientes = remember(operacionesFiltradas) {
        operacionesFiltradas.filter { it.estado == EstadoOperacion.SIN_ASIGNAR }
    }

    val listaCompletados = remember(operacionesFiltradas) {
        operacionesFiltradas.filter { it.estado != EstadoOperacion.SIN_ASIGNAR }
    }

    // Colores adaptativos según tema
    val bgScreen = if (isDark) Color(0xFF0D1117) else Color(0xFFECFDF5)
    val topBarBg = if (isDark) Color(0xFF0D1117) else Color.White
    val topBarBorder = if (isDark) Color(0xFF2D333B) else Color(0xFFE5E7EB)
    val textTitle = if (isDark) Color.White else Color(0xFF111827)
    val textSubtitle = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)

    Scaffold(
        containerColor = bgScreen,
        topBar = {
            Surface(
                color = topBarBg,
                border = BorderStroke(1.dp, topBarBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Punto 7: Botón atrás (←) siempre visible cuando hay manejador onBack
                        if (onBack != null) {
                            Surface(
                                onClick = onBack,
                                shape = CircleShape,
                                color = Color.Transparent,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Volver atrás",
                                        tint = textTitle,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = "Operaciones",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = textTitle,
                            letterSpacing = (-0.5).sp
                        )
                    }

                    // Punto 1: Eliminar avatar "T!" arriba a la derecha (no se muestra nada)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            // ---- 2. FILTRO DE ESTADO (Todas / Pendientes) ----
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isDark) Color(0xFF1A1F26) else Color.White,
                border = BorderStroke(1.dp, if (isDark) Color(0xFF2D333B) else Color(0xFFE5E7EB)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(5.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Pestaña "Todas" (seleccionada por defecto)
                    Surface(
                        onClick = { pestanaSeleccionada = 0 },
                        shape = RoundedCornerShape(12.dp),
                        color = if (pestanaSeleccionada == 0) Color(0xFF00A651) else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "Todas",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (pestanaSeleccionada == 0) Color.White else textSubtitle
                            )
                        }
                    }

                    // Pestaña "Pendientes"
                    Surface(
                        onClick = { pestanaSeleccionada = 1 },
                        shape = RoundedCornerShape(12.dp),
                        color = if (pestanaSeleccionada == 1) Color(0xFFFEF3C7) else Color(0xFFFEF3C7).copy(alpha = 0.6f),
                        border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Pendientes",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF78350F)
                            )
                            Spacer(modifier = Modifier.width(8.dp))

                            // Punto 10: Contador legible con estilo amarillo y alineación centrada
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFF59E0B),
                                modifier = Modifier.size(22.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Text(
                                        text = "$pendientesTotales",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        textAlign = TextAlign.Center,
                                        style = TextStyle(
                                            platformStyle = PlatformTextStyle(includeFontPadding = false),
                                            lineHeight = 12.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ---- 3. BUSCADOR (Punto 2: Placeholder completo y legible) ----
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isDark) Color(0xFF1A1F26) else Color.White,
                border = BorderStroke(1.dp, if (isDark) Color(0xFF2D333B) else Color(0xFFE5E7EB)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar",
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        if (textoBusqueda.isEmpty()) {
                            Text(
                                text = "Buscar por cliente o monto...",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF9CA3AF),
                                maxLines = 1
                            )
                        }
                        BasicTextField(
                            value = textoBusqueda,
                            onValueChange = { textoBusqueda = it },
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isDark) Color.White else Color(0xFF111827)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // ---- 4. RANGO DE TIEMPO (Punto 5 & 6: 10 min por defecto y fila completa scrollable) ----
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RANGO DE TIEMPO",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = textSubtitle,
                        letterSpacing = 0.55.sp
                    )
                    Text(
                        text = "Hoy",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF00A651)
                    )
                }

                val opcionesTiempo = listOf("10 min", "30 min", "60 min", "1h", "Personalizado")
                LazyRow(
                    contentPadding = PaddingValues(end = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(opcionesTiempo.size) { index ->
                        val seleccionado = rangoSeleccionado == index
                        Surface(
                            onClick = { rangoSeleccionado = index },
                            shape = RoundedCornerShape(8.dp),
                            color = if (seleccionado) Color(0xFF00A651) else if (isDark) Color(0xFF1A1F26) else Color.White,
                            border = BorderStroke(1.dp, if (seleccionado) Color(0xFF00A651) else if (isDark) Color(0xFF2D333B) else Color(0xFFE5E7EB))
                        ) {
                            Text(
                                text = opcionesTiempo[index],
                                fontSize = 12.sp,
                                fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.SemiBold,
                                color = if (seleccionado) Color.White else if (isDark) Color(0xFFD4D4D4) else Color(0xFF4B5563),
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
            }

            // ---- 5. BILLETERA (Punto 3: Logos más grandes; Punto 4: Solo medios con operaciones hoy; Punto 9: Sin selección por defecto) ----
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "BILLETERA",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = textSubtitle,
                    letterSpacing = 0.55.sp,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )

                if (billeterasVisibles.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        billeterasVisibles.forEach { (tipo, nombre, resId) ->
                            val estaSeleccionada = billeteraFiltro == tipo
                            Surface(
                                onClick = {
                                    billeteraFiltro = if (estaSeleccionada) null else tipo
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(76.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = if (isDark) Color(0xFF1A1F26) else Color.White,
                                border = BorderStroke(
                                    width = if (estaSeleccionada) 2.dp else 1.dp,
                                    color = if (estaSeleccionada) Color(0xFF00A651) else if (isDark) Color(0xFF2D333B) else Color(0xFFE5E7EB)
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(vertical = 6.dp, horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    // Punto 3: Logos más grandes (40dp) y sin fondo de color
                                    Image(
                                        painter = painterResource(id = resId),
                                        contentDescription = nombre,
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = nombre,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) Color.White else Color(0xFF374151)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Punto 4: Si no hay medios con operaciones hoy, la fila queda vacía
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            // ---- 6 & 7. CONTENIDO (Vacío vs Con Datos) ----
            if (operacionesFiltradas.isEmpty()) {
                // ---- 6. ESTADO VACÍO ----
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isDark) Color(0xFF1A1F26) else Color.White,
                    border = BorderStroke(1.dp, if (isDark) Color(0xFF2D333B) else Color(0xFFE2E8F0)),
                    shadowElevation = 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "¿Nada llega aún? Asegúrate de que la red sea estable.",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = textSubtitle,
                            lineHeight = 22.sp,
                            modifier = Modifier.widthIn(max = 280.dp)
                        )
                    }
                }
            } else {
                // ---- 7. CON DATOS ----
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // ---- SECCIÓN 1: POR ASIGNAR O PENDIENTES ----
                    if (listaPendientes.isNotEmpty()) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isDark) Color(0xFF1E232B) else Color.White,
                                border = BorderStroke(1.dp, if (isDark) Color(0x66F59E0B) else Color(0xCCFDE68A)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(15.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Encabezado de Pendientes
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Schedule,
                                                contentDescription = null,
                                                tint = Color(0xFFD97706),
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Text(
                                                text = "Por asignar o pendientes",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = textTitle,
                                                letterSpacing = (-0.35).sp
                                            )
                                        }

                                        Surface(
                                            shape = CircleShape,
                                            color = Color(0xFFF59E0B),
                                            modifier = Modifier.size(20.dp)
                                        ) {
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier.fillMaxSize()
                                            ) {
                                                Text(
                                                    text = "${listaPendientes.size}",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color.White,
                                                    textAlign = TextAlign.Center,
                                                    style = TextStyle(
                                                        platformStyle = PlatformTextStyle(includeFontPadding = false),
                                                        lineHeight = 12.sp
                                                    )
                                                )
                                            }
                                        }
                                    }

                                    // Lista de Tarjetas de Cobro Pendiente
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        listaPendientes.forEach { op ->
                                            TarjetaOperacionPendiente(
                                                operacion = op,
                                                isDark = isDark,
                                                onAsignarClick = {
                                                    onReclamarOperacion?.invoke(op.id)
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ---- SECCIÓN 2: COBROS RECIBIDOS ----
                    if (listaCompletados.isNotEmpty()) {
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                // Encabezado de Completados
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Cobros recibidos",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textTitle
                                    )
                                    Text(
                                        text = "${listaCompletados.size} completados",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = textSubtitle
                                    )
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listaCompletados.forEach { op ->
                                        TarjetaOperacionCompletada(
                                            operacion = op,
                                            isDark = isDark,
                                            onClick = {
                                                onSeleccionarOperacion?.invoke(op.id)
                                            }
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
}

@Composable
private fun TarjetaOperacionPendiente(
    operacion: Operacion,
    isDark: Boolean,
    onAsignarClick: () -> Unit
) {
    val resIdLogo = when (operacion.metodoPago) {
        TipoMetodoCobro.YAPE -> R.drawable.ic_pago_yape
        TipoMetodoCobro.PLIN -> R.drawable.ic_pago_plin
        TipoMetodoCobro.BCP -> R.drawable.ic_pago_bcp
        TipoMetodoCobro.INTERBANK -> R.drawable.ic_pago_interbank
        else -> R.drawable.ic_pago_yape
    }

    val metodoNombre = operacion.metodoPago?.name?.lowercase()
        ?.replaceFirstChar { it.uppercase() } ?: "Pago"
    val horaRelativa = formatearTiempoRelativo(operacion.fechaHora)

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isDark) Color(0xFF1A1F26) else Color.White,
        border = BorderStroke(1.dp, if (isDark) Color(0xFF2D333B) else Color(0xFFE5E7EB)),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(13.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Image(
                        painter = painterResource(id = resIdLogo),
                        contentDescription = metodoNombre,
                        modifier = Modifier.size(36.dp)
                    )
                    Column {
                        Text(
                            text = "Op. #${operacion.id.takeLast(6)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else Color(0xFF111827)
                        )
                        Text(
                            text = "$metodoNombre · $horaRelativa",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF6B7280)
                        )
                    }
                }

                Text(
                    text = "S/ %.2f".format(Locale.US, operacion.monto),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isDark) Color.White else Color(0xFF111827)
                )
            }

            // Botón Asignar Pago
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Surface(
                    onClick = onAsignarClick,
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF00A651)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Asignar Pago",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaOperacionCompletada(
    operacion: Operacion,
    isDark: Boolean,
    onClick: () -> Unit
) {
    val resIdLogo = when (operacion.metodoPago) {
        TipoMetodoCobro.YAPE -> R.drawable.ic_pago_yape
        TipoMetodoCobro.PLIN -> R.drawable.ic_pago_plin
        TipoMetodoCobro.BCP -> R.drawable.ic_pago_bcp
        TipoMetodoCobro.INTERBANK -> R.drawable.ic_pago_interbank
        else -> R.drawable.ic_pago_yape
    }

    val metodoNombre = operacion.metodoPago?.name?.lowercase()
        ?.replaceFirstChar { it.uppercase() } ?: "Pago"
    val horaTexto = formatearHora(operacion.fechaHora)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isDark) Color(0xFF1A1F26) else Color.White,
        border = BorderStroke(1.dp, if (isDark) Color(0xFF2D333B) else Color(0xFFE5E7EB)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(13.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Image(
                    painter = painterResource(id = resIdLogo),
                    contentDescription = metodoNombre,
                    modifier = Modifier.size(36.dp)
                )
                Column {
                    Text(
                        text = "Op. #${operacion.id.takeLast(6)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Color(0xFF111827)
                    )
                    Text(
                        text = "$metodoNombre · $horaTexto",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF6B7280)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "S/ %.2f".format(Locale.US, operacion.monto),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isDark) Color.White else Color(0xFF111827)
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color(0xFF6B7280),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

private fun formatearHora(fechaHora: Long): String {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = fechaHora
    }
    val formatter = SimpleDateFormat("hh:mm a", Locale("es", "PE"))
    return formatter.format(calendar.time)
}

private fun formatearTiempoRelativo(fechaHora: Long): String {
    val diffMs = System.currentTimeMillis() - fechaHora
    val diffMin = (diffMs / (1000 * 60)).coerceAtLeast(1)
    return when {
        diffMin < 60 -> "Hace $diffMin min"
        else -> "Hace ${diffMin / 60} h"
    }
}
