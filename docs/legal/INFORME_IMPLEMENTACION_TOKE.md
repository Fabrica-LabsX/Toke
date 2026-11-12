# Informe de implementación (Centro Legal de TOKE)

Fecha: 2026-11-12. Nada se publicó. No se tocó la app, Supabase, RLS ni datos.

## Qué estaba bien (se dejó igual)
- Precios, cuotas Free y renovación manual: coinciden con `config_planes`, la landing y Planes y pagos.
- Eliminación de cuenta y negocio, QR en menos de 1 día, limpieza de cuentas sin verificar (24 h) y de sesiones anónimas (7 d): demostrados por la base y las funciones.
- Cookies: no hay cookies propias ni analítica; coincide con el JavaScript y las cabeceras.
- Uso aceptable y Términos §13 (con salvaguarda de derechos irrenunciables).
- Enlaces internos: 0 rotos; sin documentos duplicados; nombre del grupo uniforme.

## Qué estaba incompleto
- No se decía que el celular del propietario guarda una copia del texto de las notificaciones de pago.
- No se aclaraba que el nombre del pagador es un dato de terceros ni cuánto dura.
- No se citaba el Reglamento D.S. 016-2024-JUS ni el término "derechos ARCO".

## Qué estaba equivocado
- Privacidad y Seguridad prometían que TOKE solo lee avisos de pago o "nada más", cuando Android entrega todas las notificaciones al servicio (lo que hace TOKE es descartar sin leer las que no vienen de las 13 apps de pago).
- Privacidad decía que los registros técnicos ocultan los datos sensibles, pero el código imprime el texto de notificaciones en logcat.
- Privacidad §10 enumeraba procesos de seguridad (revisión de eventos, gestión de vulnerabilidades, evaluación de proveedores) sin evidencia.

## Qué se corrigió automáticamente (en `web/`, sin publicar)
- `privacidad.html` pasa a **v0.2** con historial de versiones: §3.3 (qué llega al servidor, qué queda en el celular, datos de terceros), §3.4 (registros), §4 (acceso a notificaciones y lista de 13 apps), §10 (solo medidas demostradas), §11 (cita D.S. 016-2024-JUS y derechos ARCO).
- `seguridad.html`: precisión sobre la lectura de avisos.
- Generador de la web actualizado (`.artifacts/web_build.py`, local) y 4 documentos nuevos en `docs/legal/`.

## Qué se dejó pendiente
- 28 preguntas en [CUESTIONARIO_PENDIENTES_TOKE.md](CUESTIONARIO_PENDIENTES_TOKE.md) (identidad, privacidad, proveedores, eliminación, seguridad, comercial). Ninguna presenta como aprobado lo que depende de ellas: los documentos siguen en 0.x, sin fecha de entrada en vigor.
- Las páginas Términos, Cookies, Planes y Uso aceptable no cambiaron (0.1).

## Qué necesita aprobación humana
1. Publicar los cambios de Privacidad 0.2 y Seguridad (Cloudflare).
2. Autorizar los cambios de la app: quitar `Log` con texto en la versión pública, excluir datos locales del respaldo de Google y limitar el tiempo de la copia local de notificaciones (Q-SEG-3, Q-PRI-6).
3. Decidir el Libro de Reclamaciones y la identidad del proveedor (Q-COM-1, Q-ID-1 a 4).

## Qué no pudo verificarse
Ver la sección 5 de [AUDITORIA_LEGAL_TOKE.md](AUDITORIA_LEGAL_TOKE.md): textos oficiales de las normas, plazos ARCO, regiones de proveedores, contratos, copias internas de Supabase, texto de la pantalla de permiso en la app y pruebas con 4 roles.

## Riesgos que siguen abiertos
- **Alto:** posible obligación del Libro de Reclamaciones (Ley 32495) y falta de identificación del proveedor; el texto "impuestos incluidos" sin emisor fiscal.
- **Alto:** registros con texto de notificaciones y respaldo de Google mientras no se cambie la app.
- **Medio:** sin registro de banco de datos verificado, sin procedimiento de incidentes ni responsable de datos.
- **Medio:** los documentos son redactados por el equipo, sin revisión de abogado; las citas legales se apoyan en fuentes secundarias.

## Control de calidad
- Build de la web (generador): correcto. Enlaces: 0 rotos. Sitemap: 14 URL.
- Tests de la app: **no se ejecutaron** (esta tarea no cambió la app; hay cambios locales sin compilar del "deshacer de transiciones").
- No se publican datos confidenciales: los documentos citan archivos y nombres de paquetes de apps públicas, no claves ni correos personales.
