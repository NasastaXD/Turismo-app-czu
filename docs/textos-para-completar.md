# Textos de la app — para completar

**9 claves esperando texto.** 62 ya están escritas.

Todo texto de interfaz de la app sale de aquí, no del código. Cambiar cualquiera
de estos no requiere publicar un APK nuevo: se editan del lado del panel y la app
los toma en el siguiente arranque.

Se editan en dos sitios: `app/src/main/assets/textos/es.json`, que es el respaldo
que viaja dentro del APK para el primer arranque sin red, y el mismo juego de
claves en `GET /strings/es` del panel. Los otros dos idiomas (`en`, `gn`) usan las
mismas claves.

No hace falta completarlas todas de una: una clave sin texto se muestra entre
ángulos y no rompe nada.

---

## Faltan

Ocho de las nueve son de la hoja de filtros, que es pantalla nueva. Son palabras
sueltas y con eso la pantalla queda terminada.

| Clave | Dónde aparece | Qué escribir |
|---|---|---|
| `filtro.titulo` | buscar · hoja de filtros | una palabra — el título de la hoja |
| `filtro.aplicar` | buscar · hoja de filtros | una o dos palabras — el botón verde que cierra la hoja |
| `filtro.limpiar` | buscar · hoja de filtros | una palabra — quita todos los filtros puestos |
| `filtro.categoria` | buscar · hoja de filtros | una palabra — título del grupo |
| `filtro.zona` | buscar · hoja de filtros | una palabra — título del grupo |
| `filtro.etiqueta` | buscar · hoja de filtros | una palabra — título del grupo |
| `filtro.precio` | buscar · hoja de filtros | una palabra — título del grupo |
| `ficha.leerMas` | buscar · ficha | dos o tres palabras — abre el resto del texto, que arranca plegado |
| `perfil.avisosDetalle` | perfil | una frase corta bajo el interruptor de avisos — qué avisa y cada cuánto |

---

## Lo que cambió con el rediseño

**Se fue una clave**: `estado.pendiente`. Era el texto de la pantalla "sección
todavía sin construir", y ya no hay ninguna sección en ese estado. Si vuelve a
hacer falta, el texto estaba escrito y está en el historial.

**Tres claves cambiaron de lugar** sin cambiar de sentido:

- `barra.buscar` es ahora también la etiqueta de la sección en la barra
  inferior, además del marcador del campo de búsqueda.
- `nav.inventario` pasó de ser la etiqueta de una pestaña a ser el título de la
  pantalla de búsqueda.
- `banda.verTodo` aparece ahora en varios encabezados de sección del inicio.

Ninguna otra clave se tocó.
