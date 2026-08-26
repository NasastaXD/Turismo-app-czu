Compilación de desarrollo de la app de turismo de Caaguazú.

## Cuál descargar

**`caaguazu-…-universal.apk`** si no sabés qué procesador tiene tu teléfono. Funciona en todos.

**`caaguazu-…-arm64.apk`** es más liviano y sirve para prácticamente cualquier teléfono de los últimos años.

Hay que permitir la instalación desde orígenes desconocidos.

## Qué esperar

Los textos entre ángulos —`‹nav.principal›`— no son un error: son las claves que todavía no tienen texto escrito. Las fotos de relleno son cuadrados de color.

El mapa funciona **sin conexión**: viaja dentro del APK.

## Advertencias

Está firmada con clave de depuración, así que **una versión nueva puede no instalarse encima de la anterior**: en ese caso hay que desinstalar primero. Se arregla en cuanto exista el keystore del proyecto.

El contenido sale de datos de ejemplo, no del panel: la API ya existe pero la app todavía apunta a los mocks.

El archivo `mapping-….txt` sirve para traducir los registros de error de esta compilación. Sin él, un log de fallo es ilegible.
