-- Planes Free y Pro: quién es Pro, cuota diaria de notificaciones y límites, todo decidido en el servidor.
-- El plan lo manda la cuenta DUEÑA del negocio. La app solo lo muestra; no puede cambiarlo.

-- 1) Qué plan tiene cada cuenta (sin fila = Free).
create table if not exists public.planes_cuenta (
  cuenta_id uuid primary key references public.cuentas(id) on delete cascade,
  plan text not null default 'FREE' check (plan in ('FREE', 'PRO')),
  pro_hasta timestamptz,            -- nulo con PRO = sin vencimiento
  nota text,
  actualizado_en timestamptz not null default now()
);
alter table public.planes_cuenta enable row level security;
create policy planes_select_propio on public.planes_cuenta
  for select to authenticated using (cuenta_id = auth.uid());
-- Nadie escribe desde la app: solo las funciones admin_* (desde el panel de Supabase).

-- 2) Ajustes globales (una sola fila).
create table if not exists public.config_planes (
  id integer primary key default 1 check (id = 1),
  pro_para_todos_hasta timestamptz,
  cuota_diaria_free integer not null default 20 check (cuota_diaria_free >= 0),
  max_negocios_free integer not null default 1 check (max_negocios_free >= 1),
  max_colaboradores_free integer not null default 2 check (max_colaboradores_free >= 0)
);
insert into public.config_planes (id) values (1) on conflict do nothing;
alter table public.config_planes enable row level security;  -- sin políticas: solo lo leen las funciones

-- 3) Plan efectivo de una cuenta ahora mismo.
create or replace function public.plan_efectivo(p_cuenta uuid) returns text
language sql stable security definer set search_path = public as $$
  select case
    when exists (select 1 from public.config_planes c
                  where c.pro_para_todos_hasta is not null and c.pro_para_todos_hasta > now())
      or exists (select 1 from public.planes_cuenta p
                  where p.cuenta_id = p_cuenta and p.plan = 'PRO'
                    and (p.pro_hasta is null or p.pro_hasta > now()))
    then 'PRO' else 'FREE' end;
$$;

-- Inicio del día de hoy en Lima, en milisegundos (la cuota se reinicia a las 12:00 a. m. de Perú).
create or replace function public.inicio_dia_lima_ms() returns bigint
language sql stable as $$
  select (extract(epoch from (date_trunc('day', now() at time zone 'America/Lima') at time zone 'America/Lima')) * 1000)::bigint;
$$;

-- 4) Lo que la app consulta para pintar el plan y la barra de cuota. Solo miembros del negocio.
create or replace function public.estado_plan(p_negocio_id uuid) returns jsonb
language plpgsql stable security definer set search_path = public as $$
declare
  dueno uuid; plan_actual text; limite integer; usadas integer; hasta timestamptz;
begin
  if auth.uid() is null then raise exception 'No autenticado'; end if;
  select dueno_cuenta_id into dueno from public.negocios where id = p_negocio_id;
  if dueno is null then raise exception 'Negocio no encontrado'; end if;
  if dueno <> auth.uid() and not public.es_miembro_de_negocio(p_negocio_id) then
    raise exception 'No autorizado';
  end if;

  plan_actual := public.plan_efectivo(dueno);
  select pro_hasta into hasta from public.planes_cuenta where cuenta_id = dueno and plan = 'PRO';
  select count(*) into usadas from public.operaciones
   where negocio_id = p_negocio_id and fecha_creacion >= public.inicio_dia_lima_ms();
  limite := case when plan_actual = 'PRO' then null
                 else (select cuota_diaria_free from public.config_planes where id = 1) end;

  return jsonb_build_object(
    'plan', plan_actual,
    'pro_hasta', case when hasta is null then null else (extract(epoch from hasta) * 1000)::bigint end,
    'limite', limite,
    'usadas', usadas,
    'restantes', case when limite is null then null else greatest(limite - usadas, 0) end,
    'max_colaboradores', case when plan_actual = 'PRO' then null
                              else (select max_colaboradores_free from public.config_planes where id = 1) end
  );
end;
$$;

-- 5) Barreras: el servidor rechaza lo que el plan no permite, aunque la app se modifique.

-- 5a) Cuota diaria de notificaciones (cobros detectados) en Free.
create or replace function public.tg_cuota_operaciones() returns trigger
language plpgsql security definer set search_path = public as $$
declare dueno uuid; cuota integer; usadas integer;
begin
  select dueno_cuenta_id into dueno from public.negocios where id = new.negocio_id;
  if dueno is null or public.plan_efectivo(dueno) = 'PRO' then return new; end if;
  select cuota_diaria_free into cuota from public.config_planes where id = 1;
  select count(*) into usadas from public.operaciones
   where negocio_id = new.negocio_id and fecha_creacion >= public.inicio_dia_lima_ms();
  if usadas >= cuota then
    raise exception 'CUOTA_AGOTADA: se acabó la cuota de hoy del plan Free' using errcode = 'P0001';
  end if;
  return new;
end;
$$;
drop trigger if exists trg_cuota_operaciones on public.operaciones;
create trigger trg_cuota_operaciones before insert on public.operaciones
  for each row execute function public.tg_cuota_operaciones();

