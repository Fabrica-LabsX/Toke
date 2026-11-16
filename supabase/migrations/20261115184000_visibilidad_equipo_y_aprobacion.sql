-- Corrección de visibilidad de nombres de equipo y unión con/sin aprobación previa

-- 1. equipo_de_negocio: leer username desde public.cuentas en lugar de perfiles_publicos
CREATE OR REPLACE FUNCTION public.equipo_de_negocio(p_negocio_id uuid)
 RETURNS TABLE(cuenta_id uuid, username text, rol text, estado text)
 LANGUAGE sql
 STABLE SECURITY DEFINER
 SET search_path TO 'public'
AS $function$
  SELECT r.cuenta_id,
         COALESCE(NULLIF(c.username, ''), 'usuario') AS username,
         r.rol,
         r.estado
  FROM public.relacion_cuenta_negocio r
  LEFT JOIN public.cuentas c ON c.id = r.cuenta_id
  WHERE r.negocio_id = p_negocio_id
    AND r.estado = 'ACTIVA'
    AND (
      EXISTS (
        SELECT 1 FROM public.negocios n
        WHERE n.id = p_negocio_id AND n.dueno_cuenta_id = auth.uid()
      )
      OR EXISTS (
        SELECT 1 FROM public.relacion_cuenta_negocio m
        WHERE m.negocio_id = p_negocio_id
          AND m.cuenta_id = auth.uid()
          AND m.estado = 'ACTIVA'
      )
    );
$function$;

-- 2. obtener_preview_invitacion: leer dueno desde public.cuentas y retornar requiere_aprobacion
CREATE OR REPLACE FUNCTION public.obtener_preview_invitacion(codigo_invitacion text)
 RETURNS json
 LANGUAGE plpgsql
 SECURITY DEFINER
 SET search_path TO 'public'
AS $function$
DECLARE
  v_invitacion record;
BEGIN
  SELECT
    i.codigo,
    i.negocio_id,
    i.estado,
    i.expiracion,
    i.fecha_creacion,
    i.requiere_aprobacion,
    n.nombre AS negocio_nombre,
    COALESCE(NULLIF(c.username, ''), 'El dueño') AS dueno_nombre_visible,
    COALESCE(NULLIF(c.username, ''), 'El dueño') AS dueno_username
  INTO v_invitacion
  FROM public.invitaciones_trabajador i
  JOIN public.negocios n ON n.id = i.negocio_id
  LEFT JOIN public.cuentas c ON c.id = n.dueno_cuenta_id
  WHERE upper(trim(i.codigo)) = upper(trim(codigo_invitacion))
    AND i.estado = 'PENDIENTE'
    AND i.expiracion > (extract(epoch FROM now()) * 1000);

  IF v_invitacion.codigo IS NULL THEN
    RETURN NULL;
  END IF;

  RETURN json_build_object(
    'codigo', v_invitacion.codigo,
    'negocio_id', v_invitacion.negocio_id,
    'estado', v_invitacion.estado,
    'expiracion', v_invitacion.expiracion,
    'fecha_creacion', v_invitacion.fecha_creacion,
    'requiere_aprobacion', v_invitacion.requiere_aprobacion,
    'negocio_nombre', v_invitacion.negocio_nombre,
    'dueno_nombre_visible', v_invitacion.dueno_nombre_visible,
    'dueno_username', v_invitacion.dueno_username
  );
END;
$function$;

-- 3. aceptar_invitacion: permitir unión directa (sin aprobación) o con aprobación, actualizando estados previos
CREATE OR REPLACE FUNCTION public.aceptar_invitacion(codigo_invitacion text)
 RETURNS json
 LANGUAGE plpgsql
 SECURITY DEFINER
 SET search_path TO 'public'
AS $function$
DECLARE
  v_negocio_id uuid;
  v_dueno_cuenta_id uuid;
  v_cuenta_id uuid := auth.uid();
  v_estado_actual text;
  v_requiere_aprobacion boolean;
  v_estado_nuevo text;
  v_codigo_clean text;
