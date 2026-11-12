-- Aplicada en Supabase como "endurecer_funciones_trigger".
revoke execute on function public.tg_cuota_operaciones() from public, anon, authenticated;
revoke execute on function public.tg_limite_colaboradores() from public, anon, authenticated;
revoke execute on function public.tg_limite_metodos() from public, anon, authenticated;
revoke execute on function public.tg_limite_negocios() from public, anon, authenticated;
revoke execute on function public.validar_billetera_unica_por_dueno() from public, anon, authenticated;
alter function public.inicio_dia_lima_ms() set search_path = '';
-- Bucket metodos-qr: límite de 5 MB (update storage.buckets set file_size_limit = 5242880 where id = 'metodos-qr';)
