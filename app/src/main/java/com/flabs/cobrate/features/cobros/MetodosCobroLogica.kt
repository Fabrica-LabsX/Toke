package com.flabs.cobrate.features.cobros

import com.flabs.cobrate.core.model.MetodoDeCobro
import com.flabs.cobrate.core.model.TipoMetodoCobro

/**
 * Lógica de la pantalla de métodos de cobro. Sin nada de interfaz: nombres legibles, cuál
 * método está seleccionado y qué texto mostrar para titular y número.
 */
object MetodosCobroLogica {

    const val SIN_CONFIGURAR = "Sin configurar"

    fun nombreLegible(tipo: TipoMetodoCobro): String = when (tipo) {
        TipoMetodoCobro.YAPE -> "Yape"
        TipoMetodoCobro.PLIN -> "Plin"
        TipoMetodoCobro.TRANSFERENCIA -> "Transferencia"
        TipoMetodoCobro.BCP -> "BCP"
        TipoMetodoCobro.INTERBANK -> "Interbank"
        TipoMetodoCobro.BIM -> "BIM"
        TipoMetodoCobro.AGORA_TUNKI -> "Agora/Tunki"
        TipoMetodoCobro.BBVA -> "BBVA"
        TipoMetodoCobro.BANBIF -> "BanBif"
        TipoMetodoCobro.LIGO -> "Ligo"
        TipoMetodoCobro.SCOTIABANK -> "Scotiabank"
        TipoMetodoCobro.PICHINCHA -> "Pichincha"
        TipoMetodoCobro.SIP -> "Sip"
        TipoMetodoCobro.LEMON_CASH -> "Lemon Cash"
    }

    /** El método elegido, o el primero si no hay elección o ya no existe. */
    fun seleccionado(metodos: List<MetodoDeCobro>, idElegido: String?): MetodoDeCobro? =
        metodos.find { it.id == idElegido } ?: metodos.firstOrNull()

    fun titular(metodo: MetodoDeCobro): String =
        metodo.nombreDestinatario?.trim()?.takeIf { it.isNotBlank() } ?: SIN_CONFIGURAR

    fun numero(metodo: MetodoDeCobro): String =
        metodo.datoDeCobro.trim().ifBlank { SIN_CONFIGURAR }

    /**
     * La marca que va junto al nombre ("Interbank | Plin"). Es solo visual: se deduce de la billetera,
     * sin guardar nada ni cambiar cómo se detectan los pagos de esa billetera.
     */
    fun marcaPlin(metodo: MetodoDeCobro): String? =
        if (NuevoMetodoLogica.esDeLaRedPlin(metodo.tipo)) "Plin" else null

    /** Solo se puede copiar un número que sí está configurado. */
    fun numeroCopiable(metodo: MetodoDeCobro): String? =
        metodo.datoDeCobro.trim().takeIf { it.isNotBlank() }

    /** Extensión según el tipo de imagen, mirando los primeros bytes. */
    fun extensionDeImagen(bytes: ByteArray): String = when {
        bytes.size >= 4 && bytes[0] == 0x89.toByte() && bytes[1] == 'P'.code.toByte() -> "png"
        bytes.size >= 12 && bytes[8] == 'W'.code.toByte() && bytes[9] == 'E'.code.toByte() -> "webp"
        else -> "jpg"
    }
}
