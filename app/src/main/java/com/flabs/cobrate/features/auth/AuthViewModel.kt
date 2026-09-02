package com.flabs.cobrate.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flabs.cobrate.core.data.CuentaStore
import com.flabs.cobrate.core.model.Cuenta
import com.flabs.cobrate.core.data.SupabaseAuthBridge
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
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

    /** Crea una cuenta Dueño en Supabase y la persiste localmente. */
    suspend fun crearCuenta(email: String, password: String): Result<Unit> {
        // 1. Llama a Supabase para crear la cuenta
        val createRes = SupabaseAuthBridge.crearCuentaDueño(email, password)
        if (createRes.isFailure) return createRes
        // 2. Esperamos la sesión activa
        val userInfo = SupabaseAuthBridge.sesionActual.first()
        if (userInfo == null) return Result.failure(Exception("No active Supabase session después de crear cuenta"))
        val uid = userInfo.id
        val username = email
        val correoVerificado = userInfo.email // email is present after signup
        val cuenta = Cuenta(
            id = uid,
            username = username,
            correoVerificado = correoVerificado,
            telefonoVerificado = null,
            ultimoAcceso = System.currentTimeMillis(),
            fechaCreacion = System.currentTimeMillis()
        )
        val hashDummy = ""
        CuentaStore.crear(cuenta, hashDummy)
        CuentaStore.iniciarSesion(cuenta)
        return Result.success(Unit)
    }

    /** Inicia sesión de Dueño en Supabase y sincroniza la Cuenta local. */
    suspend fun iniciarSesion(email: String, password: String): Result<Unit> {
        val siginRes = SupabaseAuthBridge.iniciarSesionDueño(email, password)
        if (siginRes.isFailure) return siginRes
        val userInfo = SupabaseAuthBridge.sesionActual.first()
        if (userInfo == null) return Result.failure(Exception("No active Supabase session después de iniciar sesión"))
        val uid = userInfo.id
        val correo = userInfo.email ?: email
        val username = email
        val existing = CuentaStore.cuentas.value.find { it.id == uid }
        if (existing != null) {
            CuentaStore.iniciarSesion(existing)
            return Result.success(Unit)
        }
        val cuenta = Cuenta(
            id = uid,
            username = username,
            correoVerificado = correo,
            telefonoVerificado = null,
            ultimoAcceso = System.currentTimeMillis(),
            fechaCreacion = System.currentTimeMillis()
        )
        val hashDummy = ""
        CuentaStore.crear(cuenta, hashDummy)
        CuentaStore.iniciarSesion(cuenta)
        return Result.success(Unit)
    }

    /** Cierra la sesión local y en Supabase. */
    fun cerrarSesion() {
        CuentaStore.cerrarSesion()
        viewModelScope.launch {
            SupabaseAuthBridge.cerrarSesionSupabase()
        }
    }
}
