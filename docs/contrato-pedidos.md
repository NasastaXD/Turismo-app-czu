# Pedidos sobre el contrato de la API

De: el lado de la app Android.
Sobre: el contrato de la Parte 3 del brief, antes de que se implemente.

Versión para leer y compartir: https://claude.ai/code/artifact/c60448b1-2e1e-48fd-b40b-85a3cecbd81f

El brief dice que el contrato está propuesto y no grabado en piedra, y que cambiarlo
antes de implementarlo es barato. Esto es eso: un cambio que quita trabajo, seis
huecos que bloquean pantallas concretas, y cuatro confirmaciones.

Ninguno toca identidad, permisos ni flujo editorial. La app los consume tal como
están.

---

## 1. Los tiles ya no hacen falta

**Esto quita trabajo, no lo agrega.**

El brief (§4.1) asigna al panel generar y servir una pirámide ráster de zoom 10 a 17.
Medido sobre el territorio real, esa pirámide pesa unos **250 MB** — cada nivel de
zoom cuadruplica el anterior, y llegar a z17 es lo que hace falta para que un turista
pueda acercarse a ver una calle.

El mapa ya está resuelto por el lado de la app con tiles **vectoriales**, que guardan
geometrías en vez de imágenes y por eso se redibujan nítidos a cualquier zoom sin
almacenar cada nivel. El archivo completo del distrito pesa **2,0 MB** y viaja dentro
del APK.

Consecuencias:

- No hay que generar tiles.
- No hay que servirlos ni alojarlos.
- El mapa funciona sin conexión y sin depender de que el servidor esté vivo.

**Lo que sí sigue haciendo falta, sin cambios:** `GET /mapa/markers` y los PNG de
marcador por categoría. La separación entre markers y mapa base es justo lo que
mantiene el mapa retroactivo: se registra un lugar y el pin aparece sin regenerar nada.

---

## 2. Falta el payload de `GET /inventario`

Está especificado `/inventario/{id}` pero no el elemento de la lista.

La pantalla que lo consume muestra, por cada elemento: foto, nombre, rango de precios
y horario. Traer la ficha completa de cada uno para pintar eso sería traer artículo,
galería y accesos para descartarlos.

Propuesta:

```json
{
  "items": [
    {
      "id": 41,
      "tipo": "destino",
      "titulo": "…",
      "gancho": "…",
      "categoria": { "id": 12, "nombre": "…", "color": "#2E7D32" },
      "zona": { "id": 3, "nombre": "…" },
      "coordenadas": { "lat": -25.4669, "lng": -56.0175 },
      "portada": { "url": "…", "credito": "…" },
      "rango_precio": 2,
      "horario_resumen": "…",
      "actualizado": "2026-08-20T14:00:00Z"
    }
  ],
  "total": 128,
  "pagina": 1,
  "por_pagina": 20
}
```

`total` importa: sin él la app no puede saber si quedan más páginas sin pedir una de
más.

---

## 3. El rango de precios necesita ser un número

Hoy el precio existe solo como `practicos.costo`, texto libre.

Con texto libre no se puede pintar el indicador de rango que lleva cada tarjeta ni
filtrar por precio: habría que interpretar frases escritas por distintos promotores.

Propuesta: agregar `rango_precio`, entero de 0 a 4, donde 0 es gratuito y 1 a 4 van de
más barato a más caro. El texto libre se conserva tal cual para el detalle, porque dice
cosas que un número no dice.

Si el criterio de qué es caro en Caaguazú lo fija el promotor al cargar, mejor: es una
decisión editorial, no calculable.

---

## 4. Faltan los artículos relacionados en la ficha

En la ficha abierta, donde la referencia visual tiene etiquetas, va una lista de
artículos relacionados. El payload de `/inventario/{id}` no tiene ese campo, así que
esa sección hoy no se puede construir.

Propuesta: agregar a `/inventario/{id}`

```json
"articulos_relacionados": [
  { "id": 55, "titulo": "…", "portada": "…" }
]
```

Con lo mínimo para pintar la tarjeta y navegar. El resto se pide al abrir el artículo.

---

## 5. Falta el payload de los artículos

Están los endpoints `/articulos` y `/articulos/{id}`, pero no qué devuelven.

Propuesta para la lista:

```json
{
  "id": 55,
  "titulo": "…",
  "bajada": "…",
  "portada": { "url": "…", "credito": "…" },
  "autor": { "id": 7, "nombre": "…" },
  "publicado": "2026-08-20T14:00:00Z"
}
```

Y para el detalle, lo anterior más `cuerpo_html`, `pie_portada` y `relacionados`.

