# Hoja de traspaso — para seguir este proyecto en local

Esto lo escribió la instancia de Claude Code que trabajó el proyecto en la nube,
para la instancia que lo siga en una Mac. Está pensado para leerse de una vez
antes de tocar nada.

**Fecha:** 2026-09-30 · **Rama:** `claude/apk-programming-wczoo7` · **Último
commit:** `0025546` · **Repo:** `github.com/NasastaXD/Turismo-app-czu`

---

## 1. Qué es esto y en qué estado está

App Android **y iOS** de turismo del distrito de Caaguazú, Paraguay. Cliente de
un backend de WordPress que ya existe y funciona (`caaguazu.net`, API
`/wp-json/czu-app/v1/`). La app no tiene servidor propio, no tiene cuenta de
usuario, y no manda nada de nadie a ninguna parte.

**Las dos apps son la misma app.** Hay un módulo `:interfaz` con el sistema
visual y todas las pantallas, compartido por las dos plataformas, y dos cáscaras
mínimas.

| Qué | Estado |
|---|---|
| Android | Listo para cargar a Play. El `.aab` se compila firmado desde Actions. |
| iOS | Compila, enlaza, **corre y dibuja** en el simulador, y compila para teléfono. |
| Pruebas | 63 en verde: 33 `:compartido`, 19 `:interfaz`, 11 `:app` |
| Nunca se vio | La app en un teléfono **real**, de ninguna de las dos. |

`docs/imagenes/inicio-en-iphone.png` es la captura de la app corriendo en el
simulador de iOS, sacada por el CI. `docs/imagenes/mapa-en-iphone.png` es la
del mapa.

---

## 2. Lo primero que hay que leer

**`CLAUDE.md`, entero, antes de escribir una línea.** No es un readme: es el
criterio con el que se toman las decisiones acá, y tiene reglas que el código
hace cumplir automáticamente. Las cuatro que más importan:

1. **Ningún texto visible vive en el código.** Todo sale de `Textos.t("clave")`.
   `SinRedaccionTest` **falla la compilación** si aparece un literal visible.
2. **Un agente no escribe contenido de producto.** Como mucho un título o tres
   palabras. Ni artículos, ni descripciones, ni copy de relleno.
3. **Ningún color, radio o elevación suelto.** Salen de `Tono`, `Radio`,
   `Elevacion`, `Medida`. `SistemaDeDisenoTest` falla si no.
4. **No se agrega una dependencia que se pueda evitar.** Hay trece, y la cuenta
   se cuida a mano.

Después, en este orden: `docs/ios.md` (arquitectura y decisiones de iOS),
`docs/compilar-en-mac.md` (los comandos, paso a paso),
`docs/publicar-en-tiendas.md` (qué falta para las tiendas).

---

## 3. La estructura, y por qué

```
:compartido    el contrato con el panel, la red, la caché, el disco.
               Kotlin puro. Sin Compose de interfaz.
                      ▲
:interfaz      el sistema visual y TODAS las pantallas, una sola vez.
               Compose Multiplatform. 41 archivos comunes + 10 por plataforma.
               ▲                                    ▲
:app                                          :ios
5 archivos: Activity, Application,            1 archivo: puntoDeEntrada().
registro a Logcat, avisos (WorkManager).
```

**Las dos cáscaras son chicas a propósito.** Si una empieza a crecer, es que
algo que debería compartirse se está escribiendo dos veces.

Que las pantallas cruzaran casi sin tocarlas no fue suerte: el proyecto decidió
hace tiempo **no usar Material3** y dibujar su propio sistema sobre
`compose.foundation`. Por eso nunca estuvieron atadas a Android. **No
introduzcas Material3**: rompería esa propiedad y sumaría peso.

### Lo que sí necesita `expect`/`actual`

Cada uno en su archivo `.android.kt` / `.ios.kt` al lado del `expect`:

| Qué | Android | iOS |
|---|---|---|
| `Preferencias` | `SharedPreferences` | `NSUserDefaults` |
| `Empaquetado` (textos de respaldo) | assets del APK | el bundle |
| `Archivos` (en `:compartido`) | `java.io.File` | `NSFileManager` |
| `Http.pedir` (en `:compartido`) | `HttpURLConnection` | `NSURLSession` |
| `Sistema.*` | intents | UIKit / un `.ics` |
| `Sans` / `Serif` | `R.font` | los mismos `.ttf`, del bundle |
| `LienzoMapa` | `MapView` en `AndroidView` | `maplibre-compose` |
| `estiloDelMapa` | copia el `.pmtiles` a disco | lee el bundle |
| `ManejarVolver` | `BackHandler` | nada: iOS no tiene |
| `FilaDeAvisos` | el interruptor | nada: sin avisos en la v1 |

