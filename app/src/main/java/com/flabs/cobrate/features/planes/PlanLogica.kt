package com.flabs.cobrate.features.planes

import com.flabs.cobrate.features.reportes.PeriodoReporte
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class Plan { FREE, PRO }

/** Lo que el servidor dice del plan de un negocio (el plan lo manda la cuenta dueña). */
@Serializable
data class EstadoPlanDto(
    @SerialName("plan") val plan: String = "FREE",
    @SerialName("pro_hasta") val proHasta: Long? = null,
    @SerialName("limite") val limite: Int? = null,
    @SerialName("usadas") val usadas: Int = 0,
    @SerialName("max_colaboradores") val maxColaboradores: Int? = null,
    @SerialName("trabajadores_activos") val trabajadoresActivos: Int? = null,
)

data class EstadoPlan(
    val plan: Plan,
    val proHasta: Long?,
    val limite: Int?,
    val usadas: Int,
    val maxColaboradores: Int?,
    val trabajadoresActivos: Int? = null,
)

/** Cómo se ve la cuota: de más tranquila a agotada. */
enum class NivelCuota { NORMAL, AMARILLO, ROJO, AGOTADA }

/** Reglas de los planes Free y Pro. Solo lógica: sin red ni pantallas. */
object PlanLogica {
    /** De 17 a 20 notificaciones restantes la cuota se pinta en amarillo; desde 16, en rojo. */
    // Los avisos se miden en proporción a la cuota: con 50 son 20 y 16 restantes; con 20 son 8 y 6.
    const val CUOTA_BASE = 50
    const val AMARILLO_PORCIENTO = 40
    const val ROJO_PORCIENTO = 32
    const val DIAS_HISTORIAL_FREE = 30
    const val MAX_NEGOCIOS_FREE = 1

    fun aEstado(dto: EstadoPlanDto): EstadoPlan = EstadoPlan(
        plan = if (dto.plan.equals("PRO", ignoreCase = true)) Plan.PRO else Plan.FREE,
        proHasta = dto.proHasta,
        limite = dto.limite,
        usadas = dto.usadas,
        maxColaboradores = dto.maxColaboradores,
        trabajadoresActivos = dto.trabajadoresActivos,
    )

    /** Se usa lo mayor entre lo que cuenta el servidor y lo que ya llegó a este celular hoy. */
    fun usadas(servidor: Int, localesHoy: Int): Int = maxOf(servidor, localesHoy)

    fun restantes(limite: Int?, usadas: Int): Int? = limite?.let { (it - usadas).coerceAtLeast(0) }

    /** Null cuando no hay cuota que mostrar (Pro, o todavía no se sabe el plan). */
    fun nivel(restantes: Int?, limite: Int = CUOTA_BASE): NivelCuota? = when {
        restantes == null -> null
        restantes <= 0 -> NivelCuota.AGOTADA
        restantes * 100 <= limite * ROJO_PORCIENTO -> NivelCuota.ROJO
        restantes * 100 <= limite * AMARILLO_PORCIENTO -> NivelCuota.AMARILLO
        else -> NivelCuota.NORMAL
    }

    /** Parte de la barra que queda llena, de 0 a 1. */
    fun progreso(limite: Int?, restantes: Int?): Float =
        if (limite == null || restantes == null || limite <= 0) 0f else (restantes.toFloat() / limite).coerceIn(0f, 1f)

    fun textoRestantes(restantes: Int): String = when (restantes) {
        0 -> "Cuota de hoy agotada"
        1 -> "1 notificación restante"
        else -> "$restantes notificaciones restantes"
    }

    /** Sin saber el plan no se bloquea nada (los pagos no se interrumpen); el servidor es la barrera real. */
    private fun esFree(plan: Plan?) = plan == Plan.FREE

    fun puedeVozBasica(plan: Plan?) = !esFree(plan)

    fun puedeVerPeriodo(plan: Plan?, periodo: PeriodoReporte) = !esFree(plan) || periodo == PeriodoReporte.DIA

    fun puedeCrearOtroNegocio(plan: Plan?, negociosPropios: Int) = !esFree(plan) || negociosPropios < MAX_NEGOCIOS_FREE

    /** Desde cuándo (milisegundos) se ven las operaciones: Free ve los últimos 30 días; Pro, todo. */
    fun desdeHistorial(plan: Plan?, ahora: Long): Long =
        if (esFree(plan)) ahora - DIAS_HISTORIAL_FREE * 24L * 60 * 60 * 1000 else Long.MIN_VALUE

    /** El servidor rechaza con este texto al llegar a la cuota (ver migración planes_free_pro). */
    fun esCuotaAgotada(mensajeDeError: String?): Boolean = mensajeDeError?.contains("CUOTA_AGOTADA") == true

    /** Solo se celebra cuando antes se vio Free y ahora Pro (la primera vez que se consulta no se celebra nada). */
    fun esActivacionNueva(anterior: Plan?, nuevo: Plan): Boolean = anterior == Plan.FREE && nuevo == Plan.PRO

    /** "Activo hasta 7 de noviembre" o, sin fecha, "Sin fecha de vencimiento". */
    fun textoVigencia(proHasta: Long?): String =
        if (proHasta == null) "Sin fecha de vencimiento"
        else "Activo hasta " + java.text.SimpleDateFormat("d 'de' MMMM", java.util.Locale.forLanguageTag("es-PE"))
            .format(java.util.Date(proHasta))

    /** Enlace de WhatsApp para pedir la activación: deja solo los dígitos del número. */
    fun enlaceWhatsApp(telefono: String): String =
        "https://wa.me/${telefono.filter { it.isDigit() }}?text=" +
            java.net.URLEncoder.encode("Hola, quiero activar el plan Pro de TOKE", "UTF-8").replace("+", "%20")

    /** Texto para el usuario cuando el servidor rechaza algo por el plan; null si el error no es de plan. */
    fun mensajeDeLimite(mensajeDeError: String?): String? = when {
        mensajeDeError == null -> null
        mensajeDeError.contains("LIMITE_NEGOCIOS") -> "El plan Free permite 1 negocio. Pasa a Pro para crear más."
        mensajeDeError.contains("LIMITE_COLABORADORES") -> "El plan Free permite hasta 2 colaboradores. Pasa a Pro para sumar más."
        mensajeDeError.contains("LIMITE_METODOS") -> "El plan Free permite hasta 4 métodos de cobro. Pasa a Pro para vincular más."
        mensajeDeError.contains("CUOTA_AGOTADA") -> "Se acabó la cuota de hoy del plan Free. Espera a mañana o activa Pro."
        else -> null
    }
}

/** Cuota ya calculada para pintarla en el Home (solo el plan Free tiene cuota). */
data class CuotaUi(val restantes: Int, val limite: Int, val nivel: NivelCuota)

fun cuotaDeHoy(estado: EstadoPlan?, cobrosLocalesHoy: Int): CuotaUi? {
    if (estado == null || estado.plan != Plan.FREE || estado.limite == null) return null
    val restantes = PlanLogica.restantes(estado.limite, PlanLogica.usadas(estado.usadas, cobrosLocalesHoy)) ?: return null
    val nivel = PlanLogica.nivel(restantes, estado.limite) ?: return null
    return CuotaUi(restantes, estado.limite, nivel)
}
