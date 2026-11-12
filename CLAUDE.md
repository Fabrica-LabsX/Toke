# TOKE (Cobrate) — guía del proyecto, estado y auditoría de lanzamiento

Última revisión: 2026-11-11 · Rama: `feature/figma-precision-2026-10-11` (126 commits, **nada subido a GitHub todavía**).
Idioma de trabajo con el usuario: español. Textos de la app y la web: español neutro, tono simple.

## 1. Qué es
App Android (Kotlin + Jetpack Compose) para negocios en Perú que reciben pagos por billeteras (Yape, Plin, etc.).
El **dueño** crea negocios y métodos de cobro; los **trabajadores** entran con un código y reciben el aviso de cada
cobro. La app detecta el pago por notificaciones del celular del dueño, lo registra en Supabase y avisa al equipo.

- Paquete: `com.flabs.cobrate` · minSdk 24 · target/compileSdk 37 · versionCode 1 / versionName "1.0".
- Backend: Supabase, proyecto `ianiulmdyzsqtrqpujkx` (Auth, Postgres con RLS, Realtime, Storage, Edge Functions).
- Push: FCM con mensajes solo de datos, enviados por la Edge Function `enviar-push`.
- Web: sitio estático en Cloudflare Pages, proyecto `tokelabs` → https://tokelabs.pages.dev
- Soporte: AeroCorpKT@protonmail.com (correo de soporte y remitente de Supabase vía Brevo) y WhatsApp +51 998360217.

## 2. Comandos
```bash
# JAVA_HOME = JBR de Android Studio. Redirigir la salida a un archivo: Gradle tarda y se cuelga si no.
export JAVA_HOME="C:/Program Files/Android/Android Studio/jbr"
./gradlew :app:testDebugUnitTest :app:assembleDebug --console=plain -q > .artifacts/build.log 2>&1
./gradlew assembleRelease -PfirmarConDebug      # SOLO para probar el release en un teléfono propio

# Web (el login de wrangler ya está hecho)
wrangler pages deploy web --project-name tokelabs --branch main --commit-dirty=true
```
- Credenciales locales (no versionadas): `local.properties` (SUPABASE_URL, SUPABASE_ANON_KEY) y `app/google-services.json`.
- Herramientas instaladas: wrangler, `cf` CLI, `gh` (sin sesión iniciada: **no subir nada hasta que el usuario lo pida**).
- Plataforma de trabajo: Windows; el shell Bash tiene sintaxis POSIX. Para escribir archivos Kotlin usar la herramienta Write (los heredocs largos fallan).

## 3. Arquitectura (app/src/main/java/com/flabs/cobrate)
- `core/model` modelos · `core/logic` reglas puras con pruebas (AssignmentEngine, FechaCobro, VoicePhraseBuilder, LogSeguro, ErrorMapper…)
  · `core/data` repositorios y stores (CuentaStore, NegocioStore, RelacionCuentaNegocioStore, MetodoDeCobroStore, SupabaseAuthBridge, RealtimeSyncManager…).
- `features/` pantallas por función: `acceso` (bienvenida, login, componentes de acceso), `auth` (AuthViewModel, LoginScreen,
  `RecuperacionEnCurso`), `cuenta` (Mi cuenta), `actualizacion` (actualizador de APK), `business`, `crearnegocio`, `equipo`,
  `cobros`, `negocios`, `reportes`, `settings` (aviso de voz), `soporte` (manual, FAQ, Acerca de), `shared/HomeScreen`, `onboarding`.
- `components/` piezas reutilizables (TokeIcons, TokeBottomBar, OperacionCard, MasSheet, DialogoPermiso, BadgeSincronizacion…).
- `ui/theme/` `TokePaleta.kt` (paleta única), `TokeMedidas.kt` (medidas de botones y espacios), `TokeMaterial.kt`.
- `MainActivity.kt` navegación global: según la sesión va a Arranque → Bienvenida/Login → Inicio.
  **Ojo:** al verificar un código OTP, Supabase abre sesión y la navegación saltaría a Inicio. `RecuperacionEnCurso`
  pausa ese salto durante recuperar contraseña y durante el último paso del registro. Cualquier flujo nuevo que abra sesión antes de terminar debe usarla.

### Reglas del código que se acordaron
- Lógica separada de la UI y con pruebas unitarias. Sin hex de color sueltos: usar `tokePaleta()`.
- Un solo aspecto claro/oscuro = el interruptor de la app. Sin variantes oscuras inventadas, sin "glass", sin estética de IA genérica.
- Colores sólidos (no translúcidos) en insignias y avisos. Textos de diálogos cortos (≤3 líneas).
- Archivos **protegidos** (no modificar salvo autorización explícita; comparar md5 antes/después): `HomeScreen.kt`, `TokeBottomBar.kt`, `TokeIcons.kt` (solo se permite añadir entradas).
- Un commit por bloque de trabajo. Nada se da por hecho sin compilar y correr las pruebas.
- Nunca poner correos personales, claves ni tokens en el repositorio ni en servicios externos.

