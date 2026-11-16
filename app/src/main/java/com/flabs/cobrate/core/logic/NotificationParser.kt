package com.flabs.cobrate.core.logic


/**
 * Resultado del parsing de una notificación.
 */
sealed class ResultadoParseo {
    /** Se detectó un monto en soles, con un nivel de confianza asociado. */
    data class MontoDetectado(
        val monto: Double,
        val confianza: NivelConfianzaParseo,
        val pagador: String? = null
    ) : ResultadoParseo()

    /** No se detectó un patrón de monto: no parece un pago. */
    object NoEsPago : ResultadoParseo()
}

/** Nivel de confianza de la detección de monto. */
enum class NivelConfianzaParseo { ALTA, MEDIA }

/**
 * Parser/normalizador de eventos de notificación de pago (CAPA 6c & CAPA 9).
 *
 * Función pura: recibe el título, el texto y el paquete de origen de una
 * notificación y devuelve si contiene un monto en soles (y con qué
 * confianza). No tiene acceso a Context ni a ningún Store; no genera
 * operaciones ni muta estado.
 *
 * Sin librerías de terceros: solo Regex de Kotlin y java.text.Normalizer.
 */
object NotificationParser {

    /**
     * Parsea [titulo]/[texto]/[datosAdicionales] de un evento y devuelve:
     *  - [ResultadoParseo.MontoDetectado] con confianza ALTA (PAGO REAL) si hay
     *    un monto válido, un patrón de pago entrante y no activa ningún veto.
     *  - [ResultadoParseo.NoEsPago] si la notificación es promocional (PROMO / NO PAGO)
     *    o si no cumple los criterios transaccionales entrantes (NI LO UNO NI LO OTRO).
     *
     * Este archivo solo ordena los pasos. Cada decisión vive en su propio archivo:
     *  - [NormalizadorDeTexto]: prepara el texto.
     *  - [FiltroPromocional], [FiltroSeguridad], [FiltroEgresos]: los tres vetos.
     *  - [ExtractorDeMonto]: lee el monto en soles.
     *  - [PatronesPagoEntrante]: confirma que el dinero fue recibido.
     */
    fun parsear(
        titulo: String,
        texto: String,
        paqueteOrigen: String,
        datosAdicionales: String? = null
    ): ResultadoParseo {
        // Enrutamiento específico para Yape (CAPA 6c)
        if (paqueteOrigen == YapeNotificationParser.PAQUETE_YAPE) {
            return YapeNotificationParser.parsear(titulo, texto, paqueteOrigen, datosAdicionales)
        }

        // Comportamiento heredado para cualquier otro paquete:
        // Paso 0 — Normalización
        val contenido = NormalizadorDeTexto.unificar(titulo, texto, datosAdicionales)

        // Paso 1 — VETO PROMOCIONAL
        if (FiltroPromocional.detecta(contenido)) return ResultadoParseo.NoEsPago

        // Paso 2 — VETO SEGURIDAD / NO TRANSACCIONAL
        if (FiltroSeguridad.detecta(contenido)) return ResultadoParseo.NoEsPago

        // Paso 3 — VETO SALIENTE
        if (FiltroEgresos.detecta(contenido)) return ResultadoParseo.NoEsPago

        // Paso 4 — Monto
        val monto = ExtractorDeMonto.extraer(contenido) ?: return ResultadoParseo.NoEsPago

        // Paso 5 — PATRÓN DE PAGO ENTRANTE
        if (!PatronesPagoEntrante.coincide(contenido)) return ResultadoParseo.NoEsPago

        // Paso 6 — Veredicto
        return ResultadoParseo.MontoDetectado(monto, NivelConfianzaParseo.ALTA)
    }
}
