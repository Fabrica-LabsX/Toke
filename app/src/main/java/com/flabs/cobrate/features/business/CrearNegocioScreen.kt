package com.flabs.cobrate.features.business

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flabs.cobrate.R
import com.flabs.cobrate.core.data.PreferenciasAppStore
import com.flabs.cobrate.core.logic.FeedbackManager
import com.flabs.cobrate.core.model.Negocio
import com.flabs.cobrate.core.model.TipoNegocio
import com.flabs.cobrate.ui.theme.ThemeMode
import kotlinx.coroutines.launch

private enum class PasoWizard {
    PASO_1_NOMBRE_Y_COLOR,
    PASO_2_TIPO_NEGOCIO,
    CONEXION_EXITOSA,
    PASO_3_FINALIZADO
}

private data class OpcionTipoNegocio(
    val titulo: String,
    val descripcion: String,
    val tipo: TipoNegocio,
    val icono: ImageVector
)

private data class ColorSwatch(
    val nombre: String,
    val hex: String,
    val color: Color
)

/**
 * Onboarding de Creación de Negocio conforme al diseño oficial de Figma (Ronda 12).
 * Nodos: 14:71 (Paso 1), 14:243 (Paso 2), 14:182 (Conexión Exitosa), 14:2 (Paso 3 Final).
 */
