package com.flabs.cobrate.ui.theme

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

/**
 * Fuente de toques para un botón. El efecto de hundirse lo dibuja [IndicacionToke] (instalada en el tema),
 * así que [modificador] queda vacío; se conserva para no tocar los 28 botones que ya lo encadenan.
 * Uso: pasar `pulso.fuente` al `Surface(interactionSource = …)`.
 */
class Pulsacion(val fuente: MutableInteractionSource, val modificador: Modifier = Modifier)

@Composable
fun rememberPulsacion(): Pulsacion = remember { Pulsacion(MutableInteractionSource()) }
