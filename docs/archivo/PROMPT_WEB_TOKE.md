# Prompt — Web pública de TOKE (para construir el `index.html` y sus páginas)

Construye el sitio estático de TOKE para Cloudflare Pages. Sin login, sin paneles, sin tablas operativas, sin secretos en el frontend.

## 1. Estilo (el mismo de la app Android)
- Fondo claro `#F2F4F7` (oscuro `#0A0D10`), tarjetas `#FFFFFF` (oscuro `#14171D`), borde `#E2E8F0` (oscuro `#1E232B`).
- Verde de marca `#059669` (oscuro `#00C853`) con texto blanco encima; texto `#0F172A` / `#F8FAFC`; secundario `#64748B` / `#8E9BAE`; enlaces azules `#2563EB`.
- Tipografía Plus Jakarta Sans (400, 500, 600, 700, 800). Esquinas 12–20 px; botones en píldora de 50 px de alto; margen lateral 20 px.
- Logo: loseta verde redondeada con el icono de TOKE en blanco.
- Prohibido: vidrio/blur, degradados morados, gradientes decorativos, tarjetas genéricas repetidas, emojis como iconos, textos de relleno. Cada sección debe decir algo real de TOKE.
- Respeta modo claro/oscuro del sistema y `prefers-reduced-motion`. Movimiento mínimo (opacidad y desplazamiento corto).
- Fotos y capturas: solo **capturas reales** de la app. Mientras no existan, deja el hueco marcado `PENDIENTE: captura real`; no inventes pantallas.

## 2. Páginas
| Ruta | Contenido |
|---|---|
| `/` | Landing: navbar, hero con propuesta de valor, capturas reales, problema que resuelve, cómo funciona, funciones realmente implementadas, seguridad y permisos, compatibilidad Android, CTA de descarga, preguntas frecuentes, footer con enlaces legales |
| `/descargas` | APK oficial, versión actual, fecha, tamaño, requisitos, novedades, enlace directo al APK (datos desde la fuente pública, sección 3) |
| `/novedades` | Historial de versiones |
| `/privacidad`, `/terminos` | Textos legales **sin inventar**: dejar `PENDIENTE: texto aprobado` hasta recibirlos |
| `/eliminar-cuenta` | Pasos dentro de la app (Más → Mi cuenta → Eliminar cuenta), qué se borra y qué se conserva, y solicitud alternativa por correo/WhatsApp de soporte |
| `/seguridad`, `/ayuda`, `/contacto`, `/licencias` | Información pública, guías y preguntas frecuentes (reutiliza las de la app: Manual y Preguntas), soporte, atribuciones |

## 3. Fuente única de versiones
Ni la web ni Android escriben la versión a mano: ambos leen `GET https://<proyecto>.supabase.co/functions/v1/check-app-release?channel=stable` (Edge Function pública, solo lectura). Respuesta:
```json
{ "applicationId":"com.flabs.cobrate","channel":"stable","latestVersionCode":2,"latestVersionName":"1.1",
  "minSupportedVersionCode":1,"apkUrl":"https://…/toke-1.1.apk","apkSizeBytes":0,"sha256":"…64 hex…",
  "releaseNotesUrl":"https://…/novedades","publishedAt":"2026-…Z" }
```
En `/descargas` muestra versión, fecha, tamaño en MB, SHA-256 (copiable) y botón al `apkUrl`. Si la consulta falla, muestra "No pudimos consultar la versión" y no pongas un enlace inventado. El APK vive fuera de Pages (R2 o GitHub Releases).

## 4. Archivos técnicos
`index.html`, `robots.txt`, `sitemap.xml`, favicon, `<title>`/meta description/canonical/Open Graph por página, `404.html`, `_headers` (CSP estricta, `X-Content-Type-Options`, `Referrer-Policy`, caché larga para assets con hash y corta para HTML), `_redirects` solo si hace falta.

## 5. Enlaces que usa la app Android (ya cableados en `strings.xml`)
`url_terminos` → `/terminos`, `url_privacidad` → `/privacidad`, `url_eliminar_cuenta` → `/eliminar-cuenta`, `url_seguridad` → `/seguridad`, `url_ayuda` → `/ayuda`. Hoy apuntan a `https://toke.example/…`: reemplazar por el dominio real.

## 6. Entregable
Un paso a paso por cada página (qué archivo crear, qué datos lee, cómo probarla) y una lista final de pendientes: textos legales, capturas reales, dominio, APK publicado.
