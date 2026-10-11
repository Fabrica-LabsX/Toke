package com.flabs.cobrate.features.nav

import androidx.navigation.NavController
import com.flabs.cobrate.Routes

/**
 * Pestañas de la barra (Inicio, Mis cobros, Mi equipo): siempre parten de Inicio, sin acumular pantallas.
 * CAPA 25.
 */
fun NavController.irAPestana(ruta: String) {
    if (ruta == Routes.INICIO) {
        val popExitoso = popBackStack(Routes.INICIO, inclusive = false)
        if (!popExitoso) {
            navigate(Routes.INICIO)
        }
    } else {
        navigate(ruta) {
            popUpTo(Routes.INICIO) { inclusive = false }
            launchSingleTop = true
        }
    }
}

/**
 * Pantallas de detalle (Mis QR, Reportes, Mis locales, Mi plan, Permisos, Aviso en voz alta).
 * CAPA 25.
 */
fun NavController.irADetalle(ruta: String) {
    navigate(ruta) { launchSingleTop = true }
}
