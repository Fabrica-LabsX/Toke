package com.flabs.cobrate.core.logic

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

/**
 * Utilidad para seleccionar imágenes desde la galería.
 * Convierte la imagen a ByteArray listo para upload.
 * Valida tipo MIME y tamaño básico.
 */
object ImagePickerUtil {

    /**
     * Máximo tamaño de imagen en bytes: 5 MB.
     * Ajustable según requerimientos.
     */
    private const val MAX_SIZE_BYTES = 5 * 1024 * 1024 // 5 MB

    /**
     * MIME types aceptados.
     */
    private val ACCEPTED_MIME_TYPES = setOf("image/jpeg", "image/png", "image/webp")

    /**
     * Lee la imagen desde Uri, valida y convierte a ByteArray.
     * Se ejecuta en IO dispatcher para no bloquear UI.
     *
     * @param context contexto para resolver Uri
     * @param imageUri Uri de la imagen seleccionada
     * @return ByteArray con datos de imagen, o error
     */
    suspend fun processImage(context: Context, imageUri: Uri): Result<ByteArray> = withContext(Dispatchers.IO) {
        runCatching {
            val inputStream = context.contentResolver.openInputStream(imageUri)
                ?: throw IllegalStateException("No se pudo abrir la imagen")

            inputStream.use { stream ->
                // Leer todo el contenido
                val originalBytes = stream.readBytes()

                // Validar tamaño original
                if (originalBytes.size > MAX_SIZE_BYTES) {
                    // Si es demasiado grande, comprimir
                    val bitmap = BitmapFactory.decodeByteArray(originalBytes, 0, originalBytes.size)
                    compressBitmap(bitmap)
                } else {
                    originalBytes
                }
            }
        }
    }

    /**
     * Comprime un Bitmap a JPEG 85% de calidad.
     * Preserva legibilidad de QR.
     */
    private fun compressBitmap(bitmap: Bitmap): ByteArray {
        val output = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, output)
        return output.toByteArray()
    }

    /**
     * Valida si el MIME type es aceptable.
     * Llamar DESPUÉS de obtener el Uri desde Photo Picker.
     */
    fun isAcceptedMimeType(mimeType: String?): Boolean {
        return mimeType != null && ACCEPTED_MIME_TYPES.contains(mimeType.lowercase())
    }
}
