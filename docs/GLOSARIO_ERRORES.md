# Glosario de códigos de error de TOKE

Para soporte. El usuario ve un mensaje corto con un código; el teléfono (versión pública) solo escribe en el registro un código `E-xxxx` y la clase de la excepción. Nunca nombres de tablas, rutas, ids ni claves. Generado el 2026-11-12; la tabla `E-xxxx` se regenera con `python .artifacts/glosarios.py`.

## 1. Lo que ve el usuario en pantalla
| Texto / código | Cuándo sale | Qué hacer |
|---|---|---|
| `(Código RED)` Sin conexión | Sin internet o servidor inalcanzable | Revisar internet y reintentar |
| `(Código TIEMPO)` | La conexión tardó demasiado | Reintentar; si se repite, revisar la señal |
| `(Código 401)` | Sesión vencida | Iniciar sesión otra vez |
| `(Código 403)` | La regla de acceso del servidor (RLS) lo rechazó | Revisar que la cuenta sea dueño/miembro activo del negocio |
| `(Código 409)` | Registro duplicado o en conflicto | No repetir la acción; revisar si ya existe |
| `(Código 400)` | Datos no válidos | Corregir el dato enviado |
| `(Código <HTTP>)` | Otro error del servidor | Reintentar más tarde; avisar a soporte si persiste |
| `(Código AUTH)` | Fallo de autenticación genérico | Cerrar sesión y entrar de nuevo |
| `(Código TOKE-000)` | Error inesperado sin clasificar | Pedir captura y hora; buscar en el registro `E-xxxx` |

## 2. Invitaciones (código de ayuda de 3 dígitos)
| Código | Nombre interno | Significado | Acción de soporte |
|---|---|---|---|
| 101 | INVITACION_NO_ENCONTRADA | El código no existe | Confirmar que lo escribió bien; el dueño genera uno nuevo |
| 102 | INVITACION_YA_UTILIZADA | Ya la usó otra persona | Generar una invitación nueva |
| 103 | INVITACION_EXPIRADA | Pasaron los 20 minutos | Generar una invitación nueva |
| 104 | ERROR_VERIFICANDO_INVITACION | Falló la consulta de verificación | Reintentar; revisar conexión |
| 105 | RESPUESTA_INVITACION_INVALIDA | El servidor respondió algo inesperado | Actualizar la app; avisar al desarrollador |
| 106 | ERROR_SESION_TRABAJADOR | No se pudo crear la sesión del trabajador | Reintentar; revisar conexión |
| 107 | SESION_TRABAJADOR_NO_DISPONIBLE | No hay sesión disponible para aceptar | Cerrar y abrir la app |
| 108 | ERROR_ACEPTANDO_INVITACION | Falló al aceptar | Reintentar con un código nuevo |
| 109 | ERROR_RED_INVITACION | Sin red al procesar la invitación | Revisar internet |
| 110 | ERROR_INESPERADO_INVITACION | Cualquier otro fallo | Pedir captura y hora |

## 3. Límites del plan Free (vienen del servidor como texto con prefijo)
| Prefijo en el servidor | Mensaje al usuario | Causa |
|---|---|---|
| `LIMITE_NEGOCIOS` | El plan Free permite 1 negocio | `tg_limite_negocios` al crear un negocio |
| `LIMITE_COLABORADORES` | El plan Free permite hasta 2 colaboradores | `tg_limite_colaboradores` al aceptar un trabajador |
| `LIMITE_METODOS` | El plan Free permite hasta 4 métodos de cobro | `tg_limite_metodos` al vincular un método |
| `CUOTA_AGOTADA` | Se acabó la cuota de hoy del plan Free | `tg_cuota_operaciones` al registrar un cobro |

Solución: activar Pro (ver `GLOSARIO_SQL.md`, §Planes) o esperar al día siguiente (cuota).

## 4. Errores de las funciones SQL (RPC)
| Mensaje | Función | Causa |
|---|---|---|
| No autenticado | actualizar_negocio, estado_plan, liberar_sesion, reclamar_sesion | Sin sesión válida |
| No autorizado | actualizar_negocio, estado_plan | La cuenta no es dueña del negocio |
| El nombre es muy corto / Color no válido / Rubro no válido | actualizar_negocio | Datos del formulario rechazados |
| Negocio no encontrado | estado_plan | El negocio ya no existe |
| Dispositivo no válido | reclamar_sesion | Id de dispositivo vacío o mal formado |
| Esta billetera ya está activa en otro negocio tuyo | validar_billetera_unica_por_dueno | El mismo método ya está activo en otro negocio del dueño |
| No existe una cuenta con ese correo | admin_* | Correo mal escrito |
| Código no válido / No existe una cuenta con ese código / Más de una cuenta coincide con ese código | admin_buscar_codigo | Código `TOKE-XXXXXXXX` incompleto, inexistente o ambiguo |

