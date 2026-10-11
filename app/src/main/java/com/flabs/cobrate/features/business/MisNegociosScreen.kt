package com.flabs.cobrate.features.business

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flabs.cobrate.components.resolverNombreDueno
import com.flabs.cobrate.core.data.CuentaStore
import com.flabs.cobrate.core.data.NegocioStore
import com.flabs.cobrate.core.data.PreferenciasAppStore
import com.flabs.cobrate.core.model.EstadoRelacion
import com.flabs.cobrate.core.model.Negocio
import com.flabs.cobrate.core.model.RelacionCuentaNegocio
import com.flabs.cobrate.core.model.RolCuentaNegocio
import com.flabs.cobrate.ui.theme.ThemeMode

/**
 * Pantalla "Mis Negocios" (Figma Nodos 1:210 y 1:314).
 *
 * Muestra la vista detallada de todos los negocios de la cuenta,
 * dividiendo de forma estricta "Negocios donde eres dueño" y
 * "Negocios donde eres trabajador".
 */
@Composable
fun MisNegociosScreen(
    negocioViewModel: NegocioViewModel,
    onVolver: () -> Unit,
    onCrearNegocio: () -> Unit,
    onUnirmeNegocio: () -> Unit
) {
    val todosLosNegocios by NegocioStore.negocios.collectAsState()
    val misRelaciones by negocioViewModel.misRelaciones.collectAsState()
    val negocioActivoId by negocioViewModel.negocioSeleccionadoId.collectAsState()
    val sesionActual by CuentaStore.sesionActual.collectAsState()
    val cuentasCheadas by CuentaStore.cuentas.collectAsState()
    val currentUid = sesionActual?.id

    val themeMode by PreferenciasAppStore.themeMode.collectAsState()
    val isDark = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    // REGLA 1: "Negocios donde eres dueño": lista negocios donde negocio.dueñoCuentaId == uid.
    // No usa relaciones para esta sección.
    val negociosDueno = remember(todosLosNegocios, currentUid) {
        if (currentUid == null) emptyList()
        else todosLosNegocios.filter { it.dueñoCuentaId == currentUid }
    }

    // REGLA 2 & 3: "Negocios donde eres trabajador": lista relaciones de la cuenta con rol == TRABAJADOR,
    // estado en ACTIVA, PAUSADA o SOLICITADA, y negocio.dueñoCuentaId != uid.
    // Oculta la sección si queda vacía. Ningún negocio aparece en ambas secciones.
    val relacionesTrabajador = remember(misRelaciones, todosLosNegocios, currentUid) {
        if (currentUid == null) emptyList()
        else misRelaciones.filter { rel ->
            if (rel.cuentaId != currentUid) return@filter false
            if (rel.rol != RolCuentaNegocio.TRABAJADOR) return@filter false
            if (rel.estado != EstadoRelacion.ACTIVA &&
                rel.estado != EstadoRelacion.PAUSADA &&
                rel.estado != EstadoRelacion.SOLICITADA) return@filter false

            val neg = todosLosNegocios.find { it.id == rel.negocioId }
            neg == null || neg.dueñoCuentaId != currentUid
        }
    }

    // Colores adaptativos de pantalla
    val bgScreen = if (isDark) Color(0xFF0D1117) else Color(0xFFF4FBF6)
    val topBarBg = if (isDark) Color(0xFF0D1117) else Color.White
    val topBarBorder = if (isDark) Color(0xFF2D333B) else Color(0xFFE2E8F0)
    val backBtnBg = if (isDark) Color(0xFF1A1F26) else Color(0xFFF8FAFC)
    val titleColor = if (isDark) Color.White else Color(0xFF0F172A)
    val subtitleColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

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
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        onClick = onVolver,
                        shape = RoundedCornerShape(12.dp),
                        color = backBtnBg,
                        border = BorderStroke(1.dp, topBarBorder),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver",
                                tint = titleColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Mis Negocios",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = titleColor
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Comercios donde colaboras",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = subtitleColor
                        )
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                color = topBarBg,
                border = BorderStroke(1.dp, topBarBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Botón Primario
                    Surface(
                        onClick = onCrearNegocio,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = if (isDark) Color(0xFF00C853) else Color(0xFF00A651),
                        shadowElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = if (isDark) Color.Black else Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Crear nuevo negocio",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.Black else Color.White
                            )
                        }
                    }

                    // Botón Secundario
                    Surface(
                        onClick = onUnirmeNegocio,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = if (isDark) Color(0xFF1A1F26) else Color.White,
                        border = BorderStroke(1.dp, if (isDark) Color(0xFF2D333B) else Color(0xFFE2E8F0)),
                        shadowElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = null,
                                tint = titleColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Tengo un código de invitación",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = titleColor
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ---- SECCIÓN 1: NEGOCIOS DONDE ERES DUEÑO ----
            if (negociosDueno.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Encabezado de sección
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(width = 6.5.dp, height = 8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF00A651))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "NEGOCIOS DONDE ERES DUEÑO",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color(0xFFD4D4D4) else Color(0xFF64748B),
                                    letterSpacing = 0.6.sp
                                )
                            }
                            // REGLA 5: Contador exacto
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isDark) Color(0x1A00A651) else Color(0xFFDCFCE7),
                                border = BorderStroke(1.dp, if (isDark) Color(0x3300A651) else Color(0x4000A651))
                            ) {
                                Text(
                                    text = "${negociosDueno.size} propio(s)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF00A651),
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                                )
                            }
                        }

                        // Tarjetas de negocio propio
                        negociosDueno.forEach { negocio ->
                            val esActivo = negocio.id == negocioActivoId
                            TarjetaNegocioDueno(
                                negocio = negocio,
                                esActivo = esActivo,
                                isDark = isDark,
                                onClick = {
                                    negocioViewModel.seleccionarNegocio(negocio.id)
                                    onVolver()
                                }
                            )
                        }
                    }
                }
            }

            // ---- SECCIÓN 2: NEGOCIOS DONDE ERES TRABAJADOR ----
            if (relacionesTrabajador.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        val vinculados = relacionesTrabajador.count { it.estado == EstadoRelacion.ACTIVA }
                        val enEspera = relacionesTrabajador.count { it.estado == EstadoRelacion.SOLICITADA }
                        val pausados = relacionesTrabajador.count { it.estado == EstadoRelacion.PAUSADA }

                        // Encabezado de sección
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(width = 6.5.dp, height = 8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF00A651))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "NEGOCIOS DONDE ERES",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) Color(0xFFD4D4D4) else Color(0xFF64748B),
                                        letterSpacing = 0.6.sp
                                    )
                                    Text(
                                        text = "TRABAJADOR",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) Color(0xFFD4D4D4) else Color(0xFF64748B),
                                        letterSpacing = 0.6.sp
                                    )
                                }
                            }

                            // REGLA 5: Contadores exactos
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                if (vinculados > 0) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isDark) Color(0x1A00A651) else Color(0xFFDCFCE7),
                                        border = BorderStroke(1.dp, if (isDark) Color(0x3300A651) else Color(0x4000A651))
                                    ) {
                                        Text(
                                            text = "$vinculados vinculado(s)",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF00A651),
                                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                                if (enEspera > 0) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isDark) Color(0x1AFBBF24) else Color(0xFFFEF3C7),
                                        border = BorderStroke(1.dp, if (isDark) Color(0x33FBBF24) else Color(0x4D245158))
                                    ) {
                                        Text(
                                            text = "$enEspera en espera",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706),
                                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                                if (pausados > 0) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isDark) Color(0x1AFBBF24) else Color(0xFFFEF3C7),
                                        border = BorderStroke(1.dp, if (isDark) Color(0x33FBBF24) else Color(0x4D245158))
                                    ) {
                                        Text(
                                            text = "$pausados pausado(s)",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706),
                                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Tarjetas de trabajador
                        relacionesTrabajador.forEach { rel ->
                            val negocio = todosLosNegocios.find { it.id == rel.negocioId } ?: return@forEach
                            val esActivo = negocio.id == negocioActivoId
                            val nombreDueno = remember(negocio.dueñoCuentaId, cuentasCheadas, currentUid) {
                                resolverNombreDueno(
                                    duenoCuentaId = negocio.dueñoCuentaId,
                                    currentUid = currentUid,
                                    cuentasCheadas = cuentasCheadas,
                                    onCargarFaltante = { negocioViewModel.cargarCuentasFaltantes(listOf(it)) }
                                )
                            }

                            TarjetaNegocioTrabajador(
                                negocio = negocio,
                                relacion = rel,
                                esActivo = esActivo,
                                nombreDueno = nombreDueno,
                                isDark = isDark,
                                onClick = {
                                    if (rel.estado == EstadoRelacion.ACTIVA) {
                                        negocioViewModel.seleccionarNegocio(negocio.id)
                                        onVolver()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// REGLA 4: Etiqueta "Rol: Dueño" solo para la tarjeta de dueño
@Composable
private fun TarjetaNegocioDueno(
    negocio: Negocio,
    esActivo: Boolean,
    isDark: Boolean,
    onClick: () -> Unit
) {
    val iniciales = negocio.nombre.take(2).uppercase()

    val cardBg = if (isDark) Color(0xFF1A1F26) else Color.White
    val cardBorder = if (esActivo) {
        BorderStroke(1.dp, if (isDark) Color(0xFF00C853) else Color(0xFF00A651))
    } else {
        BorderStroke(1.dp, if (isDark) Color(0xFF2D333B) else Color(0xFFE2E8F0))
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = cardBg,
        border = cardBorder,
        shadowElevation = 1.dp,
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
                    // Avatar con iniciales
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isDark) Color(0x1A00C853) else Color(0xFFDCFCE7),
                        border = BorderStroke(1.dp, if (isDark) Color(0x4D00C853) else Color(0x4000A651)),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = iniciales,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isDark) Color(0xFF00C853) else Color(0xFF00A651)
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = negocio.nombre,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else Color(0xFF0F172A)
                            )
                            if (esActivo) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (isDark) Color(0xFF00C853) else Color(0xFF00A651)
                                ) {
                                    Text(
                                        text = "ACTIVO",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) Color.Black else Color.White,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        // Etiqueta explícita de Dueño
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (isDark) Color(0xFF12161A) else Color(0xFFF8FAFC),
                            border = BorderStroke(1.dp, if (isDark) Color(0xFF2D333B) else Color(0xFFE2E8F0))
                        ) {
                            Text(
                                text = "Rol: Dueño",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            // Info de dueño
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isDark) Color(0xFF12161A) else Color(0xFFF8FAFC),
                border = BorderStroke(1.dp, if (isDark) Color(0xFF2D333B) else Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 13.dp, vertical = 9.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Dueño del negocio",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                    )
                    Text(
                        text = "Tú",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDark) Color.White else Color(0xFF0F172A)
                    )
                }
            }

            // Botón de ingreso
            Surface(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = if (isDark) Color(0xFF00C853) else Color(0xFF00A651),
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (esActivo) "Negocio actual" else "Ingresar al negocio",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.Black else Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = if (isDark) Color.Black else Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// REGLA 4: Etiqueta "Rol: Trabajador" solo para la tarjeta de trabajador
@Composable
private fun TarjetaNegocioTrabajador(
    negocio: Negocio,
    relacion: RelacionCuentaNegocio,
    esActivo: Boolean,
    nombreDueno: String,
    isDark: Boolean,
    onClick: () -> Unit
) {
    val esSolicitado = relacion.estado == EstadoRelacion.SOLICITADA
    val esPausado = relacion.estado == EstadoRelacion.PAUSADA
    val iniciales = negocio.nombre.take(2).uppercase()

    val cardBg = when {
        isDark && (esSolicitado || esPausado) -> Color(0xFF1E232B)
        isDark -> Color(0xFF1A1F26)
        esSolicitado || esPausado -> Color(0xFFFFFBEB)
        else -> Color.White
    }

    val cardBorder = when {
        esSolicitado || esPausado -> BorderStroke(1.dp, if (isDark) Color(0x66F59E0B) else Color(0xFFFDE68A))
        isDark -> BorderStroke(1.dp, Color(0xFF2D333B))
        else -> BorderStroke(1.dp, Color(0xFFE2E8F0))
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = cardBg,
        border = cardBorder,
        shadowElevation = 1.dp,
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
                    // Avatar con iniciales
                    val (avatarBg, avatarBorder, avatarText) = when {
                        esSolicitado || esPausado -> Triple(
                            if (isDark) Color(0x1AF59E0B) else Color(0xFFFEF3C7),
                            if (isDark) Color(0x4DF59E0B) else Color(0x4DF59E0B),
                            Color(0xFFF59E0B)
                        )
                        else -> Triple(
                            if (isDark) Color(0x1A00C853) else Color(0xFFDCFCE7),
                            if (isDark) Color(0x4D00C853) else Color(0x4800A651),
                            if (isDark) Color(0xFF00C853) else Color(0xFF00A651)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = avatarBg,
                        border = BorderStroke(1.dp, avatarBorder),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = iniciales,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = avatarText
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = negocio.nombre,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else Color(0xFF0F172A)
                            )

                            if (esSolicitado) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFF59E0B)
                                ) {
                                    Text(
                                        text = "EN ESPERA",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            } else if (esPausado) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFF59E0B)
                                ) {
                                    Text(
                                        text = "PAUSADO",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            } else if (esActivo) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (isDark) Color(0xFF00C853) else Color(0xFF00A651)
                                ) {
                                    Text(
                                        text = "ACTIVO",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) Color.Black else Color.White,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        val chipBg = when {
                            isDark && (esSolicitado || esPausado) -> Color(0x1AF59E0B)
                            isDark -> Color(0xFF12161A)
                            esSolicitado || esPausado -> Color(0xFFFEF3C7)
                            else -> Color(0xFFF8FAFC)
                        }
                        val chipBorder = when {
                            esSolicitado || esPausado -> if (isDark) Color(0x4DF59E0B) else Color(0xFFFDE68A)
                            isDark -> Color(0xFF2D333B)
                            else -> Color(0xFFE2E8F0)
                        }
                        val chipText = when {
                            esSolicitado || esPausado -> Color(0xFFF59E0B)
                            isDark -> Color(0xFF94A3B8)
                            else -> Color(0xFF64748B)
                        }

                        // Etiqueta explícita de Trabajador
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = chipBg,
                            border = BorderStroke(1.dp, chipBorder)
                        ) {
                            Text(
                                text = "Rol: Trabajador",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = chipText,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            // Info de dueño y aviso de estado
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isDark) Color(0xFF12161A) else if (esSolicitado || esPausado) Color.White else Color(0xFFF8FAFC),
                border = BorderStroke(1.dp, if (esSolicitado || esPausado) Color(0x26F59E0B) else if (isDark) Color(0xFF2D333B) else Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(11.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Dueño del negocio",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                        )
                        Text(
                            text = nombreDueno,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDark) Color.White else Color(0xFF0F172A)
                        )
                    }

                    if (esSolicitado) {
                        HorizontalDivider(color = Color(0x26F59E0B), thickness = 1.dp)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Esperando aprobación del dueño",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFF59E0B)
                            )
                        }
                    } else if (esPausado) {
                        HorizontalDivider(color = Color(0x26F59E0B), thickness = 1.dp)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PauseCircle,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Pausado por el dueño",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFF59E0B)
                            )
                        }
                    }
                }
            }

            // Botón de acción
            if (esSolicitado) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF59E0B),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Pendiente de aprobacion",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            } else if (esPausado) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF59E0B),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Pausado",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.PauseCircle,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            } else {
                Surface(
                    onClick = onClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = if (isDark) Color(0xFF00C853) else Color(0xFF00A651),
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (esActivo) "Negocio actual" else "Ingresar al negocio",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.Black else Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = if (isDark) Color.Black else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
