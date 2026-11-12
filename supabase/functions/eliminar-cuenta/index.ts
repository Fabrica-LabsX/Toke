// eliminar-cuenta: elimina la cuenta del usuario AUTENTICADO. verify_jwt = true.
// Usa service_role solo aquí, en el servidor. Idempotente: si la cuenta ya no existe responde ok.
//
// Reglas (según las claves foráneas reales de la base):
//  - Trabajador: se borran sus vínculos y dispositivos; los cobros que atendió se conservan (trabajador_asignado_id -> NULL).
//  - Dueño con negocios: borrar la cuenta cierra sus negocios y, por ON DELETE CASCADE, también sus cobros.
//    Por eso se rechaza (HAS_BUSINESSES) salvo que el cliente confirme `cerrarNegocios: true`.
import { createClient } from "npm:@supabase/supabase-js@2";

const cors = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, apikey, content-type",
};
const json = (b: unknown, status = 200) =>
  new Response(JSON.stringify(b), { status, headers: { ...cors, "Content-Type": "application/json" } });

Deno.serve(async (req) => {
  if (req.method === "OPTIONS") return new Response(null, { status: 204, headers: cors });
  if (req.method !== "POST") return json({ error: "method" }, 405);

  const url = Deno.env.get("SUPABASE_URL")!;
  const jwt = (req.headers.get("Authorization") ?? "").replace("Bearer ", "");
  const admin = createClient(url, Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!);

  const { data: u, error: uerr } = await admin.auth.getUser(jwt);
  if (uerr || !u?.user) return json({ ok: true, yaEliminada: true }); // token de una cuenta ya inexistente
  const user = u.user;

  const body = await req.json().catch(() => ({}));
  const cerrarNegocios = body?.cerrarNegocios === true;

  // Verificación de identidad: cuentas con correo exigen la contraseña actual; las anónimas, la frase escrita.
  if (user.email && !user.is_anonymous) {
    const anon = createClient(url, Deno.env.get("SUPABASE_ANON_KEY")!);
    const { error } = await anon.auth.signInWithPassword({ email: user.email, password: String(body?.password ?? "") });
    if (error) return json({ error: "REAUTH_FAILED" }, 403);
  } else if (body?.confirmacion !== "ELIMINAR") {
    return json({ error: "REAUTH_FAILED" }, 403);
  }

  const { data: negocios } = await admin.from("negocios").select("id").eq("dueno_cuenta_id", user.id);
  if ((negocios?.length ?? 0) > 0 && !cerrarNegocios) {
    return json({ error: "HAS_BUSINESSES", negocios: negocios!.length }, 409);
  }

  // Los QR (archivos en <negocio_id>/) no se borran en cascada: se quitan primero. Si algo falla, `limpiar-qr` lo barre después.
  for (const n of negocios ?? []) {
    const { data: archivos } = await admin.storage.from("metodos-qr").list(n.id, { limit: 1000 });
    if (archivos?.length) await admin.storage.from("metodos-qr").remove(archivos.map((a) => `${n.id}/${a.name}`));
  }

  // Borrar el usuario de Auth elimina en cascada cuenta, vínculos, dispositivos, plan y sesiones.
  const { error: derr } = await admin.auth.admin.deleteUser(user.id);
  if (derr && !/not.?found/i.test(derr.message)) return json({ error: "DELETE_FAILED" }, 500);
  return json({ ok: true });
});
