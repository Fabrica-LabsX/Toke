package com.flabs.cobrate.ui.theme

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

/** Esquema de Material armado solo con la paleta de TOKE: así los componentes de Material y las pantallas nuevas se ven igual. */
fun esquemaToke(oscuro: Boolean): ColorScheme {
    val p = if (oscuro) TokePaletaOscura else TokePaletaClara
    return if (oscuro) {
        darkColorScheme(
            primary = p.verde, onPrimary = p.verdeTexto,
            primaryContainer = p.tileFondo, onPrimaryContainer = p.textoPrincipal,
            secondary = p.textoSecundario, onSecondary = p.fondo,
            secondaryContainer = p.tileFondo, onSecondaryContainer = p.textoPrincipal,
            tertiary = p.ambar, onTertiary = p.ambarTexto,
            tertiaryContainer = p.ambar, onTertiaryContainer = p.ambarTexto,
            inverseSurface = p.textoPrincipal, inverseOnSurface = p.fondo,
            background = p.fondo, onBackground = p.textoPrincipal,
            surface = p.tarjeta, onSurface = p.textoPrincipal,
            surfaceVariant = p.tileFondo, onSurfaceVariant = p.textoSecundario,
            outline = p.bordeControl, outlineVariant = p.borde,
            error = p.rojo, onError = Color.White,
            errorContainer = p.rojoFondo, onErrorContainer = p.rojo,
        )
    } else {
        lightColorScheme(
            primary = p.verde, onPrimary = p.verdeTexto,
            primaryContainer = p.tileFondo, onPrimaryContainer = p.textoPrincipal,
            secondary = p.textoSecundario, onSecondary = p.fondo,
            secondaryContainer = p.tileFondo, onSecondaryContainer = p.textoPrincipal,
            tertiary = p.ambar, onTertiary = p.ambarTexto,
            tertiaryContainer = p.ambar, onTertiaryContainer = p.ambarTexto,
            inverseSurface = p.textoPrincipal, inverseOnSurface = p.fondo,
            background = p.fondo, onBackground = p.textoPrincipal,
            surface = p.tarjeta, onSurface = p.textoPrincipal,
            surfaceVariant = p.tileFondo, onSurfaceVariant = p.textoSecundario,
            outline = p.bordeControl, outlineVariant = p.borde,
            error = p.rojo, onError = Color.White,
            errorContainer = p.rojoFondo, onErrorContainer = p.rojo,
        )
    }
}

/** Envuelve una pantalla con el esquema de [esquemaToke] (el tema general ya lo usa; se conserva por compatibilidad). */
@Composable
fun TokeMaterial(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = esquemaToke(tokeEsOscuro()), shapes = Shapes, typography = Typography, content = content)
}

/** Igual que `composable`, pero con la pantalla envuelta en [TokeMaterial]. */
fun NavGraphBuilder.composableToke(
    route: String,
    content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
) {
    composable(route) { entrada -> TokeMaterial { content(entrada) } }
}
