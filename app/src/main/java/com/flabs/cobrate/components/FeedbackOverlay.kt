package com.flabs.cobrate.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.flabs.cobrate.core.logic.FeedbackManager
import com.flabs.cobrate.core.logic.FeedbackType
import com.flabs.cobrate.ui.theme.TokeSuccess

/**
 * Overlay global que reacciona al estado de FeedbackManager.
 * Muestra Snackbars para mensajes temporales y Banners/Loadings para estados persistentes.
 */
@Composable
fun FeedbackOverlay() {
    val feedback by FeedbackManager.currentFeedback.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Reacciona a cambios en el feedback para mostrar Snackbars
    LaunchedEffect(feedback) {
        val current = feedback ?: return@LaunchedEffect
        
        if (current.durationMillis != null) {
            val result = snackbarHostState.showSnackbar(
                message = current.message,
                actionLabel = current.actionLabel,
                withDismissAction = true,
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                current.action?.invoke()
            }
            // Eliminado el delay(current.durationMillis) redundante (Defecto 3)
            FeedbackManager.clear()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Host de Snackbars (Error, Success, Info temporales)
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 88.dp) // Defecto 1: Espacio para barra C9 (64) + FAB (24)
                .padding(16.dp)
        ) { data ->
            // Defecto 4: Uso de roles del colorScheme en lugar de literales
            val (containerColor, contentColor) = when (feedback?.type) {
                FeedbackType.SUCCESS -> TokeSuccess to MaterialTheme.colorScheme.onPrimary
                FeedbackType.WARNING -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
                else -> MaterialTheme.colorScheme.inverseSurface to MaterialTheme.colorScheme.inverseOnSurface
            }
            
            Snackbar(
                snackbarData = data,
                containerColor = containerColor,
                contentColor = contentColor,
                actionColor = contentColor
            )
        }

        // Banners para estados persistentes (Offline, Loading)
        feedback?.let { state ->
            if (state.durationMillis == null) {
                when (state.type) {
                    FeedbackType.LOADING -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.3f))
                                // Defecto 2: Bloquear interacción bajo el scrim
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { },
                            contentAlignment = Alignment.Center
                        ) {
                            Card {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CircularProgressIndicator()
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(text = state.message)
                                }
                            }
                        }
                    }
                    FeedbackType.OFFLINE -> {
                        Surface(
                            modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter),
                            color = MaterialTheme.colorScheme.errorContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "⚠️ ${state.message}",
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}
