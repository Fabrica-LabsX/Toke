package com.flabs.cobrate.core.data

import com.flabs.cobrate.core.model.Negocio
import com.flabs.cobrate.core.model.ResultadoRpc
import com.flabs.cobrate.core.model.TipoNegocio
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.storage
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
object SafeTipoNegocioSerializer : KSerializer<TipoNegocio> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("TipoNegocio", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: TipoNegocio) = encoder.encodeString(value.name)
    override fun deserialize(decoder: Decoder): TipoNegocio {
        val raw = decoder.decodeString().uppercase()
        return runCatching { TipoNegocio.valueOf(raw) }.getOrDefault(TipoNegocio.OTRO)
    }
}

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

    /**
     * Limpia los archivos QR del bucket 'metodos-qr' y ejecuta la RPC eliminar_negocio (CAPA 39).
     */
    suspend fun eliminarNegocio(negocioId: String): Result<ResultadoRpc> = runCatching {
        val storage = SupabaseClientProvider.supabase.storage.from("metodos-qr")
        val archivos = runCatching { storage.list(prefix = "$negocioId/") }.getOrNull()
        if (!archivos.isNullOrEmpty()) {
            val rutas = archivos.map { "$negocioId/${it.name}" }
            storage.delete(rutas)
        }

        val respuesta = postgrest.rpc(
            function = "eliminar_negocio",
            parameters = buildJsonObject {
                put("p_negocio_id", negocioId)
            }
        ).decodeAs<ResultadoRpc>()

        if (!respuesta.exito) {
            throw Exception(respuesta.mensaje)
        }
        respuesta
    }

    /** Tope de trabajadores del negocio; null = sin límite (o si el servidor aún no tiene la columna). */
    suspend fun obtenerMaxTrabajadores(negocioId: String): Result<Int?> = runCatching {
        postgrest.from("negocios")
            .select(Columns.list("max_trabajadores")) { filter { eq("id", negocioId) } }
            .decodeSingleOrNull<MaxTrabajadoresDto>()?.maxTrabajadores
    }

    /** Edita el nombre y/o el tope de trabajadores (solo el dueño; lo valida el servidor). */
    suspend fun actualizarNegocio(negocioId: String, nombre: String?, max: Int?, limpiarMax: Boolean): Result<Unit> = runCatching {
        postgrest.rpc(
            function = "actualizar_negocio",
            parameters = buildJsonObject {
                put("p_negocio_id", negocioId)
                if (nombre != null) put("p_nombre", nombre)
                if (max != null) put("p_max_trabajadores", max)
                put("p_limpiar_max", limpiarMax)
            }
        )
        Unit
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

        // 2. Negocios donde existe una relación visible en relacion_cuenta_negocio.
        val relacionesActivas = postgrest.from("relacion_cuenta_negocio")
            .select {
                filter {
                    eq("cuenta_id", cuentaId)
                    isIn("estado", listOf("ACTIVA", "PAUSADA", "SOLICITADA", "RECHAZADA"))
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
        @Serializable(with = SafeTipoNegocioSerializer::class)
        @SerialName("tipo") val tipo: TipoNegocio,
        @SerialName("dueno_cuenta_id") val duenoCuentaId: String,
        @SerialName("color_marca") val colorMarca: String? = null
    )

    @Serializable
    internal data class MaxTrabajadoresDto(
        @SerialName("max_trabajadores") val maxTrabajadores: Int? = null
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
    duenoCuentaId = dueñoCuentaId,
    colorMarca = colorMarca
)

internal fun NegocioRepository.NegocioDto.toDomain() = Negocio(
    id = id,
    nombre = nombre,
    tipo = tipo,
    dueñoCuentaId = duenoCuentaId,
    colorMarca = colorMarca
)
