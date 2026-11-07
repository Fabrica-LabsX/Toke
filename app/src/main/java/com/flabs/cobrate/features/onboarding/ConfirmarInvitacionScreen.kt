package com.flabs.cobrate.features.onboarding

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flabs.cobrate.R
import com.flabs.cobrate.components.TokeIcons
import com.flabs.cobrate.core.data.CuentaStore
import com.flabs.cobrate.core.data.InvitacionRepository
import com.flabs.cobrate.core.data.NegocioStore
import com.flabs.cobrate.core.logic.ErrorMapper
import com.flabs.cobrate.core.logic.FeedbackManager
import com.flabs.cobrate.core.logic.LogSeguro
import com.flabs.cobrate.core.logic.TokeErrorCodes
import com.flabs.cobrate.core.model.InvitacionTrabajador
import com.flabs.cobrate.core.model.PreviewInvitacion
import com.flabs.cobrate.features.acceso.AccesoFondo
import com.flabs.cobrate.features.acceso.AccesoLogica
import com.flabs.cobrate.features.acceso.AvisoAcceso
import com.flabs.cobrate.features.acceso.BotonAcceso
import com.flabs.cobrate.features.acceso.BotonAtras
import com.flabs.cobrate.features.acceso.EspacioAcceso
import com.flabs.cobrate.features.acceso.TituloAcceso
import com.flabs.cobrate.ui.theme.PlusJakartaSans
import com.flabs.cobrate.ui.theme.TokePaleta
import com.flabs.cobrate.ui.theme.tokePaleta
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Pantalla "Negocio encontrado": muestra el negocio y quién invitó antes de aceptar la invitación.
 */
@Composable
fun ConfirmarInvitacionScreen(
    codigo: String,
    invitacion: InvitacionTrabajador? = null,
    onAceptada: () -> Unit,
    onVolver: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val repository = remember { InvitacionRepository() }
    val paleta = tokePaleta()

    var preview by remember { mutableStateOf<PreviewInvitacion?>(null) }
    var loading by remember { mutableStateOf(true) }
    var errorPreview by remember { mutableStateOf<String?>(null) }
    var errorAceptar by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(codigo) {
        loading = true
        errorPreview = null
        repository.obtenerPreview(codigo)
            .onSuccess {
                preview = it
                loading = false
                if (it == null) {
                    Log.w("TokeInvitacion", "preview devolvió null")
                    errorPreview = "Código de invitación no encontrado. Código de ayuda: ${TokeErrorCodes.INVITACION_NO_ENCONTRADA}"
                }
            }
            .onFailure { throwable ->
                LogSeguro.e("TokeInvitacion", "preview falló", throwable)
                errorPreview = ErrorMapper.translateInvitacionError(
                    throwable,
                    TokeErrorCodes.ERROR_VERIFICANDO_INVITACION
                )
                loading = false
            }
    }

    AccesoFondo(paleta = paleta) {
        BotonAtras(paleta, onVolver)
        TituloAcceso(paleta, stringResource(R.string.encontrado_titulo))

        if (loading) {
            Box(modifier = Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = paleta.verde)
            }
        }
        errorPreview?.let { MensajeError(it) }
        errorAceptar?.let { MensajeError(it) }

        if (!loading && errorPreview == null) {
            val negocio = preview?.negocioNombre ?: "Comercio"
            val dueno = remember(preview) {
                AccesoLogica.nombreDueno(preview?.duenoNombreVisible, preview?.duenoUsername)
            }
            TarjetaNegocio(paleta, negocio, stringResource(R.string.encontrado_te_invito, dueno))
            AvisoAcceso(
                paleta = paleta,
                titulo = stringResource(R.string.encontrado_verificacion_importante),
                texto = stringResource(R.string.encontrado_verificacion_texto),
            )
        }

        EspacioAcceso(4)
        BotonAcceso(paleta, stringResource(R.string.encontrado_aceptar), principal = !loading && errorPreview == null) {
            if (loading || errorPreview != null) return@BotonAcceso
            scope.launch {
                errorAceptar = null
                var cuentaId = CuentaStore.sesionActual.value?.id

                if (cuentaId == null) {
                    val cuentaReady = withTimeoutOrNull(8000L) {
                        CuentaStore.sesionActual.filter { it != null }.first()
                    }
                    cuentaId = cuentaReady?.id
                }

                if (cuentaId == null) {
                    errorAceptar = "Sesión del trabajador no disponible. Código de ayuda: ${TokeErrorCodes.SESION_TRABAJADOR_NO_DISPONIBLE}"
                    return@launch
                }

                repository.aceptarInvitacion(codigo)
                    .onSuccess { resultado ->
                        if (resultado.exito) {
                            CuentaStore.sincronizarDatos(cuentaId)
                            preview?.negocioId?.let { negId -> NegocioStore.seleccionarNegocio(negId) }
                            FeedbackManager.showSuccess(resultado.mensaje)
                            onAceptada()
                        } else {
                            errorAceptar = "${resultado.mensaje} (Código de ayuda: ${TokeErrorCodes.ERROR_ACEPTANDO_INVITACION})"
                        }
                    }
                    .onFailure { throwable ->
                        LogSeguro.e("TokeInvitacion", "aceptarInvitacion falló", throwable)
                        errorAceptar = ErrorMapper.translateInvitacionError(
                            throwable,
                            TokeErrorCodes.ERROR_ACEPTANDO_INVITACION
                        )
                    }
            }
        }
        BotonAcceso(paleta, stringResource(R.string.pausado_volver), principal = false, onClick = onVolver)
        Text(
            text = stringResource(R.string.encontrado_footer_nota),
            style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Medium, fontSize = 12.5.sp),
            color = paleta.textoSecundario,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        EspacioAcceso(8)
    }
}

@Composable
private fun TarjetaNegocio(paleta: TokePaleta, negocio: String, invito: String) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = paleta.tarjeta,
        border = BorderStroke(1.dp, paleta.borde),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(shape = RoundedCornerShape(22.dp), color = paleta.tileFondo, modifier = Modifier.size(80.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(id = TokeIcons.Tienda),
                        contentDescription = null,
                        tint = paleta.textoPrincipal,
                        modifier = Modifier.size(38.dp),
                    )
                }
            }
            Text(
                text = negocio,
                style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp),
                color = paleta.textoPrincipal,
                textAlign = TextAlign.Center,
            )
            Surface(shape = RoundedCornerShape(9999.dp), color = paleta.tileFondo) {
                Text(
                    text = invito,
                    style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 13.sp),
                    color = paleta.textoSecundario,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                )
            }
        }
    }
}

@Composable
private fun MensajeError(texto: String) {
    val rojo = Color(0xFFDC2626)
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = rojo.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, rojo.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = texto,
            modifier = Modifier.padding(16.dp),
            style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Medium, fontSize = 14.sp),
            color = rojo,
        )
    }
}
