package com.flabs.cobrate.features.cuenta

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.ui.res.painterResource
import com.flabs.cobrate.components.TokeIcons
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flabs.cobrate.R
import com.flabs.cobrate.core.data.CuentaStore
import com.flabs.cobrate.core.data.SupabaseAuthBridge
import com.flabs.cobrate.features.acceso.AccesoFondo
import com.flabs.cobrate.features.acceso.BotonAcceso
import com.flabs.cobrate.features.acceso.BotonAtras
import com.flabs.cobrate.features.acceso.CampoAcceso
import com.flabs.cobrate.features.acceso.TituloAcceso
import com.flabs.cobrate.ui.theme.PlusJakartaSans
import com.flabs.cobrate.ui.theme.TokeMedidas
import com.flabs.cobrate.ui.theme.TokePaleta
import com.flabs.cobrate.ui.theme.tokePaleta
import kotlinx.coroutines.launch

private enum class Vista { PRINCIPAL, NOMBRE, CORREO, CODIGO_CORREO, PASSWORD, ELIMINAR }

private fun estilo(peso: FontWeight, tam: Int) = TextStyle(fontFamily = PlusJakartaSans, fontWeight = peso, fontSize = tam.sp)

/**
 * Mi cuenta: nombre, correo y su verificación, cambio de correo y de contraseña, eliminación de la cuenta y enlaces legales.
 * Muestra solo lo que la cuenta realmente tiene: las cuentas de trabajador (sesión anónima, sin correo) no ven correo ni contraseña.
 */
