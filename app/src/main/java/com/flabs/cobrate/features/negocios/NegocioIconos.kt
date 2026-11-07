package com.flabs.cobrate.features.negocios

import androidx.annotation.DrawableRes
import com.flabs.cobrate.components.TokeIcons
import com.flabs.cobrate.core.model.TipoNegocio

/** Icono del rubro de un negocio. Cada rubro tiene el suyo, y es el mismo al crear y al editar. */
@DrawableRes
fun iconoDeNegocio(tipo: TipoNegocio): Int = when (tipo) {
    TipoNegocio.RESTAURANTE -> TokeIcons.Hamburguesa
    TipoNegocio.MARISQUERIA -> TokeIcons.Recibo
    TipoNegocio.ABARROTES -> TokeIcons.Canasta
    TipoNegocio.ROPA -> TokeIcons.Carrito
    TipoNegocio.ELECTRODOMESTICOS -> TokeIcons.Herramienta
    TipoNegocio.SERVICIOS -> TokeIcons.Maletin
    TipoNegocio.OTRO -> TokeIcons.Tienda
}
