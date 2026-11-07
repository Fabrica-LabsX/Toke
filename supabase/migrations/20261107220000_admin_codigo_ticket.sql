-- Busca y activa cuentas por el código "ticket" (TOKE-XXXXXXXX) que envía la app por WhatsApp,
-- para no mandar el correo en el mensaje. Solo se usan desde el panel de Supabase (SQL Editor).

create or replace function public.admin_buscar_codigo(p_codigo text)
returns table(correo text, cuenta_id uuid, plan_guardado text, pro_hasta timestamptz, plan_efectivo text)
language plpgsql stable security definer set search_path = public as $$
declare
  limpio text := lower(regexp_replace(regexp_replace(btrim(p_codigo), '^toke-', '', 'i'), '[^0-9a-fA-F]', '', 'g'));
  cuantas int;
begin
  if length(limpio) < 8 then raise exception 'Código no válido'; end if;
  select count(*) into cuantas from auth.users u where replace(u.id::text, '-', '') like limpio || '%';
  if cuantas = 0 then raise exception 'No existe una cuenta con ese código'; end if;
  if cuantas > 1 then raise exception 'Más de una cuenta coincide con ese código'; end if;
  return query
    select u.email::text, u.id, coalesce(p.plan, 'FREE'), p.pro_hasta, public.plan_efectivo(u.id)
      from auth.users u left join public.planes_cuenta p on p.cuenta_id = u.id
     where replace(u.id::text, '-', '') like limpio || '%';
end;
$$;

create or replace function public.admin_activar_pro_codigo(p_codigo text, p_dias integer default null)
returns text
language plpgsql security definer set search_path = public as $$
declare correo_cuenta text;
begin
  select b.correo into correo_cuenta from public.admin_buscar_codigo(p_codigo) b;
  return public.admin_activar_pro(correo_cuenta, p_dias);
end;
$$;

revoke all on function public.admin_buscar_codigo(text) from public, anon, authenticated;
revoke all on function public.admin_activar_pro_codigo(text, integer) from public, anon, authenticated;