BEGIN
  IF v_cuenta_id IS NULL THEN
    RETURN json_build_object('exito', false, 'mensaje', 'Sesión no válida');
  END IF;

  v_codigo_clean := upper(trim(codigo_invitacion));

  SELECT i.negocio_id, n.dueno_cuenta_id, i.requiere_aprobacion
    INTO v_negocio_id, v_dueno_cuenta_id, v_requiere_aprobacion
  FROM public.invitaciones_trabajador i
  JOIN public.negocios n ON n.id = i.negocio_id
  WHERE upper(trim(i.codigo)) = v_codigo_clean
    AND i.estado = 'PENDIENTE'
    AND i.expiracion > (extract(epoch FROM now()) * 1000)
  FOR UPDATE OF i;

  IF v_negocio_id IS NULL THEN
    RETURN json_build_object('exito', false, 'mensaje', 'Invitación inválida o ya utilizada');
  END IF;

  IF v_dueno_cuenta_id = v_cuenta_id THEN
    RETURN json_build_object('exito', false, 'mensaje', 'No puedes unirte a tu propio negocio.');
  END IF;

  SELECT r.estado INTO v_estado_actual
  FROM public.relacion_cuenta_negocio r
  WHERE r.cuenta_id = v_cuenta_id
    AND r.negocio_id = v_negocio_id
    AND r.rol = 'TRABAJADOR';

  IF v_estado_actual = 'ACTIVA' THEN
    RETURN json_build_object('exito', true, 'mensaje', 'Ya formas parte de este negocio.');
  END IF;

  UPDATE public.invitaciones_trabajador
  SET estado = 'ACEPTADA'
  WHERE upper(trim(codigo)) = v_codigo_clean
    AND estado = 'PENDIENTE'
    AND expiracion > (extract(epoch FROM now()) * 1000)
  RETURNING negocio_id INTO v_negocio_id;

  IF v_negocio_id IS NULL THEN
    RETURN json_build_object('exito', false, 'mensaje', 'Invitación inválida o ya utilizada');
  END IF;

  v_estado_nuevo := CASE
    WHEN COALESCE(v_requiere_aprobacion, true) THEN 'SOLICITADA'
    ELSE 'ACTIVA'
  END;

  INSERT INTO public.relacion_cuenta_negocio
    (cuenta_id, negocio_id, rol, estado, fecha_vinculacion, codigo_usado)
  VALUES
    (v_cuenta_id, v_negocio_id, 'TRABAJADOR', v_estado_nuevo,
     (extract(epoch FROM now()) * 1000)::bigint, v_codigo_clean)
  ON CONFLICT (cuenta_id, negocio_id, rol)
  DO UPDATE SET
    estado = EXCLUDED.estado,
    codigo_usado = EXCLUDED.codigo_usado,
    fecha_vinculacion = (extract(epoch FROM now()) * 1000)::bigint;

  IF v_estado_nuevo = 'SOLICITADA' THEN
    RETURN json_build_object('exito', true, 'mensaje', 'Solicitud enviada. Espera a que el dueño te acepte.');
  ELSE
    RETURN json_build_object('exito', true, 'mensaje', '¡Ya puedes entrar al negocio!');
  END IF;
END;
$function$;

-- 4. tg_limite_colaboradores: no contar a la propia cuenta para permitir reingreso/reactivación
CREATE OR REPLACE FUNCTION public.tg_limite_colaboradores()
 RETURNS trigger
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $function$
declare dueno uuid; tope integer; cantidad integer;
begin
  if new.rol <> 'TRABAJADOR' then return new; end if;
  if new.estado not in ('ACTIVA', 'PAUSADA', 'SOLICITADA') then return new; end if;

  select dueno_cuenta_id into dueno from public.negocios where id = new.negocio_id;
  if dueno is null or public.plan_efectivo(dueno) = 'PRO' then return new; end if;
  select max_colaboradores_free into tope from public.config_planes where id = 1;

  select count(*) into cantidad from public.relacion_cuenta_negocio
   where negocio_id = new.negocio_id 
     and rol = 'TRABAJADOR' 
     and estado in ('ACTIVA', 'PAUSADA', 'SOLICITADA')
     and cuenta_id <> new.cuenta_id;

  if cantidad >= tope then
    raise exception 'LIMITE_COLABORADORES: el plan Free permite % colaboradores', tope using errcode = 'P0001';
  end if;
  return new;
end;
$function$;

-- 5. invitaciones_forzar_vigencia: corregir nombre de tabla a relacion_cuenta_negocio
CREATE OR REPLACE FUNCTION public.invitaciones_forzar_vigencia()
 RETURNS trigger
 LANGUAGE plpgsql
 SECURITY DEFINER
 SET search_path TO 'public'
AS $function$
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
        from public.relacion_cuenta_negocio
       where negocio_id = new.negocio_id
         and rol = 'TRABAJADOR'
         and estado in ('ACTIVA', 'PAUSADA', 'SOLICITADA');

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
$function$;

-- 6. RLS en public.cuentas: visibilidad mutua entre dueño y miembros del mismo negocio
DROP POLICY IF EXISTS cuentas_select_miembro_negocio ON public.cuentas;

CREATE POLICY cuentas_select_miembro_negocio ON public.cuentas
FOR SELECT
USING (
  -- Dueño ve las cuentas de los miembros de su negocio
  id IN (
    SELECT r.cuenta_id FROM public.relacion_cuenta_negocio r
    WHERE r.negocio_id IN (
      SELECT n.id FROM public.negocios n WHERE n.dueno_cuenta_id = auth.uid()
    )
  )
  OR
  -- Miembro ve la cuenta del dueño de su negocio
  id IN (
    SELECT n.dueno_cuenta_id FROM public.negocios n
    WHERE n.id IN (
      SELECT r.negocio_id FROM public.relacion_cuenta_negocio r WHERE r.cuenta_id = auth.uid()
    )
  )
  OR
  -- Miembros ven las cuentas de sus compañeros en el mismo negocio
  id IN (
    SELECT r2.cuenta_id FROM public.relacion_cuenta_negocio r1
    JOIN public.relacion_cuenta_negocio r2 ON r1.negocio_id = r2.negocio_id
    WHERE r1.cuenta_id = auth.uid()
  )
);
