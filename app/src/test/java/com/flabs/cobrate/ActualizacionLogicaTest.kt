package com.flabs.cobrate

import com.flabs.cobrate.features.actualizacion.ActualizacionLogica
import com.flabs.cobrate.features.actualizacion.EstadoActualizacion
import com.flabs.cobrate.features.actualizacion.ManifiestoVersion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ActualizacionLogicaTest {
    private val hash = "a".repeat(64)
    private fun m(latest: Int, min: Int, pkg: String = "com.flabs.cobrate", url: String = "https://cdn.toke.pe/toke.apk", sha: String = hash) =
        ManifiestoVersion(pkg, "stable", latest, "v$latest", min, url, 1_000_000, sha)

    @Test
    fun alDiaCuandoLaInstaladaEsLaUltima() {
        assertEquals(EstadoActualizacion.AlDia, ActualizacionLogica.decidir(10, m(10, 5), 0, 0))
    }

    @Test
    fun opcionalCuandoHayNuevaPeroSigueAdmitida() {
        assertTrue(ActualizacionLogica.decidir(7, m(10, 5), 0, 0) is EstadoActualizacion.Opcional)
    }

    @Test
    fun obligatoriaPorDebajoDeLaMinima() {
        assertTrue(ActualizacionLogica.decidir(4, m(10, 5), 0, 0) is EstadoActualizacion.Obligatoria)
    }

    @Test
    fun laComparacionEsNumericaNoDeTexto() {
        // Como texto "9" > "10"; como número 9 < 10 y 9 < min 10.
        assertTrue(ActualizacionLogica.decidir(9, m(12, 10), 0, 0) is EstadoActualizacion.Obligatoria)
        assertEquals(EstadoActualizacion.AlDia, ActualizacionLogica.decidir(100, m(12, 10), 0, 0))
    }

    @Test
    fun sinRespuestaVerificadaNuncaBloquea() {
        assertEquals(EstadoActualizacion.AlDia, ActualizacionLogica.decidir(1, null, 0, 0))
    }

    @Test
    fun laObligatoriaGuardadaSigueBloqueandoSinRedPeroLaOpcionalVencidaNo() {
        val vieja = ActualizacionLogica.TOLERANCIA_OPCIONAL_MS + 1
        assertTrue(ActualizacionLogica.decidir(4, m(10, 5), 0, vieja) is EstadoActualizacion.Obligatoria)
        assertEquals(EstadoActualizacion.AlDia, ActualizacionLogica.decidir(7, m(10, 5), 0, vieja))
    }

    @Test
    fun manifiestoInvalidoSeRechaza() {
        val p = "com.flabs.cobrate"
        assertTrue(ActualizacionLogica.manifiestoValido(m(10, 5), p))
        assertFalse(ActualizacionLogica.manifiestoValido(m(10, 5, pkg = "com.otra.app"), p))
        assertFalse(ActualizacionLogica.manifiestoValido(m(10, 5, url = "http://x/toke.apk"), p))
        assertFalse(ActualizacionLogica.manifiestoValido(m(10, 5, sha = "xyz"), p))
        assertFalse(ActualizacionLogica.manifiestoValido(m(10, 11), p)) // mínima mayor que la última
    }

    @Test
    fun apkDeOtroPaqueteOFirmaDistintaOVersionDistintaSeRechaza() {
        val cert = setOf("c1")
        val p = "com.flabs.cobrate"
        assertTrue(ActualizacionLogica.apkAceptable(p, 10, cert, p, 10, cert))
        assertFalse(ActualizacionLogica.apkAceptable("com.malo", 10, cert, p, 10, cert))
        assertFalse(ActualizacionLogica.apkAceptable(p, 10, setOf("otro"), p, 10, cert))
        assertFalse(ActualizacionLogica.apkAceptable(p, 9, cert, p, 10, cert))
        assertFalse(ActualizacionLogica.apkAceptable(p, 10, emptySet(), p, 10, emptySet()))
        assertFalse(ActualizacionLogica.apkAceptable(null, 10, cert, p, 10, cert))
    }

    @Test
    fun progresoCalculaPorcentajeVelocidadYTiempoRestante() {
        val pr = ActualizacionLogica.progreso(5_242_880, 10_485_760, 5_000) // 5 MB en 5 s
        assertEquals(50, pr.porcentaje)
        assertEquals(1_048_576, pr.bytesPorSegundo)
        assertEquals(5L, pr.segundosRestantes)
        assertNull(ActualizacionLogica.progreso(0, 100, 0).segundosRestantes)
        assertEquals("1.0 MB/s", ActualizacionLogica.formatoVelocidad(1_048_576))
        assertEquals("1 min 5 s", ActualizacionLogica.formatoTiempo(65))
    }
}
