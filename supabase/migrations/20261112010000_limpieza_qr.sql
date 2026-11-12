-- Cola de borrado de QR: los archivos de Storage no se borran en cascada con la base.
-- Cada vez que se borra un método, se cambia su QR o se borra un negocio, se anota aquí y la función
-- `limpiar-qr` (cada 10 minutos) los elimina de Storage. Ruta de los archivos: <negocio_id>/<archivo>.
create table if not exists public.qr_por_borrar (
  id bigint generated always as identity primary key,
  negocio_id uuid not null,
  cuenta_id uuid,
  ruta text,                       -- null = toda la carpeta del negocio
  motivo text not null,
  creado_en timestamptz not null default now()
);
alter table public.qr_por_borrar enable row level security;
revoke all on public.qr_por_borrar from anon, authenticated;

create or replace function public.tg_qr_encolar_metodo() returns trigger
language plpgsql security definer set search_path = '' as $$
begin
  if old.qr_url is not null and (tg_op = 'DELETE' or new.qr_url is distinct from old.qr_url) then
    insert into public.qr_por_borrar(negocio_id, cuenta_id, ruta, motivo)
    values (old.negocio_id, (select dueno_cuenta_id from public.negocios where id = old.negocio_id), old.qr_url,
            case when tg_op = 'DELETE' then 'METODO_BORRADO' else 'QR_REEMPLAZADO' end);
  end if;
  return null;
end $$;

create or replace function public.tg_qr_encolar_negocio() returns trigger
language plpgsql security definer set search_path = '' as $$
begin
  insert into public.qr_por_borrar(negocio_id, cuenta_id, ruta, motivo) values (old.id, old.dueno_cuenta_id, null, 'NEGOCIO_BORRADO');
  return null;
end $$;
revoke execute on function public.tg_qr_encolar_metodo(), public.tg_qr_encolar_negocio() from public, anon, authenticated;

drop trigger if exists trg_qr_encolar_metodo on public.metodos_de_cobro;
create trigger trg_qr_encolar_metodo after delete or update of qr_url on public.metodos_de_cobro
  for each row execute function public.tg_qr_encolar_metodo();
drop trigger if exists trg_qr_encolar_negocio on public.negocios;
create trigger trg_qr_encolar_negocio after delete on public.negocios
  for each row execute function public.tg_qr_encolar_negocio();

-- Vista de panel: de qué negocio y de qué cuenta es cada archivo QR (solo lectura, sin acceso para la app).
create or replace view public.v_qr_archivos with (security_invoker = true) as
select o.name as ruta, split_part(o.name, '/', 1) as negocio_id, n.nombre as negocio, n.dueno_cuenta_id as cuenta_id, u.email as cuenta_correo,
       m.tipo as metodo,
       case when n.id is null then 'NEGOCIO_BORRADO' when m.id is null then 'SIN_USO' else 'EN_USO' end as estado, o.created_at
from storage.objects o
left join public.negocios n on n.id::text = split_part(o.name, '/', 1)
left join auth.users u on u.id = n.dueno_cuenta_id
left join public.metodos_de_cobro m on m.qr_url = o.name
where o.bucket_id = 'metodos-qr' and o.name not like '%.emptyFolderPlaceholder';
revoke all on public.v_qr_archivos from anon, authenticated;

select cron.schedule('limpiar-qr', '*/10 * * * *', $$select net.http_post(url := 'https://ianiulmdyzsqtrqpujkx.supabase.co/functions/v1/limpiar-qr', body := '{}'::jsonb)$$);
