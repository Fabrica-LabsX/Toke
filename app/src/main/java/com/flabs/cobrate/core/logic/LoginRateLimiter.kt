package com.flabs.cobrate.core.logic

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * Control de intentos fallidos de inicio de sesión (Rate Limiting).
 * Bloquea temporalmente las solicitudes de login por correo tras múltiples fallos consecutivos.
 */
object LoginRateLimiter {

    private const val PREFS_NAME = "toke_login_rate_limiter_prefs"
    private const val KEY_PREFIX_FALLOS = "fallos_"
    private const val KEY_PREFIX_LOCK_UNTIL = "lock_until_"

    private const val INTENTOS_LIBRES = 2
    private const val BLOQUEO_3_INTENTOS_MS = 30_000L      // 30 segundos
    private const val BLOQUEO_4_INTENTOS_MS = 60_000L      // 1 minuto
    private const val BLOQUEO_MAXIMO_MS = 300_000L         // 5 minutos

    private var prefs: SharedPreferences? = null

    /**
     * Inicializa el almacenamiento persistente de intentos con el contexto de la aplicación.
     */
    @Synchronized
    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    /**
     * Devuelve el timestamp (ms) hasta el cual el correo especificado está bloqueado,
     * o 0L si no está bloqueado.
     */
    @Synchronized
    fun getLockedUntil(email: String): Long {
        val clave = normalizarEmail(email)
        if (clave.isEmpty()) return 0L

        val lockUntil = prefs?.getLong("$KEY_PREFIX_LOCK_UNTIL$clave", 0L) ?: 0L
        val now = System.currentTimeMillis()
        return if (lockUntil > now) lockUntil else 0L
    }

    /**
     * Registra un inicio de sesión exitoso, reiniciando los contadores y bloqueos del correo.
     */
    @Synchronized
    fun registrarExito(email: String) {
        val clave = normalizarEmail(email)
        if (clave.isEmpty()) return

        prefs?.edit {
            remove("$KEY_PREFIX_FALLOS$clave")
            remove("$KEY_PREFIX_LOCK_UNTIL$clave")
        }
    }

    /**
     * Registra un intento fallido de inicio de sesión para el correo.
     * Retorna el timestamp (ms) de bloqueo si superó el límite, o 0L si no está bloqueado.
     */
    @Synchronized
    fun registrarFallo(email: String): Long {
        val clave = normalizarEmail(email)
        if (clave.isEmpty()) return 0L

        val p = prefs ?: return 0L
        val fallosActuales = p.getInt("$KEY_PREFIX_FALLOS$clave", 0) + 1
        val now = System.currentTimeMillis()

        val duracionBloqueoMs = when {
            fallosActuales <= INTENTOS_LIBRES -> 0L
            fallosActuales == 3 -> BLOQUEO_3_INTENTOS_MS
            fallosActuales == 4 -> BLOQUEO_4_INTENTOS_MS
            else -> BLOQUEO_MAXIMO_MS
        }

        val lockUntil = if (duracionBloqueoMs > 0L) now + duracionBloqueoMs else 0L

        p.edit {
            putInt("$KEY_PREFIX_FALLOS$clave", fallosActuales)
            putLong("$KEY_PREFIX_LOCK_UNTIL$clave", lockUntil)
        }

        return lockUntil
    }

    private fun normalizarEmail(email: String): String {
        return email.trim().lowercase()
    }
}
