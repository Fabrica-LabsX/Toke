# Qué billeteras y bancos van dentro de Plin

Verificado el 2026-11-06 con fuentes públicas. Define `NuevoMetodoLogica` (`features/cobros`).

## Criterio
"Dentro de Plin" = entidades que forman parte de la red Plin, es decir, que ofrecen Plin a sus clientes.
La interoperabilidad entre billeteras (Yape ↔ Plin, pagos con QR único) es otra cosa: permite enviar de
una billetera a otra, pero no convierte a Yape ni a BCP en "una billetera de Plin".

## Dentro de Plin (se llega eligiendo Plin)
| Entidad en TOKE | Motivo | Fuente |
|---|---|---|
| Interbank | Administrador del acuerdo de pago Plin (Circular BCRP 0024-2022) | Infobae / Gestión |
| BBVA | Administrador del acuerdo de pago Plin | Infobae / Gestión |
| Scotiabank | Administrador del acuerdo de pago Plin | Infobae / Gestión |
| BanBif | Participante de la red Plin (2024) | Infobae 2024-01-19 |
| Agora/Tunki | Billetera de Interbank; cobra con el QR de Plin y figura en la red Plin | Trome, InfoMercado |

## Fuera de Plin (siguen en la primera pantalla)
| Entidad | Motivo |
|---|---|
| Yape | Billetera del BCP; es interoperable con Plin pero es otro servicio |
| BCP | Opera Yape, no Plin |
| Pichincha | Sin confirmación de que ofrezca Plin (aparece en transferencias por celular en general) |
| BIM | Interopera con Yape y Plin vía Niubiz (2023-2024), pero no es parte de la red Plin |
| Ligo, Sip, Lemon Cash | Sin evidencia de Plin |
| Transferencia | No es una entidad |

## Para revisar
- Las fuentes son de 2022 a 2024; ninguna es un listado oficial vigente del BCRP.
- BIM es el caso más discutible: si se prefiere contar la interoperabilidad con Plin, se agrega a `bancosPlin` o
  `billeterasPlin` en `NuevoMetodoLogica`.

## Fuentes
- https://www.infobae.com/peru/2024/01/19/plin-2024-servicios-transferencias-y-funcionalidades-de-este-aplicativo-movil-durante-este-ano/
- https://gestion.pe/economia/empresas/billeteras-electronicas-usuarios-ahora-podran-hacer-transferencias-entre-yape-y-plin-bcp-bbva-interbank-scotiabank-rmmn-noticia/
- https://elcomercio.pe/respuestas/que/que-bancos-trabajan-con-plin-tdpe-noticia/
- https://trome.com/emprende-trome/tips-para-emprendedores/emprendedor-tunki-ahora-permite-a-los-pequenos-negocios-cobrar-con-el-qr-de-plin-noticia/
- https://infomercado.pe/billetera-movil-transferencia-de-dinero-entre-yape-plin-tunki-lukita-y-bim-sera-posible-a-fin-de-ano/
- https://forbes.pe/negocios/2023-10-11/niubiz-sobre-pagos-de-bim-a-plin-y-yape-las-transacciones-estaran-desplegadas-integramente-en-noviembre-proximo/
- https://interoperabilidad.pe/
