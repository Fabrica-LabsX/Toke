# CORE BASE — TOKE (Fábrica Labs)
> **Documento de Memoria Central y Arquitectura Maestra**  
> Última actualización: 14 de noviembre de 2026.  
> Este archivo representa la fuente única de verdad arquitectónica, técnica y operativa para cualquier agente o desarrollador que trabaje en el proyecto.

---

## 1. Identidad y Propósito del Producto
- **Nombre**: TOKE.
- **Autor / Organización**: Fábrica Labs (FLABS).
- **Misión**: Aplicación Android nativa para pequeños y medianos comercios en Perú. Permite al dueño del negocio y a sus trabajadores escuchar y visualizar en tiempo real los pagos recibidos por **Yape** y **Plin**.
- **Mecanismo Operativo**:
  - `TokeListener` (`NotificationListenerService`): Escucha en segundo plano las notificaciones locales del teléfono emitidas por apps de pago compatibles.
  - `TextToSpeech`: Canta el pago en voz alta ("Pago recibido de 25 soles de Juan Pérez") en el dispositivo del negocio y en los dispositivos sincronizados de los trabajadores.
  - **Límites Legales y Operativos**: TOKE **no** es un banco, **no** es una billetera electrónica, **no** procesa transacciones monetarias, no recibe ni retiene dinero, y jamás solicita contraseñas bancarias ni accesos a Yape o Plin.

---

## 2. Ecosistema de Repositorios y Servicios

```
┌────────────────────────────────────────────────────────────────────────┐
│                              ECOSISTEMA TOKE                           │
├────────────────────────────────┬───────────────────────────────────────┤
│ Repositorio / Servicio         │ Función & Nivel de Acceso             │
├────────────────────────────────┼───────────────────────────────────────┤
│ Fabrica-LabsX/Toke             │ Repositorio PRIVADO en GitHub.         │
│ (Rama: main)                   │ Código fuente nativo de la app Android│
├────────────────────────────────┼───────────────────────────────────────┤
│ Fabrica-LabsX/toke-releases    │ Repositorio PÚBLICO en GitHub.        │
│ (Rama: versiones)              │ ÚNICAMENTE aloja el APK final y notas.│
│                                │ CERO código fuente.                   │
├────────────────────────────────┼───────────────────────────────────────┤
│ Fabrica-LabsX/toke-web         │ Repositorio de la web pública.        │
│ (tokelabs.pages.dev)           │ 100% estático en Cloudflare Pages.    │
│                                │ Cero conexión con Supabase.           │
├────────────────────────────────┼───────────────────────────────────────┤
│ Supabase Project (Toque!)      │ Backend Postgres + Auth + Realtime    │
│ ref: ianiulmdyzsqtrqpujkx      │ + Edge Functions en sa-east-1.        │
└────────────────────────────────┴───────────────────────────────────────┘
```

---

## 3. Reglas de Negocio y Planes (Free vs. Pro)

### Plan Free (Básico)
1. **Negocios**: Máximo 1 negocio por cuenta de dueño.
2. **Métodos de Cobro**: Máximo 1 billetera de cobro vinculada.
3. **Equipo**: Máximo 1 colaborador hospedado y 1 código de invitación activo simultáneamente.
4. **Reportes Excel**: Cuota máxima de 3 exportaciones por semana (`ExportQuotaStore`).
5. **Barrera de Pago**: Al alcanzar o intentar exceder cualquiera de estos límites, la app bloquea la acción y despliega el modal de planes con candado PRO (`CandadoPro.kt`).

### Plan Pro
- Acceso ilimitado a creación de comercios, colaboradores, métodos de cobro y descargas de Excel.
- Activación manual mediante soporte por WhatsApp (`+51 998 360 217`).

---

## 4. Protocolos de Seguridad y Estado del Sistema

### A. Sesión Única (Estilo WhatsApp Web)
- **Implementación**: Tabla `public.sesiones_activas` y función RPC `reclamar_sesion(p_dispositivo_id, p_dispositivo_nombre, p_forzar)`.
- **Comportamiento**:
  - Al iniciar sesión en un segundo teléfono, la app notifica: *"Tu cuenta está activa en otro teléfono"*.
  - El usuario puede presionar **«Usar aquí»** (`p_forzar = true`).
  - Esto actualiza la base de datos y, mediante el canal Realtime `sesion_unica_$uid`, el teléfono anterior recibe la orden remota de cerrar sesión de inmediato (`onCerrarSesionRemoto`).
  - Si el usuario cierra y vuelve a abrir sesión en el **mismo** celular, entra de inmediato sin falsos avisos.

### B. Invitaciones de Trabajador Endurecidas
- **Duración**: Los códigos de referido duran exactamente **5 minutos** desde su generación.
- **Ciclo de vida**: Al vencer los 5 minutos pasan al estado `REVOCADA` durante 1 hora (para dar visibilidad al usuario de que caducó) y posteriormente son purgados de la base de datos de manera definitiva.

### C. Protocolo de Versión Única Vigente y Bloqueo Obligatorio
- **Base de datos**: Tabla `public.app_releases` registra cada versión:
  - `v1.0.0` (version_code 1): `active = false` **(Descontinuado)**.
  - `v1.1.0` (version_code 2): `active = true` **(Vigente)**, con `min_supported_version_code = 2`.
- **Edge Function**: `GET /functions/v1/check-app-release?channel=stable` entrega el manifiesto oficial en tiempo real.
- **Bloqueo en el Cliente**:
  - `ActualizacionStore` evalúa en el arranque si `versionInstalada < latestVersionCode`.
  - Si no es la última versión, `CobrateApp()` **no se compone**. Toda la aplicación es reemplazada por `ActualizacionPantalla(obligatoria = true)`.
  - El botón Atrás cierra el proceso (`finishAffinity()`). El usuario no puede entrar hasta actualizar.

### D. Limpieza de Interfaz de Operaciones
- Se eliminó cualquier campo o referencia a "código de operación" en el detalle de cobro detectado, manteniendo la vista limpia para el cajero.

---

## 5. Diseño y Estándares Visuales (Anti-IA Glassmorphism)
1. **Colores Sólidos**: Queda terminantemente prohibido el uso de glassmorphism, fondos translúcidos difusos o gradientes transparentes que degraden la legibilidad. Todo fondo y estado debe ser sólido (verde `#059669` / `#00C853`, rojo `#EF4444`, amarillo `#F59E0B`, fondos neutros oscuros/claros).
2. **Proporción 9:16 sin Scroll Innecesario**: Pantallas de bienvenida y selección como *"¿Cómo deseas empezar?"* deben estar calculadas para verse expandidas y completas en dispositivos con relación de aspecto 9:16 (como el Redmi 9A) sin requerir desplazamiento.

---

## 6. Credenciales de Firma y Compilación

- **Llave de Firma de Producción**:
  - Archivo: `toke-release.jks` (RSA 4096 bits).
  - Alias: `toke`.
  - Contraseña y archivo: En posesión exclusiva del dueño en su respaldo seguro externo (USB / Bóveda privada).
  - **Regla de oro**: Este archivo y `keystore.properties` viven **FUERA** del repositorio y del proyecto por seguridad. Solo se colocan temporalmente para ejecutar `./gradlew assembleRelease` y luego se retiran.
- **Ofuscación y R8**: Activado en `buildTypes.release` con `proguard-android-optimize.txt` y `proguard-rules.pro`. Los logs de depuración (`Log.d/v/i`) se despojan automáticamente del binario final.
