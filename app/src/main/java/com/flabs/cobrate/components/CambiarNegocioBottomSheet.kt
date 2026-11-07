package com.flabs.cobrate.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flabs.cobrate.core.data.CuentaStore
import com.flabs.cobrate.features.business.NegocioViewModel
import com.flabs.cobrate.features.negocios.ListaNegocios
import com.flabs.cobrate.features.negocios.NegocioFilaSelector
import com.flabs.cobrate.features.negocios.NegociosBotonesPie
import com.flabs.cobrate.ui.theme.PlusJakartaSans
import com.flabs.cobrate.ui.theme.TokePaleta
import com.flabs.cobrate.ui.theme.tokeEsOscuro
import com.flabs.cobrate.ui.theme.tokePaleta

/**
 * Selector "Cambiar de negocio". Reúne los datos y arma las piezas:
 *  - qué negocios salen y en qué estado lo decide [ListaNegocios];
 *  - cada fila vive en NegocioFilaSelector y los botones en NegociosBotonesPie.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CambiarNegocioBottomSheet(
    negocioViewModel: NegocioViewModel,
    onDismiss: () -> Unit,
    onCrearNegocio: () -> Unit,
    onUnirmeNegocio: () -> Unit,
) {
    val paleta = tokePaleta()
    val esOscuro = tokeEsOscuro()

    val misNegocios by negocioViewModel.misNegocios.collectAsState()
    val misRelaciones by negocioViewModel.misRelaciones.collectAsState()
    val negocioActivoId by negocioViewModel.negocioSeleccionadoId.collectAsState()
    val sesionActual by CuentaStore.sesionActual.collectAsState()
    val cuentaId = sesionActual?.id

    val items = remember(misNegocios, misRelaciones, cuentaId, negocioActivoId) {
        ListaNegocios.construir(misNegocios, misRelaciones, cuentaId, negocioActivoId)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = paleta.tarjeta,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .height(5.dp)
                        .clip(CircleShape)
                        .background(paleta.bordeControl),
                )
            }
        },
    ) {
        Column(modifier = Modifier.fillMaxWidth().navigationBarsPadding()) {
            Encabezado(paleta = paleta, cantidad = items.size, onCerrar = onDismiss)
            Linea(paleta)
            LazyColumn(
                modifier = Modifier.weight(1f, fill = false),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(items, key = { it.negocio.id }) { item ->
                    NegocioFilaSelector(
                        paleta = paleta,
                        esOscuro = esOscuro,
                        item = item,
                        onClick = {
                            negocioViewModel.seleccionarNegocio(item.negocio.id)
                            onDismiss()
                        },
                    )
                }
            }
            Linea(paleta)
            NegociosBotonesPie(
                paleta = paleta,
                textoPrincipal = "+ Crear Negocio",
                onPrincipal = {
                    onDismiss()
                    onCrearNegocio()
                },
                textoSecundario = "Unirme a un Negocio",
                onSecundario = {
                    onDismiss()
                    onUnirmeNegocio()
                },
            )
        }
    }
}

@Composable
private fun Encabezado(paleta: TokePaleta, cantidad: Int, onCerrar: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Cambiar de negocio",
                style = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    letterSpacing = (-0.5).sp,
                ),
                color = paleta.textoPrincipal,
            )
            Text(
                text = "$cantidad comercios asociados",
                style = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                ),
                color = paleta.textoSecundario,
            )
        }
        Surface(
            onClick = onCerrar,
            shape = RoundedCornerShape(12.dp),
            color = paleta.tileFondo,
            border = BorderStroke(1.dp, paleta.bordeControl),
            modifier = Modifier.size(40.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(id = TokeIcons.Cerrar),
                    contentDescription = "Cerrar",
                    tint = paleta.textoSecundario,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@Composable
private fun Linea(paleta: TokePaleta) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(paleta.borde),
    )
}
