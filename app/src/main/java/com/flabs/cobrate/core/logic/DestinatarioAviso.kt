package com.flabs.cobrate.core.logic

/**
 * Quién recibe el aviso de "pago confirmado" (notificación y voz): el trabajador o colaborador.
 * El dueño no: él solo recibe los avisos de "esperando red" y "cobro no enviado".
 */
object DestinatarioAviso {
    fun avisaPagoConfirmado(esDuenoDelNegocio: Boolean): Boolean = !esDuenoDelNegocio
}
