package com.flabs.cobrate.core.data

import com.flabs.cobrate.core.logic.AuthErrorUtils
import com.flabs.cobrate.core.logic.LogSeguro
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface EstadoSesion {
    object Inicializando : EstadoSesion
    data class Activa(val usuario: UserInfo) : EstadoSesion
    object CerradaPorUsuario : EstadoSesion
    object SinSesion : EstadoSesion
    data class FalloDeRefresco(val porRed: Boolean) : EstadoSesion
}

/**
 * Puente de Supabase Auth (CAPA 7b-1) — EXPERIMENTAL.
 */
object SupabaseAuthBridge {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /**
     * Estado de inicialización de Supabase Auth.
     */
    val estaInicializando: StateFlow<Boolean> =
        SupabaseClientProvider.supabase.auth.sessionStatus
            .map { it is SessionStatus.Initializing }
            .distinctUntilChanged()
            .stateIn(scope, SharingStarted.Eagerly, true)

    /**
     * Usuario autenticado actual en Supabase, o null si no hay sesión.
     * Estabilizado: Filtra estados transitorios (como Initializing) para evitar
     * que la UI reaccione a falsos cierres de sesión al volver de background.
     */
    val sesionActual: StateFlow<UserInfo?> =
        SupabaseClientProvider.supabase.auth.sessionStatus
            .filter { it !is SessionStatus.Initializing }
            .map { status ->
                when (status) {
                    is SessionStatus.Authenticated -> status.session?.user ?: status.session.user
                    else -> null
                }
            }
            .distinctUntilChanged()
            .stateIn(scope, SharingStarted.Eagerly, null)

    /**
     * Estado detallado de la sesión de Supabase Auth (CAPA 20).
     */
    val estadoSesion: StateFlow<EstadoSesion> =
        SupabaseClientProvider.supabase.auth.sessionStatus
            .map { status ->
                when (status) {
                    is SessionStatus.Initializing -> EstadoSesion.Inicializando
                    is SessionStatus.Authenticated -> {
                        val usuario = status.session?.user ?: status.session.user
                        if (usuario != null) EstadoSesion.Activa(usuario) else EstadoSesion.SinSesion
                    }
                    is SessionStatus.NotAuthenticated -> {
                        if (status.isSignOut) EstadoSesion.CerradaPorUsuario
                        else EstadoSesion.SinSesion
                    }
                    is SessionStatus.RefreshFailure -> {
                        @Suppress("DEPRECATION")
                        val cause = status.cause
                        @Suppress("DEPRECATION")
                        val porRed = esErrorDeRed(cause)
                        if (!porRed) {
                            LogSeguro.w(
                                "SupabaseAuthBridge",
                                "Fallo de refresco por token revocado o invalido. Limpiando sesion local Supabase.",
                                cause as? Throwable
                            )
                            scope.launch {
                                runCatching { SupabaseClientProvider.supabase.auth.clearSession() }
                            }
                        }
                        EstadoSesion.FalloDeRefresco(porRed)
                    }
                }
            }
            .distinctUntilChanged()
            .stateIn(scope, SharingStarted.Eagerly, EstadoSesion.Inicializando)

    internal fun esErrorDeRed(cause: Any?): Boolean = AuthErrorUtils.esErrorDeRed(cause)

    /** Crea una cuenta de Dueño con email + contraseña reales en Supabase Auth. */
    suspend fun crearCuentaDueño(email: String, password: String): Result<Unit> = runCatching {
        SupabaseClientProvider.supabase.auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
    }

    /** Inicia sesión de Dueño con email + contraseña reales en Supabase Auth. */
    suspend fun iniciarSesionDueño(email: String, password: String): Result<Unit> = runCatching {
        SupabaseClientProvider.supabase.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    /** Inicia sesión de Trabajador como sesión anónima de Supabase (sin correo). */
    suspend fun iniciarSesionTrabajadorAnonimo(): Result<Unit> = runCatching {
        SupabaseClientProvider.supabase.auth.signInAnonymously()
    }

    /** Cierra la sesión actual de Supabase, si existe. */
    suspend fun cerrarSesionSupabase(): Result<Unit> = runCatching {
        SupabaseClientProvider.supabase.auth.signOut()
    }

    /** Limpia la sesión guardada localmente en Supabase Auth sin hacer llamadas de red. */
    suspend fun limpiarSesionLocal(): Result<Unit> = runCatching {
        SupabaseClientProvider.supabase.auth.clearSession()
    }
}
