package com.flabs.cobrate.core.data

import com.flabs.cobrate.core.model.Negocio
import com.flabs.cobrate.core.model.TipoNegocio
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Repositorio de Negocios conectado a Supabase (Postgrest).
 * CAPA 7d-1.
 */
internal class NegocioRepository {

    private val postgrest = SupabaseClientProvider.supabase.postgrest

    suspend fun crear(negocio: Negocio): Result<Negocio> = runCatching {
        val dto = negocio.toDto()
        val createdDto = postgrest.from("negocios")
            .insert(dto) {
                select()
            }
            .decodeSingle<NegocioDto>()
        createdDto.toDomain()
    }

    suspend fun obtenerPorId(id: String): Result<Negocio?> = runCatching {
        val dto = postgrest.from("negocios")
            .select {
                filter {
                    eq("id", id)
                }
            }
            .decodeSingleOrNull<NegocioDto>()
        dto?.toDomain()
    }

    /**
     * Obtiene una lista de negocios por sus IDs en una sola petición.
     */
    suspend fun obtenerVariosPorId(ids: List<String>): Result<List<Negocio>> = runCatching {
        if (ids.isEmpty()) return@runCatching emptyList()
        postgrest.from("negocios")
            .select {
                filter {
                    isIn("id", ids)
                }
            }
            .decodeList<NegocioDto>()
            .map { it.toDomain() }
    }

    suspend fun obtenerNegociosDeCuenta(cuentaId: String): Result<List<Negocio>> = runCatching {
        // Negocios donde es dueño O donde existe una relación activa.
        // En Supabase, esto se puede hacer con un select con join o via RPC si es complejo.
        // Para CAPA 7d-1, implementamos la lógica simple de consulta:
        // 1. Negocios donde dueno_cuenta_id = cuentaId.
        val negociosPropios = postgrest.from("negocios")
            .select {
                filter {
                    eq("dueno_cuenta_id", cuentaId)
                }
            }
            .decodeList<NegocioDto>()

        // 2. Negocios donde existe una relacion activa en relacion_cuenta_negocio.
        val relacionesActivas = postgrest.from("relacion_cuenta_negocio")
            .select {
                filter {
                    eq("cuenta_id", cuentaId)
                    eq("estado", "ACTIVA")
                }
            }
            .decodeList<NegocioRelacionDto>()

        val idsOtrosNegocios = relacionesActivas.map { it.negocioId }.filter { id ->
            negociosPropios.none { it.id == id }
        }

        val otrosNegocios = if (idsOtrosNegocios.isNotEmpty()) {
            postgrest.from("negocios")
                .select {
                    filter {
                        isIn("id", idsOtrosNegocios)
                    }
                }
                .decodeList<NegocioDto>()
        } else emptyList()

        (negociosPropios + otrosNegocios).map { it.toDomain() }
    }

    @Serializable
    internal data class NegocioDto(
        @SerialName("id") val id: String,
        @SerialName("nombre") val nombre: String,
        @SerialName("tipo") val tipo: TipoNegocio,
        @SerialName("dueno_cuenta_id") val duenoCuentaId: String
    )

    @Serializable
    internal data class NegocioRelacionDto(
        @SerialName("negocio_id") val negocioId: String
    )
}

internal fun Negocio.toDto() = NegocioRepository.NegocioDto(
    id = id,
    nombre = nombre,
    tipo = tipo,
    duenoCuentaId = dueñoCuentaId
)

internal fun NegocioRepository.NegocioDto.toDomain() = Negocio(
    id = id,
    nombre = nombre,
    tipo = tipo,
    dueñoCuentaId = duenoCuentaId
)
