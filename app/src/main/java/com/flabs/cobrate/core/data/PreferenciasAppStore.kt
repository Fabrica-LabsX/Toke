package com.flabs.cobrate.core.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.flabs.cobrate.ui.theme.ThemeMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Persistencia de preferencias globales de la aplicación aisladas por cuenta (UID).
 * CAPA 8-TEMA.
 */
object PreferenciasAppStore {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _themeMode = MutableStateFlow(ThemeMode.LIGHT)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _avisarCobroEnviado = MutableStateFlow(false)
    val avisarCobroEnviado: StateFlow<Boolean> = _avisarCobroEnviado.asStateFlow()

    private val _avisoBateriaMostrado = MutableStateFlow(false)
    val avisoBateriaMostrado: StateFlow<Boolean> = _avisoBateriaMostrado.asStateFlow()

    private val _conteoAperturas = MutableStateFlow(0)
    val conteoAperturas: StateFlow<Int> = _conteoAperturas.asStateFlow()

    private var currentSessionUid: String? = null
    private var appContext: Context? = null

    private val Context.dataStore by preferencesDataStore(name = "preferencias_app")

    private fun keyTema(uid: String) = stringPreferencesKey("theme_$uid")
    private fun keyAvisarCobroEnviado(uid: String) = booleanPreferencesKey("avisar_cobro_enviado_$uid")
    private fun keyAvisoBateriaMostrado(uid: String) = booleanPreferencesKey("aviso_bateria_mostrado_$uid")
    private fun keyConteoAperturas(uid: String) = androidx.datastore.preferences.core.intPreferencesKey("conteo_aperturas_$uid")

    /**
     * Inicializa el store y observa la sesión para cargar las preferencias correspondientes.
     */
    fun init(context: Context) {
        val ctx = context.applicationContext
        appContext = ctx
        
        scope.launch {
            CuentaStore.sesionActual.collect { cuenta ->
                val newUid = cuenta?.id
                if (newUid != currentSessionUid) {
                    currentSessionUid = newUid
                    if (newUid != null) {
                        cargarPreferencias(newUid)
                    } else {
                        // Antes de login o tras logout -> valores por defecto
                        _themeMode.value = ThemeMode.LIGHT
                        _avisarCobroEnviado.value = false
                        _avisoBateriaMostrado.value = false
                        _conteoAperturas.value = 0
                    }
                }
            }
        }
    }

    private suspend fun cargarPreferencias(uid: String) {
        val ctx = appContext ?: return
        val (tema, avisar, bateriaMostrado, conteo) = ctx.dataStore.data.map { prefs ->
            val temaSaved = prefs[keyTema(uid)]?.let { name ->
                runCatching { ThemeMode.valueOf(name) }.getOrNull()
            } ?: ThemeMode.LIGHT
            val avisarSaved = prefs[keyAvisarCobroEnviado(uid)] ?: false
            val bateriaSaved = prefs[keyAvisoBateriaMostrado(uid)] ?: false
            val conteoActual = prefs[keyConteoAperturas(uid)] ?: 0
            listOf(temaSaved, avisarSaved, bateriaSaved, conteoActual)
        }.first()

        val nuevoConteo = (conteo as Int) + 1
        scope.launch {
            ctx.dataStore.edit { prefs ->
                prefs[keyConteoAperturas(uid)] = nuevoConteo
            }
        }

        @Suppress("UNCHECKED_CAST")
        _themeMode.value = tema as ThemeMode
        _avisarCobroEnviado.value = avisar as Boolean
        _avisoBateriaMostrado.value = bateriaMostrado as Boolean
        _conteoAperturas.value = nuevoConteo
    }

    /**
     * Cambia el tema global y lo persiste para la cuenta activa.
     */
    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        val uid = currentSessionUid
        val ctx = appContext
        if (uid != null && ctx != null) {
            scope.launch {
                ctx.dataStore.edit { prefs ->
                    prefs[keyTema(uid)] = mode.name
                }
            }
        }
    }

    /**
     * Cambia la opción de avisar cuando se envía un cobro a trabajadores y la persiste por UID.
     */
    fun setAvisarCobroEnviado(enabled: Boolean) {
        _avisarCobroEnviado.value = enabled
        val uid = currentSessionUid
        val ctx = appContext
        if (uid != null && ctx != null) {
            scope.launch {
                ctx.dataStore.edit { prefs ->
                    prefs[keyAvisarCobroEnviado(uid)] = enabled
                }
            }
        }
    }

    /**
     * Marca el aviso de batería como mostrado para el usuario activo (CAPA 63).
     */
    fun marcarAvisoBateriaMostrado() {
        _avisoBateriaMostrado.value = true
        val uid = currentSessionUid
        val ctx = appContext
        if (uid != null && ctx != null) {
            scope.launch {
                ctx.dataStore.edit { prefs ->
                    prefs[keyAvisoBateriaMostrado(uid)] = true
                }
            }
        }
    }
}
