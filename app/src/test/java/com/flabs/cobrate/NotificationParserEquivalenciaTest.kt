// TEXTOS SINTÉTICOS: validar contra notificaciones reales del dispositivo
package com.flabs.cobrate

import com.flabs.cobrate.core.logic.AppsDePagoConfiables
import com.flabs.cobrate.core.logic.NotificationParser
import com.flabs.cobrate.core.logic.ResultadoParseo
import com.flabs.cobrate.core.logic.YapeNotificationParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Random

/**
 * Compara el parser actual con la copia congelada del original ([NotificationParserLegacy])
 * sobre decenas de miles de mensajes generados. Si la separación por archivos cambiara el
 * resultado de un solo mensaje, esta prueba falla y muestra cuál.
 */
class NotificationParserEquivalenciaTest {

    private val promocionales = listOf(
        "gana", "ganar", "ganate", "gane", "sorteo", "sortea", "premio", "cashback", "cupón", "regalo",
        "bono", "participa", "canjea", "obtén", "aprovecha", "oferta", "promo", "promoción", "promociones",
        "descuento", "dscto", "20% off", "20%off", "hasta S/ 500", "desde S/ 9", "por solo S/ 9",
        "solo por S/ 9", "paga tu", "pagando tu", "pagar tu", "paga con", "pagando con", "solicita",
        "préstamo", "crédito", "preaprobado", "pre aprobado", "tarjeta de crédito", "invita", "refiere",
        "no te lo pierdas", "últimos días", "SOAT", "recibo de luz", "recibo de agua", "vence", "vencimiento"
    )
    private val seguridad = listOf(
        "código de verificación", "clave", "token", "no compartas", "nunca compartas",
        "actualiza tu app", "inicio de sesión"
    )
    private val salientes = listOf(
        "pagaste", "enviaste", "yapeaste", "transferiste", "realizaste", "retiraste", "compraste",
        "has pagado", "pago realizado", "pago de servicio", "consumo", "cargo", "débito", "se debitó",
        "se descontó", "retiro"
    )
    private val entrantes = listOf(
        "recibiste", "has recibido", "recibió", "te envió", "te yapeó", "te transfirió", "te depositó",
        "te abonó", "te pagó", "te mandó", "te han enviado", "te ha transferido", "te han depositado",
        "te ha abonado", "te han yapeado", "te enviaron", "pago recibido", "abono recibida",
        "transferencia recibida", "depósito recibido", "ingreso recibido", "yapeo recibido",
        "se acreditó", "se abonó", "se depositó"
    )
    private val montos = listOf(
        "S/ 35.00", "S/35", "S/. 1,250.50", "s/ 0.5", "S/ 12,5", "S/  80", "S/ 1.2.3", "S/ abc", "S/ ,",
        "S/ 1,000", "S/. 7", "S/ 100.", "s/.12.30", "S/ 0", "S/ 99999999999999999999"
    )
    private val relleno = listOf(
        "", "Yape!", "Hola", "Juan P.", "Ana R.", "en tu cuenta", "de", "por", "BCP", "Interbank",
        "para ti", "ahora", "Saldo disponible", "novedades", "un pago", "una transferencia", "Plin"
    )
    // Derivado directamente de la fuente de verdad excluyendo Yape (exactamente 12 paquetes)
    private val paquetes = (AppsDePagoConfiables.paquetesConfiables - YapeNotificationParser.PAQUETE_YAPE).toList().sorted()

    private fun elige(r: Random, lista: List<String>) = lista[r.nextInt(lista.size)]

    private fun pieza(r: Random): String = when (r.nextInt(10)) {
        0 -> elige(r, promocionales)
        1 -> elige(r, seguridad)
        2 -> elige(r, salientes)
        3, 4, 5 -> elige(r, entrantes)
        6, 7 -> elige(r, montos)
        else -> elige(r, relleno)
    }

    private fun variante(r: Random, s: String): String = when (r.nextInt(5)) {
        0 -> s.uppercase()
        1 -> s.lowercase()
        else -> s
    }

    private fun texto(r: Random): String =
        (1..(1 + r.nextInt(6))).joinToString(" ") { variante(r, pieza(r)) }

    @Test
    fun elParserNuevoDaElMismoResultadoQueElOriginal() {
        val r = Random(20261106L)
        val paquetesProbados = mutableSetOf<String>()
        var pagos = 0
        var noPagos = 0
        repeat(60_000) { i ->
            val titulo = if (r.nextInt(3) == 0) texto(r) else ""
            val cuerpo = texto(r)
            val extras = if (r.nextInt(4) == 0) texto(r) else null
            val paquete = elige(r, paquetes)
            paquetesProbados.add(paquete)

            val esperado = NotificationParserLegacy.parsear(titulo, cuerpo, paquete, extras)
            val actual = NotificationParser.parsear(titulo, cuerpo, paquete, extras)

            assertEquals(
                "Difieren en el caso $i -> titulo=\"$titulo\" texto=\"$cuerpo\" extras=\"$extras\"",
                esperado,
                actual
            )
            if (esperado is ResultadoParseo.MontoDetectado) pagos++ else noPagos++
        }
        // Verificar que los 12 paquetes fueron efectivamente probados
        assertEquals(12, paquetes.size)
        assertEquals(12, paquetesProbados.size)
        assertEquals(paquetes.toSet(), paquetesProbados)

        // El corpus tiene que ejercitar los dos resultados, si no la prueba no demuestra nada.
        assertTrue("Muy pocos pagos en el corpus: $pagos", pagos > 1_000)
        assertTrue("Muy pocos no-pagos en el corpus: $noPagos", noPagos > 1_000)
    }

    @Test
    fun ningunPaqueteNoYapeEsDirigidoAlParserDeYape() {
        val paquetesNoYape = AppsDePagoConfiables.paquetesConfiables - YapeNotificationParser.PAQUETE_YAPE
        assertEquals(12, paquetesNoYape.size)
        val textoTipicoYape = "Yape! Juan Perez te envió un pago por S/ 25. El cód. de seguridad es: 123"

        for (paquete in paquetesNoYape) {
            // YapeNotificationParser rechaza directamente cualquier paquete que no sea Yape
            val resultadoYapeDirecto = YapeNotificationParser.parsear("Confirmación de Pago", textoTipicoYape, paquete)
            assertEquals(
                "YapeNotificationParser debe rechazar paquete $paquete",
                ResultadoParseo.NoEsPago,
                resultadoYapeDirecto
            )

            // NotificationParser deriva a legacy para paquetes no-Yape
            val resultadoCoord = NotificationParser.parsear("Confirmación de Pago", textoTipicoYape, paquete)
            val resultadoLegacy = NotificationParserLegacy.parsear("Confirmación de Pago", textoTipicoYape, paquete)
            assertEquals(
                "NotificationParser debe derivar $paquete a legacy de forma idéntica",
                resultadoLegacy,
                resultadoCoord
            )

            // En legacy, MontoDetectado nunca contiene pagador
            if (resultadoCoord is ResultadoParseo.MontoDetectado) {
                assertNull("Un paquete no-Yape no debe recibir pagador del parser de Yape", resultadoCoord.pagador)
            }
        }
    }
}
