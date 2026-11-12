// enviar-push: avisa por Google (FCM) a los teléfonos de Toke cuando pasa algo importante.
// La llaman los triggers de la base de datos con un secreto compartido (cabecera x-push-secret).
// No lleva montos en el mensaje: el teléfono lee los datos con su propia sesión.
// Copia del código desplegado en Supabase (versión 2). verify_jwt = false: la protege el secreto compartido.
import { createClient } from "jsr:@supabase/supabase-js@2";

const SUPABASE_URL = Deno.env.get("SUPABASE_URL") ?? "";
const SERVICE_KEY = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY") ?? "";
const admin = createClient(SUPABASE_URL, SERVICE_KEY, { auth: { persistSession: false } });

type Payload = {
  tipo: string;
  operacion_id?: string;
  negocio_id?: string;
  cuenta_id?: string;
  estado?: string;
};
type CuentaServicio = { project_id: string; client_email: string; private_key: string };

const json = (cuerpo: unknown, status = 200) =>
  new Response(JSON.stringify(cuerpo), { status, headers: { "Content-Type": "application/json" } });

// Comparación de texto en tiempo constante (evita adivinar el secreto por tiempos de respuesta)
function iguales(a: string, b: string): boolean {
  if (a.length !== b.length) return false;
  let r = 0;
  for (let i = 0; i < a.length; i++) r |= a.charCodeAt(i) ^ b.charCodeAt(i);
  return r === 0;
}

// ---------- Google OAuth (cuenta de servicio -> token de acceso) ----------
let tokenCache: { valor: string; expira: number } | null = null;

function cargarCuentaServicio(): CuentaServicio {
  const crudo = Deno.env.get("FCM_SERVICE_ACCOUNT");
  if (!crudo) throw new Error("Falta el secreto FCM_SERVICE_ACCOUNT en Supabase");
  let sa: Partial<CuentaServicio>;
  try {
    sa = JSON.parse(crudo);
  } catch {
    throw new Error("FCM_SERVICE_ACCOUNT no es un JSON válido (pega el archivo completo de la clave privada)");
  }
  if (!sa.project_id || !sa.client_email || !sa.private_key) {
    throw new Error(
      "FCM_SERVICE_ACCOUNT no parece la clave de cuenta de servicio (faltan project_id, client_email o private_key). " +
        "No sirve google-services.json: hay que generar la clave privada en Firebase > Cuentas de servicio",
    );
  }
  return sa as CuentaServicio;
}

function b64url(datos: Uint8Array | string): string {
  const bytes = typeof datos === "string" ? new TextEncoder().encode(datos) : datos;
  let bin = "";
  for (const b of bytes) bin += String.fromCharCode(b);
  return btoa(bin).replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/, "");
}

function pemADer(pem: string): ArrayBuffer {
  const limpio = pem
    .replace(/-----BEGIN PRIVATE KEY-----/, "")
    .replace(/-----END PRIVATE KEY-----/, "")
    .replace(/\s+/g, "");
  const bin = atob(limpio);
  const bytes = new Uint8Array(bin.length);
  for (let i = 0; i < bin.length; i++) bytes[i] = bin.charCodeAt(i);
  return bytes.buffer;
}

async function obtenerAccessToken(sa: CuentaServicio): Promise<string> {
  const ahora = Math.floor(Date.now() / 1000);
  if (tokenCache && tokenCache.expira - 60 > ahora) return tokenCache.valor;

  const cabecera = { alg: "RS256", typ: "JWT" };
  const reclamo = {
    iss: sa.client_email,
    scope: "https://www.googleapis.com/auth/firebase.messaging",
    aud: "https://oauth2.googleapis.com/token",
    iat: ahora,
    exp: ahora + 3600,
  };
  const sinFirmar = `${b64url(JSON.stringify(cabecera))}.${b64url(JSON.stringify(reclamo))}`;
  const llave = await crypto.subtle.importKey(
    "pkcs8",
    pemADer(sa.private_key),
    { name: "RSASSA-PKCS1-v1_5", hash: "SHA-256" },
    false,
    ["sign"],
  );
  const firma = await crypto.subtle.sign("RSASSA-PKCS1-v1_5", llave, new TextEncoder().encode(sinFirmar));
  const assertion = `${sinFirmar}.${b64url(new Uint8Array(firma))}`;

  const res = await fetch("https://oauth2.googleapis.com/token", {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: new URLSearchParams({ grant_type: "urn:ietf:params:oauth:grant-type:jwt-bearer", assertion }),
  });
  const cuerpo = await res.json().catch(() => ({}));
  if (!res.ok || !cuerpo.access_token) {
    throw new Error(`Google rechazó la clave de la cuenta de servicio (HTTP ${res.status}: ${cuerpo.error ?? "sin detalle"})`);
  }
  tokenCache = { valor: cuerpo.access_token as string, expira: ahora + Number(cuerpo.expires_in ?? 3600) };
  return tokenCache.valor;
}

