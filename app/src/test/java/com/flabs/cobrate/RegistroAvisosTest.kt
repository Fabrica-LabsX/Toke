package com.flabs.cobrate

import com.flabs.cobrate.core.logic.RegistroAvisos
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RegistroAvisosTest {

    @Before
    fun setUp() {
        RegistroAvisos.limpiar()
    }

    @Test
    fun testPrimerAvisoDevuelveFalseYElSegundoDevuelveTrue() {
        val opId = "op-123"
        assertFalse(RegistroAvisos.yaNotificadoOAgregar(opId))
        assertTrue(RegistroAvisos.yaNotificadoOAgregar(opId))
    }

    @Test
    fun testCapacidadMaximaDesplazaElMasAntiguo() {
        for (i in 1..200) {
            assertFalse(RegistroAvisos.yaNotificadoOAgregar("op-$i"))
        }

        // op-2 sigue en el registro
        assertTrue(RegistroAvisos.yaNotificadoOAgregar("op-2"))

        // Agregamos op-201. Como hay 200 elementos, remueve el mas antiguo (op-1)
        assertFalse(RegistroAvisos.yaNotificadoOAgregar("op-201"))

        // op-1 ya fue removido, por lo que al consultarlo da false
        assertFalse(RegistroAvisos.yaNotificadoOAgregar("op-1"))
    }
}
