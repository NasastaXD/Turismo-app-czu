# Filosofía del proyecto

App Android de turismo de Caaguazú. Este documento es el criterio con el que se
toman las decisiones acá — para quien siga el proyecto, humano o agente.

Si algo de lo que sigue estorba, se discute y se cambia el documento. Lo que no
se hace es ignorarlo en silencio.

---

## 1. Qué es esta app y qué no es

`caaguazu.net` ya tiene el sistema de turismo funcionando: cinco plugins de
WordPress con identidad propia, flujo editorial de ocho estados y un panel de
promotores instalable como PWA. **La app es un cliente más de ese backend, no un
sistema paralelo.**

Eso significa que la app **no** construye:

- Identidad, sesiones ni contraseñas. Consume `/auth/login` y guarda un token.
- Permisos. El servidor manda el array `permisos` ya resuelto y la interfaz se
  gatea con eso, nunca con el rol: hay un segundo eje —el nivel de confianza—
  que desbloquea capacidades por su cuenta.
- Flujo de revisión. Ya existe. Si algún día se carga una ficha desde la app,
  entra a ese mismo flujo.
- Una base de contenido. Caché local sí, descartable por definición. Fuente de
  verdad, no.

El punto de contacto es la API `/wp-json/czu-app/v1/` y nada más. Nunca acceso
directo a la base.

---

## 2. La redacción

**La regla dura del proyecto.** El público es en buena parte gente mayor que no
lee párrafos, y todo texto de producto lo escribe una persona.

Un agente escribe, como mucho, un título o tres palabras. Ni artículos, ni
descripciones, ni copy de relleno, ni textos de ejemplo que queden en
producción. Si hace falta un marcador para maquetar, va marcado de forma
inequívoca —`‹clave›`— para que sea imposible confundirlo con texto terminado.

**Tres idiomas: castellano, inglés y portugués.** El castellano es el original
—en el que se escribe todo— y los otros dos son una capa encima. El idioma se
elige en Ajustes y se guarda; sin elegir, se sigue al del teléfono, y si el
teléfono está en uno que no tenemos, se abre en castellano.

La lista de idiomas **no va compilada**: la manda el panel en `GET /idiomas`, y
lo que viaja en el APK es sólo el respaldo para el primer arranque sin red. El
guaraní está previsto del lado del panel y va a aparecer ahí antes de que salga
un APK nuevo.

**El contenido cae al original campo por campo, no por pieza.** Una ficha puede
venir con el título traducido y el cuerpo en castellano porque alguien tradujo
uno y todavía no el otro. Por eso el contenido trae `traducido`, y por eso la
ficha avisa en vez de esconderse: a medio traducir sigue teniendo la foto, el
mapa, el horario y el precio, que es la mayor parte de para qué se abre.

**El idioma viaja en la URL de todo lo que lleva texto**, incluso en castellano.
La clave de la caché es la URL, así que dejarlo afuera para el original haría
que la ficha 260 en inglés pise a la 260 en castellano.

**Los nombres de categorías y etiquetas no se traducen con la ficha.** Son del
sistema y los comparten cientos de fichas: traducirlos por ficha sería traducir
lo mismo una vez por lugar. `GET /categorias` y `GET /etiquetas` aceptan
`?idioma` y devuelven `nombre` ya traducido, y lo mismo vale para el
`categoria`/`etiquetas[]` embebido en una ficha, un artículo o un recorrido: no
hay que buscar nada aparte, lo que trae el campo es lo que se muestra.

**Ningún texto visible vive en el código.** Todos salen de `Textos.t("clave")`,
y las claves de un JSON. Eso permite cambiar cualquier texto sin publicar un APK
nuevo: se editan del lado del panel en `GET /strings/{locale}` y la app los toma
en el siguiente arranque. Los archivos `app/src/main/assets/textos/{es,en,pt}.json` son solo
el respaldo del primer arranque sin red. El castellano es además el piso de los
otros dos: una clave que falta en inglés sale en castellano, nunca marcada.

**Los textos del servidor se fusionan sobre el respaldo, nunca lo reemplazan.**
Un panel a medio cargar no puede dejar sin texto al resto de la app — y menos a
`mapa.atribucion`, que es obligatoria por licencia.

