# Informe de seguridad TOKE (revisión del 2026-11-11)

Regla: solo se marca "comprobado" lo que se verificó de verdad. Lo demás queda como pendiente.

## Controles existentes (comprobados leyendo el servidor)
| Control | Estado |
|---|---|
| RLS activa en todas las tablas públicas | Comprobado: 0 tablas sin RLS |
| Políticas sin `USING (true)` / `WITH CHECK (true)` | Comprobado: ninguna política abierta |
| Operaciones: sin política UPDATE; el estado solo cambia por RPC (`reclamar_operacion`, `confirmar_pago`) | Comprobado |
| Trabajador solo ve operaciones asignadas a él o sin asignar de métodos visibles de su negocio | Comprobado en la política `operaciones_select_miembro` |
| Invitaciones: solo cuentas no anónimas y dueñas del negocio | Comprobado |
| Cambiar de dueño un negocio por UPDATE | Bloqueado: el `WITH CHECK` implícito exige `dueno_cuenta_id = auth.uid()` |
| `service_role` fuera del APK | Comprobado: la app solo usa la anon key |
| Bucket `metodos-qr` privado | Comprobado |
| Contraseñas y hashing | Los administra Supabase Auth; la app no guarda contraseñas |
| Eliminación de cuenta por Edge Function con reautenticación | Hecho; **falta probarla con un login real** |
| Verificación del APK (SHA-256, paquete y firma) | Hecho en código; falta publicar una versión real |

## Cambios hechos hoy
1. Migración `endurecer_funciones_trigger`: se quitó `EXECUTE` a `public/anon/authenticated` de las 5 funciones de disparador
   (`tg_cuota_operaciones`, `tg_limite_colaboradores`, `tg_limite_metodos`, `tg_limite_negocios`, `validar_billetera_unica_por_dueno`).
   Ya no se pueden llamar desde `/rest/v1/rpc`. Los triggers siguen disparándose. `inicio_dia_lima_ms` ahora tiene `search_path` fijo.
2. Bucket `metodos-qr`: límite de 5 MB por archivo.
3. App: filtro de entradas (`EntradaSegura`) en nombres, correo, datos de cobro y titular: quita caracteres de control/invisibles
   y rechaza patrones de inyección SQL/HTML. Con pruebas unitarias. **Nota:** la defensa real contra SQL injection es que la app solo usa PostgREST/RPC con parámetros;
   el filtro es una segunda capa.
4. Limpieza automática de usuarios sin verificar y vista `v_usuarios` sin acceso para la app (hechos antes).

## Vulnerabilidades y hallazgos pendientes
| # | Hallazgo | Gravedad | Corrección propuesta |
|---|---|---|---|
| 1 | `cambios_insert_via_operacion`: un trabajador puede insertar filas en `cambios_de_estado` de operaciones que tiene asignadas (podría falsear su historial) | Media | Quitar el INSERT directo y escribir el historial solo desde las RPC; probar que la app no lo usa |
| 2 | Protección contra contraseñas filtradas **desactivada** | Media | Activar en Dashboard → Authentication → Password security (no se puede por API) |
| 3 | `es_dueno_de_negocio` y `es_miembro_de_negocio` ejecutables por `anon` | Baja | Las usan las políticas; cambiar las políticas de `public` a `authenticated` y luego revocar a `anon` |
| 4 | Varias políticas están en el rol `public` (incluye `anon`) aunque dependen de `auth.uid()` | Baja | Pasarlas a `authenticated` |
| 5 | `enviar-push` con `verify_jwt=false` y su código no está en el repositorio | Alta hasta revisar | Traer el código, exigir autenticación o un secreto, y probar |
| 6 | Bucket sin restricción de tipo de archivo | Baja | Añadir `allowed_mime_types` tras comprobar con qué tipo sube la app (hoy sube bytes con nombre `.png`) |
| 7 | `pg_net` instalada en `public` | Baja | Moverla a `extensions` con cuidado (puede afectar webhooks) |
| 8 | Límite de intentos para códigos de invitación | Por revisar | Comprobar la función `aceptar_invitacion` y añadir bloqueo por cuenta/IP |
| 9 | `allowBackup="true"` y 77 llamadas `Log` en release | Media | Ver CLAUDE.md sección 10 |
| 10 | CORS | No aplica | No se añadió ningún dominio: la app Android no usa CORS |

## Pruebas de acceso pendientes (no se han hecho)
Probar con sesión real, mostrando el resultado de cada consulta: dueño, trabajador del mismo negocio, trabajador de otro negocio, usuario no autenticado.
Hasta correr esas pruebas, **no se declara la autorización como verificada**.

## Datos personales
- Pendiente: aviso de privacidad y términos definitivos, plazos de conservación, lista de terceros (Supabase, Firebase/FCM, Brevo, Cloudflare, ML Kit) y su finalidad, y revisión legal en Perú (incluido el tratamiento de menores).
- Pendiente: auditar qué recopilan los SDK (Firebase Messaging, ML Kit, Coil).
