package com.flabs.cobrate.core.data

import android.content.Context
import java.io.File

/** Solo borra el archivo antiguo `eventos_notificacion` (ya no se escribe). */
internal class EventoDeNotificacionLocalStore(private val context: Context) {

    fun borrarTodo() {
        runCatching {
            val file = File(context.filesDir, "datastore/eventos_notificacion.preferences_pb")
            if (file.exists()) {
                file.delete()
            }
        }
    }
}
