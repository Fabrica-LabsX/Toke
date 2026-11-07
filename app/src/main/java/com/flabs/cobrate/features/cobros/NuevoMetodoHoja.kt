package com.flabs.cobrate.features.cobros

import androidx.activity.compose.BackHandler
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
import com.flabs.cobrate.components.IconoMetodoPago
import com.flabs.cobrate.components.TokeIcons
import com.flabs.cobrate.core.model.MetodoDeCobro
import com.flabs.cobrate.core.model.TipoMetodoCobro
import com.flabs.cobrate.ui.theme.PlusJakartaSans
import com.flabs.cobrate.ui.theme.TokePaleta

/**
 * Ventana "Vincular un método de cobro", en tres pasos pensados para quien no sabe nada de
 * cómo funciona por dentro:
 *  1. elegir la billetera o banco (las que ya existen salen apagadas con "(Ya existe)");
 *  2. si eligió Plin, elegir en qué billetera o banco recibe, con un aviso previo;
 *  3. escribir el número y, si quiere, el nombre del titular.
 *
 * Plin no se guarda como una billetera aparte: se crea la billetera o banco elegido.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevoMetodoHoja(
    paleta: TokePaleta,
    existentes: List<MetodoDeCobro>,
    onDismiss: () -> Unit,
    onConfirm: (tipo: TipoMetodoCobro, dato: String, titular: String) -> Unit,
) {
    var paso by remember { mutableStateOf(PasoNuevoMetodo.ELEGIR) }
    var tipo by remember { mutableStateOf<TipoMetodoCobro?>(null) }
    var viaPlin by remember { mutableStateOf(false) }
    var dato by remember { mutableStateOf("") }
    var titular by remember { mutableStateOf("") }

    fun atras() {
        when (paso) {
            PasoNuevoMetodo.DATOS -> paso = if (viaPlin) PasoNuevoMetodo.ELEGIR_PARA_PLIN else PasoNuevoMetodo.ELEGIR
            PasoNuevoMetodo.ELEGIR_PARA_PLIN -> {
                viaPlin = false
                paso = PasoNuevoMetodo.ELEGIR
            }
            PasoNuevoMetodo.ELEGIR -> onDismiss()
        }
    }

    BackHandler(enabled = paso != PasoNuevoMetodo.ELEGIR) { atras() }

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
            when (paso) {
                PasoNuevoMetodo.ELEGIR -> {
                    Encabezado(
                        paleta = paleta,
                        titulo = "Vincular un método de cobro",
                        subtitulo = "Elige cómo te pagan tus clientes",
                        conAtras = false,
                        onAtras = ::atras,
                        onCerrar = onDismiss,
                    )
                    NuevoMetodoLogica.gruposPrincipales().forEach { grupo ->
                        TituloGrupo(paleta, grupo.titulo)
                        grupo.tipos.forEach { opcion ->
                            val habilitada = NuevoMetodoLogica.puedeElegir(opcion, existentes)
                            OpcionMetodoFila(
                                paleta = paleta,
                                tipo = opcion,
                                subtitulo = NuevoMetodoLogica.subtituloDe(opcion),
                                habilitada = habilitada,
                                yaExiste = opcion != TipoMetodoCobro.PLIN && NuevoMetodoLogica.yaExiste(opcion, existentes),
                                onClick = {
                                    if (opcion == TipoMetodoCobro.PLIN) {
                                        viaPlin = true
                                        paso = PasoNuevoMetodo.ELEGIR_PARA_PLIN
                                    } else {
                                        viaPlin = false
                                        tipo = opcion
                                        paso = PasoNuevoMetodo.DATOS
                                    }
                                },
                            )
                        }
                    }
                }

                PasoNuevoMetodo.ELEGIR_PARA_PLIN -> {
                    Encabezado(
                        paleta = paleta,
                        titulo = "Plin",
                        subtitulo = "¿En qué banco o billetera recibes?",
                        conAtras = true,
                        onAtras = ::atras,
                        onCerrar = onDismiss,
                    )
                    AvisoPlin(paleta)
                    NuevoMetodoLogica.gruposParaPlin().forEach { grupo ->
                        TituloGrupo(paleta, grupo.titulo)
                        grupo.tipos.forEach { opcion ->
                            val existe = NuevoMetodoLogica.yaExiste(opcion, existentes)
                            OpcionMetodoFila(
                                paleta = paleta,
                                tipo = opcion,
                                subtitulo = null,
                                habilitada = !existe,
                                yaExiste = existe,
                                onClick = {
                                    tipo = opcion
                                    paso = PasoNuevoMetodo.DATOS
                                },
                            )
                        }
                    }
                }

                PasoNuevoMetodo.DATOS -> {
                    val elegido = tipo
                    if (elegido != null) {
                        val nombre = MetodosCobroLogica.nombreLegible(elegido)
                        Encabezado(
                            paleta = paleta,
                            titulo = if (viaPlin) "Tu Plin en $nombre" else "Datos de $nombre",
                            subtitulo = "Solo falta un paso",
                            conAtras = true,
                            onAtras = ::atras,
                            onCerrar = onDismiss,
                        )
                        ResumenElegido(paleta, elegido, viaPlin)
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
                        BotonGuardar(
                            paleta = paleta,
                            habilitado = NuevoMetodoLogica.datosValidos(dato),
                            onClick = { onConfirm(elegido, dato.trim(), titular.trim()) },
                        )
                        Text(
                            text = "Después podrás cargar la foto de tu QR.",
                            style = TextStyle(
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp,
                            ),
                            color = paleta.textoSecundario,
                            modifier = Modifier.padding(horizontal = 4.dp),
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun Encabezado(
    paleta: TokePaleta,
    titulo: String,
    subtitulo: String,
    conAtras: Boolean,
    onAtras: () -> Unit,
    onCerrar: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (conAtras) {
            BotonCuadrado(paleta, TokeIcons.FlechaAtras, "Volver", onAtras)
            Spacer(modifier = Modifier.width(12.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = titulo,
                style = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 21.sp,
                    letterSpacing = (-0.4).sp,
                ),
                color = paleta.textoPrincipal,
            )
            Text(
                text = subtitulo,
                style = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                ),
                color = paleta.textoSecundario,
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        BotonCuadrado(paleta, TokeIcons.Cerrar, "Cerrar", onCerrar)
    }
}

@Composable
private fun BotonCuadrado(paleta: TokePaleta, icono: Int, descripcion: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = paleta.tarjeta,
        border = BorderStroke(1.dp, paleta.borde),
        modifier = Modifier.size(42.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(id = icono),
                contentDescription = descripcion,
                tint = paleta.textoPrincipal,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun ResumenElegido(paleta: TokePaleta, tipo: TipoMetodoCobro, viaPlin: Boolean) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = paleta.tarjeta,
        border = BorderStroke(1.dp, paleta.borde),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            IconoMetodoPago(tipo = tipo, tamano = 44.dp)
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = MetodosCobroLogica.nombreLegible(tipo),
                    style = TextStyle(
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp,
                    ),
                    color = paleta.textoPrincipal,
                )
                if (viaPlin) {
                    Text(
                        text = "Para recibir pagos Plin",
                        style = TextStyle(
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                        ),
                        color = paleta.textoSecundario,
                    )
                }
            }
        }
    }
}

@Composable
private fun BotonGuardar(paleta: TokePaleta, habilitado: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        enabled = habilitado,
        shape = RoundedCornerShape(9999.dp),
        color = if (habilitado) paleta.verde else paleta.bordeControl,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = "Guardar método",
                style = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                ),
                color = if (habilitado) paleta.verdeTexto else paleta.textoSecundario,
            )
        }
    }
}
