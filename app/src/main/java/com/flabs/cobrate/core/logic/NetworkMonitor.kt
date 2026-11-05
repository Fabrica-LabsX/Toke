package com.flabs.cobrate.core.logic

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Singleton que monitorea la disponibilidad de internet en tiempo real.
 * CAPA TAREA E-1: NetworkMonitor
 */
object NetworkMonitor {
    private val _disponible = MutableStateFlow(false)
    val disponible: StateFlow<Boolean> = _disponible.asStateFlow()

    private val _redActiva = MutableStateFlow<Network?>(null)
    val redActiva: StateFlow<Network?> = _redActiva.asStateFlow()

    fun init(context: Context) {
        val cm = context.getSystemService(ConnectivityManager::class.java)
            ?: return

        fun evaluarRed() {
            val activeNetwork = cm.activeNetwork
            val caps = cm.getNetworkCapabilities(activeNetwork)
            val tieneInternet = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
            val estaValidada = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true

            if (tieneInternet && estaValidada) {
                _disponible.value = true
                _redActiva.value = activeNetwork
            } else {
                _disponible.value = false
                _redActiva.value = null
            }
        }

        // Callback que reacciona a cambios de red activa
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                evaluarRed()
            }

            override fun onLost(network: Network) {
                evaluarRed()
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                evaluarRed()
            }
        }

        // Registrar callback para la red predeterminada (default network)
        cm.registerDefaultNetworkCallback(callback)

        // Estado inicial
        evaluarRed()
    }
}