## 4. Roles y asignación de cobros
- El rol es **por negocio**, no por persona: `negocios.dueno_cuenta_id` = dueño; `relacion_cuenta_negocio` (rol TRABAJADOR/ADMINISTRADOR, estado ACTIVA/EXPULSADA…).
- Trabajadores = sesión **anónima** de Supabase (sin correo). Dueños = cuenta con correo verificado.
- Asignación: 0 trabajadores → "Sin asignar"; **1** → se asigna sola (`AssignmentEngine`); **2 o más** → quedan "Sin asignar" y cada
  trabajador **reclama** (`reclamar_operacion`, atómica: gana el primero). No hay reparto automático por turnos. Probado en vivo solo con 1 trabajador.
- Salir de un negocio borra la relación y devuelve a la bolsa los cobros asignados sin confirmar. Un expulsado conserva su relación (para enterarse).
- El dueño no recibe notificación de sus propios cobros; el trabajador recibe "Nuevo pago asignado".

## 5. Supabase (proyecto `ianiulmdyzsqtrqpujkx`)
- Tablas públicas con RLS activa (0 tablas sin RLS). Principales: cuentas, perfiles_publicos, negocios, relacion_cuenta_negocio, operaciones,
  cambios_de_estado, metodos_de_cobro, dispositivos, sesiones_activas (sesión única), planes_cuenta, config_planes, app_releases.
- RPC clave: aceptar_invitacion, reclamar_operacion, confirmar_pago, salir_del_negocio, eliminar_negocio, actualizar_negocio, equipo_de_negocio,
  estado_plan, plan_efectivo, reclamar_sesion/liberar_sesion, registrar_dispositivo, responder_solicitud, revocar_invitacion.
- Edge Functions: `enviar-push` (verify_jwt=false; **su código no está en `supabase/functions` del repo**), `check-app-release` (pública, lee `app_releases` con service_role),
  `eliminar-cuenta` (verify_jwt=true; reautentica con contraseña o con la frase ELIMINAR; 409 HAS_BUSINESSES salvo `cerrarNegocios=true`).
- Limpieza automática (pg_cron, cada hora minuto 15, `limpiar_usuarios_sin_usar()`): borra correos **sin verificar > 24 h** y sesiones **anónimas sin negocio y sin actividad > 7 días**.
  Las cuentas con correo verificado sin negocio **no** se borran. Expulsados: su relación impide el borrado (pendiente decidir un plazo, p. ej. 30 días).
- Vista de panel `public.v_usuarios` (solo lectura, sin acceso para la app): nombre, correo, tipo de cuenta, negocios propios/como trabajador y rol resumido.
- Correo de Auth: SMTP de Brevo, remitente AeroCorp. Plantillas **solo con `{{ .Token }}`** (sin enlace) — ver `docs/PLANTILLAS_CORREO_SUPABASE.md`.
  Ajustes requeridos: "Secure email change" apagado, Site URL = https://tokelabs.pages.dev, remitente verificado en Brevo. Límite de correos de Auth subido a 30/h.
- Un registro repetido de un correo ya confirmado **no envía correo** (comportamiento normal de Supabase); probar con otro correo o con "¿Olvidaste tu contraseña?".
- Migraciones en `supabase/migrations/` (la de limpieza y la vista están aplicadas en el servidor; la vista no tiene archivo propio todavía).

## 6. Cuenta, registro y recuperación (hecho)
- Registro en 2 pasos con código OTP, nombre y contraseña; el nombre se guarda en Auth (`username`, `display_name`, `full_name`, `name`) y en `cuentas`.
- Recuperar contraseña con código; "Mi cuenta": nombre (lápiz, sin verificación), correo (cambio con código al correo nuevo), contraseña (reautentica), eliminar cuenta.
- Avisos "Activa los avisos" y "Para no perder cobros" solo salen en Home (con al menos un negocio), no en "¿Cómo deseas empezar?".

## 7. Actualizador de APK (hecho en código; falta publicar una versión real)
- La app consulta `check-app-release`; compara `version_code` y `min_supported_version_code` (obligatoria u opcional), descarga con HttpURLConnection y
  **verifica**: SHA-256 del archivo, tamaño, nombre de paquete y **firma del certificado = la de la app instalada** (`PAQUETE_O_FIRMA`). Luego instala por FileProvider.
