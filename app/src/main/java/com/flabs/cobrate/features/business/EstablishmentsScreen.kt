package com.flabs.cobrate.features.business

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flabs.cobrate.R
import com.flabs.cobrate.components.TokeIcons
import com.flabs.cobrate.core.data.CuentaStore
import com.flabs.cobrate.core.data.NegocioStore
import com.flabs.cobrate.core.logic.FeedbackManager
import com.flabs.cobrate.core.model.EstadoRelacion
import com.flabs.cobrate.core.model.Negocio
import com.flabs.cobrate.core.model.RolCuentaNegocio
import com.flabs.cobrate.features.acceso.BotonAcceso
import com.flabs.cobrate.features.acceso.BotonAtras
import com.flabs.cobrate.features.negocios.EditarNegocioHoja
import com.flabs.cobrate.ui.theme.PlusJakartaSans
import com.flabs.cobrate.ui.theme.TokeMedidas
import com.flabs.cobrate.ui.theme.TokePaleta
import com.flabs.cobrate.ui.theme.tokePaleta
import kotlinx.coroutines.launch

/**
 * "Mis locales": la lista de negocios de la cuenta. Quien es dueño de uno ve un lápiz para
 * cambiar su nombre, poner un máximo de trabajadores o borrarlo.
 */
@Composable
fun EstablishmentsScreen(
    negocioViewModel: NegocioViewModel,
    onNuevoNegocio: () -> Unit,
    onUnirmeNegocio: () -> Unit,
    onBack: () -> Unit,
    onBorrarNegocio: (String) -> Unit = {},
) {
    val paleta = tokePaleta()
    val scope = rememberCoroutineScope()
    val misNegocios by negocioViewModel.misNegocios.collectAsState()
    val misRelaciones by negocioViewModel.misRelaciones.collectAsState()
    val activoId by NegocioStore.negocioActivoId.collectAsState()
    val sesion by CuentaStore.sesionActual.collectAsState()

    var editando by remember { mutableStateOf<Negocio?>(null) }
    var maxInicial by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(paleta.fondo)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = TokeMedidas.MargenLateral),
    ) {
        Row(modifier = Modifier.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            BotonAtras(paleta, onBack)
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = stringResource(R.string.nombre_mis_locales),
                style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp),
                color = paleta.textoPrincipal,
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(TokeMedidas.EntreGrupos),
        ) {
            if (misNegocios.isEmpty()) {
                item {
                    Text(
                        text = "Aún no tienes negocios. Crea uno o únete con un código.",
                        style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Medium, fontSize = 15.sp),
                        color = paleta.textoSecundario,
                        modifier = Modifier.padding(vertical = 24.dp),
                    )
                }
            }
            items(misNegocios, key = { it.id }) { negocio ->
                val relacion = misRelaciones.find { it.negocioId == negocio.id }
                val esDueno = negocio.dueñoCuentaId == sesion?.id
                FilaLocal(
                    paleta = paleta,
                    negocio = negocio,
                    rol = if (relacion?.rol == RolCuentaNegocio.TRABAJADOR) "Trabajador" else "Dueño",
                    pausado = relacion?.estado == EstadoRelacion.PAUSADA,
                    esActual = negocio.id == activoId,
                    onElegir = { negocioViewModel.seleccionarNegocio(negocio.id) },
                    onEditar = if (esDueno) {
                        {
                            editando = negocio
                            maxInicial = null
                            scope.launch { maxInicial = NegocioStore.maxTrabajadores(negocio.id) }
                        }
                    } else {
                        null
                    },
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            BotonAcceso(paleta, "Nuevo negocio", principal = true, onClick = onNuevoNegocio)
            BotonAcceso(paleta, "Unirme a un negocio", principal = false, onClick = onUnirmeNegocio)
        }
    }

    editando?.let { negocio ->
        EditarNegocioHoja(
            paleta = paleta,
            negocio = negocio,
            maxInicial = maxInicial,
            onDismiss = { editando = null },
            onBorrar = {
                editando = null
                onBorrarNegocio(negocio.id)
            },
            onGuardar = { nombre, max, limpiar ->
                scope.launch {
                    FeedbackManager.setLoading(true, "Guardando cambios...")
                    val resultado = NegocioStore.editar(negocio.id, nombre, max, limpiar)
                    FeedbackManager.setLoading(false)
                    resultado.onSuccess {
                        FeedbackManager.showSuccess("Cambios guardados.")
                        editando = null
                    }.onFailure { FeedbackManager.showError(it) }
                }
            },
        )
    }
}

@Composable
private fun FilaLocal(
    paleta: TokePaleta,
    negocio: Negocio,
    rol: String,
    pausado: Boolean,
    esActual: Boolean,
    onElegir: () -> Unit,
    onEditar: (() -> Unit)?,
) {
    Surface(
        onClick = onElegir,
        shape = RoundedCornerShape(20.dp),
        color = paleta.tarjeta,
        border = BorderStroke(if (esActual) 2.dp else 1.dp, if (esActual) paleta.verde else paleta.borde),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(modifier = Modifier.padding(TokeMedidas.PaddingTarjeta), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(12.dp), color = paleta.tileFondo, modifier = Modifier.size(44.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(id = TokeIcons.Tienda),
                        contentDescription = null,
                        tint = paleta.textoPrincipal,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = negocio.nombre,
                    style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp),
                    color = paleta.textoPrincipal,
                    maxLines = 1,
                )
                Text(
                    text = if (pausado) "$rol · En pausa" else if (esActual) "$rol · Actual" else rol,
                    style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Medium, fontSize = 13.sp),
                    color = if (pausado) paleta.rojo else paleta.textoSecundario,
                )
            }
            if (onEditar != null) {
                Spacer(modifier = Modifier.width(10.dp))
                Surface(
                    onClick = onEditar,
                    shape = RoundedCornerShape(12.dp),
                    color = paleta.tileFondo,
                    border = BorderStroke(1.dp, paleta.bordeControl),
                    modifier = Modifier.size(TokeMedidas.BotonIcono),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(id = TokeIcons.Editar),
                            contentDescription = "Editar negocio",
                            tint = paleta.textoPrincipal,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
        }
    }
}
