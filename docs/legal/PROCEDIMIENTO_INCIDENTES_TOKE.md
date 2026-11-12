# Procedimiento interno de respuesta a incidentes (v0.1, interno, no se publica)

Fecha: 2026-11-12. Redactado por el equipo; sin revisión de abogado.

**Responsable:** el titular del proyecto (persona natural en regularización; nombre por confirmar). Suplente: otro integrante de FLABS que él designe (por definir).

**Qué es un incidente:** acceso no autorizado a cuentas o datos, pérdida o filtración de datos, clave o token expuestos, función de Supabase o Cloudflare comprometida, vulnerabilidad reportada que permita ver datos de otros.

**Canal de reportes:** el correo y el WhatsApp de soporte; asunto "Vulnerabilidad de seguridad" para fallas.

## Pasos
1. **Recibir y registrar** (hora, quién reporta, qué pasó). Responder que se recibió.
2. **Contener** (primeras horas): rotar claves o tokens expuestos (Supabase, FCM), desactivar la función o política afectada, cerrar sesiones si hace falta, mantener evidencia.
3. **Evaluar:** qué datos y cuántas personas; si hay datos sensibles; si hay daño probable.
4. **Notificar cuando corresponda:** a la ANPD y a las personas afectadas. **El plazo exacto no está confirmado en texto oficial** (fuentes secundarias indican 48 horas desde que se conoce el incidente): tratarlo como 48 horas hasta que se confirme.
5. **Corregir** la causa y probar.
6. **Cerrar:** escribir qué pasó, qué se hizo y qué cambia; actualizar políticas si corresponde.

## Contactos a completar
- Autoridad: ANPD (canal oficial por confirmar).
- Proveedores: Supabase, Google/Firebase, Cloudflare, Brevo (soporte de cada uno).
