package com.flabs.cobrate.features.soporte

import com.flabs.cobrate.components.TokeIcons

/** Pieza de una pantalla de ejemplo del manual. Usan los mismos estilos que la app. */
sealed class ElementoMock {
    data class Fila(val icono: Int, val texto: String) : ElementoMock()
    data class Boton(val texto: String, val principal: Boolean = true) : ElementoMock()
    data class Campo(val etiqueta: String, val valor: String) : ElementoMock()
    data class Interruptor(val texto: String, val activo: Boolean) : ElementoMock()
}

/** Una pantalla de ejemplo: [resaltado] es la posición del elemento que hay que tocar. */
data class MockPantalla(val titulo: String, val elementos: List<ElementoMock>, val resaltado: Int)

data class PasoManual(val texto: String, val mock: MockPantalla)

data class ArticuloManual(
    val id: String,
    val titulo: String,
    val resumen: String,
    val icono: Int,
    val pasos: List<PasoManual>,
)

data class PreguntaFrecuente(val pregunta: String, val respuesta: String)

/** Contenido del Manual y de las Preguntas frecuentes. Solo datos: se puede leer y probar sin pantalla. */
object ManualContenido {

    private fun menuMas(resaltado: Int) = MockPantalla(
        titulo = "Más",
        elementos = listOf(
            ElementoMock.Fila(TokeIcons.Maletin, "Mis negocios"),
            ElementoMock.Fila(TokeIcons.NavEquipo, "Mi equipo"),
            ElementoMock.Fila(TokeIcons.Reportes, "Reportes"),
            ElementoMock.Fila(TokeIcons.MisQrs, "Mis QRs"),
            ElementoMock.Fila(TokeIcons.VolumenAlto, "Aviso en voz alta"),
        ),
        resaltado = resaltado,
    )

    private fun hojaEditar(resaltado: Int, asignacion: Boolean = true) = MockPantalla(
        titulo = "Editar negocio",
        elementos = listOf(
            ElementoMock.Campo("Nombre del negocio", "Mi Bodeguita"),
            ElementoMock.Campo("Máximo de trabajadores", "Sin límite"),
            ElementoMock.Interruptor("Asignación de cobros", asignacion),
            ElementoMock.Boton("Guardar cambios"),
        ),
        resaltado = resaltado,
    )

