package com.flabs.cobrate.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.flabs.cobrate.features.acceso.CampoAcceso
import com.flabs.cobrate.ui.theme.tokePaleta

/**
 * Confirmación de acciones importantes o destructivas. Si [textoQueHayQueEscribir] no es nulo,
 * el botón de confirmar solo se habilita cuando la persona escribe ese texto (por ejemplo el nombre del negocio).
 */
@Composable
fun ConfirmarAccionDialog(
    titulo: String,
    texto: String,
    textoConfirmar: String,
    textoCancelar: String = "Cancelar",
    destructivo: Boolean = true,
    textoQueHayQueEscribir: String? = null,
    cargando: Boolean = false,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit,
    alVolverAtras: (() -> Unit)? = null
) {
    var textoIngresado by remember { mutableStateOf("") }
    val paleta = tokePaleta()
    val habilitado = textoQueHayQueEscribir == null ||
        textoIngresado.trim().equals(textoQueHayQueEscribir.trim(), ignoreCase = true)

    DialogoToke(
        titulo = titulo,
        texto = texto,
        textoConfirmar = textoConfirmar,
        textoCancelar = textoCancelar,
        destructivo = destructivo,
        cargando = cargando,
        confirmarHabilitado = habilitado,
        onConfirmar = onConfirmar,
        onCancelar = alVolverAtras ?: onCancelar,
        contenidoExtra = if (textoQueHayQueEscribir != null) {
            {
                CampoAcceso(
                    paleta = paleta,
                    etiqueta = "Escribe el nombre del negocio para confirmar",
                    valor = textoIngresado,
                    placeholder = textoQueHayQueEscribir,
                    onCambia = { textoIngresado = it },
                )
            }
        } else {
            null
        },
    )
}
