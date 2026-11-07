package com.flabs.cobrate.features.business

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flabs.cobrate.components.TokeIcons
import com.flabs.cobrate.features.acceso.BotonAcceso
import com.flabs.cobrate.ui.theme.PlusJakartaSans
import com.flabs.cobrate.ui.theme.TokeMedidas
import com.flabs.cobrate.ui.theme.tokePaleta

/** Pasos de la subida del QR, en el orden en que ocurren. */
enum class QrUploadStage(val message: String) {
    PREPARANDO("Preparando QR..."),
    SUBIENDO("Subiendo QR de forma segura..."),
    GUARDANDO("Guardando configuración..."),
    PREPARANDO_PREVIEW("Preparando vista previa..."),
    LISTO("¡QR configurado!")
}

/** Progreso de la subida de un QR: el paso actual, o el error con "Reintentar" y "Volver". */
@Composable
fun QrUploadProgressScreen(
    currentStage: QrUploadStage,
    errorMessage: String?,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    val paleta = tokePaleta()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(paleta.fondo)
            .statusBarsPadding()
            .padding(horizontal = TokeMedidas.MargenLateral, vertical = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val titulo = if (errorMessage != null) "No pudimos subir tu QR" else currentStage.message
        Icon(
            painter = painterResource(
                id = when {
                    errorMessage != null -> TokeIcons.Advertencia
                    currentStage == QrUploadStage.LISTO -> TokeIcons.CheckVerde
                    else -> TokeIcons.MisQrs
                }
            ),
            contentDescription = null,
            tint = if (errorMessage == null && currentStage != QrUploadStage.LISTO) paleta.textoPrincipal else Color.Unspecified,
            modifier = Modifier.size(72.dp),
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = titulo,
            style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp),
            color = paleta.textoPrincipal,
            textAlign = TextAlign.Center,
        )
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage,
                style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Medium, fontSize = 15.sp, lineHeight = 21.sp),
                color = paleta.textoSecundario,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(24.dp))
            BotonAcceso(paleta, "Reintentar", principal = true, onClick = onRetry)
            Spacer(modifier = Modifier.height(10.dp))
            BotonAcceso(paleta, "Volver", principal = false, onClick = onBack)
        } else if (currentStage != QrUploadStage.LISTO) {
            Spacer(modifier = Modifier.height(20.dp))
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(0.7f).height(6.dp),
                color = paleta.verde,
                trackColor = paleta.bordeControl,
            )
        }
    }
}
