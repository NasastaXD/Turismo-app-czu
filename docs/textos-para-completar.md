# Textos de la app — para completar

**0 claves esperando texto.** Las 69 están escritas.

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

Nada por ahora. Las nueve claves que abrió el rework anterior —ocho de la hoja
de filtros y `ficha.leerMas`— ya tienen texto.

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

**Se fueron dos claves más**: `perfil.idioma` y `perfil.acerca`. Eran dos filas
de la pantalla de ajustes con chevrón de "esto lleva a algún lado" que no
llevaban a ninguno — no había selector de idioma ni pantalla de "acerca de"
detrás. El texto estaba escrito y queda en el historial: cuando esas dos
pantallas existan, las claves vuelven.

Aparte del rework, `main` sacó el modo mocks y con él dos claves más:
`barra.perfil` pasó a llamarse `barra.ajustes` (ya no es un perfil de cuenta,
es la pantalla de ajustes del teléfono) y `diag.origen` desapareció entera —
ya no hay un origen que elegir, sólo queda el panel.
