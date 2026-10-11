package com.flabs.cobrate.features.auth

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.flabs.cobrate.R
import com.flabs.cobrate.core.data.PreferenciasAppStore
import com.flabs.cobrate.core.logic.FeedbackManager
import com.flabs.cobrate.ui.theme.ThemeMode
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    modoInicialCrear: Boolean = false,
    onVolver: () -> Unit = {}
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var modoCrear by rememberSaveable { mutableStateOf(modoInicialCrear) }
    val scope = rememberCoroutineScope()

    val themeMode by PreferenciasAppStore.themeMode.collectAsState()
    val isDark = themeMode == ThemeMode.DARK
    val logoResource = if (isDark) {
        R.drawable.ic_toke_logo
    } else {
        R.drawable.ic_toke_logo_green
    }

    BackHandler {
        onVolver()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.login_volver_desc)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Logo TOKE!
            Image(
                painter = painterResource(id = logoResource),
                contentDescription = stringResource(R.string.app_name),
                modifier = Modifier
                    .size(128.dp)
                    .align(Alignment.CenterHorizontally),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = if (modoCrear) stringResource(R.string.bienvenida_crear_cuenta) else stringResource(R.string.bienvenida_iniciar_sesion),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(stringResource(R.string.login_email_label)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(stringResource(R.string.login_password_label)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            val creandoMsg = stringResource(R.string.login_creando_cuenta_status)
            val iniciandoMsg = stringResource(R.string.login_iniciando_sesion_status)

            Button(
                onClick = {
                    scope.launch {
                        FeedbackManager.setLoading(true, if (modoCrear) creandoMsg else iniciandoMsg)
                        val result = if (modoCrear) {
                            authViewModel.crearCuenta(email, password)
                        } else {
                            authViewModel.iniciarSesion(email, password)
                        }
                        FeedbackManager.setLoading(false)
                        result.onFailure { FeedbackManager.showError(it) }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (modoCrear) stringResource(R.string.bienvenida_crear_cuenta) else stringResource(R.string.bienvenida_iniciar_sesion)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = {
                    modoCrear = !modoCrear
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (modoCrear) stringResource(R.string.login_ya_tengo_cuenta)
                    else stringResource(R.string.login_no_tengo_cuenta)
                )
            }
        }
    }
}
