package com.flabs.cobrate.features.business

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flabs.cobrate.components.TokeIcons
import com.flabs.cobrate.core.data.CuentaStore
import com.flabs.cobrate.core.data.NegocioStore
import com.flabs.cobrate.core.data.RelacionCuentaNegocioStore
import com.flabs.cobrate.core.logic.FeedbackManager
import com.flabs.cobrate.core.model.EstadoRelacion
import com.flabs.cobrate.core.model.InvitacionTrabajador
import com.flabs.cobrate.core.model.RolCuentaNegocio
import com.flabs.cobrate.features.acceso.BotonAcceso
import com.flabs.cobrate.ui.theme.PlusJakartaSans
import com.flabs.cobrate.ui.theme.TokePaleta
import com.flabs.cobrate.ui.theme.TokeRojo
import com.flabs.cobrate.ui.theme.tokePaleta
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Tarjeta "Invitar colaborador" del lado del dueño: genera un código, se copia o se comparte,
 * muestra cuánto falta para que venza y permite revocarlo.
 */
@Composable
fun InvitarTrabajadorScreen(
    businessViewModel: BusinessViewModel,
    onCerrar: () -> Unit,
    onIrAPlanes: () -> Unit = {},
) {
    val paleta = tokePaleta()
    var invitacionActiva by remember { mutableStateOf<InvitacionTrabajador?>(null) }
    var tiempoRestanteMs by remember { mutableStateOf(0L) }
    var creandoInvitacion by remember { mutableStateOf(false) }
    var requiereAprobacion by remember { mutableStateOf(true) }

    val sesion by CuentaStore.sesionActual.collectAsState()
    val relaciones by RelacionCuentaNegocioStore.relaciones.collectAsState()
    val negocios by NegocioStore.negocios.collectAsState()
    val negocioActivoId by NegocioStore.negocioActivoId.collectAsState()
    val planEstado by com.flabs.cobrate.features.planes.PlanStore.estado.collectAsState()
    val esPro = planEstado?.plan == com.flabs.cobrate.features.planes.Plan.PRO
    val trabajadores by businessViewModel.trabajadoresReales.collectAsState()
    val codigosActivos by businessViewModel.codigosActivos.collectAsState()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val cuentaId = sesion?.id
    // Negocio actual: estrictamente el negocio activo seleccionado con validación de permisos.
    val negocioActual = remember(negocioActivoId, cuentaId, relaciones, negocios) {
        val activeId = negocioActivoId ?: return@remember null
        val uid = cuentaId ?: return@remember null
        val negocio = negocios.find { it.id == activeId } ?: return@remember null
        val esDueno = negocio.dueñoCuentaId == uid
        val esAdminActivo = relaciones.any {
            it.cuentaId == uid && it.negocioId == activeId &&
                    it.estado == EstadoRelacion.ACTIVA && it.rol == RolCuentaNegocio.ADMINISTRADOR
        }
        if (esDueno || esAdminActivo) negocio else null
    }

    LaunchedEffect(invitacionActiva) {
        val inv = invitacionActiva ?: return@LaunchedEffect
        while (true) {
            val restante = inv.expiracion - System.currentTimeMillis()
            tiempoRestanteMs = if (restante > 0) restante else 0L
            if (restante <= 0) break
            delay(1000)
        }
    }

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = paleta.tarjeta,
        border = BorderStroke(1.dp, paleta.borde),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Invitar colaborador",
                        style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.ExtraBold, fontSize = 19.sp),
                        color = paleta.textoPrincipal,
                    )
                    if (negocioActual != null) {
                        Text(
                            text = negocioActual.nombre,
                            style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Medium, fontSize = 13.sp),
                            color = paleta.textoSecundario,
                            maxLines = 1,
                        )
                    }
                }
                Surface(
                    onClick = onCerrar,
                    shape = RoundedCornerShape(12.dp),
                    color = paleta.tileFondo,
                    border = BorderStroke(1.dp, paleta.bordeControl),
                    modifier = Modifier.size(40.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(id = TokeIcons.Cerrar),
                            contentDescription = "Cerrar",
                            tint = paleta.textoPrincipal,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }

            if (negocioActual != null) {
                FilaAprobacion(paleta, requiereAprobacion) { requiereAprobacion = it }

                val puedeCrear = esPro || (trabajadores.size < com.flabs.cobrate.features.planes.PlanLogica.MAX_TRABAJADORES_FREE && codigosActivos.size < com.flabs.cobrate.features.planes.PlanLogica.MAX_CODIGOS_ACTIVOS_FREE)
                val aforoLleno = codigosActivos.size >= com.flabs.cobrate.features.planes.PlanLogica.MAX_CODIGOS_ACTIVOS_GLOBAL

                if (aforoLleno) {
                    BotonAcceso(
                        paleta = paleta,
                        texto = "Aforo máximo de 5 códigos alcanzado",
                        principal = false,
                        onClick = {},
                    )
                } else if (!puedeCrear) {
                    com.flabs.cobrate.components.BotonProBloqueado(
                        paleta = paleta,
                        texto = if (trabajadores.size >= 1) "Máx 1 colaborador en Free (Ver planes)" else "Máx 1 código en Free (Ver planes)",
                        onClick = onIrAPlanes,
                    )
                } else {
                    BotonAcceso(
                        paleta = paleta,
                        texto = when {
                            creandoInvitacion -> "Generando..."
                            invitacionActiva == null -> "Generar código"
                            else -> "Generar otro código"
                        },
                        principal = !creandoInvitacion,
                    ) {
                        if (creandoInvitacion) return@BotonAcceso
                        creandoInvitacion = true
                        scope.launch {
                            try {
                                businessViewModel.crearInvitacion(negocioActual.id, requiereAprobacion).onSuccess {
                                    invitacionActiva = it
                                    if (requiereAprobacion) {
                                        FeedbackManager.showSuccess("Código creado. Quien lo use deberá esperar tu aprobación.")
                                    }
                                }.onFailure { err ->
                                    if (err.message?.contains("PLAN_REQUERIDO") == true) {
                                        onIrAPlanes()
                                    } else {
                                        FeedbackManager.showErrorMessage(com.flabs.cobrate.core.logic.ErrorMapper.amable(err, "Error al crear invitación"))
                                    }
                                }
                            } finally {
                                creandoInvitacion = false
                            }
                        }
                    }
                }

                invitacionActiva?.let { inv ->
                    TarjetaCodigo(
                        paleta = paleta,
                        codigo = inv.codigo,
                        restanteMs = tiempoRestanteMs,
                        onCopiar = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Código TOKE", inv.codigo))
                            Toast.makeText(context, "Código copiado", Toast.LENGTH_SHORT).show()
                        },
                        onCompartir = {
                            val envio = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "Tu código para unirte a ${negocioActual.nombre} en TOKE: ${inv.codigo}")
                            }
                            context.startActivity(Intent.createChooser(envio, "Compartir código"))
                        },
                        onRevocar = {
                            businessViewModel.revocarCodigo(inv.codigo)
                            invitacionActiva = null
                        },
                    )
                }
            } else {
                Text(
                    text = "No se encontró un negocio activo que administres.",
                    style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Medium, fontSize = 14.sp),
                    color = TokeRojo,
                )
            }
        }
    }
}

