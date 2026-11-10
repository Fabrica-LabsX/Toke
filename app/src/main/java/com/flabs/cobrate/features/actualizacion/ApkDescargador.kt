package com.flabs.cobrate.features.actualizacion

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import kotlin.coroutines.coroutineContext

/** Por qué se rechazó o falló una descarga. */
enum class MotivoFallo { RED, TAMANO, HASH, PAQUETE_O_FIRMA, ALMACENAMIENTO }

class FalloDescarga(val motivo: MotivoFallo) : Exception(motivo.name)

/** Descarga el APK oficial por HTTPS, lo verifica y lo entrega al instalador de Android. */
class ApkDescargador(private val contexto: Context) {

    private val carpeta get() = File(contexto.cacheDir, "actualizaciones")

    /** Cancelable de verdad: al cancelar la corrutina se cierra la conexión y se borra el archivo parcial. */
    suspend fun descargar(m: ManifiestoVersion, alProgresar: (ProgresoDescarga) -> Unit): Result<File> = withContext(Dispatchers.IO) {
        carpeta.mkdirs()
        carpeta.listFiles()?.forEach { it.delete() }
        val destino = File(carpeta, "toke-${m.latestVersionCode}.apk")
        val parcial = File(carpeta, "toke-${m.latestVersionCode}.part")
        var con: HttpURLConnection? = null
        try {
            if (!m.apkUrl.startsWith("https://")) throw FalloDescarga(MotivoFallo.RED)
            con = (URL(m.apkUrl).openConnection() as HttpURLConnection).apply {
                connectTimeout = 15000
                readTimeout = 20000
            }
            if (con.responseCode != 200) throw FalloDescarga(MotivoFallo.RED)
            val inicio = System.currentTimeMillis()
            var total = 0L
            val sha = MessageDigest.getInstance("SHA-256")
            var ultimoAviso = 0L
            try {
                con.inputStream.use { entrada ->
                    parcial.outputStream().use { salida ->
                        val buf = ByteArray(32 * 1024)
                        while (true) {
                            coroutineContext.ensureActive()
                            val n = entrada.read(buf)
                            if (n < 0) break
                            salida.write(buf, 0, n)
                            sha.update(buf, 0, n)
                            total += n
                            val ahora = System.currentTimeMillis()
                            if (ahora - ultimoAviso > 200) {
                                ultimoAviso = ahora
                                alProgresar(ActualizacionLogica.progreso(total, m.apkSizeBytes, ahora - inicio))
                            }
                        }
                    }
                }
            } catch (e: IOException) {
                coroutineContext.ensureActive()
                throw FalloDescarga(if (e.message?.contains("ENOSPC") == true) MotivoFallo.ALMACENAMIENTO else MotivoFallo.RED)
            }
            alProgresar(ActualizacionLogica.progreso(total, m.apkSizeBytes, System.currentTimeMillis() - inicio))
            if (total != m.apkSizeBytes) throw FalloDescarga(MotivoFallo.TAMANO)
            val hash = sha.digest().joinToString("") { "%02x".format(it) }
            if (!hash.equals(m.sha256, ignoreCase = true)) throw FalloDescarga(MotivoFallo.HASH)
            if (!esApkDeEstaApp(parcial, m)) throw FalloDescarga(MotivoFallo.PAQUETE_O_FIRMA)
            if (!parcial.renameTo(destino)) throw FalloDescarga(MotivoFallo.ALMACENAMIENTO)
            Result.success(destino)
        } catch (e: CancellationException) {
            parcial.delete()
            throw e
        } catch (e: FalloDescarga) {
            parcial.delete()
            Result.failure(e)
        } catch (e: Exception) {
            parcial.delete()
            Result.failure(FalloDescarga(MotivoFallo.RED))
        } finally {
            con?.disconnect()
        }
    }

    @Suppress("DEPRECATION")
    private fun certificados(info: PackageInfo?): Set<String> {
        if (info == null) return emptySet()
        val firmas = if (Build.VERSION.SDK_INT >= 28) info.signingInfo?.apkContentsSigners else info.signatures
        return firmas.orEmpty()
            .map { MessageDigest.getInstance("SHA-256").digest(it.toByteArray()).joinToString("") { b -> "%02x".format(b) } }
            .toSet()
    }

    @Suppress("DEPRECATION")
    private fun esApkDeEstaApp(archivo: File, m: ManifiestoVersion): Boolean {
        val pm = contexto.packageManager
        val bandera = if (Build.VERSION.SDK_INT >= 28) PackageManager.GET_SIGNING_CERTIFICATES else PackageManager.GET_SIGNATURES
        val apk = pm.getPackageArchiveInfo(archivo.path, bandera)
        val instalada = runCatching { pm.getPackageInfo(contexto.packageName, bandera) }.getOrNull()
        return ActualizacionLogica.apkAceptable(
            paqueteApk = apk?.packageName,
            codigoApk = apk?.longVersionCode ?: -1L,
            certApk = certificados(apk),
            paquete = contexto.packageName,
            codigoEsperado = m.latestVersionCode,
            certInstalado = certificados(instalada),
        )
    }

    /** true si Android ya permite a TOKE instalar paquetes; si no, hay que mandar al usuario a activarlo. */
    fun puedeInstalar(): Boolean = Build.VERSION.SDK_INT < 26 || contexto.packageManager.canRequestPackageInstalls()

    /** Abre la pantalla de Android donde el usuario permite a TOKE instalar actualizaciones. */
    fun abrirPermisoDeInstalacion() {
        contexto.startActivity(
            Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${contexto.packageName}"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }

    /** Entrega el APK ya verificado al instalador oficial; Android pide la confirmación al usuario. */
    fun instalar(apk: File) {
        val uri = FileProvider.getUriForFile(contexto, "${contexto.packageName}.fileprovider", apk)
        contexto.startActivity(
            Intent(Intent.ACTION_VIEW).setDataAndType(uri, "application/vnd.android.package-archive")
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }
}
