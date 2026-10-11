package com.flabs.cobrate.core.logic

import android.util.Log
import com.flabs.cobrate.core.data.SupabaseClientProvider
import com.flabs.cobrate.core.model.TipoMetodoCobro
import io.github.jan.supabase.storage.storage
import java.util.UUID

/**
 * Uploader de QR a Storage privado de Supabase.
 * Maneja validación, upload y generación de referencia de path.
 */
object QrImageUploader {

    private const val TAG = "QrImageUploader"
    private const val BUCKET_NAME = "metodos-qr"

    suspend fun uploadQr(
        negocioId: String,
        tipo: TipoMetodoCobro,
        imageData: ByteArray
    ): Result<String> = runCatching {
        // Validaciones básicas
        if (negocioId.isBlank()) throw IllegalArgumentException("negocioId no puede estar vacío")
        if (imageData.isEmpty()) throw IllegalArgumentException("imageData no puede estar vacío")

        // Generar nombre único: {tipo}-{uuid}.png
        val uuid = UUID.randomUUID().toString()
        val fileName = "${tipo.name.lowercase()}-${uuid}.png"
        
        // Path final: negocioId/{tipo}-{uuid}.png
        val fullPath = "$negocioId/$fileName"

        Log.d(TAG, "Iniciando upload a: $fullPath (tamaño confirmado: ${imageData.size} bytes)")

        // Upload a Storage
        val storage = SupabaseClientProvider.supabase.storage.from(BUCKET_NAME)
        storage.upload(fullPath, imageData)

        Log.d(TAG, "Upload exitoso. Path: $fullPath")

        // Retornar el path persistente
        fullPath
    }

    /**
     * Intenta eliminar el QR anterior (best-effort).
     * Si falla, solo registra el error y continúa.
     * NO lanza excepción.
     *
     * @param negocioId ID del negocio
     * @param oldPath path anterior a eliminar (ej: negocioId/yape-uuid.png)
     */
    suspend fun tryDeleteOldQr(negocioId: String, oldPath: String?) {
        if (oldPath.isNullOrBlank()) {
            Log.d(TAG, "No hay QR anterior para eliminar")
            return
        }

        try {
            val fileNameOnly = oldPath.substringAfterLast("/")
            LogSeguro.d(TAG, "Intentando eliminar QR anterior: $fileNameOnly")
            val storage = SupabaseClientProvider.supabase.storage.from(BUCKET_NAME)
            storage.delete(oldPath)
            LogSeguro.d(TAG, "QR anterior eliminado exitosamente")
        } catch (e: Exception) {
            val fileNameOnly = oldPath.substringAfterLast("/")
            LogSeguro.w(TAG, "Fallo al eliminar QR anterior ($fileNameOnly)", e)
        }
    }

    /**
     * Valida que un path de QR tenga el formato esperado.
     * Usado para verificar integridad antes de actualizar BD.
     */
    fun isValidQrPath(path: String?): Boolean {
        if (path.isNullOrBlank()) return false
        // Formato esperado: negocioId/tipo-uuid.png
        val parts = path.split("/")
        return parts.size == 2 && parts[1].contains("-") && parts[1].endsWith(".png")
    }
}
