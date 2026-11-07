package com.flabs.cobrate.features.business

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.flabs.cobrate.core.logic.FeedbackManager
import com.flabs.cobrate.core.model.Negocio
import com.flabs.cobrate.core.model.TipoNegocio
import com.flabs.cobrate.features.crearnegocio.CrearNegocioLogica
import com.flabs.cobrate.features.crearnegocio.PasoConexion
import com.flabs.cobrate.features.crearnegocio.PasoFinal
import com.flabs.cobrate.features.crearnegocio.PasoNombreYColor
import com.flabs.cobrate.features.crearnegocio.PasoTipo
import com.flabs.cobrate.ui.theme.tokePaleta
import kotlinx.coroutines.launch

private enum class PasoWizard {
    PASO_1_NOMBRE_Y_COLOR,
    PASO_2_TIPO_NEGOCIO,
    CONEXION_EXITOSA,
    PASO_3_FINALIZADO
}

/**
 * Asistente para crear el negocio: nombre y color, rubro, conexión exitosa y confirmación final.
 * Guarda el estado y crea el negocio; el dibujo de cada paso vive en `features/crearnegocio`.
 */
@Composable
fun CrearNegocioScreen(
    negocioViewModel: NegocioViewModel,
    onCreado: () -> Unit,
    onIrAMisQRs: (() -> Unit)? = null,
    onVolver: (() -> Unit)? = null
) {
    val scope = rememberCoroutineScope()
    val paleta = tokePaleta()
    var paso by rememberSaveable { mutableStateOf(PasoWizard.PASO_1_NOMBRE_Y_COLOR) }
    var procesando by remember { mutableStateOf(false) }

    var nombreComercial by rememberSaveable { mutableStateOf("") }
    var colorHexSeleccionado by rememberSaveable { mutableStateOf<String?>(CrearNegocioLogica.COLOR_POR_DEFECTO) }
    var tipoSeleccionado by rememberSaveable { mutableStateOf<TipoNegocio?>(null) }
    var negocioCreado by remember { mutableStateOf<Negocio?>(null) }

    // Una vez creado el negocio no se vuelve al paso 2: se crearía otro igual.
    BackHandler(paso != PasoWizard.PASO_1_NOMBRE_Y_COLOR) {
        when (paso) {
            PasoWizard.PASO_2_TIPO_NEGOCIO -> paso = PasoWizard.PASO_1_NOMBRE_Y_COLOR
            PasoWizard.CONEXION_EXITOSA -> onCreado()
            PasoWizard.PASO_3_FINALIZADO -> paso = PasoWizard.CONEXION_EXITOSA
            else -> Unit
        }
    }

    when (paso) {
        PasoWizard.PASO_1_NOMBRE_Y_COLOR -> PasoNombreYColor(
            paleta = paleta,
            nombre = nombreComercial,
            onNombre = { nombreComercial = it },
            colorHex = colorHexSeleccionado,
            onColor = { colorHexSeleccionado = it },
            onContinuar = { paso = PasoWizard.PASO_2_TIPO_NEGOCIO },
            onAtras = { onVolver?.invoke() },
        )

        PasoWizard.PASO_2_TIPO_NEGOCIO -> PasoTipo(
            paleta = paleta,
            elegido = tipoSeleccionado,
            onElegir = { tipoSeleccionado = it },
            ocupado = procesando,
            onAtras = { paso = PasoWizard.PASO_1_NOMBRE_Y_COLOR },
            onCrear = {
                val tipo = tipoSeleccionado ?: return@PasoTipo
                procesando = true
                scope.launch {
                    FeedbackManager.setLoading(true, "Creando negocio...")
                    val result = negocioViewModel.crearNegocio(
                        nombre = nombreComercial,
                        tipo = tipo,
                        colorMarca = colorHexSeleccionado
                    )
                    FeedbackManager.setLoading(false)
                    result.onSuccess { neg ->
                        negocioCreado = neg
                        FeedbackManager.showSuccess("¡Listo! Negocio creado.")
                        paso = PasoWizard.CONEXION_EXITOSA
                    }.onFailure { err ->
                        FeedbackManager.showError(err)
                        procesando = false
                    }
                }
            },
        )

        PasoWizard.CONEXION_EXITOSA -> PasoConexion(
            paleta = paleta,
            nombreNegocio = negocioCreado?.nombre ?: nombreComercial,
            onRecibirCobros = { onIrAMisQRs?.invoke() ?: onCreado() },
            onMiNegocio = { paso = PasoWizard.PASO_3_FINALIZADO },
            onEntrar = onCreado,
        )

        PasoWizard.PASO_3_FINALIZADO -> PasoFinal(
            paleta = paleta,
            nombreNegocio = negocioCreado?.nombre ?: nombreComercial,
            tipo = tipoSeleccionado,
            onAtras = { paso = PasoWizard.CONEXION_EXITOSA },
            onEntrar = onCreado,
        )
    }
}
