# Glosario de SQL para el administrador de TOKE

Se ejecutan en Supabase → SQL Editor (proyecto `ianiulmdyzsqtrqpujkx`), con el rol del panel (postgres). Las funciones `admin_*` **solo** las puede ejecutar `postgres`/`service_role`: la app y los usuarios no pueden llamarlas. Cambia lo que está en `MAYÚSCULAS_ENTRE_COMILLAS`.
**Regla de oro:** primero un `select` para ver a quién afecta; solo después un `update`/`delete`. Los `delete` son irreversibles y TOKE no tiene copias de seguridad.

## 0. El código personalizado de una cuenta
Es `TOKE-` + los 8 primeros caracteres hexadecimales del id de la cuenta (por ejemplo `TOKE-E67F794A`). La app lo muestra en "Mi plan". Con un código de 8 o más caracteres se identifica a una sola cuenta; si coinciden dos, la función avisa.

## 1. Buscar personas
```sql
-- Por código personalizado (devuelve correo, id, plan guardado, vencimiento y plan efectivo)
select * from public.admin_buscar_codigo('TOKE-XXXXXXXX');

-- Por correo (o parte del correo)
select * from public.v_usuarios where correo ilike '%CORREO%' order by creada desc;

-- Por nombre
select * from public.v_usuarios where nombre ilike '%NOMBRE%';

-- Los últimos 20 registrados, con su código personalizado
select 'TOKE-' || upper(left(replace(id::text,'-',''),8)) as codigo, nombre, correo, tipo_cuenta,
       negocios_propios, negocios_como_trabajador, creada, ultimo_acceso
from public.v_usuarios order by creada desc limit 20;

-- Convertir un código en el id completo
select id, email from auth.users where replace(id::text,'-','') ilike 'E67F794A%';
```

## 2. Planes (Pro / Free)
```sql
-- Ver el plan de un correo
select * from public.admin_ver_plan('CORREO');

-- Activar Pro por N días a un correo  (30 = mensual, 90 = pack de 3 meses)
select public.admin_activar_pro('CORREO', 30);

-- Activar Pro a quien tenga un código personalizado
select public.admin_activar_pro_codigo('TOKE-XXXXXXXX', 30);

-- Pro sin vencimiento (usar solo para pruebas internas)
select public.admin_activar_pro('CORREO', null);

-- Devolver a Free (reembolso o fin del periodo); no borra datos
select public.admin_quitar_pro('CORREO');

-- Pro para todos durante N días (promoción) / apagarlo con 0
select public.admin_pro_para_todos(7);
select public.admin_pro_para_todos(0);

-- Quién tiene Pro hoy y hasta cuándo
select u.email, p.plan, p.pro_hasta, p.nota
from public.planes_cuenta p join auth.users u on u.id = p.cuenta_id
where p.plan = 'PRO' order by p.pro_hasta nulls last;

-- Pro que vence en los próximos 7 días
select u.email, p.pro_hasta from public.planes_cuenta p join auth.users u on u.id = p.cuenta_id
where p.plan = 'PRO' and p.pro_hasta between now() and now() + interval '7 days' order by p.pro_hasta;

-- Anotar el pago (referencia, medio, fecha) en la cuenta
update public.planes_cuenta set nota = 'Yape 14.90 – 12/11/2026 – op 123456' where cuenta_id = 'ID_DE_LA_CUENTA';

-- Límites del plan Free y "Pro para todos"
select * from public.config_planes;
update public.config_planes set cuota_diaria_free = 20, max_negocios_free = 1, max_colaboradores_free = 2, max_metodos_free = 4 where id = 1;
```
Los períodos del plan: mensual 30 días; pack de 3 meses 90 días (S/ 40). Al vencer, la cuenta vuelve sola a Free sin borrar datos.

## 3. Negocios, equipo y cobros
```sql
-- Negocios de un dueño (por correo)
select n.id, n.nombre, n.tipo, n.max_trabajadores
from public.negocios n join auth.users u on u.id = n.dueno_cuenta_id where lower(u.email) = lower('CORREO');

-- Equipo de un negocio
select r.rol, r.estado, r.fecha_vinculacion, r.codigo_usado, p.nombre_visible
from public.relacion_cuenta_negocio r left join public.perfiles_publicos p on p.id = r.cuenta_id
where r.negocio_id = 'ID_DEL_NEGOCIO';

-- Últimos cobros de un negocio
select to_timestamp(fecha_hora/1000.0) at time zone 'America/Lima' as hora, monto, metodo_pago, estado, trabajador_asignado_id
from public.operaciones where negocio_id = 'ID_DEL_NEGOCIO' order by fecha_hora desc limit 30;

-- Total cobrado hoy por negocio (hora de Lima)
select n.nombre, count(*) cobros, sum(o.monto) total
from public.operaciones o join public.negocios n on n.id = o.negocio_id
where o.fecha_hora >= public.inicio_dia_lima_ms() group by n.nombre order by total desc;

-- Cobros por día de los últimos 14 días
select (to_timestamp(fecha_hora/1000.0) at time zone 'America/Lima')::date dia, count(*) cobros, sum(monto) total
from public.operaciones where fecha_hora >= (extract(epoch from now() - interval '14 days')*1000)::bigint
group by 1 order by 1 desc;

-- Métodos de cobro de un negocio
select tipo, nombre_destinado, activo, oculto, qr_url is not null as tiene_qr
from public.metodos_de_cobro where negocio_id = 'ID_DEL_NEGOCIO';

-- Expulsados (quién y desde cuándo se conserva el vínculo)
select r.negocio_id, r.cuenta_id, r.fecha_vinculacion from public.relacion_cuenta_negocio r where r.estado = 'EXPULSADA';
```

