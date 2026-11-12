# Matriz de decisiones aprobadas y estado de implementación

Fecha: 2026-11-12. Continúa desde el commit `953feb9`. Estados: **IMPLEMENTADO** (verificado en código/web local), **PARCIAL**, **DOCUMENTADO** (solo en documento, falta construirlo), **BLOQUEADO** (falta dato o autorización). Nada está publicado ni desplegado.

| ID | Decisión aprobada | Dónde se aplicó | Estado | Notas |
|----|-------------------|-----------------|--------|-------|
| IDEN | Regularizar TOKE a nombre de una persona natural; FLABS = nombre del grupo, sin RUC ni personalidad jurídica | Términos §1, Privacidad §2, Centro legal: "se está regularizando… a nombre de una persona natural". No se publica "Diego A." | PARCIAL | Faltan datos: ver `REGULARIZACION_PERSONA_NATURAL_TOKE.md` |
| PRI-1 | Propietario responsable de los datos de sus pagadores; TOKE encargado; TOKE responsable de cuentas y soporte | Privacidad §2, con la marca "modelo de trabajo, sujeto a validación" | IMPLEMENTADO (texto) | No es conclusión jurídica |
| PRI-2 | Cobros se conservan hasta que el propietario elimine el negocio | Privacidad §9: tabla por tipo de dato | IMPLEMENTADO (texto) | Verificado: no hay borrado automático de cobros |
| PRI-3 | Sin estadísticas propias | Privacidad §9 | IMPLEMENTADO (texto) | No hay analítica en la app ni en la web (verificado) |
| PRI-5 | Procedimiento para terceros | Privacidad §11.1 (asunto "Datos de terceros", datos para identificar el pago, traslado al negocio) | IMPLEMENTADO (texto) | Plazo de respuesta: "el que fije la ley" (no verificado) |
| PRI-6 | No guardar el texto original de la notificación | Android: ver informe §2 | IMPLEMENTADO (Android + texto) | Build y pruebas en verde; falta probar en el celular |
| PRI-7 | Verificar inscripción de bancos de datos | `OBLIGACIONES_LEGALES_VERIFICADAS_TOKE.md` | BLOQUEADO | Texto oficial inaccesible desde esta sesión |
| PRI-8 | Verificar Oficial de Datos Personales | Igual | BLOQUEADO | Igual |
| ELI-1 | Eliminación de trabajadores sin correo | Eliminar cuenta: sección "Si eres trabajador y no tienes correo" (confirma el propietario) | IMPLEMENTADO (texto) | Procedimiento manual |
| ELI-2 | Expulsado: 30 días y borrar | Informe §4: propuesta SQL | DOCUMENTADO / BLOQUEADO | Falta columna con la fecha de expulsión y tarea programada (cambio en Supabase: sin autorización) |
| SEG-1 | Responsable y procedimiento de incidentes | `PROCEDIMIENTO_INCIDENTES_TOKE.md` (interno); Privacidad §10 | IMPLEMENTADO (doc) | Plazos legales sin confirmar en fuente oficial |
| SEG-2 | Canal de soporte, asunto "Vulnerabilidad de seguridad" | Contacto, Seguridad | IMPLEMENTADO | |
| SEG-3 | Logs y respaldos | Android: ver informe §2 y §3 | IMPLEMENTADO | Falta probar en el celular y en una versión de producción real |
| PRV | Investigar proveedores y accesos | Informe §5 | PARCIAL | Faltan regiones de Firebase/Brevo/Cloudflare y quién accede a los paneles |
| COM-2 | Precios aprobados | Landing, Planes y pagos (sin cambios) | IMPLEMENTADO | La app no muestra precios |
| COM-3 | Al vencer Pro: Free, solo lectura, bloqueo de altas | Planes y pagos §7 | PARCIAL | Real: se bloquean altas nuevas y no se borra nada; **no hay "solo lectura"** (editar sigue permitido). El texto dice solo lo real |
| COM-4 | Reembolso por el mismo medio, hasta 7 días hábiles | Planes y pagos §6 | IMPLEMENTADO (texto) | Sin sustituir derechos del consumidor |
| COM-1/5 | Libro de Reclamaciones y contratación a distancia | `OBLIGACIONES_LEGALES_VERIFICADAS_TOKE.md` | BLOQUEADO | Texto oficial inaccesible; fuentes secundarias indican obligación para plataformas digitales |
| LIC-1 | Yape/Plin solo en texto, sin logotipos | Landing y Seguridad: logos reemplazados por etiquetas de texto; pie y Licencias: "no afiliada, respaldada ni representa" | IMPLEMENTADO | Los logos PNG se eliminaron del sitio |
| (nuevo) | No anunciar como operativa una modalidad de cobro que dependa de datos sin regularizar | Landing: botón "Pro: próximamente"; Planes y pagos: aviso de estado | IMPLEMENTADO (web) | La app todavía abre WhatsApp en "Activar plan Pro": decisión pendiente |
