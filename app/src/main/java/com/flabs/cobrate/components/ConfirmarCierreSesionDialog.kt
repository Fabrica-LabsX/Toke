package com.flabs.cobrate.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.flabs.cobrate.R

/** Confirmación para cerrar la sesión. */
@Composable
fun ConfirmarCierreSesionDialog(
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    DialogoToke(
        titulo = stringResource(R.string.dialogo_cerrar_titulo),
        texto = stringResource(R.string.dialogo_cerrar_texto),
        textoConfirmar = stringResource(R.string.dialogo_cerrar_confirmar),
        textoCancelar = stringResource(R.string.dialogo_cancelar),
        destructivo = true,
        onConfirmar = onConfirmar,
        onCancelar = onCancelar,
    )
}
