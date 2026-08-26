# Textos de la app — para completar

**50 claves esperando texto.** 8 ya están escritas.

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
| `accion.volver` | artículos · lectura, inventario · ficha, recorridos · detalle | **no se ve**: descripción para lectores de pantalla |
| `banda.verTodo` | inicio | dos palabras — comparte línea con el título de sección |
| `barra.buscar` | barra superior | **no se ve**: descripción para lectores de pantalla |
| `barra.perfil` | barra superior | **no se ve**: descripción para lectores de pantalla |
| `diag.borrar` | diagnóstico | una o dos palabras — pantalla técnica, la ve poca gente |
| `diag.cache` | diagnóstico | una o dos palabras — pantalla técnica, la ve poca gente |
| `diag.compartir` | diagnóstico | una o dos palabras — pantalla técnica, la ve poca gente |
| `diag.origen` | diagnóstico | una o dos palabras — pantalla técnica, la ve poca gente |
| `diag.vaciar` | diagnóstico | una o dos palabras — pantalla técnica, la ve poca gente |
| `diag.vacio` | diagnóstico | una o dos palabras — pantalla técnica, la ve poca gente |
| `diag.version` | diagnóstico, perfil | una o dos palabras — pantalla técnica, la ve poca gente |
| `estado.cargando` | estados de carga | una frase corta — es lo que se lee cuando algo carga, falla o está vacío |
| `estado.error` | estados de carga | una frase corta — es lo que se lee cuando algo carga, falla o está vacío |
| `estado.guardado` | estados de carga | una frase corta — es lo que se lee cuando algo carga, falla o está vacío |
| `estado.pendiente` | secciones pendientes | una frase corta — es lo que se lee cuando algo carga, falla o está vacío |
| `estado.reintentar` | estados de carga | una frase corta — es lo que se lee cuando algo carga, falla o está vacío |
| `estado.vacio` | estados de carga | una frase corta — es lo que se lee cuando algo carga, falla o está vacío |
| `ficha.acceso` | inventario · ficha | una o dos palabras — etiqueta de campo o de botón |
| `ficha.agregar` | inventario · ficha | una o dos palabras — etiqueta de campo o de botón |
| `ficha.autor` | inventario · ficha | una o dos palabras — etiqueta de campo o de botón |
| `ficha.camino` | inventario · ficha | una o dos palabras — etiqueta de campo o de botón |
| `ficha.contacto` | inventario · ficha | una o dos palabras — etiqueta de campo o de botón |
| `ficha.costo` | inventario · ficha | una o dos palabras — etiqueta de campo o de botón |
| `ficha.duracion` | inventario · ficha | una o dos palabras — etiqueta de campo o de botón |
| `ficha.fuentes` | inventario · ficha | una o dos palabras — etiqueta de campo o de botón |
| `ficha.galeria` | inventario · ficha | una o dos palabras — etiqueta de campo o de botón |
| `ficha.horario` | inventario · ficha | una o dos palabras — etiqueta de campo o de botón |
| `ficha.info` | inventario · ficha | una o dos palabras — etiqueta de campo o de botón |
| `ficha.llegar` | inventario · ficha | una o dos palabras — etiqueta de campo o de botón |
| `ficha.mapa` | inventario · ficha | una o dos palabras — etiqueta de campo o de botón |
| `ficha.quitar` | inventario · ficha | una o dos palabras — etiqueta de campo o de botón |
| `ficha.relacionados` | artículos · lectura, inventario · ficha | una o dos palabras — etiqueta de campo o de botón |
| `ficha.servicios` | inventario · ficha | una o dos palabras — etiqueta de campo o de botón |
| `ficha.temporada` | inventario · ficha | una o dos palabras — etiqueta de campo o de botón |
| `inv.lista` | piezas comunes | una o dos palabras |
| `inv.mapa` | piezas comunes | una o dos palabras |
| `inv.resultados` | inventario · lista | una o dos palabras |
| `mapa.error.detalle` | mapa | frase corta. `mapa.atribucion` **no se toca** |
| `mapa.error.titulo` | mapa | frase corta. `mapa.atribucion` **no se toca** |
| `perfil.acerca` | perfil | una o dos palabras |
| `perfil.idioma` | perfil | una o dos palabras |
| `principal.eventos` | inicio | una o dos palabras — es un título de sección |
| `rec.abrir` | recorridos, recorridos · detalle | una o dos palabras; `rec.vacio` y `rec.demasiadas` son una frase corta |
| `rec.demasiadas` | recorridos | una o dos palabras; `rec.vacio` y `rec.demasiadas` son una frase corta |
| `rec.mio` | recorridos | una o dos palabras; `rec.vacio` y `rec.demasiadas` son una frase corta |
| `rec.noDisponible` | recorridos · detalle | una o dos palabras; `rec.vacio` y `rec.demasiadas` son una frase corta |
| `rec.paradas` | recorridos | una o dos palabras; `rec.vacio` y `rec.demasiadas` son una frase corta |
| `rec.prehechos` | recorridos | una o dos palabras; `rec.vacio` y `rec.demasiadas` son una frase corta |
| `rec.quitar` | recorridos | una o dos palabras; `rec.vacio` y `rec.demasiadas` son una frase corta |
| `rec.vacio` | recorridos | una o dos palabras; `rec.vacio` y `rec.demasiadas` son una frase corta |

## Ya escritas

| Clave | Texto | Dónde aparece |
|---|---|---|
| `app.nombre` | Caaguazú | armazón |
| `mapa.atribucion` | © OpenStreetMap | mapa |
| `nav.articulos` | Artículos Turísticos | barra inferior, inicio |
| `nav.ia` | AI | barra inferior |
| `nav.inventario` | Inventario Turístico | barra inferior, inicio, inventario · lista |
| `nav.principal` | Principal | barra inferior, inventario · lista |
| `nav.recorridos` | Recorridos | barra inferior, inicio |
| `precio.gratis` | Gratis | piezas comunes |

---

## Dos advertencias

**`mapa.atribucion` no se toca.** Dice `© OpenStreetMap` y es obligatorio por la
licencia de los datos del mapa. Si se cambia o se vacía, la app deja de cumplir.

**Las claves `barra.*` y `accion.*` no se ven en pantalla.** Son lo que lee un
lector de pantalla para alguien que no ve la interfaz. Conviene que digan qué hace
el control, no cómo se ve.

---

Una prueba verifica en cada compilación que no falte ninguna clave que el código
pida, y que no sobre ninguna que nadie muestre. Si se agrega una pantalla nueva
con textos nuevos, la lista de arriba queda desactualizada pero la prueba avisa.
