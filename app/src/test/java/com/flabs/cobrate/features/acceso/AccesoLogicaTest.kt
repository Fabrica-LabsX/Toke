package com.flabs.cobrate.features.acceso

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AccesoLogicaTest {
    @Test
    fun correoValidoPideArroba() {
        assertTrue(AccesoLogica.correoValido("a@b.com"))
        assertFalse(AccesoLogica.correoValido("  "))
        assertFalse(AccesoLogica.correoValido("sin-arroba"))
    }

    @Test
    fun passwordsCortasOdistintasDanError() {
        assertEquals("La contraseña debe tener al menos 6 caracteres.", AccesoLogica.errorPasswords("12345", "12345"))
        assertEquals("Las contraseñas no coinciden.", AccesoLogica.errorPasswords("123456", "1234567"))
        assertNull(AccesoLogica.errorPasswords("123456", "123456"))
    }

    @Test
    fun nombreDuenoPrefiereElVisibleYQuitaElDominio() {
        assertEquals("Ana", AccesoLogica.nombreDueno(" Ana ", "x@y.com"))
        assertEquals("carlos", AccesoLogica.nombreDueno(null, "carlos@mail.com"))
        assertEquals("carlos", AccesoLogica.nombreDueno("", "carlos"))
        assertEquals("El dueño", AccesoLogica.nombreDueno(null, " "))
    }
}
