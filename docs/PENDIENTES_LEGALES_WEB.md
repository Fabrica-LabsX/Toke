# Pendientes legales y de la web (interno, no se publica)

Estado del 2026-11-12. Los documentos de la web están en **versión 0.1, pendientes de revisión jurídica**. No se declara que un abogado los revisó.

## Decidido por el dueño (2026-11-12) y ya en la web
- Responsable: solo el grupo "Fábrica Labs (FLABS)"; sin empresa, RUC ni domicilio.
- Plazo de 30 días para borrar restos tras eliminar la cuenta, y ventana de 30 días para pedir reembolso.
- Edad mínima 18 años. Precios con impuestos incluidos. Pro siempre mensual; sin renovación automática.
- Pack Pro 3 meses por adelantado: se publicó S/ 54.70 (14.90 + 19.90 + 19.90). El dueño dijo "14.9+20+20" (=54.90): CONFIRMAR cifra.
- Libro de Reclamaciones (`/libro-de-reclamaciones`): por correo/WhatsApp; sin RUC puede no cumplir la norma peruana, validar con asesoría.
- Política de reembolsos: borrador (no activado, falla atribuible a TOKE, meses no iniciados del pack; 30 días): CONFIRMAR.

## Datos que aún faltan (no inventar)
- Fecha de entrada en vigor de cada documento y revisión jurídica.
- Registro del banco de datos personales ante la autoridad (consultar asesoría peruana).
- Dónde se alojará el APK (ver CLAUDE.md, sección de releases) para completar Descargas.

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