@Composable
fun MiCuentaScreen(onVolver: () -> Unit, onCuentaEliminada: () -> Unit) {
    val paleta = tokePaleta()
    val contexto = LocalContext.current
    val scope = rememberCoroutineScope()
    val usuario by SupabaseAuthBridge.sesionActual.collectAsState()
    val cuenta by CuentaStore.sesionActual.collectAsState()
    val correo = usuario?.email
    val tipo = MiCuentaLogica.tipo(correo)
    val verificado = usuario?.emailConfirmedAt != null

    var vista by rememberSaveable { mutableStateOf(Vista.PRINCIPAL) }
    var mensaje by remember { mutableStateOf<String?>(null) }
    var ocupado by remember { mutableStateOf(false) }

    // Campos (viven aquí para sobrevivir al cambio de vista; las contraseñas nunca se guardan ni se persisten).
    var nombreNuevo by rememberSaveable { mutableStateOf("") }
    var correoNuevo by rememberSaveable { mutableStateOf("") }
    var codigo by rememberSaveable { mutableStateOf("") }
    var passActual by remember { mutableStateOf("") }
    var passNueva by remember { mutableStateOf("") }
    var passConfirma by remember { mutableStateOf("") }
    var frase by remember { mutableStateOf("") }
    var verPass by remember { mutableStateOf(false) }
    var pidioCerrarNegocios by remember { mutableStateOf<Int?>(null) }

    fun limpiarCampos() { passActual = ""; passNueva = ""; passConfirma = ""; frase = ""; codigo = ""; mensaje = null; pidioCerrarNegocios = null }
    fun ir(v: Vista) { limpiarCampos(); vista = v }
    fun abrir(url: String) {
        runCatching { contexto.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
    }

    BackHandler(enabled = vista != Vista.PRINCIPAL && !ocupado) { ir(Vista.PRINCIPAL) }

    val msgSinRed = stringResource(R.string.cuenta_error_red)
    val msgGeneral = stringResource(R.string.cuenta_error_general)
    val msgCorreoEnviado = stringResource(R.string.cuenta_correo_enviado)
    val msgPassOk = stringResource(R.string.cuenta_password_ok)
    val msgPassMala = stringResource(R.string.cuenta_password_incorrecta)
    val msgCorreoOk = stringResource(R.string.cuenta_correo_actualizado)
    val msgCodigoMalo = stringResource(R.string.cuenta_codigo_invalido)
    val msgCorreoInvalido = stringResource(R.string.cuenta_correo_invalido)
    val msgCorreoIgual = stringResource(R.string.cuenta_correo_igual)
    val msgNombreOk = stringResource(R.string.cuenta_nombre_ok)

    AccesoFondo(paleta = paleta) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BotonAtras(paleta) { if (vista == Vista.PRINCIPAL) onVolver() else if (!ocupado) ir(Vista.PRINCIPAL) }
        }
        when (vista) {
            Vista.PRINCIPAL -> {
                TituloAcceso(paleta, stringResource(R.string.cuenta_titulo))
                Tarjeta(paleta) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.weight(1f)) {
                            Fila(paleta, stringResource(R.string.cuenta_nombre), cuenta?.username?.takeIf { it.isNotBlank() } ?: stringResource(R.string.cuenta_sin_nombre))
                        }
                        Surface(
                            onClick = { nombreNuevo = cuenta?.username.orEmpty(); ir(Vista.NOMBRE) },
                            shape = RoundedCornerShape(12.dp),
                            color = paleta.tileFondo,
                            border = BorderStroke(1.dp, paleta.bordeControl),
                            modifier = Modifier.size(TokeMedidas.BotonIcono),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    painter = painterResource(id = TokeIcons.Editar),
                                    contentDescription = stringResource(R.string.cuenta_editar_nombre),
                                    tint = paleta.textoPrincipal,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    }
                    if (tipo == TipoCuenta.CON_CORREO) {
                        Fila(paleta, stringResource(R.string.cuenta_correo), correo.orEmpty())
                        Fila(
                            paleta, stringResource(R.string.cuenta_estado_correo),
                            stringResource(if (verificado) R.string.cuenta_correo_verificado else R.string.cuenta_correo_sin_verificar),
                        )
                    } else {
                        Text(stringResource(R.string.cuenta_sin_correo_explicacion), style = estilo(FontWeight.Medium, 13), color = paleta.textoSecundario)
                    }
                }
                mensaje?.let { Aviso(paleta, it, error = false) }
                if (tipo == TipoCuenta.CON_CORREO) {
                    BotonAcceso(paleta, stringResource(R.string.cuenta_actualizar_correo), principal = false) { ir(Vista.CORREO) }
                    BotonAcceso(paleta, stringResource(R.string.cuenta_cambiar_password), principal = false) { ir(Vista.PASSWORD) }
                }
                Tarjeta(paleta) {
                    Enlace(paleta, stringResource(R.string.cuenta_privacidad)) { abrir(contexto.getString(R.string.url_privacidad)) }
                    Enlace(paleta, stringResource(R.string.cuenta_terminos)) { abrir(contexto.getString(R.string.url_terminos)) }
                    Enlace(paleta, stringResource(R.string.cuenta_pagina_eliminar)) { abrir(contexto.getString(R.string.url_eliminar_cuenta)) }
                }
                BotonAcceso(paleta, stringResource(R.string.cuenta_eliminar), principal = false) { ir(Vista.ELIMINAR) }
            }

            Vista.NOMBRE -> {
                TituloAcceso(paleta, stringResource(R.string.cuenta_editar_nombre), stringResource(R.string.cuenta_editar_nombre_ayuda))
                CampoAcceso(paleta, stringResource(R.string.cuenta_nombre_nuevo), nombreNuevo, "", { nombreNuevo = it.take(MiCuentaLogica.NOMBRE_MAX) })
                mensaje?.let { Aviso(paleta, it, error = true) }
                val puedeGuardar = MiCuentaLogica.nombreValido(nombreNuevo, cuenta?.username) && !ocupado
                BotonAcceso(paleta, stringResource(R.string.cuenta_guardar_nombre), principal = puedeGuardar) {
                    if (!puedeGuardar) return@BotonAcceso
                    ocupado = true
                    scope.launch {
                        CuentaStore.actualizarNombre(nombreNuevo)
                            .onSuccess { limpiarCampos(); vista = Vista.PRINCIPAL; mensaje = msgNombreOk }
                            .onFailure { mensaje = if (esRed(it)) msgSinRed else msgGeneral }
                        ocupado = false
                    }
                }
            }

            Vista.CORREO -> {
                TituloAcceso(paleta, stringResource(R.string.cuenta_actualizar_correo), stringResource(R.string.cuenta_actualizar_correo_ayuda))
                CampoAcceso(paleta, stringResource(R.string.cuenta_correo_nuevo), correoNuevo, "nuevo@correo.com", { correoNuevo = it }, KeyboardType.Email)
                mensaje?.let { Aviso(paleta, it, error = true) }
                BotonAcceso(paleta, stringResource(R.string.cuenta_enviar_codigo), principal = !ocupado) {
                    if (ocupado) return@BotonAcceso
                    val err = MiCuentaLogica.errorCorreoNuevo(correoNuevo, correo)
                    if (err != null) {
                        mensaje = when (err) {
                            ErrorCorreo.IGUAL_AL_ACTUAL -> msgCorreoIgual
                            else -> msgCorreoInvalido
                        }
                        return@BotonAcceso
                    }
                    ocupado = true
                    scope.launch {
                        SupabaseAuthBridge.solicitarCambioCorreo(correoNuevo)
                            .onSuccess { codigo = ""; mensaje = null; vista = Vista.CODIGO_CORREO }
                            .onFailure { mensaje = if (esRed(it)) msgSinRed else msgGeneral }
                        ocupado = false
                    }
                }
            }

            Vista.CODIGO_CORREO -> {
                TituloAcceso(paleta, stringResource(R.string.cuenta_confirmar_correo), stringResource(R.string.cuenta_confirmar_correo_ayuda, correoNuevo.trim()))
                CampoAcceso(paleta, stringResource(R.string.cuenta_codigo), codigo, "123456", { codigo = it.filter(Char::isDigit).take(8) }, KeyboardType.Number)
                mensaje?.let { Aviso(paleta, it, error = true) }
                BotonAcceso(paleta, stringResource(R.string.cuenta_confirmar), principal = codigo.length >= 6 && !ocupado) {
                    if (ocupado || codigo.length < 6) return@BotonAcceso
                    ocupado = true
                    scope.launch {
                        SupabaseAuthBridge.confirmarCambioCorreo(correoNuevo, codigo)
                            .onSuccess {
                                SupabaseAuthBridge.refrescarUsuario()
                                correoNuevo = ""; limpiarCampos(); vista = Vista.PRINCIPAL; mensaje = msgCorreoOk
                            }
                            .onFailure { mensaje = if (esRed(it)) msgSinRed else msgCodigoMalo }
                        ocupado = false
                    }
                }
                // Si el usuario confirmó con el enlace del correo, aquí actualizamos lo mostrado.
                BotonAcceso(paleta, stringResource(R.string.cuenta_ya_confirme), principal = false) {
                    if (ocupado) return@BotonAcceso
                    ocupado = true
                    scope.launch {
                        val u = SupabaseAuthBridge.refrescarUsuario().getOrNull()
                        if (u?.email.equals(correoNuevo.trim(), ignoreCase = true)) {
                            correoNuevo = ""; limpiarCampos(); vista = Vista.PRINCIPAL; mensaje = msgCorreoOk
                        } else {
                            mensaje = msgCorreoEnviado
                        }
                        ocupado = false
                    }
                }
            }

            Vista.PASSWORD -> {
                TituloAcceso(paleta, stringResource(R.string.cuenta_cambiar_password), stringResource(R.string.cuenta_cambiar_password_ayuda))
                CampoAcceso(paleta, stringResource(R.string.cuenta_password_actual), passActual, "", { passActual = it }, esPassword = true, visible = verPass, onAlternarVisible = { verPass = !verPass })
                CampoAcceso(paleta, stringResource(R.string.cuenta_password_nueva), passNueva, "", { passNueva = it }, esPassword = true, visible = verPass, onAlternarVisible = { verPass = !verPass })
                CampoAcceso(paleta, stringResource(R.string.cuenta_password_confirmar), passConfirma, "", { passConfirma = it }, esPassword = true, visible = verPass, onAlternarVisible = { verPass = !verPass })
                mensaje?.let { Aviso(paleta, it, error = true) }
                BotonAcceso(paleta, stringResource(R.string.cuenta_guardar_password), principal = !ocupado) {
                    if (ocupado) return@BotonAcceso
                    val err = MiCuentaLogica.errorCambioPassword(passActual, passNueva, passConfirma)
                    if (err != null) { mensaje = err; return@BotonAcceso }
                    ocupado = true
                    scope.launch {
                        SupabaseAuthBridge.cambiarPassword(correo.orEmpty(), passActual, passNueva)
                            .onSuccess { limpiarCampos(); vista = Vista.PRINCIPAL; mensaje = msgPassOk }
                            .onFailure { mensaje = if (esRed(it)) msgSinRed else msgPassMala }
                        ocupado = false
                    }
                }
            }

            Vista.ELIMINAR -> {
                TituloAcceso(paleta, stringResource(R.string.cuenta_eliminar), stringResource(R.string.cuenta_eliminar_consecuencias))
                Tarjeta(paleta) {
                    Text(stringResource(R.string.cuenta_eliminar_lista), style = estilo(FontWeight.Medium, 14), color = paleta.textoPrincipal)
                }
                if (tipo == TipoCuenta.CON_CORREO) {
                    CampoAcceso(paleta, stringResource(R.string.cuenta_eliminar_password), passActual, "", { passActual = it }, esPassword = true, visible = verPass, onAlternarVisible = { verPass = !verPass })
                } else {
                    CampoAcceso(paleta, stringResource(R.string.cuenta_eliminar_frase), frase, MiCuentaLogica.FRASE_ELIMINAR, { frase = it })
                }
                pidioCerrarNegocios?.let {
                    Aviso(paleta, stringResource(R.string.cuenta_eliminar_negocios, it), error = true)
                }
                mensaje?.let { Aviso(paleta, it, error = true) }
                val puede = MiCuentaLogica.puedeConfirmarEliminacion(tipo, passActual, frase) && !ocupado
                BotonAcceso(
                    paleta,
                    stringResource(if (pidioCerrarNegocios != null) R.string.cuenta_eliminar_todo else R.string.cuenta_eliminar_confirmar),
                    principal = puede,
                ) {
                    if (!puede) return@BotonAcceso
                    ocupado = true
                    scope.launch {
                        val r = SupabaseAuthBridge.eliminarCuenta(
                            password = passActual.takeIf { tipo == TipoCuenta.CON_CORREO },
                            confirmacion = frase.trim().uppercase().takeIf { tipo == TipoCuenta.ANONIMA },
                            cerrarNegocios = pidioCerrarNegocios != null,
                        )
                        when (r) {
                            ResultadoEliminacion.Eliminada -> onCuentaEliminada()
                            ResultadoEliminacion.ContrasenaIncorrecta -> mensaje = msgPassMala
                            is ResultadoEliminacion.TieneNegocios -> { mensaje = null; pidioCerrarNegocios = r.cantidad }
                            ResultadoEliminacion.SinRed -> mensaje = msgSinRed
                            ResultadoEliminacion.ErrorServidor -> mensaje = msgGeneral
                        }
                        ocupado = false
                    }
                }
            }
        }
    }
}

