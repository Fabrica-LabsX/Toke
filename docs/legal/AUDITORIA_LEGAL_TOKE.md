# Auditoría legal y documental de TOKE

Fecha: 2026-11-12. Alcance: web pública (`web/`), código de la app, base y funciones de Supabase (solo lectura), documentación del proyecto. No se modificó la app, la base ni los datos.

## 1. Descubrimiento
- **Web:** sitio estático (HTML/CSS/JS propio, sin framework) en Cloudflare Pages, proyecto `tokelabs`. Las páginas legales se generan con un script local (`.artifacts/web_build.py`, ignorado por git) sobre una plantilla común; el Centro Legal está en `/legal`.
- **Páginas legales (14 URL en el sitemap):** Términos, Privacidad, Cookies, Planes y pagos, Uso aceptable, Eliminación de cuenta, Seguridad, Licencias, Centro legal, Contacto, Descargas, Novedades, Ayuda, portada. Sin duplicados. Enlaces internos: 0 rotos.
- **Publicación:** `wrangler pages deploy web --project-name tokelabs --branch main`. Esta auditoría **no** publicó nada: la última publicación (`ff1167c6`) es anterior a los cambios de esta tarea.
- **Documentos internos relacionados:** `CLAUDE.md`, `docs/INFORME_SEGURIDAD.md`, `docs/PENDIENTES_LEGALES_WEB.md`, `docs/PLANTILLAS_CORREO_SUPABASE.md`.
- **Vercel:** no hay ningún rastro en el repositorio ni en la web. No se declara en las políticas (ver Q-PRV-1).

## 2. Qué hace realmente TOKE (evidencia)
| Tema | Hallazgo | Fuente |
|------|----------|--------|
| Notification Listener | Android entrega todas las notificaciones al servicio. El código mira el paquete **antes** de leer título/texto y descarta sin guardar lo que no está en la lista de 13 apps de pago (Yape, BCP, Interbank, Interbank Negocios, Tunki, BBVA, BIM, BanBif, Ligo, Scotiabank, Pichincha, Tarjeta Oh!, Lemon Cash). | `TokeNotificationListenerService.kt:162-202`, `AppsDePagoConfiables.kt` |
| Quién detecta | Solo continúa si el usuario es **dueño** de algún negocio. | `...Service.kt:279-283` |
| Dato local | Se guarda en el celular el título, texto y extras de la notificación de pago detectada (DataStore `eventos_notificacion`). No se encontró borrado ni límite. | `EventoDeNotificacionStore.kt`, `EventoDeNotificacionLocalStore.kt` |
| Dato enviado | monto, método, estado, fecha/hora, trabajador asignado, id del evento y nombre del pagador. | `OperacionRepository.kt` (`OperacionDto`) |
| Columnas sin uso | `notif_snapshot_app` y `notif_snapshot_texto`: existen, 0 filas rellenas. | consulta a `operaciones` |
| Registros técnicos | 55 `Log.d/i/v`; el listener escribe `"$titulo: $texto"` en logcat. R8 desactivado. | `...Service.kt:242` |
| Copias del sistema | `allowBackup="true"`; solo se excluye la sesión cifrada. | `AndroidManifest.xml:15`, `backup_rules.xml` |
| Permisos | VIBRATE, POST_NOTIFICATIONS, ACCESS_NETWORK_STATE, INTERNET, REQUEST_INSTALL_PACKAGES, FOREGROUND_SERVICE (+ especial) y el acceso a notificaciones. | `AndroidManifest.xml` |
| Datos en servidor | cuentas (usuario, fechas), perfiles_publicos, dispositivos (token FCM), sesiones_activas, negocios, relaciones, invitaciones, métodos de cobro (dato de cobro, titular y QR), operaciones, cambios de estado, planes. RLS activa en todas las tablas. | Supabase `list_tables`, `INFORME_SEGURIDAD.md` |
| Región | Supabase `sa-east-1` (São Paulo). Plan Free. | `get_project`, `get_organization` |
| Borrado | Cuenta/negocio: cascada inmediata. QR: cola + limpieza cada 10 min (probado). Cuentas sin verificar: 24 h. Sesiones anónimas sin negocio: 7 días. | `eliminar-cuenta` v3, `limpiar-qr`, pg_cron |
| Pagos del plan | Activación manual por WhatsApp; sin pasarela en el código; la app no muestra precios. | `planes_cuenta`, código |

