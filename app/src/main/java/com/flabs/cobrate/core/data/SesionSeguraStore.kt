package com.flabs.cobrate.core.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

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
internal class SesionSeguraStore(context: Context) {

    private val prefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    /** Id de la cuenta de la sesión activa (null si no hay). */
    fun leerSesionActiva(): String? = prefs.getString(KEY_SESION_ACTIVA_ID, null)

    /** Guarda el id de la sesión activa en almacenamiento cifrado. */
    fun guardarSesionActiva(cuentaId: String) {
        prefs.edit().putString(KEY_SESION_ACTIVA_ID, cuentaId).apply()
    }

    /** Deja sin sesión activa guardada. */
    fun limpiarSesion() {
        prefs.edit().remove(KEY_SESION_ACTIVA_ID).apply()
    }

    private companion object {
        const val FILE_NAME = "sesion_segura"
        const val KEY_SESION_ACTIVA_ID = "sesion_activa_id"
    }
}