package com.flabs.cobrate.core.data

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MetodoDeCobroSerializationTest {

    private val json = Json {
        encodeDefaults = true
        explicitNulls = false
    }

    @Test
    fun testMetodoDeCobroInsertDtoSerializationKeys() {
        val dto = MetodoDeCobroRepository.MetodoDeCobroInsertDto(
            negocioId = "test-negocio-id",
            tipo = "YAPE",
            datoDeCobro = "987654321",
            nombreDestinatario = "Titular Prueba",
            qrUrl = null,
            activo = true,
            oculto = false
        )

        val jsonString = json.encodeToString(dto)

        // 1. Debe contener "nombre_destinado"
        assertTrue(
            "El JSON serializado debe contener 'nombre_destinado'",
            jsonString.contains("\"nombre_destinado\"")
        )

        // 2. NO debe contener "nombre_destinatario"
        assertFalse(
            "El JSON serializado NO debe contener 'nombre_destinatario'",
            jsonString.contains("\"nombre_destinatario\"")
        )
    }

    @Test
    fun testMetodoDeCobroDtoSerializationKeys() {
        val dto = MetodoDeCobroRepository.MetodoDeCobroDto(
            id = "test-id",
            negocioId = "test-negocio-id",
            tipo = "YAPE",
            datoDeCobro = "987654321",
            nombreDestinatario = "Titular Prueba",
            qrUrl = null,
            activo = true,
            oculto = false,
            creadoEn = "2025-01-01T00:00:00Z"
        )

        val jsonString = json.encodeToString(dto)

        assertTrue(
            "El DTO de lectura debe contener 'nombre_destinado'",
            jsonString.contains("\"nombre_destinado\"")
        )

        assertFalse(
            "El DTO de lectura NO debe contener 'nombre_destinatario'",
            jsonString.contains("\"nombre_destinatario\"")
        )
    }
}