## 3. Normativa verificada (y límites de la verificación)
La verificación se hizo con **fuentes secundarias** (El Peruano, gob.pe/Indecopi, Gestión, La Cámara, CMS, Baker McKenzie). **No se leyeron los textos oficiales completos.** Todo lo de esta tabla debe confirmarse en El Peruano antes de la versión definitiva.

| Norma | Qué se confirmó | Estado |
|-------|-----------------|--------|
| Ley 29733 (datos personales) | Sigue vigente; define el marco y las transferencias internacionales. | Verificado (secundarias) |
| D.S. 016-2024-JUS | Publicado el 30-11-2024; vigente desde el 30-03-2025; deroga el D.S. 003-2013-JUS. Notificación de incidentes a la autoridad en 48 horas (fuentes secundarias). Menciona Oficial de Datos Personales (art. 37). | Verificado (secundarias); **plazos ARCO no confirmados** |
| Ley 29571 (consumo) | Marco general del consumidor, libro de reclamaciones. | No se leyó el texto |
| Ley 32495 | Modifica arts. 150-151: libro de reclamaciones virtual y enlace visible para plataformas de comercio electrónico (webs, apps). Respuesta en 15 días hábiles; multas hasta 50 UIT. Hay discrepancia entre fuentes sobre si el plazo es prorrogable. | Verificado (secundarias) |
| Ley 31814 / D.S. 115-2025-PCM (IA) | No se pudo verificar en esta tarea. Las políticas mencionan IA de forma descriptiva, sin citar la norma. | **No verificado** |

## 4. Análisis por tema (A–O)
| | Tema | Resultado |
|---|------|-----------|
| A | Protección de datos | Política v0.2 describe qué se guarda y qué llega a servidores. Falta el plazo de conservación (Q-PRI-2, Q-PRI-6). |
| B | Derechos ARCO | Se citan; el procedimiento es "escribe al correo". Falta plazo de respuesta (no verificado) y verificación de identidad de trabajadores (Q-ELI-1). |
| C | Bases legales | Descritas en términos generales. Falta el consentimiento específico para el acceso a notificaciones en la app (el permiso lo pide Android; no se auditó el texto de la pantalla). |
| D | Registro de bancos de datos | Sin evidencia de inscripción (Q-PRI-7). |
| E | Transferencias internacionales | Mencionadas; sin garantías documentadas (Q-PRV-3, 4). |
| F | Responsable y encargado | No definido (Q-PRI-1). |
| G | Contratos con proveedores | No hay copias archivadas (Q-PRV-3). |
| H | Retención y eliminación | Eliminación inmediata verificada. Retención de operaciones sin plazo (Q-PRI-2). |
| I | Incidentes | No hay procedimiento escrito; la política ya no lo promete (Q-SEG-1). |
| J | Datos de terceros | Nombre del pagador: ahora declarado; procedimiento pendiente (Q-PRI-5). |
| K | Trabajadores y propietarios | Roles separados por RLS; no se probó con 4 roles (riesgo abierto en `INFORME_SEGURIDAD.md`). |
| L | Identificación del proveedor | Incompleta por decisión del dueño (Q-ID-1 a 4). |
| M | Información al consumidor | Precios, límites, activación y cancelación están publicados y coinciden con la base. Falta la identidad del proveedor. |
| N | Libro de Reclamaciones | Retirado temporalmente; posible obligación (Q-COM-1). |
| O | Contratación y cancelación | Sin renovación automática; reembolso de 7 días; falta medio de devolución (Q-COM-4, 5). |

## 5. Lo que no se pudo verificar
- Textos oficiales completos de las normas (solo fuentes secundarias).
- Plazos ARCO del reglamento nuevo y los supuestos del Oficial de Datos Personales.
- Regiones de Firebase, Brevo y Cloudflare; contratos de encargo.
- Si Supabase conserva copias internas de la base.
- El texto que muestra la app al pedir el acceso a notificaciones.
- Control de acceso completo con 4 roles (no hay pruebas hechas).
