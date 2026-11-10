package com.flabs.cobrate.features.actualizacion

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flabs.cobrate.R
import com.flabs.cobrate.features.acceso.BotonAcceso
import com.flabs.cobrate.features.acceso.LogoToke
import com.flabs.cobrate.ui.theme.PlusJakartaSans
import com.flabs.cobrate.ui.theme.TokeMedidas
import com.flabs.cobrate.ui.theme.TokePaleta
import com.flabs.cobrate.ui.theme.tokePaleta
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File

private sealed interface FaseDescarga {
    data object Inicio : FaseDescarga
    data class Descargando(val p: ProgresoDescarga) : FaseDescarga
    data class Lista(val apk: File) : FaseDescarga
    data class Error(val motivo: MotivoFallo) : FaseDescarga
    data object Cancelada : FaseDescarga
}

private fun estilo(peso: FontWeight, tam: Int) = TextStyle(fontFamily = PlusJakartaSans, fontWeight = peso, fontSize = tam.sp)

/**
 * Pantalla de actualización. Si es [obligatoria] ocupa toda la app: no hay "Después" y el botón Atrás no hace nada
 * (la única salida es actualizar o cerrar TOKE con "Salir"). Si es opcional, se puede posponer.
 */
@Composable
fun ActualizacionPantalla(
    manifiesto: ManifiestoVersion,
    obligatoria: Boolean,
    onDespues: () -> Unit,
    onSalir: () -> Unit,
) {
    val paleta = tokePaleta()
    val contexto = LocalContext.current
    val scope = rememberCoroutineScope()
    val descargador = remember { ApkDescargador(contexto) }
    var fase by remember { mutableStateOf<FaseDescarga>(FaseDescarga.Inicio) }
    var trabajo by remember { mutableStateOf<Job?>(null) }
    var necesitaPermiso by remember { mutableStateOf(false) }
    val instalada = remember { ActualizacionStore.versionInstalada(contexto) }

    // Bloqueo real: Atrás no deja salir de una actualización obligatoria; una opcional solo se pospone.
    BackHandler(enabled = true) { if (!obligatoria && fase !is FaseDescarga.Descargando) onDespues() }

    fun empezar() {
        trabajo?.cancel()
        fase = FaseDescarga.Descargando(ProgresoDescarga(0, manifiesto.apkSizeBytes, 0, null))
        trabajo = scope.launch {
            val r = descargador.descargar(manifiesto) { p -> fase = FaseDescarga.Descargando(p) }
            fase = r.fold(
                onSuccess = { FaseDescarga.Lista(it) },
                onFailure = { FaseDescarga.Error((it as? FalloDescarga)?.motivo ?: MotivoFallo.RED) },
            )
        }
    }

    fun cancelar() {
        trabajo?.cancel()
        fase = FaseDescarga.Cancelada
    }

    fun instalar(apk: File) {
        if (descargador.puedeInstalar()) {
            necesitaPermiso = false
            descargador.instalar(apk)
        } else {
            necesitaPermiso = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(paleta.fondo)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = TokeMedidas.MargenLateral, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(TokeMedidas.EntreGrupos),
    ) {
        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(TokeMedidas.EntreGrupos),
        ) {
            LogoToke(paleta, tamano = 52, descripcion = "TOKE")
            Text(
                text = stringResource(if (obligatoria) R.string.actualizacion_titulo_obligatoria else R.string.actualizacion_titulo_opcional),
                style = estilo(FontWeight.ExtraBold, 24),
                color = paleta.textoPrincipal,
            )
            Text(
                text = stringResource(if (obligatoria) R.string.actualizacion_mensaje_obligatorio else R.string.actualizacion_mensaje_opcional),
                style = estilo(FontWeight.Medium, 15),
                color = paleta.textoSecundario,
            )
            TarjetaVersiones(paleta, instalada, manifiesto)
            when (val f = fase) {
                is FaseDescarga.Descargando -> TarjetaProgreso(paleta, f.p)
                is FaseDescarga.Error -> Aviso(paleta, stringResource(textoError(f.motivo)), error = true)
                FaseDescarga.Cancelada -> Aviso(paleta, stringResource(R.string.actualizacion_cancelada), error = false)
                is FaseDescarga.Lista -> Aviso(paleta, stringResource(R.string.actualizacion_lista), error = false)
                FaseDescarga.Inicio -> Unit
            }
            if (necesitaPermiso) {
                Aviso(paleta, stringResource(R.string.actualizacion_permiso_explicacion), error = false)
            }
        }

        val f = fase
        when {
            f is FaseDescarga.Descargando -> BotonAcceso(paleta, stringResource(R.string.actualizacion_cancelar), principal = false) { cancelar() }
            f is FaseDescarga.Lista && necesitaPermiso -> {
                BotonAcceso(paleta, stringResource(R.string.actualizacion_dar_permiso), principal = true) { descargador.abrirPermisoDeInstalacion() }
                BotonAcceso(paleta, stringResource(R.string.actualizacion_instalar), principal = false) { instalar(f.apk) }
            }
            f is FaseDescarga.Lista -> BotonAcceso(paleta, stringResource(R.string.actualizacion_instalar), principal = true) { instalar(f.apk) }
            f is FaseDescarga.Error || f is FaseDescarga.Cancelada ->
                BotonAcceso(paleta, stringResource(R.string.actualizacion_reintentar), principal = true) { empezar() }
            else -> BotonAcceso(paleta, stringResource(R.string.actualizacion_descargar), principal = true) { empezar() }
        }
        if (fase !is FaseDescarga.Descargando) {
            if (obligatoria) {
                BotonAcceso(paleta, stringResource(R.string.actualizacion_salir), principal = false) { onSalir() }
            } else {
                BotonAcceso(paleta, stringResource(R.string.actualizacion_despues), principal = false) { onDespues() }
            }
        }
    }
}

