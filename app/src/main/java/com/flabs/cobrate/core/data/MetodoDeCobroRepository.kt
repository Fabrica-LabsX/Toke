package com.flabs.cobrate.core.data

import com.flabs.cobrate.core.model.MetodoDeCobro
import com.flabs.cobrate.core.model.TipoMetodoCobro
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/**
 * Repositorio para tabla metodos_de_cobro en Supabase.
 * CRUD directo desde Android.
 */
internal class MetodoDeCobroRepository {

    private val postgrest = SupabaseClientProvider.supabase.postgrest

    suspend fun obtenerPorNegocio(negocioId: String): Result<List<MetodoDeCobro>> = runCatching {
        postgrest.from("metodos_de_cobro")
            .select {
                filter {
                    eq("negocio_id", negocioId)
                }
            }
            .decodeList<MetodoDeCobroDto>()
            .map { it.toDomain() }
    }

    suspend fun obtenerPorId(id: String): Result<MetodoDeCobro?> = runCatching {
        postgrest.from("metodos_de_cobro")
            .select {
                filter {
                    eq("id", id)
                }
            }
            .decodeSingleOrNull<MetodoDeCobroDto>()
            ?.toDomain()
    }

    suspend fun crear(negocioId: String, tipo: TipoMetodoCobro, datoDeCobro: String, nombreDestinatario: String): Result<MetodoDeCobro> = runCatching {
        val dtoInsert = MetodoDeCobroInsertDto(
            negocioId = negocioId,
            tipo = tipo.name,
            datoDeCobro = datoDeCobro,
            nombreDestinatario = nombreDestinatario,
            qrUrl = null,
            activo = true,
            oculto = false
        )
        postgrest.from("metodos_de_cobro")
            .insert(dtoInsert) {
                select()
            }
            .decodeSingle<MetodoDeCobroDto>()
            .toDomain()
    }

    suspend fun actualizar(id: String, datoDeCobro: String? = null, nombreDestinatario: String? = null, qrUrl: String? = null, activo: Boolean? = null, oculto: Boolean? = null): Result<MetodoDeCobro> = runCatching {
        val updated = postgrest.from("metodos_de_cobro")
            .update({
                if (datoDeCobro != null) set("dato_de_cobro", datoDeCobro)
                if (nombreDestinatario != null) set("nombre_destinado", nombreDestinatario)
                if (qrUrl != null) set("qr_url", qrUrl)
                if (activo != null) set("activo", activo)
                if (oculto != null) set("oculto", oculto)
            }) {
                filter {
                    eq("id", id)
                }
                select()
            }
            .decodeSingle<MetodoDeCobroDto>()
        updated.toDomain()
    }

    suspend fun eliminar(id: String): Result<Unit> = runCatching {
        postgrest.from("metodos_de_cobro")
            .delete {
                filter {
                    eq("id", id)
                }
            }
    }

    @Serializable
    internal data class MetodoDeCobroDto(
        @SerialName("id") val id: String?,
        @SerialName("negocio_id") val negocioId: String,
        @SerialName("tipo") val tipo: String,
        @SerialName("dato_de_cobro") val datoDeCobro: String?,
        @SerialName("nombre_destinado") val nombreDestinatario: String?,
        @SerialName("qr_url") val qrUrl: String?,
        @SerialName("activo") val activo: Boolean,
        @SerialName("oculto") val oculto: Boolean,
        @SerialName("creado_en") val creadoEn: String?
    )

    @Serializable
    internal data class MetodoDeCobroInsertDto(
        @SerialName("negocio_id") val negocioId: String,
        @SerialName("tipo") val tipo: String,
        @SerialName("dato_de_cobro") val datoDeCobro: String?,
        @SerialName("nombre_destinado") val nombreDestinatario: String?,
        @SerialName("qr_url") val qrUrl: String?,
        @SerialName("activo") val activo: Boolean,
        @SerialName("oculto") val oculto: Boolean
    )
}

internal fun MetodoDeCobroRepository.MetodoDeCobroDto.toDomain() = MetodoDeCobro(
    id = id ?: "",
    negocioId = negocioId,
    tipo = TipoMetodoCobro.valueOf(tipo),
    datoDeCobro = datoDeCobro ?: "",
    nombreDestinatario = nombreDestinatario,
    qrUrl = qrUrl,
    activo = activo,
    oculto = oculto,
    creadoEn = creadoEn?.let { iso ->
        runCatching {
            // ISO 8601 fallback for API 24
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            format.timeZone = TimeZone.getTimeZone("UTC")
            format.parse(iso)?.time ?: 0L
        }.getOrElse { 0L }
    } ?: 0
)
