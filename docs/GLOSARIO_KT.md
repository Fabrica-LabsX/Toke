# Glosario de archivos Kotlin de TOKE

Generado el 2026-11-12 con `.artifacts/glosarios.py` leyendo **todos** los `.kt` del proyecto (300 archivos). La descripción sale del primer comentario del archivo o de su primera declaración; si no hay comentario en el código, se dice así (no se inventa). Para cada archivo se listan sus declaraciones de primer nivel (clases, objetos, funciones). Para regenerar: `python .artifacts/glosarios.py`.

## Código de la app (`app/src/main`) — 248 archivos

### `com/flabs/cobrate`

- **`MainActivity.kt`** (1132 líneas) — (sin comentario en el código)
  - `class MainActivity`
  - `fun CobrateApp`
  - `object Routes`

### `com/flabs/cobrate/components`

- **`AccesoPausadoPiezas.kt`** (254 líneas) — Icono grande de pausa en un cuadro rosado, con el punto rojo de alerta.
  - `fun AccesoPausadoIcono`: Icono grande de pausa en un cuadro rosado, con el punto rojo de alerta.
  - `fun AccesoPausadoEstado`: Tarjeta "Estado en el negocio": nombre del negocio, estado pausado y qué hacer.
  - `fun AccesoPausadoBoton`: Botón grande del pie.
  - `fun AccesoPausadoEnlace`: Acción discreta en texto, para lo que no es lo principal (cerrar sesión, salir, crear propio).
- **`AvatarIniciales.kt`** (62 líneas) — Función pura que deriva las iniciales a partir de un nombre o identificador.
  - `fun inicialesDe`: Función pura que deriva las iniciales a partir de un nombre o identificador.
- **`BadgeConexionOnline.kt`** (44 líneas) — Badge de conexión en línea (cabecera del Home del dueño y del trabajador).
  - `fun BadgeConexionOnline`: Badge de conexión en línea (cabecera del Home del dueño y del trabajador).
- **`BadgeSincronizacion.kt`** (60 líneas) — Badge del estado del servicio de TOKE, junto al de conexión (mismo tamaño y forma): - [operativo]: fondo verde con el icono de sincronización (sin check); - si no: fondo rojo suave con el icono de sincronización en rojo, sin check
  - `fun BadgeSincronizacion`: Badge del estado del servicio de TOKE, junto al de conexión (mismo tamaño y forma): - [operativo]: fondo verde con el icono de sincronización (sin check); - si no: fondo rojo suave con el icono de sincronización en rojo, sin check
- **`BilleteraColores.kt`** (56 líneas) — (sin comentario en el código)
  - `object BilleteraColores`
  - `fun colorBilletera`
  - `fun colorBilletera`
- **`CambiarNegocioBottomSheet.kt`** (188 líneas) — Selector "Cambiar de negocio".
  - `fun CambiarNegocioBottomSheet`: Selector "Cambiar de negocio".
- **`CampoBusqueda.kt`** (101 líneas) — Campo de búsqueda común de las pantallas nuevas (Operaciones, Equipo).
  - `fun CampoBusqueda`: Campo de búsqueda común de las pantallas nuevas (Operaciones, Equipo).
- **`ConfirmarAccionDialog.kt`** (58 líneas) — Confirmación de acciones importantes o destructivas.
  - `fun ConfirmarAccionDialog`: Confirmación de acciones importantes o destructivas.
- **`ConfirmarCierreSesionDialog.kt`** (23 líneas) — Confirmación para cerrar la sesión.
  - `fun ConfirmarCierreSesionDialog`: Confirmación para cerrar la sesión.
- **`DialogoPermiso.kt`** (24 líneas) — Ventana para pedir un permiso del sistema, con el mismo estilo que el resto de confirmaciones.
  - `fun DialogoPermiso`: Ventana para pedir un permiso del sistema, con el mismo estilo que el resto de confirmaciones.
- **`DialogoToke.kt`** (154 líneas) — Ventana de confirmación única de la app: título, un texto de hasta 3 líneas y dos botones (cancelar a la izquierda, confirmar a la derecha; en rojo si [destructivo]).
  - `fun DialogoToke`: Ventana de confirmación única de la app: título, un texto de hasta 3 líneas y dos botones (cancelar a la izquierda, confirmar a la derecha; en rojo si [destructivo]).
- **`FeedbackOverlay.kt`** (168 líneas) — Avisos globales.
  - `fun FeedbackOverlay`: Avisos globales.
- **`FondoPanelBorroso.kt`** (93 líneas) — Fondo borroso compartido para pantallas de acceso limitado (PAUSADO, SOLICITADO, RECHAZADO).
  - `fun FondoPanelBorroso`: Fondo borroso compartido para pantallas de acceso limitado (PAUSADO, SOLICITADO, RECHAZADO).
- **`IconoMetodoPago.kt`** (80 líneas) — Componente único y reutilizable que muestra el logo oficial de un método de cobro (C10).
  - `fun IconoMetodoPago`: Componente único y reutilizable que muestra el logo oficial de un método de cobro (C10).
- **`MasSheet.kt`** (335 líneas) — Una opción del menú Más.
  - `class ItemMas`: Una opción del menú Más.
  - `fun MasSheet`: Menú inferior "Más": aspecto de la app, aviso sin conexión (solo administradores), la lista de destinos y, siempre visibles al pie, Cerrar sesión y Apagar TOKE con su texto completo.
- **`OpcionCard.kt`** (96 líneas) — Tarjeta de opción reutilizable para selectores y menús de entrada.
  - `fun OpcionCard`: Tarjeta de opción reutilizable para selectores y menús de entrada.
- **`OperacionCard.kt`** (240 líneas) — Card reutilizable de Operaciones conforme a la especificación visual TOKE!
  - `fun OperacionCard`: Card reutilizable de Operaciones conforme a la especificación visual TOKE!
  - `class Quadruple`
  - `class OperacionCardAccion`: Acción opcional dentro de la card.
- **`PantallaAccesoPausado.kt`** (156 líneas) — Pantalla "Acceso pausado por el dueño": se muestra cuando la relación de la cuenta con el negocio está pausada.
  - `fun PantallaAccesoPausado`: Pantalla "Acceso pausado por el dueño": se muestra cuando la relación de la cuenta con el negocio está pausada.
- **`PantallaEsperandoAceptacion.kt`** (370 líneas) — Pantalla "Esperando Aprobación del Dueño" conforme al diseño oficial de Figma (Nodo 14:483).
  - `fun PantallaEsperandoAceptacion`: Pantalla "Esperando Aprobación del Dueño" conforme al diseño oficial de Figma (Nodo 14:483).
- **`PantallaSolicitudRechazada.kt`** (79 líneas) — Aviso al trabajador de que el dueño rechazó su solicitud de ingreso.
  - `fun PantallaSolicitudRechazada`: Aviso al trabajador de que el dueño rechazó su solicitud de ingreso.
- **`PedirExencionBateria.kt`** (62 líneas) — Diálogo de aviso de optimización de batería para el trabajador (CAPA 63).
  - `fun PedirExencionBateria`: Diálogo de aviso de optimización de batería para el trabajador (CAPA 63).
- **`PedirPermisoNotificaciones.kt`** (59 líneas) — (sin comentario en el código)
  - `fun PedirPermisoNotificaciones`
- **`TokeBottomBar.kt`** (299 líneas) — Barra de navegación inferior persistente con FAB central.
  - `class NavPalette`
  - `class ItemNav`
  - `fun TokeBottomBar`: Barra de navegación inferior persistente con FAB central.
- **`TokeIcons.kt`** (100 líneas) — (sin comentario en el código)
  - `object TokeIcons`

### `com/flabs/cobrate/core/data`

- **`AlertaLocalStore.kt`** (89 líneas) — Persistencia local de alertas aisladas por usuario usando Jetpack DataStore (Preferences).
  - `class AlertaLocalStore`: Persistencia local de alertas aisladas por usuario usando Jetpack DataStore (Preferences).
  - `fun AlertaLocalStore.AlertaDto.aModelo`
- **`AlertaStore.kt`** (121 líneas) — Fuente de alertas compartida en memoria (singleton) con persistencia local aislada por cuenta.
  - `object AlertaStore`: Fuente de alertas compartida en memoria (singleton) con persistencia local aislada por cuenta.
- **`ConfiguracionDeVozLocalStore.kt`** (92 líneas) — Persistencia local de la configuración de voz usando Jetpack DataStore (Preferences) con serialización JSON manual vía kotlinx.serialization — MISMO patrón que [CuentaLocalStore]/[NegocioLocalStore].
  - `class ConfiguracionDeVozLocalStore`: Persistencia local de la configuración de voz usando Jetpack DataStore (Preferences) con serialización JSON manual vía kotlinx.serialization — MISMO patrón que [CuentaLocalStore]/[NegocioLocalStore].
  - `fun ConfiguracionDeVozLocalStore.ConfiguracionDeVozDto.aModelo`
- **`ConfiguracionDeVozStore.kt`** (69 líneas) — Fuente única de configuración de voz compartida en memoria (singleton) con persistencia local.
  - `object ConfiguracionDeVozStore`: Fuente única de configuración de voz compartida en memoria (singleton) con persistencia local.
- **`CuentaLocalStore.kt`** (101 líneas) — Persistencia local de cuentas usando Jetpack DataStore (Preferences) con serialización JSON manual vía kotlinx.serialization — MISMO patrón que [OperationLocalStore].
  - `class CuentaLocalStore`: Persistencia local de cuentas usando Jetpack DataStore (Preferences) con serialización JSON manual vía kotlinx.serialization — MISMO patrón que [OperationLocalStore].
  - `fun CuentaLocalStore.CuentaDto.aModelo`: Convierte un DTO persistido en el modelo de dominio [Cuenta] (sin hash).
- **`CuentaRepository.kt`** (89 líneas) — Repositorio de Cuentas conectado a Supabase.
  - `class CuentaRepository`: Repositorio de Cuentas conectado a Supabase.
  - `fun Cuenta.toDto`
  - `fun CuentaRepository.CuentaDto.toDomain`
- **`CuentaStore.kt`** (350 líneas) — Estados del flujo de sincronización remota de datos del usuario.
  - `class EstadoSincronizacionRemota`: Estados del flujo de sincronización remota de datos del usuario.
  - `object CuentaStore`: Fuente de cuentas compartida en memoria (singleton) con persistencia local.
- **`DispositivoRepository.kt`** (50 líneas) — Repositorio para registrar y quitar tokens de dispositivos en Supabase (CAPA 52).
  - `class DispositivoRepository`: Repositorio para registrar y quitar tokens de dispositivos en Supabase (CAPA 52).
