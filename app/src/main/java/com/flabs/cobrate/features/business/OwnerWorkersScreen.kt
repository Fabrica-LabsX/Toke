package com.flabs.cobrate.features.business

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.flabs.cobrate.core.data.CuentaStore
import com.flabs.cobrate.core.data.EstadoServiciosStore
import com.flabs.cobrate.core.data.NegocioStore
import com.flabs.cobrate.core.data.RelacionCuentaNegocioStore
import com.flabs.cobrate.core.model.EstadoRelacion
import com.flabs.cobrate.core.model.EstadoTrabajador
import com.flabs.cobrate.core.model.ModoRegistro
import com.flabs.cobrate.core.model.RolCuentaNegocio
import com.flabs.cobrate.core.model.Trabajador

/**
 * Pantalla de trabajadores del dueño.
 * El estado vive en BusinessViewModel (en memoria, sin ViewModel de terceros).
 *
 * CONFLICTO PENDIENTE DE UNIFICACIÓN (reportado, sin resolver): esta pantalla
 * convive temporalmente con dos conceptos de "trabajador":
 *  1. El [Trabajador] de [TrabajadorStore] (modelo viejo de CAPA 0, sin cuenta).
 *  2. La invitación por código de CAPA 4, que al aceptarse crea una
 *     [RelacionCuentaNegocio] con rol TRABAJADOR (modelo nuevo multi-negocio).
 * No se decidió cuál reemplaza a cuál; ambos conviven por ahora.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerWorkersScreen(
    businessViewModel: BusinessViewModel
) {
    val trabajadores by businessViewModel.trabajadores.collectAsState()
    var mostrandoFormulario by remember { mutableStateOf(false) }
    var mostrandoInvitar by remember { mutableStateOf(false) }

    // SERVICIOS (CAPA 5b): estado del control de EJECUCIÓN para el negocio actual.
    // Se refleja aquí para que el trabajador entienda por qué no recibe eventos si
    // está apagado. Solo lectura del estado; no altera datos históricos ni sesión.
    val sesion by CuentaStore.sesionActual.collectAsState()
    val relaciones by RelacionCuentaNegocioStore.relaciones.collectAsState()
    val negocios by NegocioStore.negocios.collectAsState()
    val estadosServicios by EstadoServiciosStore.estados.collectAsState()

    val negocioActual = remember(sesion?.id, relaciones, negocios) {
        val cuentaId = sesion?.id ?: return@remember null
        relaciones
            .filter {
                it.cuentaId == cuentaId &&
                    it.estado == EstadoRelacion.ACTIVA &&
                    it.rol == RolCuentaNegocio.ADMINISTRADOR
            }
            .mapNotNull { negocio -> negocios.find { it.id == negocio.negocioId } }
            .firstOrNull()
    }
    val servicioActivado = negocioActual?.let { negocio ->
        estadosServicios.firstOrNull { it.negocioId == negocio.id }?.activado ?: true
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Trabajadores",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(16.dp))

                // CAPA 4: invitación por código. Convive temporalmente con el listado
                // de [TrabajadorStore] (modelo viejo de CAPA 0). Ver nota de conflicto
                // al final de este archivo: no se decidió aún cuál reemplaza a cuál.
                Button(
                    onClick = { mostrandoInvitar = !mostrandoInvitar },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = if (mostrandoInvitar) "Cerrar invitación" else "Invitar trabajador")
                }
                if (mostrandoInvitar) {
                    Spacer(modifier = Modifier.height(16.dp))
                    InvitarTrabajadorScreen(
                        onCerrar = { mostrandoInvitar = false }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (mostrandoFormulario) {
                    FormularioAgregarTrabajador(
                        onAgregar = { nombre, modo ->
                            businessViewModel.agregarTrabajador(nombre, modo)
                            mostrandoFormulario = false
                        },
                        onCancelar = { mostrandoFormulario = false }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Button(
                    onClick = { mostrandoFormulario = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Agregar trabajador")
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(trabajadores, key = { it.id }) { trabajador ->
                    TrabajadorCard(
                        trabajador = trabajador,
                        servicioActivado = servicioActivado,
                        onAlternarEstado = {
                            val nuevoEstado = if (trabajador.estado == EstadoTrabajador.ACTIVO) {
                                EstadoTrabajador.PAUSADO
                            } else {
                                EstadoTrabajador.ACTIVO
                            }
                            businessViewModel.cambiarEstado(trabajador.id, nuevoEstado)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun FormularioAgregarTrabajador(
    onAgregar: (String, ModoRegistro) -> Unit,
    onCancelar: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var modo by remember { mutableStateOf(ModoRegistro.APP) }
    var menuAbierto by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Nuevo trabajador",
                style = MaterialTheme.typography.titleMedium
            )

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Column {
                OutlinedButton(onClick = { menuAbierto = true }) {
                    Text(text = "Modo: ${modo.name}")
                }
                DropdownMenu(
                    expanded = menuAbierto,
                    onDismissRequest = { menuAbierto = false }
                ) {
                    ModoRegistro.entries.forEach { m ->
                        DropdownMenuItem(
                            text = { Text(m.name) },
                            onClick = {
                                modo = m
                                menuAbierto = false
                            }
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        if (nombre.isNotBlank()) {
                            onAgregar(nombre, modo)
                        }
                    },
                    enabled = nombre.isNotBlank()
                ) {
                    Text(text = "Agregar")
                }
                OutlinedButton(onClick = onCancelar) {
                    Text(text = "Cancelar")
                }
            }
        }
    }
}

@Composable
private fun TrabajadorCard(
    trabajador: Trabajador,
    // SERVICIOS (CAPA 5b): refleja el estado del control de ejecución del negocio.
    servicioActivado: Boolean?,
    onAlternarEstado: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = trabajador.nombre,
                style = MaterialTheme.typography.titleMedium
            )
            // Indicador SERVICIOS: le explica al trabajador por qué no recibe eventos
            // remotos si el control está apagado (CAPA 5b, sin distribución real aún).
            servicioActivado?.let { activado ->
                Text(
                    text = if (activado) "Servicio activado" else "Servicio desactivado",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (activado) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outline
                    }
                )
            }
            Text(
                text = "Modo: ${trabajador.modo.name}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Estado: ${trabajador.estado.name}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(onClick = onAlternarEstado) {
                    Text(
                        text = if (trabajador.estado == EstadoTrabajador.ACTIVO) "Pausar" else "Reactivar"
                    )
                }
            }
        }
    }
}