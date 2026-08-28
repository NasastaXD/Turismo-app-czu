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

**Ningún texto visible vive en el código.** Todos salen de `Textos.t("clave")`,
y las claves de un JSON. Eso permite cambiar cualquier texto sin publicar un APK
nuevo: se editan del lado del panel en `GET /strings/{locale}` y la app los toma
en el siguiente arranque. El archivo `app/src/main/assets/textos/es.json` es solo
el respaldo del primer arranque sin red.

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

## 3. El sistema visual — "Alpine Editorial"

Referencia: la app de Crans-Montana. La definición en una línea: **contenedores
rectangulares de esquina viva sobre bandas alternadas de blanco y gris cálido,
con toda la interacción encapsulada en píldoras de radio completo.** La tensión
entre esquina 0 y radio 999 es la firma, y no se rompe.

### Paleta

Vive entera en `Tono`. Ningún color se escribe suelto en una pantalla.

| Token | Uso |
|---|---|
| `papel` #FFFFFF | fondo base y tarjeta de lista |
| `banda` #F4F3F1 | banda de sección alterna, gris cálido |
| `superficie` #F5F5F5 | cuerpo de texto de tarjeta de carrusel |
| `tinta` #1D1D1F | títulos y texto principal |
| `tintaSuave` #55555A | descripciones y texto secundario |
| `linea` #E6E4E1 | hairlines de 1px |
| `acento` #E9503F | coral |
| `negro` #000000 | navegación, botón central, filtros, toggles |
| `velo` 45% negro | scrim sobre foto en tiles de menú |

**El acento tiene una regla estrecha:** solo va en fechas y metadatos de
cuándo/dónde/contacto, badges, bordes de iconos circulares y breadcrumb. Nunca
como fondo de superficie grande. Negro y acento no compiten en el mismo
componente: el negro es control, el acento es metadato.

### Tipografía

Una sola familia: **Poppins**, sans geométrica. La única excepción es el
**artículo**, que lleva serif —Source Serif 4— en titular, bajada y cuerpo. Se
justifica porque leer un texto largo es un contexto distinto de operar una
interfaz, y porque la referencia del proyecto para artículos es un diario.

Los títulos de tarjeta truncan con puntos suspensivos; **las descripciones
cortan sin ellos**.

### Radios y sombras

Radio 0 en toda superficie de contenido. Radio 999 en todo control. Radio 8 en
tarjetas de lista. No hay una cuarta opción.

**Una sola sombra en toda la app**, la del botón central de la barra inferior.

### Verificado, no documentado

`SistemaDeDisenoTest` falla la compilación si aparece una segunda sombra, un
radio que no salga de `Radio.*`, o un color que no salga de `Tono`. Es la única
forma de que estas reglas sigan siendo ciertas dentro de tres meses.

### Movimiento

Contenido, como el resto. La animación está para explicar de dónde sale algo y
adónde va, no para lucirse: nada rebota, nada gira. Los tiempos son
deliberadamente tranquilos, porque para este público una transición rápida no se
lee como ágil sino como un parpadeo del que uno no se entera.

**Si el teléfono tiene las animaciones apagadas, se respeta** — por
accesibilidad, por batería o por preferencia, en los tres casos animar igual
sería ignorar una decisión ya tomada.

---

## 4. El código

**Sin capas ceremoniales.** Si hay una sola implementación de algo, no hay
interfaz ni fábrica ni inyección de dependencias. Un único modelo por entidad,
sin cadenas DTO → dominio → interfaz que solo copian campos. Sin procesadores de
anotaciones: nada de Room ni Hilt.

La **única abstracción con más de una implementación** en todo el proyecto es
`Contrato`, y se justifica porque es el interruptor entre los mocks y la API
real.

**Sin Material3.** El diseño es propio de punta a punta y su tema no se usa en
ninguna pantalla; arrastrarlo solo sumaría peso.

### Dependencias

Doce en total, y solo tres fuera de AndroidX/Compose: **MapLibre** para el mapa,
**kotlinx.serialization** y **Coil** para imágenes. Sin cliente HTTP externo:
`HttpURLConnection` alcanza para GET y POST de JSON con ETag.

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

**Sin señal se sirve la copia guardada** en lugar de una pantalla de error. En un
distrito de 942 km² eso es la situación normal, no la excepción.

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

## 5. El mapa

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

## 6. Cómo se trabaja

Las correcciones se acumulan como commits en la rama de trabajo hasta que se
pida **"Release!"**. Cada release compila en Actions y publica los tres APK más
el `mapping.txt`.

**Se dice lo que no está verificado.** No hay emulador en el entorno de
desarrollo, así que el renderizado real siempre lo comprueba una persona con el
APK. Decir "listo" sobre algo que no se vio dibujado sería mentir.

---

## 7. Lo que no se hace

- Escribir contenido de producto.
- Usar imágenes de archivo.
- Meter un texto o un color directamente en una pantalla.
- Agregar una dependencia que se pueda evitar.
- Redondear una superficie de contenido, o poner una segunda sombra.
- Usar el acento fuera de su regla.
- Construir de nuevo algo que el panel ya resuelve.
- Dar por bueno lo que no se comprobó.
