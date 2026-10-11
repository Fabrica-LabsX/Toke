package com.flabs.cobrate.features.business

import androidx.compose.runtime.*
import com.flabs.cobrate.features.shared.OperacionesScreen

/**
 * Historial del dueño: pantalla de Operaciones unificada conforme a la Ronda 9 de Figma.
 */
@Composable
fun OwnerHistoryScreen(
    businessViewModel: BusinessViewModel,
    onRefrescarTab: (() -> Unit)? = null,
    onSeleccionarOperacion: ((String) -> Unit)? = null,
) {
    val operaciones by businessViewModel.operaciones.collectAsState()

    LaunchedEffect(Unit) {
        businessViewModel.cargarMetodos()
    }

    OperacionesScreen(
        operaciones = operaciones,
        onReclamarOperacion = { id ->
            // Asignación/reclamo de operación
        },
        onSeleccionarOperacion = onSeleccionarOperacion
    )
}
