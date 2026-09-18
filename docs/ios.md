# iOS — qué se eligió, por qué, y qué hay hecho

Investigación y primer código para llevar la app a iOS. Lo que sigue separa
tres cosas que conviene no mezclar: **la decisión** (con qué se hace),
**lo construido** (qué hay en el repo), y **lo que falta** — que es la
mayor parte, y donde hay dos preguntas abiertas que no me corresponde
resolver solo.

---

## 1. La decisión: Compose Multiplatform

Las tres opciones que estaban sobre la mesa en `publicar-en-tiendas.md`:

| Opción | Reutiliza | Costo | Riesgo |
|---|---|---|---|
| Swift/SwiftUI de cero | nada | rehacer 9 pantallas + datos en otro lenguaje | dos apps que se desincronizan |
| KMP con interfaz en SwiftUI | datos y contrato | rehacer 9 pantallas en SwiftUI | el sistema visual se mantiene dos veces |
| **Compose Multiplatform** | **datos, contrato y pantallas** | adaptar lo que toca el sistema | API del mapa todavía inestable |
| Flutter / React Native | nada | reescribir todo | choca con casi todo lo decidido acá |

**Se eligió Compose Multiplatform (CMP).** La razón de fondo es una
decisión que ya estaba tomada en este proyecto por otro motivo: **no hay
Material3.** El sistema visual es propio de punta a punta —`Tono`, `Radio`,
`Elevacion`, `Medida`, `Letra`, e iconos dibujados a mano como
`ImageVector`— y todo eso es `compose.foundation` y `compose.ui`, que son
multiplataforma. Lo que parecía una restricción del proyecto es justamente
lo que hace que las pantallas puedan cruzar: no hay una capa de Material
atada a Android de la que despegarse.

La contraparte honesta: **CMP para iOS es estable desde 1.8.0 (mayo de
2025)**, pero `maplibre-compose` —el envoltorio del mapa— declara Android e
iOS en **beta**, y rompe la API entre versiones menores. Por eso su versión
queda fijada exacta en `libs.versions.toml` y no con rango.

### Lo que decidió la viabilidad

Había un único riesgo capaz de tumbar todo el enfoque, y era el mapa: los
2 MB de `caaguazu.pmtiles` que viajan dentro del APK. Si en iOS hubiera
hecho falta un servidor de tiles, se caía la premisa central del proyecto
—mapa sin servicios externos— y con ella el sentido de compartir código.

**MapLibre iOS tiene soporte nativo de `pmtiles://` desde la 6.10.** El
recorte cruza sin tocarlo, sin una línea de Swift y sin servidor. Ese dato
es el que convirtió la opción en viable, y está verificado contra la fuente
del propio MapLibre, no supuesto.

---

## 2. Lo construido

Dos módulos nuevos. **La app Android no cambió de forma:** no depende de
ninguno de los dos hacia abajo, y no ve CMP ni `maplibre-compose` por
ningún lado. Eso es a propósito — hay un `.aab` a punto de entrar a Play y
no es momento de reacomodarle el grafo de dependencias.

### `:compartido` — el contrato, una sola vez

Kotlin puro: ni Android, ni interfaz, ni Compose más allá de `@Immutable`.

- `datos/Modelos.kt` — los modelos del contrato con el panel. **Salieron de
  `:app`, no se copiaron**: hay una sola definición y el paquete no cambió,
  así que para `:app` siguen estando donde estaban.
- `core/Json.kt` — el analizador tolerante.
- `core/Resultado.kt` — `Resultado`/`Falla`, el idioma de fallos del
  proyecto. `intentar` se quedó en `:app`, porque necesita `Registro`, que
  escribe a Logcat y a un archivo.
- `datos/Encuadre.kt` — centro y zoom del mapa. Estaban sólo del lado
  Android; una segunda copia en iOS se habría desviado de la primera sin
  que nadie se enterara.
- `ContratoCompartidoTest` — cinco pruebas que decodifican payloads reales
  del contrato. Corren en la JVM **y en el simulador de iOS**, que es el
  punto: la serialización no siempre se comporta igual fuera de la JVM, y
  descubrirlo en un teléfono sería tarde.

### `:ios` — la cáscara con el mapa

- `BaseMapaIos.kt` — resuelve el estilo con las rutas del bundle ya
  sustituidas. Es más simple que el lado Android: iOS lee el bundle como
  archivos reales, así que PMTiles hace lectura por posición ahí mismo y no
  hay que copiar nada al almacenamiento privado.
