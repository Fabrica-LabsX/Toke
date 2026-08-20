package com.flabs.cobrate.features.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Pantalla de entrada de TOKE V1 (CAPA 2 — autenticación local, sin backend).
 *
 * Muestra los campos username/contraseña en dos modos:
 *   - "Iniciar sesión" (por defecto), con botón para pasar a crear cuenta.
 *   - "Crear cuenta", con botón para volver a iniciar sesión.
 *
 * Los errores (username ya existe, usuario/contraseña incorrectos) se muestran
 * como un simple mensaje bajo los campos.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    authViewModel: AuthViewModel
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var modoCrear by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Toke",
                style = MaterialTheme.typography.headlineLarge
            )
            Text(
                text = if (modoCrear) "Crear cuenta" else "Iniciar sesión",
                style = MaterialTheme.typography.titleMedium
            )

            OutlinedTextField(
                value = username,
                onValueChange = { username = it; error = null },
                label = { Text("Nombre de usuario") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it; error = null },
                label = { Text("Contraseña") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            error?.let { mensaje ->
                Text(
                    text = mensaje,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Button(
                onClick = {
                    val resultado = if (modoCrear) {
                        authViewModel.crearCuenta(username, password)
                    } else {
                        authViewModel.iniciarSesion(username, password)
                    }
                    resultado
                        .onSuccess { }
                        .onFailure { error = it.message }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = if (modoCrear) "Crear cuenta" else "Iniciar sesión")
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = {
                    modoCrear = !modoCrear
                    error = null
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (modoCrear) "Ya tengo cuenta — Iniciar sesión"
                    else "¿No tienes cuenta? Crear cuenta"
                )
            }
        }
    }
}