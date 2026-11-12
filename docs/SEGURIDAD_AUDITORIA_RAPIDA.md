# Auditoría de seguridad rápida (método de cloudflare/security-audit-skill)

Fecha: 2026-11-12. Rama `feature/figma-precision-2026-10-11`. Skill descargada en `.artifacts/ext/security-audit-skill` (solo lectura; es una guía de método, no un programa que se ejecuta).

## Alcance real (cobertura PARCIAL)
Perfil **quick**, hecho a mano sin subagentes ni validadores de la skill (no corrí el flujo completo de 6 fases). Revisado: 4 funciones Edge, todas las políticas RLS y de Storage de Supabase, columnas de `cuentas`/`negocios`/`operaciones`. **No revisado a fondo:** servicio de notificaciones, actualizador de APK, RPC `SECURITY DEFINER` (cuerpos), pruebas dinámicas. Nada se probó contra producción.

## Cómo funciona TOKE (resumen de protocolo, desde la web y el código)
App Android lee notificaciones → filtra 13 apps de pago → normaliza → el dueño inserta la operación en Supabase (RLS) → trigger llama `enviar-push` (secreto compartido) → FCM avisa a trabajadores → trabajadores leen con su propia sesión (anónima) y reclaman por RPC. QR en bucket `metodos-qr/<negocio_id>/`. Actualizador valida SHA-256, tamaño, paquete y firma.

## Hallazgos
| # | Severidad | Hallazgo | Arreglo mínimo |
|---|-----------|----------|----------------|
| 1 | Baja | `negocios_update_dueno` no tiene `WITH CHECK`: el dueño podría cambiar `dueno_cuenta_id` de su negocio a otro UUID y "regalarlo" (necesita conocer el UUID de la víctima; los límites de plan se validan solo al INSERT). | Añadir `WITH CHECK (dueno_cuenta_id = auth.uid())`. |
| 2 | Baja | `cuentas_update_propia` sin `WITH CHECK`: el usuario puede escribir sus propios `correo_verificado`/`telefono_verificado` (texto). Hoy no autorizan nada, pero no deben tomarse como prueba. | `WITH CHECK (auth.uid() = id)` y no leer esos campos para decisiones, o quitar `UPDATE` de esas columnas. |
| 3 | Baja | `limpiar-qr` es pública (`verify_jwt=false`) y recorre Storage y la base en cada llamada: cualquiera podría llamarla en bucle y gastar cuota. No borra nada que no esté ya en cola/huérfano. | Exigir cabecera `x-cron-secret` como en `enviar-push`. |
| 4 | Informativa | `operaciones.notif_snapshot_titulo` guarda el nombre del pagador; `notif_snapshot_texto` y `_app` están vacías (0 de 2 filas). Coincide con lo publicado, pero el nombre de columna sugiere más de lo que guarda. | Renombrar en una migración futura (no urgente). |

## Verificado correcto
- 0 tablas sin RLS; Storage `metodos-qr`: el dueño gestiona solo su carpeta; el trabajador solo lee QR activos y visibles de su negocio.
- `eliminar-cuenta` reautentica (contraseña o frase) antes de borrar y usa `service_role` solo en el servidor.
- `enviar-push` compara el secreto en tiempo constante y responde 401 sin él.
- `check-app-release` es solo lectura y no devuelve datos privados; el APK se verifica en el teléfono.
- Las invitaciones solo las crea un dueño con correo (no anónimo).
- Código de la app: sin SDK de publicidad ni analítica; `LogSeguro` sin datos en release; texto de notificaciones no se guarda.

## No verificado / riesgos
- Cuerpos de las RPC (`reclamar_operacion`, `confirmar_pago`, etc.) y sus permisos de ejecución (ver avisos pendientes en `CLAUDE.md` §11).
- Protección de contraseñas filtradas desactivada en Auth.
- Ningún cambio se aplicó en Supabase: los arreglos 1–3 están solo propuestos (requieren tu autorización).
