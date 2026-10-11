package com.flabs.cobrate.core.logic

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.min

/**
 * Utilidad aislada de procesamiento de imagen basada en Google ML Kit.
 * Detecta códigos QR, calcula el área con un margen técnico de seguridad y los recorta.
 */
object QrAutoCropper {

    private const val TAG = "QrAutoCropper"

    /**
     * Procesa los bytes de una imagen, detecta un código QR y lo auto-recorta de ser necesario.
     * Si no detecta ningún QR o ocurre un error, retorna los bytes originales como fallback seguro.
     */
    fun cropIfContainsQr(imageData: ByteArray): ByteArray {
        if (imageData.isEmpty()) return imageData

        try {
            val bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.size) ?: return imageData
            
            Log.d(TAG, "[FORENSE] Imagen original: W=${bitmap.width}, H=${bitmap.height}, bytes=${imageData.size}")
            
            // Configurar ML Kit específicamente para QR Codes
            val options = BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build()
            val scanner = BarcodeScanning.getClient(options)
            val image = InputImage.fromBitmap(bitmap, 0)

            // Ejecución síncrona/bloqueante controlada (se llama en hilo IO)
            val barcodeTask = scanner.process(image)
            val barcodes = Tasks.await(barcodeTask)

            val barcodeCount = barcodes?.size ?: 0
            Log.d(TAG, "[FORENSE] Resultado ML Kit: barcodeCount=$barcodeCount")

            if (barcodes.isNullOrEmpty()) {
                Log.d(TAG, "[FORENSE] cropApplied=false (No se detectó ningún QR Code en la imagen). Fallback a original.")
                return imageData
            }

            // Filtrar solo los QR Codes reales y ordenar por área descendente
            val qrBarcodes = barcodes.filter { it.format == Barcode.FORMAT_QR_CODE }
            Log.d(TAG, "[FORENSE] QRs filtrados: count=${qrBarcodes.size}")
            
            if (qrBarcodes.isEmpty()) {
                Log.d(TAG, "[FORENSE] cropApplied=false (No hay códigos de formato QR detectados). Fallback a original.")
                return imageData
            }

            // Priorizar el de mayor área si hay múltiples
            val bestQr = qrBarcodes.maxByOrNull { qr ->
                val bounds = qr.boundingBox
                if (bounds != null) bounds.width() * bounds.height() else 0
            } ?: qrBarcodes.first()

            val boundingBox = bestQr.boundingBox ?: return imageData
            Log.d(TAG, "[FORENSE] QR seleccionado: format=${bestQr.format}, boundingBox=$boundingBox, cornerPoints=${bestQr.cornerPoints?.joinToString { "(${it.x},${it.y})" }}")
            
            // Calcular regiones de recorte con margen proporcional (5-8%)
            val qrWidth = boundingBox.width()
            val qrHeight = boundingBox.height()
            
            // Margen proporcional seguro (7%)
            val marginX = (qrWidth * 0.07f).toInt()
            val marginY = (qrHeight * 0.07f).toInt()

            val left = max(0, boundingBox.left - marginX)
            val top = max(0, boundingBox.top - marginY)
            val right = min(bitmap.width, boundingBox.right + marginX)
            val bottom = min(bitmap.height, boundingBox.bottom + marginY)

            val widthToCrop = right - left
            val heightToCrop = bottom - top

            // Caso 2: Verificar si el QR ya ocupa prácticamente toda la imagen original (ej: > 90% del tamaño)
            if (widthToCrop >= bitmap.width * 0.92 && heightToCrop >= bitmap.height * 0.92) {
                Log.d(TAG, "[FORENSE] cropApplied=false (El QR ya ocupa la mayor parte de la imagen). No se requiere recorte excesivo.")
                return imageData
            }

            if (widthToCrop <= 0 || heightToCrop <= 0) {
                Log.d(TAG, "[FORENSE] cropApplied=false (Dimensiones calculadas inválidas: ${widthToCrop}x${heightToCrop})")
                return imageData
            }

            Log.d(TAG, "[FORENSE] Recortando QR: L=$left, T=$top, W=$widthToCrop, H=$heightToCrop (Original: ${bitmap.width}x${bitmap.height})")
            
            val croppedBitmap = Bitmap.createBitmap(bitmap, left, top, widthToCrop, heightToCrop)
            
            // Comprimir manteniendo calidad intacta usando PNG (no destructivo)
            val outputStream = ByteArrayOutputStream()
            croppedBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            
            val finalBytes = outputStream.toByteArray()
            Log.d(TAG, "[FORENSE] cropApplied=true, croppedWidth=${croppedBitmap.width}, croppedHeight=${croppedBitmap.height}, croppedByteSize=${finalBytes.size}")
            
            return finalBytes
        } catch (e: Exception) {
            Log.e(TAG, "[FORENSE] Error durante el auto-recorte inteligente del QR: ${e.message}. Fallback activo.", e)
            return imageData
        }
    }

    /**
     * Detecta un código QR dentro de un Bitmap y devuelve su boundingBox (Rect) si existe.
     * Usado para la asistencia del botón AIM en el editor sin modificar ni recortar físicamente nada.
     */
    fun detectQrBounds(bitmap: Bitmap): Rect? {
        return try {
            val options = BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build()
            val scanner = BarcodeScanning.getClient(options)
            val image = InputImage.fromBitmap(bitmap, 0)

            val barcodeTask = scanner.process(image)
            val barcodes = Tasks.await(barcodeTask)

            if (barcodes.isNullOrEmpty()) return null

            val qrBarcodes = barcodes.filter { it.format == Barcode.FORMAT_QR_CODE }
            if (qrBarcodes.isEmpty()) return null

            val bestQr = qrBarcodes.maxByOrNull { qr ->
                val bounds = qr.boundingBox
                if (bounds != null) bounds.width() * bounds.height() else 0
            } ?: qrBarcodes.first()

            bestQr.boundingBox
        } catch (e: Exception) {
            Log.e(TAG, "Error detectando bordes del QR para el botón AIM: ${e.message}")
            null
        }
    }
}