### Los puntos de enganche, y por qué no son interfaces

`Bitacora.destino`, `RegistroVisible`, `TrabajoDeAvisos`, `Entorno`. Son
propiedades de función que cada cáscara rellena al arrancar. **No los conviertas
en interfaces**: el proyecto prohíbe explícitamente una interfaz con una sola
implementación, y estos existen justamente para que el código compartido no
tenga que recibir un `Context` de Android por parámetro.

---

## 4. Los comandos que funcionan

```sh
# Android: lo que hay que correr antes de cada commit.
./gradlew :compartido:testDebugUnitTest :interfaz:testDebugUnitTest :app:testDebugUnitTest

# Android: el APK de release (cae a firma de depuración sin el keystore).
./gradlew :app:assembleRelease

# Android: el .aab para Play. La propiedad NO es opcional.
./gradlew :app:bundleRelease -PparaTienda=true

# iOS: la biblioteca que consume Xcode. La primera vez tarda 10-20 min.
./gradlew :ios:linkDebugFrameworkIosSimulatorArm64

# iOS: generar el proyecto de Xcode (requiere `brew install xcodegen`).
cd ios/xcode && xcodegen generate && open Turismo.xcodeproj
```

**El `.xcodeproj` no está en el repositorio a propósito.** Se genera de
`ios/xcode/project.yml`. Si necesitás cambiar algo del proyecto de Xcode,
**cambialo en el YAML y regenerá** — un cambio hecho en Xcode se pierde en la
próxima generación.

`docs/compilar-en-mac.md` tiene esto mismo con los requisitos previos y una
sección de "cuando algo falle".

---

## 5. Las trampas, aprendidas a golpes

Cada una de estas costó al menos una vuelta de CI en un Mac. Están acá para que
no las pagues de nuevo.

1. **La Mac tiene que ser de chip Apple.** `maplibre-compose` no publica la
   variante `iosX64`, así que en una Mac Intel esto no compila. No hay arreglo.
2. **Los nombres de prueba de `commonTest` no llevan coma ni punto.**
   Kotlin/Native los rechaza ("Name contains illegal characters"); la JVM los
   acepta. Pasan en Linux y rompen al compilar para iOS.
3. **`NSLog` mata el proceso.** Un `String` de Kotlin pasado como vararg de
   Objective-C no se convierte a `NSString`: `NSLog` le pregunta
   `respondsToSelector` y el proceso muere con `EXC_BAD_ACCESS`. Usá `println`.
   Está documentado entero en `ios/.../PuntoDeEntrada.kt`.
4. **`Dispatchers.IO` no existe en Kotlin/Native.** Por eso hay
   `despachadorIo` con `expect`/`actual` en `:compartido`.
5. **`pmtiles://` exige una URL completa detrás.** Sin el prefijo `file://`,
   MapLibre no resuelve el archivo y el mapa queda con el fondo plano y ninguna
   capa encima, sin avisar.
6. **Las carpetas del bundle de iOS van como *referencia de carpeta*, no como
   grupo.** `map/`, `textos/` y `font/`. Si Xcode aplana la estructura: el mapa
   dibuja sin una sola etiqueta, los textos salen entre ‹angulitos› y la
   tipografía es la del sistema. Nada avisa.
7. **`CADisableMinimumFrameDurationOnPhone` tiene que estar en el Info.plist.**
   Compose Multiplatform lo comprueba al arrancar y lanza si falta.
8. **Los filtros de `paths:` de los workflows de iOS incluyen `interfaz/**`.**
   Si agregás un módulo, agregalo ahí también — si no, tus arreglos no disparan
   ninguna corrida y el fallo aparece atribuido a un commit que no lo causó.
9. **Un aviso abierto que NO se tocó:** `coil-core` trae `skiko:0.9.4` y Compose
   lo sube a `0.150.1`. Compila y es sólo un aviso. **Si las fotos se dibujan en
   Android y no en iPhone, ese es el primer sospechoso.**

---

## 6. Decisiones tomadas que no conviene revertir sin hablarlo

- **Coil en iOS no usa Ktor.** Hay un `NetworkClient` propio de ~50 líneas sobre
  `NSURLSession` (`interfaz/src/iosMain/.../RedDeImagenes.ios.kt`). La interfaz
  de Coil que hay que implementar es **un solo método**; verificado en el
  artefacto. Lo alternativo eran dos dependencias nuevas.
