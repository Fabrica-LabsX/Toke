package com.flabs.cobrate.features.permisos
import com.flabs.cobrate.ui.theme.TokeMedidas

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.flabs.cobrate.core.logic.FeedbackManager
import com.flabs.cobrate.core.logic.Requisito
import com.flabs.cobrate.ui.theme.PlusJakartaSans
import com.flabs.cobrate.ui.theme.TokePaleta
import com.flabs.cobrate.ui.theme.tokePaleta

/**
 * Pantalla "Permisos y Servicio". Reúne el estado real de los permisos y arma las piezas:
 *  - en qué estado está el servicio y qué permisos aplican lo decide [EvaluadorServicio];
 *  - cómo se ve cada pieza vive en PermisosBanner y PermisoFila;
 *  - abrir los Ajustes del sistema vive en [PermisosAjustes].
 *
 * Con [esOnboarding] se agrega el botón fijo "Listo, todo configurado".
 */
@Composable
fun DiagnosticoPermisosScreen(
    requisitos: Set<Requisito> = emptySet(),
    esOnboarding: Boolean = false,
    onVolver: () -> Unit,
) {
    val paleta = tokePaleta()
    val context = LocalContext.current

    // Estado real de los permisos; se revalida solo al regresar de Ajustes.
    val permisosActuales = rememberPermisosActuales()
    val accesoNotificaciones = permisosActuales.accesoNotificaciones
    val sinRestriccionesBateria = permisosActuales.bateriaSinRestricciones

    val requiereAcceso = EvaluadorServicio.requiereAccesoNotificaciones(requisitos)
    val requiereBateria = EvaluadorServicio.requiereBateria(requisitos)
    val estado = EvaluadorServicio.evaluar(
        requiereAcceso = requiereAcceso,
        accesoConcedido = accesoNotificaciones,
        requiereBateria = requiereBateria,
        bateriaSinRestricciones = sinRestriccionesBateria,
    )

    val permisos = buildList {
        if (requiereAcceso) {
            add(
                PermisoUi(
                    icono = TokeIcons.CampanaAlerta,
                    titulo = "Acceso a notificaciones",
                    descripcion = "Reconoce los pagos entrantes de billeteras digitales y banca móvil.",
                    concedido = accesoNotificaciones,
                    resaltado = false,
                    onActivar = { PermisosAjustes.abrirAccesoNotificaciones(context) },
                ),
            )
        }
        if (requiereBateria) {
            add(
                PermisoUi(
                    icono = if (sinRestriccionesBateria) TokeIcons.Rayo else TokeIcons.RayoAmbar,
                    titulo = "Seguir activo con pantalla apagada",
                    descripcion = "Permite enviar avisos a tu gente aunque tengas el teléfono guardado.",
                    concedido = sinRestriccionesBateria,
                    resaltado = !sinRestriccionesBateria,
                    onActivar = { PermisosAjustes.abrirBateria(context) },
                ),
            )
        }
    }

    Scaffold(
        containerColor = paleta.fondo,
        topBar = { Cabecera(paleta = paleta, onVolver = onVolver) },
        bottomBar = {
            if (esOnboarding) {
                BotonListo(
                    paleta = paleta,
                    onClick = {
                        if (requiereAcceso && !accesoNotificaciones) {
                            FeedbackManager.showErrorMessage(
                                "Activa el acceso a notificaciones para que TOKE pueda leer los avisos de pago.",
                            )
                            PermisosAjustes.abrirAccesoNotificaciones(context)
                        } else {
                            onVolver()
                        }
                    },
                )
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            PermisosBanner(paleta = paleta, estado = estado)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "PERMISOS DE ESTE CELULAR",
                    style = TextStyle(
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = 0.8.sp,
                    ),
                    color = paleta.textoSecundario,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
                PermisosLista(paleta = paleta, permisos = permisos)
            }
            PermisosAviso(paleta = paleta, conTitulo = estado != EstadoServicio.OPERATIVO)
        }
    }
}

@Composable
private fun Cabecera(paleta: TokePaleta, onVolver: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            onClick = onVolver,
            shape = RoundedCornerShape(12.dp),
            color = paleta.tarjeta,
            border = BorderStroke(1.dp, paleta.borde),
            modifier = Modifier.size(44.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(id = TokeIcons.FlechaAtras),
                    contentDescription = "Volver",
                    tint = paleta.textoPrincipal,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = "Permisos y Servicio",
            style = TextStyle(
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp,
                letterSpacing = (-0.5).sp,
            ),
            color = paleta.textoPrincipal,
        )
    }
}

@Composable
private fun BotonListo(paleta: TokePaleta, onClick: () -> Unit) {
    Surface(color = paleta.tarjeta, border = BorderStroke(1.dp, paleta.borde), modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Surface(
                onClick = onClick,
                shape = RoundedCornerShape(9999.dp),
                color = paleta.verde,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(TokeMedidas.BotonAltura),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "Listo, todo configurado",
                        style = TextStyle(
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                        ),
                        color = paleta.verdeTexto,
                    )
                }
            }
            Text(
                text = "Puedes modificar estas opciones en cualquier momento.",
                style = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                ),
                color = paleta.textoSecundario,
            )
        }
    }
}
