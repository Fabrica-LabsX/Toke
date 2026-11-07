package com.flabs.cobrate.services

import android.app.Notification
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.os.Process
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.flabs.cobrate.core.data.AlertaStore
import com.flabs.cobrate.core.data.ConfiguracionDeVozStore
import com.flabs.cobrate.core.data.CuentaStore
import com.flabs.cobrate.core.data.EventoDeNotificacionStore
import com.flabs.cobrate.core.data.MetodoDeCobroStore
import com.flabs.cobrate.core.model.EventoDeNotificacion
import java.util.UUID
import com.flabs.cobrate.core.data.InMemoryOperationStore
import com.flabs.cobrate.core.data.RelacionCuentaNegocioStore
import com.flabs.cobrate.core.data.NegocioStore
import com.flabs.cobrate.core.data.InvitacionStore
import com.flabs.cobrate.core.logic.AndroidNotifier
import com.flabs.cobrate.core.logic.AppsDePagoConfiables
import com.flabs.cobrate.core.logic.AssignmentEngine
import com.flabs.cobrate.core.logic.MapeoAppsMetodoDeCobro
import com.flabs.cobrate.core.logic.NivelConfianzaParseo
import com.flabs.cobrate.core.logic.NotificationParser
import com.flabs.cobrate.core.logic.NotificacionesInternas
import com.flabs.cobrate.core.logic.OperationHistory
import com.flabs.cobrate.core.logic.OperationNotifier
import com.flabs.cobrate.core.logic.ResultadoParseo
import com.flabs.cobrate.core.model.EstadoRelacion
import com.flabs.cobrate.core.model.Operacion
import com.flabs.cobrate.core.model.RolCuentaNegocio
import com.flabs.cobrate.core.model.EstadoOperacion
import com.flabs.cobrate.core.model.OrigenOperacion
import com.flabs.cobrate.core.data.EstadoServiciosStore
import com.flabs.cobrate.core.data.EstadoOperativoStore
import com.flabs.cobrate.core.logic.LogSeguro
import com.flabs.cobrate.core.model.MetodoDeCobro
import com.flabs.cobrate.core.model.RelacionCuentaNegocio
import com.flabs.cobrate.core.model.CambioDeEstado
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * NotificationListenerService de TOKE (CAPA 6b).
 *
 * CAPA 6a declaró el servicio; esta capa añade la captura y persistencia del
 * evento crudo: extrae el paquete de origen y el texto legible de cada
 * notificación entrante (sin interpretar su significado) y lo guarda en
 * [EventoDeNotificacionStore] (memoria + persistencia local).
 *
 * NO se hace parsing de montos, NO se generan operaciones todavía — eso llega
 * en capas posteriores (6c+).
 */