- **`EstadoOperativoStore.kt`** (37 líneas) — Fuente del estado operativo global de la aplicación (encendido vs.
  - `object EstadoOperativoStore`: Fuente del estado operativo global de la aplicación (encendido vs.
- **`EstadoServiciosLocalStore.kt`** (98 líneas) — Persistencia local del estado de SERVICIOS usando Jetpack DataStore (Preferences) con serialización JSON manual vía kotlinx.serialization — MISMO patrón que [CuentaLocalStore]/[AlertaLocalStore].
  - `class EstadoServiciosLocalStore`: Persistencia local del estado de SERVICIOS usando Jetpack DataStore (Preferences) con serialización JSON manual vía kotlinx.serialization — MISMO patrón que [CuentaLocalStore]/[AlertaLocalStore].
  - `fun EstadoServiciosLocalStore.EstadoServiciosDto.aModelo`
- **`EstadoServiciosStore.kt`** (73 líneas) — Fuente del control "SERVICIOS" compartida en memoria (singleton) con persistencia local.
  - `object EstadoServiciosStore`: Fuente del control "SERVICIOS" compartida en memoria (singleton) con persistencia local.
- **`EventoDeNotificacionLocalStore.kt`** (17 líneas) — Solo borra el archivo antiguo `eventos_notificacion` (ya no se escribe).
  - `class EventoDeNotificacionLocalStore`: Solo borra el archivo antiguo `eventos_notificacion` (ya no se escribe).
- **`EventoDeNotificacionStore.kt`** (22 líneas) — PRI-6: TOKE ya no guarda el texto original de las notificaciones.
  - `object EventoDeNotificacionStore`: PRI-6: TOKE ya no guarda el texto original de las notificaciones.
- **`InMemoryOperationStore.kt`** (316 líneas) — Fuente de operaciones compartida en memoria (singleton) con persistencia local.
  - `object InMemoryOperationStore`: Fuente de operaciones compartida en memoria (singleton) con persistencia local.
- **`InvitacionLocalStore.kt`** (84 líneas) — Persistencia local de invitaciones de trabajador usando Jetpack DataStore (Preferences) con serialización JSON manual vía kotlinx.serialization — MISMO patrón que [CuentaLocalStore].
  - `class InvitacionLocalStore`: Persistencia local de invitaciones de trabajador usando Jetpack DataStore (Preferences) con serialización JSON manual vía kotlinx.serialization — MISMO patrón que [CuentaLocalStore].
  - `fun InvitacionLocalStore.InvitacionDto.aModelo`
- **`InvitacionRepository.kt`** (220 líneas) — Repositorio de Invitaciones de Trabajador conectado a Supabase.
  - `object SafeEstadoInvitacionSerializer`
  - `class InvitacionRepository`: Repositorio de Invitaciones de Trabajador conectado a Supabase.
  - `fun InvitacionRepository.InvitacionDto.toDomainOrNull`
  - `fun InvitacionRepository.InvitacionDto.toDomain`
- **`InvitacionStore.kt`** (154 líneas) — Fuente de invitaciones de trabajador compartida en memoria (singleton) con persistencia local.
  - `object InvitacionStore`: Fuente de invitaciones de trabajador compartida en memoria (singleton) con persistencia local.
- **`MetodoDeCobroLocalStore.kt`** (92 líneas) — Persistencia local de métodos de cobro por negocio usando DataStore + JSON.
  - `class MetodoDeCobroLocalStore`: Persistencia local de métodos de cobro por negocio usando DataStore + JSON.
  - `fun MetodoDeCobro.aDto`
  - `fun MetodoDeCobroLocalStore.MetodoDeCobroDto.aModelo`
- **`MetodoDeCobroRealtimeListener.kt`** (97 líneas) — Listener de Realtime para cambios en metodos_de_cobro por negocio.
  - `object MetodoDeCobroRealtimeListener`: Listener de Realtime para cambios en metodos_de_cobro por negocio.
- **`MetodoDeCobroRepository.kt`** (130 líneas) — Repositorio para tabla metodos_de_cobro en Supabase.
  - `class MetodoDeCobroRepository`: Repositorio para tabla metodos_de_cobro en Supabase.
  - `fun MetodoDeCobroRepository.MetodoDeCobroDto.toDomain`
- **`MetodoDeCobroStore.kt`** (281 líneas) — Store de métodos de cobro con caché por negocio (singleton).
  - `object MetodoDeCobroStore`: Store de métodos de cobro con caché por negocio (singleton).
- **`NegocioLocalStore.kt`** (89 líneas) — Persistencia local de negocios usando Jetpack DataStore (Preferences) con serialización JSON manual vía kotlinx.serialization — MISMO patrón que [CuentaLocalStore].
  - `class NegocioLocalStore`: Persistencia local de negocios usando Jetpack DataStore (Preferences) con serialización JSON manual vía kotlinx.serialization — MISMO patrón que [CuentaLocalStore].
  - `fun NegocioLocalStore.NegocioDto.aModelo`
- **`NegocioRepository.kt`** (198 líneas) — Repositorio de Negocios conectado a Supabase (Postgrest).
  - `object SafeTipoNegocioSerializer`
  - `class NegocioRepository`: Repositorio de Negocios conectado a Supabase (Postgrest).
  - `fun Negocio.toDto`
  - `fun NegocioRepository.NegocioDto.toDomain`
- **`NegocioStore.kt`** (199 líneas) — Fuente de negocios compartida en memoria (singleton) con persistencia local.
  - `object NegocioStore`: Fuente de negocios compartida en memoria (singleton) con persistencia local.
- **`OperacionPendienteStore.kt`** (411 líneas) — Estado local de sincronización remota para operaciones pendientes de transmisión.
  - `class EstadoSincronizacion`: Estado local de sincronización remota para operaciones pendientes de transmisión.
  - `class ElementoPendiente`: Representa un elemento en la cola offline/reintento.
  - `object OperacionPendienteStore`: Cola de operaciones pendientes que fallaron al enviarse por falta de red o error de servidor.
- **`OperacionRepository.kt`** (243 líneas) — Contrato del Repositorio de Operaciones para inyección desacoplada en ViewModels y Tests.
  - `interface OperacionRepositoryContract`: Contrato del Repositorio de Operaciones para inyección desacoplada en ViewModels y Tests.
  - `class OperacionRepository`: Repositorio de Operaciones conectado a Supabase.
  - `class ResultadoReclamo`: Resultado de la operación atómica de reclamo en el servidor.
  - `class ResultadoConfirmarPago`: Resultado de la confirmación de pago en el servidor.
- **`OperacionesRealtimeListener.kt`** (115 líneas) — Listener de cambios en tiempo real para la tabla 'operaciones'.
  - `object OperacionesRealtimeListener`: Listener de cambios en tiempo real para la tabla 'operaciones'.
- **`OperationLocalStore.kt`** (119 líneas) — Persistencia local de operaciones por usuario (UID) usando DataStore + JSON.
  - `class OperationLocalStore`: Persistencia local de operaciones por usuario (UID) usando DataStore + JSON.
- **`PreferenciasAppStore.kt`** (134 líneas) — Persistencia de preferencias globales de la aplicación aisladas por cuenta (UID).
  - `object PreferenciasAppStore`: Persistencia de preferencias globales de la aplicación aisladas por cuenta (UID).
- **`RealtimeSyncManager.kt`** (414 líneas) — Resultado del cálculo del diferencial de canales para sincronizar escuchas Realtime.
  - `class DiffResult`: Resultado del cálculo del diferencial de canales para sincronizar escuchas Realtime.
  - `object RealtimeChannelDiff`: Función pura de producción para calcular qué canales iniciar y cuáles detener.
  - `object RealtimeSyncManager`: Orquestador de la sincronización en tiempo real de operaciones.
- **`RelacionCuentaNegocioLocalStore.kt`** (82 líneas) — Persistencia local de relaciones Cuenta↔Negocio usando Jetpack DataStore (Preferences) con serialización JSON manual — MISMO patrón que [CuentaLocalStore].
  - `class RelacionCuentaNegocioLocalStore`: Persistencia local de relaciones Cuenta↔Negocio usando Jetpack DataStore (Preferences) con serialización JSON manual — MISMO patrón que [CuentaLocalStore].
  - `fun RelacionCuentaNegocioLocalStore.RelacionDto.aModelo`
- **`RelacionCuentaNegocioRepository.kt`** (225 líneas) — Repositorio de Relaciones Cuenta-Negocio conectado a Supabase.
  - `object SafeRolSerializer`
  - `object SafeEstadoRelacionSerializer`
  - `class RelacionCuentaNegocioRepository`: Repositorio de Relaciones Cuenta-Negocio conectado a Supabase.
  - `fun RelacionCuentaNegocio.toDto`
  - `fun RelacionCuentaNegocioRepository.RelacionDto.toDomainOrNull`
  - `fun RelacionCuentaNegocioRepository.RelacionDto.toDomain`
- **`RelacionCuentaNegocioStore.kt`** (134 líneas) — Fuente de relaciones Cuenta↔Negocio compartida en memoria (singleton) con persistencia local.
  - `object RelacionCuentaNegocioStore`: Fuente de relaciones Cuenta↔Negocio compartida en memoria (singleton) con persistencia local.
- **`SesionSeguraStore.kt`** (108 líneas) — Almacenamiento cifrado de la "sesión activa" (id de la cuenta actualmente logueada) usando [EncryptedSharedPreferences] con un [MasterKey] respaldado por el Android Keystore.
  - `class SesionSeguraStore`: Almacenamiento cifrado de la "sesión activa" (id de la cuenta actualmente logueada) usando [EncryptedSharedPreferences] con un [MasterKey] respaldado por el Android Keystore.
- **`SesionUnicaStore.kt`** (170 líneas) — Aviso que la app debe mostrar por la sesión única.
  - `class AvisoSesion`: Aviso que la app debe mostrar por la sesión única.
  - `object SesionUnicaStore`: Sesión única: una cuenta solo puede estar abierta en un celular.
- **`SupabaseAuthBridge.kt`** (279 líneas) — Puente de Supabase Auth (CAPA 7b-1) — EXPERIMENTAL.
  - `interface EstadoSesion`
  - `object SupabaseAuthBridge`: Puente de Supabase Auth (CAPA 7b-1) — EXPERIMENTAL.
- **`SupabaseClientProvider.kt`** (52 líneas) — Proveedor central del cliente de Supabase.
  - `object SupabaseClientProvider`: Proveedor central del cliente de Supabase.

### `com/flabs/cobrate/core/data/dto`

- **`OperacionLocalDto.kt`** (67 líneas) — Serializador seguro para [TipoMetodoCobro].
  - `object SafeTipoMetodoCobroSerializer`: Serializador seguro para [TipoMetodoCobro].
  - `class OperacionLocalDto`: DTO unificado para persistencia local (Caché y Cola Offline).
  - `class CambioDeEstadoLocalDto`

### `com/flabs/cobrate/core/logic`

- **`AccesoNegocio.kt`** (45 líneas) — Regla pura de acceso a negocio (CAPA 36 + CAPA 47).
  - `object AccesoNegocio`: Regla pura de acceso a negocio (CAPA 36 + CAPA 47).
- **`AndroidNotifier.kt`** (320 líneas) — Estados posibles para la observabilidad de envío de un cobro hacia Supabase.
  - `class EstadoObservabilidadEnvio`: Estados posibles para la observabilidad de envío de un cobro hacia Supabase.
  - `object AndroidNotifier`: Disparador de notificaciones internas de TOKE (CAPA 13).
- **`AndroidTtsVoiceEngine.kt`** (62 líneas) — Motor de voz (CAPA 14) que reproduce frases vía [TtsSpeaker].
  - `class AndroidTtsVoiceEngine`: Motor de voz (CAPA 14) que reproduce frases vía [TtsSpeaker].
- **`AppsDePagoConfiables.kt`** (53 líneas) — Única fuente de verdad de qué apps pueden originar un pago real (CAPA 6c-2).
  - `object AppsDePagoConfiables`: Única fuente de verdad de qué apps pueden originar un pago real (CAPA 6c-2).
- **`AssignmentEngine.kt`** (43 líneas) — Regla de asignación automática de operaciones a trabajadores (CAPA 8).
  - `object AssignmentEngine`: Regla de asignación automática de operaciones a trabajadores (CAPA 8).
- **`AuthErrorUtils.kt`** (48 líneas) —  Determina si una excepción ocurrida durante el refresco o autenticación se debe a un problema real de red/conectividad (p. ej., offline, timeout, DNS),
  - `object AuthErrorUtils`
- **`BusquedaDeTerminos.kt`** (25 líneas) — Búsqueda de un término dentro de un texto ya normalizado.
  - `object BusquedaDeTerminos`: Búsqueda de un término dentro de un texto ya normalizado.
- **`DailySummary.kt`** (37 líneas) — Resumen diario calculado a partir de las operaciones.
  - `object DailySummary`: Resumen diario calculado a partir de las operaciones.
- **`DestinatarioAviso.kt`** (10 líneas) — Quién recibe el aviso de "pago confirmado" (notificación y voz): el trabajador o colaborador.
  - `object DestinatarioAviso`: Quién recibe el aviso de "pago confirmado" (notificación y voz): el trabajador o colaborador.
- **`DuracionAviso.kt`** (15 líneas) — Cuánto tiempo se deja un aviso en pantalla: lo justo para leerlo, sin pedir que lo cierren.
  - `object DuracionAviso`: Cuánto tiempo se deja un aviso en pantalla: lo justo para leerlo, sin pedir que lo cierren.
- **`EntradaSegura.kt`** (57 líneas) — Filtro de texto que escribe el usuario (nombres, correo, números, titulares).
  - `object EntradaSegura`: Filtro de texto que escribe el usuario (nombres, correo, números, titulares).
- **`EstadoOperativoLogic.kt`** (11 líneas) — Lógica pura para evaluar si TOKE debe operar en segundo plano (CAPA 60).
  - `object EstadoOperativoLogic`: Lógica pura para evaluar si TOKE debe operar en segundo plano (CAPA 60).
- **`ExtractorDeMonto.kt`** (37 líneas) — Extrae el monto en soles de un texto ya normalizado.
  - `object ExtractorDeMonto`: Extrae el monto en soles de un texto ya normalizado.
- **`FechaCobro.kt`** (29 líneas) — Cómo se muestra la hora de un cobro en las listas: solo la hora si es de hoy; con el día si es de otro día.
  - `object FechaCobro`: Cómo se muestra la hora de un cobro en las listas: solo la hora si es de hoy; con el día si es de otro día.
- **`FeedbackManager.kt`** (55 líneas) — Gestor global de feedback para la UI.
  - `object FeedbackManager`: Gestor global de feedback para la UI.
- **`FeedbackState.kt`** (211 líneas) — Tipos de feedback visual soportados por la aplicación.
  - `class FeedbackType`: Tipos de feedback visual soportados por la aplicación.
  - `class FeedbackState`: Representa un mensaje o estado de feedback para mostrar al usuario.
  - `object TokeErrorCodes`: Catálogo de códigos de ayuda TOKE de 3 dígitos (Glosario local en cliente Android).
  - `object ErrorMapper`: Mapper para convertir errores técnicos en mensajes humanos con códigos TOKE de 3 dígitos.
- **`FiltroEgresos.kt`** (22 líneas) — Veto 3: descarta transacciones salientes (cuando el usuario paga, envía o le debitan dinero).
  - `object FiltroEgresos`: Veto 3: descarta transacciones salientes (cuando el usuario paga, envía o le debitan dinero).
- **`FiltroPromocional.kt`** (26 líneas) — Veto 1: descarta publicidad, sorteos, ofertas y avisos de cobro de servicios.
  - `object FiltroPromocional`: Veto 1: descarta publicidad, sorteos, ofertas y avisos de cobro de servicios.
- **`FiltroSeguridad.kt`** (20 líneas) — Veto 2: descarta avisos de seguridad o no transaccionales (códigos, claves, inicio de sesión, actualizaciones).
  - `object FiltroSeguridad`: Veto 2: descarta avisos de seguridad o no transaccionales (códigos, claves, inicio de sesión, actualizaciones).
- **`HapticFeedback.kt`** (67 líneas) — Hace vibrar el dispositivo con un patrón corto de 200 ms.
  - `fun vibrar`: Hace vibrar el dispositivo con un patrón corto de 200 ms.
- **`InvitacionValidation.kt`** (45 líneas) — Validaciones puras de una [InvitacionTrabajador].
  - `object InvitacionValidation`: Validaciones puras de una [InvitacionTrabajador].
  - `class MotivoRechazo`: Motivos por los que una invitación no puede aceptarse.
- **`LogSeguro.kt`** (86 líneas) — Utilidad de logging seguro que desinfecta mensajes de excepción para prevenir la fuga de tokens de autenticación o claves en Logcat (CAPA 31).
  - `object LogSeguro`: Utilidad de logging seguro que desinfecta mensajes de excepción para prevenir la fuga de tokens de autenticación o claves en Logcat (CAPA 31).
- **`LoginRateLimiter.kt`** (96 líneas) — Control de intentos fallidos de inicio de sesión (Rate Limiting).
  - `object LoginRateLimiter`: Control de intentos fallidos de inicio de sesión (Rate Limiting).
- **`MapeoAppsMetodoDeCobro.kt`** (54 líneas) — Mapea cada paquete de origen confiable (ver [AppsDePagoConfiables]) al [TipoMetodoCobro] correspondiente.
  - `object MapeoAppsMetodoDeCobro`: Mapea cada paquete de origen confiable (ver [AppsDePagoConfiables]) al [TipoMetodoCobro] correspondiente.
- **`NetworkMonitor.kt`** (63 líneas) — Singleton que monitorea la disponibilidad de internet en tiempo real.
  - `object NetworkMonitor`: Singleton que monitorea la disponibilidad de internet en tiempo real.
- **`NormalizadorDeTexto.kt`** (32 líneas) — Paso 0 del parseo de notificaciones: une título, texto y datos extra en un solo texto comparable (sin tildes, en minúsculas y con espacios simples).
  - `object NormalizadorDeTexto`: Paso 0 del parseo de notificaciones: une título, texto y datos extra en un solo texto comparable (sin tildes, en minúsculas y con espacios simples).
- **`NotificacionesInternas.kt`** (39 líneas) — Constructores de contenido de notificaciones internas de TOKE (CAPA 13).
  - `object NotificacionesInternas`: Constructores de contenido de notificaciones internas de TOKE (CAPA 13).
  - `class ContenidoNotificacion`: Contenido ya construido de una notificación interna de TOKE.
- **`NotificationParser.kt`** (74 líneas) — Resultado del parsing de una notificación.
  - `class ResultadoParseo`: Resultado del parsing de una notificación.
  - `class NivelConfianzaParseo`: Nivel de confianza de la detección de monto.
  - `object NotificationParser`: Parser/normalizador de eventos de notificación de pago (CAPA 6c & CAPA 9).
- **`OperationHistory.kt`** (31 líneas) — Regla de registro del historial de cambios de una [Operacion].
  - `object OperationHistory`: Regla de registro del historial de cambios de una [Operacion].
- **`OperationNotifier.kt`** (107 líneas) — Orquestador de avisos al usuario (Android, Voz, Vibración) tras una operación exitosa.
  - `object OperationNotifier`: Orquestador de avisos al usuario (Android, Voz, Vibración) tras una operación exitosa.
- **`PagadorExtractor.kt`** (52 líneas) — Saca de la notificación de la billetera el nombre de quien paga, tal como lo muestra la billetera (por ejemplo "Juan P*" o "Ana R.").
  - `object PagadorExtractor`: Saca de la notificación de la billetera el nombre de quien paga, tal como lo muestra la billetera (por ejemplo "Juan P*" o "Ana R.").
- **`PatronesPagoEntrante.kt`** (25 líneas) — Paso final: confirma que el mensaje dice que el usuario RECIBIÓ dinero ("recibiste", "te yapeó", "se acreditó", etc.).
  - `object PatronesPagoEntrante`: Paso final: confirma que el mensaje dice que el usuario RECIBIÓ dinero ("recibiste", "te yapeó", "se acreditó", etc.).
- **`PermisoPublicarNotificaciones.kt`** (20 líneas) — True si la app puede publicar notificaciones (cubre API < 33 y el interruptor de Ajustes).
  - `object PermisoPublicarNotificaciones`
- **`PermisosChecker.kt`** (41 líneas) — Consultas de solo lectura sobre el estado REAL de permisos del sistema Android (no simulado).
  - `object PermisosChecker`: Consultas de solo lectura sobre el estado REAL de permisos del sistema Android (no simulado).
- **`QrImageUploader.kt`** (73 líneas) — Uploader de QR a Storage privado de Supabase.
  - `object QrImageUploader`: Uploader de QR a Storage privado de Supabase.
- **`RegistroAvisos.kt`** (34 líneas) — Registro en memoria para evitar notificaciones duplicadas entre Realtime y FCM Push (CAPA 53).
  - `object RegistroAvisos`: Registro en memoria para evitar notificaciones duplicadas entre Realtime y FCM Push (CAPA 53).
- **`RegistroDispositivos.kt`** (37 líneas) — Interfaz abstracta para obtener y borrar el token push del dispositivo.
  - `interface ProveedorDeToken`: Interfaz abstracta para obtener y borrar el token push del dispositivo.
  - `object RegistroDispositivos`: Lógica pura de registro/des-registro del dispositivo para notificaciones push (CAPA 52).
- **`ReintentoPorReloj.kt`** (28 líneas) — Si el servidor rechaza el token por desfase de reloj (PGRST303 / "JWT issued at future"), espera y reintenta UNA vez (CAPA 31.2).
  - `fun conReintentoPorReloj`: Si el servidor rechaza el token por desfase de reloj (PGRST303 / "JWT issued at future"), espera y reintenta UNA vez (CAPA 31.2).
- **`RequisitosPermisos.kt`** (24 líneas) — Dueño o administrador activo de al menos un negocio: los tres. Solo trabajador: AVISOS y BATERIA.
  - `class Requisito`
  - `object RequisitosPermisos`
- **`RolResolver.kt`** (53 líneas) — Resuelve el rol efectivo de una cuenta a partir de sus relaciones activas.
  - `object RolResolver`: Resuelve el rol efectivo de una cuenta a partir de sus relaciones activas.
  - `class RolEfectivo`: Rol efectivo resuelto de una cuenta en el sistema.
- **`SolicitudesIngreso.kt`** (50 líneas) — Reglas puras para la gestión de solicitudes de ingreso y códigos de invitación (CAPA 43).
  - `object SolicitudesIngreso`: Reglas puras para la gestión de solicitudes de ingreso y códigos de invitación (CAPA 43).
- **`SoundEffectPlayer.kt`** (59 líneas) — Reproduce el efecto de sonido identificado por [resourceId] si está cargado.
  - `fun reproducir`: Reproduce el efecto de sonido identificado por [resourceId] si está cargado.
- **`TabRefreshManager.kt`** (57 líneas) — Gestor centralizado del estado de refresco manual de pestañas (CAPA P1).
  - `class TabRefreshState`
  - `object TabRefreshManager`: Gestor centralizado del estado de refresco manual de pestañas (CAPA P1).
- **`TransicionesRelacion.kt`** (48 líneas) — Detección pura de transiciones de estado en relaciones Cuenta-Negocio (CAPA 50).
  - `object TransicionesRelacion`: Detección pura de transiciones de estado en relaciones Cuenta-Negocio (CAPA 50).
- **`TtsSpeaker.kt`** (102 líneas) — Hablador de voz (CAPA 14): envuelve [TextToSpeech] de Android en una clase simple SIN dependencia de ViewModel, para poder usarla tanto desde la UI (vía [AndroidTtsVoiceEngine]) como desde un [android.app.Service] (p.
  - `class TtsSpeaker`: Hablador de voz (CAPA 14): envuelve [TextToSpeech] de Android en una clase simple SIN dependencia de ViewModel, para poder usarla tanto desde la UI (vía [AndroidTtsVoiceEngine]) como desde un [android.app.Service] (p.
- **`VoicePhraseBuilder.kt`** (51 líneas) — Construye la frase de voz que se anuncia al confirmarse un pago.
  - `object VoicePhraseBuilder`: Construye la frase de voz que se anuncia al confirmarse un pago.

### `com/flabs/cobrate/core/model`

- **`Alerta.kt`** (23 líneas) — Representa una alerta asociada a un negocio y cuenta para el Dueño/Administrador.
  - `class Alerta`: Representa una alerta asociada a un negocio y cuenta para el Dueño/Administrador.
  - `class TipoAlerta`: Tipos de alerta.
- **`ConfiguracionDeVoz.kt`** (52 líneas) — Configuración de aviso por voz de quien usa la app.
  - `class ConfiguracionDeVoz`: Configuración de aviso por voz de quien usa la app.
  - `class ModoVoz`: Modos de voz al confirmarse un pago.
  - `object ModoVozMigracion`: Convierte lo que se guardó en versiones anteriores (había un modo "Toke") al modo actual.
- **`Cuenta.kt`** (23 líneas) — Cuenta única multi-negocio de TOKE V1.
  - `class Cuenta`: Cuenta única multi-negocio de TOKE V1.
- **`EstadoServicios.kt`** (39 líneas) — Estado del control "SERVICIOS" de un negocio (CAPA 5b).
  - `class EstadoServicios`: Estado del control "SERVICIOS" de un negocio (CAPA 5b).
  - `class ProgramacionServicios`: Programación horaria de SERVICIOS.
  - `class DiaSemana`: Días de la semana para la programación de SERVICIOS.
- **`EventoDeNotificacion.kt`** (28 líneas) — Evento de notificación de pago detectado (aviso de Yape/Plin u otro).
  - `class EventoDeNotificacion`: Evento de notificación de pago detectado (aviso de Yape/Plin u otro).
- **`InvitacionTrabajador.kt`** (36 líneas) — Invitación de un trabajador a un negocio.
  - `class InvitacionTrabajador`: Invitación de un trabajador a un negocio.
  - `class EstadoInvitacion`: Estados posibles de una invitación de trabajador.
- **`MetodoDeCobro.kt`** (36 líneas) — Representa un método de cobro del negocio con QR y visibilidad.
  - `class MetodoDeCobro`: Representa un método de cobro del negocio con QR y visibilidad.
  - `class TipoMetodoCobro`: Tipos de método de cobro soportados.
- **`MiembroEquipo.kt`** (13 líneas) — Representa un miembro del equipo de un negocio (retornado por RPC equipo_de_negocio).
  - `class MiembroEquipo`: Representa un miembro del equipo de un negocio (retornado por RPC equipo_de_negocio).
- **`Negocio.kt`** (31 líneas) — Representa un negocio (dueño) de TOKE V1.
  - `class Negocio`: Representa un negocio (dueño) de TOKE V1.
  - `class TipoNegocio`: Rubro/tipo del negocio.
- **`Operacion.kt`** (51 líneas) — Representa una operación de cobro en el sistema.
  - `class Operacion`: Representa una operación de cobro en el sistema.
  - `class EstadoOperacion`: Estados posibles de una operación de cobro.
  - `class OrigenOperacion`: Origen/forma en que se creó la operación.
  - `class CambioDeEstado`: Un cambio de estado en el historial de una operación.
- **`PreviewInvitacion.kt`** (34 líneas) — Vista previa enriquecida de una invitación para el trabajador.
  - `class PreviewInvitacion`: Vista previa enriquecida de una invitación para el trabajador.
  - `fun PreviewInvitacion.toInvitacion`: Convierte el preview en un objeto de dominio de invitación.
- **`RelacionCuentaNegocio.kt`** (44 líneas) — Relación entre una [Cuenta] y un [Negocio].
  - `class RelacionCuentaNegocio`: Relación entre una [Cuenta] y un [Negocio].
  - `class RolCuentaNegocio`: Rol de una cuenta dentro de un negocio.
  - `class EstadoRelacion`: Estado de la relación de una cuenta con un negocio.
- **`ResultadoRpc.kt`** (14 líneas) — Modelo estándar para respuestas JSON de RPCs de Supabase (`{"exito": boolean, "mensaje": string}`).
  - `class ResultadoRpc`: Modelo estándar para respuestas JSON de RPCs de Supabase (`{"exito": boolean, "mensaje": string}`).

### `com/flabs/cobrate/features/acceso`

- **`AccesoFormularios.kt`** (145 líneas) — Formulario de un solo campo (nombre, correo o código) con su botón y un enlace opcional.
  - `fun ColumnScope.FormUnCampo`: Formulario de un solo campo (nombre, correo o código) con su botón y un enlace opcional.
  - `fun ColumnScope.FormPasswords`: Formulario de dos contraseñas (nueva y confirmación) con botón y un enlace opcional.
  - `fun ColumnScope.FormLogin`: Formulario de "Iniciar sesión": correo, contraseña, olvidé mi contraseña y crear cuenta.
  - `fun ColumnScope.FormRegistroDatos`: Paso 1 del registro: nombre de usuario con su aviso, y el correo.
  - `fun ColumnScope.FormRegistroClave`: Paso 2 del registro: el código que llegó al correo y la contraseña, en una sola pantalla.
- **`AccesoLogica.kt`** (30 líneas) — Reglas de validación de los formularios de acceso.
  - `object AccesoLogica`: Reglas de validación de los formularios de acceso.
- **`AccesoPiezas.kt`** (404 líneas) — Fondo de pantalla completa con desplazamiento.
  - `fun AccesoFondo`: Fondo de pantalla completa con desplazamiento.
  - `fun LogoToke`: Logo oficial de TOKE!: el `ic_toke_logo` blanco y transparente sobre una loseta verde redondeada.
  - `fun BotonAtras`
  - `fun TituloAcceso`
  - `fun CampoAcceso`: Campo con la etiqueta arriba.
  - `fun BotonAcceso`: Botón en píldora: [principal] va en verde; si no, blanco con borde.
  - `fun EnlaceAcceso`: Enlace de texto.
  - `fun EspacioAcceso`
  - `fun AvisoAcceso`: Aviso con título y texto corto, para explicar algo antes de que la persona siga.
  - `fun LogoTokeSuave`: Símbolo de TOKE!
- **`AvisoTerminos.kt`** (45 líneas) — Línea corta al pie del inicio de sesión y del registro.
  - `fun AvisoTerminos`: Línea corta al pie del inicio de sesión y del registro.
- **`IconoAccesoAnimado.kt`** (82 líneas) — Icono de una pantalla de acceso: loseta verde con el dibujo en blanco.
  - `fun IconoAccesoAnimado`: Icono de una pantalla de acceso: loseta verde con el dibujo en blanco.
- **`SesionUnicaAviso.kt`** (40 líneas) — Aviso de "TOKE solo funciona en un celular a la vez".
  - `fun SesionUnicaAviso`: Aviso de "TOKE solo funciona en un celular a la vez".
- **`SesionUnicaLogica.kt`** (63 líneas) — Lo que responde el servidor al intentar abrir la sesión en este celular.
  - `class RespuestaSesion`: Lo que responde el servidor al intentar abrir la sesión en este celular.
  - `class AccionSesion`: Qué hace la app con la respuesta.
  - `object SesionUnicaLogica`: Reglas de la sesión única.
- **`TarjetaOpcionAcceso.kt`** (86 líneas) — Tarjeta de una opción de inicio: icono, etiqueta, título, explicación y su contenido.
  - `fun TarjetaOpcionAcceso`: Tarjeta de una opción de inicio: icono, etiqueta, título, explicación y su contenido.
- **`ValidarInvitacion.kt`** (75 líneas) — Valida un código de invitación: lo busca, comprueba que sirva y se asegura de que exista una sesión (anónima si hace falta).
  - `fun validarCodigoInvitacion`: Valida un código de invitación: lo busca, comprueba que sirva y se asegura de que exista una sesión (anónima si hace falta).

### `com/flabs/cobrate/features/actualizacion`

- **`ActualizacionLogica.kt`** (92 líneas) — Metadatos públicos de la versión estable (los entrega la Edge Function check-app-release).
  - `class ManifiestoVersion`: Metadatos públicos de la versión estable (los entrega la Edge Function check-app-release).
  - `interface EstadoActualizacion`
  - `class ProgresoDescarga`
  - `object ActualizacionLogica`: Reglas puras del actualizador (sin Android) para poder probarlas.
- **`ActualizacionPantalla.kt`** (232 líneas) — Pantalla de actualización.
  - `interface FaseDescarga`
  - `fun ActualizacionPantalla`: Pantalla de actualización.
- **`ActualizacionStore.kt`** (100 líneas) — Comprueba al abrir la app si hay una versión nueva.
  - `object ActualizacionStore`: Comprueba al abrir la app si hay una versión nueva.
- **`ApkDescargador.kt`** (139 líneas) — Por qué se rechazó o falló una descarga.
  - `class MotivoFallo`: Por qué se rechazó o falló una descarga.
  - `class FalloDescarga`
  - `class ApkDescargador`: Descarga el APK oficial por HTTPS, lo verifica y lo entrega al instalador de Android.

### `com/flabs/cobrate/features/auth`

- **`ArranqueScreen.kt`** (25 líneas) — Pantalla mientras la app decide si hay sesión: el símbolo de TOKE con su entrada suave.
  - `fun ArranqueScreen`: Pantalla mientras la app decide si hay sesión: el símbolo de TOKE con su entrada suave.
- **`AuthViewModel.kt`** (102 líneas) — ViewModel de autenticación (CAPA Auth, Rate Limiting & Recuperación en Línea).
  - `class AuthViewModel`: ViewModel de autenticación (CAPA Auth, Rate Limiting & Recuperación en Línea).
- **`BienvenidaScreen.kt`** (72 líneas) — Pantalla de bienvenida: se muestra al abrir la app sin sesión o al cerrarla.
  - `fun BienvenidaScreen`: Pantalla de bienvenida: se muestra al abrir la app sin sesión o al cerrarla.
- **`IlustracionBienvenida.kt`** (151 líneas) — Algo que gira alrededor del logo: una etiqueta con icono, un icono suelto o el logo de una billetera.
  - `class Satelite`: Algo que gira alrededor del logo: una etiqueta con icono, un icono suelto o el logo de una billetera.
  - `fun IlustracionBienvenida`: Ilustración de la bienvenida: el logo de TOKE al centro, con sus funciones girando a su alrededor como planetas (voz alta, equipo, QR, Yape, Plin, avisos).
- **`LoginScreen.kt`** (391 líneas) — Pantalla de acceso: iniciar sesión, recuperar la contraseña y crear la cuenta en pasos.
  - `class ModoAuth`
  - `fun LoginScreen`: Pantalla de acceso: iniciar sesión, recuperar la contraseña y crear la cuenta en pasos.
- **`RecuperacionEnCurso.kt`** (18 líneas) — Marca "estoy en recuperar contraseña o terminando de crear mi cuenta".
  - `object RecuperacionEnCurso`: Marca "estoy en recuperar contraseña o terminando de crear mi cuenta".

### `com/flabs/cobrate/features/business`

- **`BillingPlanScreen.kt`** (223 líneas) — "Mi plan": tu plan de hoy, cuánto llevas usado y qué cambia con Pro.
  - `fun BillingPlanScreen`: "Mi plan": tu plan de hoy, cuánto llevas usado y qué cambia con Pro.
- **`BusinessStatsScreen.kt`** (301 líneas) — Reportes del negocio: totales por día, semana, mes o año con los cobros reales, una recomendación para cerrar el día y la exportación a Excel.
  - `fun BusinessStatsScreen`: Reportes del negocio: totales por día, semana, mes o año con los cobros reales, una recomendación para cerrar el día y la exportación a Excel.
- **`BusinessViewModel.kt`** (521 líneas) — Estado de la aplicación para el lado del Dueño.
  - `class BusinessViewModel`: Estado de la aplicación para el lado del Dueño.
  - `class SolicitudUi`: Información de una solicitud de ingreso (CAPA 45).
  - `class TrabajadorRealInfo`: Información de un trabajador real vinculada a una cuenta.
  - `class ResumenDelDia`: Resumen del día expuesto a la UI.
- **`CrearNegocioScreen.kt`** (133 líneas) — Asistente para crear el negocio: nombre y color, rubro, medios de cobro, conexión exitosa y confirmación final.
  - `class PasoWizard`
  - `fun CrearNegocioScreen`: Asistente para crear el negocio: nombre y color, rubro, medios de cobro, conexión exitosa y confirmación final.
- **`InvitarTrabajadorScreen.kt`** (334 líneas) — Tarjeta "Invitar colaborador" del lado del dueño: genera un código, se copia o se comparte, muestra cuánto falta para que venza y permite revocarlo.
  - `fun InvitarTrabajadorScreen`: Tarjeta "Invitar colaborador" del lado del dueño: genera un código, se copia o se comparte, muestra cuánto falta para que venza y permite revocarlo.
- **`NegocioViewModel.kt`** (266 líneas) — ViewModel de Negocios y relaciones, usado tanto por el flujo de Dueño como por el de Trabajador.
  - `class AutoCleanupState`
  - `class NegocioViewModel`: ViewModel de Negocios y relaciones, usado tanto por el flujo de Dueño como por el de Trabajador.
- **`OwnerHistoryScreen.kt`** (29 líneas) — Historial del dueño: pantalla de Operaciones unificada conforme a la Ronda 9 de Figma.
  - `fun OwnerHistoryScreen`: Historial del dueño: pantalla de Operaciones unificada conforme a la Ronda 9 de Figma.
- **`QrEditorScreen.kt`** (355 líneas) — Pantalla completa de edición manual de QR con asistencia del botón AIM.
  - `fun QrEditorScreen`: Pantalla completa de edición manual de QR con asistencia del botón AIM.
- **`QrUploadProgressScreen.kt`** (101 líneas) — Pasos de la subida del QR, en el orden en que ocurren.
  - `class QrUploadStage`: Pasos de la subida del QR, en el orden en que ocurren.
  - `fun QrUploadProgressScreen`: Progreso de la subida de un QR: el paso actual, o el error con "Reintentar" y "Volver".

### `com/flabs/cobrate/features/cobros`

- **`CobrosAcciones.kt`** (41 líneas) — Acciones con efecto de la pantalla de métodos de cobro: copiar el número y compartir la imagen del QR.
  - `object CobrosAcciones`: Acciones con efecto de la pantalla de métodos de cobro: copiar el número y compartir la imagen del QR.
- **`CobrosBotones.kt`** (77 líneas) — Botón grande de la pantalla de cobros.
  - `fun CobrosBoton`: Botón grande de la pantalla de cobros.
- **`CobrosCabecera.kt`** (73 líneas) — Cabecera de Métodos de Cobro: botón atrás (si hay a dónde volver) y título.
  - `fun CobrosCabecera`: Cabecera de Métodos de Cobro: botón atrás (si hay a dónde volver) y título.
- **`CobrosChips.kt`** (117 líneas) — Fila de chips con los métodos de cobro del negocio.
  - `fun CobrosChips`: Fila de chips con los métodos de cobro del negocio.
- **`CobrosDialogos.kt`** (26 líneas) — Confirmación antes de borrar un método de cobro.
  - `fun EliminarMetodoDialogo`: Confirmación antes de borrar un método de cobro.
- **`CobrosEstados.kt`** (117 líneas) — Indicador mientras se cargan los métodos.
  - `fun CobrosCargando`: Indicador mientras se cargan los métodos.
  - `fun CobrosError`: Aviso cuando falló la carga.
  - `fun CobrosVacio`: Tarjeta cuando el negocio todavía no tiene ningún método de cobro.
- **`CobrosQr.kt`** (165 líneas) — Imagen del QR guardado.
  - `fun QrImagen`: Imagen del QR guardado.
  - `fun QrCuadro`: Cuadro del QR dentro de la tarjeta.
  - `fun QrAmpliadoDialogo`: QR a pantalla completa para que el cliente lo escanee.
- **`CobrosTarjeta.kt`** (185 líneas) — Tarjeta del método elegido: nombre, botón Editar, el QR, titular y número con su botón Copiar.
  - `fun CobrosTarjeta`: Tarjeta del método elegido: nombre, botón Editar, el QR, titular y número con su botón Copiar.
- **`EditarMetodoHoja.kt`** (246 líneas) — Ventana "Editar método": misma hoja que "Vincular un método de cobro".
  - `fun EditarMetodoHoja`: Ventana "Editar método": misma hoja que "Vincular un método de cobro".
- **`MetodosCobroLogica.kt`** (59 líneas) — Lógica de la pantalla de métodos de cobro.
  - `object MetodosCobroLogica`: Lógica de la pantalla de métodos de cobro.
- **`MetodosDeCobroScreen.kt`** (243 líneas) — Pantalla "Métodos de Cobro" (Cobrar con QR).
  - `fun MetodosDeCobroScreen`: Pantalla "Métodos de Cobro" (Cobrar con QR).
- **`NombreMetodoUi.kt`** (61 líneas) — Nombre del método de cobro.
  - `fun NombreMetodo`: Nombre del método de cobro.
- **`NuevoMetodoHoja.kt`** (360 líneas) — Ventana "Vincular un método de cobro", en tres pasos pensados para quien no sabe nada de cómo funciona por dentro: 1.
  - `fun NuevoMetodoHoja`: Ventana "Vincular un método de cobro", en tres pasos pensados para quien no sabe nada de cómo funciona por dentro: 1.
  - `fun BotonCuadrado`
- **`NuevoMetodoLogica.kt`** (99 líneas) — Un grupo de opciones con su título ("Billeteras", "Bancos", ...).
  - `class PasoNuevoMetodo`
  - `class GrupoMetodos`: Un grupo de opciones con su título ("Billeteras", "Bancos", ...).
  - `object NuevoMetodoLogica`
- **`NuevoMetodoPiezas.kt`** (243 líneas) — Fila de una billetera o banco.
  - `fun OpcionMetodoFila`: Fila de una billetera o banco.
  - `fun TituloGrupo`: Título de un grupo de opciones ("BANCOS", "BILLETERAS", ...).
  - `fun AvisoPlin`: Aviso previo de Plin: con qué billeteras y bancos trabaja.
  - `fun CampoNuevoMetodo`: Campo de texto con etiqueta clara arriba y una ayuda corta debajo.

### `com/flabs/cobrate/features/crearnegocio`

- **`CrearNegocioLogica.kt`** (48 líneas) — Un color de marca que el dueño puede elegir para su negocio.
  - `class ColorMarca`: Un color de marca que el dueño puede elegir para su negocio.
  - `class OpcionTipoNegocio`: Un rubro que el dueño puede elegir, con el texto que se le muestra.
  - `object CrearNegocioLogica`: Datos y reglas del asistente "Crear negocio".
- **`CrearNegocioPasos.kt`** (392 líneas) — Paso 1: nombre del negocio y color de marca, con una vista previa.
  - `fun colorDe`
  - `fun iconoDeTipo`
  - `fun PasoNombreYColor`: Paso 1: nombre del negocio y color de marca, con una vista previa.
  - `fun PasoTipo`: Paso 2: rubro del negocio.
  - `fun PasoConexion`: Conexión exitosa: el negocio ya existe; se elige a dónde ir primero.
  - `fun PasoFinal`: Confirmación final: resumen del negocio y lo que ya queda activo.
  - `fun PasoMediosCobro`: Paso 3: medios de cobro.
  - `fun SelectorColorMarca`: Fila de colores de marca.

### `com/flabs/cobrate/features/cuenta`

- **`MiCuentaLogica.kt`** (70 líneas) — Tipo real de cuenta: los trabajadores entran con sesión anónima y no tienen correo ni contraseña.
  - `class TipoCuenta`: Tipo real de cuenta: los trabajadores entran con sesión anónima y no tienen correo ni contraseña.
  - `class ErrorCorreo`
  - `object MiCuentaLogica`: Reglas puras de la pantalla Mi cuenta.
  - `interface ResultadoEliminacion`: Resultado de pedir la eliminación al servidor.
- **`MiCuentaScreen.kt`** (334 líneas) — Mi cuenta: nombre, correo y su verificación, cambio de correo y de contraseña, eliminación de la cuenta y enlaces legales.
  - `class Vista`
  - `fun MiCuentaScreen`: Mi cuenta: nombre, correo y su verificación, cambio de correo y de contraseña, eliminación de la cuenta y enlaces legales.

### `com/flabs/cobrate/features/equipo`

- **`EquipoBotonPrincipal.kt`** (69 líneas) — Botón grande al pie: oscuro en el aspecto claro y blanco en el oscuro, como en el diseño.
  - `fun EquipoBotonPrincipal`: Botón grande al pie: oscuro en el aspecto claro y blanco en el oscuro, como en el diseño.
- **`EquipoCabecera.kt`** (63 líneas) — Cabecera de Equipo: título y, debajo, el nombre del negocio activo.
  - `fun EquipoCabecera`: Cabecera de Equipo: título y, debajo, el nombre del negocio activo.
- **`EquipoCodigoFila.kt`** (101 líneas) — Fila de un código de invitación vigente, con el botón para revocarlo.
  - `fun EquipoCodigoFila`: Fila de un código de invitación vigente, con el botón para revocarlo.
- **`EquipoDialogoExpulsar.kt`** (26 líneas) — Confirmación antes de expulsar a un colaborador del negocio.
  - `fun EquipoDialogoExpulsar`: Confirmación antes de expulsar a un colaborador del negocio.
- **`EquipoPestanas.kt`** (105 líneas) — Píldoras "Mi Equipo (N)", "Códigos" y "Solicitudes (M)".
  - `fun EquipoPestanas`: Píldoras "Mi Equipo (N)", "Códigos" y "Solicitudes (M)".
- **`EquipoScreen.kt`** (232 líneas) — Pantalla de Equipo.
  - `fun EquipoScreen`: Pantalla de Equipo.
- **`EquipoSolicitudFila.kt`** (151 líneas) — Fila de una solicitud de ingreso, con los botones Aceptar y Rechazar.
  - `fun EquipoSolicitudFila`: Fila de una solicitud de ingreso, con los botones Aceptar y Rechazar.
- **`EquipoTrabajadorFila.kt`** (225 líneas) — Fila de un colaborador: iniciales, nombre, estado y, si quien mira puede administrar, el botón Pausar/Reanudar y el menú con "Expulsar".
  - `fun EquipoTrabajadorFila`: Fila de un colaborador: iniciales, nombre, estado y, si quien mira puede administrar, el botón Pausar/Reanudar y el menú con "Expulsar".
- **`FiltroEquipo.kt`** (56 líneas) —  Lógica de la pantalla de Equipo. Sin nada de interfaz: decide qué lista se muestra, qué dice el buscador y a qué pestaña se vuelve. 
  - `class PestanaEquipo`
  - `object FiltroEquipo`

### `com/flabs/cobrate/features/nav`

- **`Navegacion.kt`** (31 líneas) — Pestañas de la barra (Inicio, Mis cobros, Mi equipo): siempre parten de Inicio, sin acumular pantallas.
  - `fun NavController.irAPestana`: Pestañas de la barra (Inicio, Mis cobros, Mi equipo): siempre parten de Inicio, sin acumular pantallas.
  - `fun NavController.irADetalle`: Pantallas de detalle (Mis QR, Reportes, Mis locales, Mi plan, Permisos, Aviso en voz alta).

### `com/flabs/cobrate/features/negocios`

- **`EditarNegocioHoja.kt`** (233 líneas) — Hoja para editar un negocio: nombre, máximo de trabajadores y, abajo, borrarlo.
  - `fun EditarNegocioHoja`: Hoja para editar un negocio: nombre, máximo de trabajadores y, abajo, borrarlo.
- **`ListaNegocios.kt`** (87 líneas) —  Lógica de la lista de negocios de una cuenta (pantalla "Mis Negocios" y selector "Cambiar de negocio"). Sin nada de interfaz: decide qué negocios aparecen, con qué rol y en qué estado. 
  - `class RolEnNegocio`
  - `class EstadoEnNegocio`
  - `class NegocioItem`
  - `object ListaNegocios`
- **`MisNegociosCabecera.kt`** (83 líneas) — Cabecera de Mis Negocios: botón atrás, título y subtítulo.
  - `fun MisNegociosCabecera`: Cabecera de Mis Negocios: botón atrás, título y subtítulo.
- **`MisNegociosPie.kt`** (116 líneas) — Los dos botones grandes: uno principal (oscuro en claro, blanco en oscuro) y otro con borde.
  - `fun NegociosBotonesPie`: Los dos botones grandes: uno principal (oscuro en claro, blanco en oscuro) y otro con borde.
  - `fun MisNegociosPie`: Contenedor del pie de Mis Negocios: fondo, línea superior y espacio de la barra del sistema.
- **`MisNegociosScreen.kt`** (210 líneas) — Pantalla "Mis Negocios".
  - `fun MisNegociosScreen`: Pantalla "Mis Negocios".
- **`MisNegociosTarjeta.kt`** (219 líneas) — Tarjeta de un negocio en Mis Negocios.
  - `fun MisNegociosTarjeta`: Tarjeta de un negocio en Mis Negocios.
- **`NegocioEdicionLogica.kt`** (29 líneas) — Reglas para editar un negocio y para el tope de trabajadores.
  - `object NegocioEdicionLogica`: Reglas para editar un negocio y para el tope de trabajadores.
- **`NegocioFilaSelector.kt`** (77 líneas) — Fila de un negocio dentro del selector.
  - `fun NegocioFilaSelector`: Fila de un negocio dentro del selector.
- **`NegocioIconos.kt`** (18 líneas) — Icono del rubro de un negocio.
  - `fun iconoDeNegocio`: Icono del rubro de un negocio.
- **`NegocioPiezas.kt`** (142 líneas) — Cuadro con el icono del negocio.
  - `fun NegocioTile`: Cuadro con el icono del negocio.
  - `fun NegocioEtiqueta`: Etiqueta de rol o de estado: "Dueño", "Colaborador", "Pausado" o "Pendiente de aprobación".
  - `fun NegocioAccion`: Píldora de acción de una tarjeta ("Actual", "Entrar ›", "Ver estado", "Cancelar").
- **`NegociosAcciones.kt`** (68 líneas) — Acciones con efecto de la lista de negocios (hablan con el servidor y avisan al usuario).
  - `object NegociosAcciones`: Acciones con efecto de la lista de negocios (hablan con el servidor y avisan al usuario).
- **`NombreDueno.kt`** (27 líneas) — Nombre con el que se muestra al dueño de un negocio.
  - `fun resolverNombreDueno`: Nombre con el que se muestra al dueño de un negocio.

### `com/flabs/cobrate/features/onboarding`

- **`ConfirmarInvitacionScreen.kt`** (239 líneas) — Pantalla "Negocio encontrado": muestra el negocio y quién invitó antes de aceptar la invitación.
  - `fun ConfirmarInvitacionScreen`: Pantalla "Negocio encontrado": muestra el negocio y quién invitó antes de aceptar la invitación.
- **`EmpezarScreen.kt`** (155 líneas) — Pantalla "Selección de Inicio": quien entra sin negocio elige entre crear el suyo o unirse con el código de invitación que le dio un dueño.
  - `fun EmpezarScreen`: Pantalla "Selección de Inicio": quien entra sin negocio elige entre crear el suyo o unirse con el código de invitación que le dio un dueño.
- **`IngresarCodigoScreen.kt`** (152 líneas) — Pantalla "Soy trabajador": pide el código de invitación.
  - `fun IngresarCodigoScreen`: Pantalla "Soy trabajador": pide el código de invitación.

### `com/flabs/cobrate/features/operaciones`

- **`AsignacionLogica.kt`** (68 líneas) — Los "pagos por asignar" solo existen cuando el negocio tiene 2 o más trabajadores activos.
  - `object AsignacionLogica`: Los "pagos por asignar" solo existen cuando el negocio tiene 2 o más trabajadores activos.
  - `object AsignacionPreferencia`: Interruptor "Asignación de cobros" por negocio.
  - `fun rememberAsignacionHabilitada`: ¿El negocio activo tiene pagos por asignar?
- **`ConstructorDetalleOperacion.kt`** (89 líneas) — Qué icono lleva cada fila de información.
  - `class IconoFila`: Qué icono lleva cada fila de información.
  - `class FilaDetalle`
  - `class DetalleOperacion`
  - `object ConstructorDetalleOperacion`
- **`DetalleOperacionCabecera.kt`** (68 líneas) — Cabecera del detalle: botón atrás y título centrado.
  - `fun DetalleOperacionCabecera`: Cabecera del detalle: botón atrás y título centrado.
- **`DetalleOperacionInfo.kt`** (144 líneas) — Tarjeta "Información de la operación".
  - `fun DetalleOperacionInfo`: Tarjeta "Información de la operación".
- **`DetalleOperacionMetodo.kt`** (69 líneas) — Tarjeta con el logo y el nombre del método de pago.
  - `fun DetalleOperacionMetodo`: Tarjeta con el logo y el nombre del método de pago.
- **`DetalleOperacionResumen.kt`** (81 líneas) — Monto protagonista y la píldora de estado ("Recibido" en verde o "Sin asignar" en ámbar).
  - `fun DetalleOperacionResumen`: Monto protagonista y la píldora de estado ("Recibido" en verde o "Sin asignar" en ámbar).
- **`DetalleOperacionScreen.kt`** (155 líneas) — Detalle de una operación.
  - `fun DetalleOperacionScreen`: Detalle de una operación.
- **`FiltroOperaciones.kt`** (88 líneas) — Rango de tiempo.
  - `class PestanaOperaciones`
  - `class RangoTiempo`: Rango de tiempo.
  - `class FiltroBilletera`: Grupos de billetera que se pueden filtrar.
  - `class FiltrosOperaciones`
  - `object FiltroOperaciones`
- **`OperacionFilas.kt`** (205 líneas) — Fila de una operación por asignar: logo, referencia, monto y botón "Asignar Cobro".
  - `fun OperacionPendienteFila`: Fila de una operación por asignar: logo, referencia, monto y botón "Asignar Cobro".
  - `fun OperacionRecibidaFila`: Fila de un cobro recibido: logo, referencia, hora, monto y flecha de detalle.
- **`OperacionesCabecera.kt`** (95 líneas) — Cabecera de Operaciones: botón atrás (solo si hay a dónde volver), título centrado y botón de filtros a la derecha.
  - `fun OperacionesCabecera`: Cabecera de Operaciones: botón atrás (solo si hay a dónde volver), título centrado y botón de filtros a la derecha.
- **`OperacionesChips.kt`** (185 líneas) — Fila de chips bajo el buscador: selector de rango de tiempo (con menú) y los filtros de billetera (Yape, Plin, BCP / Bancos).
  - `fun OperacionesChips`: Fila de chips bajo el buscador: selector de rango de tiempo (con menú) y los filtros de billetera (Yape, Plin, BCP / Bancos).
- **`OperacionesPestanas.kt`** (113 líneas) — Píldoras "Todas (N)" y "Pendientes (M)".
  - `fun OperacionesPestanas`: Píldoras "Todas (N)" y "Pendientes (M)".
- **`OperacionesScreen.kt`** (210 líneas) — Pantalla de Operaciones.
  - `fun OperacionesScreen`: Pantalla de Operaciones.
- **`OperacionesVacio.kt`** (98 líneas) — Tarjeta de lista vacía.
  - `fun OperacionesVacio`: Tarjeta de lista vacía.
- **`TextosCobro.kt`** (18 líneas) — Textos de un cobro cuando la billetera no entrega datos.
  - `object TextosCobro`: Textos de un cobro cuando la billetera no entrega datos.

### `com/flabs/cobrate/features/permisos`

- **`DiagnosticoPermisosScreen.kt`** (231 líneas) — Pantalla "Permisos y Servicio".
  - `fun DiagnosticoPermisosScreen`: Pantalla "Permisos y Servicio".
- **`EstadoServicio.kt`** (38 líneas) — Lógica de la pantalla "Permisos y Servicio".
  - `class EstadoServicio`: Lógica de la pantalla "Permisos y Servicio".
  - `object EvaluadorServicio`
- **`EstadoServicioActual.kt`** (72 líneas) — Lo que el sistema tiene concedido en este momento.
  - `class PermisosActuales`: Lo que el sistema tiene concedido en este momento.
  - `fun rememberPermisosActuales`: Lee los permisos reales del sistema y los vuelve a leer cada vez que la app regresa a primer plano (por ejemplo, tras concederlos en Ajustes).
  - `fun rememberEstadoServicio`: Estado del servicio de TOKE para la cuenta actual, según el rol (qué permisos le exigen) y lo que el sistema tiene concedido.
- **`PermisoFila.kt`** (231 líneas) — Un permiso de la lista: lo que es, para qué sirve y su estado ("Listo" o el botón "Activar").
  - `class PermisoUi`: Un permiso de la lista: lo que es, para qué sirve y su estado ("Listo" o el botón "Activar").
  - `fun PermisosLista`: Tarjeta con la lista de permisos, separados por una línea.
  - `fun PermisosAviso`: Nota de privacidad al pie.
- **`PermisosAjustes.kt`** (22 líneas) — Abre las pantallas de Ajustes del sistema donde el usuario concede cada permiso.
  - `object PermisosAjustes`: Abre las pantallas de Ajustes del sistema donde el usuario concede cada permiso.
- **`PermisosBanner.kt`** (141 líneas) — Tarjeta grande de estado del servicio: verde (operativo), ámbar (parcial) o roja (desactivado).
  - `fun PermisosBanner`: Tarjeta grande de estado del servicio: verde (operativo), ámbar (parcial) o roja (desactivado).
  - `class DatosBanner`

### `com/flabs/cobrate/features/planes`

- **`CuotaBarra.kt`** (58 líneas) — Color de la cuota: texto normal (negro en claro, blanco en oscuro), amarillo, rojo.
  - `fun colorDeCuota`: Color de la cuota: texto normal (negro en claro, blanco en oscuro), amarillo, rojo.
  - `fun CuotaMensaje`: Mensaje "50 notificaciones restantes" que va arriba del monto.
  - `fun CuotaBarra`: Barrita que va debajo del monto: se vacía y cambia de color a medida que baja la cuota.
- **`PlanComparacion.kt`** (100 líneas) — Una fila de la comparación: qué ofrece cada plan.
  - `class FilaPlan`: Una fila de la comparación: qué ofrece cada plan.
  - `object PlanComparacion`: Lo que cambia entre Free y Pro.
  - `fun TablaComparacionPlanes`: Tabla Free vs Pro.
- **`PlanLogica.kt`** (135 líneas) — Lo que el servidor dice del plan de un negocio (el plan lo manda la cuenta dueña).
  - `class Plan`
  - `class EstadoPlanDto`: Lo que el servidor dice del plan de un negocio (el plan lo manda la cuenta dueña).
  - `class EstadoPlan`
  - `class NivelCuota`: Cómo se ve la cuota: de más tranquila a agotada.
  - `object PlanLogica`: Reglas de los planes Free y Pro.
  - `class CuotaUi`: Cuota ya calculada para pintarla en el Home (solo el plan Free tiene cuota).
  - `fun cuotaDeHoy`
- **`PlanStore.kt`** (88 líneas) — Plan del negocio activo, tal como lo dice el servidor.
  - `object PlanStore`: Plan del negocio activo, tal como lo dice el servidor.
- **`PlanesDialogos.kt`** (78 líneas) — Cómo activar el plan Pro: llamar al número de contacto.
  - `fun ActivarProDialogo`: Cómo activar el plan Pro: llamar al número de contacto.
  - `fun LimitePlanDialogo`: Aviso cuando el plan Free no permite lo que se intentó (otro negocio, más colaboradores).
  - `fun CuotaAgotadaDialogo`: Aviso cuando se acaba la cuota diaria del plan Free.
- **`ProActivadoPantalla.kt`** (129 líneas) — Pantalla de éxito: se muestra una vez, cuando el plan pasa de Free a Pro.
  - `fun ProActivadoPantalla`: Pantalla de éxito: se muestra una vez, cuando el plan pasa de Free a Pro.

### `com/flabs/cobrate/features/reportes`

- **`ReportesAcciones.kt`** (47 líneas) — Genera el Excel de un periodo y lo comparte con la app que elija la persona.
  - `object ReportesAcciones`: Genera el Excel de un periodo y lo comparte con la app que elija la persona.
- **`ReportesLogica.kt`** (117 líneas) — Periodos que el dueño puede ver y exportar.
  - `class PeriodoReporte`: Periodos que el dueño puede ver y exportar.
  - `class ResumenPeriodo`: Resumen de un periodo.
  - `object ReportesLogica`: Reglas de los reportes: qué cobros entran en un periodo y cómo se arma la tabla a exportar.
- **`XlsxWriter.kt`** (82 líneas) — Una celda de la hoja.
  - `class Celda`: Una celda de la hoja.
  - `object XlsxWriter`: Escribe un .xlsx de una sola hoja ("Cobros") sin librerías: un zip con las partes mínimas de Office Open XML.

### `com/flabs/cobrate/features/settings`

- **`VoiceSettingsScreen.kt`** (253 líneas) — Aviso de voz: se activa o se apaga, se elige qué tanto dice (Cauteloso o Básico), la velocidad, y se puede probar con una frase de ejemplo.
  - `fun VoiceSettingsScreen`: Aviso de voz: se activa o se apaga, se elige qué tanto dice (Cauteloso o Básico), la velocidad, y se puede probar con una frase de ejemplo.

### `com/flabs/cobrate/features/shared`

- **`HomeScreen.kt`** (1452 líneas) — (sin comentario en el código)
  - `class HomePalette`
  - `fun HomeScreen`
- **`PanelTrabajador.kt`** (253 líneas) — Botón de tres puntos de la cabecera del trabajador: abre el menú Más.
  - `fun BotonMasOpciones`: Botón de tres puntos de la cabecera del trabajador: abre el menú Más.
  - `fun UltimoCobroCard`: "Último cobro recibido": monto grande y tres píldoras (billetera, hace cuánto, quién).
  - `fun FichasTrabajador`: Dos fichas iguales: Mis QRs y, a la derecha, pagos por reclamar o, si no hay, Mis Negocios.

### `com/flabs/cobrate/features/soporte`

- **`AcercaDeLogica.kt`** (17 líneas) — Textos de "Acerca de y soporte" que se arman con datos: se pueden probar sin pantalla.
  - `object AcercaDeLogica`: Textos de "Acerca de y soporte" que se arman con datos: se pueden probar sin pantalla.
- **`AcercaDeScreen.kt`** (183 líneas) — "Acerca de y soporte": versión de la app, ayuda por WhatsApp y documentos legales.
  - `fun AcercaDeScreen`: "Acerca de y soporte": versión de la app, ayuda por WhatsApp y documentos legales.
- **`ManualContenido.kt`** (296 líneas) — Pieza de una pantalla de ejemplo del manual.
  - `class ElementoMock`: Pieza de una pantalla de ejemplo del manual.
  - `class MockPantalla`: Una pantalla de ejemplo: [resaltado] es la posición del elemento que hay que tocar.
  - `class PasoManual`
  - `class ArticuloManual`
  - `class PreguntaFrecuente`
  - `object ManualContenido`: Contenido del Manual y de las Preguntas frecuentes.
- **`ManualScreen.kt`** (267 líneas) — Manual: la lista de guías y, al tocar una, sus pasos con una pantalla de ejemplo que se anima.
  - `fun ManualScreen`: Manual: la lista de guías y, al tocar una, sus pasos con una pantalla de ejemplo que se anima.
  - `fun PreguntasFrecuentesScreen`: Preguntas frecuentes: cada una se abre y se cierra al tocarla.

### `com/flabs/cobrate/features/worker`

- **`EncuentraMiPedidoScreen.kt`** (118 líneas) — "Encuentra mi pedido": el trabajador escribe el monto que acaba de recibir y se listan los cobros sin asignar con ese monto (tolerancia de ±S/0.50).
  - `fun EncuentraMiPedidoScreen`: "Encuentra mi pedido": el trabajador escribe el monto que acaba de recibir y se listan los cobros sin asignar con ese monto (tolerancia de ±S/0.50).
- **`PanelTrabajadorLogica.kt`** (26 líneas) — Textos del panel del trabajador.
  - `object PanelTrabajadorLogica`: Textos del panel del trabajador.
- **`ResumenHoy.kt`** (48 líneas) — Resúmenes de "hoy" para el Home.
  - `object ResumenHoy`: Resúmenes de "hoy" para el Home.
- **`WorkerOperationsScreen.kt`** (38 líneas) — Pantalla de Operaciones para Trabajador conforme a Ronda 9.
  - `fun WorkerOperationsScreen`: Pantalla de Operaciones para Trabajador conforme a Ronda 9.
- **`WorkerViewModel.kt`** (198 líneas) — ViewModel que gestiona las acciones de consulta, reclamo y confirmación del trabajador sobre operaciones existentes.
  - `class WorkerViewModel`: ViewModel que gestiona las acciones de consulta, reclamo y confirmación del trabajador sobre operaciones existentes.

### `com/flabs/cobrate/services`

- **`FirebaseProveedorDeToken.kt`** (40 líneas) — Implementación de [ProveedorDeToken] basada en Firebase Messaging (CAPA 52).
  - `class FirebaseProveedorDeToken`: Implementación de [ProveedorDeToken] basada en Firebase Messaging (CAPA 52).
- **`ServiciosToke.kt`** (143 líneas) — Fachada única para controlar el estado encendido/apagado global de TOKE (CAPA 57).
  - `object ServiciosToke`: Fachada única para controlar el estado encendido/apagado global de TOKE (CAPA 57).
- **`TokeActionReceiver.kt`** (67 líneas) — Receptor de acciones desde las notificaciones de TOKE (C11).
  - `class TokeActionReceiver`: Receptor de acciones desde las notificaciones de TOKE (C11).
- **`TokeMessagingService.kt`** (156 líneas) — Servicio de mensajes push de Firebase para TOKE (CAPA 51, 52 y 53).
  - `class TokeMessagingService`: Servicio de mensajes push de Firebase para TOKE (CAPA 51, 52 y 53).
- **`TokeNotificationListenerService.kt`** (434 líneas) — NotificationListenerService de TOKE (CAPA 6b).
  - `class TokeNotificationListenerService`: NotificationListenerService de TOKE (CAPA 6b).
- **`TokeOperandoService.kt`** (122 líneas) — Servicio en primer plano que sostiene la notificación permanente "TOKE está operando" (CAPA 58).
  - `class TokeOperandoService`: Servicio en primer plano que sostiene la notificación permanente "TOKE está operando" (CAPA 58).

### `com/flabs/cobrate/ui/theme`

- **`EfectoPulsacion.kt`** (39 líneas) — Fuente de toques y modificador que hunde un poco el botón mientras se pulsa.
  - `class Pulsacion`: Fuente de toques y modificador que hunde un poco el botón mientras se pulsa.
  - `fun rememberPulsacion`: Efecto de "hundirse" al tocar, como en Waze: el botón se encoge y se apaga un poco al pulsar (70 ms) y vuelve con un pequeño rebote al soltar.
  - `val ESCALA_HUNDIDO`: Cuánto se encoge un botón al pulsarlo (94 %).
- **`Theme.kt`** (35 líneas) — Modos de tema soportados por TOKE!
  - `class ThemeMode`: Modos de tema soportados por TOKE!
  - `val Shapes`
  - `fun CobrateTheme`
- **`TokeMaterial.kt`** (65 líneas) — Esquema de Material armado solo con la paleta de TOKE: así los componentes de Material y las pantallas nuevas se ven igual.
  - `fun esquemaToke`: Esquema de Material armado solo con la paleta de TOKE: así los componentes de Material y las pantallas nuevas se ven igual.
  - `fun TokeMaterial`: Envuelve una pantalla con el esquema de [esquemaToke] (el tema general ya lo usa; se conserva por compatibilidad).
  - `fun NavGraphBuilder.composableToke`: Igual que `composable`, pero con la pantalla envuelta en [TokeMaterial].
- **`TokeMedidas.kt`** (27 líneas) — Medidas únicas de la app.
  - `object TokeMedidas`: Medidas únicas de la app.
- **`TokePaleta.kt`** (120 líneas) — Paleta única de TOKE.
  - `class TokePaleta`: Paleta única de TOKE.
  - `val TokeRojo`: Rojo de error y de acciones que borran; el mismo en claro y oscuro.
  - `val TokeAmbarTexto`: Ámbar oscuro para texto sobre fondos claros (avisos, iconos resaltados).
  - `val TokePaletaClara`
  - `val TokePaletaOscura`
  - `fun tokePaleta`: Paleta vigente según el aspecto elegido en la app.
  - `fun tokeEsOscuro`: ¿El aspecto elegido en la app es el oscuro?
  - `fun Color.solidoSobre`: Mezcla este color sobre [base] y devuelve un color SÓLIDO (sin transparencia): los fondos y bordes suaves no deben dejar ver lo de atrás.
- **`Type.kt`** (132 líneas) — SISTEMA TIPOGRÁFICO TOKE!
  - `val PlusJakartaSans`
  - `val Typography`: SISTEMA TIPOGRÁFICO TOKE!

## Pruebas unitarias (`app/src/test`) — 51 archivos

### `com/flabs/cobrate`

- **`AccesoNegocioTest.kt`** (87 líneas) — (sin comentario en el código)
  - `class AccesoNegocioTest`
- **`ActualizacionLogicaTest.kt`** (84 líneas) — (sin comentario en el código)
  - `class ActualizacionLogicaTest`
- **`AlertaCapa1Test.kt`** (135 líneas) — (sin comentario en el código)
  - `class AlertaCapa1Test`
- **`AsignacionYAvisoTest.kt`** (29 líneas) — (sin comentario en el código)
  - `class AsignacionYAvisoTest`
- **`ConstructorDetalleOperacionTest.kt`** (99 líneas) — (sin comentario en el código)
  - `class ConstructorDetalleOperacionTest`
- **`DuracionAvisoTest.kt`** (18 líneas) — (sin comentario en el código)
  - `class DuracionAvisoTest`
- **`EntradaSeguraTest.kt`** (57 líneas) — (sin comentario en el código)
  - `class EntradaSeguraTest`
- **`ErrorMapperAmableTest.kt`** (52 líneas) — (sin comentario en el código)
  - `class ErrorMapperAmableTest`
- **`EstadoOperativoTest.kt`** (37 líneas) — Combinación 1: encendido = true, haySesion = true, permisoNotificaciones = true -> TRUE
  - `class EstadoOperativoTest`
- **`EvaluadorServicioTest.kt`** (64 líneas) — (sin comentario en el código)
  - `class EvaluadorServicioTest`
- **`ExampleUnitTest.kt`** (17 líneas) — Example local unit test, which will execute on the development machine (host).
  - `class ExampleUnitTest`: Example local unit test, which will execute on the development machine (host).
- **`FechaCobroTest.kt`** (28 líneas) — (sin comentario en el código)
  - `class FechaCobroTest`
- **`FiltroEquipoTest.kt`** (86 líneas) — (sin comentario en el código)
  - `class FiltroEquipoTest`
- **`FiltroOperacionesTest.kt`** (133 líneas) — (sin comentario en el código)
  - `class FiltroOperacionesTest`
  - `class FiltroOperacionesHoyTest`
- **`InvitacionCapa1Test.kt`** (71 líneas) — (sin comentario en el código)
  - `class InvitacionCapa1Test`
- **`ListaNegociosTest.kt`** (131 líneas) — (sin comentario en el código)
  - `class ListaNegociosTest`
- **`LogSeguroTest.kt`** (98 líneas) — (sin comentario en el código)
  - `class LogSeguroTest`
- **`ManualContenidoTest.kt`** (38 líneas) — (sin comentario en el código)
  - `class ManualContenidoTest`
- **`MatrizBilleterasTest.kt`** (95 líneas) — TEXTOS REPRESENTATIVOS, no capturados de las apps: validar con pagos reales de cada billetera.
  - `class MatrizBilleterasTest`: TEXTOS REPRESENTATIVOS, no capturados de las apps: validar con pagos reales de cada billetera.
- **`MetodosCobroLogicaTest.kt`** (59 líneas) — (sin comentario en el código)
  - `class MetodosCobroLogicaTest`
- **`MiCuentaLogicaTest.kt`** (55 líneas) — (sin comentario en el código)
  - `class MiCuentaLogicaTest`
- **`NegocioEdicionLogicaTest.kt`** (34 líneas) — (sin comentario en el código)
  - `class NegocioEdicionLogicaTest`
- **`NombreCuentaTest.kt`** (22 líneas) — (sin comentario en el código)
  - `class NombreCuentaTest`
- **`NotificacionOperandoTest.kt`** (81 líneas) — (sin comentario en el código)
  - `class NotificacionOperandoTest`
- **`NotificationParserEquivalenciaTest.kt`** (98 líneas) — Compara el parser actual con la copia congelada del original ([NotificationParserLegacy]) sobre decenas de miles de mensajes generados.
  - `class NotificationParserEquivalenciaTest`: Compara el parser actual con la copia congelada del original ([NotificationParserLegacy]) sobre decenas de miles de mensajes generados.
- **`NotificationParserFiltroTest.kt`** (99 líneas) — TEXTOS SINTÉTICOS: validar contra notificaciones reales del dispositivo
  - `class NotificationParserFiltroTest`: TEXTOS SINTÉTICOS: validar contra notificaciones reales del dispositivo
- **`NotificationParserLegacy.kt`** (141 líneas) — Copia congelada del parser ANTES de separarlo por responsabilidades.
  - `object NotificationParserLegacy`: Copia congelada del parser ANTES de separarlo por responsabilidades.
- **`NuevoMetodoLogicaTest.kt`** (100 líneas) — (sin comentario en el código)
  - `class NuevoMetodoLogicaTest`
- **`ObservabilidadEnvioTest.kt`** (132 líneas) — (sin comentario en el código)
  - `class ObservabilidadEnvioTest`
- **`OperacionPendienteRehidratacionTest.kt`** (112 líneas) — (sin comentario en el código)
  - `class OperacionPendienteRehidratacionTest`
- **`PagadorExtractorTest.kt`** (52 líneas) — (sin comentario en el código)
  - `class PagadorExtractorTest`
- **`PanelTrabajadorLogicaTest.kt`** (27 líneas) — (sin comentario en el código)
  - `class PanelTrabajadorLogicaTest`
- **`PlanLogicaTest.kt`** (98 líneas) — (sin comentario en el código)
  - `class PlanLogicaTest`
- **`PrivacidadCodigoTest.kt`** (46 líneas) — Guardia de privacidad (PRI-6 y SEG-3), revisa el código fuente: - Nadie llama a `android.util.Log` directamente: todo pasa por `LogSeguro` (que apaga `d` en la versión pública y no imprime el detalle de las excepciones).
  - `class PrivacidadCodigoTest`: Guardia de privacidad (PRI-6 y SEG-3), revisa el código fuente: - Nadie llama a `android.util.Log` directamente: todo pasa por `LogSeguro` (que apaga `d` en la versión pública y no imprime el detalle de las excepciones).
- **`RegistroAvisosTest.kt`** (39 líneas) — (sin comentario en el código)
  - `class RegistroAvisosTest`
- **`ReportesTest.kt`** (91 líneas) — (sin comentario en el código)
  - `class ReportesTest`
- **`RequisitosPermisosTest.kt`** (89 líneas) — (sin comentario en el código)
  - `class RequisitosPermisosTest`
- **`ResumenHoyTest.kt`** (47 líneas) — (sin comentario en el código)
  - `class ResumenHoyTest`
- **`SesionUnicaLogicaTest.kt`** (51 líneas) — (sin comentario en el código)
  - `class SesionUnicaLogicaTest`
- **`SolicitudesIngresoTest.kt`** (74 líneas) — (sin comentario en el código)
  - `class SolicitudesIngresoTest`
- **`TabRefreshAndInvitationTest.kt`** (63 líneas) — (sin comentario en el código)
  - `class TabRefreshAndInvitationTest`
- **`TextoNoSeCortaTest.kt`** (42 líneas) — Guardia de regresión: un texto largo con `maxLines = 1` se corta con "…" ("Estás seg…").
  - `class TextoNoSeCortaTest`: Guardia de regresión: un texto largo con `maxLines = 1` se corta con "…" ("Estás seg…").
- **`TransicionesRelacionTest.kt`** (119 líneas) — (sin comentario en el código)
  - `class TransicionesRelacionTest`
- **`VozTest.kt`** (46 líneas) — (sin comentario en el código)
  - `class VozTest`
- **`WorkerMirrorTest.kt`** (137 líneas) — (sin comentario en el código)
  - `class WorkerMirrorTest`

### `com/flabs/cobrate/core/data`

- **`MetodoDeCobroSerializationTest.kt`** (70 líneas) — (sin comentario en el código)
  - `class MetodoDeCobroSerializationTest`
- **`SupabaseAuthBridgeTest.kt`** (61 líneas) — (sin comentario en el código)
  - `class SupabaseAuthBridgeTest`

### `com/flabs/cobrate/features/acceso`

- **`AccesoLogicaTest.kt`** (32 líneas) — (sin comentario en el código)
  - `class AccesoLogicaTest`

### `com/flabs/cobrate/features/crearnegocio`

- **`CrearNegocioLogicaTest.kt`** (36 líneas) — (sin comentario en el código)
  - `class CrearNegocioLogicaTest`

### `com/flabs/cobrate/ui/theme`

- **`MedidasBotonesTest.kt`** (22 líneas) — Las alturas de botones salen de TokeMedidas; no se fijan números sueltos de 46 a 56 dp.
  - `class MedidasBotonesTest`: Las alturas de botones salen de TokeMedidas; no se fijan números sueltos de 46 a 56 dp.
- **`PaletaUnicaTest.kt`** (22 líneas) — Los colores salen solo de TokePaleta.kt; los de cada billetera viven en BilleteraColores.kt.
  - `class PaletaUnicaTest`: Los colores salen solo de TokePaleta.kt; los de cada billetera viven en BilleteraColores.kt.

## Pruebas instrumentadas (`app/src/androidTest`) — 1 archivos

### `com/flabs/cobrate`

- **`ExampleInstrumentedTest.kt`** (24 líneas) — Instrumented test, which will execute on an Android device.
  - `class ExampleInstrumentedTest`: Instrumented test, which will execute on an Android device.
