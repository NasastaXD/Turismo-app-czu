# Qué falta para publicar — Play Store y App Store

Estado a partir de una auditoría del código el 2026-09-16, revisada el
2026-09-18. Se actualiza a mano cuando algo de esta lista se resuelve.

---

## Android (Google Play)

### Ya resuelto en el código

- **El bundle (`.aab`) ya compila.** Los splits por ABI (pensados para las
  APK sueltas de GitHub Releases) rompían la generación del bundle — AGP no
  deja combinar las dos cosas. Se separó con la propiedad `paraTienda`:
  `./gradlew :app:bundleRelease -PparaTienda=true` arma el `.aab` sin
  splits; `./gradlew :app:assembleRelease` (sin la propiedad) sigue armando
  las tres APK de siempre para GitHub Releases, sin cambios.
- **Workflow nuevo: "Publicar bundle (Play Store)".** Se dispara a mano
  desde Actions, compila el `.aab` firmado con la misma clave de release, y
  lo deja como artefacto descargable (no como release pública: la primera
  carga a Play Console siempre es manual).
- **Firma de release estable**, en uso desde la v1.3.0.
- **`targetSdk`/`compileSdk` 36**, al día con lo que exige Play.
- **arm64-v8a incluido** (Play exige 64 bits).
- **R8 + `mapping.txt` adjunto** a cada compilación — necesario para leer
  los reportes de fallos que llegan desde Play Console.
- **`network_security_config.xml` nuevo**: bloquea tráfico sin cifrar. La
  app ya solo hablaba por HTTPS; esto lo deja explícito para el escáner de
  Play.
- **Ícono adaptable real** (no el de ejemplo de Android Studio): un pin de
  mapa, coherente con el resto de la iconografía.

### Cerrado en la revisión del 2026-09-18

- **El ícono faltaba en Android 7.0 y 7.1.** Solo existía
  `mipmap-anydpi-v26`, y ese calificador excluye por completo a API 24 y 25,
  que entran por `minSdk`. En esos teléfonos no había ninguna configuración
  que coincidiera y el sistema mostraba el ícono gris por omisión. Se
  agregaron los cinco PNG de densidad, generados del mismo vector; ver
  `docs/iconos.md`. Comprobado en el APK compilado: `mipmap/ic_launcher`
  ahora tiene seis configuraciones (`mdpi` a `xxxhdpi` más `anydpi-v26`),
  leído con `aapt2 dump resources`.
- **Los dos permisos de ubicación se quitaron de la fusión.** MapLibre los
  declara en su propio manifiesto para su componente de "mi posición", que
  la app no usa — comprobado que ningún archivo toca `locationComponent` ni
  `LocationEngine`. Quedaban listados en la ficha de Play como "Ubicación" y
  obligaban a declararlos en seguridad de datos. Con `tools:node="remove"`
  desaparecen: comprobado con `aapt2 dump badging` sobre el APK nuevo.
- **`-PparaTienda=false` apagaba los splits igual que `=true`.** Era
  `hasProperty`, que solo mira si la propiedad existe. Ahora se lee el valor.
  No afectaba a ningún workflow —los dos pasan `=true`— pero era una trampa
  puesta para quien viniera después.
- **Queda dicho, sin cambiar nada:** `coil-network-okhttp` arrastra OkHttp y
  Okio al APK. `CLAUDE.md` dice "sin cliente HTTP externo", y para la API es
  cierto —`Http` es `HttpURLConnection`—, pero las imágenes sí traen uno por
  transitividad. Coil 3.2.0 no publica un motor sobre `HttpURLConnection`
  (`coil-network-android` quedó en `3.0.0-alpha02`), así que sacarlo
  significa escribir el motor a mano: unas 40 líneas. Es una decisión del
  dueño, no un bug, y no se toca sin que se pida.

### Pendiente, y es una decisión, no un bug

- **Rotar el keystore de release.** Sigue siendo el mismo que quedó
  expuesto en el historial de git en agosto (`Document.txt` +
  `release.keystore` subidos y borrados después, pero recuperables del
  historial de un repo público) — en su momento se decidió no actuar sobre
  esto. Ahora que se viene una carga real a Play, conviene reconsiderarlo:
  **Play exige "Play App Signing"** para todo bundle nuevo — Google guarda
  la clave real y firma lo que le llega a cada usuario; lo que se sube acá
  es solo una "clave de carga", y si esa se pierde o se filtra, Play tiene
  un proceso de recuperación sin perder la app. Es decir: generar un
  keystore nuevo ahora **no cuesta nada** (no hay ninguna instalación real
  desde una tienda que dependa de seguir con el actual) y de paso cierra el
  tema pendiente. Si se quiere, lo genero y lo mando aparte, como la vez
  pasada.

### Fuera del código — hay que hacerlo desde las cuentas

- [ ] Cuenta de **Google Play Console** (pago único, USD 25).
- [ ] Cargar el **`.aab`** (del workflow nuevo) y **habilitar Play App
      Signing** en el flujo de carga.
- [ ] **Ficha de la tienda**: título, descripción corta, descripción larga,
      ícono de 512×512, gráfico de funciones (1024×500), capturas de
      pantalla (mínimo 2, por cada tipo de dispositivo que se declare). Todo
      esto es texto de producto — lo tiene que escribir una persona, no yo.