private fun esRed(t: Throwable): Boolean = com.flabs.cobrate.core.logic.AuthErrorUtils.esErrorDeRed(t)

@Composable
private fun Tarjeta(paleta: TokePaleta, contenido: @Composable () -> Unit) {
    Surface(shape = RoundedCornerShape(16.dp), color = paleta.tarjeta, border = BorderStroke(1.dp, paleta.borde)) {
        Column(modifier = Modifier.fillMaxWidth().padding(TokeMedidas.PaddingTarjeta), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            contenido()
        }
    }
}

@Composable
private fun Fila(paleta: TokePaleta, etiqueta: String, valor: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(etiqueta, style = estilo(FontWeight.SemiBold, 12), color = paleta.textoSecundario)
        Text(valor, style = estilo(FontWeight.Bold, 15), color = paleta.textoPrincipal)
    }
}

@Composable
private fun Enlace(paleta: TokePaleta, texto: String, onClick: () -> Unit) {
    Surface(onClick = onClick, color = paleta.tarjeta, shape = RoundedCornerShape(8.dp)) {
        Text(texto, style = estilo(FontWeight.SemiBold, 14), color = paleta.enlaceAzul, modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp))
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
            texto,
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            style = estilo(FontWeight.Medium, 14),
            color = if (error) paleta.rojo else paleta.textoPrincipal,
        )
    }
}
