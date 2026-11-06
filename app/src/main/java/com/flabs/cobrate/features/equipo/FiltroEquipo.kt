package com.flabs.cobrate.features.equipo

import com.flabs.cobrate.core.model.EstadoRelacion
import com.flabs.cobrate.core.model.InvitacionTrabajador
import com.flabs.cobrate.features.business.SolicitudUi
import com.flabs.cobrate.features.business.TrabajadorRealInfo

/**
 * Lógica de la pantalla de Equipo. Sin nada de interfaz: decide qué lista se muestra,
 * qué dice el buscador y a qué pestaña se vuelve.
 */

enum class PestanaEquipo { MI_EQUIPO, CODIGOS, SOLICITUDES }

object FiltroEquipo {

    fun placeholder(pestana: PestanaEquipo): String = when (pestana) {
        PestanaEquipo.MI_EQUIPO -> "Buscar colaborador..."
        PestanaEquipo.CODIGOS -> "Buscar código..."
        PestanaEquipo.SOLICITUDES -> "Buscar solicitud..."
    }

    fun trabajadores(lista: List<TrabajadorRealInfo>, texto: String): List<TrabajadorRealInfo> =
        if (texto.isBlank()) lista else lista.filter { it.username.contains(texto, ignoreCase = true) }

    fun codigos(lista: List<InvitacionTrabajador>, texto: String): List<InvitacionTrabajador> =
        if (texto.isBlank()) lista else lista.filter { it.codigo.contains(texto, ignoreCase = true) }

    fun solicitudes(lista: List<SolicitudUi>, texto: String): List<SolicitudUi> =
        if (texto.isBlank()) {
            lista
        } else {
            lista.filter {
                it.nombre.contains(texto, ignoreCase = true) ||
                    it.codigoUsado?.contains(texto, ignoreCase = true) == true
            }
        }

    /** Si ya no quedan solicitudes y se estaba en esa pestaña, se vuelve a Mi Equipo. */
    fun pestanaTrasCambioDeSolicitudes(actual: PestanaEquipo, cantidadSolicitudes: Int): PestanaEquipo =
        if (cantidadSolicitudes == 0 && actual == PestanaEquipo.SOLICITUDES) PestanaEquipo.MI_EQUIPO else actual

    /** Pausar a un trabajador activo; cualquier otro estado lo reactiva. */
    fun estadoAlPausarOReanudar(estado: EstadoRelacion): EstadoRelacion =
        if (estado == EstadoRelacion.ACTIVA) EstadoRelacion.PAUSADA else EstadoRelacion.ACTIVA
}
