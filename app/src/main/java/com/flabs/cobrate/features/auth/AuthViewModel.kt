package com.flabs.cobrate.features.auth

import androidx.lifecycle.ViewModel
import com.flabs.cobrate.core.data.CuentaStore
import com.flabs.cobrate.core.logic.AuthValidation
import com.flabs.cobrate.core.model.Cuenta
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

/**
 * ViewModel de autenticación local (CAPA 2 de TOKE V1), sin backend.
 *
 * Expone la sesión actual ([sesionActual]) leyendo de [CuentaStore], y las
 * operaciones crearCuenta / iniciarSesion / cerrarSesion.
 *
 * No contiene lógica de backend: la validación la resuelve [AuthValidation]
 * (funciones puras) y la unicidad de username la garantiza [CuentaStore].
 */
class AuthViewModel : ViewModel() {

    /** Cuenta actualmente logueada (null si no hay sesión activa). */
    val sesionActual: StateFlow<Cuenta?> = CuentaStore.sesionActual

    /**
     * Crea una cuenta nueva y queda logueado automáticamente.
     *
     * Valida username/contraseña, verifica que el username no exista, crea la
     * [Cuenta], la persiste en [CuentaStore] y establece la sesión.
     */
    fun crearCuenta(username: String, password: String): Result<Unit> {
        val usernameLimpio = username.trim()
        if (!AuthValidation.usernameValido(usernameLimpio)) {
            return Result.failure(
                IllegalArgumentException(
                    "El nombre de usuario debe tener al menos 3 caracteres y no contener espacios."
                )
            )
        }
        if (!AuthValidation.passwordValida(password)) {
            return Result.failure(
                IllegalArgumentException(
                    "La contraseña debe tener al menos ${AuthValidation.MIN_LONGITUD_PASSWORD} caracteres."
                )
            )
        }
        if (CuentaStore.buscarPorUsername(usernameLimpio) != null) {
            return Result.failure(
                IllegalArgumentException("Ese nombre de usuario ya existe.")
            )
        }

        val cuenta = Cuenta(
            id = UUID.randomUUID().toString(),
            username = usernameLimpio,
            fechaCreacion = System.currentTimeMillis()
        )
        val hash = AuthValidation.hashPassword(password)

        CuentaStore.crear(cuenta, hash)
        CuentaStore.iniciarSesion(cuenta)
        return Result.success(Unit)
    }

    /**
     * Inicia sesión con username/contraseña si coinciden con lo guardado.
     */
    fun iniciarSesion(username: String, password: String): Result<Unit> {
        val usernameLimpio = username.trim()
        val cuenta = CuentaStore.buscarPorUsername(usernameLimpio)
        val hashGuardado = cuenta?.let { CuentaStore.hashDe(it.id) }

        if (cuenta == null || hashGuardado == null ||
            hashGuardado != AuthValidation.hashPassword(password)
        ) {
            return Result.failure(
                IllegalArgumentException("Usuario o contraseña incorrectos.")
            )
        }

        CuentaStore.iniciarSesion(cuenta)
        return Result.success(Unit)
    }

    /** Cierra la sesión actual. */
    fun cerrarSesion() {
        CuentaStore.cerrarSesion()
    }
}