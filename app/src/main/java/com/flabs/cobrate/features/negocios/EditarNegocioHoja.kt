package com.flabs.cobrate.features.negocios

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flabs.cobrate.components.TokeIcons
import com.flabs.cobrate.core.logic.FeedbackManager
import com.flabs.cobrate.core.model.Negocio
import com.flabs.cobrate.features.acceso.BotonAcceso
import com.flabs.cobrate.features.acceso.CampoAcceso
import com.flabs.cobrate.ui.theme.PlusJakartaSans
import com.flabs.cobrate.ui.theme.TokeMedidas
import com.flabs.cobrate.ui.theme.TokePaleta
import com.flabs.cobrate.ui.theme.rememberPulsacion

/** Hoja para editar un negocio: nombre, máximo de trabajadores y, abajo, borrarlo. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarNegocioHoja(
    paleta: TokePaleta,
    negocio: Negocio,
    maxInicial: Int?,
    onGuardar: (nombre: String, max: Int?, limpiarMax: Boolean) -> Unit,
    onBorrar: () -> Unit,
    onDismiss: () -> Unit,
) {
    var nombre by remember { mutableStateOf(negocio.nombre) }
    var maxTexto by remember(maxInicial) { mutableStateOf(maxInicial?.toString() ?: "") }

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
                .padding(horizontal = TokeMedidas.MargenLateral),
            verticalArrangement = Arrangement.spacedBy(TokeMedidas.EntreGrupos),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Editar negocio",
                        style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.ExtraBold, fontSize = 21.sp),
                        color = paleta.textoPrincipal,
                    )
                    Text(
                        text = "Cambia el nombre o el tope de trabajadores",
                        style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Medium, fontSize = 14.sp),
                        color = paleta.textoSecundario,
                    )
                }
                Surface(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    color = paleta.tarjeta,
                    border = BorderStroke(1.dp, paleta.borde),
                    modifier = Modifier.size(TokeMedidas.BotonIcono),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(id = TokeIcons.Cerrar),
                            contentDescription = "Cerrar",
                            tint = paleta.textoPrincipal,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }

            CampoAcceso(paleta, "Nombre del negocio", nombre, "Ej: Mi Bodeguita", { nombre = it })
            CampoAcceso(paleta, "Máximo de trabajadores", maxTexto, "Sin límite", { maxTexto = it.filter(Char::isDigit) }, KeyboardType.Number)
            Text(
                text = "Déjalo vacío si no quieres poner límite.",
                style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Medium, fontSize = 13.sp),
                color = paleta.textoSecundario,
                modifier = Modifier.padding(start = 4.dp),
            )

            Spacer(modifier = Modifier.height(4.dp))
            BotonAcceso(paleta, "Guardar cambios", principal = true) {
                val errorNombre = NegocioEdicionLogica.errorNombre(nombre)
                val (max, errorMax) = NegocioEdicionLogica.leerMax(maxTexto)
                when {
                    errorNombre != null -> FeedbackManager.showErrorMessage(errorNombre)
                    errorMax != null -> FeedbackManager.showErrorMessage(errorMax)
                    else -> onGuardar(nombre.trim(), max, max == null)
                }
            }
            BotonBorrar(paleta, onBorrar)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun BotonBorrar(paleta: TokePaleta, onClick: () -> Unit) {
    val pulso = rememberPulsacion()
    Surface(
        onClick = onClick,
        interactionSource = pulso.fuente,
        shape = RoundedCornerShape(9999.dp),
        color = paleta.rojoFondo,
        border = BorderStroke(1.dp, paleta.rojoBorde),
        modifier = Modifier.fillMaxWidth().height(TokeMedidas.BotonAltura).then(pulso.modificador),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = "Borrar negocio",
                style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 16.sp),
                color = paleta.rojo,
            )
        }
    }
}
