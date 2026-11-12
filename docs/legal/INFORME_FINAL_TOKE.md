# Informe final: centro legal simplificado, APK y privacidad

Fecha: 2026-11-12. Etiquetas: **[CÓDIGO]** verificado en código · **[OFICIAL]** verificado en documentación oficial · **[CONSOLA]** verificado en consola/configuración · **[DECISIÓN]** decisión aprobada por el propietario · **[RECOMENDACIÓN]** · **[PENDIENTE]** pendiente de verificación.
Complementa a `INFORME_CONSOLIDACION_TOKE.md` (Android) y a los demás documentos de `docs/legal/`.

## 1. Resumen ejecutivo
Se simplificó el centro legal (estructura inspirada en la presentación sencilla de Tingu Alerta: secciones cortas, tablas, "lo que no recogemos"; no se copió texto). Quedó en 6 documentos públicos, versión 0.4, con la misma ficha (versión, fecha, a quién aplica). Se añadió a Descargas el apartado del APK. Nada se publicó ni desplegó. No se tocó Supabase.

## 2. Documentos públicos modificados (local)
Términos (14 secciones cortas), Privacidad (14 secciones, tablas), Cookies, Planes y pagos (ficha nueva), Eliminar cuenta (aviso "desinstalar no elimina la cuenta"), **Seguridad y uso aceptable** (se fusionó "Uso aceptable"; `/uso-aceptable` redirige con `_redirects`), Centro legal (6 tarjetas), Descargas (APK). Licencias: sin cambios en esta ronda.

## 3. Documentos internos
`docs/legal/` (auditoría, matriz de coherencia, cuestionario, matriz de decisiones, obligaciones legales, regularización, procedimiento de incidentes, informes). Este informe reemplaza en la práctica al de implementación para fines de revisión final.

## 4. Web
- Enlaces: 0 rotos; un solo H1; sin desborde horizontal en 375 px en 10 páginas **[CÓDIGO]** (revisión en navegador local, no en teléfono real).
- Pro sigue "próximamente"; no hay pasarela ni contratación **[DECISIÓN]**.
- Logos de Yape y Plin fuera; aclaración de no afiliación **[DECISIÓN]**.
- No se publica el nombre personal del propietario **[DECISIÓN]**.

## 5. Android (ya hecho en `5266a32`, comprobado de nuevo)
- Sin almacenamiento del texto original, sin logs sensibles, copias automáticas en lista blanca **[CÓDIGO]**: 0 llamadas directas a `Log.*`, prueba `PrivacidadCodigoTest` en verde.
- No hay otra ruta que guarde el texto: solo el analizador y el normalizador leen título y texto, y trabajan en memoria **[CÓDIGO]**.
- Lo que sí se guarda en el celular: copias locales de cuenta, negocios, relaciones, métodos, cobros (con nombre del pagador), cola de cobros pendientes, alertas y preferencias **[CÓDIGO]**. La web ya lo dice.

## 6. Auditoría del APK
| Punto | Resultado |
|-------|-----------|
| Qué es | Paquete instalable de la app; sin billetera ni movimiento de dinero **[CÓDIGO]** |
| Dónde se descarga | Solo desde la página Descargas **[DECISIÓN]**. Hoy **no hay APK ni enlace publicado** **[CONSOLA]**: la tabla `app_releases` está vacía |
| Versión pública | `versionCode 1`, `versionName "1.0"` **[CÓDIGO]**; ninguna publicada |
| Firma Android | Sin llave de firma de lanzamiento: solo se firma con la de depuración para pruebas (`-PfirmarConDebug`) **[CÓDIGO]**. Consistencia entre versiones: **no hay versiones públicas** |
| R8 / ofuscación | Apagado **[CÓDIGO]** |
| SHA-256 publicado | No existe todavía; se declara como mejora pendiente en la web **[PENDIENTE]** |
| Actualizaciones | Manuales. La app consulta la función `check-app-release`, descarga, comprueba tamaño, SHA-256, nombre del paquete y que la firma sea la de la app instalada, y abre el instalador con confirmación de Android **[CÓDIGO]** |
| Detección de APK modificados | No hay control propio, salvo la comprobación de firma al actualizar **[CÓDIGO]** |
| Mínimo de Android | 7.0 (minSdk 24), target 37 **[CÓDIGO]** |

## 7. Permisos reales (Manifest) **[CÓDIGO]**
VIBRATE, POST_NOTIFICATIONS, ACCESS_NETWORK_STATE, INTERNET, REQUEST_INSTALL_PACKAGES, FOREGROUND_SERVICE, FOREGROUND_SERVICE_SPECIAL_USE y el servicio de acceso a notificaciones (`BIND_NOTIFICATION_LISTENER_SERVICE`). La exención de batería no es un permiso: abre los ajustes del sistema. No hay permisos de ubicación, contactos, SMS, cámara, micrófono ni almacenamiento. Dependencias relevantes: Supabase, Firebase Messaging y ML Kit (lector de QR en el celular).

