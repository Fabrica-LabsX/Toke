package com.flabs.cobrate.features.operaciones

import com.flabs.cobrate.core.model.EstadoOperacion
import com.flabs.cobrate.core.model.Operacion
import com.flabs.cobrate.core.model.TipoMetodoCobro
import java.util.Locale

/**
 * Lógica de filtrado de la pantalla de Operaciones. Sin nada de interfaz:
 * recibe la lista y los filtros elegidos y devuelve la lista que se debe mostrar.
 */

enum class PestanaOperaciones { TODAS, PENDIENTES }

/** Rango de tiempo. [minutos] null significa "sin límite". */
enum class RangoTiempo(val etiqueta: String, val minutos: Long?) {
    HOY("Hoy", null),
    DIEZ_MIN("10 min", 10),
    TREINTA_MIN("30 min", 30),
    SESENTA_MIN("60 min", 60),
    UNA_HORA("1 h", 60),
    PERSONALIZADO("Todo", null),
}

/** Grupos de billetera que se pueden filtrar. [BANCOS] agrupa todo lo que no es Yape ni Plin. */
enum class FiltroBilletera(val etiqueta: String) {
    YAPE("Yape"),
    PLIN("Plin"),
    BANCOS("BCP / Bancos");

    fun coincide(metodo: TipoMetodoCobro?): Boolean = when (this) {
        YAPE -> metodo == TipoMetodoCobro.YAPE
        PLIN -> metodo == TipoMetodoCobro.PLIN
        BANCOS -> metodo != null && metodo != TipoMetodoCobro.YAPE && metodo != TipoMetodoCobro.PLIN
    }
}

data class FiltrosOperaciones(
    val pestana: PestanaOperaciones = PestanaOperaciones.TODAS,
    val texto: String = "",
    val rango: RangoTiempo = RangoTiempo.HOY,
    val billetera: FiltroBilletera? = null,
)

object FiltroOperaciones {

    /** Operaciones que cumplen todos los filtros, de la más reciente a la más antigua. */
    fun aplicar(
        operaciones: List<Operacion>,
        filtros: FiltrosOperaciones,
        ahora: Long,
    ): List<Operacion> {
        val limiteTiempo = if (filtros.rango == RangoTiempo.HOY) inicioDelDia(ahora)
        else filtros.rango.minutos?.let { ahora - it * 60 * 1000L } ?: 0L

        return operaciones.filter { op ->
            val cumplePestana = when (filtros.pestana) {
                PestanaOperaciones.PENDIENTES -> op.estado == EstadoOperacion.SIN_ASIGNAR
                PestanaOperaciones.TODAS -> true
            }
            val cumpleBusqueda = coincideBusqueda(op, filtros.texto)
            val cumpleTiempo = if (limiteTiempo > 0) op.fechaHora >= limiteTiempo else true
            val cumpleBilletera = filtros.billetera?.coincide(op.metodoPago) ?: true

            cumplePestana && cumpleBusqueda && cumpleTiempo && cumpleBilletera
        }.sortedByDescending { it.fechaHora }
    }

    /** Medianoche de hoy en la hora del celular, en milisegundos. */
    fun inicioDelDia(ahora: Long): Long = java.util.Calendar.getInstance().apply {
        timeInMillis = ahora
        set(java.util.Calendar.HOUR_OF_DAY, 0)
        set(java.util.Calendar.MINUTE, 0)
        set(java.util.Calendar.SECOND, 0)
        set(java.util.Calendar.MILLISECOND, 0)
    }.timeInMillis

    fun contarPendientes(operaciones: List<Operacion>): Int =
        operaciones.count { it.estado == EstadoOperacion.SIN_ASIGNAR }

    private fun coincideBusqueda(op: Operacion, texto: String): Boolean {
        if (texto.isBlank()) return true
        val consulta = texto.trim().lowercase()
        val montoTexto = "%.2f".format(Locale.US, op.monto)
        return montoTexto.contains(consulta) || op.id.lowercase().contains(consulta)
    }
}
