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

## 3. Requisitos de compilación

- **Android Studio**: Hedgehog (2023.1.1) o superior (Ladybug / Meerkat recomendados).
- **JDK**: Java Development Kit 17 (o JBR 17/21 incluido en Android Studio).
- **Android SDK**: `compileSdk = 37`, `targetSdk = 37`, `minSdk = 24` (Android 7.0 Nougat o superior).
- **Gradle**: Versión de Gradle Wrapper incluida (`9.6.0`).

---

## 4. Configuración del entorno local

El proyecto utiliza un esquema de configuración desacoplado donde ninguna clave privada ni credencial se versiona en Git.

### Paso 1: Configurar `local.properties`
Copia el archivo de plantilla a la raíz del proyecto:
```bash
cp local.properties.example local.properties
```
Edita `local.properties` e introduce la ruta de tu SDK de Android y las claves públicas de Supabase:
```properties
sdk.dir=C\:\\Users\\TuUsuario\\AppData\\Local\\Android\\Sdk
SUPABASE_URL=https://tu-proyecto.supabase.co
SUPABASE_ANON_KEY=sb_publishable_tu_clave_anon_aqui
```

### Paso 2: Configurar Firebase (`google-services.json`)
Copia la plantilla de Firebase en el módulo `app`:
```bash
cp app/google-services.json.example app/google-services.json
```
Reemplaza los valores con la configuración cliente de tu consola de Firebase.

### Paso 3: Configurar firma de release (opcional)
Para generar binarios de producción firmados localmente, crea `keystore.properties` a partir de la plantilla:
```bash
cp keystore.properties.example keystore.properties
```
Configura la ruta a tu archivo `.jks` y sus contraseñas. Si este archivo no existe, la compilación de release saldrá sin firmar o podrás firmarla con la llave de depuración para pruebas locales (`-PfirmarConDebug`).

---

## 5. Instrucciones de compilación

### Compilar versión de depuración (Debug)
```bash
./gradlew assembleDebug
```
El instalador APK resultante se genera en `app/build/outputs/apk/debug/app-debug.apk`.

### Ejecutar pruebas unitarias
```bash
./gradlew test
```

### Compilar versión optimizada (Release)
Para compilar release con firma de depuración (pruebas locales):
```bash
./gradlew assembleRelease -PfirmarConDebug
```

Para compilar release oficial firmada con `keystore.properties`:
```bash
./gradlew assembleRelease
```
El APK optimizado y ofuscado por R8 se ubicará en `app/build/outputs/apk/release/app-release.apk`.

---

## 6. Reporte de vulnerabilidades y seguridad

Si detectas un problema de seguridad o un falso positivo en herramientas de análisis:
- **Correo de seguridad**: [AeroCorpKT@protonmail.com](mailto:AeroCorpKT@protonmail.com) (asunto: *"Reporte de seguridad / Falso positivo"*)
- **WhatsApp de soporte**: +51 998 360 217 (atención diaria de 2:00 p. m. a 8:00 p. m. PET)
- **Portal de descargas y transparencia**: [https://tokelabs.pages.dev/descargas](https://tokelabs.pages.dev/descargas)