// ---------- Destinatarios ----------
async function trabajadoresActivos(negocioId: string): Promise<string[]> {
  const { data, error } = await admin
    .from("relacion_cuenta_negocio")
    .select("cuenta_id")
    .eq("negocio_id", negocioId)
    .eq("rol", "TRABAJADOR")
    .eq("estado", "ACTIVA");
  if (error) throw new Error(`No se pudo leer el equipo: ${error.message}`);
  return (data ?? []).map((r: { cuenta_id: string }) => r.cuenta_id);
}

async function esTrabajadorActivo(negocioId: string, cuentaId: string): Promise<boolean> {
  const { data, error } = await admin
    .from("relacion_cuenta_negocio")
    .select("cuenta_id")
    .eq("negocio_id", negocioId)
    .eq("cuenta_id", cuentaId)
    .eq("rol", "TRABAJADOR")
    .eq("estado", "ACTIVA")
    .maybeSingle();
  if (error) throw new Error(`No se pudo leer la relación: ${error.message}`);
  return data != null;
}

type Plan = { destinatarios: string[]; data: Record<string, string> } | { motivo: string };

async function planear(p: Payload): Promise<Plan> {
  switch (p.tipo) {
    case "cobro":
    case "asignado": {
      if (!p.operacion_id) return { motivo: "falta operacion_id" };
      const { data: op, error } = await admin
        .from("operaciones")
        .select("id, negocio_id, estado, trabajador_asignado_id")
        .eq("id", p.operacion_id)
        .maybeSingle();
      if (error) throw new Error(`No se pudo leer la operación: ${error.message}`);
      if (!op) return { motivo: "la operación no existe" };
      if (op.estado !== "SIN_ASIGNAR" && op.estado !== "ASIGNADA") return { motivo: `estado ${op.estado}: no se avisa` };
      let destinatarios: string[];
      if (op.trabajador_asignado_id) {
        destinatarios = (await esTrabajadorActivo(op.negocio_id, op.trabajador_asignado_id))
          ? [op.trabajador_asignado_id]
          : [];
      } else {
        destinatarios = await trabajadoresActivos(op.negocio_id);
      }
      return {
        destinatarios,
        data: {
          tipo: p.tipo,
          operacion_id: op.id,
          negocio_id: op.negocio_id,
          asignada: op.trabajador_asignado_id ? "1" : "0",
        },
      };
    }
    case "solicitud": {
      if (!p.negocio_id || !p.cuenta_id) return { motivo: "faltan datos" };
      const { data: neg, error } = await admin
        .from("negocios")
        .select("dueno_cuenta_id")
        .eq("id", p.negocio_id)
        .maybeSingle();
      if (error) throw new Error(`No se pudo leer el negocio: ${error.message}`);
      if (!neg) return { motivo: "el negocio no existe" };
      return {
        destinatarios: [neg.dueno_cuenta_id],
        data: { tipo: "solicitud", negocio_id: p.negocio_id, cuenta_id: p.cuenta_id },
      };
    }
    case "respuesta_solicitud": {
      if (!p.negocio_id || !p.cuenta_id || !p.estado) return { motivo: "faltan datos" };
      return {
        destinatarios: [p.cuenta_id],
        data: { tipo: "respuesta_solicitud", negocio_id: p.negocio_id, estado: p.estado },
      };
    }
    default:
      return { motivo: `tipo desconocido: ${p.tipo}` };
  }
}

// ---------- Envío ----------
async function enviarUno(
  sa: CuentaServicio,
  acceso: string,
  token: string,
  data: Record<string, string>,
): Promise<{ ok: boolean; borrar: boolean }> {
  const res = await fetch(`https://fcm.googleapis.com/v1/projects/${sa.project_id}/messages:send`, {
    method: "POST",
    headers: { Authorization: `Bearer ${acceso}`, "Content-Type": "application/json" },
    // Solo datos (sin texto ni monto): la app arma la notificación con sus propios datos
    body: JSON.stringify({ message: { token, data, android: { priority: "HIGH", ttl: "600s" } } }),
  });
  if (res.ok) return { ok: true, borrar: false };
  const cuerpo = await res.json().catch(() => null);
  const detalles: Array<{ errorCode?: string }> = cuerpo?.error?.details ?? [];
  const codigo = detalles.find((d) => d.errorCode)?.errorCode ?? cuerpo?.error?.status ?? "";
  const borrar = res.status === 404 || codigo === "UNREGISTERED" || codigo === "NOT_FOUND";
  console.error(`FCM respondió HTTP ${res.status} (${codigo})`);
  return { ok: false, borrar };
}

