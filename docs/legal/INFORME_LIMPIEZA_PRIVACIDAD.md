# Limpieza final de Privacidad y coherencia entre páginas

Fecha: 2026-11-12. Continúa desde `5266a32` y `9405e24` (no se revirtió nada técnico). **La web no se publicó con estos cambios** (último despliegue: anterior a esta limpieza). Android no cambió.

## 1. Textos provisionales encontrados y corregidos
| Dónde | Antes | Ahora |
|-------|-------|-------|
| Ficha de todos los documentos | "La fecha de entrada en vigor se anunciará al publicar la versión definitiva", "sin revisión de abogado", versión 0.4 | "Versión 1.0 · Vigente desde el 12 de noviembre de 2026" y alcance |
| Privacidad §2, Términos §3, Centro legal | "La titularidad se está regularizando… se publicarán aquí" | "FLABS (Fábrica Labs), grupo personal de desarrollo. No es una empresa constituida." |
| Privacidad §11 (proveedores) | "Pendiente de verificar" | Ubicación real o "no conocemos la región" |
| Privacidad (copias) | "Si algún día las ofrecemos, lo diremos aquí" | Explicación de las tres clases de copias |
| Privacidad (IA) | "Si algún día se agrega IA…" | Solo lo que hay hoy |
| Cookies | "Si algún día agregamos herramientas…" | Eliminado |
| Planes y pagos | Aviso "los pagos se habilitarán…", "si en el futuro se habilita" | "El plan Pro no se paga dentro de la web ni de la app; para consultar su activación, escríbenos" |
| Landing | "Play Store llegará pronto", "Play Store (pronto)", "fase beta", "Los pagos de Pro se habilitarán…", "Los Términos pueden cambiar en el futuro… los aceptas" | Quitado o reescrito; el cambio de términos ahora coincide con el procedimiento de los Términos |
| Descargas | "Todavía no hay…", "Play Store (pronto)", "mejora pendiente" | "Aún no hay una versión disponible", "Descarga no disponible por ahora" (se conservan: son necesarios para decir que no hay APK) |
| Novedades | "Todavía no hay versiones publicadas…" | "Aquí se publican las novedades de cada versión" |
| Licencias | "PENDIENTE: revisión legal del texto de atribuciones" | Eliminado |
| Eliminar cuenta | "Los plazos están en definición", "Por ahora no contamos con respaldo…" | Tabla por situación y explicación de copias |

## 2. Contradicciones corregidas
- Seguridad "Nada más" / lectura solo de pagos → Android entrega todo y TOKE filtra (ya corregido; se mantiene).
- Landing "aceptas los cambios por registrarte o iniciar sesión" vs Términos "te avisamos y pedimos aceptación" → alineado con los Términos.
- Planes "impuestos incluidos" sin emisor fiscal → eliminado ("Precios en soles peruanos (PEN)").
- Eliminar cuenta ("se borra de inmediato" universal) → distingue trabajador, dueño, negocio y expulsión.
- Seguridad "no es peligrosa" → "pide un permiso sensible".
- Planes: ya no describe un proceso de pago operativo; coincide con el botón "Contáctanos".

## 3. Afirmaciones que se mantienen (verificadas)
Detalles en el informe de consolidación: listado de 13 apps y descarte sin leer, texto original no guardado ni impreso, copias de Android limitadas a tema y aviso de voz, RLS por negocio, sesión cifrada y única, borrado en cascada de cuenta/negocio, QR en menos de 1 día, limpieza de 24 h y 7 días, Supabase en São Paulo, precios y límites Free, reembolso de 7 días hábiles, Pro vuelve a Free sin borrar datos ni "solo lectura".

## 4. Lo que no puede publicarse todavía sin inventar datos
- Identidad legal de la persona natural: nombre, DNI, RUC si corresponde, domicilio, medio de pago y comprobantes. Es un pendiente interno separado (`REGULARIZACION_PERSONA_NATURAL_TOKE.md`), ya no aparece como "pendiente" en la web.
- Regiones de Firebase y Brevo (la web dice que no las conocemos).
- Plazo de 30 días para expulsados: aprobado, **no implementado**; la web describe el comportamiento real (el vínculo se conserva hasta que se elimina el negocio o la cuenta). Propuesta de migración en `INFORME_CONSOLIDACION_TOKE.md` §4.
- Inscripción de bancos de datos, Oficial de Datos, plazos de incidentes y ARCO, Libro de Reclamaciones: no se pudo leer el texto oficial; la web no afirma cumplimiento.
- Que el Libro de Reclamaciones siga retirado queda como riesgo interno.

## 5. Archivos
`web/`: privacidad, terminos, cookies, planes-y-pagos, eliminar-cuenta, seguridad, descargas, legal, index, novedades, licencias (+ CSS sin cambios de hash). Generadores locales: `.artifacts/web_legal2.py`, `web_legal3.py`, `parchar7.py`, `parchar8.py`.

## 6. Resultados
- Enlaces internos: 0 rotos. Un H1 por página. Sin desborde horizontal a 375 px en 9 páginas.
- Búsqueda global de frases provisionales: solo quedan "Aún no hay una versión disponible" y "Descarga no disponible por ahora" (necesarias).
- Android: sin cambios en esta ronda (último build y pruebas en verde: `5266a32`; build final con R8 en `e004bf7`).

## 7. Publicación
**La web no fue publicada con estos cambios.** Fecha de vigencia puesta: 12 de noviembre de 2026; si se publica otro día, se cambia `FECHA` en `.artifacts/web_legal2.py` y se regenera.

## Nota sobre la versión 1.0
Se asignó 1.0 porque el texto quedó consolidado con lo que hace la app. No significa que un abogado lo haya revisado ni que TOKE esté regularizada.
