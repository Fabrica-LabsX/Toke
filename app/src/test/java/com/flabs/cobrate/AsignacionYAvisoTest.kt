package com.flabs.cobrate

import com.flabs.cobrate.core.logic.DestinatarioAviso
import com.flabs.cobrate.features.operaciones.AsignacionLogica
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AsignacionYAvisoTest {
    @Test
    fun pagosPorAsignarSoloConDosOMasTrabajadores() {
        assertFalse(AsignacionLogica.habilitada(0, null))
        assertFalse(AsignacionLogica.habilitada(1, null))
        assertFalse(AsignacionLogica.habilitada(1, 1))
        assertTrue(AsignacionLogica.habilitada(2, null))
        // El celular de un trabajador solo ve su propia relación: manda lo que dice el servidor.
        assertTrue(AsignacionLogica.habilitada(1, 2))
    }

    @Test
    fun elDuenoNoRecibeElPagoConfirmado() {
        assertFalse(DestinatarioAviso.avisaPagoConfirmado(esDuenoDelNegocio = true))
        assertTrue(DestinatarioAviso.avisaPagoConfirmado(esDuenoDelNegocio = false))
    }
}
