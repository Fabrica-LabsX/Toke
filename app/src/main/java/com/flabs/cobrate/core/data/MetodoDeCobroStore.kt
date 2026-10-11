package com.flabs.cobrate.core.data

import android.content.Context
import com.flabs.cobrate.core.logic.QrImageUploader
import com.flabs.cobrate.core.model.MetodoDeCobro
import com.flabs.cobrate.core.model.TipoMetodoCobro
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Store de métodos de cobro con caché por negocio (singleton).
 * Patrón: Repository → LocalStore + MutableStateFlow → Realtime
 * IMPORTANTE: los métodos se indexan por negocioId internamente.
 */
object MetodoDeCobroStore {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Indexado por negocioId: Map<negocioId, List<MetodoDeCobro>>
    private val _metodosPorNegocio = MutableStateFlow<Map<String, List<MetodoDeCobro>>>(emptyMap())
    val metodosPorNegocio: StateFlow<Map<String, List<MetodoDeCobro>>> = _metodosPorNegocio

    // Caché de imágenes de QR descargadas (path -> bytes) para evitar descargas repetidas.
    private val _qrCache = MutableStateFlow<Map<String, ByteArray>>(emptyMap())
    val qrCache: StateFlow<Map<String, ByteArray>> = _qrCache.asStateFlow()

    private var localStore: MetodoDeCobroLocalStore? = null
    private val repository by lazy { MetodoDeCobroRepository() }

    fun init(context: Context) {
        val appContext = context.applicationContext
        if (localStore == null) {
            localStore = MetodoDeCobroLocalStore(appContext)
        }
    }

    /**
     * Carga los métodos de un negocio desde Supabase (fuente de verdad).
     * Si la consulta remota tiene éxito, reemplaza completamente la lista local y la persiste en DataStore.
     * Si falla (p. ej. sin conectividad), usa el DataStore local como fallback offline.
     */
    suspend fun cargarPorNegocio(negocioId: String) {
        val remoteResult = repository.obtenerPorNegocio(negocioId)
        if (remoteResult.isSuccess) {
            val remoteList = remoteResult.getOrDefault(emptyList())
            _metodosPorNegocio.value = _metodosPorNegocio.value.toMutableMap().apply {
                this[negocioId] = remoteList
            }
            persistir(negocioId)
        } else {
            val local = localStore?.leerPorNegocio(negocioId) ?: emptyList()
            if (local.isNotEmpty()) {
                _metodosPorNegocio.value = _metodosPorNegocio.value.toMutableMap().apply {
                    this[negocioId] = local.map { it.aModelo() }
                }
            }
        }
    }

    /**
     * Obtiene los métodos de un negocio desde la caché.
     */
    fun obtenerPorNegocio(negocioId: String): List<MetodoDeCobro> =
        _metodosPorNegocio.value[negocioId] ?: emptyList()

    /**
     * ¿El método está activo en este negocio?
     */
    fun estaActivo(negocioId: String, tipo: TipoMetodoCobro): Boolean? =
        obtenerPorNegocio(negocioId).find { it.tipo == tipo }?.activo

    /**
     * Crea un nuevo método en Supabase y lo añade a la caché.
     */
    suspend fun crear(negocioId: String, tipo: TipoMetodoCobro, datoDeCobro: String, nombreDestinatario: String): Result<MetodoDeCobro> {
        val result = repository.crear(negocioId, tipo, datoDeCobro, nombreDestinatario)
        if (result.isSuccess) {
            val nuevo = result.getOrThrow()
            val actualizados = obtenerPorNegocio(negocioId) + nuevo
            _metodosPorNegocio.value = _metodosPorNegocio.value.toMutableMap().apply {
                this[negocioId] = actualizados
            }
            persistir(negocioId)
            AlertaStore.resolverPorMetodo(negocioId, tipo)
            return Result.success(nuevo)
        }
        return result
    }

    /**
     * Actualiza un método en Supabase y la caché.
     */
    suspend fun actualizar(id: String, negocioId: String, datoDeCobro: String? = null, nombreDestinatario: String? = null, qrUrl: String? = null, activo: Boolean? = null, oculto: Boolean? = null): Result<MetodoDeCobro> {
        val result = repository.actualizar(id, datoDeCobro, nombreDestinatario, qrUrl, activo, oculto)
        if (result.isSuccess) {
            val actualizado = result.getOrThrow()
            val lista = obtenerPorNegocio(negocioId).map { if (it.id == id) actualizado else it }
            _metodosPorNegocio.value = _metodosPorNegocio.value.toMutableMap().apply {
                this[negocioId] = lista
            }
            persistir(negocioId)
            if (actualizado.activo) {
                AlertaStore.resolverPorMetodo(negocioId, actualizado.tipo)
            }
            return Result.success(actualizado)
        }
        return result
    }

    /**
     * Elimina un método de Supabase y la caché.
     */
    suspend fun eliminar(id: String, negocioId: String): Result<Unit> {
        val result = repository.eliminar(id)
        if (result.isSuccess) {
            val lista = obtenerPorNegocio(negocioId).filterNot { it.id == id }
            _metodosPorNegocio.value = _metodosPorNegocio.value.toMutableMap().apply {
                this[negocioId] = lista
            }
            persistir(negocioId)
            return Result.success(Unit)
        }
        return result
    }

