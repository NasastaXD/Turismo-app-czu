# Textos de la app — para completar

**2 claves esperando texto.** 63 ya están escritas.

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

| Clave | Dónde aparece | Qué escribir |
|---|---|---|
| `ficha.leerMas` | inventario · ficha | dos o tres palabras — abre el resto del texto de una ficha, que arranca plegado |
| `perfil.avisosDetalle` | perfil | una frase corta bajo el interruptor de avisos — qué avisa y cada cuánto |

---

## Lo que cambió con el rediseño

Ninguna clave se borró y ninguna cambió de significado: el rediseño movió
piezas, no texto. La única clave nueva es `ficha.leerMas`.

Dos claves cambiaron de lugar sin cambiar de sentido:

- `banda.verTodo` ahora aparece también en la cabecera del inventario, como
  salida a la lista sin filtrar.
- `barra.perfil` pasó de ser solo la descripción del botón a ser además el
  título de la pantalla de perfil.