## 8. Datos procesados y enviados
- **Android entrega temporalmente:** todas las notificaciones al servicio. **Descartadas sin leer:** las que no son de 13 apps de pago (Yape, BCP, Interbank, Interbank Negocios, Tunki, BBVA, BIM, BanBif, Ligo, Scotiabank, Pichincha, Tarjeta Oh!, Lemon Cash) **[CÓDIGO]**.
- **En memoria:** título y texto, solo para buscar el pago; solo en el celular del dueño **[CÓDIGO]**.
- **Se envía a Supabase:** id, negocio, trabajador, método, monto, estado, fecha/hora, id del evento y nombre del pagador **[CÓDIGO]**.
- **FCM recibe:** identificador del celular y un aviso con solo datos (sin montos) **[CÓDIGO]**.
- Columnas `notif_snapshot_app` y `notif_snapshot_texto`: existen y están vacías **[CONSOLA]**.

## 9. Proveedores y regiones
| Proveedor | Estado |
|-----------|--------|
| Supabase | Región `sa-east-1` (São Paulo), plan Free **[CONSOLA]** |
| Firebase (FCM) | Integrado **[CÓDIGO]**; región **[PENDIENTE]** |
| Cloudflare | Aloja la web **[CÓDIGO]**; región **[PENDIENTE]** |
| Brevo | Solo en documentación del proyecto; configuración **[PENDIENTE]** |
| Vercel | Sin rastro en código ni web; no se declara **[CÓDIGO]** |
Acuerdos/contratos y quién accede a los paneles: **[PENDIENTE]**.

## 10. Obligaciones legales verificadas
Ninguna **[OFICIAL]**: los enlaces oficiales de `gob.pe` están bloqueados para el lector automático (HTTP 418) y El Peruano / SPIJ no entregaron texto. Detalle en `OBLIGACIONES_LEGALES_VERIFICADAS_TOKE.md`. La web **no afirma cumplimiento**. Se citan la Ley 29733 y el D.S. 016-2024-JUS solo como marco de derechos (acceso, rectificación, cancelación, oposición) y sin plazos.

## 11. Obligaciones pendientes
Inscripción de bancos de datos, Oficial de Datos Personales, plazos de notificación de incidentes y de respuesta a derechos, Libro de Reclamaciones (Ley 32495), contratación a distancia, RUC y comprobantes **[PENDIENTE]**. Libro de Reclamaciones retirado por decisión del propietario **[DECISIÓN]**: el riesgo sigue.

## 12. Requiere autorización
ELI-2 (migración de Supabase para la fecha de expulsión y borrado a los 30 días: propuesta en el informe de consolidación), cualquier cambio de la app no cubierto (por ejemplo el botón "Activar plan Pro", que abre WhatsApp mientras la web dice "próximamente"), publicar la web, publicar el APK.

## 13. Pruebas
- Android: `testDebugUnitTest` + `assembleDebug` en verde (51 archivos, 0 fallos) tras el último cambio de código **[CÓDIGO]**. Esta ronda no cambió código Android.
- Web: enlaces, H1, desborde móvil (375 px) y redirección revisados en servidor local. **Sin dispositivo físico:** no se probó la app ni la descarga en un celular.
- Backend: solo lectura en esta ronda.

## 14. Riesgos abiertos
- Hasta que salga un APK nuevo, el APK anterior (si alguien lo tiene) guarda texto y escribe logs; publicar la web junto con el APK nuevo.
- Sin llave de firma de lanzamiento ni R8: la versión pública no está lista.
- Textos escritos por el equipo, sin abogado ni verificación oficial; "no regularizado".
- Libro de Reclamaciones y titularidad pendientes.
- El texto de privacidad pide que **Brevo, Firebase y Cloudflare** se verifiquen antes de declarar regiones.

## 15–17. Archivos y commits
Archivos: `web/` (privacidad, terminos, cookies, planes-y-pagos, seguridad, eliminar-cuenta, descargas, legal, sitemap, `_redirects`; `uso-aceptable.html` eliminado) y `docs/legal/INFORME_FINAL_TOKE.md`. Commits previos: `953feb9`, `5266a32`, `9405e24`; el de esta ronda se indica en la respuesta. Compilación Android: ver punto 13. La web se genera con `.artifacts/web_legal2.py` (local, ignorado por git).

## 18. Confirmación
**No se realizó deploy, ni publicación del APK, ni cambios en Supabase** (solo lecturas de configuración).
