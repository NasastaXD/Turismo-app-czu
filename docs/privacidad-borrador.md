# Política de privacidad — borrador

Armado a partir de una auditoría del código de la app (1.6.0) y del plugin
`caaguazu-app-api` del panel, no de suposiciones. Sirve para las dos tiendas.

Antes de publicarlo hace falta:

1. **Completar lo que está entre `‹ángulos›`**: quién es el responsable, un correo
   de contacto, qué proveedor de IA está cargado hoy en el panel y la fecha.
2. **Que lo lea alguien con criterio legal.** Esto describe con exactitud qué
   hace la app; si alcanza para la ley paraguaya de datos personales es otra
   pregunta, y no me corresponde contestarla.
3. **Publicarlo en una URL pública.** La versión lista para subir, en castellano
   y en inglés, está en [`app-store/sitio/privacidad.html`](app-store/sitio/privacidad.html).
   Si se cambia una, se cambia la otra.

> **Qué cambió respecto del borrador anterior:** el asistente. Antes la app no
> mandaba nada escrito por la persona a ningún lado; desde la 1.6.0, lo que se
> escribe en el asistente viaja al servidor y a un proveedor de IA externo. Eso
> obliga a decirlo acá, en la ficha de las dos tiendas y en el manifiesto de
> privacidad de iOS.

---

## Qué es esta app

**Caaguazú Go** es una guía turística del distrito de Caaguazú (Paraguay). Muestra lugares,
eventos, artículos y recorridos publicados en caaguazu.net, un mapa del
distrito y un asistente que responde preguntas sobre ese contenido. No tiene
cuentas de usuario ni inicio de sesión.

## Lo que la app no hace

- No pide nombre, correo, teléfono ni ningún dato para usarla.
- No tiene analítica ni publicidad, y no usa identificadores de publicidad.
- No sigue a nadie entre apps o sitios.
- No pide la ubicación: el mapa se dibuja sin saber dónde está quien lo mira.
- No vende ni comparte datos con fines comerciales.

## Lo que se guarda en el teléfono

Todo esto queda en el teléfono y no se sube a ningún servidor:

| Qué | Para qué |
|---|---|
| El idioma elegido | Abrir siempre en ese idioma |
| Favoritos y el recorrido que uno arma | Que no se pierdan al cerrar la app |
| Si los avisos están encendidos, y qué artículos y eventos ya se avisaron (solo Android) | No avisar dos veces lo mismo |
| Una copia del contenido y de las fotos | Que la app funcione sin señal |
| Un registro técnico de funcionamiento | Diagnosticar fallos. Solo sale del teléfono si la persona lo comparte a mano desde la pantalla de diagnóstico |

Se borra todo al desinstalar la app.

## Lo que se pide por internet

La app se conecta solo por HTTPS a `caaguazu.net` para traer el contenido
público y sus fotos. Esos pedidos llevan el idioma, para recibir los textos en
ese idioma, y nada que identifique a la persona. Como cualquier sitio web, el
servidor puede registrar datos técnicos de la conexión —como la dirección IP—
en sus registros habituales.

## El asistente

El asistente es opcional, y antes de la primera pregunta la app avisa lo que
sigue y pide aceptarlo. Si se usa:

- **Lo que se escribe se manda a caaguazu.net**, junto con el idioma y un
  identificador de conversación al azar que crea el servidor. No va ningún dato
  de la persona ni del teléfono.
- **El servidor guarda la conversación hasta 2 horas** (es un ajuste del panel,
  que puede ir de 1 a 24 horas), para poder seguir la charla. Pasado ese tiempo
  se borra sola. La conversación no está asociada a ninguna cuenta, porque no
  hay cuentas.
- **Para responder, el servidor manda la pregunta y los últimos mensajes de esa
  conversación a un proveedor de inteligencia artificial externo**
  (‹nombre del proveedor que esté cargado en el panel›), junto con el contenido
  publicado en la app. Ese proveedor procesa el texto según sus propias
  condiciones.
- Para evitar abusos, el servidor cuenta cuántas preguntas llegan desde una
  misma dirección IP, guardada en forma cifrada (hash), durante un día como
  máximo.

**Por eso conviene no escribir en el asistente datos personales** —nombre
completo, documento, teléfono, salud— que no hagan falta para la pregunta.

## Otras apps del teléfono

Algunas funciones abren otra app, que tiene su propia política de privacidad:

- **Cómo llegar** abre Google Maps —su app o su sitio web— o la app de mapas del teléfono, con el destino cargado.
- **Agendar un evento** abre el calendario (Android) o la hoja de compartir con
  el evento (iPhone). La persona confirma ahí; la app no lee ni escribe la
  agenda.
- **Compartir** abre la hoja de compartir del sistema.

## Permisos

- **iPhone:** ninguno.
- **Android:** acceso a internet y al estado de la red, para traer el contenido;
  y, desde Android 13, notificaciones, para avisar de artículos nuevos y eventos
  próximos. Los avisos se pueden apagar en cualquier momento desde Ajustes, en
  la app o en el sistema.

## Menores de edad

La app no está dirigida a niños y no pide datos a nadie. Lo único que podría
contener datos personales es lo que alguien escriba en el asistente.

## Cómo borrar los datos

- Lo guardado en el teléfono se borra al desinstalar la app.
- Las conversaciones del asistente se borran solas del servidor en el plazo de
  arriba. Para pedir que se borre una antes, o para cualquier consulta sobre
  privacidad, escribir a ‹correo de contacto›.

## Cambios

Si esta política cambia, se actualiza esta página y su fecha.

## Responsable y contacto

‹Nombre de la persona o la institución responsable de la app›
‹Correo de contacto›
‹Ciudad, Paraguay›

_Última actualización: ‹fecha›._