Y esto no es una convención documentada: **`SinRedaccionTest` falla la
compilación** si un literal visible aparece en el código de interfaz, y
`ClavesDeTextoTest` falla si el código pide una clave que no existe o si sobra
una que nadie muestra.

Lo mismo con las imágenes: las de interfaz salen de `/media-manifest`, no de
recursos incrustados. Y nunca imágenes de archivo (stock): o son del destino, o
son marcadores evidentemente sintéticos.

---

## 3. La forma de la app

Cuatro secciones abajo: **inicio, buscar, artículos, recorridos**. El perfil no
ocupa una: vive en un botón redondo en la cabecera de cada sección, porque se
entra dos veces y no se vuelve.

**Buscar no es un índice, es una búsqueda.** El inventario dejó de ser un árbol
por el que se baja —categorías, lista, ficha— y pasó a ser una sola pantalla que
cambia de cara: mientras no se pidió nada, muestra las categorías como mosaico;
en cuanto hay algo escrito o filtrado, muestra resultados. Las categorías siguen
estando, pero como sugerencia y no como peaje.

**Los filtros viven en una hoja que sube.** Cuatro ejes —categoría, zona,
etiqueta y precio— en una capa sobre lo que ya estaba, no en una pantalla
aparte. La diferencia no es estética: quien filtra quiere ver cómo cambia lo que
tiene detrás. Se aplican al tocarlos; el botón de abajo solo cierra.

**El mapa es la misma búsqueda, dibujada sobre el lienzo.** Ocupa la pantalla
entera, los controles flotan encima y el pin tocado abre una tarjeta abajo.
Antes el mapa era el último tercio de una columna con cabecera, buscador, chips
y contador: en un teléfono quedaba del tamaño de una estampilla, que para un
distrito de 942 km² no alcanza para ubicarse.

**El inicio tiene jerarquía, no cuatro rieles.** Búsqueda, atajos de categoría,
*una* cosa grande —el evento que está pasando o el que viene, que es lo único de
la app que caduca—, el mosaico de lugares, la banda del recorrido y los
artículos como lista. Cuatro carruseles horizontales iguales son un índice
disfrazado de portada: ninguna foto se ve y las cuatro secciones pesan lo mismo
aunque no lo valgan.

**La ficha es una hoja que sube sobre la foto.** La foto queda fija al fondo, a
sangre y hasta arriba de todo; el contenido la tapa con esquinas redondeadas. Al
desplazar, la foto se queda y la hoja avanza, así que el lugar sigue presente
mientras se lee sobre él. El título va en la hoja, en tinta — un titular blanco
sobre una imagen depende de que la imagen sea oscura justo ahí, y las fotos del
destino no se eligen pensando en eso.

**El precio se filtra en el teléfono.** El contrato no tiene parámetro para eso
y esperar una versión del servidor para poder mover un control sería atarse las
manos. Cuando el panel lo agregue, se cambia por el parámetro y se borra el
filtro local.

---

## 4. El sistema visual

La definición en una línea: **fotos y tarjetas muy redondeadas que flotan sobre
un fondo claro con una sombra ancha y suave, con toda la interacción encapsulada
en píldoras de radio completo sobre relleno liso.** Lo que separa un bloque del
siguiente es su propia sombra o el aire entre ellos: nunca una línea, nunca un
cambio de fondo.

**El sistema separa por superficie, no por contorno.** Un control en reposo es
un relleno claro (`campo`); elegido, es ese mismo relleno en `contraste`. No hay
bordes de 1px alrededor de chips ni de botones secundarios: llenaban la pantalla
de rectángulos y competían con el único botón que sí tiene que verse.

**La foto es el contenido y no lleva marco.** En el inicio y en las grillas, las
fotos van sueltas sobre el fondo con su radio, y el texto va debajo. Encerrar una
foto en una tarjeta blanca deja un borde de papel entre la foto y el ojo que no
dice nada.

La app sigue el **modo claro u oscuro del teléfono**. No hay un interruptor
propio: quien ya eligió en su sistema no tiene que volver a elegir acá.

### Paleta

Vive entera en `Tono`. Ningún color se escribe suelto en una pantalla.

Tres colores de marca, **iguales en los dos modos** — una identidad que cambia
de color según la hora del día deja de serlo:

