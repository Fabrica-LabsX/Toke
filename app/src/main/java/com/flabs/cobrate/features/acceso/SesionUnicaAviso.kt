package com.flabs.cobrate.features.acceso

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import com.flabs.cobrate.components.DialogoToke
import com.flabs.cobrate.core.data.AvisoSesion
import com.flabs.cobrate.core.data.SesionUnicaStore
import com.flabs.cobrate.features.auth.AuthViewModel
import kotlinx.coroutines.launch

/** Aviso de "TOKE solo funciona en un celular a la vez". Se dibuja encima de cualquier pantalla. */
@Composable
fun SesionUnicaAviso(authViewModel: AuthViewModel) {
    val aviso by SesionUnicaStore.aviso.collectAsState()
    val scope = rememberCoroutineScope()
    when (val a = aviso) {
        is AvisoSesion.Ocupada -> DialogoToke(
            titulo = "TOKE solo funciona en un celular a la vez",
            texto = "Tu cuenta está abierta en ${a.dispositivoNombre} desde " +
                SesionUnicaLogica.cuando(a.iniciadaEn, System.currentTimeMillis()) + ". " +
                "Si la usas aquí, el otro celular se cerrará.",
            textoConfirmar = "Usar aquí",
            textoCancelar = "Cerrar sesión",
            onConfirmar = { scope.launch { SesionUnicaStore.usarAqui() } },
            onCancelar = { authViewModel.cerrarSesion() },
        )
        is AvisoSesion.Desplazada -> DialogoToke(
            titulo = "Tu cuenta se abrió en otro celular",
            texto = if (!a.dispositivoNombre.isNullOrBlank()) {
                "Tu cuenta ahora está activa en ${a.dispositivoNombre}. TOKE solo funciona en un celular a la vez."
            } else {
                "TOKE solo funciona en un celular a la vez, por lo que la sesión se trasladó a otro celular."
            },
            textoConfirmar = "Usar aquí",
            textoCancelar = "Cerrar sesión",
            onConfirmar = { scope.launch { SesionUnicaStore.usarAqui() } },
            onCancelar = { authViewModel.cerrarSesion() },
        )
        null -> Unit
    }
}