- [ ] **Política de privacidad**, alojada en una URL pública. Dejé un
      borrador técnico en `docs/privacidad-borrador.md`, armado desde una
      auditoría real de qué guarda la app (nada del lado del servidor, nada
      de analítica) — falta completar los datos de contacto, que alguien
      con criterio legal lo revise, y subirlo a algún lugar público (podría
      ser una página más de caaguazu.net).
- [ ] **Formulario de Data Safety.** Con lo que audité, la respuesta
      honesta es **"No collects any user data"** en todas las categorías:
      no hay cuentas, no hay analítica, no hay publicidad, y todo lo que se
      guarda queda en el teléfono sin subirse a ningún lado.
- [ ] **Clasificación de contenido** (cuestionario IARC dentro de la
      consola): es una app de información turística sin contenido para
      adultos, sin interacción entre usuarios y sin compras — debería salir
      con la clasificación más baja en todos los sistemas.
- [ ] **Testing cerrado previo**: Play exige un período de prueba cerrada
      antes de la publicación general para cuentas nuevas de desarrollador
      — es un requisito de proceso de la cuenta, no algo que se resuelva
      en el código.

---

## iOS (App Store)

**La decisión está tomada y hay código.** El detalle completo —la
investigación, lo construido, lo verificado y lo que falta— está en
[`docs/ios.md`](ios.md). El resumen:

Se eligió **Compose Multiplatform**: se comparten datos, contrato *y*
pantallas. Lo que lo hizo posible fue una decisión que ya estaba tomada en
este proyecto por otro motivo: **no hay Material3**, así que el sistema
visual es `compose.foundation`/`compose.ui` puro y cruza sin despegarse de
nada atado a Android.

Lo que decidió la viabilidad fue el mapa: **MapLibre iOS lee `pmtiles://`
de forma nativa desde la 6.10**, así que los 2 MB embebidos cruzan sin
servidor y sin una línea de Swift. Si eso no hubiera existido, se caía la
premisa central del proyecto y con ella el sentido de compartir código.

Hay dos módulos nuevos, `:compartido` (el contrato, ya compartido de verdad
con `:app`) y `:ios` (la cáscara con el mapa), y un workflow **"Verificar
iOS"** en un runner macOS, que es la única forma de comprobar que compilan:
Kotlin/Native no cruza a iOS desde Linux.

**La app Android no cambió de forma** y no ve Compose Multiplatform por
ningún lado. Eso es deliberado: hay un `.aab` a punto de entrar a Play.

Las dos preguntas que estaban abiertas se cerraron: **HTTP en iOS va sobre
NSURLSession** con `expect`/`actual` (el ETag, el reintento corto y la caída
a la copia guardada son Kotlin puro y se comparten), y **los avisos quedan
fuera de la primera versión de iOS**.

El proyecto de Xcode existe, y como especificación: `ios/xcode/project.yml`,
que XcodeGen convierte en `.xcodeproj` en un comando. Eso es lo que permite
armarlo en un runner y lo que hace realista compilar sin una Mac propia. El
workflow **"Captura de iOS"** arranca un simulador y saca la foto del mapa
dibujado con sus pines — está en `docs/imagenes/mapa-en-iphone.png`.

Lo que falta para poder mandar algo a Apple es **`Textos` y las pantallas**.
Hoy la app de iOS es el mapa, y una app que hace tan poco cae justo en la
regla de *funcionalidad mínima*, que es de las que Apple más usa para
rechazar. No es un problema técnico: es que todavía no está terminada.

Cerrado en la revisión del 2026-09-18, del lado de iOS:

- **El ícono no existía.** App Store Connect rechaza una subida sin él
  (`ITMS-90717` si además tiene canal alfa). Se agregó el catálogo con el
  PNG de 1024×1024, opaco y sin redondear, del mismo vector que el de
  Android. El workflow comprueba que `actool` lo dejó dentro del `.app`.
- **No se podía compilar para un teléfono**, que es el único camino a la
  tienda: la firma estaba apagada para todas las configuraciones y no solo
  para el simulador, y el framework de Kotlin se buscaba únicamente en la
  carpeta que usa el simulador en Debug. Ahora las rutas van por SDK y por
  configuración, y el workflow compila también con `-sdk iphoneos` para que
  no vuelva a romperse sin que nadie se entere.
- **`ITSAppUsesNonExemptEncryption: false`** declarado en el Info.plist: la
  app solo usa HTTPS, que es un uso exento. Sin esa clave, App Store Connect
  pregunta por el cifrado en cada subida.

**Además, aparte del código:**

- [ ] Cuenta de **Apple Developer Program** (USD 99/año).
- [ ] Certificados de firma y perfiles de aprovisionamiento (Xcode).
- [ ] Todo lo de la ficha de la tienda, otra vez: ícono, capturas,
      descripciones — con las medidas propias de App Store Connect.
- [ ] La misma política de privacidad de arriba sirve para las dos tiendas.
- [ ] **App Privacy ("nutrition label")** de Apple: con la misma auditoría,
      la respuesta es "Data Not Collected" en la mayoría de las
      categorías.

---

## Resumen para decidir hoy

- **Android**: technically listo para cargar a Play Console. Lo que falta
  es todo lo que no es código — cuenta, ficha de tienda, y la decisión
  sobre rotar el keystore.
- **iOS**: el camino está elegido (Compose Multiplatform) y el contrato
  ya se comparte de verdad con la app Android. Falta la mayor parte del
  trabajo, y sobre todo un Mac: el proyecto de Xcode no se puede generar
  desde acá. Antes de seguir conviene decidir las dos preguntas abiertas de
  `docs/ios.md` — HTTP y avisos.
