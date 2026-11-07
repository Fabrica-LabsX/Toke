-- La cuota diaria del plan Free es de la CUENTA dueña, no de cada negocio: se comparte entre todos sus negocios.
-- Los cobros que llegan mientras la cuenta es Pro no gastan cuota: al vencer el Pro, la cuota vuelve a empezar llena.
alter table public.operaciones
  add column if not exists cuenta_cuota boolean not null default true;

create or replace function public.tg_cuota_operaciones() returns trigger
language plpgsql security definer set search_path = public as $$
declare dueno uuid; cuota integer; usadas integer;
begin
  select dueno_cuenta_id into dueno from public.negocios where id = new.negocio_id;
  if dueno is null then return new; end if;
  if public.plan_efectivo(dueno) = 'PRO' then
    new.cuenta_cuota := false;
    return new;
  end if;
  new.cuenta_cuota := true;
  select cuota_diaria_free into cuota from public.config_planes where id = 1;
  select count(*) into usadas from public.operaciones o
   join public.negocios n on n.id = o.negocio_id
   where n.dueno_cuenta_id = dueno and o.cuenta_cuota and o.fecha_creacion >= public.inicio_dia_lima_ms();
  if usadas >= cuota then
    raise exception 'CUOTA_AGOTADA: se acabó la cuota de hoy del plan Free' using errcode = 'P0001';
  end if;
  return new;
end;
$$;

create or replace function public.estado_plan(p_negocio_id uuid) returns jsonb
language plpgsql stable security definer set search_path = public as $$
declare
  dueno uuid; plan_actual text; limite integer; usadas integer; hasta timestamptz; trabajadores integer;
begin
  if auth.uid() is null then raise exception 'No autenticado'; end if;
  select dueno_cuenta_id into dueno from public.negocios where id = p_negocio_id;
  if dueno is null then raise exception 'Negocio no encontrado'; end if;
  if dueno <> auth.uid() and not public.es_miembro_de_negocio(p_negocio_id) then
    raise exception 'No autorizado';
  end if;

  plan_actual := public.plan_efectivo(dueno);
  select pro_hasta into hasta from public.planes_cuenta where cuenta_id = dueno and plan = 'PRO';
  select count(*) into usadas from public.operaciones o
   join public.negocios n on n.id = o.negocio_id
   where n.dueno_cuenta_id = dueno and o.cuenta_cuota and o.fecha_creacion >= public.inicio_dia_lima_ms();
  select count(*) into trabajadores from public.relacion_cuenta_negocio
   where negocio_id = p_negocio_id and rol = 'TRABAJADOR' and estado = 'ACTIVA';
  limite := case when plan_actual = 'PRO' then null
                 else (select cuota_diaria_free from public.config_planes where id = 1) end;

  return jsonb_build_object(
    'plan', plan_actual,
    'pro_hasta', case when hasta is null then null else (extract(epoch from hasta) * 1000)::bigint end,
    'limite', limite,
    'usadas', usadas,
    'restantes', case when limite is null then null else greatest(limite - usadas, 0) end,
    'max_colaboradores', case when plan_actual = 'PRO' then null
                              else (select max_colaboradores_free from public.config_planes where id = 1) end,
    'trabajadores_activos', trabajadores
  );
end;
$$;