**Sobre el autor, un detalle que importa.** WordPress exige un autor válido en cada
entrada, y por la decisión de no tener a la gente del panel como usuarios de WordPress,
el autor técnico es siempre el usuario de servicio. En la app el autor que se muestra
es el promotor real, así que tiene que salir del meta donde se guarda el dueño, no de
`post_author`. Si sale de `post_author`, todos los artículos aparecerán firmados por
`caaguazu-servicio`.

---

## 6. Los eventos no traen imagen

El payload de `/eventos` tiene título, fechas, lugar, costo, categoría y artículo, pero
no foto. Las tarjetas de evento llevan imagen.

Propuesta: agregar `portada` con la misma forma que en el inventario.

---

## 7. El manifiesto de medios debería admitir animaciones

Una restricción del proyecto es que el público no lee párrafos, y que donde haría falta
explicar algo va una animación en lugar de texto. Si esas animaciones se sirven por el
manifiesto igual que las imágenes, se pueden cambiar sin publicar un APK nuevo.

Propuesta: que cada entrada declare su tipo.

```json
{
  "splash.fondo":        { "tipo": "imagen",    "url": "…", "alt": "" },
  "onboarding.recorrido": { "tipo": "animacion", "formato": "lottie", "url": "…" }
}
```

La app ignora los tipos que no sepa dibujar, así que agregar tipos nuevos más adelante
no rompe versiones viejas.

---

## 8. `/sync` necesita decir qué se borró

El endpoint está listado como delta para caché offline, pero sin payload. Y hay un
detalle que suele quedar afuera y rompe la caché en silencio: **una lista de lo que
cambió no alcanza, hace falta saber qué dejó de existir.**

Si una ficha se despublica y el delta solo trae altas y modificaciones, la app la sigue
mostrando para siempre.

Propuesta:

```json
{
  "desde": "2026-08-20T14:00:00Z",
  "hasta": "2026-08-26T09:00:00Z",
  "cambiados": { "inventario": [41, 42], "articulos": [55], "eventos": [] },
  "eliminados": { "inventario": [17], "articulos": [], "eventos": [12] },
  "completo": false
}
```

`completo: true` sería la señal de que el delta no alcanza y la app debe recargar todo
desde cero — útil cuando pasó demasiado tiempo o hubo una migración.

---

## 9. Confirmaciones

Cuatro cosas que la app asume y conviene dejar dichas:

1. **`ETag` en `/strings/{locale}` y `/media-manifest`.** La app cachea con eso y, si el
   refresco falla, sigue con la copia local. Sin `ETag` se descarga todo en cada
   arranque.
2. **`tipo` siempre presente en `/inventario`.** Ya acordado en el brief: la app está
   programada asumiendo que existe, así que cualquiera de las tres salidas sobre
   unificar atractivos, eventos y comercios la deja funcionando.
3. **Formato de error.** Qué cuerpo acompaña a un 4xx o 5xx, para poder distinguir un
   fallo de red de un fallo de servidor y mostrar lo correcto.
4. **Zonas.** Aparecen como filtro en `/inventario` y como campo en la ficha, pero no
   hay endpoint que las liste. Si van a ser un filtro visible, hace falta uno.

---

## 10. Lo que no cambia

Para que quede claro qué **no** se está pidiendo tocar:

- Autenticación, sesiones bearer y el array `permisos`. La app gatea con `permisos`,
  nunca con el rol.
- El flujo de revisión editorial y sus ocho estados.
- `GET /categorias`, incluidos los PNG de marcador pre-renderizados.
- `GET /mapa/markers`.
- El modelo de datos de las fichas.

La primera versión de la app es solo para turistas y no usa `/auth`, `/contenido` ni
`/mis-recorridos`. No hace falta apurarlos, pero tampoco descartarlos: se usan en
cuanto entre la carga desde el teléfono.

---

## Prioridad

Si hay que ordenarlo por lo que bloquea antes:

| | Bloquea |
|---|---|
| Payload de `/inventario` (§2) | La pantalla de lista, que es la principal |
| Payload de artículos (§5) | La sección Artículos entera |
| `rango_precio` (§3) | El indicador y el filtro de precio |
| `articulos_relacionados` (§4) | Una sección de la ficha |
| `portada` en eventos (§6) | Las tarjetas de evento |
| `eliminados` en `/sync` (§8) | Nada todavía; rompe la caché más adelante |

Mientras tanto la app se construye contra mocks con estos mismos payloads, así que
cuando la API exista la integración es cambiar la URL base.
