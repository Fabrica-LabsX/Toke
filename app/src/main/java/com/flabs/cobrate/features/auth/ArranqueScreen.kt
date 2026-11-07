package com.flabs.cobrate.features.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.flabs.cobrate.R
import com.flabs.cobrate.features.acceso.LogoTokeSuave
import com.flabs.cobrate.ui.theme.tokePaleta

/** Pantalla mientras la app decide si hay sesión: el símbolo de TOKE con su entrada suave. */
@Composable
fun ArranqueScreen() {
    val paleta = tokePaleta()
    Box(
        modifier = Modifier.fillMaxSize().background(paleta.fondo),
        contentAlignment = Alignment.Center,
    ) {
        LogoTokeSuave(paleta, tamano = 96, descripcion = stringResource(R.string.app_name))
    }
}
