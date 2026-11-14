package com.flabs.cobrate.features.reportes

import android.content.Context
import android.content.SharedPreferences
import com.flabs.cobrate.features.planes.PlanLogica
import java.util.Calendar

/**
 * Cuota de exportaciones a Excel (CAPA Reportes).
 * El plan Free tiene un aforo de 3 exportaciones por semana; el plan PRO es ilimitado.
 */
object ExportQuotaStore {
    private const val PREFS = "export_quota_prefs"
    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        }
    }

    private fun semanaClave(): String {
        val cal = Calendar.getInstance()
        val anio = cal.get(Calendar.YEAR)
        val semana = cal.get(Calendar.WEEK_OF_YEAR)
        return "cuota_exp_${anio}_w${semana}"
    }

    fun restantes(esPro: Boolean): Int {
        if (esPro) return Int.MAX_VALUE
        val p = prefs ?: return PlanLogica.MAX_EXPORTES_SEMANALES_FREE
        val usados = p.getInt(semanaClave(), 0)
        return (PlanLogica.MAX_EXPORTES_SEMANALES_FREE - usados).coerceAtLeast(0)
    }

    fun puedeExportar(esPro: Boolean): Boolean {
        return esPro || restantes(false) > 0
    }

    fun registrarExportacion(esPro: Boolean) {
        if (esPro) return
        val p = prefs ?: return
        val clave = semanaClave()
        val usados = p.getInt(clave, 0)
        p.edit().putInt(clave, usados + 1).apply()
    }
}
