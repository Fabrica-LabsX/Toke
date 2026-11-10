package com.flabs.cobrate.core.logic

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** Cómo se muestra la hora de un cobro en las listas: solo la hora si es de hoy; con el día si es de otro día. */
object FechaCobro {
    fun texto(
        fechaHora: Long,
        ahora: Long,
        zona: TimeZone = TimeZone.getDefault(),
        locale: Locale = Locale.forLanguageTag("es-PE"),
    ): String {
        val hora = SimpleDateFormat("hh:mm a", locale).apply { timeZone = zona }.format(Date(fechaHora))
        if (mismoDia(fechaHora, ahora, zona)) return hora
        val dia = SimpleDateFormat("d MMM", locale).apply { timeZone = zona }.format(Date(fechaHora)).replace(".", "")
        return "$dia · $hora"
    }

    private fun mismoDia(a: Long, b: Long, zona: TimeZone): Boolean {
        val ca = Calendar.getInstance(zona).apply { timeInMillis = a }
        val cb = Calendar.getInstance(zona).apply { timeInMillis = b }
        return ca.get(Calendar.YEAR) == cb.get(Calendar.YEAR) && ca.get(Calendar.DAY_OF_YEAR) == cb.get(Calendar.DAY_OF_YEAR)
    }
}
