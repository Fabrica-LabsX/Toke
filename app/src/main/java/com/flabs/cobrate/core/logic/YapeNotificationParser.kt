package com.flabs.cobrate.core.logic

import java.math.BigDecimal

/**
 * Parser determinista y específico para notificaciones de Yape (com.bcp.innovacxion.yapeapp).
 *
 * Función pura: sin Context, sin Supabase, sin Firebase, sin almacenamiento ni llamadas de red.
 */
object YapeNotificationParser {

    const val PAQUETE_YAPE = "com.bcp.innovacxion.yapeapp"

    /**
     * Resultado detallado del parseo específico de Yape.
     *
     * [monto] Importe validado y convertido a Double.
     * [pagador] Nombre del pagador original (conserva tildes, mayúsculas y caracteres especiales).
     */
    data class ResultadoYape(
        val monto: Double,
        val pagador: String
    )

    // Patrón positivo principal:
    // Prefijo opcional: "Yape!" o "¡Yape!"
    // Pagador: letras Unicode, espacios, asteriscos (censura), puntos, guiones, apóstrofos y ampersand (&).
    // Delimitador: "te envió un pago por S/" (o "te envio un pago por S/.")
    // Monto crudo: token completo posterior a S/
    // Sufijo de seguridad opcional tolerado sin capturar: ". El cód. de seguridad es: XXX"
    private val regexEstructuraPositiva = Regex(
        """^(?:[¡!]?\s*yape[!:]?\s+)?([\p{L}\s.*'’\-&]+?)\s+te\s+envi[oó]\s+un\s+pago\s+por\s+s/\.?\s*(\S+?)(?:\.?\s+el\s+c[oó]d(?:\.|igo)?\s+de\s+seguridad\s+es:\s*[0-9a-zA-Z]+)?\.?\s*$""",
        setOf(RegexOption.IGNORE_CASE)
    )

    // Caracteres permitidos para el nombre del pagador
    private val regexCaracteresNombre = Regex("""^[\p{L}\s.*'’\-&]+$""")

    // Formatos de monto válidos:
    // 1. Entero o decimal con punto: ej. "10", "2.5", "10.00"
    private val regexEnteroODecimal = Regex("""^\d+(\.\d+)?$""")
    // 2. Agrupación de miles con coma y decimal con punto: ej. "1,000", "1,250.50"
    private val regexMiles = Regex("""^\d{1,3}(,\d{3})+(\.\d+)?$""")

    // Frases de veto explícito para evitar falsos positivos
    private val vetosExplicitos = listOf(
        "codigo de verificacion",
        "código de verificación",
        "clave de verificacion",
        "clave de verificación",
        "clave de acceso",
        "inicio de sesion",
        "inicio de sesión",
        "actualiza tu app",
        "token",
        "pagaste",
        "enviaste",
        "yapeaste",
        "transferiste",
        "realizaste una transferencia",
        "saldo disponible",
        "tu saldo es",
        "participa en el sorteo",
        "sorteo y gana",
        "solicita tu prestamo",
        "solicita tu préstamo",
        "credito personal",
        "crédito personal",
        "prestamo preaprobado",
        "préstamo preaprobado"
    )

    /**
     * Punto de entrada compatible con [ResultadoParseo] del coordinador [NotificationParser].
     */
    fun parsear(
        titulo: String,
        texto: String,
        paqueteOrigen: String,
        datosAdicionales: String? = null
    ): ResultadoParseo {
        if (paqueteOrigen != PAQUETE_YAPE) return ResultadoParseo.NoEsPago

        val resultado = parsearYape(titulo, texto, datosAdicionales) ?: return ResultadoParseo.NoEsPago
        return ResultadoParseo.MontoDetectado(
            monto = resultado.monto,
            confianza = NivelConfianzaParseo.ALTA,
            pagador = resultado.pagador
        )
    }