| Token | Color | Regla de uso |
|---|---|---|
| `primario` | Eton Blue #96C8A2 | la acción principal. Como mucho una por pantalla |
| `sobrePrimario` | #1E3A28 | la tinta sobre el verde |
| `acento` | Bittersweet #FF6F61 | favoritos, fechas y metadatos de cuándo/dónde |
| `destacado` | Mango #FFC300 | lo que está pasando **ahora**. Hoy, solo el evento en curso |
| `sobreFoto` | #FFFFFF | la tinta sobre una foto, siempre con velo debajo |

Y las superficies, que **sí** cambian con el modo:

| Token | Claro | Oscuro | Uso |
|---|---|---|---|
| `fondo` | #F7F7F5 | #0F0F10 | fondo de página |
| `papel` | #FFFFFF | #1B1B1D | la tarjeta |
| `banda` | #F2F1EF | #151517 | hueco de una foto que todavía no bajó |
| `campo` | #F0EFED | #232326 | relleno de control: buscador, chip, píldora, atajo |
| `tinta` | #333333 | #F2F2F2 | títulos y texto principal |
| `tintaSuave` | #6E6E73 | #9A9AA0 | descripciones y texto secundario |
| `linea` | #E8E6E3 | #2C2C2E | hairlines de 1px dentro de una tarjeta |
| `contraste` | #1F1F21 | #F2F2F2 | control de máximo contraste: sección activa, chip elegido |
| `sobreContraste` | #FFFFFF | #1F1F21 | lo que se escribe encima |
| `sombra` | 8% gris | negro | color de toda sombra |
| `velo` | 35% negro | igual | scrim sobre foto |
| `veloProfundo` | 55% negro | igual | el pie del degradado, donde va el texto |

**Cada color de marca tiene un rol y no sale de él.** Tres colores sin regla son
ruido. En particular: el verde es acción, nunca decoración; el mango no se usa
para nada que no esté ocurriendo en este momento.

**El texto sobre el verde va en verde oscuro, no en blanco.** La referencia usa
blanco, que sobre ese verde da 1,9:1 — ilegible al sol y muy por debajo del
mínimo accesible. Con la tinta oscura la pieza se ve igual y el contraste sube a
7:1. Para un público que en buena parte es gente mayor leyendo en la calle, eso
no es un detalle.

**El velo sobre foto es un degradado, no un tinte parejo.** Carga abajo, que es
donde vive el texto, y deja limpia la parte de arriba. Velar la foto entera por
parejo apaga justo lo que se vino a ver.

### Tipografía

Una sola familia: **Poppins**, sans geométrica. La única excepción es el
**artículo**, que lleva serif —Source Serif 4— en titular, bajada y cuerpo. Se
justifica porque leer un texto largo es un contexto distinto de operar una
interfaz, y porque la referencia del proyecto para artículos es un diario.

Cada pantalla abre con su **título grande alineado a la izquierda**
(`tituloPantalla`, 26). No hay barra superior: una barra con el nombre de la app
repetido arriba de las cinco pantallas gasta alto para decir algo que nadie
necesita leer cinco veces.

**`descripcion` (15sp) no se achica.** Es el único tamaño que no se toca: es el
texto que se lee de corrido, y el público es en buena parte gente mayor leyendo
en la calle. Compactar una pantalla a costa de ese tamaño se paga en quien no
puede leerla.

Los títulos de tarjeta truncan con puntos suspensivos; **las descripciones
cortan sin ellos**.

### Radios y elevación

Todo radio sale de `Radio`: `tarjeta` 20, `media` y `lista` 16, `hoja` 28,
`completo` 999 para cualquier control, y `ninguno` 0 —que es solo para el medio
que va a sangre dentro de un contenedor que ya recorta, porque redondear de
nuevo dejaría una esquina doble. Una forma puede redondear esquina por esquina
—la cabecera de una ficha va redondeada solo abajo— siempre que **cada** valor
salga de `Radio`.

La elevación tiene **dos alturas** y salen de `Elevacion`: `tarjeta` 8 para lo
que apenas se despega del fondo, `flotante` 14 para lo que tiene que despegarse
de una foto o un mapa. Toda sombra lleva `Tono.sombra` como `ambientColor` y
`spotColor`: sin eso sale del negro por omisión y en modo oscuro pinta un halo
sucio alrededor de cada tarjeta.

### El ritmo

`Medida` fija el aire: `margen` 20, `entreTarjetas` 14, `dentroTarjeta` 16,
`entreSecciones` 30. El sistema es aireado a propósito — apretarlo fue lo
primero que se notó mal en la versión anterior.

