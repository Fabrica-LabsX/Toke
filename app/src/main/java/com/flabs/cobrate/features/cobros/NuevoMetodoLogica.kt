package com.flabs.cobrate.features.cobros

import com.flabs.cobrate.core.model.MetodoDeCobro
import com.flabs.cobrate.core.model.TipoMetodoCobro

/**
 * Lógica de la ventana "Vincular un método de cobro".
 *
 * En esta versión, exclusivamente Yape está habilitado para vinculación.
 * Plin y todas las demás entidades bancarias y billeteras se presentan visibles
 * pero deshabilitadas con la etiqueta "Próximamente".
 */

enum class PasoNuevoMetodo { ELEGIR, ELEGIR_PARA_PLIN, DATOS }

/** Un grupo de opciones con su título ("Billeteras", "Bancos", ...). */
data class GrupoMetodos(val titulo: String, val tipos: List<TipoMetodoCobro>)

object NuevoMetodoLogica {

    /**
     * Entidades de TOKE que forman parte de la red Plin (verificado en fuentes públicas: ver
     * docs/PLIN_ENTIDADES.md). BBVA, Interbank y Scotiabank son sus administradores; BanBif es
     * participante; Agora/Tunki es la billetera de Interbank y cobra con el QR de Plin.
     */
    private val bancosPlin = listOf(
        TipoMetodoCobro.INTERBANK,
        TipoMetodoCobro.BBVA,
        TipoMetodoCobro.SCOTIABANK,
        TipoMetodoCobro.BANBIF,
    )

    private val billeterasPlin = listOf(
        TipoMetodoCobro.AGORA_TUNKI,
    )

    private val billeterasComunes = listOf(
        TipoMetodoCobro.BIM,
        TipoMetodoCobro.LIGO,
        TipoMetodoCobro.SIP,
        TipoMetodoCobro.LEMON_CASH,
    )

    private val bancosComunes = listOf(
        TipoMetodoCobro.BCP,
        TipoMetodoCobro.PICHINCHA,
    )

    /**
     * Grupos del catálogo principal para vincular métodos de cobro.
     * Incluye todas las opciones del sistema:
     * - Billeteras: Yape (disponible), Plin y demás billeteras ("Próximamente").
     * - Bancos: todos los bancos ("Próximamente").
     * - Otros: transferencia ("Próximamente").
     */
    fun gruposPrincipales(): List<GrupoMetodos> = listOf(
        GrupoMetodos(
            titulo = "Billeteras",
            tipos = listOf(TipoMetodoCobro.YAPE, TipoMetodoCobro.PLIN) + billeterasComunes + billeterasPlin,
        ),
        GrupoMetodos(titulo = "Bancos", tipos = bancosComunes + bancosPlin),
        GrupoMetodos(titulo = "Otros", tipos = listOf(TipoMetodoCobro.TRANSFERENCIA)),
    )

    /** Lo que se ve dentro de Plin: entidades de la red Plin conservadas como referencia. */
    fun gruposParaPlin(): List<GrupoMetodos> = listOf(
        GrupoMetodos(titulo = "Bancos", tipos = bancosPlin),
        GrupoMetodos(titulo = "Billeteras", tipos = billeterasPlin),
    )

    fun tiposParaPlin(): List<TipoMetodoCobro> = gruposParaPlin().flatMap { it.tipos }

    /** ¿Esta billetera o banco pertenece a la red Plin? */
    fun esDeLaRedPlin(tipo: TipoMetodoCobro): Boolean = tipo in tiposParaPlin()

    /** ¿El negocio ya tiene un método de esta billetera o banco? */
    fun yaExiste(tipo: TipoMetodoCobro, existentes: List<MetodoDeCobro>): Boolean =
        existentes.any { it.tipo == tipo }

    /**
     * Indica si un método está en estado "Próximamente".
     * En esta versión, solo Yape está disponible; todos los demás tipos están en Próximamente.
     */
    fun esProximamente(tipo: TipoMetodoCobro): Boolean =
        tipo != TipoMetodoCobro.YAPE

    /**
     * Solo Yape puede elegirse, siempre que no exista ya en el negocio.
     * Plin y todos los demás métodos no se pueden elegir.
     */
    fun puedeElegir(tipo: TipoMetodoCobro, existentes: List<MetodoDeCobro>): Boolean =
        if (tipo == TipoMetodoCobro.YAPE) {
            !yaExiste(TipoMetodoCobro.YAPE, existentes)
        } else {
            false
        }

    /**
     * Regla estricta de elegibilidad de negocio: rechaza cualquier tipo distinto de YAPE.
     */
    fun esTipoPermitidoParaCrear(tipo: TipoMetodoCobro): Boolean =
        tipo == TipoMetodoCobro.YAPE

    /** El número o cuenta es obligatorio; el titular es opcional. */
    fun datosValidos(dato: String): Boolean = com.flabs.cobrate.core.logic.EntradaSegura.datoDeCobroSeguro(dato)

    fun subtituloDe(tipo: TipoMetodoCobro): String? = null
}
