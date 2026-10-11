package com.flabs.cobrate.core.logic

import java.util.Collections

/**
 * Registro en memoria para evitar notificaciones duplicadas entre Realtime y FCM Push (CAPA 53).
 * Mantiene hasta un máximo de 200 IDs (el más antiguo sale primero).
 */
object RegistroAvisos {
    private const val MAX_CAPACIDAD = 200
    private val avisados = Collections.synchronizedList(mutableListOf<String>())

    /** Devuelve true si la operación ya fue avisada anteriormente. Si no, la registra y devuelve false. */
    fun yaNotificadoOAgregar(id: String): Boolean {
        synchronized(avisados) {
            if (avisados.contains(id)) {
                return true
            }
            if (avisados.size >= MAX_CAPACIDAD) {
                avisados.removeAt(0)
            }
            avisados.add(id)
            return false
        }
    }

    /** Limpia el registro (para pruebas). */
    fun limpiar() {
        synchronized(avisados) {
            avisados.clear()
        }
    }
}
