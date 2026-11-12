# Informe de consolidación (legal, privacidad y seguridad)

Fecha: 2026-11-12. Continúa desde `953feb9`. **Nada se publicó, desplegó ni tocó en producción.** No se modificó Supabase ni datos.

## 1. Documentos y web (local)
- Privacidad **v0.3**: roles (modelo de trabajo), conservación por tipo de dato, "no se usa para estadísticas propias", procedimiento para terceros (§11.1), texto original ya no se guarda, logs y copias automáticas, titularidad en regularización, ubicación de proveedores pendiente.
- Términos y Centro legal: titularidad en regularización (sin nombre ni "persona jurídica").
- Planes y pagos: aviso "los pagos de Pro se habilitarán…", reembolso "mismo medio, hasta 7 días hábiles", vencimiento de Pro con lo que de verdad ocurre.
- Landing: botón "Pro: próximamente". Logos de Yape y Plin reemplazados por etiquetas de texto; aviso "no afiliada, respaldada ni representa".
- Eliminar cuenta (trabajadores sin correo), Contacto y Seguridad (asunto "Vulnerabilidad de seguridad").
- Verificación: 0 enlaces rotos, un solo H1 por página, sin referencias a los logos antiguos.

## 2. Android autorizado: PRI-6 y SEG-3
**Qué pasaba:** el servicio guardaba título, texto y datos adicionales de cada notificación de pago en un DataStore local (`eventos_notificacion`), sin límite, y escribía `"$titulo: $texto"` en logcat. Había 55 llamadas directas a `Log`.

**Qué se hizo (archivos):**
| Archivo | Cambio |
|---------|--------|
| `services/TokeNotificationListenerService.kt` | El evento vive solo en memoria mientras se parsea; ya no se guarda, ni se marca, ni se imprime su texto. Quitados los montos de los logs. |
| `core/data/EventoDeNotificacionStore.kt`, `EventoDeNotificacionLocalStore.kt` | Reducidos a una limpieza: borran la copia que dejaron versiones anteriores. |
| `MainActivity.kt` | Llama a esa limpieza al abrir la app (aunque el servicio de notificaciones no se conecte). |
| `core/data/CuentaStore.kt`, `features/operaciones/DetalleOperacionScreen.kt` | Quitada la dependencia del almacén de eventos; el pagador sale de la operación normalizada. |
| `core/model/EventoDeNotificacion.kt` | Comentario corregido (ya no se persiste). |
| 17 archivos con `Log.*` | Todo pasa por `LogSeguro`. |
| `core/logic/LogSeguro.kt` | En la versión pública, `e` y `w` imprimen solo la clase del error, no su mensaje (puede traer filas o textos). |
| `core/logic/TabRefreshManager.kt`, `InMemoryOperationStore.kt`, `PlanStore.kt`, `QrEditorScreen.kt`, `CuentaStore.kt` | Quitado `${...message}` de los mensajes. |
| `res/xml/backup_rules.xml`, `data_extraction_rules.xml` | De lista de exclusión a **lista blanca**: solo `preferencias_app` (tema, avisos) y `configuracion_voz`. |
| `app/src/test/.../PrivacidadCodigoTest.kt` (nuevo) | Falla si alguien vuelve a usar `Log.*` directo, si el servicio guarda/imprime el texto, o si las copias incluyen algo fuera de la lista. |

**Cambia el comportamiento:** el título de la notificación ya no es el "pagador" de reserva. Si una operación no trae pagador, el detalle muestra "sin nombre".

**Protegidos:** `HomeScreen.kt` y `TokeIcons.kt` sin cambios (md5 idéntico). `TokeBottomBar.kt` solo tiene el deshacer de transiciones que ya estaba pendiente.

## 3. Evidencia y pruebas
- `./gradlew :app:testDebugUnitTest :app:assembleDebug`: **EXIT 0**; 51 archivos de prueba, 0 fallos, incluido `PrivacidadCodigoTest` (3 pruebas). Este mismo build verifica también el "deshacer de transiciones" pendiente.
- Búsqueda en el código: 0 llamadas a `Log.*` fuera de `LogSeguro.kt`.
- Web: generador, enlaces y estructura revisados.
- **No verificado:** la app en un celular. Falta probar que la detección, la asignación y los avisos siguen funcionando sin el almacén de eventos, y que una versión de producción (con R8 apagado todavía) no imprime nada sensible. El borrado de la copia antigua tampoco se probó en un celular.

