package com.flabs.cobrate.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flabs.cobrate.core.data.CuentaStore
import com.flabs.cobrate.core.data.NegocioStore
import com.flabs.cobrate.core.data.PreferenciasAppStore
import com.flabs.cobrate.core.data.RelacionCuentaNegocioStore
import com.flabs.cobrate.core.model.EstadoRelacion
import com.flabs.cobrate.core.model.RolCuentaNegocio
import com.flabs.cobrate.ui.theme.PlusJakartaSans
import com.flabs.cobrate.ui.theme.ThemeMode
import com.flabs.cobrate.ui.theme.TokeMedidas
import com.flabs.cobrate.ui.theme.TokePaleta
import com.flabs.cobrate.ui.theme.rememberPulsacion
import com.flabs.cobrate.ui.theme.scrimSolido
import com.flabs.cobrate.ui.theme.tokePaleta
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch

/** Una opción del menú Más. [destructivo] la pinta en rojo (salir de un negocio, borrarlo). */
data class ItemMas(
    val label: String,
    @androidx.annotation.DrawableRes val icono: Int,
    val destructivo: Boolean = false,
    val onClick: () -> Unit,
)

/**
 * Menú inferior "Más": aspecto de la app, aviso sin conexión (solo administradores), la lista de
 * destinos y, siempre visibles al pie, Cerrar sesión y Apagar TOKE con su texto completo.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MasSheet(
    items: List<ItemMas>,
    onDismiss: () -> Unit,
    onCerrarSesion: () -> Unit,
    onApagarToke: () -> Unit
) {
    val paleta = tokePaleta()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val scope = rememberCoroutineScope()
    var cerrando by remember { mutableStateOf(false) }

    fun ejecutarConCierre(accion: () -> Unit) {
        if (cerrando) return
        cerrando = true
        scope.launch {
            try {
                sheetState.hide()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // Si hide() falla por otra razón, se continúa con el cierre normal.
            }
            ensureActive()
            onDismiss()
            accion()
        }
    }

    ModalBottomSheet(
        scrimColor = com.flabs.cobrate.ui.theme.tokePaleta().let { it.scrimSolido() },
        onDismissRequest = {
            if (!cerrando) {
                cerrando = true
                onDismiss()
            }
        },
        sheetState = sheetState,
        containerColor = paleta.fondo,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .verticalScroll(
                        state = rememberScrollState(),
                        enabled = sheetState.currentValue == SheetValue.Expanded
                    )
                    .padding(horizontal = TokeMedidas.MargenLateral),
                verticalArrangement = Arrangement.spacedBy(TokeMedidas.EntreGrupos),
            ) {
                SelectorAspecto(paleta)
                AvisoSinConexion(paleta)
                ListaDestinos(paleta, items) { item -> ejecutarConCierre(item.onClick) }
                Spacer(modifier = Modifier.height(4.dp))
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = TokeMedidas.MargenLateral, vertical = 12.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                BotonPie(paleta, "Cerrar sesión", rojo = false, habilitado = !cerrando) { ejecutarConCierre(onCerrarSesion) }
                BotonPie(paleta, "Apagar TOKE", rojo = true, habilitado = !cerrando) { ejecutarConCierre(onApagarToke) }
            }
        }
    }
}

@Composable
private fun TituloSeccion(paleta: TokePaleta, texto: String) {
    Text(
        text = texto.uppercase(),
        style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 0.8.sp),
        color = paleta.textoSecundario,
        modifier = Modifier.padding(start = 4.dp),
    )
}

@Composable
private fun SelectorAspecto(paleta: TokePaleta) {
    val actual by PreferenciasAppStore.themeMode.collectAsState()
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TituloSeccion(paleta, "Aspecto")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(TokeMedidas.BotonAltura)
                .background(paleta.tileFondo, RoundedCornerShape(9999.dp))
                .padding(4.dp),
        ) {
            OpcionAspecto(paleta, "Claro", actual == ThemeMode.LIGHT, Modifier.weight(1f)) {
                PreferenciasAppStore.setThemeMode(ThemeMode.LIGHT)
            }
            OpcionAspecto(paleta, "Oscuro", actual == ThemeMode.DARK, Modifier.weight(1f)) {
                PreferenciasAppStore.setThemeMode(ThemeMode.DARK)
            }
        }
    }
}

@Composable
private fun OpcionAspecto(paleta: TokePaleta, texto: String, elegido: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val pulso = rememberPulsacion()
    Surface(
        onClick = onClick,
        interactionSource = pulso.fuente,
        shape = RoundedCornerShape(9999.dp),
        color = if (elegido) paleta.seleccionFondo else androidx.compose.ui.graphics.Color.Transparent,
        modifier = modifier.height(TokeMedidas.BotonAltura - 8.dp).then(pulso.modificador),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = texto,
                style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = if (elegido) FontWeight.Bold else FontWeight.Medium, fontSize = 15.sp),
                color = if (elegido) paleta.seleccionTexto else paleta.textoSecundario,
            )
        }
    }
}


/** Interruptor del dueño: avisar cuando un cobro detectado no pudo llegar al equipo por falta de internet. */
@Composable
private fun AvisoSinConexion(paleta: TokePaleta) {
    val sesionActual by CuentaStore.sesionActual.collectAsState()
    val negocioActivoId by NegocioStore.negocioActivoId.collectAsState()
    val relaciones by RelacionCuentaNegocioStore.relaciones.collectAsState()

    val esAdminActivo = remember(sesionActual?.id, negocioActivoId, relaciones) {
        val uid = sesionActual?.id ?: return@remember false
        val negId = negocioActivoId ?: return@remember false
        relaciones.any {
            it.cuentaId == uid &&
                it.negocioId == negId &&
                it.rol == RolCuentaNegocio.ADMINISTRADOR &&
                it.estado == EstadoRelacion.ACTIVA
        }
    }
    if (!esAdminActivo) return

    val activo by PreferenciasAppStore.avisarCobroEnviado.collectAsState()
    Surface(
        onClick = { PreferenciasAppStore.setAvisarCobroEnviado(!activo) },
        shape = RoundedCornerShape(18.dp),
        color = paleta.tarjeta,
        border = BorderStroke(1.dp, paleta.borde),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Avisar si no hay conexión",
                    style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 15.sp),
                    color = paleta.textoPrincipal,
                )
                Text(
                    text = "Si detectas un cobro sin internet, verás “Cobro detectado, esperando red” hasta que llegue a tu equipo.",
                    style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Medium, fontSize = 13.sp, lineHeight = 18.sp),
                    color = paleta.textoSecundario,
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Switch(
                checked = activo,
                onCheckedChange = { PreferenciasAppStore.setAvisarCobroEnviado(it) },
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
}

