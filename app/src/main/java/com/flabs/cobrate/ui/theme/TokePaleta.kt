package com.flabs.cobrate.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import com.flabs.cobrate.core.data.PreferenciasAppStore

/**
 * Paleta global de TOKE para las pantallas nuevas. Los valores son los del Home del dueño
 * (referencia de identidad visual) y salen del aspecto que elige el usuario en la app
 * ([PreferenciasAppStore.themeMode]); nunca del aspecto del sistema.
 *
 * Solo colores. Ningún texto ni dato de negocio va aquí.
 */
data class TokePaleta(
    val fondo: Color,
    val tarjeta: Color,
    val borde: Color,
    val tileFondo: Color,
    val textoPrincipal: Color,
    val textoSecundario: Color,
    /** Píldora o chip seleccionado (oscuro en claro, blanco en oscuro). */
    val seleccionFondo: Color,
    val seleccionTexto: Color,
    /** Ámbar de pendientes y acciones de asignar. */
    val ambar: Color,
    val ambarTexto: Color,
    val enlaceVerde: Color,
)

val TokePaletaClara = TokePaleta(
    fondo = Color(0xFFF2F4F7),
    tarjeta = Color(0xFFFFFFFF),
    borde = Color(0xFFE2E8F0),
    tileFondo = Color(0xFFF1F4F9),
    textoPrincipal = Color(0xFF0F172A),
    textoSecundario = Color(0xFF64748B),
    seleccionFondo = Color(0xFF0F172A),
    seleccionTexto = Color(0xFFFFFFFF),
    ambar = Color(0xFFF59E0B),
    ambarTexto = Color(0xFF181202),
    enlaceVerde = Color(0xFF0A8F42),
)

val TokePaletaOscura = TokePaleta(
    fondo = Color(0xFF0A0D10),
    tarjeta = Color(0xFF14171D),
    borde = Color(0xFF1E232B),
    tileFondo = Color(0xFF1E242F),
    textoPrincipal = Color(0xFFF8FAFC),
    textoSecundario = Color(0xFF8E9BAE),
    seleccionFondo = Color(0xFFF8FAFC),
    seleccionTexto = Color(0xFF0F172A),
    ambar = Color(0xFFF59E0B),
    ambarTexto = Color(0xFF181202),
    enlaceVerde = Color(0xFF00C853),
)

/** Paleta vigente según el aspecto elegido en la app. */
@Composable
fun tokePaleta(): TokePaleta {
    val themeMode by PreferenciasAppStore.themeMode.collectAsState()
    return when (themeMode) {
        ThemeMode.DARK -> TokePaletaOscura
        ThemeMode.LIGHT -> TokePaletaClara
    }
}

/** ¿El aspecto elegido en la app es el oscuro? */
@Composable
fun tokeEsOscuro(): Boolean {
    val themeMode by PreferenciasAppStore.themeMode.collectAsState()
    return themeMode == ThemeMode.DARK
}
