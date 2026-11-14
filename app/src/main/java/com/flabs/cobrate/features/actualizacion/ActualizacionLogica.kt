package com.flabs.cobrate.features.actualizacion

import kotlinx.serialization.Serializable

/** Metadatos públicos de la versión estable (los entrega la Edge Function check-app-release). */
@Serializable
data class ManifiestoVersion(
    val applicationId: String,
    val channel: String = "stable",
    val latestVersionCode: Int,
    val latestVersionName: String,
    val minSupportedVersionCode: Int,
    val apkUrl: String,
    val apkSizeBytes: Long,
    val sha256: String,
    val releaseNotesUrl: String? = null,
    val publishedAt: String? = null,
)

sealed interface EstadoActualizacion {
    data object AlDia : EstadoActualizacion
    data class Opcional(val manifiesto: ManifiestoVersion) : EstadoActualizacion
    data class Obligatoria(val manifiesto: ManifiestoVersion) : EstadoActualizacion
}

data class ProgresoDescarga(val descargados: Long, val total: Long, val bytesPorSegundo: Long, val segundosRestantes: Long?) {
    val porcentaje: Int get() = if (total <= 0) 0 else ((descargados * 100) / total).toInt().coerceIn(0, 100)
}

/** Reglas puras del actualizador (sin Android) para poder probarlas. */
object ActualizacionLogica {
    /** Una respuesta guardada vale como aviso de "hay una nueva" durante 7 días; pasado eso se vuelve a consultar. */
    const val TOLERANCIA_OPCIONAL_MS = 7L * 24 * 60 * 60 * 1000

    private val SHA256 = Regex("^[0-9a-f]{64}$")

    /** Descarta respuestas que no son de este paquete, no usan HTTPS o son incoherentes. */
    fun manifiestoValido(m: ManifiestoVersion, paquete: String): Boolean =
        m.applicationId == paquete &&
            m.apkUrl.startsWith("https://") &&
            SHA256.matches(m.sha256.lowercase()) &&
            m.apkSizeBytes > 0 &&
            m.latestVersionCode > 0 &&
            m.minSupportedVersionCode in 1..m.latestVersionCode

    /**
     * Compara números de versión (nunca texto).
     * - Sin datos verificados: no se bloquea a nadie.
     * - Si la versión instalada deja de ser la más reciente (instalado < latestVersionCode):
     *   se bloquea directamente como Obligatoria hasta que el usuario actualice.
     */
    fun decidir(
        instalado: Int,
        m: ManifiestoVersion?,
        verificadoEn: Long,
        ahora: Long,
        tolerancia: Long = TOLERANCIA_OPCIONAL_MS,
    ): EstadoActualizacion = when {
        m == null -> EstadoActualizacion.AlDia
        instalado < m.latestVersionCode -> EstadoActualizacion.Obligatoria(m)
        else -> EstadoActualizacion.AlDia
    }

    fun progreso(descargados: Long, total: Long, transcurridoMs: Long): ProgresoDescarga {
        val vel = if (transcurridoMs <= 0) 0L else descargados * 1000 / transcurridoMs
        val resta = if (vel <= 0 || total <= descargados) null else (total - descargados) / vel
        return ProgresoDescarga(descargados, total, vel, resta)
    }

    /** El APK solo se instala si es de este paquete, es la versión anunciada y lleva el mismo certificado que la app instalada. */
    fun apkAceptable(
        paqueteApk: String?,
        codigoApk: Long,
        certApk: Set<String>,
        paquete: String,
        codigoEsperado: Int,
        certInstalado: Set<String>,
    ): Boolean = paqueteApk == paquete && codigoApk == codigoEsperado.toLong() && certApk.isNotEmpty() && certApk == certInstalado

    fun formatoMb(bytes: Long): String = String.format(java.util.Locale.US, "%.1f", bytes / 1_048_576.0)

    fun formatoVelocidad(bps: Long): String =
        if (bps >= 1_048_576) formatoMb(bps) + " MB/s" else (bps / 1024).toString() + " KB/s"

    fun formatoTiempo(seg: Long?): String = when {
        seg == null -> "—"
        seg < 60 -> "$seg s"
        else -> "${seg / 60} min ${seg % 60} s"
    }
}
