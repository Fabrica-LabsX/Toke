package com.flabs.cobrate.features.cobros

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.flabs.cobrate.core.logic.FeedbackManager
import java.io.File

/**
 * Acciones con efecto de la pantalla de métodos de cobro: copiar el número y compartir la
 * imagen del QR. Se mantienen aquí para que la interfaz solo las dispare.
 */
object CobrosAcciones {

    fun copiarNumero(context: Context, numero: String) {
        val portapapeles = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
        portapapeles.setPrimaryClip(ClipData.newPlainText("Número asociado", numero))
        FeedbackManager.showSuccess("Número copiado")
    }

    /** Comparte la imagen del QR con la app que elija el usuario. */
    fun compartirQr(context: Context, imagen: ByteArray) {
        runCatching {
            val carpeta = File(context.cacheDir, "qr").apply { mkdirs() }
            val archivo = File(carpeta, "qr_cobro.${MetodosCobroLogica.extensionDeImagen(imagen)}")
            archivo.writeBytes(imagen)
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
            val enviar = Intent(Intent.ACTION_SEND).apply {
                type = "image/*"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(enviar, "Compartir QR con cliente"))
        }.onFailure {
            FeedbackManager.showErrorMessage("No se pudo compartir el QR")
        }
    }
}