### Verificado, no documentado

`SistemaDeDisenoTest` falla la compilación si un radio no sale de `Radio.*`, si
una elevación no sale de `Elevacion.*`, si una sombra no lleva `Tono.sombra`, si
un color se escribe suelto en una pantalla, o si un token de `Tono` define su
valor en un solo modo. Es la única forma de que estas reglas sigan siendo
ciertas dentro de tres meses.

### Movimiento

Contenido, como el resto. La animación está para explicar de dónde sale algo y
adónde va, no para lucirse: nada rebota, nada gira. Los tiempos son
deliberadamente tranquilos, porque para este público una transición rápida no se
lee como ágil sino como un parpadeo del que uno no se entera.

**Si el teléfono tiene las animaciones apagadas, se respeta** — por
accesibilidad, por batería o por preferencia, en los tres casos animar igual
sería ignorar una decisión ya tomada.

---

## 5. El código

**Sin capas ceremoniales.** Si hay una sola implementación de algo, no hay
interfaz ni fábrica ni inyección de dependencias. Un único modelo por entidad,
sin cadenas DTO → dominio → interfaz que solo copian campos. Sin procesadores de
anotaciones: nada de Room ni Hilt.

**No hay modo mocks.** Existió mientras la API todavía no estaba publicada;
ahora que lo está, `ApiHttp` es el único acceso a los datos, sin una interfaz
`Contrato` de por medio — sería la abstracción con una sola implementación que
la regla de arriba prohíbe.

**Sin Material3.** El diseño es propio de punta a punta y su tema no se usa en
ninguna pantalla; arrastrarlo solo sumaría peso.

### Dependencias

Trece en total, y solo tres fuera de AndroidX/Compose: **MapLibre** para el mapa,
**kotlinx.serialization** y **Coil** para imágenes. Sin cliente HTTP externo:
`HttpURLConnection` alcanza para GET y POST de JSON con ETag.

**WorkManager** es la decimotercera y entró por los avisos. Es la única forma de
que Android deje correr una revisión periódica sobreviviendo a Doze y al
reinicio del teléfono. Sin ella la alternativa era Firebase, que es justo lo que
el proyecto evita.

Menos servicios externos es un objetivo explícito del proyecto, no una
consecuencia.

### Failsafes

Los errores no cruzan capas como excepciones sueltas: las operaciones que pueden
fallar devuelven `Resultado`, y quien llama está obligado a contemplar el fallo.

Cada pantalla tiene sus cuatro estados reales —cargando, con datos, vacía, error
con reintentar—, porque "sin datos" y "falló al traerlos" se arreglan de formas
distintas.

El analizador de JSON **ignora campos desconocidos a propósito**: el panel puede
ampliar el contrato en cualquier momento y una app ya publicada no debe empezar
a fallar porque el servidor mandó un campo de más.

**Un elemento roto no tumba la lista entera.** Las listas y páginas se
decodifican elemento por elemento con `DecodificadorTolerante`: un solo campo
fuera de tipo en un solo sitio de mil no puede convertirse en una pantalla vacía
para todos. El elemento se omite y queda registrado.

**Sin señal se sirve la copia guardada** en lugar de una pantalla de error. En un
distrito de 942 km² eso es la situación normal, no la excepción. Antes de
resignarse a la copia guardada, `Http` reintenta una vez tras una espera corta:
la mayoría de los cortes que se ven en el teléfono son momentáneos.

### El registro

Propio, sin dependencias, sin telemetría saliendo del dispositivo. **Las
etiquetas se escriben a mano como texto literal**, nunca derivadas del nombre de
clase: R8 las renombra y el registro dejaría de servir justo en release, que es
cuando hace falta.

Escribe fuera del hilo que lo llama —una escritura por línea en el hilo que
dibuja se siente como tirones—, salvo la caída, que es síncrona porque ahí no
hay un después.

Se exporta desde la pantalla de diagnóstico, oculta tras siete toques en la
versión. Cada release adjunta su `mapping.txt`: sin él, un registro ofuscado es
ilegible incluso para quien escribió el código.

### Optimización

