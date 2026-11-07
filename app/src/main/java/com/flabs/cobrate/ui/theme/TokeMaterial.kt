package com.flabs.cobrate.ui.theme

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

/**
 * Hace que los componentes de Material de una pantalla (campos, interruptores, textos) usen la
 * paleta global de la app. Se aplica pantalla por pantalla y no al tema general, porque el Home
 * está cerrado y todavía lee los colores del tema general.
 */
@Composable
fun TokeMaterial(content: @Composable () -> Unit) {
    val p = tokePaleta()
    val rojo = Color(0xFFDC2626)
    val esquema = if (tokeEsOscuro()) {
        darkColorScheme(
            primary = p.verde, onPrimary = p.verdeTexto,
            secondary = p.textoSecundario, onSecondary = p.fondo,
            background = p.fondo, onBackground = p.textoPrincipal,
            surface = p.tarjeta, onSurface = p.textoPrincipal,
            surfaceVariant = p.tileFondo, onSurfaceVariant = p.textoSecundario,
            outline = p.bordeControl, outlineVariant = p.borde,
            error = rojo, onError = Color.White,
        )
    } else {
        lightColorScheme(
            primary = p.verde, onPrimary = p.verdeTexto,
            secondary = p.textoSecundario, onSecondary = p.fondo,
            background = p.fondo, onBackground = p.textoPrincipal,
            surface = p.tarjeta, onSurface = p.textoPrincipal,
            surfaceVariant = p.tileFondo, onSurfaceVariant = p.textoSecundario,
            outline = p.bordeControl, outlineVariant = p.borde,
            error = rojo, onError = Color.White,
        )
    }
    MaterialTheme(colorScheme = esquema, shapes = Shapes, typography = Typography, content = content)
}

/** Igual que `composable`, pero con la pantalla envuelta en [TokeMaterial]. */
fun NavGraphBuilder.composableToke(
    route: String,
    content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
) {
    composable(route) { entrada -> TokeMaterial { content(entrada) } }
}
