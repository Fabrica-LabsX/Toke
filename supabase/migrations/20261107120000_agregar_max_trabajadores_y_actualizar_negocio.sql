-- Aditiva: tope opcional de trabajadores por negocio y función para que el dueño edite su negocio
-- (nombre, rubro, color y tope). No cambia datos existentes: max_trabajadores nulo = "sin límite".

alter table public.negocios
  add column if not exists max_trabajadores integer
  check (max_trabajadores is null or (max_trabajadores between 1 and 500));

create or replace function public.actualizar_negocio(
  p_negocio_id uuid,
  p_nombre text default null,
  p_max_trabajadores integer default null,
  p_limpiar_max boolean default false,
  p_tipo text default null,
  p_color_marca text default null
) returns void
language plpgsql
security definer
set search_path = public
as $$
begin
  if auth.uid() is null then
    raise exception 'No autenticado';
  end if;

  -- Solo el dueño del negocio puede editarlo.
  if not exists (
    select 1 from public.negocios n
    where n.id = p_negocio_id and n.dueno_cuenta_id = auth.uid()
  ) then
    raise exception 'No autorizado';
  end if;

  if p_nombre is not null and char_length(btrim(p_nombre)) < 2 then
    raise exception 'El nombre es muy corto';
  end if;

  if p_tipo is not null and p_tipo not in
     ('RESTAURANTE','MARISQUERIA','ABARROTES','ROPA','ELECTRODOMESTICOS','SERVICIOS','OTRO') then
    raise exception 'Rubro no válido';
  end if;

  if p_color_marca is not null and p_color_marca !~ '^#[0-9A-Fa-f]{6}$' then
    raise exception 'Color no válido';
  end if;

  update public.negocios
     set nombre = coalesce(nullif(btrim(p_nombre), ''), nombre),
         tipo = coalesce(p_tipo, tipo),
         color_marca = coalesce(p_color_marca, color_marca),
         max_trabajadores = case
           when p_limpiar_max then null
           else coalesce(p_max_trabajadores, max_trabajadores)
         end
   where id = p_negocio_id;
end;
$$;

revoke all on function public.actualizar_negocio(uuid, text, integer, boolean, text, text) from public, anon;
grant execute on function public.actualizar_negocio(uuid, text, integer, boolean, text, text) to authenticated;
