// limpiar-qr: borra de Storage los QR que ya no tienen dueño. verify_jwt = false; la protege el mismo secreto de Vault que enviar-push (cabecera x-push-secret).
// 1) Procesa la cola `qr_por_borrar` (método borrado, QR reemplazado, negocio borrado).
// 2) Barrido: carpetas de negocios que ya no existen y archivos sin método (con más de 1 hora, para no chocar con una subida en curso).
// Lo llama pg_cron cada 10 minutos. Ruta de los archivos: <negocio_id>/<archivo>.
import { createClient } from "npm:@supabase/supabase-js@2";

const B = "metodos-qr";
function iguales(a: string, b: string): boolean {
  if (a.length !== b.length) return false;
  let r = 0;
  for (let i = 0; i < a.length; i++) r |= a.charCodeAt(i) ^ b.charCodeAt(i);
  return r === 0;
}

Deno.serve(async (req: Request) => {
  const admin = createClient(Deno.env.get("SUPABASE_URL")!, Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!);
  const { data: esperado } = await admin.rpc("secreto_push");
  const recibido = req.headers.get("x-push-secret") ?? "";
  if (typeof esperado !== "string" || !esperado || !iguales(recibido, esperado)) {
    return new Response(JSON.stringify({ error: "No autorizado" }), { status: 401, headers: { "Content-Type": "application/json" } });
  }
  const st = admin.storage.from(B);
  let borrados = 0;

  async function vaciarCarpeta(negocioId: string) {
    const { data } = await st.list(negocioId, { limit: 1000 });
    const rutas = (data ?? []).map((f) => `${negocioId}/${f.name}`);
    if (rutas.length) { await st.remove(rutas); borrados += rutas.length; }
  }

  const { data: cola } = await admin.from("qr_por_borrar").select("id,negocio_id,ruta").order("id").limit(500);
  for (const c of cola ?? []) {
    if (c.ruta) { await st.remove([c.ruta]); borrados++; } else await vaciarCarpeta(c.negocio_id);
    await admin.from("qr_por_borrar").delete().eq("id", c.id);
  }

  const { data: carpetas } = await st.list("", { limit: 1000 });
  for (const car of carpetas ?? []) {
    if (car.id) continue; // archivo suelto en la raíz: no es una carpeta
    const { data: neg } = await admin.from("negocios").select("id").eq("id", car.name).maybeSingle();
    if (!neg) { await vaciarCarpeta(car.name); continue; }
    const { data: usados } = await admin.from("metodos_de_cobro").select("qr_url").eq("negocio_id", car.name).not("qr_url", "is", null);
    const enUso = new Set((usados ?? []).map((m) => m.qr_url));
    const { data: archivos } = await st.list(car.name, { limit: 1000 });
    const viejos = (archivos ?? []).filter((f) => !f.name.startsWith(".") && !enUso.has(`${car.name}/${f.name}`) &&
      Date.now() - new Date(f.created_at ?? Date.now()).getTime() > 3600_000).map((f) => `${car.name}/${f.name}`);
    if (viejos.length) { await st.remove(viejos); borrados += viejos.length; }
  }
  return new Response(JSON.stringify({ ok: true, borrados }), { headers: { "Content-Type": "application/json" } });
});
