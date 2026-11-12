# Obligaciones legales: qué se pudo verificar

Fecha: 2026-11-12.

## Resultado honesto
**No se pudo leer ningún texto oficial completo** desde esta sesión. Se intentó con 3 consultas automáticas:
- `congreso.gob.pe` (PDF del D.S. 016-2024-JUS): 404.
- `diariooficial.elperuano.pe` (idNorma=23): un PDF que no es una norma (archivo de diseño gráfico). La portada carga "Cargando…" sin enlaces a normas.
- `spij.minjus.gob.pe`: carga sin contenido (aplicación JavaScript).
- `gob.pe` (Indecopi, Minjus): bloqueado (HTTP 418).
- `sunat.gob.pe`: la portada carga; las páginas de RUC/comprobantes (`cpe`, `emprender`, `personas`): bloqueadas (HTTP 403).
- `indecopi.gob.pe`: no resuelve el dominio.

Por eso **ninguna obligación de esta lista está "confirmada" con texto oficial**. Por la regla del proyecto, las fuentes secundarias no prueban una obligación legal. Se clasifican así:

| Categoría | Significado |
|-----------|-------------|
| 1 | Obligación legal confirmada con texto oficial |
| 2 | Recomendación de seguridad o buena práctica |
| 3 | Decisión comercial del proyecto |
| 4 | Dato pendiente de confirmar (hay indicios en fuentes secundarias) |

## Lista
| Tema | Categoría | Qué se sabe | Cómo cerrarlo |
|------|-----------|-------------|---------------|
| Reglamento de la Ley 29733 vigente | 4 | D.S. 016-2024-JUS, publicado el 30-11-2024, vigente desde el 30-03-2025 (El Peruano, CMS, Baker McKenzie: secundarias) | Leer el D.S. en https://busquedas.elperuano.pe/ (Diario Oficial) |
| Inscripción de bancos de datos | 4 | La Ley 29733 prevé un registro; no se leyó el artículo ni sus excepciones | Texto de la ley y del reglamento + ANPD |
| Oficial de Datos Personales (art. 37) | 4 | Mencionado por fuentes secundarias; umbrales no leídos | Texto del D.S. |
| Notificación de incidentes | 4 | Fuentes secundarias indican 48 horas a la ANPD; no confirmado | Texto del D.S. |
| Plazos de derechos ARCO | 4 | No encontrados | Texto del D.S. |
| Transferencia internacional | 4 | Ley 29733 exige nivel de protección adecuado o garantías (secundarias) | Texto de la ley |
| Libro de Reclamaciones virtual (Ley 32495) | 4 | Gob.pe/Indecopi (vía buscador) y prensa: aplica a plataformas de comercio electrónico, enlace visible, respuesta en 15 días hábiles | https://busquedas.elperuano.pe/ y Archivo Digital del Congreso (https://wb2server.congreso.gob.pe/adlp-visor/) |
| Contratación a distancia / devolución | 4 | No leído | Código de Protección y Defensa del Consumidor (Ley 29571) |
| RUC y comprobantes de una persona natural | 4 | No leído | https://www.gob.pe/284-inscripcion-en-el-ruc, https://cpe.sunat.gob.pe/ (revisión manual) |
| Reembolso en 7 días hábiles | 3 | Decisión del proyecto | — |
| HTTPS, RLS, sesión cifrada, sin logs sensibles | 2 | Buenas prácticas ya aplicadas | — |

## Enlaces oficiales a revisar a mano
- Diario Oficial El Peruano, búsqueda de normas: https://busquedas.elperuano.pe/
- Archivo Digital de la Legislación del Perú (Congreso): https://wb2server.congreso.gob.pe/adlp-visor/
- Inscripción en el RUC (SUNAT, vía gob.pe): https://www.gob.pe/284-inscripcion-en-el-ruc
- Comprobantes de pago electrónicos (SUNAT): https://cpe.sunat.gob.pe/

La web **no afirma cumplimiento** de ninguna de estas normas.
