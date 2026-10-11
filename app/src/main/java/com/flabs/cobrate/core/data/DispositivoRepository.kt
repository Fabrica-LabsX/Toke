package com.flabs.cobrate.core.data

import com.flabs.cobrate.core.logic.LogSeguro
import com.flabs.cobrate.core.model.ResultadoRpc
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Repositorio para registrar y quitar tokens de dispositivos en Supabase (CAPA 52).
 */
class DispositivoRepository {

    private val postgrest = SupabaseClientProvider.supabase.postgrest

    suspend fun registrar(token: String): Result<ResultadoRpc> = runCatching {
        LogSeguro.d(TAG, "Registrando token de dispositivo: ${token.take(6)}...")
        val respuesta = postgrest.rpc(
            function = "registrar_dispositivo",
            parameters = buildJsonObject {
                put("p_token", token)
            }
        ).decodeAs<ResultadoRpc>()

        if (!respuesta.exito) {
            throw Exception(respuesta.mensaje)
        }
        respuesta
    }

    suspend fun quitar(token: String): Result<ResultadoRpc> = runCatching {
        LogSeguro.d(TAG, "Quitando token de dispositivo: ${token.take(6)}...")
        val respuesta = postgrest.rpc(
            function = "quitar_dispositivo",
            parameters = buildJsonObject {
                put("p_token", token)
            }
        ).decodeAs<ResultadoRpc>()

        if (!respuesta.exito) {
            throw Exception(respuesta.mensaje)
        }
        respuesta
    }

    private companion object {
        const val TAG = "DispositivoRepo"
    }
}
