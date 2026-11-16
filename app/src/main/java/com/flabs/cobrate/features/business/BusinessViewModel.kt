package com.flabs.cobrate.features.business

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flabs.cobrate.core.data.NegocioStore
import com.flabs.cobrate.core.data.CuentaRepository
import com.flabs.cobrate.core.data.CuentaStore
import com.flabs.cobrate.core.data.InMemoryOperationStore
import com.flabs.cobrate.core.data.InvitacionRepository
import com.flabs.cobrate.core.data.InvitacionStore
import com.flabs.cobrate.core.data.OperacionRepository
import com.flabs.cobrate.core.data.RelacionCuentaNegocioRepository
import com.flabs.cobrate.core.data.RelacionCuentaNegocioStore
import com.flabs.cobrate.core.data.MetodoDeCobroStore
import com.flabs.cobrate.core.logic.DailySummary
import com.flabs.cobrate.core.logic.FeedbackManager
import com.flabs.cobrate.core.logic.LogSeguro
import com.flabs.cobrate.core.logic.SolicitudesIngreso
import com.flabs.cobrate.core.logic.conReintentoPorReloj
import com.flabs.cobrate.core.model.EstadoInvitacion
import com.flabs.cobrate.core.model.EstadoRelacion
import com.flabs.cobrate.core.model.InvitacionTrabajador
import com.flabs.cobrate.core.model.MetodoDeCobro
import com.flabs.cobrate.core.model.MiembroEquipo
import com.flabs.cobrate.core.model.Negocio
import com.flabs.cobrate.core.model.Operacion
import com.flabs.cobrate.core.model.RelacionCuentaNegocio
import com.flabs.cobrate.core.model.RolCuentaNegocio
import com.flabs.cobrate.core.model.TipoMetodoCobro
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Collections

/**
 * Estado de la aplicación para el lado del Dueño.
 */
class BusinessViewModel : ViewModel() {

    private val relacionRepo = RelacionCuentaNegocioRepository()
    private val cuentaRepo = CuentaRepository()
    private val invitacionRepo = InvitacionRepository()

    // ---- Códigos de invitación activos y Solicitudes (CAPA 45) ----
    private val _codigosActivos = MutableStateFlow<List<InvitacionTrabajador>>(emptyList())
    val codigosActivos: StateFlow<List<InvitacionTrabajador>> = _codigosActivos.asStateFlow()

    private val mapaCodigosNombres = Collections.synchronizedMap(mutableMapOf<String, String>())

    // ---- Estados de Métodos de Cobro (C8) ----
    
    // Estado de progreso de subida de QR para la UI
    private val _uploadStage = MutableStateFlow<String?>(null)
    val uploadStage: StateFlow<String?> = _uploadStage.asStateFlow()

    // Caché de QRs descargados (puente al Store)
    val qrCache = MetodoDeCobroStore.qrCache

    // IDs de cuentas que están siendo descargadas actualmente para evitar peticiones duplicadas.
    private val idsEnVuelo = Collections.synchronizedSet(mutableSetOf<String>())

    // ID del negocio que este ViewModel debe mostrar.
    // Única fuente de verdad desde el contexto central (C9).
    val negocioActualId: StateFlow<String?> = NegocioStore.negocioActivoId

    private val _equipoVisible = MutableStateFlow<List<MiembroEquipo>>(emptyList())
    val equipoVisible: StateFlow<List<MiembroEquipo>> = _equipoVisible.asStateFlow()

    init {
        viewModelScope.launch {
            negocioActualId.collect { negId ->
                com.flabs.cobrate.features.planes.PlanStore.refrescar(negId)
                if (negId != null) {
                    cargarEquipoDelNegocio()
                    cargarEquipoVisible()
                    cargarCodigosActivos()
                }
            }
        }
        viewModelScope.launch {
            while (isActive) {
                delay(3000L)
                val actuales = _codigosActivos.value
                if (actuales.isNotEmpty()) {
                    val ahora = System.currentTimeMillis()
                    val vigentes = SolicitudesIngreso.codigosVigentes(actuales, ahora)
                    if (vigentes.size != actuales.size) {
                        _codigosActivos.value = vigentes
                    }
                }
            }
        }
    }

