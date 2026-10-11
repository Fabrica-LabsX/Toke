package com.flabs.cobrate.services

import com.flabs.cobrate.core.logic.LogSeguro
import com.flabs.cobrate.core.logic.ProveedorDeToken
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Implementación de [ProveedorDeToken] basada en Firebase Messaging (CAPA 52).
 */
class FirebaseProveedorDeToken : ProveedorDeToken {

    override suspend fun obtenerToken(): String? = suspendCancellableCoroutine { continuation ->
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    continuation.resume(task.result)
                } else {
                    LogSeguro.w(TAG, "Error obteniendo token FCM", task.exception)
                    continuation.resume(null)
                }
            }
    }

    override suspend fun borrarToken(): Unit = suspendCancellableCoroutine { continuation ->
        FirebaseMessaging.getInstance().deleteToken()
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    LogSeguro.w(TAG, "Error borrando token FCM", task.exception)
                }
                continuation.resume(Unit)
            }
    }

    private companion object {
        const val TAG = "FirebaseTokenProv"
    }
}
