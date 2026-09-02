package com.flabs.cobrate.core.data

import com.flabs.cobrate.core.model.EstadoInvitacion
import com.flabs.cobrate.core.model.InvitacionTrabajador
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Repositorio de Invitaciones de Trabajador conectado a Supabase.
 * CAPA 7d-1.
 */
internal class InvitacionRepository {

    private val postgrest = SupabaseClientProvider.supabase.postgrest

    suspend fun crear(negocioId: String, nombrePropuesto: String?, codigo: String, expiracion: Long): Result<InvitacionTrabajador> = runCatching {
        val dto = InvitacionDto(
            codigo = codigo,
            negocioId = negocioId,
            nombrePropuesto = nombrePropuesto,
            metodo = "APP", // Valor por defecto
            estado = EstadoInvitacion.PENDIENTE,
            expiracion = expiracion,
            fechaCreacion = System.currentTimeMillis()
        )
        val createdDto = postgrest.from("invitaciones_trabajador")
            .insert(dto) {
                select()
            }
            .decodeSingle<InvitacionDto>()
        createdDto.toDomain()
    }

    suspend fun buscarPorCodigo(codigo: String): Result<InvitacionTrabajador?> = runCatching {
        val dto = postgrest.from("invitaciones_trabajador")
            .select {
                filter {
                    eq("codigo", codigo)
                }
            }
            .decodeSingleOrNull<InvitacionDto>()
        dto?.toDomain()
    }

    suspend fun marcarAceptada(codigo: String): Result<Unit> = runCatching {
        postgrest.from("invitaciones_trabajador")
            .update(
                mapOf("estado" to EstadoInvitacion.ACEPTADA.name)
            ) {
                filter {
                    eq("codigo", codigo)
                }
            }
    }

    @Serializable
    internal data class InvitacionDto(
        @SerialName("codigo") val codigo: String,
        @SerialName("negocio_id") val negocioId: String,
        @SerialName("nombre_propuesto") val nombrePropuesto: String?,
        @SerialName("metodo") val metodo: String,
        @SerialName("estado") val estado: EstadoInvitacion,
        @SerialName("expiracion") val expiracion: Long,
        @SerialName("fecha_creacion") val fechaCreacion: Long
    )
}

internal fun InvitacionRepository.InvitacionDto.toDomain() = InvitacionTrabajador(
    id = codigo, // El dominio usa id, mapeamos codigo a id.
    negocioId = negocioId,
    codigo = codigo,
    estado = estado,
    fechaCreacion = fechaCreacion,
    expiracion = expiracion
)
