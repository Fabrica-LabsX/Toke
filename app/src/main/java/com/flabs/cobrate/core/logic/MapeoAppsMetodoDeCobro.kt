package com.flabs.cobrate.core.logic

import com.flabs.cobrate.core.model.TipoMetodoCobro

/**
 * Mapea cada paquete de origen confiable (ver [AppsDePagoConfiables]) al
 * [TipoMetodoCobro] correspondiente. CAPA 6c-2.
 */
object MapeoAppsMetodoDeCobro {

    /**
     * Paquete -> método de cobro.
     *
     * Interbank Negocios y Interbank Mobile Banking ambos mapean a
     * [TipoMetodoCobro.INTERBANK].
     *
     * TODO: Plin no tiene paquete propio confirmado; suele venir integrado en
     * apps bancarias (BCP/Interbank/BBVA). Si el usuario confirma que Plin
     * genera notificaciones desde uno de esos paquetes con texto distinguible,
     * agregar lógica de detección por contenido dentro de ese paquete, no un
     * paquete nuevo.
     */
    val mapeo: Map<String, TipoMetodoCobro> = mapOf(
        // Yape
        "com.bcp.innovacxion.yapeapp" to TipoMetodoCobro.YAPE,
        // BCP (banca móvil)
        "com.bcp.bank.bcp" to TipoMetodoCobro.BCP,
        // Interbank (banca móvil) e Interbank Negocios (ex-IziPayYa)
        "pe.com.interbank.mobilebanking" to TipoMetodoCobro.INTERBANK,
        "pe.com.interbank.mpay.customer" to TipoMetodoCobro.INTERBANK,
        // Agora/Tunki
        "pe.indigital.tunki.user" to TipoMetodoCobro.AGORA_TUNKI,
        // BBVA
        "com.bbva.nxt_peru" to TipoMetodoCobro.BBVA,
        // BIM
        "com.pdp.bim" to TipoMetodoCobro.BIM,
        // BanBif
        "pe.com.banbif.pnappmobile" to TipoMetodoCobro.BANBIF,
        // Ligo
        "pe.com.tarjetasperuanasprepago.tppapp" to TipoMetodoCobro.LIGO,
        // Scotiabank
        "pe.com.scotiabank.blpm.android.client" to TipoMetodoCobro.SCOTIABANK,
        // Pichincha
        "pe.pichincha.bm" to TipoMetodoCobro.PICHINCHA,
        // Sip (Tarjeta Oh!)
        "pe.solera.tarjetaoh" to TipoMetodoCobro.SIP,
        // LemonCash
        "com.applemoncash" to TipoMetodoCobro.LEMON_CASH
    )

    /** Devuelve el [TipoMetodoCobro] del paquete, o null si no está mapeado. */
    fun metodoDeCobroPara(paquete: String): TipoMetodoCobro? =
        mapeo[paquete]
}