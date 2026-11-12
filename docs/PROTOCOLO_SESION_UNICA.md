# Sesión única: una cuenta, un solo TOKE a la vez

Estado: **aplicada y en uso** (tabla `sesiones_activas`, RPC `reclamar_sesion`/`liberar_sesion`).

## Regla
Una cuenta solo puede estar abierta en un celular. Si se abre en otro, se le pregunta a la persona qué hacer.

## Qué ve la persona (estilo WhatsApp)
Al iniciar sesión en un celular nuevo y la cuenta ya está abierta en otro:

> **TOKE solo funciona en un celular a la vez**
> Tu cuenta está abierta en **Samsung SM-G973U1** desde ayer, 8:15 p. m.
>
> [ **Usar aquí** ]  
> [ Cerrar sesión ]

- **Usar aquí:** este celular pasa a ser el activo. El otro se cierra solo y le avisa: "Tu cuenta se abrió en otro celular".
- **Cerrar sesión:** no toca el otro celular; este queda sin sesión.

## Cómo funciona por dentro
1. Cada instalación guarda un `dispositivo_id` propio (un UUID guardado una sola vez) y un nombre legible (marca y modelo).
2. Tabla `sesiones_activas`: una fila por cuenta con el dispositivo que la usa y desde cuándo.
3. Al iniciar sesión (y al abrir la app) se llama `reclamar_sesion(dispositivo_id, nombre, forzar)`:
   - sin fila, o es este mismo celular → queda registrado y sigue (`LIBRE`);
   - hay otro celular y `forzar = false` → responde `OCUPADA` con su nombre y hora; la app muestra el aviso;
   - `forzar = true` ("Usar aquí") → reemplaza la fila.
4. Cada celular escucha en Realtime su fila de `sesiones_activas`. Si cambia a otro `dispositivo_id`, cierra su sesión local y muestra el aviso. Al cerrar, también quita su token de notificaciones.
5. Al cerrar sesión a propósito se llama `liberar_sesion` para que el siguiente celular entre sin preguntar.

## Decisiones para no dejar a nadie fuera
- **Sin latido de reloj:** "Usar aquí" siempre está disponible, así que un celular perdido o apagado no bloquea la cuenta. No se gasta batería en un latido.
- **Sin internet no se bloquea:** si no se puede consultar, la app sigue como hasta ahora; los pagos detectados no se interrumpen. Se vuelve a comprobar al reconectar.
- **Seguridad:** las funciones usan la sesión del usuario (`auth.uid()`); nadie puede mover la sesión de otra cuenta.
- Las sesiones anónimas de trabajadores por código entran en la misma regla (son cuentas).

## Piezas
- `supabase/migrations/20261107150000_sesion_unica.sql` (sin aplicar).
- `features/acceso/SesionUnicaLogica.kt` + prueba: decide qué hacer con la respuesta del servidor.
- Pendiente tras la aprobación: guardar el `dispositivo_id`, llamar a las funciones al iniciar sesión/abrir, escuchar Realtime y dibujar el aviso con `DialogoToke`.
