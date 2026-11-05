package com.flabs.cobrate.components

import androidx.compose.ui.graphics.Color
import com.flabs.cobrate.core.model.TipoMetodoCobro
import com.flabs.cobrate.ui.theme.ThemeMode

object BilleteraColores {
    data class ColorPar(
        val claro: Color,
        val oscuro: Color
    )

    val Yape = ColorPar(
        claro = Color(0xFF8F00A9), // Referencia de diseño: nodo 1:270
        oscuro = Color(0xFF732282) // Referencia de diseño: nodo 1:90
    )

    val Plin = ColorPar(
        claro = Color(0xFF00B7FF), // Referencia de diseño: nodo 1:270
        oscuro = Color(0xFF00A4E4) // Referencia de diseño: nodo 1:90
    )

    val BCP = ColorPar(
        claro = Color(0xFF004BFF), // Referencia de diseño: nodo 1:270
        oscuro = Color(0xFF002A8F) // Referencia de diseño: nodo 1:90
    )
}

fun colorBilletera(metodo: TipoMetodoCobro?, aspecto: ThemeMode): Color? {
    val par = when (metodo) {
        TipoMetodoCobro.YAPE -> BilleteraColores.Yape
        TipoMetodoCobro.PLIN -> BilleteraColores.Plin
        TipoMetodoCobro.BCP -> BilleteraColores.BCP
        else -> return null
    }
    return when (aspecto) {
        ThemeMode.DARK -> par.oscuro
        ThemeMode.LIGHT -> par.claro
    }
}

fun colorBilletera(metodoNombre: String?, aspecto: ThemeMode): Color? {
    val par = when (metodoNombre?.lowercase()) {
        "yape" -> BilleteraColores.Yape
        "plin" -> BilleteraColores.Plin
        "bcp" -> BilleteraColores.BCP
        else -> return null
    }
    return when (aspecto) {
        ThemeMode.DARK -> par.oscuro
        ThemeMode.LIGHT -> par.claro
    }
}