## 5. Funciones Edge (HTTP)
| Función | Respuesta | Significado |
|---|---|---|
| eliminar-cuenta | 403 `REAUTH_FAILED` | Contraseña o frase ELIMINAR incorrecta |
| eliminar-cuenta | 409 `HAS_BUSINESSES` | Tiene negocios; confirmar `cerrarNegocios` |
| eliminar-cuenta | 500 `DELETE_FAILED` | No se pudo borrar el usuario; reintentar |
| eliminar-cuenta | 200 `yaEliminada` | La cuenta ya no existía |
| enviar-push | 401 "No autorizado" | Falta o no coincide `x-push-secret` |
| limpiar-qr | 401 "No autorizado" | Igual (desde el parche del 2026-11-12) |
| check-app-release | 404 `no_release` / 503 `unavailable` | No hay versión activa / base no disponible |

## 6. Códigos del registro del teléfono (`E-xxxx`)
Cada etiqueta de registro se convierte en `E-` + 4 hex (los 16 bits bajos de `String.hashCode()`). Se ve con `adb logcat -s TOKE`. Ejemplo de línea: `E-8559 RestException` = fallo de red/servidor en la etiqueta indicada. Solo salen niveles error y aviso; los de depuración no existen en la versión pública.

| Código | Etiqueta | Archivo(s) |
|---|---|---|
| `E-54A7` | `Actualizacion` | ActualizacionStore.kt |
| `E-8559` | `CuentaStore` | CuentaStore.kt |
| `E-F29B` | `FirebaseTokenProv` | FirebaseProveedorDeToken.kt |
| `E-1A4B` | `HomeScreen` | HomeScreen.kt |
| `E-9800` | `InMemoryOperationStore` | InMemoryOperationStore.kt |
| `E-0415` | `InvitacionDto` | InvitacionRepository.kt |
| `E-E808` | `MainActivity` | MainActivity.kt |
| `E-BF60` | `MetodoCobroRealtime` | MetodoDeCobroRealtimeListener.kt |
| `E-973A` | `NegocioViewModel` | NegocioViewModel.kt |
| `E-EFA1` | `OperacionPendienteStore` | OperacionPendienteStore.kt |
| `E-7531` | `OperacionesRealtime` | OperacionesRealtimeListener.kt |
| `E-38DD` | `OperationLocalStore` | OperationLocalStore.kt |
| `E-2F18` | `PlanStore` | PlanStore.kt |
| `E-4D1A` | `QrEditorScreen` | QrEditorScreen.kt |
| `E-B828` | `QrImageUploader` | QrImageUploader.kt |
| `E-3A07` | `RealtimeSyncManager` | RealtimeSyncManager.kt |
| `E-BAD2` | `RelacionDto` | RelacionCuentaNegocioRepository.kt |
| `E-128B` | `SesionSeguraStore` | SesionSeguraStore.kt |
| `E-0567` | `SesionUnica` | SesionUnicaStore.kt |
| `E-FBB5` | `SupabaseAuthBridge` | SupabaseAuthBridge.kt |
| `E-9B27` | `TabRefreshManager` | TabRefreshManager.kt |
| `E-1EDA` | `TokeActionReceiver` | TokeActionReceiver.kt |
| `E-9F40` | `TokeAndroidNotifier` | AndroidNotifier.kt |
| `E-B608` | `TokeArranque` | MainActivity.kt |
| `E-EBD4` | `TokeEquipo` | BusinessViewModel.kt, RelacionCuentaNegocioRepository.kt |
| `E-385F` | `TokeInvitacion` | ConfirmarInvitacionScreen.kt, ValidarInvitacion.kt |
| `E-0109` | `TokeListener` | TokeNotificationListenerService.kt |
| `E-2D1A` | `TokeOperandoService` | TokeOperandoService.kt |
| `E-2FFF` | `TokeReloj` | ReintentoPorReloj.kt |
| `E-4D2B` | `TokeServicio` | ServiciosToke.kt |
| `E-C970` | `TokeSync` | MainActivity.kt |
