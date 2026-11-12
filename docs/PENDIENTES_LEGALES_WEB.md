# Pendientes legales y de la web (interno, no se publica)

Estado del 2026-11-12. Los documentos de la web están en **versión 0.1, pendientes de revisión jurídica**. No se declara que un abogado los revisó.

## Datos que faltan para la versión definitiva (no inventar)
- Identidad legal del responsable (nombre completo de la persona natural u operador, RUC si aplica, domicilio). Hoy se presenta FLABS como proyecto independiente unipersonal, sin sociedad.
- Fecha de entrada en vigor de cada documento.
- Plazos de conservación de operaciones, historial y copias de seguridad.
- Edad mínima de uso y procedimiento para menores.
- Política de reembolsos concreta y medios de pago aceptados (hoy: activación manual por WhatsApp, sin cobro automático).
- Si el precio incluye impuestos y si el plan Pro es siempre mensual (la web dice: S/ 14.90 el primer mes, luego S/ 19.90).
- Si corresponde el Libro de Reclamaciones y/o registro del banco de datos personales ante la autoridad (consultar con asesoría peruana).
- Dónde se alojará el APK (ver CLAUDE.md, sección de releases) para completar la página de Descargas.

## Hecho en la web (2026-11-12)
- Centro legal (`/legal`) y documentos: Términos, Privacidad, Cookies, Planes y pagos, Uso aceptable; eliminación de cuenta ampliada.
- Datos de contacto tomados de la propia web: AeroCorpKT@protonmail.com y WhatsApp +51 998 360 217 (todos los días, 2:00–8:00 p. m.).
- La web ya **no se conecta con la app ni con Supabase**: se quitó la lectura de versiones del JavaScript y el permiso en la política de seguridad (`connect-src 'none'`). Descargas y Novedades son páginas estáticas que se editan a mano en cada versión.
- Cabecera HSTS añadida, mapa del sitio actualizado, enlaces comprobados (0 rotos), un solo H1 por página.

## Por hacer en la web
- Publicar (desplegar) estos cambios: requiere aprobación del dueño del proyecto.
- Enlace real del APK, versión, tamaño y SHA-256 en `/descargas` cuando exista la primera versión pública.
- Revisión de accesibilidad con lector de pantalla y teclado (solo se verificó visualmente y sin desborde en móvil).
- Google Search Console: preparar la verificación; no se afirma que el sitio esté verificado.
- Licencias y atribuciones: `/licencias` sigue con su aviso de revisión pendiente.
