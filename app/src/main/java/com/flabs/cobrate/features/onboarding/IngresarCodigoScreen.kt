package com.flabs.cobrate.features.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import com.flabs.cobrate.R
import com.flabs.cobrate.core.model.InvitacionTrabajador
import com.flabs.cobrate.features.acceso.AccesoFondo
import com.flabs.cobrate.features.acceso.BotonAcceso
import com.flabs.cobrate.features.acceso.BotonAtras
import com.flabs.cobrate.features.acceso.CampoAcceso
import com.flabs.cobrate.features.acceso.EspacioAcceso
import com.flabs.cobrate.features.acceso.TituloAcceso
import com.flabs.cobrate.features.acceso.validarCodigoInvitacion
import com.flabs.cobrate.ui.theme.tokePaleta
import kotlinx.coroutines.launch

/**
 * Pantalla "Soy trabajador": pide el código de invitación. Al validarlo se asegura de que
 * exista una sesión anónima en Supabase antes de continuar.
 */
@Composable
fun IngresarCodigoScreen(
    onCodigoValido: (InvitacionTrabajador) -> Unit,
    onVolver: () -> Unit
) {
    var codigo by rememberSaveable { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val paleta = tokePaleta()

    AccesoFondo(paleta = paleta) {
        BotonAtras(paleta, onVolver)
        TituloAcceso(
            paleta = paleta,
            titulo = stringResource(R.string.unirse_invitacion_titulo),
            subtitulo = stringResource(R.string.unirse_invitacion_subtitulo),
        )
        CampoAcceso(
            paleta = paleta,
            etiqueta = stringResource(R.string.label_codigo_invitacion),
            valor = codigo,
            placeholder = stringResource(R.string.empezar_placeholder_codigo),
            onCambia = { codigo = it.uppercase() },
            teclado = KeyboardType.Ascii,
        )
        EspacioAcceso(4)
        BotonAcceso(paleta, stringResource(R.string.empezar_unirme), principal = codigo.isNotBlank()) {
            if (codigo.isNotBlank()) {
                scope.launch { validarCodigoInvitacion(codigo.trim())?.let(onCodigoValido) }
            }
        }
    }
}