    /** Solicitudes de ingreso pendientes (CAPA 45) */
    val solicitudes: StateFlow<List<SolicitudUi>> = combine(
        negocioActualId,
        RelacionCuentaNegocioStore.relaciones,
        CuentaStore.cuentas
    ) { negocioId, todasLasRelaciones, cuentasCheadas ->
        if (negocioId == null) return@combine emptyList()

        val pendientes = SolicitudesIngreso.pendientes(todasLasRelaciones, negocioId)

        val idsFaltantes = pendientes.map { it.cuentaId }.filter { id -> cuentasCheadas.none { it.id == id } }
        if (idsFaltantes.isNotEmpty()) {
            cargarCuentasAgrupadas(idsFaltantes)
        }

        pendientes.map { rel ->
            val codigo = rel.codigoUsado
            if (codigo != null && !mapaCodigosNombres.containsKey(codigo)) {
                cargarNombrePropuestoAsync(codigo)
            }

            val nombrePropuesto = codigo?.let { mapaCodigosNombres[it] }
            val cuenta = cuentasCheadas.find { it.id == rel.cuentaId }
            val nombreFinal = when {
                !nombrePropuesto.isNullOrBlank() -> nombrePropuesto
                !cuenta?.username.isNullOrBlank() -> cuenta.username
                else -> "Trabajador nuevo"
            }

            SolicitudUi(
                cuentaId = rel.cuentaId,
                nombre = nombreFinal,
                codigoUsado = rel.codigoUsado,
                fechaMs = rel.fechaVinculacion
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val cantidadSolicitudes: StateFlow<Int> = solicitudes
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    private fun cargarNombrePropuestoAsync(codigo: String) {
        viewModelScope.launch {
            invitacionRepo.buscarPorCodigo(codigo).onSuccess { inv ->
                val nombre = inv?.nombrePropuesto
                if (!nombre.isNullOrBlank()) {
                    mapaCodigosNombres[codigo] = nombre
                }
            }
        }
    }

    // Operaciones del negocio activo (C9 - Aislamiento).
    // Free ve los últimos 30 días; Pro, todo el historial.
    val operaciones: StateFlow<List<Operacion>> = combine(
        InMemoryOperationStore.operaciones,
        negocioActualId,
        com.flabs.cobrate.features.planes.PlanStore.estado
    ) { allOps, negId, plan ->
        val desde = com.flabs.cobrate.features.planes.PlanLogica.desdeHistorial(plan?.plan, System.currentTimeMillis())
        if (negId == null) emptyList() else allOps.filter { it.negocioId == negId && it.fechaHora >= desde }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Resumen del día calculado a partir de las operaciones del negocio activo (C9).
    val resumenDelDia: StateFlow<ResumenDelDia> = operaciones
        .map { ops ->
            ResumenDelDia(
                totalDelDia = DailySummary.totalDelDia(ops),
                numeroDeOperaciones = DailySummary.numeroDeOperaciones(ops),
                operacionesPendientes = DailySummary.operacionesPendientes(ops),
                promedioDelDia = DailySummary.promedioDelDia(ops),
                pagosRecibidos = DailySummary.pagosRecibidos(ops)
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ResumenDelDia(
                totalDelDia = 0.0,
                numeroDeOperaciones = 0,
                operacionesPendientes = 0,
                promedioDelDia = 0.0,
                pagosRecibidos = 0
            )
        )

    /**
     * Lista de trabajadores reales del negocio actual (vinculados por invitación).
     * CAPA 9: migración a RelacionCuentaNegocio real con soporte para vista de trabajador (RPC).
     */
    val trabajadoresReales: StateFlow<List<TrabajadorRealInfo>> = combine(
        negocioActualId,
        RelacionCuentaNegocioStore.relaciones,
        CuentaStore.cuentas,
        _equipoVisible
    ) { negocioId, todasLasRelaciones, cuentasCheadas, equipoVisible ->
        if (negocioId == null) return@combine emptyList()

        val relacionesTrabajador = todasLasRelaciones.filter {
            it.negocioId == negocioId &&
            it.rol == RolCuentaNegocio.TRABAJADOR &&
            com.flabs.cobrate.features.equipo.FiltroEquipo.apareceEnEquipo(it.estado)
        }

        val listaTrabajadores = if (relacionesTrabajador.isNotEmpty()) {
            relacionesTrabajador.map { rel ->
                val cuenta = cuentasCheadas.find { it.id == rel.cuentaId }
                TrabajadorRealInfo(
                    cuentaId = rel.cuentaId,
                    username = cuenta?.username ?: "Cargando...",
                    estado = rel.estado
                )
            }
        } else {
            equipoVisible.filter { it.rol == RolCuentaNegocio.TRABAJADOR }.map { m ->
                val cuenta = cuentasCheadas.find { it.id == m.cuentaId }
                TrabajadorRealInfo(
                    cuentaId = m.cuentaId,
                    username = cuenta?.username ?: m.username,
                    estado = m.estado
                )
            }
        }

        // Identificar cuentas faltantes para cargar nombres
        val idsFaltantes = listaTrabajadores
            .map { it.cuentaId }
            .filter { id -> cuentasCheadas.none { it.id == id } }

        if (idsFaltantes.isNotEmpty()) {
            cargarCuentasAgrupadas(idsFaltantes)
        }

        listaTrabajadores
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /**
     * Carga las relaciones completas de todo el negocio (Dueño/Admin) o el equipo visible (Trabajador)
     * y refresca los nombres de las cuentas en tiempo real desde Supabase.
     */
    fun cargarEquipoDelNegocio() {
        val negocioId = negocioActualId.value ?: return
        val cuentaId = CuentaStore.sesionActual.value?.id ?: return

        val negocio = NegocioStore.negocios.value.find { it.id == negocioId }
        val relaciones = RelacionCuentaNegocioStore.relaciones.value
        val esDueno = negocio?.dueñoCuentaId == cuentaId
        val esAdminActivo = relaciones.any {
            it.cuentaId == cuentaId && it.negocioId == negocioId &&
                    it.rol == RolCuentaNegocio.ADMINISTRADOR && it.estado == EstadoRelacion.ACTIVA
        }

        viewModelScope.launch {
            if (esDueno || esAdminActivo) {
                conReintentoPorReloj {
                    relacionRepo.relacionesDelNegocio(negocioId)
                }.onSuccess { resultado ->
                    RelacionCuentaNegocioStore.sincronizarNegocio(negocioId, resultado)
                    val uids = (resultado.map { it.cuentaId } + listOfNotNull(negocio?.dueñoCuentaId)).distinct()
                    if (uids.isNotEmpty()) {
                        cuentaRepo.obtenerVariosPorId(uids).onSuccess { listaCuentas ->
                            listaCuentas.forEach { CuentaStore.upsert(it) }
                        }
                    }
                }.onFailure { err ->
                    LogSeguro.w("TokeEquipo", "Error al cargar relaciones del negocio", err)
                }
            } else {
                cargarEquipoVisible()
            }
        }
    }

    /**
     * Carga el equipo del negocio llamando a la RPC equipo_de_negocio (para Trabajador)
     * y refresca los nombres de cuentas en CuentaStore.
     */
    fun cargarEquipoVisible() {
        val negocioId = negocioActualId.value ?: return
        val negocio = NegocioStore.negocios.value.find { it.id == negocioId }
        viewModelScope.launch {
            conReintentoPorReloj {
                relacionRepo.equipoDeNegocio(negocioId)
            }.onSuccess { miembros ->
                _equipoVisible.value = miembros
                val uids = (miembros.map { it.cuentaId } + listOfNotNull(negocio?.dueñoCuentaId)).distinct()
                if (uids.isNotEmpty()) {
                    cuentaRepo.obtenerVariosPorId(uids).onSuccess { listaCuentas ->
                        listaCuentas.forEach { CuentaStore.upsert(it) }
                    }
                }
            }.onFailure { err ->
                LogSeguro.w("TokeEquipo", "Error al cargar equipo visible", err)
            }
        }
    }

    private fun cargarCuentasAgrupadas(ids: List<String>) {
        val realmenteFaltantes = ids.filter { it !in idsEnVuelo }
        if (realmenteFaltantes.isEmpty()) return

        idsEnVuelo.addAll(realmenteFaltantes)
        viewModelScope.launch {
            cuentaRepo.obtenerVariosPorId(realmenteFaltantes)
                .onSuccess { cuentas ->
                    cuentas.forEach { CuentaStore.upsert(it) }
                }
                .onFailure {
                    idsEnVuelo.removeAll(realmenteFaltantes.toSet())
                }
        }
    }

    /** Cambia el estado de un trabajador (vínculo con el negocio). */
    fun cambiarEstadoTrabajador(cuentaId: String, nuevoEstado: EstadoRelacion) {
        val negocioId = negocioActualId.value ?: return
        viewModelScope.launch {
            FeedbackManager.setLoading(true, "Actualizando trabajador...")
            relacionRepo.actualizarEstado(cuentaId, negocioId, nuevoEstado).onSuccess {
                // Actualizamos el Store local (todo o nada).
                val relacionesActuales = RelacionCuentaNegocioStore.relaciones.value
                val nuevasRelaciones = relacionesActuales.map {
                    if (it.cuentaId == cuentaId && it.negocioId == negocioId) {
                        it.copy(estado = nuevoEstado)
                    } else it
                }
                RelacionCuentaNegocioStore.sincronizarNegocio(negocioId, nuevasRelaciones.filter { it.negocioId == negocioId })
                cargarEquipoDelNegocio()
                FeedbackManager.setLoading(false)
                FeedbackManager.showSuccess("Trabajador actualizado correctamente.")
            }.onFailure {
                FeedbackManager.setLoading(false)
                FeedbackManager.showError(it)
            }
        }
    }

    // ---- Operaciones de Métodos de Cobro (C8) ----

    fun cargarMetodos() {
        val id = negocioActualId.value ?: return
        viewModelScope.launch {
            MetodoDeCobroStore.cargarPorNegocio(id)
        }
    }

    private fun estaMetodoActivoEnOtroNegocio(tipo: TipoMetodoCobro, negocioIdActual: String): Boolean {
        val uid = CuentaStore.sesionActual.value?.id ?: return false
        val misNegocios = NegocioStore.negocios.value.filter { it.dueñoCuentaId == uid && it.id != negocioIdActual }
        val metodosMap = MetodoDeCobroStore.metodosPorNegocio.value
        return misNegocios.any { negocio ->
            val listaMetodos = metodosMap[negocio.id] ?: emptyList()
            listaMetodos.any { it.tipo == tipo && it.activo }
        }
    }

    fun crearMetodo(tipo: TipoMetodoCobro, dato: String, nombre: String) {
        if (!com.flabs.cobrate.features.cobros.NuevoMetodoLogica.esTipoPermitidoParaCrear(tipo)) {
            FeedbackManager.showErrorMessage("Por el momento solo se permite vincular Yape")
            return
        }
        val id = negocioActualId.value ?: return
        if (estaMetodoActivoEnOtroNegocio(tipo, id)) {
            FeedbackManager.showErrorMessage("Esta billetera ya está activa en otro negocio tuyo")
            return
        }
        val plan = com.flabs.cobrate.features.planes.PlanStore.plan
        val esPro = plan == com.flabs.cobrate.features.planes.Plan.PRO
        val metodosActuales = MetodoDeCobroStore.obtenerPorNegocio(id).size
        if (!esPro && metodosActuales >= com.flabs.cobrate.features.planes.PlanLogica.MAX_METODOS_FREE) {
            FeedbackManager.showErrorMessage("El plan Free solo permite 1 método de cobro. Pásate a Pro para vincular más.")
            return
        }
        viewModelScope.launch {
            FeedbackManager.setLoading(true, "Creando método...")
            MetodoDeCobroStore.crear(id, tipo, dato, nombre).onSuccess {
                FeedbackManager.setLoading(false)
                FeedbackManager.showSuccess("Método creado.")
            }.onFailure {
                FeedbackManager.setLoading(false)
                FeedbackManager.showError(it)
            }
        }
    }

    fun actualizarMetodo(id: String, dato: String? = null, nombre: String? = null, activo: Boolean? = null, oculto: Boolean? = null) {
        val negId = negocioActualId.value ?: return
        val listaMetodos = MetodoDeCobroStore.obtenerPorNegocio(negId)
        val metodoExistente = listaMetodos.find { it.id == id }
        if (metodoExistente != null && metodoExistente.tipo != TipoMetodoCobro.YAPE) {
            FeedbackManager.showErrorMessage("Solo se permite modificar métodos de Yape en esta versión")
            return
        }
        if (activo == true && metodoExistente != null) {
            if (estaMetodoActivoEnOtroNegocio(metodoExistente.tipo, negId)) {
                FeedbackManager.showErrorMessage("Esta billetera ya está activa en otro negocio tuyo")
                return
            }
        }
        viewModelScope.launch {
            MetodoDeCobroStore.actualizar(id, negId, dato, nombre, null, activo, oculto)
        }
    }

    fun eliminarMetodo(id: String) {
        val negId = negocioActualId.value ?: return
        viewModelScope.launch {
            FeedbackManager.setLoading(true, "Eliminando...")
            MetodoDeCobroStore.eliminar(id, negId).onSuccess {
                FeedbackManager.setLoading(false)
            }.onFailure {
                FeedbackManager.setLoading(false)
                FeedbackManager.showError(it)
            }
        }
    }

    fun subirQr(metodoId: String, imageData: ByteArray) {
        val negId = negocioActualId.value ?: return
        viewModelScope.launch {
            _uploadStage.value = "PREPARANDO"
            val result = MetodoDeCobroStore.actualizarConQr(metodoId, negId, imageData) { stage ->
                _uploadStage.value = stage
            }
            
            result.onSuccess {
                FeedbackManager.showSuccess("QR actualizado correctamente")
            }.onFailure {
                FeedbackManager.showError(it)
            }
            
            _uploadStage.value = null
        }
    }

    fun obtenerImagenQr(path: String) {
        viewModelScope.launch {
            MetodoDeCobroStore.obtenerImagenQr(path)
        }
    }

    // ---- Métodos para Códigos y Solicitudes (CAPA 45) ----

    fun cargarCodigosActivos() {
        val negocioId = negocioActualId.value ?: return
        viewModelScope.launch {
            conReintentoPorReloj {
                invitacionRepo.codigosActivos(negocioId)
            }.onSuccess { lista ->
                val vigentes = SolicitudesIngreso.codigosVigentes(lista, System.currentTimeMillis())
                _codigosActivos.value = vigentes
            }.onFailure { err ->
                LogSeguro.w("TokeEquipo", "Error al cargar códigos activos", err)
            }
        }
    }

    fun responderSolicitud(cuentaId: String, aceptar: Boolean) {
        val negocioId = negocioActualId.value ?: return
        viewModelScope.launch {
            FeedbackManager.setLoading(true, if (aceptar) "Aceptando..." else "Rechazando...")
            relacionRepo.responderSolicitud(negocioId, cuentaId, aceptar).onSuccess { res ->
                cargarEquipoDelNegocio()
                cargarCodigosActivos()
                FeedbackManager.setLoading(false)
                FeedbackManager.showSuccess(res.mensaje)
            }.onFailure { err ->
                FeedbackManager.setLoading(false)
                FeedbackManager.showErrorMessage(com.flabs.cobrate.core.logic.ErrorMapper.amable(err, "Error al procesar la solicitud"))
            }
        }
    }

    fun revocarCodigo(codigo: String) {
        viewModelScope.launch {
            FeedbackManager.setLoading(true, "Revocando...")
            _codigosActivos.value = _codigosActivos.value.filter { it.codigo != codigo }
            invitacionRepo.revocar(codigo).onSuccess { res ->
                cargarEquipoDelNegocio()
                cargarCodigosActivos()
                FeedbackManager.setLoading(false)
                FeedbackManager.showSuccess(res.mensaje)
            }.onFailure { err ->
                cargarCodigosActivos()
                FeedbackManager.setLoading(false)
                FeedbackManager.showErrorMessage(com.flabs.cobrate.core.logic.ErrorMapper.amable(err, "Error al revocar el código"))
            }
        }
    }

    /**
     * Crea un código de invitación para el negocio indicado, actualizando inmediatamente
     * la lista de códigos activos y conciliando en segundo plano.
     */
    suspend fun crearInvitacion(negocioId: String, requiereAprobacion: Boolean = true): Result<InvitacionTrabajador> {
        val plan = com.flabs.cobrate.features.planes.PlanStore.plan
        val esPro = plan == com.flabs.cobrate.features.planes.Plan.PRO
        val codigosActivos = _codigosActivos.value.size
        val actuales = trabajadoresReales.value.size

        if (codigosActivos >= com.flabs.cobrate.features.planes.PlanLogica.MAX_CODIGOS_ACTIVOS_GLOBAL) {
            return Result.failure(Exception("Llegaste al límite máximo de 5 códigos activos a la vez. Espera a que uno expire, se use o sea revocado."))
        }

        if (!esPro) {
            if (actuales >= com.flabs.cobrate.features.planes.PlanLogica.MAX_TRABAJADORES_FREE) {
                return Result.failure(Exception("PLAN_REQUERIDO: Tu plan Free solo permite hospedar 1 trabajador. Pásate a Pro para invitar a más colaboradores."))
            }
            if (codigosActivos >= com.flabs.cobrate.features.planes.PlanLogica.MAX_CODIGOS_ACTIVOS_FREE) {
                return Result.failure(Exception("PLAN_REQUERIDO: Tu plan Free solo permite 1 código de invitación activo. Pásate a Pro para generar más."))
            }
        }

        val max = NegocioStore.maxTrabajadores(negocioId)
        if (!com.flabs.cobrate.features.negocios.NegocioEdicionLogica.puedeInvitar(max, actuales)) {
            return Result.failure(Exception("Llegaste al máximo de $max trabajadores de tu negocio."))
        }
        val result = InvitacionStore.crear(negocioId, requiereAprobacion)
        if (result.isSuccess) {
            val creada = result.getOrThrow()
            val actual = _codigosActivos.value.toMutableList()
            if (actual.none { it.codigo == creada.codigo }) {
                actual.add(creada)
            }
            _codigosActivos.value = SolicitudesIngreso.codigosVigentes(actual, System.currentTimeMillis())
                .sortedBy { it.expiracion }
            cargarCodigosActivos()
            return Result.success(creada)
        }
        return result
    }

    /** Refresco coordinado de la pestaña Equipo (Miembros, Relaciones, Códigos, Solicitudes). */
    suspend fun refrescarEquipo(): Result<Unit> {
        val negocioId = negocioActualId.value ?: return Result.success(Unit)

        val resRelaciones = runCatching { relacionRepo.relacionesDelNegocio(negocioId) }.getOrNull()
        resRelaciones?.onSuccess { RelacionCuentaNegocioStore.sincronizarNegocio(negocioId, it) }

        val resCodigos = runCatching { invitacionRepo.codigosActivos(negocioId) }.getOrNull()
        resCodigos?.onSuccess { lista ->
            val vigentes = SolicitudesIngreso.codigosVigentes(lista, System.currentTimeMillis())
                .distinctBy { it.codigo }
                .sortedBy { it.expiracion }
            _codigosActivos.value = vigentes
        }

        val resMiembros = runCatching { relacionRepo.equipoDeNegocio(negocioId) }.getOrNull()
        resMiembros?.onSuccess { _equipoVisible.value = it }

        return Result.success(Unit)
    }

    /** Refresco coordinado de la pestaña Mis Cobros. */
    suspend fun refrescarMisCobros(): Result<Unit> {
        val negocioId = negocioActualId.value ?: return Result.success(Unit)
        val operacionRepo = OperacionRepository()
        val resOps = operacionRepo.obtenerDeVariosNegocios(listOf(negocioId))
        if (resOps.isSuccess) {
            val ops = resOps.getOrThrow()
            InMemoryOperationStore.sincronizar(negocioId, ops)
            return Result.success(Unit)
        }
        return Result.failure(resOps.exceptionOrNull() ?: Exception("Error al refrescar cobros"))
    }
}

/** Información de una solicitud de ingreso (CAPA 45). */
data class SolicitudUi(
    val cuentaId: String,
    val nombre: String,
    val codigoUsado: String?,
    val fechaMs: Long
)

/** Información de un trabajador real vinculada a una cuenta. */
data class TrabajadorRealInfo(
    val cuentaId: String,
    val username: String,
    val estado: EstadoRelacion
)

/** Resumen del día expuesto a la UI. */
data class ResumenDelDia(
    val totalDelDia: Double,
    val numeroDeOperaciones: Int,
    val operacionesPendientes: Int,
    val promedioDelDia: Double,
    val pagosRecibidos: Int = 0
)
