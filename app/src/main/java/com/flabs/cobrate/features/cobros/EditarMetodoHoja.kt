package com.flabs.cobrate.features.cobros
import com.flabs.cobrate.ui.theme.TokeMedidas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flabs.cobrate.components.IconoMetodoPago
import com.flabs.cobrate.components.TokeIcons
import com.flabs.cobrate.core.model.MetodoDeCobro
import com.flabs.cobrate.ui.theme.PlusJakartaSans
import com.flabs.cobrate.ui.theme.TokePaleta

/**
 * Ventana "Editar método": misma hoja que "Vincular un método de cobro". Arriba lleva el método,
 * la papelera (para eliminarlo) y cerrar. Si [onEliminar] es null no se muestra la papelera.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarMetodoHoja(
    paleta: TokePaleta,
    metodo: MetodoDeCobro,
    onDismiss: () -> Unit,
    onEliminar: (() -> Unit)?,
    onConfirm: (id: String, dato: String, titular: String, oculto: Boolean) -> Unit,
) {
    var dato by remember { mutableStateOf(metodo.datoDeCobro) }
    var titular by remember { mutableStateOf(metodo.nombreDestinatario ?: "") }
    var oculto by remember { mutableStateOf(metodo.oculto) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = paleta.fondo,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Editar método",
                        style = TextStyle(
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 21.sp,
                            letterSpacing = (-0.4).sp,
                        ),
                        color = paleta.textoPrincipal,
                    )
                    Text(
                        text = "Cambia los datos que ven tus clientes",
                        style = TextStyle(
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                        ),
                        color = paleta.textoSecundario,
                    )
                }
                if (onEliminar != null) {
                    Spacer(modifier = Modifier.width(12.dp))
                    BotonPapelera(paleta = paleta, onClick = onEliminar)
                }
                Spacer(modifier = Modifier.width(12.dp))
                BotonCuadrado(paleta, TokeIcons.Cerrar, "Cerrar", onDismiss)
            }

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = paleta.tarjeta,
                border = BorderStroke(1.dp, paleta.borde),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconoMetodoPago(tipo = metodo.tipo, tamano = 44.dp)
                    Spacer(modifier = Modifier.width(14.dp))
                    NombreMetodo(
                        metodo = metodo,
                        estilo = TextStyle(
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                        ),
                        colorNombre = paleta.textoPrincipal,
                        colorMarca = paleta.textoSecundario,
                        colorBarrera = paleta.bordeControl,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            CampoNuevoMetodo(
                paleta = paleta,
                etiqueta = "Número donde recibes",
                ayuda = "El celular o la cuenta con la que te pagan tus clientes.",
                valor = dato,
                placeholder = "Ej: 987 654 321",
                teclado = KeyboardType.Phone,
                onCambia = { dato = it },
            )
            CampoNuevoMetodo(
                paleta = paleta,
                etiqueta = "Nombre del titular (opcional)",
                ayuda = "Tal como aparece en tu cuenta. Tus clientes lo verán al pagar.",
                valor = titular,
                placeholder = "Ej: Carlos Mendoza",
                teclado = KeyboardType.Text,
                onCambia = { titular = it },
            )

            Surface(
                onClick = { oculto = !oculto },
                shape = RoundedCornerShape(18.dp),
                color = paleta.tarjeta,
                border = BorderStroke(1.dp, paleta.borde),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Ocultar método",
                            style = TextStyle(
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                            ),
                            color = paleta.textoPrincipal,
                        )
                        Text(
                            text = "No aparecerá en tu lista, pero no se borra.",
                            style = TextStyle(
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp,
                            ),
                            color = paleta.textoSecundario,
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Switch(
                        checked = oculto,
                        onCheckedChange = { oculto = it },
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = paleta.verde,
                            checkedThumbColor = paleta.verdeTexto,
                            uncheckedTrackColor = paleta.bordeControl,
                            uncheckedBorderColor = paleta.bordeControl,
                            uncheckedThumbColor = paleta.tarjeta,
                        ),
                    )
                }
            }

            val habilitado = NuevoMetodoLogica.datosValidos(dato)
            Surface(
                onClick = { onConfirm(metodo.id, com.flabs.cobrate.core.logic.EntradaSegura.limpiar(dato, 40), com.flabs.cobrate.core.logic.EntradaSegura.limpiar(titular, 40), oculto) },
                enabled = habilitado,
                shape = RoundedCornerShape(9999.dp),
                color = if (habilitado) paleta.verde else paleta.bordeControl,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(TokeMedidas.BotonAltura),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "Guardar cambios",
                        style = TextStyle(
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                        ),
                        color = if (habilitado) paleta.verdeTexto else paleta.textoSecundario,
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/** Botón cuadrado con la papelera. Abre la confirmación para borrar el método. */
@Composable
private fun BotonPapelera(paleta: TokePaleta, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = paleta.tarjeta,
        border = BorderStroke(1.dp, paleta.borde),
        modifier = Modifier.size(42.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(id = TokeIcons.Papelera),
                contentDescription = "Eliminar método de cobro",
                tint = Color.Unspecified,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
