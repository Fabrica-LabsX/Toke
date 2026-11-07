package com.flabs.cobrate.features.equipo

import androidx.compose.runtime.Composable
import com.flabs.cobrate.components.DialogoToke
import com.flabs.cobrate.features.business.TrabajadorRealInfo
import com.flabs.cobrate.ui.theme.TokePaleta

/** Confirmación antes de expulsar a un colaborador del negocio. */
@Composable
@Suppress("UNUSED_PARAMETER")
fun EquipoDialogoExpulsar(
    paleta: TokePaleta,
    trabajador: TrabajadorRealInfo,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit,
) {
    DialogoToke(
        titulo = "¿Expulsar a ${trabajador.username}?",
        texto = "Dejará de ver los cobros y de operar en este negocio.",
        textoConfirmar = "Expulsar",
        destructivo = true,
        onConfirmar = onConfirmar,
        onCancelar = onCancelar,
    )
}
