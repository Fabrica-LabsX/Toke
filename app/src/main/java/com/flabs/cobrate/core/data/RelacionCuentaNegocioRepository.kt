package com.flabs.cobrate.core.data

import android.util.Log
import com.flabs.cobrate.core.logic.LogSeguro
import com.flabs.cobrate.core.model.EstadoRelacion
import com.flabs.cobrate.core.model.MiembroEquipo
import com.flabs.cobrate.core.model.RelacionCuentaNegocio
import com.flabs.cobrate.core.model.ResultadoRpc
import com.flabs.cobrate.core.model.RolCuentaNegocio
import io.github.jan.supabase.postgrest.postgrest
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
object SafeRolSerializer : KSerializer<RolCuentaNegocio> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("RolCuentaNegocio", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: RolCuentaNegocio) = encoder.encodeString(value.name)
    override fun deserialize(decoder: Decoder): RolCuentaNegocio {
        val raw = decoder.decodeString().uppercase()
        return runCatching { RolCuentaNegocio.valueOf(raw) }.getOrDefault(RolCuentaNegocio.TRABAJADOR)
    }
}

@OptIn(ExperimentalSerializationApi::class)
object SafeEstadoRelacionSerializer : KSerializer<EstadoRelacion?> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("EstadoRelacion", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: EstadoRelacion?) = encoder.encodeString(value?.name ?: "")
    override fun deserialize(decoder: Decoder): EstadoRelacion? {
        val raw = decoder.decodeString().uppercase()
        return runCatching { EstadoRelacion.valueOf(raw) }.getOrElse {
            LogSeguro.w("RelacionDto", "EstadoRelacion no reconocido: $raw")
            null
        }
    }
}

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
        createdDto.toDomainOrNull() ?: throw Exception("Estado inválido en relación creada")
    }

    suspend fun relacionesDe(cuentaId: String): Result<List<RelacionCuentaNegocio>> = runCatching {
        postgrest.from("relacion_cuenta_negocio")
            .select {
                filter {
                    eq("cuenta_id", cuentaId)
                }
            }
            .decodeList<RelacionDto>()
            .mapNotNull { it.toDomainOrNull() }
    }

    suspend fun relacionesDelNegocio(negocioId: String): Result<List<RelacionCuentaNegocio>> = runCatching {
        postgrest.from("relacion_cuenta_negocio")
            .select {
                filter {
                    eq("negocio_id", negocioId)
                }
            }
            .decodeList<RelacionDto>()
            .mapNotNull { it.toDomainOrNull() }
    }

    /**
     * Consulta RPC equipo_de_negocio para obtener los miembros activos del negocio.
     * CAPA 22.
     */
    suspend fun equipoDeNegocio(negocioId: String): Result<List<MiembroEquipo>> = runCatching {
        postgrest.rpc(
            function = "equipo_de_negocio",
            parameters = buildJsonObject { put("p_negocio_id", negocioId) }
        ).decodeList<MiembroEquipoDto>().mapNotNull { it.aMiembroOrNull() }
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

    /**
     * Llama a la RPC salir_del_negocio para desvincular al trabajador del negocio (CAPA 39).
     */
    suspend fun salirDelNegocio(negocioId: String): Result<ResultadoRpc> = runCatching {
        val respuesta = postgrest.rpc(
            function = "salir_del_negocio",
            parameters = buildJsonObject {
                put("p_negocio_id", negocioId)
            }
        ).decodeAs<ResultadoRpc>()

        if (!respuesta.exito) {
            throw Exception(respuesta.mensaje)
        }
        respuesta
    }

    /**
     * Responde a una solicitud de ingreso (aceptar o rechazar) vía RPC (CAPA 44).
     */
    suspend fun responderSolicitud(
        negocioId: String,
        cuentaId: String,
        aceptar: Boolean
    ): Result<ResultadoRpc> = runCatching {
        val respuesta = postgrest.rpc(
            function = "responder_solicitud",
            parameters = buildJsonObject {
                put("p_negocio_id", negocioId)
                put("p_cuenta_id", cuentaId)
                put("p_aceptar", aceptar)
            }
        ).decodeAs<ResultadoRpc>()

        if (!respuesta.exito) {
            throw Exception(respuesta.mensaje)
        }
        respuesta
    }

    @Serializable
    internal data class RelacionDto(
        @SerialName("cuenta_id") val cuentaId: String,
        @SerialName("negocio_id") val negocioId: String,
        @Serializable(with = SafeRolSerializer::class)
        @SerialName("rol") val rol: RolCuentaNegocio,
        @Serializable(with = SafeEstadoRelacionSerializer::class)
        @SerialName("estado") val estado: EstadoRelacion? = null,
        @SerialName("fecha_vinculacion") val fechaVinculacion: Long? = null, // C10: BIGINT en Supabase
        @SerialName("codigo_usado") val codigoUsado: String? = null
    )

    @Serializable
    private data class MiembroEquipoDto(
        @SerialName("cuenta_id") val cuentaId: String,
        @SerialName("username") val username: String? = null,
        @SerialName("rol") val rolRaw: String? = null,
        @SerialName("estado") val estadoRaw: String? = null
    ) {
        fun aMiembroOrNull(): MiembroEquipo? {
            val rolEnum = runCatching { RolCuentaNegocio.valueOf((rolRaw ?: "").uppercase()) }.getOrElse {
                Log.w("TokeEquipo", "Rol no reconocido: $rolRaw para $cuentaId")
                return null
            }
            val estadoEnum = runCatching { EstadoRelacion.valueOf((estadoRaw ?: "").uppercase()) }.getOrElse {
                Log.w("TokeEquipo", "Estado no reconocido: $estadoRaw para $cuentaId")
                return null
            }
            return MiembroEquipo(
                cuentaId = cuentaId,
                username = username ?: "Usuario",
                rol = rolEnum,
                estado = estadoEnum
            )
        }
    }
}

internal fun RelacionCuentaNegocio.toDto() = RelacionCuentaNegocioRepository.RelacionDto(
    cuentaId = cuentaId,
    negocioId = negocioId,
    rol = rol,
    estado = estado,
    fechaVinculacion = null, // El servidor lo genera
    codigoUsado = codigoUsado
)

internal fun RelacionCuentaNegocioRepository.RelacionDto.toDomainOrNull(): RelacionCuentaNegocio? {
    val e = estado ?: return null
    return RelacionCuentaNegocio(
        cuentaId = cuentaId,
        negocioId = negocioId,
        rol = rol,
        estado = e,
        fechaVinculacion = fechaVinculacion ?: 0L,
        codigoUsado = codigoUsado
    )
}

internal fun RelacionCuentaNegocioRepository.RelacionDto.toDomain() =
    toDomainOrNull() ?: RelacionCuentaNegocio(
        cuentaId = cuentaId,
        negocioId = negocioId,
        rol = rol,
        estado = EstadoRelacion.EXPULSADA,
        fechaVinculacion = fechaVinculacion ?: 0L,
        codigoUsado = codigoUsado
    )
