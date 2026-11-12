-- Parche de la auditoría rápida (2026-11-12)
-- 1) El dueño no puede pasar su negocio a otra cuenta ni dejarlo sin dueño.
alter policy negocios_update_dueno on public.negocios
  using (dueno_cuenta_id = auth.uid())
  with check (dueno_cuenta_id = auth.uid());
-- 2) Cada cuenta solo puede dejar su propia fila con su propio id.
alter policy cuentas_update_propia on public.cuentas
  using (auth.uid() = id)
  with check (auth.uid() = id);
-- 3) limpiar-qr ahora exige el secreto de Vault (x-push-secret): el cron lo envía.
select cron.unschedule('limpiar-qr');
select cron.schedule('limpiar-qr', '*/10 * * * *', $$select net.http_post(
  url := 'https://ianiulmdyzsqtrqpujkx.supabase.co/functions/v1/limpiar-qr',
  headers := jsonb_build_object('Content-Type','application/json','x-push-secret',(select decrypted_secret from vault.decrypted_secrets where name = 'push_webhook_secret' limit 1)),
  body := '{}'::jsonb)$$);
