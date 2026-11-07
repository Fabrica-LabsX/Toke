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

    val tipos = listOf(
        OpcionTipoNegocio(TipoNegocio.ABARROTES, "Bodega / Minimarket", "Abarrotes, bebidas y víveres"),
        OpcionTipoNegocio(TipoNegocio.RESTAURANTE, "Restaurante o Cafetería", "Menú del día, sangucherías y comidas"),
        OpcionTipoNegocio(TipoNegocio.MARISQUERIA, "Marisquería & Cevichería", "Pescados, mariscos y comida marina"),
        OpcionTipoNegocio(TipoNegocio.ROPA, "Tienda de ropa y calzado", "Prendas, accesorios y calzado"),
        OpcionTipoNegocio(TipoNegocio.ELECTRODOMESTICOS, "Electrohogar & Ferretería", "Tecnología, herramientas y repuestos"),
        OpcionTipoNegocio(TipoNegocio.SERVICIOS, "Servicios y Oficios", "Peluquería, reparaciones y talleres"),
        OpcionTipoNegocio(TipoNegocio.OTRO, "Otro tipo de negocio", "Personaliza tu propio rubro comercial"),
    )

    fun nombreValido(nombre: String): Boolean = nombre.isNotBlank()

    /** Nombre del rubro tal como se le muestra a la persona. */
    fun tituloDeTipo(tipo: TipoNegocio?): String =
        tipos.firstOrNull { it.tipo == tipo }?.titulo ?: "Otro tipo de negocio"

    /** Convierte "#RRGGBB" a un entero ARGB; null si el texto no es un color válido. */
    fun argbDeHex(hex: String?): Long? {
        val limpio = hex?.removePrefix("#") ?: return null
        if (limpio.length != 6) return null
        val valor = limpio.toLongOrNull(16) ?: return null
        return 0xFF000000L or valor
    }
}
