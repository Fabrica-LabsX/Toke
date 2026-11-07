package com.flabs.cobrate.features.cobros

import androidx.compose.runtime.Composable
import com.flabs.cobrate.components.DialogoToke
import com.flabs.cobrate.core.model.MetodoDeCobro
import com.flabs.cobrate.ui.theme.TokePaleta

/** Confirmación antes de borrar un método de cobro. Borrar no se puede deshacer. */
@Composable
@Suppress("UNUSED_PARAMETER")
fun EliminarMetodoDialogo(
    paleta: TokePaleta,
    metodo: MetodoDeCobro,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    DialogoToke(
        titulo = "¿Eliminar ${MetodosCobroLogica.nombreLegible(metodo.tipo)}?",
        texto = "Se borra el método y la imagen de su QR. No se puede deshacer.",
        textoConfirmar = "Eliminar",
        destructivo = true,
        onConfirmar = onConfirm,
        onCancelar = onDismiss,
    )
}