    /**
     * Parsea la notificación buscando el formato exacto de Yape.
     * Retorna [ResultadoYape] si cumple todas las reglas, o null si no es un pago válido.
     */
    fun parsearYape(
        titulo: String,
        texto: String,
        datosAdicionales: String? = null
    ): ResultadoYape? {
        // Veto previo sobre el título si contiene mensajes de autenticación, egresos o publicidad
        val tituloLimpio = titulo.trim().lowercase()
        if (vetosExplicitos.any { tituloLimpio.contains(it) }) {
            return null
        }

        // Obtener posibles candidatos de cuerpo (texto y extras enriquecidos)
        val candidatos = mutableListOf<String>()

        if (texto.isNotBlank()) {
            candidatos.add(texto.trim())
        }

        if (!datosAdicionales.isNullOrBlank()) {
            // Evaluar primero líneas individuales de EXTRA_BIG_TEXT o EXTRA_TEXT_LINES
            val lineas = datosAdicionales.lines().map { it.trim() }.filter { it.isNotEmpty() }
            candidatos.addAll(lineas)
            // Evaluar también el bloque completo unificado por si hubo quiebres de línea en el cuerpo
            val bloqueUnificado = datosAdicionales.replace(Regex("""[\r\n]+"""), " ").trim()
            if (bloqueUnificado.isNotEmpty()) {
                candidatos.add(bloqueUnificado)
            }
        }

        // Si texto y extras estaban vacíos pero el título contenía el mensaje
        if (candidatos.isEmpty() && titulo.isNotBlank()) {
            candidatos.add(titulo.trim())
        }

        val evaluados = candidatos
            .map { it.replace(Regex("""[\r\n]+"""), " ").trim() }
            .filter { it.isNotEmpty() }
            .distinct()

        for (candidato in evaluados) {
            val resultado = evaluarCandidato(candidato)
            if (resultado != null) {
                return resultado
            }
        }

        return null
    }

    private fun evaluarCandidato(cuerpo: String): ResultadoYape? {
        val cuerpoMinusc = cuerpo.lowercase()
        if (vetosExplicitos.any { cuerpoMinusc.contains(it) }) {
            return null
        }

        val match = regexEstructuraPositiva.matchEntire(cuerpo) ?: return null

        val rawNombre = match.groupValues.getOrNull(1) ?: return null
        val rawMonto = match.groupValues.getOrNull(2) ?: return null

        val nombre = validarNombre(rawNombre) ?: return null
        val monto = validarMonto(rawMonto) ?: return null

        return ResultadoYape(
            monto = monto,
            pagador = nombre
        )
    }

    /**
     * Valida el nombre del pagador según las reglas de Yape:
     * - Letras Unicode, tildes, espacios, puntos, guiones, apóstrofos y &.
     * - Al menos 2 caracteres de longitud y al menos 1 letra.
     * - Conserva mayúsculas, minúsculas y tildes originales.
     * - No aplica límite arbitrario de 38 caracteres.
     */
    fun validarNombre(raw: String): String? {
        val n = raw.trim()
        if (n.length < 2) return null
        if (!regexCaracteresNombre.matches(n)) return null
        if (!n.any { it.isLetter() }) return null
        return n
    }

    /**
     * Valida el monto numérico:
     * - Acepta enteros y decimales con punto (10, 2.5, 10.00).
     * - Valida agrupación de miles si se usan comas (ej. 1,000 o 1,250.50).
     * - Nunca interpreta una coma como separador decimal.
     * - No acepta importes malformados, cero ni negativos.
     * - Usa BigDecimal para validación rigurosa.
     */
    fun validarMonto(raw: String): Double? {
        val s = raw.trim().removeSuffix(".")
        if (s.isEmpty()) return null

        if (!regexEnteroODecimal.matches(s) && !regexMiles.matches(s)) {
            return null
        }

        val sinComas = s.replace(",", "")
        val bd = try {
            BigDecimal(sinComas)
        } catch (_: Exception) {
            return null
        }

        if (bd <= BigDecimal.ZERO) {
            return null
        }

        return bd.toDouble()
    }
}