    /**
     * Sincroniza toda la caché para un negocio (p. ej. tras reconexión o Realtime masivo).
     */
    fun sincronizar(negocioId: String, nuevosMetodos: List<MetodoDeCobro>) {
        _metodosPorNegocio.value = _metodosPorNegocio.value.toMutableMap().apply {
            this[negocioId] = nuevosMetodos
        }
        persistir(negocioId)
    }

    /**
     * Orquestación de subida de QR + Actualización de DB (CAPA 8).
     * Garantiza que la operación crítica ocurra fuera del ciclo de vida de la UI.
     */
    suspend fun actualizarConQr(
        metodoId: String,
        negocioId: String,
        imageData: ByteArray,
        onStageChange: (String) -> Unit = {}
    ): Result<Unit> {
        val metodo = obtenerPorNegocio(negocioId).find { it.id == metodoId }
            ?: return Result.failure(Exception("Método no encontrado"))

        return runCatching {
            // 1. Subir a Storage
            onStageChange("SUBIENDO")
            val newQrPath = QrImageUploader.uploadQr(
                negocioId = negocioId,
                tipo = metodo.tipo,
                imageData = imageData
            ).getOrThrow()

            // 2. Intentar limpiar el anterior (si existe)
            if (metodo.qrUrl != null) {
                QrImageUploader.tryDeleteOldQr(negocioId, metodo.qrUrl)
            }

            // 3. Actualizar DB
            onStageChange("GUARDANDO")
            val dbResult = actualizar(
                id = metodoId,
                negocioId = negocioId,
                qrUrl = newQrPath
            )
            
            if (dbResult.isFailure) {
                // COMPENSACIÓN: Si la DB falló, intentamos borrar el archivo que acabamos de subir (C8)
                QrImageUploader.tryDeleteOldQr(negocioId, newQrPath)
                throw dbResult.exceptionOrNull() ?: Exception("Error al actualizar base de datos")
            }

            // 4. Actualizar caché de imagen local para preview inmediata
            _qrCache.value = _qrCache.value.toMutableMap().apply {
                this[newQrPath] = imageData
            }
            
            onStageChange("LISTO")
        }
    }

    /**
     * Descarga de forma segura y autenticada un QR, manejando caché interna (C8).
     */
    suspend fun obtenerImagenQr(qrPath: String): ByteArray? {
        // 1. Check caché
        _qrCache.value[qrPath]?.let { return it }

        // 2. Descarga real
        return try {
            val storage = SupabaseClientProvider.supabase.storage.from("metodos-qr")
            val bytes = storage.downloadAuthenticated(qrPath)
            
            // Guardar en caché para futuras peticiones
            _qrCache.value = _qrCache.value.toMutableMap().apply {
                this[qrPath] = bytes
            }
            bytes
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Fuerza la descarga de métodos desde el servidor. 
     * Invocado tras eventos Realtime para asegurar consistencia.
     */
    suspend fun forzarRefrescoRemoto(negocioId: String) {
        val remote = repository.obtenerPorNegocio(negocioId).getOrNull() ?: return
        sincronizar(negocioId, remote)
    }

    /**
     * Inserta o actualiza un método en la caché local tras un evento Realtime (C6).
     * Evita el re-fetch completo si el payload es suficiente.
     */
    fun upsertRemoto(metodo: MetodoDeCobro) {
        val negocioId = metodo.negocioId
        val actual = _metodosPorNegocio.value[negocioId] ?: emptyList()
        val nuevaLista = if (actual.any { it.id == metodo.id }) {
            actual.map { if (it.id == metodo.id) metodo else it }
        } else {
            actual + metodo
        }
        _metodosPorNegocio.value = _metodosPorNegocio.value.toMutableMap().apply {
            this[negocioId] = nuevaLista
        }
        persistir(negocioId)
    }

    /**
     * Elimina un método de la caché local tras un evento Realtime (C6).
     */
    fun eliminarRemoto(metodoId: String) {
        val mapaActual = _metodosPorNegocio.value.toMutableMap()
        var cambio = false
        
        mapaActual.forEach { (negocioId, lista) ->
            if (lista.any { it.id == metodoId }) {
                mapaActual[negocioId] = lista.filterNot { it.id == metodoId }
                persistir(negocioId)
                cambio = true
            }
        }
        
        if (cambio) {
            _metodosPorNegocio.value = mapaActual
        }
    }

    /**
     * Limpia un negocio de la caché (logout, cambio de negocio).
     */
    fun limpiarNegocio(negocioId: String) {
        _metodosPorNegocio.value = _metodosPorNegocio.value.toMutableMap().apply {
            remove(negocioId)
        }
        scope.launch {
            localStore?.limpiarNegocio(negocioId)
        }
    }

    /**
     * Limpia toda la sesión (logout).
     */
    fun limpiar() {
        _metodosPorNegocio.value = emptyMap()
        _qrCache.value = emptyMap()
        scope.launch {
            localStore?.limpiarTodo()
        }
    }

    private fun persistir(negocioId: String) {
        val metodos = obtenerPorNegocio(negocioId)
        localStore?.let { store ->
            scope.launch {
                store.guardarPorNegocio(negocioId, metodos)
            }
        }
    }
}
