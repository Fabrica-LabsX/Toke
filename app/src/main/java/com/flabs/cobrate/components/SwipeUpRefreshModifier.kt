package com.flabs.cobrate.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity

/**
 * NestedScrollConnection que detecta un gesto intencional de deslizamiento hacia arriba
 * al final del scroll para activar el refresco de la pestaña activa (CAPA P1).
 */
@Composable
fun rememberSwipeUpRefreshConnection(
    enabled: Boolean = true,
    onRefresh: () -> Unit
): NestedScrollConnection {
    return remember(enabled, onRefresh) {
        object : NestedScrollConnection {
            private var acumuladoArriba = 0f
            private val UMBRAL_REFRESCO_PX = 250f

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (enabled && available.y < 0f) {
                    acumuladoArriba += -available.y
                    if (acumuladoArriba >= UMBRAL_REFRESCO_PX) {
                        acumuladoArriba = 0f
                        onRefresh()
                    }
                } else if (consumed.y != 0f) {
                    acumuladoArriba = 0f
                }
                return Offset.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                acumuladoArriba = 0f
                return Velocity.Zero
            }
        }
    }
}
