import io
def rd(p): return open(p, encoding='utf-8').read()
def wr(p, s): open(p, 'w', encoding='utf-8', newline='').write(s)

p = 'app/src/main/java/com/flabs/cobrate/features/crearnegocio/CrearNegocioPasos.kt'
s = rd(p)
def rep(s, a, b, n=1):
    assert a in s, a[:60]
    return s.replace(a, b, n)

# --- Paso 1: icono tocable
s = rep(s, """    colorHex: String?,
    onColor: (String?) -> Unit,
    onContinuar: () -> Unit,
    onAtras: () -> Unit,
) {
    AccesoFondo(paleta = paleta) {
        CabeceraPaso(paleta, 1, onAtras)""", """    colorHex: String?,
    onColor: (String?) -> Unit,
    tipo: TipoNegocio?,
    onTipo: (TipoNegocio) -> Unit,
    onContinuar: () -> Unit,
    onAtras: () -> Unit,
) {
    var hojaIcono by remember { mutableStateOf(false) }
    AccesoFondo(paleta = paleta) {
        CabeceraPaso(paleta, 1, onAtras)""")
s = rep(s, """                    Surface(shape = RoundedCornerShape(12.dp), color = colorDe(colorHex), modifier = Modifier.size(44.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(id = TokeIcons.Tienda),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Así se verá tu negocio",""", """                    Surface(
                        onClick = { hojaIcono = true },
                        shape = RoundedCornerShape(12.dp),
                        color = colorDe(colorHex),
                        modifier = Modifier.size(44.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(id = iconoDeTipo(CrearNegocioLogica.normalizar(tipo))),
                                contentDescription = "Cambiar el icono del negocio",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Así se verá tu negocio",""")
s = rep(s, """                            color = paleta.textoPrincipal,
                        )
                    }
                }
            }
        }

        EspacioAcceso(4)
        BotonAcceso(paleta, "Continuar", principal = CrearNegocioLogica.nombreValido(nombre)) {
            if (CrearNegocioLogica.nombreValido(nombre)) onContinuar()
        }
    }""", """                            color = paleta.textoPrincipal,
                        )
                    }
                    Surface(
                        onClick = { hojaIcono = true },
                        shape = RoundedCornerShape(9999.dp),
                        color = paleta.tileFondo,
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = TokeIcons.Editar),
                                contentDescription = null,
                                tint = paleta.textoPrincipal,
                                modifier = Modifier.size(14.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Icono",
                                style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 12.5.sp),
                                color = paleta.textoPrincipal,
                            )
                        }
                    }
                }
            }
        }

        EspacioAcceso(4)
        BotonAcceso(paleta, "Continuar", principal = CrearNegocioLogica.nombreValido(nombre)) {
            if (CrearNegocioLogica.nombreValido(nombre)) onContinuar()
        }
    }
    if (hojaIcono) HojaIconoNegocio(paleta, tipo, onTipo) { hojaIcono = false }""")

# --- Paso 2: círculos
a = s.index("/** Paso 2: rubro del negocio.")
b = s.index("/** Resumen del negocio ya creado")
paso2 = '''/** Paso 2: rubro del negocio, en iconos con su nombre. Al continuar se crea el negocio. */
@Composable
fun PasoTipo(
    paleta: TokePaleta,
    elegido: TipoNegocio?,
    onElegir: (TipoNegocio) -> Unit,
    ocupado: Boolean,
    onCrear: () -> Unit,
    onAtras: () -> Unit,
) {
    AccesoFondo(
        paleta = paleta,
        pie = {
            BotonAcceso(paleta, "Crear mi negocio", principal = elegido != null && !ocupado) {
                if (elegido != null && !ocupado) onCrear()
            }
        },
    ) {
        CabeceraPaso(paleta, 2, onAtras)
        TituloAcceso(paleta, "¿A qué se dedica tu negocio?", "Elige el rubro que más se parezca. Podrás cambiar el icono cuando quieras.")
        SelectorIconoNegocio(paleta, elegido, onElegir)
    }
}

'''
s = s[:a] + paso2 + s[b:]

