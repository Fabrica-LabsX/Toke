package com.flabs.cobrate.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flabs.cobrate.core.data.CuentaStore
import com.flabs.cobrate.core.data.DispositivoRepository
import com.flabs.cobrate.core.data.SupabaseAuthBridge
import com.flabs.cobrate.core.logic.LoginRateLimiter
import com.flabs.cobrate.core.logic.RegistroDispositivos
import com.flabs.cobrate.core.model.Cuenta
import com.flabs.cobrate.services.FirebaseProveedorDeToken
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.Result

/**
 * ViewModel de autenticación (CAPA Auth, Rate Limiting & Recuperación en Línea).
 */
class AuthViewModel : ViewModel() {

    /** Cuenta actualmente logueada (null si no hay sesión activa). */
    val sesionActual: StateFlow<Cuenta?> = CuentaStore.sesionActual

    /** Inicia sesión de Dueño en Supabase con comprobación de rate limit. */
    suspend fun iniciarSesion(email: String, password: String): Result<Unit> {
        val lockUntil = LoginRateLimiter.getLockedUntil(email)
        val now = System.currentTimeMillis()
        if (lockUntil > now) {
            val remSecs = (lockUntil - now) / 1000L + 1L
            return Result.failure(Exception("Demasiados intentos fallidos. Intenta de nuevo en ${remSecs}s."))
        }

        val result = SupabaseAuthBridge.iniciarSesionDueño(email, password)
        if (result.isSuccess) {
            LoginRateLimiter.registrarExito(email)
        } else {
            val ex = result.exceptionOrNull()
            val msg = ex?.message?.lowercase() ?: ""
            if (msg.contains("invalid") || msg.contains("credentials") || msg.contains("grant") || msg.contains("wrong")) {
                val newLock = LoginRateLimiter.registrarFallo(email)
                if (newLock > now) {
                    val remSecs = (newLock - now) / 1000L + 1L
                    return Result.failure(Exception("Credenciales incorrectas. Bloqueado temporalmente por ${remSecs}s."))
                }
            }
        }
        return result
    }

    /** Solicita correo con OTP de recuperación. */
    suspend fun solicitarRecuperacionPassword(email: String): Result<Unit> {
        return SupabaseAuthBridge.solicitarRecuperacionPassword(email)
    }

    /** Verifica OTP de recuperación. */
    suspend fun verificarOtpRecuperacion(email: String, token: String): Result<Unit> {
        return SupabaseAuthBridge.verificarOtpRecuperacion(email, token)
    }

    /** Actualiza contraseña tras recuperación. */
    suspend fun actualizarPasswordYRevocarOtros(nuevaPassword: String): Result<Unit> {
        return SupabaseAuthBridge.actualizarPasswordYRevocarOtros(nuevaPassword)
    }

    /** Paso 1 & 2 de Registro: solicita OTP enviando correo de confirmación. */
    suspend fun solicitarOtpRegistro(email: String): Result<Unit> {
        return SupabaseAuthBridge.solicitarOtpRegistro(email)
    }

    /** Paso 3 de Registro: verifica OTP de confirmación. */
    suspend fun verificarOtpRegistro(email: String, token: String): Result<Unit> {
        return SupabaseAuthBridge.verificarOtpRegistro(email, token)
    }

    /** Paso 4 & 5 de Registro: establece nombre y contraseña elegida. */
    suspend fun completarRegistro(nombre: String, passwordElegida: String): Result<Unit> {
        return SupabaseAuthBridge.completarRegistro(nombre, passwordElegida)
    }

    /** La cuenta ya fue eliminada en el servidor: se borra todo lo local y se vuelve al acceso (sin llamar a la red). */
    fun cuentaEliminada() {
        viewModelScope.launch {
            CuentaStore.cerrarSesion()
            SupabaseAuthBridge.limpiarSesionLocal()
        }
    }

    /** Cierra la sesión local y en Supabase previa quita de token FCM. */
    fun cerrarSesion() {
        viewModelScope.launch {
            // Libera la cuenta para que el siguiente celular entre sin preguntar.
            com.flabs.cobrate.core.data.SesionUnicaStore.liberar(CuentaStore.sesionActual.value?.id)
            withTimeoutOrNull(3000L) {
                RegistroDispositivos.quitar(FirebaseProveedorDeToken(), DispositivoRepository())
            }
            CuentaStore.cerrarSesion()
            SupabaseAuthBridge.cerrarSesionSupabase()
        }
    }
}
