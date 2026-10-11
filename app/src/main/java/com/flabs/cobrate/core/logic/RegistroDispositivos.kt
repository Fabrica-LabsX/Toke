package com.flabs.cobrate.core.logic

import com.flabs.cobrate.core.data.DispositivoRepository

/**
 * Interfaz abstracta para obtener y borrar el token push del dispositivo.
 */
interface ProveedorDeToken {
    suspend fun obtenerToken(): String?
    suspend fun borrarToken()
}

/**
 * Lógica pura de registro/des-registro del dispositivo para notificaciones push (CAPA 52).
 */
object RegistroDispositivos {

    suspend fun registrar(proveedor: ProveedorDeToken, repo: DispositivoRepository): Result<Unit> = runCatching {
        val token = proveedor.obtenerToken()
        if (token.isNullOrBlank()) {
            throw Exception("Token de dispositivo nulo o vacío")
        }
        val resultado = repo.registrar(token).getOrThrow()
        if (!resultado.exito) {
            throw Exception(resultado.mensaje)
        }
    }

    suspend fun quitar(proveedor: ProveedorDeToken, repo: DispositivoRepository): Result<Unit> = runCatching {
        val token = proveedor.obtenerToken()
        if (!token.isNullOrBlank()) {
            repo.quitar(token)
        }
        proveedor.borrarToken()
    }
}