    val articulos: List<ArticuloManual> = listOf(
        ArticuloManual(
            id = "crear-negocio",
            titulo = "Cómo crear un negocio",
            resumen = "Tu primer negocio en 4 pasos.",
            icono = TokeIcons.Tienda,
            pasos = listOf(
                PasoManual("Toca “Más” y entra a “Mis negocios”.", menuMas(0)),
                PasoManual(
                    "Abajo, toca “Crear negocio”.",
                    MockPantalla("Mis negocios", listOf(ElementoMock.Fila(TokeIcons.Tienda, "Mi Bodeguita"), ElementoMock.Boton("Crear negocio")), 1),
                ),
                PasoManual(
                    "Escribe el nombre, elige el tipo de negocio y un color.",
                    MockPantalla("Nuevo negocio", listOf(ElementoMock.Campo("Nombre del negocio", "Mi Bodeguita"), ElementoMock.Boton("Continuar")), 0),
                ),
                PasoManual(
                    "Vincula tu billetera (Yape, Plin…) o toca “Lo haré después”. ¡Listo!",
                    MockPantalla("Medios de cobro", listOf(ElementoMock.Boton("Vincular un método"), ElementoMock.Boton("Lo haré después", principal = false)), 0),
                ),
            ),
        ),
        ArticuloManual(
            id = "anadir-colaborador",
            titulo = "Cómo añado a un colaborador",
            resumen = "Invítalo con un código de 6 letras.",
            icono = TokeIcons.PersonAdd,
            pasos = listOf(
                PasoManual("Toca “Mi equipo” abajo y luego “+ Invitar nuevo colaborador”.", MockPantalla("Equipo", listOf(ElementoMock.Fila(TokeIcons.Persona, "Ana"), ElementoMock.Boton("+ Invitar nuevo colaborador")), 1)),
                PasoManual(
                    "Decide si quieres aprobarlo antes de que entre y toca “Generar código”.",
                    MockPantalla("Invitar colaborador", listOf(ElementoMock.Interruptor("Aprobar antes de entrar", true), ElementoMock.Boton("Generar código")), 1),
                ),
                PasoManual("Comparte el código con tu colaborador (por WhatsApp, por ejemplo).", MockPantalla("Código listo", listOf(ElementoMock.Campo("Código", "GAB5XL"), ElementoMock.Boton("Compartir")), 1)),
                PasoManual(
                    "Tu colaborador abre TOKE, toca “Tengo un código de trabajador” y lo escribe. Si pediste aprobación, la verás en “Solicitudes”: acéptala.",
                    MockPantalla("Solicitudes", listOf(ElementoMock.Fila(TokeIcons.Persona, "Carlos quiere entrar"), ElementoMock.Boton("Aceptar")), 1),
                ),
            ),
        ),
        ArticuloManual(
            id = "quitar-colaborador",
            titulo = "Cómo borro a alguien de mi negocio",
            resumen = "Pausarlo o sacarlo para siempre.",
            icono = TokeIcons.NavEquipo,
            pasos = listOf(
                PasoManual("Entra a “Mi equipo”.", menuMas(1)),
                PasoManual(
                    "Para una salida temporal, toca “Pausar”: deja de ver cobros, pero puedes reanudarlo.",
                    MockPantalla("Mi equipo", listOf(ElementoMock.Fila(TokeIcons.Persona, "Carlos"), ElementoMock.Boton("Pausar", principal = false)), 1),
                ),
                PasoManual(
                    "Para sacarlo, toca los tres puntos de su fila y elige “Expulsar”.",
                    MockPantalla("Mi equipo", listOf(ElementoMock.Fila(TokeIcons.Persona, "Carlos  ⋮"), ElementoMock.Boton("Expulsar", principal = false)), 1),
                ),
                PasoManual("Confirma. Desde ese momento no ve nada de tu negocio y no puede volver con un código.", MockPantalla("¿Expulsar a Carlos?", listOf(ElementoMock.Boton("Expulsar")), 0)),
            ),
        ),
        ArticuloManual(
            id = "borrar-negocio",
            titulo = "Cómo elimino mi negocio",
            resumen = "Se borra con todo su historial.",
            icono = TokeIcons.Papelera,
            pasos = listOf(
                PasoManual("Toca “Más” y, abajo de la lista, “Borrar negocio”. También puedes hacerlo desde el lápiz en “Mis negocios”.", menuMas(4)),
                PasoManual("Escribe el nombre de tu negocio para confirmar que de verdad quieres borrarlo.", MockPantalla("Borrar negocio", listOf(ElementoMock.Campo("Escribe: Mi Bodeguita", "Mi Bodeguita")), 0)),
                PasoManual("Toca “Borrar”. No se puede deshacer: se pierden los cobros y el equipo de ese negocio.", MockPantalla("Borrar negocio", listOf(ElementoMock.Boton("Borrar negocio")), 0)),
            ),
        ),
        ArticuloManual(
            id = "modificar-negocio",
            titulo = "Cómo puedo modificar mi negocio",
            resumen = "Nombre, tipo, color y tope de trabajadores.",
            icono = TokeIcons.Editar,
            pasos = listOf(
                PasoManual("Entra a “Más” > “Mis negocios”.", menuMas(0)),
                PasoManual("Toca el lápiz de tu negocio.", MockPantalla("Mis negocios", listOf(ElementoMock.Fila(TokeIcons.Editar, "Mi Bodeguita  ✎")), 0)),
                PasoManual("Cambia lo que necesites: nombre, tipo, color o máximo de trabajadores.", hojaEditar(0)),
                PasoManual("Toca “Guardar cambios”.", hojaEditar(3)),
            ),
        ),
        ArticuloManual(
            id = "quitar-asignacion",
            titulo = "Cómo puedo quitar la asignación de cobros",
            resumen = "Para que no haya que reclamar cada cobro.",
            icono = TokeIcons.Intercambiar,
            pasos = listOf(
                PasoManual("Con 2 o más trabajadores, cada cobro lo reclama uno de ellos. Entra a “Mis negocios” y toca el lápiz.", MockPantalla("Mis negocios", listOf(ElementoMock.Fila(TokeIcons.Editar, "Mi Bodeguita  ✎")), 0)),
                PasoManual("Apaga el interruptor “Asignación de cobros”.", hojaEditar(2)),
                PasoManual("Toca “Guardar cambios”. Ya no habrá cobros por asignar en ese negocio.", hojaEditar(3, asignacion = false)),
            ),
        ),
        ArticuloManual(
            id = "exportar-excel",
            titulo = "Cómo exporto mis cobros a Excel",
            resumen = "Una tabla lista para abrir o compartir.",
            icono = TokeIcons.Reportes,
            pasos = listOf(
                PasoManual("Entra a “Más” > “Reportes”.", menuMas(2)),
                PasoManual("Toca “Exportar en Excel”.", MockPantalla("Reportes", listOf(ElementoMock.Fila(TokeIcons.Reportes, "S/ 1,250.00 hoy"), ElementoMock.Boton("Exportar en Excel")), 1)),
                PasoManual("Elige el periodo (día, semana, mes o año).", MockPantalla("Exportar", listOf(ElementoMock.Fila(TokeIcons.Calendario, "Este mes"), ElementoMock.Boton("Exportar")), 0)),
                PasoManual("Elige dónde enviarlo: WhatsApp, correo o guardarlo. Necesita el plan Pro.", MockPantalla("Compartir", listOf(ElementoMock.Fila(TokeIcons.Compartir, "WhatsApp")), 0)),
            ),
        ),
        ArticuloManual(
            id = "configurar-billetera",
            titulo = "Cómo configuro mi billetera para recibir cobros",
            resumen = "Vincula Yape, Plin o tu banco.",
            icono = TokeIcons.MisQrs,
            pasos = listOf(
                PasoManual("Entra a “Más” > “Mis QRs”.", menuMas(3)),
                PasoManual("Toca “Vincular un método”.", MockPantalla("Mis QRs", listOf(ElementoMock.Boton("Vincular un método")), 0)),
                PasoManual("Elige tu billetera o banco. Si usas Plin, elige en cuál lo recibes.", MockPantalla("Elige tu billetera", listOf(ElementoMock.Fila(TokeIcons.Billetera, "Yape"), ElementoMock.Fila(TokeIcons.Billetera, "Plin"), ElementoMock.Fila(TokeIcons.Billetera, "BCP")), 0)),
                PasoManual("Escribe tu número y guarda. Para que TOKE lea los avisos, activa el acceso a notificaciones en “Más” > “Permisos”.", MockPantalla("Tu número", listOf(ElementoMock.Campo("Número", "999 999 999"), ElementoMock.Boton("Guardar")), 1)),
            ),
        ),
        ArticuloManual(
            id = "avisos-voz",
            titulo = "Cómo modifico los avisos en voz alta",
            resumen = "Elige qué dice TOKE al llegar un cobro.",
            icono = TokeIcons.VolumenAlto,
            pasos = listOf(
                PasoManual("Entra a “Más” > “Aviso en voz alta”.", menuMas(4)),
                PasoManual(
                    "Elige “Cauteloso” (solo dice “Pago confirmado”) o “Básico” (dice la billetera y el monto; plan Pro).",
                    MockPantalla("Aviso en voz alta", listOf(ElementoMock.Fila(TokeIcons.VolumenAlto, "Cauteloso"), ElementoMock.Fila(TokeIcons.VolumenAlto, "Básico")), 0),
                ),
                PasoManual("Toca “Probar” para escuchar cómo suena antes de salir.", MockPantalla("Aviso en voz alta", listOf(ElementoMock.Boton("Probar aviso")), 0)),
            ),
        ),
    )