@Composable
private fun FilaAprobacion(paleta: TokePaleta, activo: Boolean, onCambia: (Boolean) -> Unit) {
    Surface(
        onClick = { onCambia(!activo) },
        shape = RoundedCornerShape(16.dp),
        color = paleta.tileFondo,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Aprobar antes de entrar",
                    style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 15.sp),
                    color = paleta.textoPrincipal,
                )
                Text(
                    text = "Quien use el código esperará a que lo aceptes.",
                    style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Medium, fontSize = 13.sp),
                    color = paleta.textoSecundario,
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Switch(
                checked = activo,
                onCheckedChange = onCambia,
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
private fun TarjetaCodigo(
    paleta: TokePaleta,
    codigo: String,
    restanteMs: Long,
    onCopiar: () -> Unit,
    onCompartir: () -> Unit,
    onRevocar: () -> Unit,
) {
    val vigente = restanteMs > 0
    Surface(shape = RoundedCornerShape(18.dp), color = paleta.tileFondo, modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = if (vigente) "Comparte este código con tu colaborador" else "Código vencido. Genera uno nuevo.",
                style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Medium, fontSize = 13.sp),
                color = if (vigente) paleta.textoSecundario else TokeRojo,
                textAlign = TextAlign.Center,
            )
            SelectionContainer {
                Text(
                    text = codigo,
                    style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.ExtraBold, fontSize = 34.sp, letterSpacing = 2.sp),
                    color = if (vigente) paleta.textoPrincipal else paleta.textoSecundario,
                )
            }
            if (vigente) {
                val minutos = (restanteMs / 1000) / 60
                val segundos = (restanteMs / 1000) % 60
                Text(
                    text = "Vence en %02d:%02d".format(minutos, segundos),
                    style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 13.sp),
                    color = paleta.textoSecundario,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    AccionCompacta(paleta, "Copiar", TokeIcons.Copiar, Modifier.weight(1f), onCopiar)
                    AccionCompacta(paleta, "Compartir", TokeIcons.Compartir, Modifier.weight(1f), onCompartir)
                }
                Surface(
                    onClick = onRevocar,
                    shape = RoundedCornerShape(9999.dp),
                    color = paleta.rojoFondo,
                    border = BorderStroke(1.dp, paleta.rojoBorde),
                    modifier = Modifier.fillMaxWidth().height(40.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "Revocar código",
                            style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 14.sp),
                            color = TokeRojo,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AccionCompacta(paleta: TokePaleta, texto: String, icono: Int, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(9999.dp),
        color = paleta.tarjeta,
        border = BorderStroke(1.dp, paleta.bordeControl),
        modifier = modifier.height(40.dp),
    ) {
        Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = icono),
                contentDescription = null,
                tint = paleta.textoPrincipal,
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = texto,
                style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 14.sp),
                color = paleta.textoPrincipal,
            )
        }
    }
}