- `PantallaMapaIos.kt` — el mapa de Caaguazú, con el mismo estilo, el mismo
  recorte y el mismo encuadre que Android.
- `PuntoDeEntrada.kt` — `puntoDeEntrada(): UIViewController`, lo único que
  Xcode necesita llamar.

Dos detalles que valen la pena:

**La atribución.** `© OpenStreetMap` es obligatoria por la ODbL. La dibuja
el overlay que `MaplibreMap` pone por omisión, y el texto sale del campo
`attribution` del propio `estilo.json` — así se cumple la licencia sin que
aparezca un literal en el código, que es lo que el proyecto prohíbe. Con
una diferencia respecto de Android, que la muestra siempre visible: en iOS
queda detrás de un botón que se despliega. Igualarlas es trabajo pendiente.

**Los glifos.** El `estilo.json` referencia las tipografías como
`asset://map/glifos/...`, y `asset://` es un esquema que sólo entiende
MapLibre en Android. El lado iOS lo reemplaza por una ruta del bundle. Lo
limpio sería una marca `__RUTA_GLIFOS__` simétrica a la de los tiles, pero
eso obliga a tocar `estilo.json` y el lado Android, cuyo renderizado no se
puede comprobar en este entorno —no hay emulador— con la app a punto de
entrar a Play. Queda anotado como la corrección a hacer cuando haya con qué
verificarla.

---

## 3. Qué está verificado y qué no

Esto importa más que lo anterior, porque el proyecto tiene una regla
explícita contra dar por bueno lo que no se comprobó.

**Verificado, corriendo:**

- `:app` compila y sus 30 pruebas pasan con los modelos y el analizador ya
  movidos a `:compartido` — la app Android está intacta.
- Las 5 pruebas del contrato compartido pasan en la JVM.
- `maplibre-compose 0.17.0` publica `iosArm64` e `iosSimulatorArm64`, y
  **no** publica `iosX64`. Leído de su Gradle module metadata; por eso el
  simulador Intel queda afuera de los targets y no por preferencia.
- Las firmas de la API del mapa (`MaplibreMap`, `rememberMapState`,
  `BaseStyle.Json`, `CameraPosition`, `Position`) salieron de las fuentes
  publicadas del artefacto, no de memoria. En particular: `Position` es
  **(longitud, latitud)** —el orden de GeoJSON, no el que se dice en voz
  alta— y por eso el código usa argumentos nombrados.

**Verificado también, y esto es lo importante: el mapa se dibuja.**

![El mapa de Caaguazú en un iPhone simulado](imagenes/mapa-en-iphone.png)

Esa captura sale del workflow **"Captura de iOS"**, que arma el proyecto de
Xcode con XcodeGen, compila la app, arranca un simulador de iPhone, la corre
y fotografía. En ella se ve, y cada cosa comprueba algo distinto:

- **La red de calles, el agua y los nombres de lugar** — el `.pmtiles` de
  2 MB leyéndose del bundle, sin servidor de tiles.
- **Los nombres en Poppins** — los glifos resolvieron, o sea que la carpeta
  del mapa entró al bundle con su estructura intacta. Era el fallo silencioso
  que más preocupaba.
- **`© OpenStreetMap`** abajo a la derecha — la ODbL cumplida.
- **El pin oscuro al centro** — viene de `/mapa/markers`, traído por red con
  NSURLSession y decodificado con los modelos compartidos. El camino de datos
  completo, de punta a punta.

Lo que sigue sin comprobar: cómo se siente en un **iPhone de verdad**, con
los dedos. Un simulador no dice nada del tamaño real de los textos ni de si
el mapa se arrastra con soltura. Eso lo comprueba una persona.

**Lo que costó llegar hasta acá**, porque la próxima vez conviene saberlo: el
mapa compiló y arrancó mucho antes de que se pudiera ver, y cinco cosas
distintas lo tapaban. En orden: el simulador Intel, que no existe para este
framework; el `import` del framework, que faltaba y que además no podía
llamarse igual que la app; la clave `CADisableMinimumFrameDurationOnPhone`,
sin la cual Compose Multiplatform se niega a arrancar; los Compose Resources
de las dependencias, que no viajaban en el bundle; y un `NSLog` con un String
de Kotlin, que mataba el proceso justo al anotar la primera línea. Ninguna de
las cinco se veía sin correr la app de verdad.

