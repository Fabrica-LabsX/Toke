package com.flabs.cobrate.features.reportes

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.flabs.cobrate.core.logic.FeedbackManager
import com.flabs.cobrate.core.model.Operacion
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Genera el Excel de un periodo y lo comparte con la app que elija la persona. */
object ReportesAcciones {
    private const val TIPO_XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"

    suspend fun exportarExcel(
        context: Context,
        operaciones: List<Operacion>,
        nombresTrabajadores: Map<String, String>,
        periodo: PeriodoReporte,
    ) {
        val ahora = System.currentTimeMillis()
        val delPeriodo = ReportesLogica.filtrar(operaciones, periodo, ahora)
        if (delPeriodo.isEmpty()) {
            FeedbackManager.showErrorMessage("No hay cobros en este periodo para exportar.")
            return
        }
        runCatching {
            val archivo = withContext(Dispatchers.IO) {
                val carpeta = File(context.cacheDir, "reportes").apply { mkdirs() }
                File(carpeta, ReportesLogica.nombreArchivo(periodo, ahora)).also { f ->
                    f.outputStream().use { XlsxWriter.escribir(ReportesLogica.filasExportacion(delPeriodo, nombresTrabajadores), it) }
                }
            }
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
            val enviar = Intent(Intent.ACTION_SEND).apply {
                type = TIPO_XLSX
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(enviar, "Exportar cobros").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }.onFailure {
            FeedbackManager.showErrorMessage("No se pudo crear el Excel. Inténtalo de nuevo.")
        }
    }
}
