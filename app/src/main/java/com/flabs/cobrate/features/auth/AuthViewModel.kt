package com.flabs.cobrate.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flabs.cobrate.core.data.CuentaStore
import com.flabs.cobrate.core.data.DispositivoRepository
import com.flabs.cobrate.core.data.SupabaseAuthBridge
import com.flabs.cobrate.core.logic.RegistroDispositivos
import com.flabs.cobrate.core.model.Cuenta
import com.flabs.cobrate.services.FirebaseProveedorDeToken
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.Result

/**
 * ViewModel de autenticación (CAPA 7b-2 y CAPA 52).
 * Implementa el login real con Supabase Auth y des-registro de token FCM al cerrar sesión.
 */
class AuthViewModel : ViewModel() {

    /** Cuenta actualmente logueada (null si no hay sesión activa). */
    val sesionActual: StateFlow<Cuenta?> = CuentaStore.sesionActual

    /** Crea una cuenta Dueño en Supabase. */
    suspend fun crearCuenta(email: String, password: String): Result<Unit> {
        return SupabaseAuthBridge.crearCuentaDueño(email, password)
    }

    /** Inicia sesión de Dueño en Supabase. */
    suspend fun iniciarSesion(email: String, password: String): Result<Unit> {
        return SupabaseAuthBridge.iniciarSesionDueño(email, password)
    }

    /** Cierra la sesión local y en Supabase previa quita de token FCM. */
    fun cerrarSesion() {
        viewModelScope.launch {
            withTimeoutOrNull(3000L) {
                RegistroDispositivos.quitar(FirebaseProveedorDeToken(), DispositivoRepository())
            }
            // Invertimos el orden: Primero cerramos Realtime y limpiamos Stores
            // mientras el token de sesión sigue siendo válido en el cliente HTTP.
            CuentaStore.cerrarSesion()
            SupabaseAuthBridge.cerrarSesionSupabase()
        }
    }
}
