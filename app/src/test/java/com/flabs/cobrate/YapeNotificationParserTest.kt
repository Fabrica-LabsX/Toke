package com.flabs.cobrate

import com.flabs.cobrate.core.logic.NivelConfianzaParseo
import com.flabs.cobrate.core.logic.NotificationParser
import com.flabs.cobrate.core.logic.ResultadoParseo
import com.flabs.cobrate.core.logic.YapeNotificationParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas unitarias específicas para el parser determinista de Yape ([YapeNotificationParser]).
 */
class YapeNotificationParserTest {

    private val paqueteYape = YapeNotificationParser.PAQUETE_YAPE
    private val tituloConfirmacion = "Confirmación de Pago"

    @Test
    fun nombreCensuradoConCodigo() {
        val texto = "Luis Per* te envió un pago por S/ 10. El cód. de seguridad es: 715"
        val resultadoCoord = NotificationParser.parsear(tituloConfirmacion, texto, paqueteYape)
        assertEquals(
            ResultadoParseo.MontoDetectado(10.0, NivelConfianzaParseo.ALTA, "Luis Per*"),
            resultadoCoord
        )

        val detalle = YapeNotificationParser.parsearYape(tituloConfirmacion, texto)
        assertNotNull(detalle)
        assertEquals(10.0, detalle!!.monto, 0.0001)
        assertEquals("Luis Per*", detalle.pagador)
    }

    @Test
    fun nombreCensuradoSinCodigo() {
        val texto = "Convercion Leo* te envió un pago por S/ 15"
        val resultadoCoord = NotificationParser.parsear(tituloConfirmacion, texto, paqueteYape)
        assertEquals(
            ResultadoParseo.MontoDetectado(15.0, NivelConfianzaParseo.ALTA, "Convercion Leo*"),
            resultadoCoord
        )

        val detalle = YapeNotificationParser.parsearYape(tituloConfirmacion, texto)
        assertNotNull(detalle)
        assertEquals(15.0, detalle!!.monto, 0.0001)
        assertEquals("Convercion Leo*", detalle.pagador)
    }

    @Test
    fun nombreCompletoConPrefijoYapeYSinCodigo() {
        val texto = "Yape! CHARLY RUBEN LLONTOP te envió un pago por S/ 10"
        val resultadoCoord = NotificationParser.parsear(tituloConfirmacion, texto, paqueteYape)
        assertEquals(
            ResultadoParseo.MontoDetectado(10.0, NivelConfianzaParseo.ALTA, "CHARLY RUBEN LLONTOP"),
            resultadoCoord
        )

        val detalle = YapeNotificationParser.parsearYape(tituloConfirmacion, texto)
        assertNotNull(detalle)
        assertEquals(10.0, detalle!!.monto, 0.0001)
        assertEquals("CHARLY RUBEN LLONTOP", detalle.pagador)
    }

    @Test
    fun nombreComercialLargoConAmpersandYSufijoEIRL() {
        val texto = "INGENIERIA & MECATRONICA AUTOMOTRIZ FERNANDEZ EIRL te envió un pago por S/ 20. El cód. de seguridad es: 001"
        val resultadoCoord = NotificationParser.parsear(tituloConfirmacion, texto, paqueteYape)
        assertEquals(
            ResultadoParseo.MontoDetectado(
                20.0,
                NivelConfianzaParseo.ALTA,
                "INGENIERIA & MECATRONICA AUTOMOTRIZ FERNANDEZ EIRL"
            ),
            resultadoCoord
        )

        val detalle = YapeNotificationParser.parsearYape(tituloConfirmacion, texto)
        assertNotNull(detalle)
        assertEquals(20.0, detalle!!.monto, 0.0001)
        assertEquals("INGENIERIA & MECATRONICA AUTOMOTRIZ FERNANDEZ EIRL", detalle.pagador)
        // Verificar que no se truncó a 38 caracteres
        assertTrue(detalle.pagador.length > 38)
    }

    @Test
    fun codigoConCerosInicialesNoAfectaResultadoYNoSeExpone() {
        data class CasoPrueba(val texto: String, val monto: Double, val pagador: String)
        val casosConCeros = listOf(
            CasoPrueba("Maria Leo* te envió un pago por S/ 5. El cód. de seguridad es: 006", 5.0, "Maria Leo*"),
            CasoPrueba("Alan Eva* te envió un pago por S/ 20. El cód. de seguridad es: 062", 20.0, "Alan Eva*"),
            CasoPrueba("Hugo Cer* te envió un pago por S/ 4. El cód. de seguridad es: 018", 4.0, "Hugo Cer*"),
            CasoPrueba("Kelvin Car* te envió un pago por S/ 10. El cód. de seguridad es: 096", 10.0, "Kelvin Car*"),
            CasoPrueba("INGENIERIA & MECATRONICA AUTOMOTRIZ FERNANDEZ EIRL te envió un pago por S/ 20. El cód. de seguridad es: 001", 20.0, "INGENIERIA & MECATRONICA AUTOMOTRIZ FERNANDEZ EIRL")
        )

        for (caso in casosConCeros) {
            val detalle = YapeNotificationParser.parsearYape(tituloConfirmacion, caso.texto)
            assertNotNull("Fallo al parsear: ${caso.texto}", detalle)
            assertEquals("Monto no coincide en: ${caso.texto}", caso.monto, detalle!!.monto, 0.0001)
            assertEquals("Pagador no coincide en: ${caso.texto}", caso.pagador, detalle.pagador)

            val coord = NotificationParser.parsear(tituloConfirmacion, caso.texto, paqueteYape)
            assertEquals(
                ResultadoParseo.MontoDetectado(caso.monto, NivelConfianzaParseo.ALTA, caso.pagador),
                coord
            )
        }

        // Demostrar que ResultadoYape no contiene ni expone el código de seguridad
        val campos = YapeNotificationParser.ResultadoYape::class.java.declaredFields.map { it.name }
        assertTrue("ResultadoYape debe contener 'monto'", campos.contains("monto"))
        assertTrue("ResultadoYape debe contener 'pagador'", campos.contains("pagador"))
        assertTrue("ResultadoYape NO debe contener campo de código de seguridad", !campos.contains("codigoSeguridad"))
    }

