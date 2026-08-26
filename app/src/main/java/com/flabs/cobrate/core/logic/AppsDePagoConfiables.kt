package com.flabs.cobrate.core.logic

/**
 * Única fuente de verdad de qué apps pueden originar un pago real (CAPA 6c-2).
 *
 * REGLA DE SEGURIDAD: [paqueteOrigen] no puede ser falsificado por otra app;
 * esta whitelist es la defensa real contra mensajes de pago falsos vía
 * SMS/WhatsApp/Telegram/shell, etc. Si una notificación NO proviene de uno de
 * estos paquetes, se descarta por completo, sin importar lo que diga el texto.
 *
 * Extender esto es trivial: basta con agregar el paquete a [paquetesConfiables]
 * (y, si aplica, su tipo en [MapeoAppsMetodoDeCobro]) sin tocar el resto de la
 * lógica.
 */
object AppsDePagoConfiables {

    /**
     * Paquetes reales confirmados por el usuario en el dispositivo.
     * 13 paquetes en total.
     */
    val paquetesConfiables: Set<String> = setOf(
        // Yape
        "com.bcp.innovacxion.yapeapp",
        // BCP (banca móvil)
        "com.bcp.bank.bcp",
        // Interbank (banca móvil)
        "pe.com.interbank.mobilebanking",
        // Interbank Negocios (ex-IziPayYa)
        "pe.com.interbank.mpay.customer",
        // Agora/Tunki
        "pe.indigital.tunki.user",
        // BBVA
        "com.bbva.nxt_peru",
        // BIM
        "com.pdp.bim",
        // BanBif
        "pe.com.banbif.pnappmobile",
        // Ligo
        "pe.com.tarjetasperuanasprepago.tppapp",
        // Scotiabank
        "pe.com.scotiabank.blpm.android.client",
        // Pichincha
        "pe.pichincha.bm",
        // Sip (Tarjeta Oh!)
        "pe.solera.tarjetaoh",
        // LemonCash
        "com.applemoncash"
    )

    /** ¿La app [paquete] está autorizada para originar un pago real? */
    fun esFuenteConfiables(paquete: String): Boolean =
        paquetesConfiables.contains(paquete)
}