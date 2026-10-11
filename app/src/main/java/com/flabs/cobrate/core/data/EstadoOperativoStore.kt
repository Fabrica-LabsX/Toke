package com.flabs.cobrate.core.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Fuente del estado operativo global de la aplicación (encendido vs. apagado).
 * CAPA 57.
 */
object EstadoOperativoStore {
    private const val PREFS_NAME = "estado_operativo_prefs"
    private const val KEY_ENCENDIDO = "estado_operativo_encendido"

    private val _encendido = MutableStateFlow<Boolean>(true)
    val encendido: StateFlow<Boolean> = _encendido.asStateFlow()

    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        if (prefs == null) {
            val appContext = context.applicationContext
            val sp = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs = sp
            _encendido.value = sp.getBoolean(KEY_ENCENDIDO, true)
        }
    }

    fun setEncendido(context: Context? = null, valor: Boolean) {
        _encendido.value = valor
        context?.let { init(it) }
        prefs?.edit()?.putBoolean(KEY_ENCENDIDO, valor)?.apply()
    }
}
