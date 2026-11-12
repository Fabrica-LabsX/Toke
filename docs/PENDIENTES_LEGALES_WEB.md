# Pendientes legales y de la web (interno, no se publica)

Estado del 2026-11-12. Los documentos de la web están en **versión 0.1, pendientes de revisión jurídica**. No se declara que un abogado los revisó.

## Decidido por el dueño (2026-11-12) y ya en la web
- Los documentos 0.1 los redacta el propio equipo; no hay abogado ni empresa. La web lo dice así (no afirma revisión jurídica).
- Responsable: solo el grupo "Fábrica Labs (FLABS)"; sin empresa, RUC ni domicilio. Edad mínima 18 años. Precios con impuestos incluidos.
- Pro siempre mensual, sin renovación automática. Pack 3 meses por adelantado con 30 % de descuento: S/ 41.79 (3 x 19.90 x 0.70). CONFIRMAR la base de cálculo.
- Reembolso: 7 días desde el pago si no estás satisfecho (Pro mensual y pack); pasado ese plazo no aplica.
- Eliminación de cuenta: se borra todo de inmediato (cascada en la base + QR en Storage, función `eliminar-cuenta` v2). Plan Free de Supabase sin copias de seguridad propias; cualquier resto de proveedor, máx. 30 días.
- Libro de Reclamaciones: quitado temporalmente (sin RUC/empresa).

## Datos que aún faltan (no inventar)
- Fecha de entrada en vigor de cada documento.
- Registro del banco de datos personales ante la autoridad.
- Dónde se alojará el APK (ver CLAUDE.md) para completar Descargas.
- Si se pasa Supabase a plan Pro (trae copias diarias): actualizar la política de privacidad.

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
