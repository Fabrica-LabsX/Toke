package com.flabs.cobrate.features.negocios

import androidx.annotation.DrawableRes
import com.flabs.cobrate.components.TokeIcons
import com.flabs.cobrate.core.model.TipoNegocio

/** Icono del rubro de un negocio. Son los mismos iconos que se eligen al crear el negocio. */
@DrawableRes
fun iconoDeNegocio(tipo: TipoNegocio): Int = when (tipo) {
    TipoNegocio.RESTAURANTE -> TokeIcons.Hamburguesa
    TipoNegocio.ABARROTES -> TokeIcons.Canasta
    TipoNegocio.SERVICIOS -> TokeIcons.Maletin
    TipoNegocio.ELECTRODOMESTICOS -> TokeIcons.Herramienta
    TipoNegocio.MARISQUERIA,
    TipoNegocio.ROPA,
    TipoNegocio.OTRO -> TokeIcons.Tienda
}