private fun textoError(m: MotivoFallo): Int = when (m) {
    MotivoFallo.RED -> R.string.actualizacion_error_red
    MotivoFallo.TAMANO, MotivoFallo.HASH -> R.string.actualizacion_error_archivo
    MotivoFallo.PAQUETE_O_FIRMA -> R.string.actualizacion_error_firma
    MotivoFallo.ALMACENAMIENTO -> R.string.actualizacion_error_espacio
}

@Composable
private fun TarjetaVersiones(paleta: TokePaleta, instalada: Int, m: ManifiestoVersion) {
    Surface(shape = RoundedCornerShape(16.dp), color = paleta.tarjeta, border = BorderStroke(1.dp, paleta.borde)) {
        Column(modifier = Modifier.fillMaxWidth().padding(TokeMedidas.PaddingTarjeta), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FilaDato(paleta, stringResource(R.string.actualizacion_version_actual), "$instalada")
            FilaDato(paleta, stringResource(R.string.actualizacion_version_nueva), "${m.latestVersionName} (${m.latestVersionCode})")
            FilaDato(paleta, stringResource(R.string.actualizacion_tamano), ActualizacionLogica.formatoMb(m.apkSizeBytes) + " MB")
        }
    }
}

@Composable
private fun FilaDato(paleta: TokePaleta, etiqueta: String, valor: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(etiqueta, style = estilo(FontWeight.Medium, 14), color = paleta.textoSecundario)
        Text(valor, style = estilo(FontWeight.Bold, 14), color = paleta.textoPrincipal)
    }
}

@Composable
private fun TarjetaProgreso(paleta: TokePaleta, p: ProgresoDescarga) {
    Surface(shape = RoundedCornerShape(16.dp), color = paleta.tarjeta, border = BorderStroke(1.dp, paleta.borde)) {
        Column(modifier = Modifier.fillMaxWidth().padding(TokeMedidas.PaddingTarjeta), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${p.porcentaje} %", style = estilo(FontWeight.ExtraBold, 22), color = paleta.textoPrincipal)
                Text(
                    "${ActualizacionLogica.formatoMb(p.descargados)} / ${ActualizacionLogica.formatoMb(p.total)} MB",
                    style = estilo(FontWeight.SemiBold, 14), color = paleta.textoSecundario,
                )
            }
            Box(modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(9999.dp)).background(paleta.tileFondo)) {
                Box(modifier = Modifier.fillMaxWidth(p.porcentaje / 100f).height(8.dp).clip(RoundedCornerShape(9999.dp)).background(paleta.verde))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(ActualizacionLogica.formatoVelocidad(p.bytesPorSegundo), style = estilo(FontWeight.Medium, 13), color = paleta.textoSecundario)
                Text(
                    stringResource(R.string.actualizacion_restante, ActualizacionLogica.formatoTiempo(p.segundosRestantes)),
                    style = estilo(FontWeight.Medium, 13), color = paleta.textoSecundario,
                )
            }
        }
    }
}

@Composable
private fun Aviso(paleta: TokePaleta, texto: String, error: Boolean) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (error) paleta.rojoFondo else paleta.tileFondo,
        border = BorderStroke(1.dp, if (error) paleta.rojoBorde else paleta.borde),
    ) {
        Text(
            text = texto,
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            style = estilo(FontWeight.Medium, 14),
            color = if (error) paleta.rojo else paleta.textoPrincipal,
        )
    }
}