@Composable
private fun ListaDestinos(paleta: TokePaleta, items: List<ItemMas>, onItem: (ItemMas) -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = paleta.tarjeta,
        border = BorderStroke(1.dp, paleta.borde),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column {
            items.forEachIndexed { indice, item ->
                FilaDestino(paleta, item) { onItem(item) }
                if (indice < items.lastIndex) {
                    HorizontalDivider(modifier = Modifier.padding(start = 68.dp), thickness = 1.dp, color = paleta.borde)
                }
            }
        }
    }
}

@Composable
private fun FilaDestino(paleta: TokePaleta, item: ItemMas, onClick: () -> Unit) {
    val color = if (item.destructivo) paleta.rojo else paleta.textoPrincipal
    val colorIcono = if (item.destructivo) androidx.compose.ui.graphics.Color.White else color
    Surface(onClick = onClick, color = androidx.compose.ui.graphics.Color.Transparent, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.height(58.dp).padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (item.destructivo) paleta.rojo else paleta.tileFondo, // sólido, sin transparencia
                modifier = Modifier.size(40.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(id = item.icono),
                        contentDescription = null,
                        tint = colorIcono,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = item.label,
                style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 16.sp),
                color = color,
                modifier = Modifier.weight(1f),
                maxLines = 1,
            )
            Icon(
                painter = painterResource(id = TokeIcons.ChevronDerecha),
                contentDescription = null,
                tint = paleta.textoSecundario,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Composable
private fun BotonPie(paleta: TokePaleta, texto: String, rojo: Boolean, habilitado: Boolean, onClick: () -> Unit) {
    val pulso = rememberPulsacion()
    Surface(
        onClick = onClick,
        enabled = habilitado,
        interactionSource = pulso.fuente,
        shape = RoundedCornerShape(9999.dp),
        color = if (rojo) paleta.rojo else paleta.textoPrincipal,
        modifier = Modifier.fillMaxWidth().height(TokeMedidas.BotonAltura).then(pulso.modificador),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = texto,
                style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 16.sp),
                color = if (rojo) androidx.compose.ui.graphics.Color.White else paleta.fondo,
                maxLines = 1,
            )
        }
    }
}
