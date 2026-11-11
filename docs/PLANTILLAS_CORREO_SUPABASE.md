# Plantillas de correo de Supabase: solo código, sin enlace

TOKE no tiene una página a la que lleve un enlace de confirmación (por eso salía `localhost`). La app pide
el **código de 6 dígitos** dentro de la pantalla, así que las plantillas deben mostrar `{{ .Token }}` y
**ningún enlace** (`{{ .ConfirmationURL }}`).

Dónde: Supabase Dashboard → **Authentication → Emails → Templates** (el proyecto `ianiulmdyzsqtrqpujkx`).
Para cada plantilla, pega el asunto y el cuerpo (modo HTML / "Source").

## 1. Confirm signup (crear cuenta)
**Asunto:** `Tu código de TOKE: {{ .Token }}`

```html
<div style="font-family:Arial,Helvetica,sans-serif;max-width:480px;margin:auto;padding:24px;color:#0F172A">
  <h2 style="margin:0 0 12px">Confirma tu correo</h2>
  <p>Escribe este código en TOKE para terminar de crear tu cuenta:</p>
  <p style="font-size:34px;letter-spacing:8px;font-weight:800;margin:20px 0">{{ .Token }}</p>
  <p style="color:#64748B;font-size:14px">El código vence en poco tiempo. Si no lo pediste, ignora este correo.</p>
</div>
```

## 2. Reset Password (olvidé mi contraseña)
**Asunto:** `Código para cambiar tu contraseña de TOKE`

```html
<div style="font-family:Arial,Helvetica,sans-serif;max-width:480px;margin:auto;padding:24px;color:#0F172A">
  <h2 style="margin:0 0 12px">Cambia tu contraseña</h2>
  <p>Escribe este código en TOKE para elegir una contraseña nueva:</p>
  <p style="font-size:34px;letter-spacing:8px;font-weight:800;margin:20px 0">{{ .Token }}</p>
  <p style="color:#64748B;font-size:14px">Si no lo pediste, ignora este correo: tu contraseña no cambia.</p>
</div>
```

## 3. Change Email Address (Mi cuenta → Actualizar correo)
**Asunto:** `Confirma tu correo nuevo en TOKE`

```html
<div style="font-family:Arial,Helvetica,sans-serif;max-width:480px;margin:auto;padding:24px;color:#0F172A">
  <h2 style="margin:0 0 12px">Confirma tu correo nuevo</h2>
  <p>Escribe este código en TOKE para cambiar tu correo a {{ .NewEmail }}:</p>
  <p style="font-size:34px;letter-spacing:8px;font-weight:800;margin:20px 0">{{ .Token }}</p>
  <p style="color:#64748B;font-size:14px">Si no lo pediste, ignora este correo.</p>
</div>
```

## 4. Reauthentication (si algún día se usa)
La plantilla por defecto ya usa `{{ .Token }}`; solo cambia el asunto a `Tu código de TOKE`.

## Ajustes que van con las plantillas
- **Authentication → Sign In / Providers → Email → "Secure email change"**: apágalo. Encendido, Supabase manda un
  código al correo viejo **y otro** al nuevo y pide los dos; la app pide solo uno. Apagado, el código llega solo
  al correo nuevo. (La app ya exige sesión abierta para cambiar el correo.)
- **Authentication → URL Configuration → Site URL**: pon `https://tokelabs.pages.dev` (o tu dominio) para que
  ningún mensaje vuelva a apuntar a `localhost`. No hace falta tocar "Redirect URLs".
- **Authentication → SMTP Settings**: ya está con Brevo y el remitente de soporte. El remitente debe estar
  verificado en Brevo (Senders & IP) o los correos no salen.
- **Longitud del código**: el predeterminado es de 6 dígitos. Mi cuenta acepta de 6 a 8; la pantalla de crear cuenta y la de recuperar contraseña no limitan el largo.

## Cómo comprobarlo
1. En la app, "Crear cuenta" con un correo tuyo: debe llegar un correo con un código de 6 dígitos, sin enlace.
2. "¿Olvidaste tu contraseña?": mismo resultado.
3. "Mi cuenta → Actualizar correo": el código llega al correo nuevo.
