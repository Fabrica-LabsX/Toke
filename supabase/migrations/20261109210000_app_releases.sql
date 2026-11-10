-- Fuente pública de versiones de TOKE (la leen la app Android y la web mediante la Edge Function check-app-release).
-- Solo metadatos de publicación: ningún dato de cuentas, negocios ni cobros.
create table if not exists public.app_releases (
  id bigint generated always as identity primary key,
  channel text not null default 'stable',
  application_id text not null,
  version_code integer not null check (version_code > 0),
  version_name text not null,
  min_supported_version_code integer not null check (min_supported_version_code > 0),
  apk_url text not null check (apk_url like 'https://%'),
  apk_size_bytes bigint not null check (apk_size_bytes > 0),
  sha256 text not null check (sha256 ~ '^[0-9a-f]{64}$'),
  release_notes_url text,
  release_notes text,
  published_at timestamptz not null default now(),
  active boolean not null default true,
  unique (channel, version_code),
  check (min_supported_version_code <= version_code)
);

alter table public.app_releases enable row level security;
-- Sin políticas para anon/authenticated: solo la Edge Function (service_role, en el servidor) la lee.
revoke all on public.app_releases from anon, authenticated;
