package com.flabs.cobrate.core.data

import com.flabs.cobrate.core.model.Cuenta
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Repositorio de Cuentas conectado a Supabase.
 * CAPA 7d-4.
 */
internal class CuentaRepository {

    private val postgrest = SupabaseClientProvider.supabase.postgrest

    /**
     * Inserta o actualiza la información de la cuenta en Supabase.
     */
    suspend fun upsert(cuenta: Cuenta): Result<Unit> = runCatching {
        val dto = cuenta.toDto()
        postgrest.from("cuentas").upsert(dto)
    }

    /**
     * Obtiene una cuenta por su id desde Supabase.
     * CAPA 9.
     */
    suspend fun obtenerPorId(id: String): Result<Cuenta?> = runCatching {
        postgrest.from("cuentas")
            .select {
                filter {
                    eq("id", id)
                }
            }
            .decodeSingleOrNull<CuentaDto>()
            ?.toDomain()
    }

    /**
     * Obtiene varias cuentas por sus IDs en una sola petición.
     */
    suspend fun obtenerVariosPorId(ids: List<String>): Result<List<Cuenta>> = runCatching {
        if (ids.isEmpty()) return@runCatching emptyList()
        postgrest.from("cuentas")
            .select {
                filter {
                    isIn("id", ids)
                }
            }
            .decodeList<CuentaDto>()
            .map { it.toDomain() }
    }

    @Serializable
    internal data class CuentaDto(
        @SerialName("id") val id: String,
        @SerialName("username") val username: String,
        @SerialName("correo_verificado") val correoVerificado: String?,
        @SerialName("telefono_verificado") val telefonoVerificado: String?,
        @SerialName("fecha_creacion") val fechaCreacion: Long,
        @SerialName("ultimo_acceso") val ultimoAcceso: Long
    )
}

internal fun Cuenta.toDto() = CuentaRepository.CuentaDto(
    id = id,
    username = username,
    correoVerificado = correoVerificado,
    telefonoVerificado = telefonoVerificado,
    fechaCreacion = fechaCreacion,
    ultimoAcceso = ultimoAcceso
)

internal fun CuentaRepository.CuentaDto.toDomain() = Cuenta(
    id = id,
    username = username,
    correoVerificado = correoVerificado,
    telefonoVerificado = telefonoVerificado,
    fechaCreacion = fechaCreacion,
    ultimoAcceso = ultimoAcceso
)