Deno.serve(async (req: Request) => {
  if (req.method !== "POST") return json({ error: "Método no permitido" }, 405);

  // 1) Autorización por secreto compartido (guardado en Vault)
  const recibido = req.headers.get("x-push-secret") ?? "";
  let esperado = "";
  try {
    const { data } = await admin.rpc("secreto_push");
    esperado = typeof data === "string" ? data : "";
  } catch (_) {
    esperado = "";
  }
  if (!esperado || !iguales(recibido, esperado)) return json({ error: "No autorizado" }, 401);

  let p: Payload;
  try {
    p = await req.json();
  } catch {
    return json({ error: "JSON inválido" }, 400);
  }

  try {
    const sa = cargarCuentaServicio();

    // Prueba de conexión con Google, sin enviar nada a nadie
    if (p.tipo === "diagnostico") {
      await obtenerAccessToken(sa);
      return json({ ok: true, proyecto: sa.project_id, token_de_google: true });
    }

    // Prueba de la API de mensajes: envía en modo "solo validar" a un token falso.
    // 400 INVALID_ARGUMENT = la API está habilitada y la clave tiene permiso (el token falso es lo que se rechaza).
    // 403 / SERVICE_DISABLED / PERMISSION_DENIED = la API de Firebase Cloud Messaging está apagada o sin permiso.
    if (p.tipo === "diagnostico_envio") {
      const acceso = await obtenerAccessToken(sa);
      const res = await fetch(`https://fcm.googleapis.com/v1/projects/${sa.project_id}/messages:send`, {
        method: "POST",
        headers: { Authorization: `Bearer ${acceso}`, "Content-Type": "application/json" },
        body: JSON.stringify({ validate_only: true, message: { token: "token-falso-de-diagnostico", data: { tipo: "prueba" } } }),
      });
      const cuerpo = await res.json().catch(() => null);
      const detalles: Array<{ errorCode?: string }> = cuerpo?.error?.details ?? [];
      const codigo = detalles.find((d) => d.errorCode)?.errorCode ?? cuerpo?.error?.status ?? null;
      const mensaje = typeof cuerpo?.error?.message === "string" ? cuerpo.error.message.slice(0, 160) : null;
      return json({ ok: true, proyecto: sa.project_id, http: res.status, codigo, mensaje });
    }

    // 2) A quién se avisa
    const plan = await planear(p);
    if ("motivo" in plan) return json({ ok: true, enviados: 0, motivo: plan.motivo });
    if (plan.destinatarios.length === 0) {
      return json({ ok: true, tipo: p.tipo, destinatarios: 0, enviados: 0, motivo: "sin destinatarios" });
    }

    // 3) Dispositivos de esos destinatarios
    const { data: disp, error } = await admin
      .from("dispositivos")
      .select("token")
      .in("cuenta_id", plan.destinatarios);
    if (error) throw new Error(`No se pudo leer los dispositivos: ${error.message}`);
    const tokens = (disp ?? []).map((d: { token: string }) => d.token);
    if (tokens.length === 0) {
      return json({ ok: true, tipo: p.tipo, destinatarios: plan.destinatarios.length, enviados: 0, motivo: "sin dispositivos registrados" });
    }

    // 4) Envío
    const acceso = await obtenerAccessToken(sa);
    const resultados = await Promise.allSettled(tokens.map((t) => enviarUno(sa, acceso, t, plan.data)));
    let enviados = 0;
    const aBorrar: string[] = [];
    resultados.forEach((r, i) => {
      if (r.status === "fulfilled") {
        if (r.value.ok) enviados++;
        else if (r.value.borrar) aBorrar.push(tokens[i]);
      }
    });
    if (aBorrar.length > 0) await admin.from("dispositivos").delete().in("token", aBorrar);

    return json({
      ok: true,
      tipo: p.tipo,
      destinatarios: plan.destinatarios.length,
      dispositivos: tokens.length,
      enviados,
      fallidos: tokens.length - enviados,
      limpiados: aBorrar.length,
    });
  } catch (e) {
    console.error("enviar-push:", (e as Error).message);
    return json({ ok: false, error: (e as Error).message }, 500);
  }
});