    val preguntas: List<PreguntaFrecuente> = listOf(
        PreguntaFrecuente(
            "¿TOKE tiene acceso a mis billeteras?",
            "No. TOKE nunca entra a Yape, Plin ni a tu banco, y no ve tus claves ni tu saldo. Solo lee el aviso de pago que ya te llega al celular, como lo haría un reloj que lee tus notificaciones.",
        ),
        PreguntaFrecuente(
            "¿TOKE puede mover o cobrar mi dinero?",
            "No. TOKE no puede enviar, pedir ni retirar dinero. Solo te avisa cuando alguien te paga.",
        ),
        PreguntaFrecuente(
            "¿Qué datos guarda TOKE?",
            "El monto, la billetera, la hora y el nombre que muestra el aviso de cada cobro, para que tú y tu equipo los vean. Nada más de tus billeteras.",
        ),
        PreguntaFrecuente(
            "¿Por qué piden permiso de notificaciones?",
            "Porque así TOKE se entera de los pagos. Sin ese permiso no puede avisarte. Lo activas en “Más” > “Permisos”.",
        ),
        PreguntaFrecuente(
            "¿Necesito internet?",
            "Sí, para avisar a tu equipo. Si te quedas sin internet, TOKE guarda el cobro y lo envía cuando vuelva (verás “Esperando red”).",
        ),
        PreguntaFrecuente(
            "¿Mis trabajadores ven todo mi negocio?",
            "No. Ven solo los cobros de las billeteras activas que les tocan. No ven reportes, ni el equipo, ni tu plan.",
        ),
        PreguntaFrecuente(
            "¿Por qué el nombre de quien me paga sale con asterisco?",
            "Porque la billetera lo muestra así, por privacidad (por ejemplo, “Juan P*”). TOKE lo copia tal cual.",
        ),
        PreguntaFrecuente(
            "¿Cuántos avisos tengo en el plan Free?",
            "20 al día, compartidos entre todos tus negocios. Con el plan Pro son sin límite y se reinician cada día.",
        ),
        PreguntaFrecuente(
            "¿Cómo activo el plan Pro?",
            "Entra a “Más” > “Mi plan” y toca “Activar plan Pro”. Se abre WhatsApp con tu código para que lo activemos.",
        ),
        PreguntaFrecuente(
            "¿Puedo usar mi cuenta en dos celulares?",
            "Solo en uno a la vez. Si la abres en otro celular, TOKE te pregunta si quieres usarla ahí y cierra la sesión en el anterior.",
        ),
        PreguntaFrecuente(
            "¿Qué billeteras y bancos detecta?",
            "Yape, BCP, Interbank, BBVA, Scotiabank, BanBif, Pichincha, BIM, Agora/Tunki, Ligo, Sip y Lemon Cash. Plin llega por la app de tu banco.",
        ),
        PreguntaFrecuente(
            "¿Qué hago si un cobro no aparece?",
            "Revisa que tengas internet, que TOKE esté encendido y que el acceso a notificaciones esté activo en “Permisos”. Si sigue igual, escríbenos por WhatsApp desde “Acerca de y soporte”.",
        ),
    )
}
