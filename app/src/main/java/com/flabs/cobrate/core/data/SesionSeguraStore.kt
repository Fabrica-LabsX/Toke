package com.flabs.cobrate.core.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.KeyStore

/**
 * Almacenamiento cifrado de la "sesión activa" (id de la cuenta actualmente
 * logueada) usando [EncryptedSharedPreferences] con un [MasterKey] respaldado
 * por el Android Keystore.
 *
 * Propósito (Capa 2b): endurecer únicamente el dato de "quién es la sesión
 * activa", que es lo que permite restaurar la sesión sin pedir login al
 * reabrir la app. La lista de cuentas (con su hash) sigue en el DataStore
 * normal por ahora; esta capa NO migra ese contenido.
 *
 * Uso: instanciar una vez con el contexto de la app y compartir la instancia.
 * Todas las operaciones son síncronas (EncryptedSharedPreferences no expone
 * flujos), por lo que los consumidores deben invocarlas fuera del hilo de UI.
 */
@Suppress("DEPRECATION")
internal class SesionSeguraStore(context: Context) {

    private val prefs: SharedPreferences by lazy {
        crearEncryptedPrefsOFallback(context)
    }

    private fun crearEncryptedPrefsOFallback(context: Context): SharedPreferences {
        return try {
            crearEncryptedPrefs(context)
        } catch (e: Exception) {
            Log.e(TAG, "Error al abrir EncryptedSharedPreferences. Limpiando llaves corruptas y reintentando...", e)
            limpiarAlmacenamientoCorrupto(context)
            try {
                crearEncryptedPrefs(context)
            } catch (e2: Exception) {
                Log.e(TAG, "Falló segundo intento de EncryptedSharedPreferences. Fallback a SharedPreferences normal.", e2)
                context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
            }
        }
    }

    private fun limpiarAlmacenamientoCorrupto(context: Context) {
        try {
            context.deleteSharedPreferences(FILE_NAME)
        } catch (e: Exception) {
            Log.e(TAG, "Error al borrar SharedPreferences $FILE_NAME", e)
        }
        try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            keyStore.deleteEntry(MasterKey.DEFAULT_MASTER_KEY_ALIAS)
        } catch (e: Exception) {
            Log.e(TAG, "Error al borrar MasterKey de KeyStore", e)
        }
    }

    private fun crearEncryptedPrefs(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    /** Id de la cuenta de la sesión activa (null si no hay). */
    fun leerSesionActiva(): String? = try {
        prefs.getString(KEY_SESION_ACTIVA_ID, null)
    } catch (e: Exception) {
        Log.e(TAG, "Error leyendo sesión activa", e)
        null
    }

    /** Guarda el id de la sesión activa en almacenamiento cifrado. */
    fun guardarSesionActiva(cuentaId: String) {
        try {
            prefs.edit().putString(KEY_SESION_ACTIVA_ID, cuentaId).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error guardando sesión activa", e)
        }
    }

    /** Deja sin sesión activa guardada. */
    fun limpiarSesion() {
        try {
            prefs.edit().remove(KEY_SESION_ACTIVA_ID).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error limpiando sesión activa", e)
        }
    }

    private companion object {
        const val TAG = "SesionSeguraStore"
        const val FILE_NAME = "sesion_segura"
        const val KEY_SESION_ACTIVA_ID = "sesion_activa_id"
    }
}