class TokeNotificationListenerService : NotificationListenerService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    @Volatile private var isListenerConnected = false
    private var sessionMonitorJob: Job? = null
    private var operationalMonitorJob: Job? = null
    private var sessionJob: Job? = null
    private var currentSessionUid: String? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "[DIAG] onCreate() pid=${Process.myPid()} t=${marcaTiempo()}")
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "[DIAG] onListenerConnected() pid=${Process.myPid()} t=${marcaTiempo()}")
        
        // Cancelar monitores y trabajos anteriores si existían (idempotencia en reconexiones)
        sessionMonitorJob?.cancel()
        operationalMonitorJob?.cancel()
        sessionJob?.cancel()

        isListenerConnected = true
        instance = this
        sessionJob = SupervisorJob()

        EstadoOperativoStore.init(this)
        CuentaStore.init(this)
        NegocioStore.init(this)
        RelacionCuentaNegocioStore.init(this)
        InvitacionStore.init(this)
        
        EventoDeNotificacionStore.init(this)
        AlertaStore.init(this)
        InMemoryOperationStore.init(this)
        ConfiguracionDeVozStore.init(this)
        MetodoDeCobroStore.init(this)
        EstadoServiciosStore.init(this)
        
        iniciarMonitoreoSesion()
        
        Log.d(TAG, "Listener conectado y stores inicializados. pid=${Process.myPid()} t=${marcaTiempo()}")
    }

    /**
     * Observa los cambios de sesión en CuentaStore para invalidar el trabajo del servicio
     * si el usuario cambia (aislamiento A -> B).
     */
    private fun iniciarMonitoreoSesion() {
        sessionMonitorJob?.cancel()
        sessionMonitorJob = serviceScope.launch {
            CuentaStore.sesionActual.collect { cuenta ->
                val nuevoUid = cuenta?.id
                if (nuevoUid != currentSessionUid) {
                    Log.d(TAG, "[SESSION] Cambio de sesión detectado: $currentSessionUid -> $nuevoUid. Invalidando contexto previo.")
                    sessionJob?.cancel()
                    sessionJob = SupervisorJob()
                    currentSessionUid = nuevoUid
                }
            }
        }
    }

    override fun onListenerDisconnected() {
        Log.d(TAG, "[DIAG] onListenerDisconnected() pid=${Process.myPid()} t=${marcaTiempo()}")
        isListenerConnected = false
        instance = null
        sessionMonitorJob?.cancel()
        sessionMonitorJob = null
        operationalMonitorJob?.cancel()
        operationalMonitorJob = null
        sessionJob?.cancel()
        sessionJob = null
        super.onListenerDisconnected()
    }

    override fun onDestroy() {
        Log.d(TAG, "[DIAG] onDestroy() pid=${android.os.Process.myPid()} t=${marcaTiempo()}")
        isListenerConnected = false
        instance = null
        sessionMonitorJob?.cancel()
        sessionMonitorJob = null
        operationalMonitorJob?.cancel()
        operationalMonitorJob = null
        sessionJob?.cancel()
        sessionJob = null
        serviceScope.cancel()
        OperationNotifier.liberar()
        super.onDestroy()
    }

    // Helper temporal de diagnóstico: timestamp legible (hora:minuto:segundo)
    // para correlacionar los eventos del servicio.
    private fun marcaTiempo(): String {
        val formato = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US)
        return formato.format(java.util.Date())
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val paquete = sbn.packageName

        // REGLA DE SEGURIDAD (CAPA 6c-2), se evalúa ANTES que cualquier otra
        // cosa, incluso antes del filtro de ruido: el paqueteOrigen no puede
        // ser falsificado por otra app; esta es la defensa real contra
        // mensajes de pago falsos vía SMS/WhatsApp/Telegram/shell/etc. Si la
        // notificación NO viene de un paquete de la whitelist, se descarta por
        // completo sin guardarla (ni siquiera como evento crudo), sin importar
        // lo que diga el texto.
        if (!AppsDePagoConfiables.esFuenteConfiables(paquete)) {
            // NOTA de depuración: este log NO debe interpretarse como "posible
            // pago perdido"; es tráfico ajeno al sistema de pagos.
            Log.d(TAG, "Ignorado (fuente no confiable): $paquete")
            return
        }

        // Filtro base de ruido (CAPA 6b): ignorar nuestras propias
        // notificaciones y las que no tienen contenido legible (título o
        // texto).
        if (paquete == applicationContext.packageName) {
            Log.d(TAG, "Notificación propia ignorada ($paquete)")
            return
        }

        val extras = sbn.notification.extras
        val titulo = extras
            .getCharSequence(Notification.EXTRA_TITLE)
            ?.toString()
            ?.trim()
            .orEmpty()
        val texto = extras
            .getCharSequence(Notification.EXTRA_TEXT)
            ?.toString()
            ?.trim()
            .orEmpty()

        if (titulo.isEmpty() && texto.isEmpty()) {
            Log.d(TAG, "Notificación sin contenido legible ignorada ($paquete)")
            return
        }

        // CAPA 6c-2: captura de extras enriquecidos.
        val datosAdicionales = capturarExtras(sbn)

        // CAPA 3-FIX: Captura de contexto de identidad para aislamiento.
        val sesionAlInicio = currentSessionUid
        val negocioId = resolverNegocioActual()

        if (sesionAlInicio == null || negocioId == null) {
            Log.d(TAG, "Notificación ignorada: no hay sesión activa o negocio resuelto")
            return
        }

        // CAPA 58: REGLA DE APAGADO GLOBAL
        if (!EstadoOperativoStore.encendido.value) {
            Log.d(TAG, "Notificación ignorada: TOKE está apagado a nivel global")
            return
        }

        // C11: REGLA DE APAGADO. Si los servicios están desactivados para este negocio,
        // ignoramos el procesamiento por completo.
        val activadoServicios = EstadoServiciosStore.estadoDe(negocioId).activado
        if (!activadoServicios) {
            Log.d(TAG, "SERVICIOS desactivado para negocio=$negocioId. Notificación ignorada.")
            return
        }

        val evento = EventoDeNotificacion(
            id = UUID.randomUUID().toString(),
            negocioId = negocioId,
            paqueteOrigen = paquete,
            titulo = titulo,
            texto = texto,
            datosAdicionales = datosAdicionales,
            fechaHora = sbn.postTime,
            procesado = false
        )

        EventoDeNotificacionStore.agregar(evento)
        Log.d(TAG, "Evento capturado de: $paquete -> \"$titulo: $texto\"")

        // CAPA 6c: aplica el parser sobre el evento recién capturado y marca el
        // evento como procesado según el resultado. No se crea ninguna
        // operación todavía.
        when (val resultado = NotificationParser.parsear(titulo, texto, paquete, datosAdicionales)) {
            is ResultadoParseo.MontoDetectado -> {
                if (resultado.confianza == NivelConfianzaParseo.ALTA) {
                    // CAPA 6c-3: pago real (confianza ALTA) desde método
                    // configurado o no.
                    procesarPagoConfianzaAlta(evento, resultado.monto, paquete, sesionAlInicio)
                } else {
                    Log.d(TAG, "Posible pago detectado: S/ ${resultado.monto} (confianza MEDIA)")
                }
            }
            ResultadoParseo.NoEsPago -> {
                Log.d(TAG, "Descartado como ruido ($paquete)")
            }
        }
        EventoDeNotificacionStore.marcarProcesado(evento.id)
    }

    /**
     * CAPA 6c-3: maneja un pago detectado con confianza ALTA desde un paquete
     * confiable. Si el método de cobro asociado está activo en el negocio,
     * continúa el flujo normal; si NO está activo, genera una alerta discreta
     * SOLO para el Dueño (el Trabajador no debe ver nada).
     */
    private fun procesarPagoConfianzaAlta(
        evento: EventoDeNotificacion,
        monto: Double,
        paquete: String,
        sesionAlInicio: String
    ) {
        val uid = CuentaStore.sesionActual.value?.id ?: return
        val todosMisNegocios = NegocioStore.negocios.value.filter { it.dueñoCuentaId == uid }

        // REGLA ABSOLUTA (Sección 4.1): El listener solo continúa si el usuario actual es el DUEÑO.
        if (todosMisNegocios.isEmpty()) {
            Log.d(TAG, "Ignorado en procesarPagoConfianzaAlta: usuario $uid no es Dueño de ningún negocio.")
            return
        }

        val tipoMetodo = MapeoAppsMetodoDeCobro.metodoDeCobroPara(paquete)
        if (tipoMetodo == null) {
            Log.d(TAG, "Posible pago detectado: S/ $monto (confianza ALTA, método no determinable)")
            return
        }

        // Resolución del negocio (Sección 4.4 - Regla A/B):
        // Buscar negocios del dueño con método tipo == tipoMetodo, activo == true, oculto == false.
        val negociosConMetodoActivo = todosMisNegocios.mapNotNull { neg ->
            val metodos = MetodoDeCobroStore.obtenerPorNegocio(neg.id)
            val m = metodos.find { it.tipo == tipoMetodo && it.activo && !it.oculto }
            if (m != null) neg to m else null
        }

        val (negocioIdResuelto, metodoCobroRealId) = when {
            negociosConMetodoActivo.size == 1 -> {
                // Regla A: Exactamente un negocio del dueño tiene la billetera activa
                val (neg, m) = negociosConMetodoActivo.first()
                neg.id to m.id
            }
            negociosConMetodoActivo.isEmpty() -> {
                // Regla B: Ninguno la tiene activa -> va al negocio seleccionado en la app sin método visible
                val seleccionadoId = NegocioStore.negocioActivoId.value ?: todosMisNegocios.first().id
                seleccionadoId to null
            }
            else -> {
                // Violación de billetera única: más de uno activo.
                LogSeguro.e(TAG, "Error: el método $tipoMetodo está activo en múltiples negocios del dueño $uid", null)
                return
            }
        }

        val operacion = Operacion(
            id = UUID.randomUUID().toString(),
            negocioId = negocioIdResuelto,
            trabajadorAsignadoId = null,
            metodoPago = tipoMetodo,
            metodoCobroId = metodoCobroRealId,
            monto = monto,
            estado = EstadoOperacion.SIN_ASIGNAR,
            origen = OrigenOperacion.Detectada(evento.id),
            fechaHora = System.currentTimeMillis(),
            historial = listOf(CambioDeEstado(System.currentTimeMillis(), EstadoOperacion.SIN_ASIGNAR)),
            pagador = com.flabs.cobrate.core.logic.PagadorExtractor.extraer(evento.titulo, evento.texto)
        )

        val trabajadoresActivos = RelacionCuentaNegocioStore.relaciones
            .value
            .filter {
                it.negocioId == negocioIdResuelto &&
                    it.rol == RolCuentaNegocio.TRABAJADOR &&
                    it.estado == EstadoRelacion.ACTIVA
            }
        val operacionFinal = AssignmentEngine.asignarSiCorresponde(operacion, trabajadoresActivos)

        val job = sessionJob ?: Job()
        serviceScope.launch(job) {
            if (currentSessionUid != sesionAlInicio) {
                Log.w(TAG, "[STALE] Abortando creación: cambio de sesión detectado ($sesionAlInicio -> $currentSessionUid)")
                return@launch
            }

            InMemoryOperationStore.agregar(operacionFinal)
                .onSuccess { OperationNotifier.notificar(this@TokeNotificationListenerService, operacionFinal) }
                .onFailure { LogSeguro.e(TAG, "Error creando operación detectada", it) }
        }

        // Si el método no estaba configurado o activo en el negocio (Regla B)
        if (metodoCobroRealId == null) {
            Log.d(TAG, "Notificando al Dueño: método $tipoMetodo no configurado/activo en negocio $negocioIdResuelto.")
            AndroidNotifier.mostrarMetodoNoConfigurado(
                context = this,
                tipo = tipoMetodo,
                existePeroInactivo = false
            )
            val mensaje = "Se detectó un pago por $tipoMetodo, pero no está configurado de forma activa."
            AlertaStore.crear(mensaje, evento.id, negocioIdResuelto, uid, tipoMetodo)
        }
    }

    private fun capturarExtras(sbn: StatusBarNotification): String? {
        val extras = sbn.notification.extras
        val partes = mutableListOf<String>()

        extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()?.trim()?.let {
            if (it.isNotEmpty()) partes.add(it)
        }
        extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()?.trim()?.let {
            if (it.isNotEmpty()) partes.add(it)
        }

        // EXTRA_TEXT_LINES es un array de CharSequence; se unen por salto de línea.
        @Suppress("DEPRECATION")
        val lineas = extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)
        if (lineas != null) {
            val textoLineas = lineas
                .filterNotNull()
                .joinToString("\n") { it.toString().trim() }
                .trim()
            if (textoLineas.isNotEmpty()) partes.add(textoLineas)
        }

        return partes.takeIf { it.isNotEmpty() }?.joinToString("\n")
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        Log.d(TAG, "Notificación eliminada de: ${sbn.packageName}")
    }

    /**
     * Resuelve el negocioId real desde la sesión del Dueño en este dispositivo.
     * La detección automática de pagos (CAPA 6b+) solo es válida si hay un
     * administrador o trabajador ACTIVO con sesión activa.
     */
    private fun resolverNegocioActual(): String? {
        val cuentaId = CuentaStore.sesionActual.value?.id ?: return null
        val negocioIdActivo = NegocioStore.negocioActivoId.value ?: return null

        val negocio = NegocioStore.buscarPorId(negocioIdActivo)
        if (negocio?.dueñoCuentaId == cuentaId) {
            return negocioIdActivo
        }

        val relacion = RelacionCuentaNegocioStore.relaciones.value.find {
            it.cuentaId == cuentaId && it.negocioId == negocioIdActivo
        }

        return if (relacion?.estado == EstadoRelacion.ACTIVA) {
            negocioIdActivo
        } else {
            null
        }
    }

    companion object {
        private const val TAG = "TokeListener"

        @Volatile
        private var instance: TokeNotificationListenerService? = null

        fun desvincular() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                instance?.requestUnbind()
            }
        }

        fun revincular(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                requestRebind(ComponentName(context, TokeNotificationListenerService::class.java))
            }
        }
    }
}