- Consecuencia: **todas las versiones públicas deben firmarse siempre con la misma llave**. Las pruebas actuales están firmadas con la llave de depuración; esas
  instalaciones deberán desinstalarse al pasar a la versión pública.
- `app_releases` está **vacía** (0 filas) → la app no ve ninguna versión publicada.

## 8. Web (https://tokelabs.pages.dev, carpeta `web/`)
- Estático, CSP `script-src 'self'` (sin scripts inline), archivos en `/assets` con hash e `immutable` → **al editar CSS/JS hay que renombrar con nuevo hash y actualizar los HTML**.
- Páginas: index, ayuda, contacto, descargas, eliminar-cuenta, licencias, novedades, privacidad, seguridad, terminos, 404. Tema claro por defecto con botón sol/luna;
  capturas reales del Samsung sin barra de estado; carrusel vertical en "Cómo funciona"; tutorial de instalación; iconos de WhatsApp/SMS vectorizados.
- La frase "Hecha con inteligencia artificial y supervisión humana estricta" va en Términos.
- Pendientes: botón de descarga con el enlace real del APK; revisar versión móvil y la tabla de precios; textos legales definitivos.

## 9. Pruebas y verificación en equipos reales
- 44 archivos de prueba unitaria en `app/src/test`; última corrida completa: **verde** (`testDebugUnitTest` + `assembleDebug`).
- Prueba en vivo con dos teléfonos (dueño Samsung S10, trabajador Redmi 9A): cobro registrado en el negocio, aviso al trabajador al instante, dueño sin aviso, bloqueo del actualizador. Resultado: correcto.
- Sin probar aún: registro de punta a punta tras el último arreglo, cambio de nombre con el lápiz, 2 trabajadores en un negocio, Mi cuenta con cambio de correo/contraseña/eliminación con login real,
  mensajes sin conexión, pantallas del trabajador sin red, correo de registro/recuperación con la app nueva instalada.

## 10. AUDITORÍA: ¿lista para publicar? **NO todavía.** Hoy el APK es de desarrollo.

### Bloqueantes (hay que resolverlos antes de dar el APK al público)
1. **No hay llave de firma ni configuración de firma de release.** `app/build.gradle.kts` solo firma con la llave de depuración si se pasa `-PfirmarConDebug`. Un `assembleRelease` normal sale sin firmar.
2. **R8/minificación apagada** (`optimization { enable = false }`, sin `shrinkResources`) y `proguard-rules.pro` casi vacío (solo serialización). Activar y probar todos los flujos
   (Supabase/Ktor, kotlinx-serialization, Firebase, ML Kit, Realtime) en un release real.
3. **Versión y publicación:** versionCode 1 / "1.0" sin esquema definido; `app_releases` vacía; sin sitio de descarga con el APK real; sin su SHA-256 publicado.
4. **Registros (Log):** 77 llamadas `Log.d/e/i/v` salen a logcat en release. `LogSeguro` solo sanea algunos casos. Quitarlas con R8 (`-assumenosideeffects`) o pasarlas por `LogSeguro` (que ya apaga en no-debug).
5. **`allowBackup="true"`:** revisar `backup_rules.xml`/`data_extraction_rules.xml`; una restauración en otro celular podría traer la sesión y chocar con la sesión única. Lo más seguro: `false` o excluir los datos de sesión.
6. **Textos legales:** política de privacidad y términos definitivos pendientes (se redactan juntos). Necesarios antes de abrir al público.
7. **Seguridad de Supabase** (avisos del asesor, ver sección 11).

### Importantes (antes del lanzamiento, no bloquean la prueba cerrada)
- Modo mantenimiento (tabla + función pública + pantalla en la app; solo pasan los 2 correos administradores, comprobado en el servidor): **no construido**.
- Ventanas pequeñas con texto cortado: revisar con el usuario cuáles son.
- Revisar cifrado/protección de datos privados de clientes en Supabase (proponer antes de aplicar).
- Repositorio **público de releases** solo con el APK (sin código fuente): crear cuando el usuario lo autorice; `gh` aún sin sesión.
- Decidir plazo de borrado de expulsados; probar el caso de 2 trabajadores; mensajes sin conexión.
- `README.md` está vacío/corrupto (UTF-16, "# Toque -"): reescribir. Archivos sin seguimiento a ignorar: `.artifacts/`, `.cloudflare/`, `.gradle_home/`, `.wrangler/`, `diag_output.txt`.
- Pagos del plan Pro: activación **manual** por WhatsApp (`plan_telefono_activacion`, ticket admin). No hay cobro automático; está bien para el lanzamiento si se comunica.
- `enviar-push`: traer su código al repo y confirmar que valida quién la llama (tiene `verify_jwt=false`).
- Existe un plan de pulido más amplio en `~/.claude/plans/probado-con-exito-a-adir-paso-witty-zephyr.md` (paleta fija, bienvenida/login fieles al diseño, Equipo, Reportes y Excel, aviso de voz,
  menú Más, tamaños de pantalla). Parte ya se aplicó (TokePaleta, TokeMedidas, colores sólidos); **contrastar contra el historial antes de darlo por terminado**.

