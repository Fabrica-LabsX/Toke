package com.flabs.cobrate.features.acceso

/** Lo que responde el servidor al intentar abrir la sesión en este celular. */
sealed class RespuestaSesion {
    /** Este celular puede usar la cuenta. */
    data object Libre : RespuestaSesion()

    /** La cuenta ya está abierta en [dispositivoNombre] desde [iniciadaEn] (milisegundos). */
    data class Ocupada(val dispositivoNombre: String, val iniciadaEn: Long) : RespuestaSesion()

    /** No se pudo consultar (sin internet o error del servidor). */
    data object SinRespuesta : RespuestaSesion()
}

/** Qué hace la app con la respuesta. */
sealed class AccionSesion {
    data object Continuar : AccionSesion()
    data class Preguntar(val dispositivoNombre: String, val iniciadaEn: Long) : AccionSesion()
}

/** Reglas de la sesión única. Solo lógica: sin red ni pantallas. */
object SesionUnicaLogica {

    /** Sin respuesta nunca se bloquea: los pagos detectados no pueden interrumpirse por falta de internet. */
    fun decidir(respuesta: RespuestaSesion): AccionSesion = when (respuesta) {
        RespuestaSesion.Libre, RespuestaSesion.SinRespuesta -> AccionSesion.Continuar
        is RespuestaSesion.Ocupada -> AccionSesion.Preguntar(respuesta.dispositivoNombre, respuesta.iniciadaEn)
    }

    /** ¿La fila que llegó por Realtime significa que otro celular me sacó? */
    fun meSacaron(miDispositivoId: String, dispositivoActivoId: String?): Boolean =
        dispositivoActivoId != null && dispositivoActivoId != miDispositivoId

    /** Nombre legible del celular: marca y modelo sin repetir la marca. */
    fun nombreDeDispositivo(marca: String, modelo: String): String {
        val m = marca.trim().replaceFirstChar { it.uppercase() }
        val mod = modelo.trim()
        return when {
            mod.isEmpty() -> m.ifEmpty { "Otro celular" }
            m.isEmpty() || mod.startsWith(m, ignoreCase = true) -> mod
            else -> "$m $mod"
        }
    }

    /** Texto del aviso: "ayer", "hoy" o el día, más la hora. [ahora] y [iniciadaEn] en milisegundos. */
    fun cuando(iniciadaEn: Long, ahora: Long, zona: java.util.TimeZone = java.util.TimeZone.getDefault()): String {
        val cal = java.util.Calendar.getInstance(zona).apply { timeInMillis = iniciadaEn }
        val hoy = java.util.Calendar.getInstance(zona).apply { timeInMillis = ahora }
        val dias = (diaDelAnio(hoy) - diaDelAnio(cal)) + 365 * (hoy.get(java.util.Calendar.YEAR) - cal.get(java.util.Calendar.YEAR))
        val hora = String.format(java.util.Locale.US, "%d:%02d %s",
            if (cal.get(java.util.Calendar.HOUR) == 0) 12 else cal.get(java.util.Calendar.HOUR),
            cal.get(java.util.Calendar.MINUTE),
            if (cal.get(java.util.Calendar.AM_PM) == java.util.Calendar.AM) "a. m." else "p. m.")
        return when (dias) {
            0 -> "hoy, $hora"
            1 -> "ayer, $hora"
            else -> "hace $dias días, $hora"
        }
    }

    private fun diaDelAnio(c: java.util.Calendar) = c.get(java.util.Calendar.DAY_OF_YEAR)
}