## 4. ELI-2 (expulsados a 30 días): documentado, no construido
- Hoy el vínculo de un expulsado queda en `relacion_cuenta_negocio` con `estado = 'EXPULSADA'` y no hay tarea que lo borre. La limpieza automática no borra cuentas anónimas que tengan vínculos.
- La tabla **no guarda la fecha de expulsión** (solo `fecha_vinculacion`), así que no se puede contar 30 días sin cambiar la base.
- Propuesta (necesita tu autorización, no se aplicó): añadir `expulsada_en timestamptz`, rellenarla al expulsar (en la función que cambia el estado) y una tarea programada diaria que borre las filas `EXPULSADA` con más de 30 días. Borra solo esa fila (cuenta + negocio), por lo que otros negocios del mismo trabajador no se tocan. El historial de cobros no cambia (ya quedó sin nombre del trabajador: `trabajador_asignado_id` pasa a nulo al borrar su cuenta, no al borrar el vínculo).

## 5. Proveedores y accesos
| Tema | Resultado |
|------|-----------|
| Vercel | **No se usa**: sin rastro en el repositorio ni en la web (va en Cloudflare Pages). No se declara. |
| Supabase | Región `sa-east-1` (São Paulo), plan Free (verificado). Sin copias de seguridad para el cliente declaradas. |
| Firebase (FCM) | Integrado (dependencia y servicio de mensajes). Región/país: no confirmado (servicio global). |
| Cloudflare | Aloja la web (Pages). Región: no confirmada. |
| Brevo | Solo consta en la documentación del proyecto (SMTP de Auth). No se pudo ver la configuración de Auth desde esta sesión. |
| Contratos/acuerdos de tratamiento | No hay copias en el proyecto. |
| Quién accede a paneles y datos | No verificable (los paneles de Supabase, Firebase y Cloudflare no listan miembros desde estas herramientas). |
| Transferencias internacionales | Hay datos en Brasil y proveedores globales; se declaran sin afirmar que se cumplan los requisitos. |

## 6. COM-3 (vencimiento de Pro): lo que ocurre de verdad
- Al pasar `pro_hasta`, `plan_efectivo` devuelve FREE solo.
- Se bloquean las altas por encima de los límites (negocios, trabajadores, métodos) y el aviso diario vuelve a 20.
- No se borra nada. **No existe "solo lectura"**: lo existente se puede seguir editando. El texto público dice solo lo que existe. Si quieres solo lectura real, es trabajo nuevo en la base y en la app.

## 7. Pendientes de información o decisión (ver también el cuestionario)
1. Datos para regularizar a la persona natural (ver `REGULARIZACION_PERSONA_NATURAL_TOKE.md`).
2. Texto oficial de las normas: leer a mano en El Peruano (la sesión no pudo). Inscripción de bancos de datos, Oficial de Datos, notificación de incidentes, plazos ARCO, Libro de Reclamaciones (Ley 32495), contratación a distancia, RUC y comprobantes.
3. **Botón "Activar plan Pro" de la app**: abre WhatsApp aunque la web diga que Pro es "próximamente". Decide si se oculta o se deja (cambio en la app no autorizado).
4. Autorizar ELI-2 (cambio en Supabase).
5. Regiones de Firebase, Brevo y Cloudflare, acuerdos y quién accede a paneles.
6. Probar la app en un celular con los cambios de Android.
7. El Libro de Reclamaciones sigue retirado.

## 8. Riesgos abiertos
- La app publicada seguiría guardando el texto y escribiendo logs hasta que salga una versión nueva: **publicar la web (Privacidad 0.3) junto con la versión nueva de la app**, no antes.
- Los documentos son redactados por el equipo, sin abogado ni verificación oficial de las normas: **no se afirma que TOKE esté regularizado ni cumpla la normativa**.
- R8 sigue apagado y sin llave de firma (ver `CLAUDE.md`): la versión pública aún no está lista.