### Lo que ya está bien
- Anon key en la app (pública por diseño) y `service_role` solo dentro de Edge Functions; todas las tablas con RLS; `eliminar-cuenta` y `check-app-release` desplegadas.
- Verificación del APK descargado (hash, tamaño, paquete y firma) y bloqueo de versión obligatoria funcionando en un Redmi real.
- Sin interruptores de "modo desarrollador" en la app: solo `BuildConfig.DEBUG` en `LogSeguro`. Lo que hace "dev" al APK actual es que es `debug` y va firmado con la llave de depuración.

## 11. Seguridad pendiente en Supabase (asesor de seguridad)
- **Protección contra contraseñas filtradas desactivada** → activar en Dashboard → Authentication.
- Funciones `SECURITY DEFINER` ejecutables por `anon`/`authenticated` desde `/rest/v1/rpc`: las de disparador (`tg_cuota_operaciones`, `tg_limite_*`, `validar_billetera_unica_por_dueno`) deben
  quitar `EXECUTE` a `anon`/`authenticated`; `es_dueno_de_negocio`, `es_miembro_de_negocio` y `obtener_preview_invitacion` revisar si `anon` realmente lo necesita (la última sí, para ver la invitación).
- `inicio_dia_lima_ms` sin `search_path` fijo; extensión `pg_net` en el esquema `public`.
- `app_releases` y `config_planes` con RLS y sin políticas: es intencional (solo service_role).
- Políticas que incluyen sesiones anónimas: es esperado (los trabajadores son anónimos); mantener las condiciones por negocio.

## 12. Procedimiento de release (hash y firma) — hacer en este orden
1. **Crear la llave una sola vez** (guardarla FUERA del repo y con copia en 2 lugares; perderla impide actualizar a todos los usuarios):
   ```bash
   keytool -genkeypair -v -keystore toke-release.jks -alias toke -keyalg RSA -keysize 4096 -validity 10000
   ```
2. Crear `keystore.properties` (ignorado por git) con ruta, alias y contraseñas; en `build.gradle.kts` añadir `signingConfigs.release` que lo lea y asignarlo a `buildTypes.release`
   con `isMinifyEnabled = true` y `isShrinkResources = true` (reemplazando `optimization { enable = false }`).
3. Subir `versionCode` (entero creciente) y `versionName`. Compilar: `./gradlew assembleRelease`.
4. Comprobar firma y hash:
   ```bash
   apksigner verify --print-certs app-release.apk        # debe mostrar la llave de release, NO "Android Debug"
   sha256sum app-release.apk                              # 64 caracteres hex en minúsculas
   ```
5. Subir el APK a un enlace **https** público (repositorio de releases solo-APK o almacenamiento) y anotar el tamaño exacto en bytes.
6. Registrar la versión (en Supabase → SQL, o con la herramienta de administración):
   ```sql
   insert into public.app_releases (application_id, version_code, version_name, min_supported_version_code,
                                    apk_url, apk_size_bytes, sha256, release_notes)
   values ('com.flabs.cobrate', 2, '1.0.0', 1, 'https://…/toke-1.0.0.apk', 12345678, '<sha256>', 'Qué cambia…');
   ```
   `min_supported_version_code` = la versión mínima permitida; las menores quedan **bloqueadas** hasta actualizar. `active=true`.
7. Instalar ese mismo APK en un teléfono de prueba, abrir la app y comprobar que el actualizador no marca error de firma, y probar una actualización desde una versión anterior firmada con la misma llave.
8. Poner el enlace en `web/descargas.html`, re-hashear assets si se tocó CSS/JS y desplegar con wrangler.

## 13. Siguiente trabajo sugerido (orden)
1. Crear la llave de firma + configuración de release + R8 + quitar logs + decidir `allowBackup`; compilar un release y probarlo en el Samsung y el Redmi.
2. Cerrar los avisos de seguridad de Supabase (sección 11) y activar la protección de contraseñas filtradas.
3. Probar registro/recuperación/nombre con la app nueva y el caso de 2 trabajadores con 3 teléfonos.
4. Textos legales (privacidad/términos) junto con el usuario; modo mantenimiento; repositorio público de releases.
5. Publicar la primera versión (sección 12) y recién entonces subir la rama y abrir el PR a `main` **si el usuario lo pide**.