- **Las tipografías no usan Compose Resources.** `Font(identity, getData = {…})`
  existe en el artefacto de iOS de Compose y carga los bytes de forma diferida,
  así que `Sans` y `Serif` son un `val` simple y `Letra` no cambió.
- **Los avisos quedan fuera de la v1 de iOS.** `BGAppRefreshTask` es
  explícitamente *best-effort*; sale sin avisos antes que con un interruptor que
  promete lo que no puede cumplir. **No hay push, no hay Firebase.**
- **Agendar en iOS es un `.ics` a la hoja de compartir**, no EventKit: así no se
  pide **ningún** permiso, que es la misma decisión que tomó Android por otro
  camino.
- **Los archivos están una sola vez.** Tipografías, textos de respaldo y mapa
  viven en `interfaz/src/androidMain/` y el `project.yml` referencia esas mismas
  carpetas. Dos copias serían dos apps que se desincronizan sin aviso.
- **El precio se filtra en el teléfono** porque el contrato no tiene parámetro.
  Cuando el panel lo agregue, se cambia por el parámetro y se borra el filtro.

---

## 7. Lo que el dueño del proyecto ya decidió

- **El keystore de release no se rota.** Quedó expuesto en el historial de git en
  agosto y se planteó rotarlo y purgar el historial; **dijo que no**. No
  reabrir el tema sin que él lo pida.
- **El PR #28 queda en draft, sin fusionar.** Todo el trabajo se apila en
  `claude/apk-programming-wczoo7`. `main` sólo tiene LICENSE y README.
- **Los textos de producto los escribe él.** Nombre en la tienda, descripción,
  palabras clave. `docs/textos-para-completar.md` son los huecos.

---

## 8. Qué sigue, por orden

1. **Verla en el iPhone.** Es lo único que ningún CI puede decir. Instalala
   (`docs/compilar-en-mac.md`, paso 6) y mirá: si los textos se cortan, si las
   fotos cargan, si el mapa se mueve bien, si la ficha se siente bien al
   desplazarla. Ahí van a aparecer cosas.
2. **Igualar lo que quede distinto** entre las dos. Lo conocido: en iOS no hay
   gesto de volver del sistema (se navega con el botón de la cabecera) y no hay
   avisos.
3. **El registro a disco en iOS.** Hoy son las últimas 500 líneas en memoria;
   en Android hay un archivo rotativo que la pantalla de diagnóstico exporta.
4. **Cargar Android a Play.** No falta nada de código: cuenta, ficha, textos,
   fotos y formularios. Está todo en `docs/guia/` (hay un PDF de 18 páginas en
   lenguaje sencillo, para él, no para vos).
5. **Recién después, la App Store.** Y ojo con esto: **Apple rechaza apps que
   hacen muy poco** (regla de *funcionalidad mínima*). Conviene que la vea
   gente en TestFlight antes de mandarla.

---

## 9. Cómo se verifica algo acá

El proyecto tiene una regla explícita: **se dice lo que no está verificado.**

- Android compila y las pruebas corren en cualquier máquina.
- **iOS sólo compila en macOS.** En la nube eso lo hacía el CI
  (`.github/workflows/verificar-ios.yml`, ~2 min con caché, y
  `captura-ios.yml`, que arranca un simulador y saca la captura). **En local ya
  no los necesitás para el ciclo normal**, pero siguen siendo la red de
  seguridad del repositorio: no los borres.
- Cuatro guardianes corren con las pruebas y **fallan la compilación**:
  `SinRedaccionTest`, `ClavesDeTextoTest`, `SistemaDeDisenoTest` y
  `NavegacionTest`. Ahora recorren los cinco juegos de fuentes del proyecto,
  incluido el código de iOS.
- Si agregás una prueba a `:compartido/commonTest`, corre también en el
  simulador de iOS. Aprovechalo: es donde conviene poner cualquier cosa que
  tenga que dar el mismo resultado en los dos teléfonos.

---

## 10. Una nota sobre cómo trabajar acá

El código de este proyecto está comentado explicando **por qué**, no qué. Los
comentarios largos no son ruido: cada uno suele ser una vuelta de CI perdida o
un fallo que se vio en un teléfono. Si vas a cambiar algo que tiene un
comentario así, leelo primero — probablemente explica por qué la forma obvia no
funciona.

Y si algo de `CLAUDE.md` estorba, la regla es discutirlo y cambiar el documento.
Lo que no se hace es ignorarlo en silencio.
