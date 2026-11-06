package com.flabs.cobrate.features.operaciones

import com.flabs.cobrate.core.model.EstadoOperacion
import com.flabs.cobrate.core.model.Operacion
import com.flabs.cobrate.core.model.TipoMetodoCobro
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Lógica del Detalle de Operación. Convierte una operación y los datos que la rodean en
 * lo que la pantalla debe mostrar. Sin nada de interfaz: así las reglas se leen y se
 * prueban sin abrir ningún composable.
 */

/** Qué icono lleva cada fila de información. */
enum class IconoFila { NEGOCIO, FECHA, HORA, PAGADOR, TRABAJADOR, ID }

data class FilaDetalle(val icono: IconoFila, val etiqueta: String, val valor: String)

data class DetalleOperacion(
    val monto: String,
    /** true: "Recibido". false: la operación sigue sin asignar. */
    val recibido: Boolean,
    val metodo: TipoMetodoCobro?,
    val metodoNombre: String,
    val filas: List<FilaDetalle>,
)

object ConstructorDetalleOperacion {

    private val localePeru: Locale = Locale.forLanguageTag("es-PE")

    /**
     * @param nombreNegocio nombre del negocio de la operación, si se conoce.
     * @param tituloNotificacion título de la notificación que originó el pago (nombre del pagador).
     * @param trabajadoresActivos cuántos trabajadores activos tiene el negocio.
     * @param nombreTrabajador nombre del trabajador asignado, si se conoce.
     */
    fun construir(
        operacion: Operacion,
        nombreNegocio: String?,
        tituloNotificacion: String?,
        trabajadoresActivos: Int,
        nombreTrabajador: String?,
    ): DetalleOperacion {
        val fecha = Date(operacion.fechaHora)
        val filas = buildList {
            nombreNegocio?.takeIf { it.isNotBlank() }?.let {
                add(FilaDetalle(IconoFila.NEGOCIO, "Negocio", it))
            }
            add(FilaDetalle(IconoFila.FECHA, "Fecha", formatearFecha(fecha)))
            add(FilaDetalle(IconoFila.HORA, "Hora", formatearHora(fecha)))
            add(FilaDetalle(IconoFila.PAGADOR, "Pagador", nombrePagador(tituloNotificacion)))
            // Solo se muestra el trabajador si la operación tiene uno asignado y el negocio
            // tiene más de un trabajador activo.
            if (operacion.trabajadorAsignadoId != null && trabajadoresActivos > 1) {
                add(
                    FilaDetalle(
                        IconoFila.TRABAJADOR,
                        "Trabajador asociado",
                        nombreTrabajador ?: "Trabajador asignado",
                    ),
                )
            }
            add(FilaDetalle(IconoFila.ID, "ID Operación", operacion.id.take(12)))
        }

        return DetalleOperacion(
            monto = "S/ %.2f".format(Locale.US, operacion.monto),
            recibido = operacion.estado != EstadoOperacion.SIN_ASIGNAR,
            metodo = operacion.metodoPago,
            metodoNombre = operacion.metodoPago?.name?.lowercase()
                ?.replaceFirstChar { it.uppercase() } ?: "Pago sin identificar",
            filas = filas,
        )
    }

    /** El nombre del pagador sale del título de la notificación; sin título se usa un nombre genérico. */
    fun nombrePagador(tituloNotificacion: String?): String =
        tituloNotificacion?.trim()?.takeIf { it.isNotBlank() } ?: "Cliente Toke"

    private fun formatearFecha(fecha: Date): String =
        SimpleDateFormat("dd 'de' MMMM 'de' yyyy", localePeru).format(fecha)

    private fun formatearHora(fecha: Date): String =
        SimpleDateFormat("hh:mm a", localePeru).format(fecha)
}
