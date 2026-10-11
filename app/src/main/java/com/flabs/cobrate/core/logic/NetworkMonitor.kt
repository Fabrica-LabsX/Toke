package com.flabs.cobrate.core.logic

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
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

    fun init(context: Context) {
        val cm = context.getSystemService(ConnectivityManager::class.java)
            ?: return

        // Callback que reacciona a cambios de red
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                _disponible.value = true
            }

            override fun onLost(network: Network) {
                _disponible.value = false
            }
        }

        // Registrar callback
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        cm.registerNetworkCallback(request, callback)

        // Estado inicial
        _disponible.value = cm.activeNetwork?.let {
            cm.getNetworkCapabilities(it)?.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_INTERNET
            )
        } ?: false
    }
}
