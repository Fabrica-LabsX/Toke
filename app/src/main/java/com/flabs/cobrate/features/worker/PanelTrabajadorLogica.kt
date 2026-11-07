package com.flabs.cobrate.features.worker

/** Textos del panel del trabajador. Solo lógica: sin pantallas. */
object PanelTrabajadorLogica {
    private const val MINUTO = 60_000L
    private const val HORA = 60 * MINUTO
    private const val DIA = 24 * HORA

    /** "Hace 5 min", "Hace 2 h", "Hace 3 d". Menos de un minuto: "Ahora". */
    fun textoHace(fechaHora: Long, ahora: Long): String {
        val dif = (ahora - fechaHora).coerceAtLeast(0)
        return when {
            dif < MINUTO -> "Ahora"
            dif < HORA -> "Hace ${dif / MINUTO} min"
            dif < DIA -> "Hace ${dif / HORA} h"
            else -> "Hace ${dif / DIA} d"
        }
    }

    /** Qué ficha va a la derecha: los pagos por reclamar si hay, y si no, Mis Negocios. */
    fun fichaDerecha(pendientes: Int): FichaDerecha =
        if (pendientes > 0) FichaDerecha.POR_RECLAMAR else FichaDerecha.MIS_NEGOCIOS

    enum class FichaDerecha { POR_RECLAMAR, MIS_NEGOCIOS }
}
