package com.flabs.cobrate.core.data

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * PRI-6: TOKE ya no guarda el texto original de las notificaciones. Solo se conservan los campos
 * normalizados de la operación (monto, billetera, hora, pagador). Este objeto únicamente borra,
 * una vez por arranque, las copias que dejaron versiones anteriores en el celular.
 */
object EventoDeNotificacionStore {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun init(context: Context) {
        val appContext = context.applicationContext
        scope.launch { EventoDeNotificacionLocalStore(appContext).borrarTodo() }
    }
}
