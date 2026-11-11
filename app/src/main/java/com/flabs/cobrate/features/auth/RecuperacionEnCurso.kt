package com.flabs.cobrate.features.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Marca "estoy cambiando mi contraseña". Verificar el código de recuperación abre sesión en Supabase;
 * mientras esta marca esté activa, la navegación global no debe sacar al usuario de la pantalla de
 * contraseña nueva hacia Inicio (ni mostrar "¿Cómo deseas empezar?").
 */
object RecuperacionEnCurso {
    private val _activa = MutableStateFlow(false)
    val activa: StateFlow<Boolean> = _activa

    fun iniciar() { _activa.value = true }
    fun terminar() { _activa.value = false }
}
