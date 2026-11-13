package com.flabs.cobrate.features.crearnegocio

import com.flabs.cobrate.core.model.TipoNegocio

/** Un color de marca que el dueño puede elegir para su negocio. */
data class ColorMarca(val nombre: String, val hex: String)

/** Un rubro que el dueño puede elegir, con el texto que se le muestra. */
data class OpcionTipoNegocio(val tipo: TipoNegocio, val titulo: String, val descripcion: String)

/** Datos y reglas del asistente "Crear negocio". Solo lógica: sin pantallas ni red. */
object CrearNegocioLogica {
    const val COLOR_POR_DEFECTO = "#007A3E"

    val colores = listOf(
        ColorMarca("Verde Bosque", "#007A3E"),
        ColorMarca("Azul Rey", "#1E40AF"),
        ColorMarca("Ámbar Cálido", "#D97706"),
        ColorMarca("Morado Toké", "#7E22CE"),
        ColorMarca("Coral Intenso", "#DC2626"),
        ColorMarca("Verde Azulado", "#0D9488"),
    )

    /** Rubros generales, en el orden en que se muestran. Cada uno agrupa varios negocios parecidos. */
    val tipos = listOf(
        OpcionTipoNegocio(TipoNegocio.RESTAURANTE, "Restaurante o comida", "Restaurantes, cevicherías, chifas, cafeterías y puestos de comida"),
        OpcionTipoNegocio(TipoNegocio.ABARROTES, "Bodega o minimarket", "Abarrotes, bebidas, víveres y mercados"),
        OpcionTipoNegocio(TipoNegocio.ROPA, "Atuendos o ropa", "Prendas, calzado y accesorios"),
        OpcionTipoNegocio(TipoNegocio.ELECTRODOMESTICOS, "Tecnología y electro", "Celulares, computadoras y electrodomésticos"),
        OpcionTipoNegocio(TipoNegocio.FERRETERIA, "Ferretería y hogar", "Herramientas, repuestos y construcción"),
        OpcionTipoNegocio(TipoNegocio.SERVICIOS, "Servicios y oficios", "Reparaciones, talleres y trabajos por encargo"),
        OpcionTipoNegocio(TipoNegocio.SALUD, "Salud y belleza", "Peluquerías, spa, farmacias y consultorios"),
        OpcionTipoNegocio(TipoNegocio.TRANSPORTE, "Transporte", "Taxis, mototaxis, delivery y fletes"),
        OpcionTipoNegocio(TipoNegocio.OTRO, "Otro negocio", "Cualquier otro rubro"),
    )

    /** Los negocios antiguos de marisquería se muestran como Restaurante. */
    fun normalizar(tipo: TipoNegocio?): TipoNegocio =
        if (tipo == TipoNegocio.MARISQUERIA) TipoNegocio.RESTAURANTE else tipo ?: TipoNegocio.OTRO

    fun nombreValido(nombre: String): Boolean = nombre.isNotBlank()

    /** Nombre del rubro tal como se le muestra a la persona. */
    fun tituloDeTipo(tipo: TipoNegocio?): String =
        tipos.first { it.tipo == normalizar(tipo) }.titulo

    /** Convierte "#RRGGBB" a un entero ARGB; null si el texto no es un color válido. */
    fun argbDeHex(hex: String?): Long? {
        val limpio = hex?.removePrefix("#") ?: return null
        if (limpio.length != 6) return null
        val valor = limpio.toLongOrNull(16) ?: return null
        return 0xFF000000L or valor
    }
}
