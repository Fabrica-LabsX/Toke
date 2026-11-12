package com.flabs.cobrate.core.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

/** Solo borra el archivo antiguo `eventos_notificacion` (ya no se escribe). */
internal class EventoDeNotificacionLocalStore(private val context: Context) {

    private val Context.dataStore by preferencesDataStore(name = "eventos_notificacion")

    suspend fun borrarTodo() {
        context.dataStore.edit { it.remove(stringPreferencesKey("lista_eventos_notificacion")) }
    }
}
