package com.flabs.cobrate.services

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.flabs.cobrate.core.data.AlertaStore
import com.flabs.cobrate.core.data.EventoDeNotificacionStore
import com.flabs.cobrate.core.data.MetodoDeCobroStore
import com.flabs.cobrate.core.logic.AppsDePagoConfiables
import com.flabs.cobrate.core.logic.MapeoAppsMetodoDeCobro
import com.flabs.cobrate.core.logic.NivelConfianzaParseo
import com.flabs.cobrate.core.logic.NotificationParser
import com.flabs.cobrate.core.logic.ResultadoParseo
import com.flabs.cobrate.core.model.EventoDeNotificacion
import java.util.UUID
import com.flabs.cobrate.core.data.InMemoryOperationStore
import com.flabs.cobrate.core.data.RelacionCuentaNegocioStore
import com.flabs.cobrate.core.logic.AssignmentEngine
import com.flabs.cobrate.core.model.EstadoRelacion
import com.flabs.cobrate.core.model.Operacion
import com.flabs.cobrate.core.model.RolCuentaNegocio
import com.flabs.cobrate.core.model.EstadoOperacion
import com.flabs.cobrate.core.model.OrigenOperacion
import com.flabs.cobrate.core.model.CambioDeEstado

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

    override fun onListenerConnected() {
        super.onListenerConnected()
        // Inicializa los stores con el contexto del servicio (mismo patrón que
        // InMemoryOperationStore.init(context)).
        EventoDeNotificacionStore.init(this)
        AlertaStore.init(this)
        Log.d(TAG, "Listener conectado y stores inicializados.")
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

        // PLACEHOLDER CAPA 1: mismo negocio por defecto que WorkerViewModel.
        // TODO(CAPA posterior): resolver el negocio activo cuando exista
        // selección real de negocio en segundo plano.
        val evento = EventoDeNotificacion(
            id = UUID.randomUUID().toString(),
            negocioId = NEGOCIO_PLACEHOLDER,
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
            val operacionFinal = AssignmentEngine.asignarSiCorresponde(operacion, trabajadoresActivos)
            InMemoryOperationStore.agregar(operacionFinal)
        } else {
            // Método NO activo (o no configurado): alerta discreta al Dueño.
            // El Trabajador jamás ve esto.
            val mensaje = "Se detectó un pago de S/ $monto por $metodo, un método " +
                "que no está activo en tu negocio. Revisa tu cuenta de $metodo " +
                "directamente."
            AlertaStore.crear(mensaje, evento.id)
            Log.d(TAG, "Alerta generada: pago por método no configurado ($metodo)")
        }
    }

    /**
     * Reúne el contenido adicional de la notificación (subtítulo, big text y
     * líneas múltiples) y lo devuelve concatenado en un solo String, o null si
     * no hay ninguno. CAPA 6c-2.
     */
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

    companion object {
        private const val TAG = "TokeListener"

        // Mismo placeholder de negocio único que WorkerViewModel
        // ("NEGOCIO_DEFAULT"), pendiente de resolverse cuando exista selección
        // real de negocio activo en segundo plano.
        private const val NEGOCIO_PLACEHOLDER = "NEGOCIO_DEFAULT"
    }
}