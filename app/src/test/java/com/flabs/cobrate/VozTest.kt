package com.flabs.cobrate

import com.flabs.cobrate.core.logic.VoicePhraseBuilder
import com.flabs.cobrate.core.model.ConfiguracionDeVoz
import com.flabs.cobrate.core.model.ModoVoz
import com.flabs.cobrate.core.model.ModoVozMigracion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class VozTest {
    private val cauteloso = ConfiguracionDeVoz(modo = ModoVoz.CAUTELOSO)
    private val basico = ConfiguracionDeVoz(modo = ModoVoz.BASICO)

    @Test
    fun cautelosoSoloConfirmaElPago() {
        val frase = VoicePhraseBuilder.construirFrase(cauteloso, 25.5, cliente = "Ana", billetera = "Yape")
        assertEquals("Pago confirmado.", frase)
        assertFalse(frase.contains("Yape") || frase.contains("Ana") || frase.contains("25"))
    }

    @Test
    fun basicoDictaBilleteraQuienPagoYMonto() {
        assertEquals(
            "Pago confirmado en Yape de Ana, 25.5 soles.",
            VoicePhraseBuilder.construirFrase(basico, 25.5, cliente = "Ana", billetera = "Yape"),
        )
        assertEquals(
            "Pago confirmado en Plin, 25 soles.",
            VoicePhraseBuilder.construirFrase(basico, 25.0, billetera = "Plin"),
        )
        assertEquals("Pago confirmado, 10 soles.", VoicePhraseBuilder.construirFrase(basico, 10.0))
    }

    @Test
    fun migraLosModosAnteriores() {
        assertEquals(ModoVoz.CAUTELOSO, ModoVozMigracion.desdeGuardado("CAUTELOSO", null))
        assertEquals(ModoVoz.BASICO, ModoVozMigracion.desdeGuardado("NEGOCIO", null))
        assertEquals(ModoVoz.BASICO, ModoVozMigracion.desdeGuardado("BASICO", null))
        assertEquals(ModoVoz.CAUTELOSO, ModoVozMigracion.desdeGuardado("TOKI", "DISCRETO"))
        assertEquals(ModoVoz.CAUTELOSO, ModoVozMigracion.desdeGuardado("TOKI", null))
        assertEquals(ModoVoz.BASICO, ModoVozMigracion.desdeGuardado("TOKI", "NEGOCIO"))
        assertEquals(ModoVoz.CAUTELOSO, ModoVozMigracion.desdeGuardado("otro", null))
    }
}
