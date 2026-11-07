package com.flabs.cobrate.features.cobros

import com.flabs.cobrate.core.model.MetodoDeCobro
import com.flabs.cobrate.core.model.TipoMetodoCobro

/**
 * Lógica de la ventana "Vincular un método de cobro". Sin nada de interfaz: qué opciones se
 * ofrecen, cuáles pertenecen a la red Plin y cuáles ya existen.
 *
 * Plin no es una billetera aparte: al elegirlo se escoge el banco o billetera de la red Plin donde
 * se recibe. El método que se crea es esa billetera, sin marca ni dato extra, y cada billetera solo
 * puede existir una vez; por eso se pueden tener varios Plin siempre que sean de billeteras
 * distintas.
 */

enum class PasoNuevoMetodo { ELEGIR, ELEGIR_PARA_PLIN, DATOS }

/** Un grupo de opciones con su título ("Billeteras", "Bancos", ...). */
data class GrupoMetodos(val titulo: String, val tipos: List<TipoMetodoCobro>)

object NuevoMetodoLogica {

    /**
     * Entidades de TOKE que forman parte de la red Plin (verificado en fuentes públicas: ver
     * docs/PLIN_ENTIDADES.md). BBVA, Interbank y Scotiabank son sus administradores; BanBif es
     * participante; Agora/Tunki es la billetera de Interbank y cobra con el QR de Plin.
     *
     * Quedan fuera, por no haber confirmación de que ofrezcan Plin: BCP (es de Yape), Pichincha,
     * BIM, Ligo, Sip y Lemon Cash. Para mover una entidad basta con agregarla o quitarla aquí.
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
     * Grupos de la primera pantalla. Las entidades de la red Plin no salen aquí: se llega a ellas
     * eligiendo Plin.
     */
    fun gruposPrincipales(): List<GrupoMetodos> = listOf(
        GrupoMetodos(
            titulo = "Billeteras",
            tipos = listOf(TipoMetodoCobro.YAPE, TipoMetodoCobro.PLIN) + billeterasComunes,
        ),
        GrupoMetodos(titulo = "Bancos", tipos = bancosComunes),
        GrupoMetodos(titulo = "Otros", tipos = listOf(TipoMetodoCobro.TRANSFERENCIA)),
    )

    /** Lo que se ve dentro de Plin: solo las entidades de la red Plin. */
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
     * Plin siempre se puede elegir mientras quede al menos una billetera o banco de su red libre;
     * el resto solo si no existe ya.
     */
    fun puedeElegir(tipo: TipoMetodoCobro, existentes: List<MetodoDeCobro>): Boolean =
        if (tipo == TipoMetodoCobro.PLIN) {
            tiposParaPlin().any { !yaExiste(it, existentes) }
        } else {
            !yaExiste(tipo, existentes)
        }

    /** El número o cuenta es obligatorio; el titular es opcional. */
    fun datosValidos(dato: String): Boolean = dato.isNotBlank()

    fun subtituloDe(tipo: TipoMetodoCobro): String? =
        if (tipo == TipoMetodoCobro.PLIN) "Elige tu banco o billetera" else null
}
