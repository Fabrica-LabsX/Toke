-- Parche 2 de la auditoría (2026-11-12): las ayudantes de RLS dejan de ser ejecutables por el rol anon.
revoke execute on function public.es_dueno_de_negocio(uuid) from public, anon;
revoke execute on function public.es_miembro_de_negocio(uuid) from public, anon;
grant execute on function public.es_dueno_de_negocio(uuid) to authenticated, service_role;
grant execute on function public.es_miembro_de_negocio(uuid) to authenticated, service_role;
alter function public.inicio_dia_lima_ms() set search_path = public, pg_temp;