-- 5b) Un solo negocio en Free (los que ya existen se conservan).
create or replace function public.tg_limite_negocios() returns trigger
language plpgsql security definer set search_path = public as $$
declare tope integer; cantidad integer;
begin
  if public.plan_efectivo(new.dueno_cuenta_id) = 'PRO' then return new; end if;
  select max_negocios_free into tope from public.config_planes where id = 1;
  select count(*) into cantidad from public.negocios where dueno_cuenta_id = new.dueno_cuenta_id;
  if cantidad >= tope then
    raise exception 'LIMITE_NEGOCIOS: el plan Free permite % negocio(s)', tope using errcode = 'P0001';
  end if;
  return new;
end;
$$;
drop trigger if exists trg_limite_negocios on public.negocios;
create trigger trg_limite_negocios before insert on public.negocios
  for each row execute function public.tg_limite_negocios();

-- 5c) Hasta 2 colaboradores en Free (activos, en pausa o esperando aprobación).
create or replace function public.tg_limite_colaboradores() returns trigger
language plpgsql security definer set search_path = public as $$
declare dueno uuid; tope integer; cantidad integer;
begin
  if new.rol <> 'TRABAJADOR' then return new; end if;
  select dueno_cuenta_id into dueno from public.negocios where id = new.negocio_id;
  if dueno is null or public.plan_efectivo(dueno) = 'PRO' then return new; end if;
  select max_colaboradores_free into tope from public.config_planes where id = 1;
  select count(*) into cantidad from public.relacion_cuenta_negocio
   where negocio_id = new.negocio_id and rol = 'TRABAJADOR' and estado in ('ACTIVA', 'PAUSADA', 'SOLICITADA');
  if cantidad >= tope then
    raise exception 'LIMITE_COLABORADORES: el plan Free permite % colaboradores', tope using errcode = 'P0001';
  end if;
  return new;
end;
$$;
drop trigger if exists trg_limite_colaboradores on public.relacion_cuenta_negocio;
create trigger trg_limite_colaboradores before insert on public.relacion_cuenta_negocio
  for each row execute function public.tg_limite_colaboradores();

-- 6) Activación manual desde el panel de Supabase (SQL Editor). Nadie más puede llamarlas.
create or replace function public.admin_activar_pro(p_correo text, p_dias integer default null) returns text
language plpgsql security definer set search_path = public as $$
declare uid uuid; hasta timestamptz;
begin
  select id into uid from auth.users where lower(email) = lower(btrim(p_correo));
  if uid is null then raise exception 'No existe una cuenta con ese correo'; end if;
  hasta := case when p_dias is null then null else now() + make_interval(days => p_dias) end;
  insert into public.planes_cuenta (cuenta_id, plan, pro_hasta, actualizado_en)
  values (uid, 'PRO', hasta, now())
  on conflict (cuenta_id) do update set plan = 'PRO', pro_hasta = excluded.pro_hasta, actualizado_en = now();
  return 'PRO activo para ' || p_correo || case when hasta is null then ' sin vencimiento' else ' hasta ' || hasta::text end;
end;
$$;

create or replace function public.admin_quitar_pro(p_correo text) returns text
language plpgsql security definer set search_path = public as $$
declare uid uuid;
begin
  select id into uid from auth.users where lower(email) = lower(btrim(p_correo));
  if uid is null then raise exception 'No existe una cuenta con ese correo'; end if;
  update public.planes_cuenta set plan = 'FREE', pro_hasta = null, actualizado_en = now() where cuenta_id = uid;
  return 'Plan Free para ' || p_correo;
end;
$$;

-- Pro para todos por un tiempo (p_dias = 0 lo apaga).
create or replace function public.admin_pro_para_todos(p_dias integer) returns text
language plpgsql security definer set search_path = public as $$
begin
  update public.config_planes
     set pro_para_todos_hasta = case when p_dias <= 0 then null else now() + make_interval(days => p_dias) end
   where id = 1;
  return case when p_dias <= 0 then 'Pro para todos apagado' else 'Pro para todos por ' || p_dias || ' días' end;
end;
$$;

-- Ver el plan real de una cuenta (identidad: correo, plan guardado y plan efectivo ahora).
create or replace function public.admin_ver_plan(p_correo text)
returns table (correo text, cuenta_id uuid, plan_guardado text, pro_hasta timestamptz, plan_efectivo text)
language sql stable security definer set search_path = public as $$
  select u.email::text, u.id, coalesce(p.plan, 'FREE'), p.pro_hasta, public.plan_efectivo(u.id)
    from auth.users u left join public.planes_cuenta p on p.cuenta_id = u.id
   where lower(u.email) = lower(btrim(p_correo));
$$;

revoke all on function public.admin_activar_pro(text, integer) from public, anon, authenticated;
revoke all on function public.admin_quitar_pro(text) from public, anon, authenticated;
revoke all on function public.admin_pro_para_todos(integer) from public, anon, authenticated;
revoke all on function public.admin_ver_plan(text) from public, anon, authenticated;
revoke all on function public.plan_efectivo(uuid) from public, anon;
grant execute on function public.plan_efectivo(uuid) to authenticated;
revoke all on function public.estado_plan(uuid) from public, anon;
grant execute on function public.estado_plan(uuid) to authenticated;
