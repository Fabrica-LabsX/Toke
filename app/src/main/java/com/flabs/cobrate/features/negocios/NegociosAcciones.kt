package com.flabs.cobrate.features.negocios

import com.flabs.cobrate.core.data.RelacionCuentaNegocioRepository
import com.flabs.cobrate.core.data.RelacionCuentaNegocioStore
import com.flabs.cobrate.core.logic.FeedbackManager
import com.flabs.cobrate.core.model.EstadoRelacion
import com.flabs.cobrate.core.model.Negocio
import com.flabs.cobrate.features.business.NegocioViewModel

/**
 * Acciones con efecto de la lista de negocios (hablan con el servidor y avisan al usuario).
 * Se mantienen aquí, aparte de las pantallas, para que la interfaz solo las dispare.
 */
object NegociosAcciones {

    /**
     * Cancela la solicitud de ingreso a [negocioId]. Si ese negocio era el que se estaba
     * usando, se deja de seleccionar.
     */
    suspend fun cancelarSolicitud(
        negocioId: String,
        cuentaId: String,
        negocioViewModel: NegocioViewModel,
    ) {
        RelacionCuentaNegocioRepository().salirDelNegocio(negocioId)
            .onSuccess { resultado ->
                RelacionCuentaNegocioStore.quitarRelacion(negocioId, cuentaId)
                if (negocioViewModel.negocioSeleccionadoId.value == negocioId) {
                    negocioViewModel.seleccionarNegocio(null)
                }
                FeedbackManager.showSuccess(resultado.mensaje)
            }
            .onFailure { error ->
                FeedbackManager.showErrorMessage(error.message ?: "Error al cancelar solicitud")
            }
    }

    /**
     * Consulta al servidor si el dueño reactivó el acceso. Si volvió a estar activo se
     * selecciona el negocio y se llama a [alReactivar].
     */
    suspend fun verificarAcceso(
        negocio: Negocio,
        cuentaId: String,
        negocioViewModel: NegocioViewModel,
        alReactivar: () -> Unit,
    ) {
        FeedbackManager.setLoading(true, "Verificando acceso...")
        RelacionCuentaNegocioRepository().relacionesDe(cuentaId)
            .onSuccess { relaciones ->
                FeedbackManager.setLoading(false)
                RelacionCuentaNegocioStore.sincronizar(cuentaId, relaciones)
                val actual = relaciones.find { it.negocioId == negocio.id }
                if (actual?.estado == EstadoRelacion.ACTIVA) {
                    negocioViewModel.seleccionarNegocio(negocio.id)
                    alReactivar()
                    FeedbackManager.showSuccess("¡Acceso reactivado por el dueño!")
                } else {
                    FeedbackManager.showSuccess("Tu acceso a «${negocio.nombre}» sigue pausado por el dueño.")
                }
            }
            .onFailure { error ->
                FeedbackManager.setLoading(false)
                FeedbackManager.showErrorMessage(error.message ?: "Error al verificar estado con el servidor")
            }
    }
}
