package com.flabs.cobrate.core.data

import com.flabs.cobrate.core.model.EstadoRelacion
import com.flabs.cobrate.core.model.RelacionCuentaNegocio
import com.flabs.cobrate.core.model.RolCuentaNegocio
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Repositorio de Relaciones Cuenta-Negocio conectado a Supabase.
 * CAPA 7d-1.
 */
internal class RelacionCuentaNegocioRepository {

    private val postgrest = SupabaseClientProvider.supabase.postgrest

    suspend fun crear(relacion: RelacionCuentaNegocio): Result<RelacionCuentaNegocio> = runCatching {
        val dto = relacion.toDto()
        val createdDto = postgrest.from("relacion_cuenta_negocio")
            .insert(dto) {
                select()
            }
            .decodeSingle<RelacionDto>()
        createdDto.toDomain()
    }

    suspend fun relacionesDe(cuentaId: String): Result<List<RelacionCuentaNegocio>> = runCatching {
        postgrest.from("relacion_cuenta_negocio")
            .select {
                filter {
                    eq("cuenta_id", cuentaId)
                }
            }
            .decodeList<RelacionDto>()
            .map { it.toDomain() }
    }

    suspend fun relacionesDelNegocio(negocioId: String): Result<List<RelacionCuentaNegocio>> = runCatching {
        postgrest.from("relacion_cuenta_negocio")
            .select {
                filter {
                    eq("negocio_id", negocioId)
                }
            }
            .decodeList<RelacionDto>()
            .map { it.toDomain() }
    }

    /**
     * Actualiza el estado de una relación específica entre cuenta y negocio.
     * CAPA 9.
     */
    suspend fun actualizarEstado(
        cuentaId: String,
        negocioId: String,
        nuevoEstado: EstadoRelacion
    ): Result<Unit> = runCatching {
        postgrest.from("relacion_cuenta_negocio")
            .update(
                mapOf("estado" to nuevoEstado.name)
            ) {
                filter {
                    eq("cuenta_id", cuentaId)
                    eq("negocio_id", negocioId)
                }
            }
    }

    @Serializable
    internal data class RelacionDto(
        @SerialName("cuenta_id") val cuentaId: String,
        @SerialName("negocio_id") val negocioId: String,
        @SerialName("rol") val rol: RolCuentaNegocio,
        @SerialName("estado") val estado: EstadoRelacion,
        @SerialName("fecha_vinculacion") val fechaVinculacion: Long
    )
}

internal fun RelacionCuentaNegocio.toDto() = RelacionCuentaNegocioRepository.RelacionDto(
    cuentaId = cuentaId,
    negocioId = negocioId,
    rol = rol,
    estado = estado,
    fechaVinculacion = fechaVinculacion
)

internal fun RelacionCuentaNegocioRepository.RelacionDto.toDomain() = RelacionCuentaNegocio(
    cuentaId = cuentaId,
    negocioId = negocioId,
    rol = rol,
    estado = estado,
    fechaVinculacion = fechaVinculacion
)