# --- Final
a = s.index("/** Resumen del negocio ya creado")
b = s.index("/** Paso 3: medios de cobro.")
listo = '''/** Paso 4: el negocio ya está creado. Pantalla para el dueño: su icono y color, lo que quedó hecho y a dónde ir. */
@Composable
fun PasoListo(
    paleta: TokePaleta,
    nombreNegocio: String,
    tipo: TipoNegocio?,
    colorHex: String?,
    metodos: List<MetodoDeCobro>,
    onCargarQr: () -> Unit,
    onEntrar: () -> Unit,
) {
    AccesoFondo(
        paleta = paleta,
        horizontal = Alignment.CenterHorizontally,
        pie = { BotonAcceso(paleta, "Entrar a mi negocio", principal = true, onClick = onEntrar) },
    ) {
        CabeceraPaso(paleta, 4, null)
        Spacer(modifier = Modifier.size(8.dp))
        Surface(shape = RoundedCornerShape(30.dp), color = colorDe(colorHex), modifier = Modifier.size(96.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(id = iconoDeTipo(CrearNegocioLogica.normalizar(tipo))),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(46.dp),
                )
            }
        }
        Text(
            text = "¡Tu negocio está listo!",
            style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp, textAlign = TextAlign.Center),
            color = paleta.textoPrincipal,
        )
        Text(
            text = "$nombreNegocio ya puede recibir cobros y avisar a tu equipo al instante.",
            style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Medium, fontSize = 15.sp, lineHeight = 21.sp, textAlign = TextAlign.Center),
            color = paleta.textoSecundario,
        )
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = paleta.tarjeta,
            border = BorderStroke(1.dp, paleta.borde),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                FilaListo(paleta, "Negocio creado", CrearNegocioLogica.tituloDeTipo(tipo), hecho = true)
                FilaListo(
                    paleta,
                    "Método de cobro",
                    if (metodos.isEmpty()) "Aún no vinculaste ninguno" else metodos.joinToString { MetodosCobroLogica.nombreLegible(it.tipo) },
                    hecho = metodos.isNotEmpty(),
                )
            }
        }
        BotonAcceso(
            paleta,
            if (metodos.isEmpty()) "Vincular un método de cobro" else "Cargar mi QR para recibir cobros",
            principal = false,
            onClick = onCargarQr,
        )
    }
}

@Composable
private fun FilaListo(paleta: TokePaleta, titulo: String, detalle: String, hecho: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(id = if (hecho) TokeIcons.CheckVerde else TokeIcons.Advertencia),
            contentDescription = if (hecho) "Hecho" else "Pendiente",
            tint = Color.Unspecified,
            modifier = Modifier.size(24.dp),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = titulo,
                style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp),
                color = paleta.textoPrincipal,
            )
            Text(
                text = detalle,
                style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Medium, fontSize = 13.sp),
                color = paleta.textoSecundario,
            )
        }
    }
}

'''
s = s[:a] + listo + s[b:]
s = rep(s, "import androidx.compose.ui.text.font.FontWeight\n", "import androidx.compose.ui.text.font.FontWeight\nimport androidx.compose.ui.text.style.TextAlign\n")
wr(p, s)

p = 'app/src/main/java/com/flabs/cobrate/features/business/CrearNegocioScreen.kt'
s = rd(p)
s = rep(s, "import com.flabs.cobrate.features.crearnegocio.PasoConexion\nimport com.flabs.cobrate.features.crearnegocio.PasoFinal\n", "import com.flabs.cobrate.features.crearnegocio.PasoListo\n")
s = rep(s, "    CONEXION_EXITOSA,\n    PASO_4_FINALIZADO\n", "    PASO_4_LISTO\n")
s = rep(s, "paso = if (businessViewModel != null) PasoWizard.PASO_3_MEDIOS_COBRO else PasoWizard.CONEXION_EXITOSA", "paso = if (businessViewModel != null) PasoWizard.PASO_3_MEDIOS_COBRO else PasoWizard.PASO_4_LISTO")
s = rep(s, "            onColor = { colorHexSeleccionado = it },\n            onContinuar", "            onColor = { colorHexSeleccionado = it },\n            tipo = tipoSeleccionado,\n            onTipo = { tipoSeleccionado = it },\n            onContinuar")
a = s.index("        PasoWizard.CONEXION_EXITOSA -> PasoConexion(")
b = s.index("    }\n}\n", a)
s = s[:a] + """        PasoWizard.PASO_4_LISTO -> PasoListo(
            paleta = paleta,
            nombreNegocio = negocioCreado?.nombre ?: nombreComercial,
            tipo = tipoSeleccionado,
            colorHex = colorHexSeleccionado,
            metodos = metodosDelNegocio,
            onCargarQr = { onIrAMisQRs?.invoke() ?: onCreado() },
            onEntrar = onCreado,
        )
""" + s[b:]
s = rep(s, "onContinuar = { paso = PasoWizard.CONEXION_EXITOSA },", "onContinuar = { paso = PasoWizard.PASO_4_LISTO },")
wr(p, s)
print("ok")