Se prioriza optimización real sobre claridad, por decisión explícita del dueño
del proyecto. En la práctica eso es: modelos inmutables para que Compose no
recomponga de más, lecturas diferidas —pasar el estado como función y no como
valor— para que la recomposición ocurra en el nodo más profundo, R8 en modo
completo, splits por ABI, y sin ceremonia que cueste kilobytes.

---

## 6. El mapa

**Vectorial, embebido, sin servidor.** El recorte de Caaguazú pesa 2 MB y viaja
dentro del APK; la pirámide ráster equivalente pesaría unos 250 MB. MapLibre lo
lee por su soporte nativo de `pmtiles://`. Sin clave de API, sin cuenta, sin
depender de que ningún servicio siga vivo dentro de dos años.

**Los pines viajan aparte de los tiles** y se dibujan encima. Eso es lo que
mantiene el mapa retroactivo: se registra un lugar nuevo y el pin aparece sin
regenerar ni redistribuir nada.

La atribución `© OpenStreetMap` es **obligatoria por licencia ODbL** y se dibuja
visible sobre el mapa. No es decoración ni es opcional.

Para navegar se delega en la app de mapas del teléfono, porque quien toca "cómo
llego" quiere llegar, y para eso su app de siempre tiene voz, tráfico y
transporte público. Con un límite conocido: **Google Maps corta en nueve paradas
intermedias**, y cuando un recorrido no entra se avisa en vez de abrirlo
incompleto sin que nadie se entere.

---

## 7. Avisos y calendario

Las dos cosas siguen el mismo criterio que el mapa: **se delega en lo que el
teléfono ya tiene, en vez de construirlo de nuevo.**

### Agendar un evento

`CalendarContract.ACTION_INSERT` abre el calendario de la persona con el
formulario ya completado, y ella confirma. **No se pide ningún permiso**: la app
nunca lee ni escribe en el calendario, solo propone. Pedir acceso a la agenda
entera de alguien para esto sería pedir mucho a cambio de nada.

El botón solo aparece cuando la ficha es un evento y su fecha se pudo
interpretar. Un botón que no puede hacer nada no se dibuja.

### Avisos

**No hay push.** No hay Firebase, no hay token de dispositivo, no hay servidor
de notificaciones. La app pregunta cada seis horas con los endpoints que ya sabe
pedir, y decide en el teléfono. Eso deja al proyecto sin un servicio externo más
—que es un objetivo explícito— y de paso sin que nadie del otro lado sepa quién
tiene la app instalada.

Seis horas es deliberado: un inventario turístico cambia por semana, no por
minuto, y revisar más seguido solo gastaría batería para encontrar lo mismo.

Cuatro reglas que evitan que los avisos se vuelvan insoportables:

- **Arrancan encendidos.** En Android 13 o más nuevo eso igual exige pedir el
  permiso del sistema una sola vez, al primer arranque; si se niega, el
  interruptor cae solo a apagado en vez de quedar prendido sin avisar nunca.
- **La primera revisión no avisa nada**, solo toma nota de lo que ya existe.
  Avisar del histórico completo al encender sería inutilizable.
- **Cinco avisos por vuelta como máximo.** Pasado ese punto no informa, molesta.
- **Un evento se avisa una sola vez**, cuando entra en la ventana de dos días.
  Avisar al publicarse sería inútil —puede faltar un mes— y avisar todos los
  días hasta que ocurra, insoportable.

Dos canales separados, artículos y eventos, porque Android deja apagar uno sin
el otro y eso solo funciona si están separados desde el principio.

---

## 8. Cómo se trabaja

Las correcciones se acumulan como commits en la rama de trabajo hasta que se
pida **"Release!"**. Cada release compila en Actions y publica los tres APK más
el `mapping.txt`.

**Se dice lo que no está verificado.** No hay emulador en el entorno de
desarrollo, así que el renderizado real siempre lo comprueba una persona con el
APK. Decir "listo" sobre algo que no se vio dibujado sería mentir.

---

## 9. Lo que no se hace

- Escribir contenido de producto.
- Usar imágenes de archivo.
- Meter un texto o un color directamente en una pantalla.
- Agregar una dependencia que se pueda evitar.
- Inventar un radio o una elevación fuera de `Radio` y `Elevacion`.
- Usar un color de marca fuera de su rol.
- Definir un color de `Tono` en un solo modo.
- Construir de nuevo algo que el panel ya resuelve.
- Dar por bueno lo que no se comprobó.
