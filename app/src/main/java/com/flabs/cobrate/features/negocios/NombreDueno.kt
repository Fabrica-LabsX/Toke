package com.flabs.cobrate.features.negocios

import com.flabs.cobrate.core.model.Cuenta

/**
 * Nombre con el que se muestra al dueño de un negocio. Si la cuenta aún no está cargada,
 * pide cargarla con [onCargarFaltante] y mientras tanto muestra un texto genérico.
 */
fun resolverNombreDueno(
    duenoCuentaId: String,
    currentUid: String?,
    cuentasCargadas: List<Cuenta>,
    onCargarFaltante: ((String) -> Unit)? = null,
): String {
    if (currentUid != null && duenoCuentaId == currentUid) return "Tú"

    val cuenta = cuentasCargadas.find { it.id == duenoCuentaId }
    if (cuenta == null) {
        onCargarFaltante?.invoke(duenoCuentaId)
        return "Dueño del negocio"
    }

    val usuario = cuenta.username.trim()
    val usuarioLimpio = if (usuario.contains("@")) usuario.substringBefore("@") else usuario
    return usuarioLimpio.ifBlank { "Dueño del negocio" }
}
