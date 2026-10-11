package com.flabs.cobrate.features.business

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.flabs.cobrate.R
import com.flabs.cobrate.core.model.RolCuentaNegocio
import com.flabs.cobrate.core.model.EstadoRelacion

/**
 * Pantalla de Establecimientos (Negocios) asociados a la cuenta.
 * Muestra todos los negocios reales y permite navegar al Quick Setup o Ingresar Código.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EstablishmentsScreen(
    negocioViewModel: NegocioViewModel,
    onNuevoNegocio: () -> Unit,
    onUnirmeNegocio: () -> Unit,
    onBack: () -> Unit
) {
    val misNegocios by negocioViewModel.misNegocios.collectAsState()
    val misRelaciones by negocioViewModel.misRelaciones.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nombre_mis_locales), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (misNegocios.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No tienes negocios activos", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                items(misNegocios) { negocio ->
                    val relacion = misRelaciones.find { it.negocioId == negocio.id }
                    val rolEtiqueta = when (relacion?.rol) {
                        RolCuentaNegocio.ADMINISTRADOR -> "Dueño"
                        RolCuentaNegocio.TRABAJADOR -> "Trabajador"
                        else -> "Desconocido"
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            negocioViewModel.seleccionarNegocio(negocio.id)
                        }
                    ) {
                        ListItem(
                            headlineContent = { 
                                Text(
                                    text = negocio.nombre,
                                    fontWeight = FontWeight.SemiBold
                                ) 
                            },
                            supportingContent = { 
                                val subText = if (relacion?.estado == EstadoRelacion.PAUSADA) 
                                    "$rolEtiqueta (Pausado)" else rolEtiqueta
                                Text(
                                    text = subText,
                                    color = if (relacion?.estado == EstadoRelacion.PAUSADA) 
                                        MaterialTheme.colorScheme.error 
                                    else MaterialTheme.colorScheme.primary
                                ) 
                            },
                            leadingContent = {
                                Icon(
                                    imageVector = Icons.Default.Store,
                                    contentDescription = null,
                                    tint = if (relacion?.estado == EstadoRelacion.PAUSADA)
                                        MaterialTheme.colorScheme.outline
                                    else MaterialTheme.colorScheme.primary
                                )
                            }
                        )
                    }
                }
            }

            // Panel de Acciones
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onNuevoNegocio,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Nuevo negocio", style = MaterialTheme.typography.titleMedium)
                }

                OutlinedButton(
                    onClick = onUnirmeNegocio,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(Icons.Default.Link, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Unirme a un negocio", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}
