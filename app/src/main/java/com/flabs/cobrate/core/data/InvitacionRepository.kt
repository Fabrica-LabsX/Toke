package com.flabs.cobrate.core.data

import android.util.Log
import com.flabs.cobrate.core.logic.LogSeguro
import com.flabs.cobrate.core.model.EstadoInvitacion
import com.flabs.cobrate.core.model.InvitacionTrabajador
import com.flabs.cobrate.core.model.PreviewInvitacion
import com.flabs.cobrate.core.model.ResultadoRpc
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@OptIn(ExperimentalSerializationApi::class)
object SafeEstadoInvitacionSerializer : KSerializer<EstadoInvitacion?> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("EstadoInvitacion", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: EstadoInvitacion?) = encoder.encodeString(value?.name ?: "")
    override fun deserialize(decoder: Decoder): EstadoInvitacion? {
        val raw = decoder.decodeString().uppercase()
        return runCatching { EstadoInvitacion.valueOf(raw) }.getOrElse {
            LogSeguro.w("InvitacionDto", "EstadoInvitacion no reconocido: $raw")
            null
        }
    }
}

/**
 * Repositorio de Invitaciones de Trabajador conectado a Supabase.
 * CAPA 7d-1.
 */
internal class InvitacionRepository {

    private val postgrest = SupabaseClientProvider.supabase.postgrest

    suspend fun crear(
        negocioId: String,
        nombrePropuesto: String?,
        codigo: String,
        expiracion: Long,
        requiereAprobacion: Boolean = true
    ): Result<InvitacionTrabajador> = runCatching {
        val dto = InvitacionDto(
            codigo = codigo,
            negocioId = negocioId,
            nombrePropuesto = nombrePropuesto,
            metodo = "CODIGO", // Valor correcto para la tabla
            estado = EstadoInvitacion.PENDIENTE,
            expiracion = expiracion,
            fechaCreacion = System.currentTimeMillis(),
            requiereAprobacion = requiereAprobacion
        )
        val createdDto = postgrest.from("invitaciones_trabajador")
            .insert(dto) {
                select()
            }
            .decodeSingle<InvitacionDto>()
        createdDto.toDomainOrNull() ?: throw Exception("Estado inválido en invitación creada")
    }

    suspend fun buscarPorCodigo(codigo: String): Result<InvitacionTrabajador?> = runCatching {
        val dto = postgrest.from("invitaciones_trabajador")
            .select {
                filter {
                    eq("codigo", codigo)
                }
            }
            .decodeSingleOrNull<InvitacionDto>()
        dto?.toDomainOrNull()
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

    /**
     * Limpia en Supabase los códigos expirados (marca como REVOCADA los vencidos y purga los de más de 1 hora).
     */
    suspend fun limpiarExpirados(): Result<Unit> = runCatching {
        postgrest.rpc("limpiar_codigos_expirados")
    }

    /**
     * Consulta las invitaciones PENDIENTES de un negocio (CAPA 44).
     */
    suspend fun codigosActivos(negocioId: String): Result<List<InvitacionTrabajador>> = runCatching {
        runCatching { limpiarExpirados() }
        val ahora = System.currentTimeMillis()
        postgrest.from("invitaciones_trabajador")
            .select {
                filter {
                    eq("negocio_id", negocioId)
                    eq("estado", EstadoInvitacion.PENDIENTE.name)
                }
            }
            .decodeList<InvitacionDto>()
            .mapNotNull { it.toDomainOrNull() }
            .filter { it.expiracion > ahora }
    }

    /**
     * Revoca un código de invitación pendiente vía RPC (CAPA 44).
     */
    suspend fun revocar(codigo: String): Result<ResultadoRpc> = runCatching {
        val respuesta = postgrest.rpc(
            function = "revocar_invitacion",
            parameters = buildJsonObject {
                put("p_codigo", codigo)
            }
        ).decodeAs<ResultadoRpc>()

        if (!respuesta.exito) {
            throw Exception(respuesta.mensaje)
        }
        respuesta
    }

    /**
     * Acepta una invitación de trabajador de forma segura vía RPC.
     * La función en Supabase valida la invitación y crea la relación atómicamente.
     */
    suspend fun aceptarInvitacion(codigo: String): Result<ResultadoAceptarInvitacion> {
        val result = runCatching {
            postgrest.rpc(
                function = "aceptar_invitacion",
                parameters = buildJsonObject {
                    put("codigo_invitacion", codigo)
                }
            ).decodeAs<ResultadoAceptarInvitacion>()
        }

        if (result.isSuccess) {
            val res = result.getOrNull()
            LogSeguro.d("DIAG_INVITACION", "[ACEPTAR] -> SUCCESS: exito=${res?.exito}, mensaje='${res?.mensaje}'")
        } else {
            val ex = result.exceptionOrNull()
            LogSeguro.d("DIAG_INVITACION", "[ACEPTAR] -> FAILURE", ex)
        }

        return result
    }

    /**
     * Obtiene una vista previa segura (nombre negocio y dueño) de una invitación
     * vía RPC, sin requerir permisos de lectura sobre las tablas.
     * CAPA 7d-3.
     */
    suspend fun obtenerPreview(codigo: String): Result<PreviewInvitacion?> {
        val result = runCatching {
            val respuesta = postgrest.rpc(
                function = "obtener_preview_invitacion",
                parameters = buildJsonObject {
                    put("codigo_invitacion", codigo)
                }
            )
            if (respuesta.data.trim() == "null") null else respuesta.decodeAs<PreviewInvitacion>()
        }

        if (result.isSuccess) {
            val prev = result.getOrNull()
            LogSeguro.d("DIAG_INVITACION", "[PREVIEW] -> SUCCESS: previewPresente=${prev != null}")
        } else {
            val ex = result.exceptionOrNull()
            LogSeguro.d("DIAG_INVITACION", "[PREVIEW] -> FAILURE", ex)
        }

        return result
    }

    @Serializable
    internal data class ResultadoAceptarInvitacion(
        @SerialName("exito") val exito: Boolean,
        @SerialName("mensaje") val mensaje: String
    )

    @Serializable
    internal data class InvitacionDto(
        @SerialName("codigo") val codigo: String,
        @SerialName("negocio_id") val negocioId: String,
        @SerialName("nombre_propuesto") val nombrePropuesto: String? = null,
        @SerialName("metodo") val metodo: String,
        @Serializable(with = SafeEstadoInvitacionSerializer::class)
        @SerialName("estado") val estado: EstadoInvitacion? = null,
        @SerialName("expiracion") val expiracion: Long,
        @SerialName("fecha_creacion") val fechaCreacion: Long,
        @SerialName("requiere_aprobacion") val requiereAprobacion: Boolean = true
    )
}

internal fun InvitacionRepository.InvitacionDto.toDomainOrNull(): InvitacionTrabajador? {
    val e = estado ?: return null
    return InvitacionTrabajador(
        id = codigo,
        negocioId = negocioId,
        codigo = codigo,
        estado = e,
        fechaCreacion = fechaCreacion,
        expiracion = expiracion,
        nombrePropuesto = nombrePropuesto,
        requiereAprobacion = requiereAprobacion
    )
}

internal fun InvitacionRepository.InvitacionDto.toDomain() =
    toDomainOrNull() ?: InvitacionTrabajador(
        id = codigo,
        negocioId = negocioId,
        codigo = codigo,
        estado = EstadoInvitacion.EXPIRADA,
        fechaCreacion = fechaCreacion,
        expiracion = expiracion,
        nombrePropuesto = nombrePropuesto,
        requiereAprobacion = requiereAprobacion
    )