    @Test
    fun importeEntero() {
        val texto = "Juan Mes* te envió un pago por S/ 10"
        val resultado = NotificationParser.parsear(tituloConfirmacion, texto, paqueteYape)
        assertEquals(ResultadoParseo.MontoDetectado(10.0, NivelConfianzaParseo.ALTA, "Juan Mes*"), resultado)
    }

    @Test
    fun importeDecimalConUnoYDosDecimales() {
        // Con un decimal
        val textoUnDecimal = "Ginez Sil* te envió un pago por S/ 2.5. El cód. de seguridad es: 122"
        val resultadoUnDecimal = NotificationParser.parsear(tituloConfirmacion, textoUnDecimal, paqueteYape)
        assertEquals(ResultadoParseo.MontoDetectado(2.5, NivelConfianzaParseo.ALTA, "Ginez Sil*"), resultadoUnDecimal)

        // Con dos decimales
        val textoDosDecimales = "Yape! SOLORZANO SALAS DIEGO ARMANDO te envió un pago por S/ 10.00"
        val resultadoDosDecimales = NotificationParser.parsear(tituloConfirmacion, textoDosDecimales, paqueteYape)
        assertEquals(ResultadoParseo.MontoDetectado(10.0, NivelConfianzaParseo.ALTA, "SOLORZANO SALAS DIEGO ARMANDO"), resultadoDosDecimales)
    }

    @Test
    fun tituloVacioConCuerpoValidoEnExtras() {
        val cuerpo = "Luis Per* te envió un pago por S/ 10. El cód. de seguridad es: 715"
        val resultado = NotificationParser.parsear(
            titulo = "",
            texto = "",
            paqueteOrigen = paqueteYape,
            datosAdicionales = cuerpo
        )
        assertEquals(ResultadoParseo.MontoDetectado(10.0, NivelConfianzaParseo.ALTA, "Luis Per*"), resultado)
    }

    @Test
    fun paqueteDistintoDeYapeNoUsaElParserNuevo() {
        val texto = "Luis Per* te envió un pago por S/ 10. El cód. de seguridad es: 715"
        val paqueteOtro = "com.bcp.bank.bcp"

        val resultado = NotificationParser.parsear(tituloConfirmacion, texto, paqueteOtro)
        // Para cualquier otro paquete se utiliza la ruta heredada (donde pagador no viene en el MontoDetectado)
        assertTrue(resultado is ResultadoParseo.MontoDetectado)
        val detectado = resultado as ResultadoParseo.MontoDetectado
        assertEquals(10.0, detectado.monto, 0.0001)
        assertNull("El parser nuevo no debió fijar el pagador para un paquete no-Yape", detectado.pagador)
    }

    @Test
    fun mensajesDeEgresoPromocionSaldoOAutenticacionNoGeneranPago() {
        val noPagos = listOf(
            "Yapeaste S/ 10 a Pedro",
            "Pagaste S/ 25.00 a Bodega Luz",
            "Enviaste S/ 15.00",
            "Realizaste una transferencia por S/ 100",
            "Gana S/100 soles Pagando tu SOAT!",
            "Participa en el sorteo y gana S/ 1000",
            "Aprovecha S/ 50 de descuento en tu primera compra",
            "Solicita tu préstamo hasta S/ 20,000",
            "Saldo disponible S/ 300",
            "Tu saldo es de S/ 50",
            "Tu clave de verificación es 123456. No compartas S/ 10",
            "Código de verificación 123456. No compartas.",
            "Inicio de sesión detectado en nuevo dispositivo",
            "Hola, tenemos novedades para ti"
        )

        for (caso in noPagos) {
            val resultado = NotificationParser.parsear(tituloConfirmacion, caso, paqueteYape)
            assertEquals("El mensaje \"$caso\" no debió generar pago", ResultadoParseo.NoEsPago, resultado)
            assertNull("parsearYape debió devolver null para \"$caso\"", YapeNotificationParser.parsearYape(tituloConfirmacion, caso))
        }
    }

    @Test
    fun importeCeroYFormatosNumericosMalformadosNoSeAceptan() {
        val casosMalformados = listOf(
            "Luis te envió un pago por S/ 0",
            "Luis te envió un pago por S/ 0.00",
            "Luis te envió un pago por S/ -10",
            "Luis te envió un pago por S/ 12,5", // coma decimal no se interpreta silenciosamente
            "Luis te envió un pago por S/ 1.2.3",
            "Luis te envió un pago por S/ 1,00", // miles mal agrupados
            "Luis te envió un pago por S/ abc",
            "Luis te envió un pago por S/ ,"
        )

        for (caso in casosMalformados) {
            val resultado = NotificationParser.parsear(tituloConfirmacion, caso, paqueteYape)
            assertEquals("El caso \"$caso\" no debió aceptarse como pago", ResultadoParseo.NoEsPago, resultado)
        }
    }
}
