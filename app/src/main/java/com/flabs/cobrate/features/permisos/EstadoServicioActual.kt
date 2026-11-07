package com.flabs.cobrate.features.permisos

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.flabs.cobrate.core.data.CuentaStore
import com.flabs.cobrate.core.data.RelacionCuentaNegocioStore
import com.flabs.cobrate.core.logic.PermisosChecker
import com.flabs.cobrate.core.logic.RequisitosPermisos

/** Lo que el sistema tiene concedido en este momento. */
data class PermisosActuales(
    val accesoNotificaciones: Boolean,
    val bateriaSinRestricciones: Boolean,
)

/**
 * Lee los permisos reales del sistema y los vuelve a leer cada vez que la app regresa a primer
 * plano (por ejemplo, tras concederlos en Ajustes).
 */
@Composable
fun rememberPermisosActuales(): PermisosActuales {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var acceso by remember { mutableStateOf(PermisosChecker.accesoNotificacionesConcedido(context)) }
    var bateria by remember { mutableStateOf(PermisosChecker.segundoPlanoSinRestricciones(context)) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                acceso = PermisosChecker.accesoNotificacionesConcedido(context)
                bateria = PermisosChecker.segundoPlanoSinRestricciones(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    return PermisosActuales(accesoNotificaciones = acceso, bateriaSinRestricciones = bateria)
}

/**
 * Estado del servicio de TOKE para la cuenta actual, según el rol (qué permisos le exigen) y
 * lo que el sistema tiene concedido.
 */
@Composable
fun rememberEstadoServicio(): EstadoServicio {
    val permisos = rememberPermisosActuales()
    val sesion by CuentaStore.sesionActual.collectAsState()
    val relaciones by RelacionCuentaNegocioStore.relaciones.collectAsState()
    val cuentaId = sesion?.id

    val requisitos = remember(cuentaId, relaciones) {
        RequisitosPermisos.para(cuentaId ?: "", relaciones)
    }
    return EvaluadorServicio.evaluar(
        requiereAcceso = EvaluadorServicio.requiereAccesoNotificaciones(requisitos),
        accesoConcedido = permisos.accesoNotificaciones,
        requiereBateria = EvaluadorServicio.requiereBateria(requisitos),
        bateriaSinRestricciones = permisos.bateriaSinRestricciones,
    )
}
