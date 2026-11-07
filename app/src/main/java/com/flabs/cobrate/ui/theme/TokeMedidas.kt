package com.flabs.cobrate.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Medidas únicas de la app. Los botones usan estas alturas y no números sueltos, para que
 * ninguno quede enorme ni diminuto.
 */
object TokeMedidas {
    /** Botón principal o secundario a todo el ancho (el valor del diseño). */
    val BotonAltura = 50.dp

    /** Botón pequeño dentro de una fila o tarjeta. */
    val BotonCompacto = 40.dp

    /** Botón cuadrado con un icono (atrás, cerrar, menú) en las cabeceras. */
    val BotonIcono = 42.dp

    /** Pestañas y chips de filtro. */
    val ChipAltura = 44.dp

    val MargenLateral = 20.dp
    val EntreTituloYContenido = 16.dp
    val EntreGrupos = 12.dp
    val PaddingTarjeta = 16.dp
}
