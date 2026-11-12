package com.flabs.cobrate.core.logic

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class TabRefreshState {
    IDLE,
    REFRESHING,
    SUCCESS
}

/**
 * Gestor centralizado del estado de refresco manual de pestañas (CAPA P1).
 */
object TabRefreshManager {

    private val _refreshStates = MutableStateFlow<Map<String, TabRefreshState>>(emptyMap())
    val refreshStates: StateFlow<Map<String, TabRefreshState>> = _refreshStates.asStateFlow()

    fun getState(route: String): TabRefreshState {
        return _refreshStates.value[route] ?: TabRefreshState.IDLE
    }

    fun isRefreshing(route: String): Boolean {
        return getState(route) == TabRefreshState.REFRESHING
    }

    suspend fun ejecutarRefresco(route: String, action: suspend () -> Result<Unit>) {
        if (isRefreshing(route)) {
            LogSeguro.d("TabRefreshManager", "Refresco en progreso para $route. Ignorando solicitud duplicada.")
            return
        }

        _refreshStates.value = _refreshStates.value + (route to TabRefreshState.REFRESHING)
        LogSeguro.d("TabRefreshManager", "[REFRESH-START] Ruta $route")

        val result = try {
            action()
        } catch (e: Exception) {
            LogSeguro.e("TabRefreshManager", "Excepción durante refresco de $route", e)
            Result.failure(e)
        }

        if (result.isSuccess) {
            _refreshStates.value = _refreshStates.value + (route to TabRefreshState.SUCCESS)
            LogSeguro.d("TabRefreshManager", "[REFRESH-SUCCESS] Ruta $route")
            delay(1500L)
            _refreshStates.value = _refreshStates.value + (route to TabRefreshState.IDLE)
        } else {
            LogSeguro.w("TabRefreshManager", "[REFRESH-FAIL] Ruta $route", result.exceptionOrNull())
            _refreshStates.value = _refreshStates.value + (route to TabRefreshState.IDLE)
        }
    }
}
