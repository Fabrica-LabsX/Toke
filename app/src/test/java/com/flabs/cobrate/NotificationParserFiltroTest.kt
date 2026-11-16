// TEXTOS SINTÉTICOS: validar contra notificaciones reales del dispositivo
package com.flabs.cobrate

import com.flabs.cobrate.core.logic.NivelConfianzaParseo
import com.flabs.cobrate.core.logic.NotificationParser
import com.flabs.cobrate.core.logic.ResultadoParseo
import org.junit.Assert.assertEquals
import org.junit.Test

// TEXTOS SINTÉTICOS: validar contra notificaciones reales del dispositivo
class NotificationParserFiltroTest {

    private val paquetePrueba = "com.bcp.bank.bcp"

    @Test
    fun testGanaSoatEsNoEsPago() {
        val resultado = NotificationParser.parsear("", "Gana S/100 soles Pagando tu SOAT!", paquetePrueba)
        assertEquals(ResultadoParseo.NoEsPago, resultado)
    }

    @Test
    fun testAprovechaDescuentoEsNoEsPago() {
        val resultado = NotificationParser.parsear("", "Aprovecha S/ 50 de descuento en tu primera compra", paquetePrueba)
        assertEquals(ResultadoParseo.NoEsPago, resultado)
    }

    @Test
    fun testParticipaSorteoEsNoEsPago() {
        val resultado = NotificationParser.parsear("", "Participa en el sorteo y gana S/ 1000", paquetePrueba)
        assertEquals(ResultadoParseo.NoEsPago, resultado)
    }

    @Test
    fun testClaveVerificacionEsNoEsPago() {
        val resultado = NotificationParser.parsear("", "Tu clave de verificación es 123456. No compartas S/ 10", paquetePrueba)
        assertEquals(ResultadoParseo.NoEsPago, resultado)
    }

    @Test
    fun testPagasteEsNoEsPago() {
        val resultado = NotificationParser.parsear("", "Pagaste S/ 25.00 a Bodega Luz", paquetePrueba)
        assertEquals(ResultadoParseo.NoEsPago, resultado)
    }

    @Test
    fun testRealizasteTransferenciaEsNoEsPago() {
        val resultado = NotificationParser.parsear("", "Realizaste una transferencia por S/ 100", paquetePrueba)
        assertEquals(ResultadoParseo.NoEsPago, resultado)
    }

    @Test
    fun testSolicitaPrestamoEsNoEsPago() {
        val resultado = NotificationParser.parsear("", "Solicita tu préstamo hasta S/ 20,000", paquetePrueba)
        assertEquals(ResultadoParseo.NoEsPago, resultado)
    }

    @Test
    fun testSeAcreditoMontoDetectado() {
        val resultado = NotificationParser.parsear("", "Se acreditó S/ 80.00 en tu cuenta", paquetePrueba)
        assertEquals(ResultadoParseo.MontoDetectado(80.0, NivelConfianzaParseo.ALTA), resultado)
    }

    @Test
    fun testRecibisteMontoDetectado() {
        val resultado = NotificationParser.parsear("", "Recibiste S/ 35.00 de Juan P.", paquetePrueba)
        assertEquals(ResultadoParseo.MontoDetectado(35.0, NivelConfianzaParseo.ALTA), resultado)
    }

    @Test
    fun testTeEnvioMontoDetectado() {
        val resultado = NotificationParser.parsear("", "Yape! Ana R. te envió un pago por S/ 12.50", paquetePrueba)
        assertEquals(ResultadoParseo.MontoDetectado(12.5, NivelConfianzaParseo.ALTA), resultado)
    }

    @Test
    fun testTeYapeoMontoDetectado() {
        val resultado = NotificationParser.parsear("", "Te yapeó S/ 1,250.50", paquetePrueba)
        assertEquals(ResultadoParseo.MontoDetectado(1250.5, NivelConfianzaParseo.ALTA), resultado)
    }

    @Test
    fun testTransferenciaRecibidaMontoDetectado() {
        val resultado = NotificationParser.parsear("", "Transferencia recibida por S/ 200", paquetePrueba)
        assertEquals(ResultadoParseo.MontoDetectado(200.0, NivelConfianzaParseo.ALTA), resultado)
    }

    @Test
    fun testSaldoDisponibleEsNoEsPago() {
        val resultado = NotificationParser.parsear("", "Saldo disponible S/ 300", paquetePrueba)
        assertEquals(ResultadoParseo.NoEsPago, resultado)
    }

    @Test
    fun testNovedadesEsNoEsPago() {
        val resultado = NotificationParser.parsear("", "Hola, tenemos novedades para ti", paquetePrueba)
        assertEquals(ResultadoParseo.NoEsPago, resultado)
    }
}
