package com.flabs.cobrate.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import com.flabs.cobrate.core.data.PreferenciasAppStore

/**
 * Paleta única de TOKE. Es la única fuente de colores de la app: las pantallas la leen con
 * [tokePaleta] y el tema de Material se arma a partir de ella ([esquemaToke]).
 * Sale del aspecto que elige el usuario en la app ([PreferenciasAppStore.themeMode]);
 * nunca del aspecto del sistema.
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
    val enlaceAzul: Color,
    /** Verde de marca: acción principal y estado "recibido", con el color del texto sobre él. */
    val verde: Color,
    val verdeTexto: Color,
    /** Borde y detalles del verde de marca (chips, tile del negocio actual). */
    val activo: Color,
    val bordeControl: Color,
    /** Rojo de error y acciones que borran, con su fondo y borde suaves. */
    val rojo: Color,
    val rojoFondo: Color,
    val rojoBorde: Color,
    /** Verde vivo de los indicadores de estado (wifi, sincronizado): el mismo en claro y en oscuro. */
    val verdeVivo: Color,
    val verdeVivoBorde: Color,
)

/** Rojo de error y de acciones que borran; el mismo en claro y oscuro. */
val TokeRojo = Color(0xFFDC2626)

/** Ámbar oscuro para texto sobre fondos claros (avisos, iconos resaltados). */
val TokeAmbarTexto = Color(0xFFB45309)

val TokePaletaClara = TokePaleta(
    fondo = Color(0xFFF2F4F7),
    tarjeta = Color(0xFFFFFFFF),
    borde = Color(0xFFE2E8F0),
    tileFondo = Color(0xFFF1F5F9),
    textoPrincipal = Color(0xFF0F172A),
    textoSecundario = Color(0xFF64748B),
    seleccionFondo = Color(0xFF0F172A),
    seleccionTexto = Color(0xFFFFFFFF),
    ambar = Color(0xFFF59E0B),
    ambarTexto = Color(0xFF181202),
    enlaceVerde = Color(0xFF047857),
    enlaceAzul = Color(0xFF2563EB),
    verde = Color(0xFF059669),
    verdeTexto = Color(0xFFFFFFFF),
    activo = Color(0xFF059669),
    bordeControl = Color(0xFFCBD5E1),
    rojo = TokeRojo,
    rojoFondo = Color(0xFFFEE2E2),
    rojoBorde = Color(0xFFFECACA),
    verdeVivo = Color(0xFF00C853),
    verdeVivoBorde = Color(0xFF00B44A),
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
    enlaceAzul = Color(0xFF60A5FA),
    verde = Color(0xFF00C853),
    verdeTexto = Color(0xFF042615),
    activo = Color(0xFF00C853),
    bordeControl = Color(0xFF2D3546),
    rojo = TokeRojo,
    rojoFondo = Color(0xFF2A1414),
    rojoBorde = Color(0xFF3D1E1E),
    verdeVivo = Color(0xFF00C853),
    verdeVivoBorde = Color(0xFF00B44A),
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

/** Mezcla este color sobre [base] y devuelve un color SÓLIDO (sin transparencia): los fondos y bordes suaves no deben dejar ver lo de atrás. */
fun Color.solidoSobre(base: Color, intensidad: Float): Color = androidx.compose.ui.graphics.lerp(base, this.copy(alpha = 1f), intensidad)

/** Fondo detrás de las hojas inferiores: un gris sólido (sin transparencia), un poco más oscuro que el fondo de la app. */
fun TokePaleta.scrimSolido(): Color = Color.Black.solidoSobre(fondo, 0.4f)