@Composable
fun CrearNegocioScreen(
    negocioViewModel: NegocioViewModel,
    onCreado: () -> Unit,
    onIrAMisQRs: (() -> Unit)? = null,
    onVolver: (() -> Unit)? = null
) {
    val scope = rememberCoroutineScope()
    var paso by rememberSaveable { mutableStateOf(PasoWizard.PASO_1_NOMBRE_Y_COLOR) }
    var procesando by remember { mutableStateOf(false) }

    // Datos del formulario
    var nombreComercial by rememberSaveable { mutableStateOf("") }
    var colorHexSeleccionado by rememberSaveable { mutableStateOf<String?>("#007A3E") }
    var tipoSeleccionado by rememberSaveable { mutableStateOf<TipoNegocio?>(null) }
    var negocioCreado by remember { mutableStateOf<Negocio?>(null) }

    val themeMode by PreferenciasAppStore.themeMode.collectAsState()
    val isDark = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    val bgScreen = if (isDark) Color(0xFF0D1117) else Color(0xFFEAF7EE)
    val cardBg = if (isDark) Color(0xFF1A1F26) else Color.White
    val cardBorder = if (isDark) Color(0xFF2D333B) else Color(0xFFE2F0E7)
    val titleColor = if (isDark) Color.White else Color(0xFF0B2316)
    val bodyTextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF4B6354)

    // Swatches de colores de marca oficiales de Figma
    val swatchesColor = remember {
        listOf(
            ColorSwatch("Verde Bosque", "#007A3E", Color(0xFF007A3E)),
            ColorSwatch("Azul Rey", "#1E40AF", Color(0xFF1E40AF)),
            ColorSwatch("Ámbar Cálido", "#D97706", Color(0xFFD97706)),
            ColorSwatch("Morado Toké", "#7E22CE", Color(0xFF7E22CE)),
            ColorSwatch("Coral Intenso", "#DC2626", Color(0xFFDC2626)),
            ColorSwatch("Verde Azulado", "#0D9488", Color(0xFF0D9488))
        )
    }

    // Opciones del Paso 2 (Tipo de Negocio)
    val opcionesTipo = remember {
        listOf(
            OpcionTipoNegocio("Bodega / Minimarket", "Abarrotes, bebidas y víveres", TipoNegocio.ABARROTES, Icons.Default.Storefront),
            OpcionTipoNegocio("Restaurante o Cafetería", "Menú del día, sangucherías y comidas", TipoNegocio.RESTAURANTE, Icons.Default.Restaurant),
            OpcionTipoNegocio("Marisquería & Cevichería", "Pescados, mariscos y comida marina", TipoNegocio.MARISQUERIA, Icons.Default.Restaurant),
            OpcionTipoNegocio("Tienda de ropa y calzado", "Prendas, accesorios y calzado", TipoNegocio.ROPA, Icons.Default.Checkroom),
            OpcionTipoNegocio("Electrohogar & Ferretería", "Tecnología, herramientas y repuestos", TipoNegocio.ELECTRODOMESTICOS, Icons.Default.Devices),
            OpcionTipoNegocio("Servicios y Oficios", "Peluquería, reparaciones y talleres", TipoNegocio.SERVICIOS, Icons.Default.Build),
            OpcionTipoNegocio("Otro tipo de negocio", "Personaliza tu propio rubro comercial", TipoNegocio.OTRO, Icons.Default.MoreHoriz)
        )
    }

    // Manejo de Back
    BackHandler(paso != PasoWizard.PASO_1_NOMBRE_Y_COLOR) {
        paso = when (paso) {
            PasoWizard.PASO_2_TIPO_NEGOCIO -> PasoWizard.PASO_1_NOMBRE_Y_COLOR
            PasoWizard.CONEXION_EXITOSA -> PasoWizard.PASO_2_TIPO_NEGOCIO
            PasoWizard.PASO_3_FINALIZADO -> PasoWizard.CONEXION_EXITOSA
            else -> paso
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgScreen)
    ) {
        when (paso) {
            // ---- PASO 1: NOMBRE Y COLOR ----
            PasoWizard.PASO_1_NOMBRE_Y_COLOR -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // TopBar Paso 1
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            onClick = { onVolver?.invoke() },
                            shape = CircleShape,
                            color = cardBg,
                            border = BorderStroke(1.dp, cardBorder),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Volver",
                                    tint = titleColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Registro de negocio",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = titleColor
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(width = 28.dp, height = 6.dp)
                                        .clip(CircleShape)
                                        .background(if (isDark) Color(0xFF00C853) else Color(0xFF007A3E))
                                )
                                Box(
                                    modifier = Modifier
                                        .size(width = 28.dp, height = 6.dp)
                                        .clip(CircleShape)
                                        .background(if (isDark) Color(0xFF2D333B) else Color(0xFFCCE8D6))
                                )
                                Box(
                                    modifier = Modifier
                                        .size(width = 28.dp, height = 6.dp)
                                        .clip(CircleShape)
                                        .background(if (isDark) Color(0xFF2D333B) else Color(0xFFCCE8D6))
                                )
                            }
                        }

                        Surface(
                            shape = CircleShape,
                            color = if (isDark) Color(0x1A00C853) else Color(0xFFDCF3E4),
                            border = BorderStroke(1.dp, if (isDark) Color(0x3300C853) else Color(0xFFC6EBD2))
                        ) {
                            Text(
                                text = "1 de 3",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color(0xFF00C853) else Color(0xFF146C36),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Contenido Paso 1
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Título de Sección
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "¿Cómo se llama tu negocio?",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = titleColor,
                                letterSpacing = (-0.6).sp
                            )
                            Text(
                                text = "Elige el nombre y color de marca que verá tu equipo.",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = bodyTextColor,
                                lineHeight = 20.sp
                            )
                        }

                        // Tarjeta 1: Nombre Comercial
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = cardBg,
                            border = BorderStroke(1.dp, cardBorder),
                            shadowElevation = if (isDark) 0.dp else 1.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(21.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Nombre comercial",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = titleColor
                                    )
                                    Surface(
                                        shape = CircleShape,
                                        color = if (isDark) Color(0x1A00C853) else Color(0xFFEAF7EE)
                                    ) {
                                        Text(
                                            text = "OBLIGATORIO",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF007A3E),
                                            letterSpacing = 0.55.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                OutlinedTextField(
                                    value = nombreComercial,
                                    onValueChange = { nombreComercial = it },
                                    placeholder = {
                                        Text("EJ: Mi Bodeguita", color = Color(0xFF94A3B8))
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(16.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF007A3E),
                                        unfocusedBorderColor = if (isDark) Color(0xFF2D333B) else Color(0xFFD5EAD9),
                                        focusedContainerColor = if (isDark) Color(0xFF12161A) else Color(0xFFF7FBF8),
                                        unfocusedContainerColor = if (isDark) Color(0xFF12161A) else Color(0xFFF7FBF8)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        // Decision 0.1: NO tipo chips in Paso 1

                        // Tarjeta 2: Color de tu Marca
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = cardBg,
                            border = BorderStroke(1.dp, cardBorder),
                            shadowElevation = if (isDark) 0.dp else 1.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(21.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Color de tu marca",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = titleColor
                                    )
                                    Text(
                                        text = "Identidad visual",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = bodyTextColor
                                    )
                                }

                                // Swatches de Color de Marca
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    swatchesColor.forEach { swatch ->
                                        val seleccionado = colorHexSeleccionado == swatch.hex
                                        Surface(
                                            onClick = {
                                                colorHexSeleccionado = if (seleccionado) null else swatch.hex
                                            },
                                            shape = CircleShape,
                                            color = swatch.color,
                                            modifier = Modifier.size(40.dp),
                                            border = if (seleccionado) BorderStroke(3.dp, Color.White) else null
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                if (seleccionado) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                // Vista Previa Directa
                                val colorPrevio = swatchesColor.find { it.hex == colorHexSeleccionado }?.color ?: Color(0xFF007A3E)
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isDark) Color(0xFF12161A) else Color(0xFFEAF7EE),
                                    border = BorderStroke(1.dp, if (isDark) Color(0xFF2D333B) else Color(0xFFD0ECD7)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(15.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = colorPrevio,
                                                modifier = Modifier.size(44.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = Icons.Default.Storefront,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }

                                            Column {
                                                Text(
                                                    text = "Vista previa del negocio",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = bodyTextColor
                                                )
                                                Text(
                                                    text = nombreComercial.ifBlank { "Mi Bodeguita" },
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = titleColor
                                                )
                                            }
                                        }

                                        Surface(
                                            shape = CircleShape,
                                            color = cardBg,
                                            border = BorderStroke(1.dp, if (isDark) Color(0xFF2D333B) else Color(0xFFD0ECD7))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .clip(CircleShape)
                                                        .background(colorPrevio)
                                                )
                                                Text(
                                                    text = "Activo",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = titleColor
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Botón Primario Paso 1
                        Surface(
                            onClick = {
                                if (nombreComercial.isNotBlank()) {
                                    paso = PasoWizard.PASO_2_TIPO_NEGOCIO
                                }
                            },
                            enabled = nombreComercial.isNotBlank(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = if (nombreComercial.isNotBlank()) (if (isDark) Color(0xFF00C853) else Color(0xFF007A3E)) else Color(0xFFE2E8F0)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Continuar al Paso 2",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (nombreComercial.isNotBlank()) (if (isDark) Color.Black else Color.White) else Color(0xFF94A3B8)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = if (nombreComercial.isNotBlank()) (if (isDark) Color.Black else Color.White) else Color(0xFF94A3B8),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // ---- PASO 2: TIPO DE NEGOCIO ----
            PasoWizard.PASO_2_TIPO_NEGOCIO -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // TopBar Paso 2
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            onClick = { paso = PasoWizard.PASO_1_NOMBRE_Y_COLOR },
                            shape = CircleShape,
                            color = cardBg,
                            border = BorderStroke(1.dp, cardBorder),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Volver",
                                    tint = titleColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "PASO 2 DE 3",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF007A3E),
                                letterSpacing = 0.65.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(width = 24.dp, height = 6.dp)
                                        .clip(CircleShape)
                                        .background(if (isDark) Color(0xFF00C853) else Color(0xFF007A3E))
                                )
                                Box(
                                    modifier = Modifier
                                        .size(width = 24.dp, height = 6.dp)
                                        .clip(CircleShape)
                                        .background(if (isDark) Color(0xFF00C853) else Color(0xFF007A3E))
                                )
                                Box(
                                    modifier = Modifier
                                        .size(width = 24.dp, height = 6.dp)
                                        .clip(CircleShape)
                                        .background(if (isDark) Color(0xFF2D333B) else Color(0xFFCFE7D6))
                                )
                            }
                        }

                        Box(modifier = Modifier.size(40.dp))
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Contenido Paso 2
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "¿Qué tipo de negocio tienes?",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = titleColor,
                                letterSpacing = (-0.6).sp
                            )
                            Text(
                                text = "Selecciona tu rubro para organizar tus cobros y ventas.",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = bodyTextColor,
                                lineHeight = 20.sp
                            )
                        }

                        // Lista de 6 Opciones
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            opcionesTipo.forEach { op ->
                                val seleccionado = tipoSeleccionado == op.tipo
                                Surface(
                                    onClick = { tipoSeleccionado = op.tipo },
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (seleccionado) (if (isDark) Color(0xFF1B221C) else Color(0xFFE8F7ED)) else cardBg,
                                    border = BorderStroke(
                                        width = if (seleccionado) 2.dp else 1.dp,
                                        color = if (seleccionado) (if (isDark) Color(0xFF00C853) else Color(0xFF007A3E)) else cardBorder
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = if (isDark) Color(0x1A00C853) else Color(0xFFF0FDF4),
                                                modifier = Modifier.size(48.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = op.icono,
                                                        contentDescription = null,
                                                        tint = if (isDark) Color(0xFF00C853) else Color(0xFF00A651),
                                                        modifier = Modifier.size(22.dp)
                                                    )
                                                }
                                            }

                                            Column {
                                                Text(
                                                    text = op.titulo,
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = titleColor
                                                )
                                                Text(
                                                    text = op.descripcion,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = bodyTextColor
                                                )
                                            }
                                        }

                                        if (seleccionado) {
                                            Surface(
                                                shape = CircleShape,
                                                color = if (isDark) Color(0xFF00C853) else Color(0xFF007A3E),
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.Transparent)
                                                    .border(2.dp, if (isDark) Color(0xFF2D333B) else Color(0xFFCFE7D6), CircleShape)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Botón Primario Continuar
                        Surface(
                            onClick = {
                                if (tipoSeleccionado != null && !procesando) {
                                    procesando = true
                                    scope.launch {
                                        FeedbackManager.setLoading(true, "Creando negocio...")
                                        val result = negocioViewModel.crearNegocio(
                                            nombre = nombreComercial,
                                            tipo = tipoSeleccionado!!,
                                            colorMarca = colorHexSeleccionado
                                        )
                                        FeedbackManager.setLoading(false)

                                        result.onSuccess { neg ->
                                            negocioCreado = neg
                                            FeedbackManager.showSuccess("¡Listo! Negocio creado.")
                                            paso = PasoWizard.CONEXION_EXITOSA
                                        }.onFailure { err ->
                                            FeedbackManager.showError(err)
                                            procesando = false
                                        }
                                    }
                                }
                            },
                            enabled = tipoSeleccionado != null && !procesando,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = CircleShape,
                            color = if (tipoSeleccionado != null) (if (isDark) Color(0xFF00C853) else Color(0xFF007A3E)) else Color(0xFFE2E8F0)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Continuar",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (tipoSeleccionado != null) (if (isDark) Color.Black else Color.White) else Color(0xFF94A3B8)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = if (tipoSeleccionado != null) (if (isDark) Color.Black else Color.White) else Color(0xFF94A3B8),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // ---- CONEXION EXITOSA (Intermedio) ----
            PasoWizard.CONEXION_EXITOSA -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Conexión Exitosa
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Conexión Exitosa",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = titleColor,
                            letterSpacing = (-0.425).sp
                        )
                        Surface(
                            shape = CircleShape,
                            color = if (isDark) Color(0xFF00C853) else Color(0xFF006B32),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    // Contenido Conexión Exitosa
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Hero Icon
                        Surface(
                            shape = CircleShape,
                            color = if (isDark) Color(0xFF00C853) else Color(0xFF008740),
                            shadowElevation = 8.dp,
                            modifier = Modifier.size(80.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(42.dp)
                                )
                            }
                        }

                        Text(
                            text = "¡TOKE está listo!",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = titleColor,
                            textAlign = TextAlign.Center,
                            letterSpacing = (-0.7).sp
                        )

                        Text(
                            text = "Tu cuenta ya está conectada a este negocio",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = bodyTextColor,
                            textAlign = TextAlign.Center
                        )

                        // Tarjeta Blanca Unificada
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = cardBg,
                            border = BorderStroke(1.dp, cardBorder),
                            shadowElevation = if (isDark) 0.dp else 2.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(21.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isDark) Color(0x1A00C853) else Color(0xFFE4F1E7),
                                    modifier = Modifier.size(56.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Storefront,
                                            contentDescription = null,
                                            tint = if (isDark) Color(0xFF00C853) else Color(0xFF00A651),
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = "Ya eres parte de:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = bodyTextColor
                                )

                                Text(
                                    text = negocioCreado?.nombre ?: nombreComercial,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = titleColor,
                                    letterSpacing = (-0.6).sp
                                )

                                // Decision 0.3: Únicamente 2 Acciones (Recibir Cobros y Entrar a TOKE - sin WA)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // Acción 1: Recibir cobros (Mis QRs)
                                    Surface(
                                        onClick = {
                                            onIrAMisQRs?.invoke() ?: onCreado()
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isDark) Color(0xFF12161A) else Color(0xCCEAF7ED),
                                        border = BorderStroke(1.dp, if (isDark) Color(0xFF2D333B) else Color(0x33BC3ABB)),
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(vertical = 4.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(13.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.QrCode,
                                                contentDescription = null,
                                                tint = titleColor,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "Recibir Cobros",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = titleColor
                                            )
                                        }
                                    }

                                    // Acción 2: Entrar a mi negocio (Paso 3)
                                    Surface(
                                        onClick = {
                                            paso = PasoWizard.PASO_3_FINALIZADO
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isDark) Color(0xFF12161A) else Color(0xCCEAF7ED),
                                        border = BorderStroke(1.dp, if (isDark) Color(0xFF2D333B) else Color(0x33BC3ABB)),
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(vertical = 4.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(13.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Storefront,
                                                contentDescription = null,
                                                tint = titleColor,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "Mi Negocio",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = titleColor
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Botón Entrar a TOKE
                        Surface(
                            onClick = onCreado,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = if (isDark) Color(0xFF00C853) else Color(0xFF006B32),
                            shadowElevation = 2.dp
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Entrar a TOKE",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // ---- PASO 3: FINALIZADO ----
            PasoWizard.PASO_3_FINALIZADO -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // TopBar Paso 3
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Paso 3 de 3",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = titleColor,
                                letterSpacing = (-0.35).sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(width = 28.dp, height = 6.dp)
                                        .clip(CircleShape)
                                        .background(if (isDark) Color(0xFF00C853) else Color(0xFF007A3E))
                                )
                                Box(
                                    modifier = Modifier
                                        .size(width = 28.dp, height = 6.dp)
                                        .clip(CircleShape)
                                        .background(if (isDark) Color(0xFF00C853) else Color(0xFF007A3E))
                                )
                                Box(
                                    modifier = Modifier
                                        .size(width = 28.dp, height = 6.dp)
                                        .clip(CircleShape)
                                        .background(if (isDark) Color(0xFF00C853) else Color(0xFF007A3E))
                                )
                            }
                        }
                    }

                    // Contenido Paso 3
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (isDark) Color(0x1A00C853) else Color(0xFFD8F2E1),
                            border = BorderStroke(1.dp, if (isDark) Color(0x3300C853) else Color(0xFFB8E4C7))
                        ) {
                            Text(
                                text = "PASO FINAL · ¡TODO LISTO!",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF007A3E),
                                letterSpacing = 0.6.sp,
                                modifier = Modifier.padding(horizontal = 15.dp, vertical = 5.dp)
                            )
                        }

                        // Hero Checkmark
                        Surface(
                            shape = CircleShape,
                            color = if (isDark) Color(0xFF00C853) else Color(0xFF007A3E),
                            shadowElevation = 8.dp,
                            modifier = Modifier.size(96.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        }

                        Text(
                            text = "¡Tu negocio está listo!",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = titleColor,
                            textAlign = TextAlign.Center,
                            letterSpacing = (-0.6).sp
                        )

                        Text(
                            text = "Tu negocio ya está preparado para recibir y organizar tus pagos al instante.",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = bodyTextColor,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.widthIn(max = 320.dp)
                        )

                        // Tarjeta Resumen
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = cardBg,
                            border = BorderStroke(1.dp, cardBorder),
                            shadowElevation = if (isDark) 0.dp else 1.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(17.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (isDark) Color(0x1A00C853) else Color(0xFFE8F7ED),
                                            modifier = Modifier.size(48.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.Storefront,
                                                    contentDescription = null,
                                                    tint = if (isDark) Color(0xFF00C853) else Color(0xFF00A651),
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }
                                        }

                                        Column {
                                            Text(
                                                text = negocioCreado?.nombre ?: nombreComercial,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = titleColor
                                            )
                                            Text(
                                                text = tipoSeleccionado?.name ?: "Bodega & Abarrotes",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = bodyTextColor
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = CircleShape,
                                        color = if (isDark) Color(0x1A00C853) else Color(0xFFE8F7ED),
                                        border = BorderStroke(1.dp, if (isDark) Color(0x3300C853) else Color(0xFFC4E9CE))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 5.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF007A3E))
                                            )
                                            Text(
                                                text = "Modo Dueño",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF146C36)
                                            )
                                        }
                                    }
                                }

                                // Servicios Activos
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isDark) Color(0xFF12161A) else Color(0xFFF4FAF6),
                                    border = BorderStroke(1.dp, cardBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(13.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "SERVICIOS TOKE ACTIVOS",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = bodyTextColor,
                                            letterSpacing = 0.55.sp
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color(0xFF00A651),
                                                modifier = Modifier.size(15.dp)
                                            )
                                            Text(
                                                text = "Alertas instantaneas para Yape y Plin",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = titleColor
                                            )
                                        }
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color(0xFF00A651),
                                                modifier = Modifier.size(15.dp)
                                            )
                                            Text(
                                                text = "Notificaciones de voz automáticas",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = titleColor
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Botón Final "Entrar a mi negocio"
                        Surface(
                            onClick = onCreado,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = CircleShape,
                            color = if (isDark) Color(0xFF00C853) else Color(0xFF007A3E),
                            shadowElevation = 4.dp
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Entrar a mi negocio",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color.Black else Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = if (isDark) Color.Black else Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}