---

## 4. Lo que falta

### En el código, por orden de lo que bloquea más

1. **`Textos`.** Todo texto visible sale de `Textos.t(...)`, y `Textos`
   todavía no cruzó. Hasta que cruce, el estado de error de la pantalla de
   iOS es una superficie vacía en lugar de un mensaje. Es el pendiente más
   visible del módulo, no un olvido.
2. **`Ajustes`, `Guardado`.** Los dos que quedan tocan preferencias del
   sistema y necesitan `expect`/`actual` con `NSUserDefaults`. `Cache`,
   `Http` y el enganche de `Registro` ya cruzaron.
3. **Las pantallas.** Son Compose y cruzan, pero hay que sacarlas de `:app`
   a un módulo compartido de interfaz, y ahí sí se tocan cosas: `Coil` es
   multiplataforma en la 3.x, los `ImageVector` cruzan tal cual, y el
   `HtmlSencillo` habrá que revisarlo.

### Fuera del código

- [ ] Cuenta de **Apple Developer Program** (USD 99/año) — y sin Mac no hay
      forma de subir un build, aunque el CI de Actions puede armarlo.
- [ ] Certificados de firma y perfiles de aprovisionamiento.
- [ ] Ficha de App Store Connect: ícono, capturas, descripciones, con las
      medidas de Apple. Texto de producto: lo escribe una persona.
- [ ] **App Privacy ("nutrition label")**: con la misma auditoría que la de
      Play, la respuesta es "Data Not Collected" en casi todo.

---

## 5. Las dos preguntas, ya decididas

**El cliente HTTP: `expect`/`actual` sobre NSURLSession**, sin dependencia
nueva. La medida que decidió: `Http` son 131 líneas y lo atado a la JVM era
**una** función privada de unas 35 (`pedir()`). El ETag con 304, el
reintento corto y la caída a la copia guardada son Kotlin puro y se
comparten. Agregar Ktor para no duplicar 35 líneas era un mal canje.

**Los avisos: fuera de la primera versión de iOS.** El equivalente de
WorkManager en iOS, `BGAppRefreshTask`, es explícitamente *best-effort*: el
sistema decide si corre y cuándo. Sale sin avisos y sin el interruptor,
antes que con uno que promete lo que no puede cumplir. No se toca el "no hay
push".

---

## 6. Cerrado en la revisión del 2026-09-18

Tres cosas que el camino a la App Store necesitaba y que no estaban:

- **El ícono.** No había ninguno: la app se instalaba con el cuadrado gris
  de iOS, y App Store Connect rechaza una subida sin él. Se agregó
  `ios/xcode/Recursos/Assets.xcassets`, con un PNG de 1024×1024 opaco y sin
  redondear (con canal alfa es `ITMS-90717`), generado del mismo vector que
  el de Android. El workflow comprueba que `actool` dejó los derivados
  dentro del `.app`.
- **No se podía compilar para un teléfono.** Dos causas, las dos en
  `project.yml`: `CODE_SIGNING_ALLOWED: NO` estaba en `settings.base`, o sea
  para todas las configuraciones y no solo para el simulador, así que un
  archivado salía sin firmar; y el framework de Kotlin se buscaba en una
  única ruta fija, `iosSimulatorArm64/debugFramework`. Ahora la ruta va por
  SDK y por configuración —cuatro combinaciones— y el enlace se hace con
  `-framework TurismoKit` en `OTHER_LDFLAGS`, porque una dependencia
  declarada no puede ser condicional. El workflow compila también con
  `-sdk iphoneos` y comprueba con `lipo` que el binario es arm64 de
  teléfono: es la mitad del camino que antes nadie miraba.
- **`ITSAppUsesNonExemptEncryption: false`** en el Info.plist. La app solo
  usa HTTPS, que es un uso exento de la normativa de exportación; sin la
  clave, App Store Connect pregunta por el cifrado en cada subida.

Lo que sigue sin comprobarse, y hay que decirlo: **nada de esto se archivó
ni se subió de verdad**. Que compile y enlace para `iphoneos` sin firmar es
lo más lejos que llega un runner sin cuenta de desarrollador. Firmar,
archivar y que App Store Connect acepte el paquete son tres pasos que
necesitan la cuenta y una Mac.
