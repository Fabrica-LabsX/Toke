package com.flabs.cobrate.features.debug

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.flabs.cobrate.core.data.SupabaseAuthBridge
import kotlinx.coroutines.launch

/**
 * Pantalla de prueba AISLADA para validar el puente de Supabase Auth (CAPA 7b-1).
 *
 * EXCLUSIVAMENTE para validación manual de la conexión real con Supabase.
 * NO forma parte del flujo real del producto: el login local (hash) sigue
 * siendo el único activo en la app.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupabaseAuthTestScreen(
    onVolver: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf<String?>(null) }

    val usuario by SupabaseAuthBridge.sesionActual.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Prueba Supabase Auth (7b-1)") })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (usuario != null) "Sesión activa: ${usuario!!.id}" else "Sin sesión",
                style = MaterialTheme.typography.titleMedium
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    scope.launch {
                        mensaje = "Creando cuenta Dueño..."
                        mensaje = SupabaseAuthBridge.crearCuentaDueño(email, password)
                            .fold(
                                onSuccess = { "Cuenta Dueño creada / sesión iniciada" },
                                onFailure = { "Error: ${it.message}" }
                            )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Crear cuenta Dueño (Supabase)")
            }

            OutlinedButton(
                onClick = {
                    scope.launch {
                        mensaje = "Iniciando sesión Dueño..."
                        mensaje = SupabaseAuthBridge.iniciarSesionDueño(email, password)
                            .fold(
                                onSuccess = { "Sesión Dueño iniciada" },
                                onFailure = { "Error: ${it.message}" }
                            )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Iniciar sesión Dueño (Supabase)")
            }

            OutlinedButton(
                onClick = {
                    scope.launch {
                        mensaje = "Iniciando sesión Trabajador anónimo..."
                        mensaje = SupabaseAuthBridge.iniciarSesionTrabajadorAnonimo()
                            .fold(
                                onSuccess = { "Sesión anónima de Trabajador iniciada" },
                                onFailure = { "Error: ${it.message}" }
                            )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Iniciar sesión Trabajador anónimo (Supabase)")
            }

            OutlinedButton(
                onClick = {
                    scope.launch {
                        mensaje = "Cerrando sesión..."
                        mensaje = SupabaseAuthBridge.cerrarSesionSupabase()
                            .fold(
                                onSuccess = { "Sesión Supabase cerrada" },
                                onFailure = { "Error: ${it.message}" }
                            )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cerrar sesión Supabase")
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onVolver,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Volver")
            }

            mensaje?.let { m ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = m,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}