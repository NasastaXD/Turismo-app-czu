# Qué falta para publicar — Play Store y App Store

Estado a partir de una auditoría del código el 2026-09-16. Se actualiza a
mano cuando algo de esta lista se resuelve.

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

**No hay ninguna app de iOS en este repositorio.** Todo el proyecto es
Android nativo (Kotlin + Jetpack Compose, MapLibre Android, WorkManager) —
nada de eso corre en iOS tal cual. Publicar en el App Store no es una
casilla más de esta lista: es un proyecto nuevo.

Antes de que tenga sentido preparar nada de código, hay una decisión de
arquitectura que no me corresponde tomar sola:

1. **Nativo en Swift/SwiftUI, de cero.** Máxima calidad y aprovecha
   MapLibre iOS (existe, con soporte de PMTiles también), pero es rehacer
   las nueve pantallas y toda la capa de datos en otro lenguaje.
2. **Kotlin Multiplatform (KMP)**: mover la capa de datos y la lógica que
   ya existe (modelos, `ApiHttp`, `Http`, `Ajustes`, etc.) a un módulo
   compartido, y escribir la interfaz de iOS en SwiftUI aparte. Reutiliza
   lo que menos cambia (el contrato con el panel) y no toca el sistema
   visual, que es Compose puro y no se comparte.
3. **Un framework cruzado** (Flutter, React Native): reescribe todo de
   cero en otra base, sin reutilizar nada de este repo, y choca de frente
   con varias decisiones ya tomadas acá (sin Material3, mapa vectorial
   embebido con soporte nativo de pmtiles, R8/ProGuard propio).

Dado cómo está armado este proyecto — separación limpia entre datos y UI,
"un solo modelo por entidad", sin capas de por medio — la opción 2 es la
que menos trabajo tira a la basura. Pero es una decisión de meses de
trabajo y de stack, no algo para arrancar sin que alguien la tome a
propósito.

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
- **iOS**: no hay para qué preparar código todavía. Lo que hace falta
  primero es elegir el camino (nativo, KMP, o cruzado) antes de escribir
  una sola línea.
