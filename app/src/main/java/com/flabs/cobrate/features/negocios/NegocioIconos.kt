package com.flabs.cobrate.features.negocios

import androidx.annotation.DrawableRes
import com.flabs.cobrate.components.TokeIcons
import com.flabs.cobrate.core.model.TipoNegocio

/** Icono del rubro de un negocio. Cada rubro tiene el suyo, y es el mismo al crear y al editar. */
@DrawableRes
fun iconoDeNegocio(tipo: TipoNegocio): Int = when (tipo) {
    TipoNegocio.RESTAURANTE -> TokeIcons.Hamburguesa
    TipoNegocio.MARISQUERIA -> TokeIcons.Hamburguesa
    TipoNegocio.ABARROTES -> TokeIcons.Canasta
    TipoNegocio.ROPA -> TokeIcons.Camiseta
    TipoNegocio.ELECTRODOMESTICOS -> TokeIcons.Celular
    TipoNegocio.FERRETERIA -> TokeIcons.Herramienta
    TipoNegocio.SERVICIOS -> TokeIcons.Maletin
    TipoNegocio.SALUD -> TokeIcons.Salud
    TipoNegocio.TRANSPORTE -> TokeIcons.Auto
    TipoNegocio.OTRO -> TokeIcons.Tienda
}
