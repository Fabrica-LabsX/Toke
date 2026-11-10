// check-app-release: endpoint PÚBLICO y de solo lectura con la versión estable de TOKE.
// Funciona antes de iniciar sesión (verify_jwt = false). No devuelve datos privados.
import { createClient } from "npm:@supabase/supabase-js@2";

const cors = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Methods": "GET, OPTIONS",
  "Access-Control-Allow-Headers": "authorization, apikey, content-type",
};

Deno.serve(async (req) => {
  if (req.method === "OPTIONS") return new Response(null, { status: 204, headers: cors });
  if (req.method !== "GET") return new Response("Method Not Allowed", { status: 405, headers: cors });

  const channel = new URL(req.url).searchParams.get("channel") ?? "stable";
  if (!/^[a-z]{1,20}$/.test(channel)) return new Response("Bad Request", { status: 400, headers: cors });

  // SUPABASE_URL y SUPABASE_SERVICE_ROLE_KEY los inyecta Supabase en el servidor; nunca viajan al cliente.
  const db = createClient(Deno.env.get("SUPABASE_URL")!, Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!);
  const { data, error } = await db
    .from("app_releases")
    .select("application_id,channel,version_code,version_name,min_supported_version_code,apk_url,apk_size_bytes,sha256,release_notes_url,published_at")
    .eq("channel", channel).eq("active", true)
    .order("version_code", { ascending: false }).limit(1).maybeSingle();

  if (error) return new Response(JSON.stringify({ error: "unavailable" }), { status: 503, headers: { ...cors, "Content-Type": "application/json" } });
  if (!data) return new Response(JSON.stringify({ error: "no_release" }), { status: 404, headers: { ...cors, "Content-Type": "application/json" } });

  const body = {
    applicationId: data.application_id,
    channel: data.channel,
    latestVersionCode: data.version_code,
    latestVersionName: data.version_name,
    minSupportedVersionCode: data.min_supported_version_code,
    apkUrl: data.apk_url,
    apkSizeBytes: data.apk_size_bytes,
    sha256: data.sha256,
    releaseNotesUrl: data.release_notes_url,
    publishedAt: data.published_at,
  };
  return new Response(JSON.stringify(body), {
    headers: { ...cors, "Content-Type": "application/json", "Cache-Control": "public, max-age=60" },
  });
});
