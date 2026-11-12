package com.flabs.cobrate

import com.flabs.cobrate.core.logic.EntradaSegura
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EntradaSeguraTest {
    @Test
    fun rechazaInyeccionSqlClasica() {
        listOf(
            "' OR 1=1 --", "admin' or '1'='1", "x'; DROP TABLE cuentas; --", "1 UNION SELECT password FROM auth.users",
            "a'; select pg_sleep(5)", "<script>alert(1)</script>", "javascript:alert(1)", "/* hola */",
        ).forEach { assertTrue(it, EntradaSegura.sospechosa(it)) }
    }

    @Test
    fun aceptaNombresReales() {
        listOf("Pollería D'Elia", "Bodega 3-B", "María José", "Select Bar", "Café & Té", "Juan Pérez Ñ", "O'Brien", "Tienda Los Olivos")
            .forEach { assertFalse(it, EntradaSegura.sospechosa(it)) }
    }

    @Test
    fun limpiaInvisiblesYEspacios() {
        assertEquals("Rosa Pérez", EntradaSegura.limpiar("  Rosa​   Pérez\u0007 "))
        assertEquals(5, EntradaSegura.limpiar("abcdefghij", 5).length)
    }

    @Test
    fun errorNombreRevisaLargoYAtaque() {
        assertNull(EntradaSegura.errorNombre("Mi Bodega", 2, 40))
        assertNotNull(EntradaSegura.errorNombre("a", 2, 40))
        assertNotNull(EntradaSegura.errorNombre("a".repeat(41), 2, 40))
        assertNotNull(EntradaSegura.errorNombre("x'; DROP TABLE negocios; --", 2, 40))
    }

    @Test
    fun correoSeguro() {
        assertTrue(EntradaSegura.correoSeguro("a.b+c@dominio.com"))
        assertFalse(EntradaSegura.correoSeguro("a@b"))
        assertFalse(EntradaSegura.correoSeguro("x' or 1=1--@a.com"))
        assertFalse(EntradaSegura.correoSeguro("a b@c.com"))
        assertFalse(EntradaSegura.correoSeguro("a@b.com; drop table x"))
    }

    @Test
    fun datoDeCobro() {
        assertTrue(EntradaSegura.datoDeCobroSeguro("987 654 321"))
        assertTrue(EntradaSegura.datoDeCobroSeguro("tienda@alias"))
        assertFalse(EntradaSegura.datoDeCobroSeguro("987'; drop table x;--"))
        assertFalse(EntradaSegura.datoDeCobroSeguro("12"))
    }
}
