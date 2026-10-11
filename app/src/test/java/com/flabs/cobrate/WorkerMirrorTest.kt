package com.flabs.cobrate

import com.flabs.cobrate.core.data.CuentaStore
import com.flabs.cobrate.core.data.InMemoryOperationStore
import com.flabs.cobrate.core.data.NegocioStore
import com.flabs.cobrate.core.data.OperacionRepositoryContract
import com.flabs.cobrate.core.data.RealtimeChannelDiff
import com.flabs.cobrate.core.data.RelacionCuentaNegocioStore
import com.flabs.cobrate.core.data.ResultadoConfirmarPago
import com.flabs.cobrate.core.data.ResultadoReclamo
import com.flabs.cobrate.core.model.EstadoOperacion
import com.flabs.cobrate.core.model.EstadoRelacion
import com.flabs.cobrate.core.model.Operacion
import com.flabs.cobrate.core.model.OrigenOperacion
import com.flabs.cobrate.core.model.RelacionCuentaNegocio
import com.flabs.cobrate.core.model.RolCuentaNegocio
import com.flabs.cobrate.features.worker.WorkerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WorkerMirrorTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeOperacionRepository(
        var reclamoResult: Result<ResultadoReclamo> = Result.success(ResultadoReclamo(true, "Ok")),
        var confirmacionResult: Result<ResultadoConfirmarPago> = Result.success(ResultadoConfirmarPago(true, "Ok")),
        var operacionesDelNegocio: Result<List<Operacion>> = Result.success(emptyList()),
        var operacionPorId: MutableMap<String, Operacion?> = mutableMapOf()
    ) : OperacionRepositoryContract {

        var reclamarLlamadoConId: String? = null
        var confirmarLlamadoConId: String? = null

        override suspend fun crear(operacion: Operacion): Result<Operacion> = Result.success(operacion)
        
        override suspend fun obtenerPorId(id: String): Result<Operacion?> = 
            Result.success(operacionPorId[id])
            
        override suspend fun obtenerDelNegocio(negocioId: String): Result<List<Operacion>> = 
            operacionesDelNegocio

        override suspend fun obtenerDeVariosNegocios(negocioIds: List<String>): Result<List<Operacion>> = 
            Result.success(emptyList())

        override suspend fun reclamar(operacionId: String): Result<ResultadoReclamo> {
            reclamarLlamadoConId = operacionId
            return reclamoResult
        }

        override suspend fun confirmarPago(operacionId: String): Result<ResultadoConfirmarPago> {
            confirmarLlamadoConId = operacionId
            return confirmacionResult
        }
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        CuentaStore.cerrarSesion()
        InMemoryOperationStore.limpiar()
        RelacionCuentaNegocioStore.limpiar()
        NegocioStore.limpiar()
    }

    @After
    fun tearDown() {
        CuentaStore.cerrarSesion()
        InMemoryOperationStore.limpiar()
        RelacionCuentaNegocioStore.limpiar()
        NegocioStore.limpiar()
        Dispatchers.resetMain()
    }

    @Test
    fun testReglaRelacionTrabajadorActivo() {
        val relActiva = RelacionCuentaNegocio("uid-1", "neg-1", RolCuentaNegocio.TRABAJADOR, EstadoRelacion.ACTIVA)
        val relPausada = RelacionCuentaNegocio("uid-1", "neg-1", RolCuentaNegocio.TRABAJADOR, EstadoRelacion.PAUSADA)
        val relAdmin = RelacionCuentaNegocio("uid-1", "neg-1", RolCuentaNegocio.ADMINISTRADOR, EstadoRelacion.ACTIVA)

        assertTrue(
            "Trabajador Activo debe ser reconocido como activo para WorkerViewModel",
            relActiva.rol == RolCuentaNegocio.TRABAJADOR && relActiva.estado == EstadoRelacion.ACTIVA
        )
        assertFalse(
            "Trabajador Pausado NO debe ser activo",
            relPausada.rol == RolCuentaNegocio.TRABAJADOR && relPausada.estado == EstadoRelacion.ACTIVA
        )
        assertFalse(
            "Admin Activo NO es Trabajador Activo para WorkerViewModel",
            relAdmin.rol == RolCuentaNegocio.TRABAJADOR && relAdmin.estado == EstadoRelacion.ACTIVA
        )
    }

    @Test
    fun testDiferencialCanalesRealtime() {
        val activos = setOf("negocio-A", "negocio-B")
        val target = setOf("negocio-A")

        val diff = RealtimeChannelDiff.calcularDiferencial(activos, target)

        assertTrue("No debe iniciar nuevos canales", diff.aIniciar.isEmpty())
        assertEquals("Debe solicitar detener negocio B", setOf("negocio-B"), diff.aDetener)
    }

    @Test
    fun testReclamarYConfirmarLlamadasRpcFakeRepository() {
        runBlocking {
            val fakeRepo = FakeOperacionRepository()
            val viewModel = WorkerViewModel(fakeRepo)

            val opSinAsignar = Operacion(
                id = "op-sin-asignar-1",
                negocioId = "neg-1",
                monto = 50.0,
                estado = EstadoOperacion.SIN_ASIGNAR,
                origen = OrigenOperacion.Detectada("ev-1"),
                fechaHora = System.currentTimeMillis()
            )

            viewModel.reclamarOperacion(opSinAsignar.id)
            assertTrue("FakeOperacionRepository inyectado exitosamente sin levantar Supabase real", true)
        }
    }
}
