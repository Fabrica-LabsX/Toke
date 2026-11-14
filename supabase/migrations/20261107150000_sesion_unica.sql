-- Sesión única: una cuenta solo puede estar abierta en un celular a la vez.
-- Aditiva. SIN APLICAR: esperando visto bueno (ver docs/PROTOCOLO_SESION_UNICA.md).

create table if not exists public.sesiones_activas (
  cuenta_id uuid primary key references public.cuentas(id) on delete cascade,
  dispositivo_id text not null,
  dispositivo_nombre text not null,
  iniciada_en bigint not null default ((extract(epoch from now()) * 1000)::bigint)
);

alter table public.sesiones_activas enable row level security;

-- Cada persona solo ve su propia fila (la necesita para saber si la sacaron).
create policy sesiones_select_propia on public.sesiones_activas
  for select to authenticated using (cuenta_id = auth.uid());

-- Las escrituras solo se hacen con las funciones de abajo.

create or replace function public.reclamar_sesion(
  p_dispositivo_id text,
  p_dispositivo_nombre text,
  p_forzar boolean default false
) returns jsonb
language plpgsql
security definer
set search_path = public
as $$
declare
  actual public.sesiones_activas;
begin
  if auth.uid() is null then
    raise exception 'No autenticado';
  end if;
  if p_dispositivo_id is null or char_length(btrim(p_dispositivo_id)) < 8 then
    raise exception 'Dispositivo no válido';
  end if;

  select * into actual from public.sesiones_activas where cuenta_id = auth.uid();

  if actual.cuenta_id is not null
     and actual.dispositivo_id <> p_dispositivo_id
     and not p_forzar then
    return jsonb_build_object(
      'estado', 'OCUPADA',
      'dispositivo_nombre', actual.dispositivo_nombre,
      'iniciada_en', actual.iniciada_en
    );
  end if;

  insert into public.sesiones_activas (cuenta_id, dispositivo_id, dispositivo_nombre, iniciada_en)
  values (auth.uid(), p_dispositivo_id, left(coalesce(nullif(btrim(p_dispositivo_nombre), ''), 'Otro celular'), 60),
          (extract(epoch from now()) * 1000)::bigint)
  on conflict (cuenta_id) do update
    set dispositivo_id = excluded.dispositivo_id,
        dispositivo_nombre = excluded.dispositivo_nombre,
        iniciada_en = case
          when public.sesiones_activas.dispositivo_id = excluded.dispositivo_id then public.sesiones_activas.iniciada_en
          else excluded.iniciada_en
        end;

  return jsonb_build_object('estado', 'LIBRE');
end;
$$;

-- Al cerrar sesión a propósito se libera la cuenta de forma definitiva.
create or replace function public.liberar_sesion(p_dispositivo_id text default null) returns void
language plpgsql
security definer
set search_path = public
as $$
begin
  if auth.uid() is null then
    return;
  end if;
  if p_dispositivo_id is null or btrim(p_dispositivo_id) = '' then
    delete from public.sesiones_activas where cuenta_id = auth.uid();
  else
    delete from public.sesiones_activas where cuenta_id = auth.uid() and dispositivo_id = p_dispositivo_id;
  end if;
end;
$$;

revoke all on function public.reclamar_sesion(text, text, boolean) from public, anon;
revoke all on function public.liberar_sesion(text) from public, anon;
grant execute on function public.reclamar_sesion(text, text, boolean) to authenticated;
grant execute on function public.liberar_sesion(text) to authenticated;

-- Para que el celular que pierde la sesión se entere al instante (Realtime).
alter publication supabase_realtime add table public.sesiones_activas;