## 4. Invitaciones y códigos de trabajador
```sql
-- Buscar una invitación por su código (el que escribe el trabajador)
select codigo, negocio_id, nombre_propuesto, metodo, estado, requiere_aprobacion,
       to_timestamp(expiracion/1000.0) at time zone 'America/Lima' as vence
from public.invitaciones_trabajador where codigo = 'CODIGO';

-- Invitaciones vigentes de un negocio
select codigo, estado, to_timestamp(expiracion/1000.0) vence from public.invitaciones_trabajador
where negocio_id = 'ID_DEL_NEGOCIO' and estado = 'PENDIENTE' and expiracion > extract(epoch from now())*1000;

-- Quién usó un código y en qué negocio
select cuenta_id, negocio_id, rol, estado from public.relacion_cuenta_negocio where codigo_usado = 'CODIGO';

-- Revocar una invitación
update public.invitaciones_trabajador set estado = 'REVOCADA' where codigo = 'CODIGO' and estado = 'PENDIENTE';
```
Estados de una invitación: `PENDIENTE`, `ACEPTADA`, `RECHAZADA`, `EXPIRADA`, `REVOCADA`. Estados de un vínculo: `ACTIVA`, `PAUSADA`, `EXPULSADA` (y los de solicitud que use la app).

## 5. Sesión única y dispositivos
```sql
-- Dónde tiene la sesión abierta una cuenta
select s.cuenta_id, s.dispositivo_nombre, to_timestamp(s.iniciada_en/1000.0) desde
from public.sesiones_activas s join auth.users u on u.id = s.cuenta_id where lower(u.email) = lower('CORREO');

-- Liberar la sesión (la persona podrá entrar desde otro celular)
delete from public.sesiones_activas where cuenta_id = 'ID_DE_LA_CUENTA';

-- Dispositivos registrados para avisos (token FCM; no mostrar a terceros)
select cuenta_id, plataforma, to_timestamp(actualizado_en/1000.0) from public.dispositivos where cuenta_id = 'ID_DE_LA_CUENTA';
```

## 6. Eliminar datos (irreversible)
```sql
-- Primero mirar qué se llevaría un negocio
select (select count(*) from public.operaciones where negocio_id = 'ID') cobros,
       (select count(*) from public.metodos_de_cobro where negocio_id = 'ID') metodos,
       (select count(*) from public.relacion_cuenta_negocio where negocio_id = 'ID') miembros;

-- Eliminar un negocio con todo lo suyo (cobros, métodos, vínculos, invitaciones); sus QR salen en <10 minutos
delete from public.negocios where id = 'ID_DEL_NEGOCIO';

-- Eliminar una cuenta (en cascada: cuenta, vínculos, dispositivos, plan, sesiones y, si es dueña, sus negocios)
-- Mejor pedirle que use "Eliminar cuenta" en la app: borra también los QR al momento.
delete from auth.users where id = 'ID_DE_LA_CUENTA';
```

## 7. QR, limpieza y tareas programadas
```sql
-- Archivos QR con su dueño y estado
select * from public.v_qr_archivos order by created_at desc;

-- Cola de QR por borrar
select * from public.qr_por_borrar order by id;

-- Tareas programadas y su último resultado
select jobid, jobname, schedule, active from cron.job;
select jobid, status, return_message, start_time from cron.job_run_details order by start_time desc limit 10;

-- Ejecutar la limpieza de usuarios sin usar ahora (borra correos sin verificar >24 h y anónimos sin negocio >7 días)
select public.limpiar_usuarios_sin_usar();

-- Respuestas recientes de las llamadas HTTP de los triggers (push y limpieza de QR)
select id, status_code, left(content, 120), created from net._http_response order by created desc limit 10;
```

## 8. Versiones de la app (APK)
```sql
-- Versiones publicadas
select version_code, version_name, min_supported_version_code, active, published_at, apk_url from public.app_releases order by version_code desc;

-- Publicar una versión (pasos completos en CLAUDE.md §12)
insert into public.app_releases (application_id, version_code, version_name, min_supported_version_code, apk_url, apk_size_bytes, sha256, release_notes)
values ('com.flabs.cobrate', 2, '1.0.0', 1, 'https://…/toke-1.0.0.apk', 12345678, 'SHA256_EN_MINUSCULAS', 'Qué cambia');

-- Obligar a actualizar (las versiones menores quedan bloqueadas)
update public.app_releases set min_supported_version_code = 2 where version_code = 2;

-- Retirar una versión
update public.app_releases set active = false where version_code = 2;
```

## 9. Diagnóstico
```sql
-- Cuentas sin verificar (se borran solas a las 24 h)
select id, email, created_at from auth.users where email_confirmed_at is null and is_anonymous is not true order by created_at;

-- Cuántas cuentas hay por tipo
select tipo_cuenta, count(*) from public.v_usuarios group by 1;

-- Políticas de seguridad vigentes (RLS)
select tablename, policyname, cmd from pg_policies where schemaname in ('public','storage') order by 1,2;

-- Tablas sin RLS (debe devolver 0 filas)
select relname from pg_class c join pg_namespace n on n.oid = c.relnamespace where n.nspname='public' and c.relkind='r' and not c.relrowsecurity;
```

## 10. Cuidado
- Nunca pegues claves, tokens ni el secreto de `push_webhook_secret` en chats, capturas o este repositorio.
- `service_role` y las funciones `admin_*` no deben llamarse desde la app ni desde la web.
- Antes de un `update`/`delete` sin `where`, para: afectará a todas las filas.
