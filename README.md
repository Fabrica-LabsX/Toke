# TOKE (`com.flabs.cobrate`)

Asistente operativo de cobros en tiempo real para comercios y puntos de venta en Perú (Yape, Plin y transferencias). Desarrollado por Fábrica Labs.

---

## 1. Qué es TOKE y qué problema resuelve

En comercios físicos con atención al público, los pagos por billeteras digitales (Yape, Plin y bancos locales) llegan al teléfono del dueño del negocio. Para confirmar un pago, el colaborador en caja suele depender de capturas de pantalla del cliente (susceptibles a fraudes) o interrumpir al dueño para verificar el saldo.

**TOKE resuelve este problema mediante:**
- Detección automática en tiempo real de notificaciones oficiales de cobro recibidas en el dispositivo mediante `NotificationListenerService`.
- Sincronización instantánea con el equipo autorizado en el punto de venta a través de Supabase Realtime y Firebase Cloud Messaging (FCM).
- Aviso audible y visual con síntesis de voz (TTS) para confirmación inmediata sin tocar el dispositivo.
- Aislamiento estricto por comercio y usuario mediante políticas de seguridad a nivel de fila (RLS en PostgreSQL).

> **Aviso de seguridad**: TOKE no procesa transacciones bancarias, no mueve fondos, no solicita claves de acceso bancarias ni lee SMS o chats privados.

---

## 2. Tecnologías principales

- **Lenguaje**: Kotlin 2.0+ (JVM Toolchain 17).
- **UI Framework**: Jetpack Compose con Material 3 y Single Activity Architecture.
- **Concurrencia**: Kotlin Coroutines y StateFlow / Flow reactivo.
- **Backend & Base de datos**: Supabase (PostgREST, Auth, Realtime, Storage).
- **Mensajería Push**: Firebase Cloud Messaging (FCM).
- **Optimización & Seguridad**: R8 / ProGuard con ofuscación, reducción de recursos y desinfección de logs en release (`LogSeguro`).
- **Web Pública**: Cloudflare Pages (`https://tokelabs.pages.dev`).

---

## 3. Seguridad y Privacidad

- **Arquitectura de aislamiento**: La aplicación implementa un filtro estricto por lista blanca en memoria volátil (`AppsDePagoConfiables`). Las notificaciones que no provienen de los 13 paquetes bancarios y de billeteras autorizados (Yape, Plin, BCP, BBVA, Interbank, etc.) son descartadas de inmediato sin leer su contenido ni registrarse.
- **Mínimo privilegio**: No solicita permisos invasivos (sin acceso a SMS, contactos, ubicación geográfica, cámara, micrófono ni almacenamiento privado).
- **Integridad de versiones**: Distribución firmada criptográficamente (RSA 4096) y verificable mediante hash SHA-256 en VirusTotal.

---

## 4. Reporte de vulnerabilidades y contacto

Si detectas un problema de seguridad o deseas más información técnica:
- **Correo de seguridad**: [AeroCorpKT@protonmail.com](mailto:AeroCorpKT@protonmail.com)
- **WhatsApp de soporte**: +51 998 360 217 (atención diaria de 2:00 p. m. a 8:00 p. m. PET)
- **Portal de descargas y transparencia**: [https://tokelabs.pages.dev/verificar](https://tokelabs.pages.dev/verificar)
