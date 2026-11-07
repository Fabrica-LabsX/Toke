package com.flabs.cobrate.features.planes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.flabs.cobrate.R
import com.flabs.cobrate.components.DialogoToke

/** Cómo activar el plan Pro: llamar al número de contacto. */
@Composable
fun ActivarProDialogo(onCerrar: () -> Unit) {
    DialogoToke(
        titulo = "Activa tu plan Pro",
        texto = "Comunícate a este número para activar tu plan: ${stringResource(R.string.plan_telefono_activacion)}",
        textoConfirmar = "Entendido",
        textoCancelar = "Cerrar",
        onConfirmar = onCerrar,
        onCancelar = onCerrar,
    )
}

/** Aviso cuando se acaba la cuota diaria del plan Free. */
@Composable
fun CuotaAgotadaDialogo(onCerrar: () -> Unit) {
    var verActivacion by remember { mutableStateOf(false) }
    if (verActivacion) {
        ActivarProDialogo(onCerrar = { verActivacion = false; onCerrar() })
        return
    }
    DialogoToke(
        titulo = "Cuota de hoy agotada",
        texto = "Tu equipo ya no recibirá notificaciones hoy. Espera hasta mañana o activa un plan superior.",
        textoConfirmar = "Activar Pro",
        textoCancelar = "Entendido",
        onConfirmar = { verActivacion = true },
        onCancelar = onCerrar,
    )
}
