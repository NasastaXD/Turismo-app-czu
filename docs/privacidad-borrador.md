# Política de privacidad — borrador técnico

Esto es un borrador armado a partir de una auditoría del código, no un texto
legal terminado. Antes de publicarlo hace falta: completar los datos entre
`‹corchetes›`, que alguien con criterio legal lo revise, y alojarlo en una URL
pública — las dos tiendas piden ese link en el formulario de alta.

---

## Qué hace esta app

Caaguazú Turismo es un cliente de consulta del contenido turístico publicado
en caaguazu.net. Muestra un mapa, un inventario de sitios, artículos y
recorridos. No tiene cuentas de usuario ni inicio de sesión.

## Qué datos NO recolecta

- No pide nombre, correo, teléfono ni ningún dato de identidad.
- No tiene analítica ni telemetría de uso: ningún tercero (Google Analytics,
  Firebase, Meta, etc.) recibe información de quién usa la app o cómo.
- No usa un identificador de publicidad (no muestra anuncios).
- No comparte datos con nadie, porque no junta ninguno para compartir.

## Qué se guarda, y dónde

Todo lo que la app recuerda queda **en el teléfono, sin salir de él**:

| Qué | Para qué | Se borra |
|---|---|---|
| Idioma elegido | Recordar la preferencia entre aperturas | Al desinstalar, o cambiándolo en Ajustes |
| Avisos encendidos/apagados | Respetar la elección | Al desinstalar, o cambiándolo en Ajustes |
| Favoritos y el recorrido propio en armado | Que no se pierdan al cerrar la app | Al desinstalar |
| IDs de artículos y eventos ya avisados | No repetir el mismo aviso dos veces | Al apagar los avisos, o al desinstalar |
| Copia de la última respuesta del servidor | Que la app funcione sin conexión | Desde la pantalla de diagnóstico, o al desinstalar |

Nada de esto se sube a ningún servidor. Si la persona desinstala la app,
desaparece todo junto con el resto de los datos de la aplicación.

## Qué se pide por red

La app se conecta, solo por HTTPS, a `caaguazu.net` para traer contenido
turístico público: categorías, sitios, artículos, recorridos y las imágenes
que los acompañan. Estas consultas no incluyen ningún dato personal — solo
lo necesario para pedir el contenido (por ejemplo, el idioma elegido, para
recibir los textos en ese idioma).

## Permisos que pide el teléfono

- **Internet y estado de la red**: para traer el contenido turístico.
- **Notificaciones** (Android 13 o más nuevo): para avisar de artículos
  nuevos o eventos próximos, si la persona lo deja encendido. Se puede
  apagar en cualquier momento desde Ajustes, dentro de la app o del sistema.

La app no pide acceso a contactos, cámara, micrófono, ubicación en segundo
plano, ni al calendario (agendar un evento abre la app de calendario del
teléfono con el evento ya cargado; la persona confirma ahí, la app nunca lee
ni escribe la agenda).

## Menores de edad

La app no está dirigida específicamente a niños y no recolecta datos de
nadie, sin importar la edad.

## Cambios a esta política

‹Cómo se va a avisar un cambio: por ejemplo, actualizando la fecha de esta
página y la versión mínima de la app a partir de la cual aplica.›

## Contacto

‹Nombre de la organización o persona responsable, y un correo de contacto.›

---

_Última actualización: ‹fecha›. Corresponde a partir de la versión ‹x.y.z›
de la app._
