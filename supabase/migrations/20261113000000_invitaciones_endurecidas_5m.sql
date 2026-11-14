-- Endurecimiento de invitaciones:
-- 1. Vigencia reducida a 5 minutos (300 000 ms).
-- 2. Aforo universal máximo de 5 códigos activos a la vez.
-- 3. Plan Free: máximo 1 código activo y máximo 1 trabajador hospedado.
-- 4. Retención de 1 hora como revocado y eliminación definitiva posterior.

create or replace function public.invitaciones_forzar_vigencia() returns trigger
language plpgsql
security definer
set search_path = public
as $$
declare
  v_ahora bigint := (extract(epoch from now()) * 1000)::bigint;
  v_max constant bigint := 5 * 60 * 1000; -- 5 minutos
  v_dueno uuid;
  v_es_pro boolean;
  v_codigos_activos int;
  v_trabajadores int;
begin
  if tg_op = 'INSERT' then
    -- Aforo máximo universal de 5 códigos a la vez
    select count(*) into v_codigos_activos
      from public.invitaciones_trabajador
     where negocio_id = new.negocio_id
       and estado = 'PENDIENTE'
       and expiracion > v_ahora;

    if v_codigos_activos >= 5 then
      raise exception 'AFORO_MAXIMO: Llegaste al aforo máximo de 5 códigos activos a la vez' using errcode = 'P0001';
    end if;

    -- Verificar límites del plan
    select dueno_cuenta_id into v_dueno from public.negocios where id = new.negocio_id;
    v_es_pro := (v_dueno is not null and public.plan_efectivo(v_dueno) = 'PRO');

    if not v_es_pro then
      if v_codigos_activos >= 1 then
        raise exception 'PLAN_REQUERIDO: El plan Free solo permite 1 código activo a la vez' using errcode = 'P0001';
      end if;

      select count(*) into v_trabajadores
        from public.relaciones_cuenta_negocio
       where negocio_id = new.negocio_id
         and rol = 'TRABAJADOR'
         and estado = 'ACTIVA';

      if v_trabajadores >= 1 then
        raise exception 'PLAN_REQUERIDO: El plan Free solo puede hospedar a 1 trabajador' using errcode = 'P0001';
      end if;
    end if;

    new.fecha_creacion := v_ahora;
    new.expiracion := v_ahora + v_max;
  else
    new.fecha_creacion := old.fecha_creacion;
    new.expiracion := least(new.expiracion, old.fecha_creacion + v_max);
  end if;
  return new;
end;
$$;

create or replace function public.limpiar_codigos_expirados() returns void
language plpgsql
security definer
set search_path = public
as $$
declare
  ahora bigint := (extract(epoch from now()) * 1000)::bigint;
begin
  -- Marcar como REVOCADA los que pasaron su tiempo de validez
  update public.invitaciones_trabajador
     set estado = 'REVOCADA'
   where estado = 'PENDIENTE' and expiracion <= ahora;

  -- Eliminar definitivamente los que llevan más de 1 hora vencidos
  delete from public.invitaciones_trabajador
   where expiracion + 3600000 <= ahora;
end;
$$;

grant execute on function public.limpiar_codigos_expirados() to authenticated, anon;
