package com.flabs.cobrate.core.data

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * Puente de Supabase Auth (CAPA 7b-1) — EXPERIMENTAL.
 *
 * Expone operaciones de autenticación contra Supabase Auth, sin conectarlas
 * todavía a las pantallas reales del producto. El login local (hash, CAPA 2)
 * sigue siendo el ÚNICO activo en la app; esta capa es de validación manual.
 *
 * Decisiones de producto confirmadas:
 *  - Dueño   -> email + contraseña reales (signUpWith/signInWith con Email).
 *  - Trabajador -> sesión anónima de Supabase (sin correo); su vínculo real
 *    al negocio sigue siendo la invitación por código ya existente. La sesión
 *    anónima solo otorga una identidad válida de Supabase para habilitar RLS
 *    en el futuro.
 *
 * Todas las funciones capturan excepciones y devuelven [Result]. No deben
 * crashear la app si Supabase no responde (sin conexión, error de red, etc.).
 */
object SupabaseAuthBridge {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /**
     * Usuario autenticado actual en Supabase, o null si no hay sesión.
     * Observa `supabase.auth.sessionStatus` en tiempo real.
     */
    val sesionActual: StateFlow<User?> =
        SupabaseClientProvider.supabase.auth.sessionStatus
            .map { status ->
                when (status) {
                    is SessionStatus.Authenticated -> status.session.user
                    else -> null
                }
            }
            .stateIn(scope, SharingStarted.Eagerly, null)

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
}