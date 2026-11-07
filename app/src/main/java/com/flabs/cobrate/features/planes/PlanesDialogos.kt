package com.flabs.cobrate.features.planes

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
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
    val contexto = LocalContext.current
    val telefono = stringResource(R.string.plan_telefono_activacion)
    DialogoToke(
        titulo = "Activa tu plan Pro",
        texto = "Comunícate por WhatsApp a este número para activar tu plan: $telefono",
        textoConfirmar = "Abrir WhatsApp",
        textoCancelar = "Cerrar",
        onConfirmar = {
            runCatching {
                contexto.startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse(PlanLogica.enlaceWhatsApp(telefono)))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }
            onCerrar()
        },
        onCancelar = onCerrar,
    )
}

/** Aviso cuando el plan Free no permite lo que se intentó (otro negocio, más colaboradores). */
@Composable
fun LimitePlanDialogo(texto: String, onCerrar: () -> Unit) {
    var verActivacion by remember { mutableStateOf(false) }
    if (verActivacion) {
        ActivarProDialogo(onCerrar = { verActivacion = false; onCerrar() })
        return
    }
    DialogoToke(
        titulo = "Disponible en Pro",
        texto = texto,
        textoConfirmar = "Activar Pro",
        textoCancelar = "Ahora no",
        onConfirmar = { verActivacion = true },
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
