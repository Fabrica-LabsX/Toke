package com.flabs.cobrate

import com.flabs.cobrate.core.logic.AppsDePagoConfiables
import com.flabs.cobrate.core.logic.MapeoAppsMetodoDeCobro
import com.flabs.cobrate.core.logic.NotificationParser
import com.flabs.cobrate.core.logic.ResultadoParseo
import org.junit.Assert.assertEquals
import org.junit.Test

// TEXTOS REPRESENTATIVOS, no capturados de las apps: validar con pagos reales de cada billetera.
class MatrizBilleterasTest {
    private data class C(val pkg: String, val t: String, val x: String, val debe: Boolean)

    private val yape = "com.bcp.innovacxion.yapeapp"
    private val bcp = "com.bcp.bank.bcp"
    private val ibk = "pe.com.interbank.mobilebanking"
    private val ibkN = "pe.com.interbank.mpay.customer"
    private val tunki = "pe.indigital.tunki.user"
    private val bbva = "com.bbva.nxt_peru"
    private val bim = "com.pdp.bim"
    private val banbif = "pe.com.banbif.pnappmobile"
    private val ligo = "pe.com.tarjetasperuanasprepago.tppapp"
    private val scotia = "pe.com.scotiabank.blpm.android.client"
    private val pich = "pe.pichincha.bm"
    private val sip = "pe.solera.tarjetaoh"
    private val lemon = "com.applemoncash"

    private val casos = listOf(
        C(yape, "Confirmación de Pago", "Juan P* te envió un pago por S/ 5.00", true),
        C(yape, "Confirmación de Pago", "Ana R* te envió un pago por S/ 10. El cód. de seguridad es: 715", true),
        C(yape, "Confirmación de Pago", "Yape! CHARLY RUBEN LLONTOP te envió un pago por S/ 20.50", true),
        C(yape, "Confirmación de Pago", "Yape! SOLORZANO SALAS DIEGO ARMANDO te envió un pago por S/ 15.00", true),
        C(yape, "Confirmación de Pago", "Luis M* te envió un pago por S/ 1,250.50", true),
        C(yape, "Confirmación de Pago", "Carlos Ganoza* te envió un pago por S/ 8", true),
        C(yape, "Confirmación de Pago", "Bonifacio R* te envió un pago por S/ 8. El cód. de seguridad es: 001", true),
        C(bcp, "Plin", "Recibiste un Plin de Juan P. por S/ 20.00", true),
        C(bcp, "Plin", "Juan P. te envió S/ 20.00 por Plin", true),
        C(bcp, "BCP", "Te transfirieron S/ 100.00 de JUAN PEREZ", true),
        C(bcp, "BCP", "Recibiste una transferencia de S/ 100.00", true),
        C(bcp, "BCP", "Te depositaron S/ 100.00", true),
        C(bcp, "BCP", "Te abonaron S/ 45.00 en tu cuenta", true),
        C(ibk, "Interbank", "Recibiste S/ 30.00 de MARIA LOPEZ por Plin", true),
        C(ibk, "Plin", "Te enviaron S/ 30.00", true),
        C(ibk, "Interbank", "Te han transferido S/ 80.00", true),
        C(ibk, "Interbank", "Has recibido una transferencia por S/ 80.00", true),
        C(ibk, "Interbank", "Recibiste un abono de S/ 80.00 desde Banco de Crédito", true),
        C(ibkN, "Interbank Negocios", "Pago recibido: S/ 25.00", true),
        C(bbva, "BBVA", "Recibiste S/ 50.00 de JUAN PEREZ vía Plin", true),
        C(bbva, "BBVA", "Te hicieron una transferencia de S/ 50.00", true),
        C(bbva, "BBVA", "Se abonó S/ 50.00 en tu cuenta", true),
        C(bbva, "BBVA", "Se acreditó S/ 50.00", true),
        C(scotia, "Scotiabank", "Has recibido una transferencia de S/ 200.00", true),
        C(scotia, "Scotiabank", "Te depositaron S/ 200.00", true),
        C(bim, "BIM", "Recibiste S/ 12.00 de Juan", true),
        C(bim, "BIM", "Te enviaron S/ 12.00 por BIM", true),
        C(tunki, "Agora", "Te enviaron S/ 10.00", true),
        C(tunki, "Tunki", "Ana te mandó S/ 10.00", true),
        C(banbif, "BanBif", "Te transfirieron S/ 75.00", true),
        C(ligo, "Ligo", "Recibiste un pago Ligo de S/ 40.00", true),
        C(ligo, "Ligo", "Te pagaron S/ 40.00 con Ligo", true),
        C(pich, "Pichincha", "Te transfirieron S/ 60.00", true),
        C(sip, "Tarjeta Oh!", "Recibiste S/ 18.00", true),
        C(lemon, "Lemon", "Recibiste S/ 22.00", true),
        C(lemon, "Lemon", "Recibiste 22.00 soles de Juan", true),
        C(lemon, "Lemon", "Recibiste 22.50 PEN", true),
        // No deben detectarse
        C(yape, "Yape", "Yapeaste S/ 10 a Pedro", false),
        C(yape, "Yape", "Ganaste S/ 50 con Yape, participa del sorteo", false),
        C(bcp, "BCP", "Realizaste una transferencia de S/ 100.00", false),
        C(bcp, "BCP", "Tu clave de acceso es 1234. S/ 10", false),
        C(bcp, "BCP", "Aprovecha y paga tu recibo de luz, S/ 120 hasta hoy", false),
        C(bcp, "BCP", "Se debitó S/ 30.00 de tu cuenta", false),
        C(yape, "Yape", "Código de verificación 123456. No compartas.", false),
        C(bcp, "BCP", "Tienes un préstamo preaprobado de S/ 10,000", false),
        C(bcp, "BCP", "Crédito personal: te depositamos S/ 5,000 hoy", false),
        C(bcp, "BCP", "Te pagaron los intereses? Solicita tu línea de crédito de S/ 3,000", false),
        C(yape, "Yape", "Recibiste un Yape", false),
        C(lemon, "Lemon", "Tu saldo es 22.00 soles", false),
        C(bcp, "BCP", "Pagaste S/ 10 a Juan", false),
    )

    @Test
    fun cadaFraseSeDetectaONoSegunLoEsperado() {
        val fallas = casos.filter {
            (NotificationParser.parsear(it.t, it.x, it.pkg) is ResultadoParseo.MontoDetectado) != it.debe
        }.map { "${it.pkg} \"${it.x}\" debía=${it.debe}" }
        assertEquals("Frases mal clasificadas:\n" + fallas.joinToString("\n"), 0, fallas.size)
    }

    @Test
    fun todasLasBilleterasConfiablesTienenMetodo() {
        assertEquals(AppsDePagoConfiables.paquetesConfiables, MapeoAppsMetodoDeCobro.mapeo.keys)
    }
}
