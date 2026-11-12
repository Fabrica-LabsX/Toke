package com.flabs.cobrate.features.business

import com.flabs.cobrate.core.logic.LogSeguro
import com.flabs.cobrate.ui.theme.TokeMedidas

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.min

/**
 * Pantalla completa de edición manual de QR con asistencia del botón AIM.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrEditorScreen(
    imageUri: Uri,
    onCancel: () -> Unit,
    onConfirmCrop: (ByteArray) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var sourceBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var estaCargandoBitmap by remember { mutableStateOf(true) }

    // Estados de las transformaciones de la imagen en el lienzo
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // Cargar el bitmap original en segundo plano de forma segura
    LaunchedEffect(imageUri) {
        withContext(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(imageUri)
                if (inputStream != null) {
                    val original = BitmapFactory.decodeStream(inputStream)
                    sourceBitmap = original
                }
            } catch (e: Exception) {
                LogSeguro.e("QrEditorScreen", "Error decodificando imagen", e)
            } finally {
                estaCargandoBitmap = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajustar QR", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color.Black
    ) { innerPadding ->
        if (estaCargandoBitmap || sourceBitmap == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Preparando lienzo...", color = Color.White)
            }
            return@Scaffold
        }

        val bitmap = sourceBitmap!!

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val viewWidth = constraints.maxWidth.toFloat()
            val viewHeight = constraints.maxHeight.toFloat()

            // Definimos un marco de recorte cuadrado centrado en el Viewport
            val boxSize = min(viewWidth, viewHeight) * 0.75f
            val boxLeft = (viewWidth - boxSize) / 2f
            val boxTop = (viewHeight - boxSize) / 2f

            // Configuración inicial de encuadre centrado perfecto al arrancar
            LaunchedEffect(bitmap) {
                val scaleW = boxSize / bitmap.width.toFloat()
                val scaleH = boxSize / bitmap.height.toFloat()
                scale = max(scaleW, scaleH) * 1.1f
                offset = Offset(
                    x = (viewWidth - bitmap.width * scale) / 2f,
                    y = (viewHeight - bitmap.height * scale) / 2f
                )
            }

            // Lienzo Principal e Interacciones de Gestos
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { centroid, pan, zoom, _ ->
                            val oldScale = scale
                            val newScale = (scale * zoom).coerceIn(0.1f, 200f)
                            val factor = if (oldScale != 0f) newScale / oldScale else 1f
                            
                            // Ajuste de posición (Pan) y Zoom centrado en el gesto (Pinch-to-zoom)
                            scale = newScale
                            offset = (offset - centroid) * factor + centroid + pan
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // 1. Dibujar la imagen transformada
                    clipRect {
                        val currentImgWidth = (bitmap.width * scale).toInt()
                        val currentImgHeight = (bitmap.height * scale).toInt()
                        drawImage(
                            image = bitmap.asImageBitmap(),
                            dstOffset = IntOffset(offset.x.toInt(), offset.y.toInt()),
                            dstSize = IntSize(currentImgWidth, currentImgHeight)
                        )
                    }

                    // 2. Máscara de recorte semi-transparente con "hueco" transparente (Path EvenOdd)
                    // 2. Máscara de recorte semi-transparente con "hueco" transparente (Path EvenOdd)
                    // 2. Máscara de recorte semi-transparente con "hueco" transparente (Path EvenOdd)
                    val overlayPath = Path().apply {
                        addRect(Rect(0f, 0f, viewWidth, viewHeight))
                        addRect(Rect(boxLeft, boxTop, boxLeft + boxSize, boxTop + boxSize))
                        fillType = PathFillType.EvenOdd
                    }
                    drawPath(
                        path = overlayPath,
                        color = Color.Black.copy(alpha = 0.25f) // Oscurecimiento muy sutil para visibilidad total
                    )

                    // Línea técnica guía que dibuja la "Quiet Zone"
                    drawRect(
                        color = Color.Green.copy(alpha = 0.8f),
                        topLeft = Offset(boxLeft, boxTop),
                        size = Size(boxSize, boxSize),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }

            // Barra inferior con panel de controles profesional y limpio
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        brush = Brush.verticalGradient(
                            0f to Color.Transparent,
                            0.3f to Color.Black.copy(alpha = 0.5f),
                            1f to Color.Black.copy(alpha = 0.95f)
                        )
                    )
                    .padding(bottom = 32.dp, start = 24.dp, end = 24.dp, top = 60.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Panel de Zoom Estilizado
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val viewCenter = Offset(viewWidth / 2f, viewHeight / 2f)

                        IconButton(
                            onClick = { 
                                val oldScale = scale
                                scale = (scale / 1.2f).coerceIn(0.1f, 200f)
                                val ratio = if (oldScale != 0f) scale / oldScale else 1f
                                offset = (offset - viewCenter) * ratio + viewCenter
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Alejar", tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        
                        Slider(
                            value = scale.coerceIn(0.1f, 100f),
                            onValueChange = { newScale ->
                                val oldScale = scale
                                scale = newScale
                                val ratio = if (oldScale != 0f) scale / oldScale else 1f
                                offset = (offset - viewCenter) * ratio + viewCenter
                            },
                            valueRange = 0.1f..100f,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(
                                thumbColor = Color.White,
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                inactiveTrackColor = Color.Gray.copy(alpha = 0.5f)
                            )
                        )
                        
                        IconButton(
                            onClick = { 
                                val oldScale = scale
                                scale = (scale * 1.2f).coerceIn(0.1f, 200f)
                                val ratio = if (oldScale != 0f) scale / oldScale else 1f
                                offset = (offset - viewCenter) * ratio + viewCenter
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Acercar", tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Ajusta el QR dentro del recuadro verde",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        // Extracción de píxeles reales
                        val origLeft = ((boxLeft - offset.x) / scale).toInt()
                        val origTop = ((boxTop - offset.y) / scale).toInt()
                        val origWidth = (boxSize / scale).toInt()
                        val origHeight = (boxSize / scale).toInt()

                        val finalLeft = max(0, min(bitmap.width - 1, origLeft))
                        val finalTop = max(0, min(bitmap.height - 1, origTop))
                        val finalWidth = max(1, min(bitmap.width - finalLeft, origWidth))
                        val finalHeight = max(1, min(bitmap.height - finalTop, origHeight))

                        scope.launch(Dispatchers.IO) {
                            try {
                                val cropped = Bitmap.createBitmap(bitmap, finalLeft, finalTop, finalWidth, finalHeight)
                                val outputStream = ByteArrayOutputStream()
                                cropped.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                                val resultBytes = outputStream.toByteArray()

                                withContext(Dispatchers.Main) {
                                    onConfirmCrop(resultBytes)
                                }
                            } catch (e: Exception) {
                                LogSeguro.e("QrEditorScreen", "Fallo al extraer recorte", e)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(TokeMedidas.BotonAltura),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Confirmar Recorte", 
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
