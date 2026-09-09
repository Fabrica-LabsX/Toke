package com.flabs.cobrate.services

import android.app.Notification
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
import com.flabs.cobrate.core.logic.AndroidNotifier
import com.flabs.cobrate.core.logic.AppsDePagoConfiables
import com.flabs.cobrate.core.logic.AssignmentEngine
import com.flabs.cobrate.core.logic.CoincidenciaVaAPagar
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
import com.flabs.cobrate.core.model.CambioDeEstado
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
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

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // DIAGNÓSTICO (temporal): logging instrumentado para investigar las
    // reconexiones del NotificationListenerService observadas en Logcat
    // (múltiples "Listener conectado y stores inicializados" en pocos segundos
    // con distintos PID). Cada callback registra el PID del proceso y un
    // timestamp legible para correlacionar cuándo y en qué proceso ocurre cada
    // evento. NO es una corrección; se revisa en la fase de testing diferido.
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "[DIAG] onCreate() pid=${android.os.Process.myPid()} t=${marcaTiempo()}")
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "[DIAG] onListenerConnected() pid=${android.os.Process.myPid()} t=${marcaTiempo()}")
        // Inicializa los stores con el contexto del servicio (mismo patrón que
        // InMemoryOperationStore.init(context)).
        EventoDeNotificacionStore.init(this)
        AlertaStore.init(this)
        InMemoryOperationStore.init(this)
        ConfiguracionDeVozStore.init(this)
        Log.d(TAG, "Listener conectado y stores inicializados. pid=${android.os.Process.myPid()} t=${marcaTiempo()}")
    }

    override fun onListenerDisconnected() {
        Log.d(TAG, "[DIAG] onListenerDisconnected() pid=${android.os.Process.myPid()} t=${marcaTiempo()}")
        super.onListenerDisconnected()
    }

    override fun onDestroy() {
        Log.d(TAG, "[DIAG] onDestroy() pid=${android.os.Process.myPid()} t=${marcaTiempo()}")
        // Libera los recursos de notificación.
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

        // CAPA 6c-2: captura de extras enriquecidos. Reúne el contenido
        // adicional que algunas apps ponen SOLO en subtítulo, big text o líneas
        // múltiples (p. ej. el detalle completo o el código de seguridad de
        // Yape). Se guarda en [EventoDeNotificacion.datosAdicionales].
        val datosAdicionales = capturarExtras(sbn)

        val negocioId = resolverNegocioActual()
        if (negocioId == null) {
            Log.d(TAG, "Notificación de pago ignorada: no hay negocio activo resuelto en este dispositivo")
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
                    procesarPagoConfianzaAlta(evento, resultado.monto, paquete)
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
        paquete: String
    ) {
        val metodo = MapeoAppsMetodoDeCobro.metodoDeCobroPara(paquete)
        if (metodo == null) {
            // Caso sin mapeo (p. ej. Plin aún sin entrada): no se puede
            // determinar el método. NO se genera alerta ni se bloquea el flujo.
            Log.d(TAG, "Posible pago detectado: S/ $monto (confianza ALTA, método no determinable)")
            return
        }

        val activo = MetodoDeCobroStore.estaActivo(metodo)
        if (activo == true) {
            // Método configurado y activo: flujo normal.
            Log.d(TAG, "Posible pago detectado: S/ $monto (confianza ALTA)")
            // CAPA 11: antes de crear una operación nueva, se comprueba si ya
            // existe una "Va a pagar" pendiente (origen VaAPagar, estado
            // ASIGNADA) del MISMO negocio y con el MISMO monto. Si hay una
            // coincidencia inequívoca, el pago real se asocia a esa operación
            // existente (se actualiza) en lugar de crear una operación
            // duplicada con origen Detectada.
            val pendientesDelNegocio = InMemoryOperationStore.operaciones.value.filter {
                it.negocioId == evento.negocioId
            }
            val coincidencia = CoincidenciaVaAPagar.buscarCoincidencia(monto, pendientesDelNegocio)

            val operacionFinal: Operacion
            if (coincidencia != null) {
                // CAPA 11: coincidencia "Va a pagar" -> TOKE asocia el pago
                // real a la operación existente. Se conservan el mismo id y su
                // trabajadorAsignadoId original (si "Va a pagar" asignó un
                // trabajador al crearla, se conserva; si era null, se respeta
                // ese null — NO se inventa ninguna asignación aquí). NO pasa
                // por AssignmentEngine. Se registra el cambio en el historial
                // con OperationHistory.registrarCambio para dejar constancia y
                // resolver la operación de forma idéntica al resto del flujo.
                Log.d(TAG, "CAPA 11: 'Va a pagar' asociada a la detección (monto S/ $monto)")
                operacionFinal = OperationHistory.registrarCambio(
                    operacion = coincidencia,
                    nuevoEstado = EstadoOperacion.ASIGNADA,
                    timestamp = System.currentTimeMillis()
                )
                scope.launch {
                    InMemoryOperationStore.actualizar(operacionFinal)
                        .onSuccess { OperationNotifier.notificar(this@TokeNotificationListenerService, operacionFinal) }
                        .onFailure { Log.e(TAG, "Error actualizando coincidencia: ${it.message}") }
                }
            } else {
                // SIN coincidencia: pago inesperado, flujo normal de CAPA 7/8.
                // Crear una operación cuando el método de cobro esté activo.
                val operacion = Operacion(
                    id = UUID.randomUUID().toString(),
                    negocioId = evento.negocioId,
                    trabajadorAsignadoId = null,
                    monto = monto,
                    estado = EstadoOperacion.SIN_ASIGNAR,
                    origen = OrigenOperacion.Detectada(evento.id),
                    fechaHora = System.currentTimeMillis(),
                    historial = listOf(CambioDeEstado(System.currentTimeMillis(), EstadoOperacion.SIN_ASIGNAR))
                )

                // CAPA 8: asignación automática. Si el negocio tiene exactamente 1
                // trabajador activo (relación rol=TRABAJADOR, estado=ACTIVA), la
                // operación se asigna a ese único trabajador antes de guardarse.
                // Si hay 0 o 2+ trabajadores, se devuelve SIN_ASIGNAR (el caso 2+
                // es CAPA 9, fuera de alcance).
                val trabajadoresActivos = RelacionCuentaNegocioStore.relaciones
                    .value
                    .filter {
                        it.negocioId == evento.negocioId &&
                            it.rol == RolCuentaNegocio.TRABAJADOR &&
                            it.estado == EstadoRelacion.ACTIVA
                    }
                operacionFinal = AssignmentEngine.asignarSiCorresponde(operacion, trabajadoresActivos)
                scope.launch {
                    InMemoryOperationStore.agregar(operacionFinal)
                        .onSuccess { OperationNotifier.notificar(this@TokeNotificationListenerService, operacionFinal) }
                        .onFailure { Log.e(TAG, "Error creando operación detectada: ${it.message}") }
                }
            }
        } else {
            // Método NO activo (o no configurado): alerta discreta al Dueño.
            // El Trabajador jamás ve esto.
            val mensaje = "Se detectó un pago de S/ $monto por $metodo, un método " +
                "que no está activo en tu negocio. Revisa tu cuenta de $metodo " +
                "directamente."
            AlertaStore.crear(mensaje, evento.id)
            Log.d(TAG, "Alerta generada: pago por método no configurado ($metodo)")

            // CAPA 13: notificación interna de alerta al Dueño (CAPA 6c-3).
            val contenido = NotificacionesInternas.notificacionAlertaDueño(mensaje)
            AndroidNotifier.mostrar(this, contenido.titulo, contenido.texto, NOTIF_ALERTA_DUENO)
            Log.d(TAG, "Notificación interna: alerta al Dueño")
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
     * administrador con sesión activa.
     */
    private fun resolverNegocioActual(): String? {
        val cuentaId = CuentaStore.sesionActual.value?.id ?: return null
        return RelacionCuentaNegocioStore.relaciones.value.find {
            it.cuentaId == cuentaId &&
                it.rol == RolCuentaNegocio.ADMINISTRADOR &&
                it.estado == EstadoRelacion.ACTIVA
        }?.negocioId
    }

    companion object {
        private const val TAG = "TokeListener"

        // Ids de notificaciones internas (CAPA 13). Constantes fijas para que
        // cada tipo reemplace la notificación previa del mismo tipo, evitando
        // acumular cientos en la bandeja.
        private const val NOTIF_ALERTA_DUENO = 1003
    }
}
