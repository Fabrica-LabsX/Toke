-- Plan Free: hasta 4 métodos de cobro por negocio (Yape, Plin y dos bancos cubren a casi todos). Pro: sin tope.
alter table public.config_planes
  add column if not exists max_metodos_free integer not null default 4 check (max_metodos_free >= 1);

create or replace function public.tg_limite_metodos() returns trigger
language plpgsql security definer set search_path = public as $$
declare dueno uuid; tope integer; cantidad integer;
begin
  select dueno_cuenta_id into dueno from public.negocios where id = new.negocio_id;
  if dueno is null or public.plan_efectivo(dueno) = 'PRO' then return new; end if;
  select max_metodos_free into tope from public.config_planes where id = 1;
  select count(*) into cantidad from public.metodos_de_cobro where negocio_id = new.negocio_id;
  if cantidad >= tope then
    raise exception 'LIMITE_METODOS: el plan Free permite % métodos de cobro', tope using errcode = 'P0001';
  end if;
  return new;
end;
$$;
drop trigger if exists trg_limite_metodos on public.metodos_de_cobro;
create trigger trg_limite_metodos before insert on public.metodos_de_cobro
  for each row execute function public.tg_limite_metodos();
