package com.flabs.cobrate.core.logic

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Gestor global de feedback para la UI.
 * Permite que cualquier componente envíe mensajes al usuario de forma estandarizada.
 */
object FeedbackManager {
    private val _currentFeedback = MutableStateFlow<FeedbackState?>(null)
    val currentFeedback: StateFlow<FeedbackState?> = _currentFeedback.asStateFlow()

    /** Muestra un mensaje de éxito. */
    fun showSuccess(message: String) {
        _currentFeedback.value = FeedbackState(FeedbackType.SUCCESS, message)
    }

    /** Muestra un error amigable a partir de uno técnico. */
    fun showError(technicalError: String?) {
        _currentFeedback.value = FeedbackState(
            FeedbackType.ERROR,
            ErrorMapper.translate(technicalError)
        )
    }

    /** Muestra un error amigable a partir de una excepción. */
    fun showError(throwable: Throwable?) {
        _currentFeedback.value = FeedbackState(
            FeedbackType.ERROR,
            ErrorMapper.translate(throwable)
        )
    }

    /** Muestra un error cuyo texto ya viene traducido; NO lo vuelve a pasar por ErrorMapper. */
    fun showErrorMessage(message: String) {
        _currentFeedback.value = FeedbackState(FeedbackType.ERROR, message)
    }

    /** Establece el estado de carga. */
    fun setLoading(isLoading: Boolean, message: String = "Cargando...") {
        if (isLoading) {
            _currentFeedback.value = FeedbackState(FeedbackType.LOADING, message, durationMillis = null)
        } else if (_currentFeedback.value?.type == FeedbackType.LOADING) {
            _currentFeedback.value = null
        }
    }

    /** Muestra un aviso de conexión. */
    fun setOffline(isOffline: Boolean) {
        if (isOffline) {
            _currentFeedback.value = FeedbackState(FeedbackType.OFFLINE, "Sin conexión a Internet", durationMillis = null)
        } else if (_currentFeedback.value?.type == FeedbackType.OFFLINE) {
            _currentFeedback.value = null
        }
    }

    /** Conveniencia para mostrar carga. */
    fun showLoading(message: String = "Cargando...") = setLoading(true, message)

    /** Conveniencia para ocultar carga. */
    fun dismissLoading() = setLoading(false)

    /** Limpia el feedback actual. */
    fun clear() {
        _currentFeedback.value = null
    }
}
