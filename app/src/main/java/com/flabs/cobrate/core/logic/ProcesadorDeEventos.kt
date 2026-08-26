package com.flabs.cobrate.core.logic

import com.flabs.cobrate.core.model.EventoDeNotificacion

/**
 * Resultado del procesamiento de un evento: el evento evaluado y el
 * resultado del parsing aplicado (con qué se clasificó).
 */
data class ResultadoProcesamiento(
    val evento: EventoDeNotificacion,
    val parseo: ResultadoParseo
)

/**
 * Procesa eventos de notificación sin procesar (CAPA 6c).
 *
 * Función PURA: recibe la lista de eventos y devuelve para cada uno el
 * resultado de aplicar [NotificationParser.parsear]. No muta el Store ni
 * tiene acceso a Context — la marcación de "procesado" en el Store la hace
 * el llamador (el servicio) vía un método del Store.
 */
object ProcesadorDeEventos {

    /**
     * Aplica el parser a la lista de [eventos] y devuelve, para cada uno,
     * su [ResultadoProcesamiento].
     *
     * No hay creación de operaciones ni asignación a trabajadores en esta
     * capa; eso llega en capas posteriores.
     */
    fun procesar(eventos: List<EventoDeNotificacion>): List<ResultadoProcesamiento> =
        eventos.map { evento ->
            val parseo = NotificationParser.parsear(
                titulo = evento.titulo,
                texto = evento.texto,
                paqueteOrigen = evento.paqueteOrigen,
                datosAdicionales = evento.datosAdicionales
            )
            ResultadoProcesamiento(evento, parseo)
        }
}