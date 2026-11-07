package com.flabs.cobrate.features.cobros
import com.flabs.cobrate.ui.theme.TokePaletaClara

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.flabs.cobrate.components.TokeIcons
import com.flabs.cobrate.ui.theme.PlusJakartaSans
import com.flabs.cobrate.ui.theme.TokePaleta

/** Imagen del QR guardado. Si aún no se descargó, la pide con [onObtener] y muestra un indicador. */
@Composable
fun QrImagen(
    qrPath: String,
    qrCache: Map<String, ByteArray>,
    onObtener: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val bytes = qrCache[qrPath]

    LaunchedEffect(qrPath) {
        if (bytes == null) onObtener(qrPath)
    }

    Box(modifier = modifier.clip(RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
        if (bytes == null) {
            CircularProgressIndicator(modifier = Modifier.size(36.dp))
        } else {
            AsyncImage(
                model = ImageRequest.Builder(context).data(bytes).crossfade(true).build(),
                contentDescription = "QR de cobro",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
            )
        }
    }
}

/**
 * Cuadro del QR dentro de la tarjeta. Con QR lo muestra y al tocarlo se amplía; sin QR invita a
 * cargarlo (solo si se puede editar).
 */
@Composable
fun QrCuadro(
    paleta: TokePaleta,
    qrPath: String?,
    qrCache: Map<String, ByteArray>,
    puedeCargar: Boolean,
    onObtener: (String) -> Unit,
    onAmpliar: () -> Unit,
    onCargar: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, paleta.borde),
        modifier = Modifier
            .fillMaxWidth(0.72f)
            .aspectRatio(1f),
    ) {
        if (qrPath != null) {
            QrImagen(
                qrPath = qrPath,
                qrCache = qrCache,
                onObtener = onObtener,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
                    .clickable(onClick = onAmpliar),
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (puedeCargar) Modifier.clickable(onClick = onCargar) else Modifier),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                // La cámara invita a subir el QR: solo se muestra a quien puede cargarlo.
                if (puedeCargar) {
                    Icon(
                        painter = painterResource(id = TokeIcons.Camara),
                        contentDescription = null,
                        tint = TokePaletaClara.textoSecundario,
                        modifier = Modifier.size(34.dp),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Text(
                    text = if (puedeCargar) "Toca para cargar tu QR" else "El dueño aún no cargó el QR",
                    style = TextStyle(
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                    ),
                    color = TokePaletaClara.textoSecundario,
                )
            }
        }
    }
}

/** QR a pantalla completa para que el cliente lo escanee. Un toque en cualquier parte lo cierra. */
@Composable
fun QrAmpliadoDialogo(
    qrPath: String,
    qrCache: Map<String, ByteArray>,
    onObtener: (String) -> Unit,
    onCerrar: () -> Unit,
) {
    Dialog(onDismissRequest = onCerrar, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onCerrar),
            color = Color.Black.copy(alpha = 0.92f),
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().padding(24.dp)) {
                QrImagen(
                    qrPath = qrPath,
                    qrCache = qrCache,
                    onObtener = onObtener,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .background(Color.White, RoundedCornerShape(16.dp)),
                )
            }
        }
    }
}
