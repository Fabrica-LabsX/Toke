package com.flabs.cobrate.debug

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.flabs.cobrate.core.data.CuentaStore
import com.flabs.cobrate.core.data.InMemoryOperationStore
import com.flabs.cobrate.core.data.NegocioStore
import com.flabs.cobrate.core.data.RelacionCuentaNegocioStore
import com.flabs.cobrate.core.logic.AssignmentEngine
import com.flabs.cobrate.core.logic.MapeoAppsMetodoDeCobro
import com.flabs.cobrate.core.logic.NotificationParser
import com.flabs.cobrate.core.logic.OperationNotifier
import com.flabs.cobrate.core.logic.ResultadoParseo
import com.flabs.cobrate.core.model.CambioDeEstado
import com.flabs.cobrate.core.model.EstadoOperacion
import com.flabs.cobrate.core.model.EstadoRelacion
import com.flabs.cobrate.core.model.Operacion
import com.flabs.cobrate.core.model.OrigenOperacion
import com.flabs.cobrate.core.model.RolCuentaNegocio
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * SOLO DEBUG. Simula un pago entrante con las mismas piezas que usa el listener real, a partir del
 * último tramo: parsea el texto, arma la operación, la asigna, la guarda y notifica. No cambia la
 * detección: el filtro por paquete confiable del listener no se puede simular desde adb.
 *
 *   adb shell am broadcast -n com.flabs.cobrate/.debug.PagoDePruebaReceiver \
 *     -a com.flabs.cobrate.DEBUG_PAGO --es paquete com.bcp.innovacxion.yapeapp \
 *     --es titulo "Yape" --es texto "Carlos Perez te yapeo S/ 15.50"
 */
class PagoDePruebaReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val paquete = intent.getStringExtra("paquete") ?: "com.bcp.innovacxion.yapeapp"
        val titulo = intent.getStringExtra("titulo") ?: "Yape"
        val texto = (intent.getStringExtra("texto") ?: "Carlos Perez te yapeo S/ 15.50").replace('_', ' ')
        val pendiente = goAsync()
        CoroutineScope(Dispatchers.Main).launch {
            try {
                procesar(context, paquete, titulo, texto)
            } finally {
                pendiente.finish()
            }
        }
    }

    private suspend fun procesar(context: Context, paquete: String, titulo: String, texto: String) {
        val tag = "PagoDePrueba"
        val resultado = NotificationParser.parsear(titulo, texto, paquete)
        val monto = (resultado as? ResultadoParseo.MontoDetectado)?.monto
        if (monto == null) {
            Log.w(tag, "El parser NO lo reconoce como pago: \"$titulo: $texto\" -> $resultado")
            return
        }
        val tipo = MapeoAppsMetodoDeCobro.metodoDeCobroPara(paquete)
        val negocioId = NegocioStore.negocioActivoId.value
        if (negocioId == null || CuentaStore.sesionActual.value == null) {
            Log.w(tag, "Sin sesión o sin negocio activo")
            return
        }
        val ahora = System.currentTimeMillis()
        val operacion = Operacion(
            id = UUID.randomUUID().toString(),
            negocioId = negocioId,
            metodoPago = tipo,
            monto = monto,
            estado = EstadoOperacion.SIN_ASIGNAR,
            origen = OrigenOperacion.Detectada("debug-$ahora"),
            fechaHora = ahora,
            historial = listOf(CambioDeEstado(ahora, EstadoOperacion.SIN_ASIGNAR)),
        )
        val trabajadores = RelacionCuentaNegocioStore.relaciones.value.filter {
            it.negocioId == negocioId && it.rol == RolCuentaNegocio.TRABAJADOR && it.estado == EstadoRelacion.ACTIVA
        }
        val final = AssignmentEngine.asignarSiCorresponde(operacion, trabajadores)
        Log.i(tag, "Pago reconocido: S/ $monto ($tipo), estado ${final.estado}")
        InMemoryOperationStore.agregar(final)
            .onSuccess {
                Log.i(tag, "Guardado en el servidor; notificando")
                OperationNotifier.notificar(context, final)
            }
            .onFailure { Log.e(tag, "No se pudo guardar: ${it.message}") }
    }
}
