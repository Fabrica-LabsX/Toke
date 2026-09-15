package com.flabs.cobrate.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flabs.cobrate.core.data.CuentaStore
import com.flabs.cobrate.core.model.Cuenta
import com.flabs.cobrate.core.data.SupabaseAuthBridge
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.Result

/**
 * ViewModel de autenticación (CAPA 7b-2).
 * Implementa el login real con Supabase Auth (Dueño vía email+contraseña
 * y Trabajador vía sesión anónima).
 */
class AuthViewModel : ViewModel() {

    /** Cuenta actualmente logueada (null si no hay sesión activa). */
    val sesionActual: StateFlow<Cuenta?> = CuentaStore.sesionActual

    /** Crea una cuenta Dueño en Supabase. */
    suspend fun crearCuenta(email: String, password: String): Result<Unit> {
        // Llama a Supabase para crear la cuenta. La persistencia local de la 
        // sesión la maneja CuentaStore al observar SupabaseAuthBridge.sesionActual.
        return SupabaseAuthBridge.crearCuentaDueño(email, password)
    }

    /** Inicia sesión de Dueño en Supabase. */
    suspend fun iniciarSesion(email: String, password: String): Result<Unit> {
        // Llama a Supabase para iniciar sesión. La persistencia local la maneja
        // CuentaStore al observar SupabaseAuthBridge.sesionActual.
        return SupabaseAuthBridge.iniciarSesionDueño(email, password)
    }

    /** Cierra la sesión local y en Supabase. */
    fun cerrarSesion() {
        viewModelScope.launch {
            // Invertimos el orden: Primero cerramos Realtime y limpiamos Stores
            // mientras el token de sesión sigue siendo válido en el cliente HTTP.
            CuentaStore.cerrarSesion()
            SupabaseAuthBridge.cerrarSesionSupabase()
        }
    }
}
