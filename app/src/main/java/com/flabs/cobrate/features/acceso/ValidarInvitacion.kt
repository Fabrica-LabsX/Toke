package com.flabs.cobrate.features.acceso

import android.util.Log
import com.flabs.cobrate.core.data.CuentaStore
import com.flabs.cobrate.core.data.InvitacionStore
import com.flabs.cobrate.core.data.SupabaseAuthBridge
import com.flabs.cobrate.core.logic.ErrorMapper
import com.flabs.cobrate.core.logic.FeedbackManager
import com.flabs.cobrate.core.logic.InvitacionValidation
import com.flabs.cobrate.core.logic.LogSeguro
import com.flabs.cobrate.core.logic.MotivoRechazo
import com.flabs.cobrate.core.logic.TokeErrorCodes
import com.flabs.cobrate.core.model.InvitacionTrabajador
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Valida un código de invitación: lo busca, comprueba que sirva y se asegura de que exista una
 * sesión (anónima si hace falta). Si algo falla muestra el aviso y devuelve null.
 * Es el flujo que antes estaba copiado en "Empezar" y en "Ingresar código".
 */
suspend fun validarCodigoInvitacion(codigo: String): InvitacionTrabajador? {
    FeedbackManager.setLoading(true, "Validando invitación...")
    val result = InvitacionStore.obtenerRemoto(codigo)

    if (result.isFailure) {
        LogSeguro.e("TokeInvitacion", "obtenerRemoto falló", result.exceptionOrNull())
        FeedbackManager.setLoading(false)
        FeedbackManager.showErrorMessage(
            ErrorMapper.translateInvitacionError(result.exceptionOrNull(), TokeErrorCodes.ERROR_VERIFICANDO_INVITACION)
        )
        return null
    }

    val invitacion = result.getOrNull()
    val motivo = InvitacionValidation.motivoDeRechazo(invitacion, System.currentTimeMillis())
    Log.d("TokeInvitacion", "motivoRechazo=$motivo")

    val mensaje = when (motivo) {
        MotivoRechazo.NO_EXISTE -> "Código de invitación no encontrado. Código de ayuda: ${TokeErrorCodes.INVITACION_NO_ENCONTRADA}"
        MotivoRechazo.NO_PENDIENTE -> "Esta invitación ya fue utilizada. Código de ayuda: ${TokeErrorCodes.INVITACION_YA_UTILIZADA}"
        MotivoRechazo.EXPIRADA -> "La invitación ha expirado. Código de ayuda: ${TokeErrorCodes.INVITACION_EXPIRADA}"
        null -> null
    }
    if (mensaje != null || invitacion == null) {
        FeedbackManager.setLoading(false)
        mensaje?.let { FeedbackManager.showErrorMessage(it) }
        return null
    }

    if (CuentaStore.sesionActual.value == null) {
        val anonRes = SupabaseAuthBridge.iniciarSesionTrabajadorAnonimo()
        if (anonRes.isFailure) {
            LogSeguro.e("TokeInvitacion", "sesión anónima falló", anonRes.exceptionOrNull())
            FeedbackManager.setLoading(false)
            FeedbackManager.showErrorMessage(
                ErrorMapper.translateInvitacionError(anonRes.exceptionOrNull(), TokeErrorCodes.ERROR_SESION_TRABAJADOR)
            )
            return null
        }
    }

    val cuentaReady = withTimeoutOrNull(8000L) { CuentaStore.sesionActual.filter { it != null }.first() }
    FeedbackManager.setLoading(false)
    if (cuentaReady == null) {
        Log.w("TokeInvitacion", "sesión no disponible tras 8s")
        FeedbackManager.showErrorMessage(
            "Sesión del trabajador no disponible. Código de ayuda: ${TokeErrorCodes.SESION_TRABAJADOR_NO_DISPONIBLE}"
        )
        return null
    }
    return invitacion
}
