# Publicar en la App Store

Lo que hace el repositorio, lo que hay que hacer a mano, y en qué orden. Para
Google Play el documento equivalente es
[`docs/publicar-en-tiendas.md`](publicar-en-tiendas.md).

> **Los pasos para hacerlo, desde Windows y sin Mac, están en
> [`docs/app-store/GUIA.md`](app-store/GUIA.md).** Este documento es el detalle
> técnico de qué hace el workflow y por qué; la guía es el orden en que se
> hace, con el script de firma para Windows, la ficha campo por campo
> ([`ficha.md`](app-store/ficha.md)) y las páginas de privacidad y soporte
> listas para publicar ([`sitio/`](app-store/sitio/)).

Una advertencia que vale para todo el documento: **el camino firmado no está
verificado**. No hay cuenta de Apple Developer todavía, y un runner sin cuenta
no puede firmar. Lo que sí está comprobado en CI es todo lo anterior a la firma,
que es la mayor parte. Dónde termina lo comprobado y empieza lo que hay que
confirmar está marcado en cada sección.

---

## 1. Lo que ya hace el repositorio

El workflow **"Publicar iOS (App Store)"** se dispara a mano desde Actions y
hace, en una sola corrida:

1. Las tres suites de pruebas, las mismas que exige un release de Android
   —incluidos los cuatro guardianes de `:interfaz`.
2. Las pruebas del contrato **corriendo en el simulador**, no en la JVM: la
   serialización no siempre se comporta igual en Kotlin/Native.
3. El framework de Kotlin en **Release** para teléfono (`iosArm64`).
4. El proyecto de Xcode, generado con XcodeGen desde `ios/xcode/project.yml`.
5. El **archivado en Release** (`.xcarchive`).
6. Siete comprobaciones sobre lo que quedó adentro del `.app`.
7. El `.ipa`, **sólo si están cargados los secretos de firma**.
8. La **subida a App Store Connect**, si además está la clave de la API. Es lo
   que permite publicar sin una Mac: Transporter y el Organizador de Xcode solo
   existen en macOS.

Antes de todo eso elige **Xcode 26**, que Apple exige para cualquier subida
desde el 28 de abril de 2026, y después del archivado lee los símbolos del
binario y falla si usa una API "con motivo obligatorio" que
`PrivacyInfo.xcprivacy` no declara (sin eso, el aviso llega como ITMS-91053 por
correo, después de subir).

Sin los secretos no falla: archiva sin firmar, comprueba todo lo demás y sube
el `.xcarchive`. Eso ya sirve para algo —prueba que Release arma— y es lo que
hoy corre.

### Las siete comprobaciones

No son decorativas: cada una existe porque es un fallo que de otro modo se
descubre después de subir, o peor, en el teléfono de alguien.

| Qué comprueba | Por qué, si falla, no se ve |
|---|---|
| `map/caaguazu.pmtiles` y `map/estilo.json` | sin ellos el mapa queda en blanco |
| `map/glifos/` con `.pbf` adentro | si Xcode aplana la carpeta, el mapa dibuja **sin una sola etiqueta** |
| `textos/{es,en,pt}.json` | aplanados, la app arranca con todas las claves entre ‹ángulos› |
| `font/*.ttf` | sin ellas dibuja con la tipografía del sistema y deja de ser esta app |
| el ícono compilado | App Store Connect rechaza la subida (`ITMS-90717`) |
| la versión del `Info.plist` | que sea la de `version.properties` y no un literal viejo |
| `lipo` arm64 y **sin** `x86_64` | una porción de simulador colada es un rechazo |

Más `PrivacyInfo.xcprivacy`, que Apple exige desde 2024.

La de la versión existe por un error real: iOS declaraba `0.1.0` mientras
Android iba en `1.6.0`. Hoy el número vive en `version.properties`, lo leen los
dos, y "Verificar iOS" falla si `project.yml` se desincroniza.

---

## 2. Los secretos

Siete. Los cuatro de firma de abajo, y tres para subir: `ASC_KEY_ID`,
`ASC_ISSUER_ID` y `ASC_KEY_P8`, de una clave de la API de App Store Connect con
rol *App Manager*. Sin esos tres, el `.ipa` queda como artefacto y no se sube.
Desde Windows se arman y se cargan todos con
[`app-store/preparar-firma.ps1`](app-store/preparar-firma.ps1); los pasos están
en la guía.

### Los cuatro de firma

Hay que cargarlos en **Settings → Secrets and variables → Actions** del
repositorio. Mientras falte alguno, el workflow archiva sin firmar y lo dice en
el resumen de la corrida.

| Secreto | Qué es |
|---|---|
| `IOS_TEAM_ID` | los diez caracteres del equipo, arriba a la derecha en developer.apple.com |
| `IOS_CERTIFICADO_P12_BASE64` | el certificado de distribución **con su clave privada**, exportado como `.p12` y pasado a base64 |
| `IOS_CERTIFICADO_CLAVE` | la contraseña que se le puso al exportar el `.p12` |
| `IOS_PERFIL_BASE64` | el perfil de aprovisionamiento de App Store (`.mobileprovision`) en base64 |

### Cómo sacarlos, si hay una Mac a mano

El certificado y el perfil se crean una vez en
`developer.apple.com/account/resources`:

- **Certificates** → `+` → *Apple Distribution*. Pide un CSR, que se genera con
  Acceso a Llaveros (*Asistente de certificados → Solicitar un certificado de
  una autoridad*). Se descarga, se abre —entra al llavero— y desde ahí se
  exporta como `.p12` **con la clave privada**, poniéndole una contraseña.
- **Identifiers** → `+` → *App ID* → `net.caaguazu.turismo`. Tiene que existir
  antes del perfil y antes del registro en App Store Connect.
- **Profiles** → `+` → *App Store Connect* (distribución) → ese App ID → ese
  certificado. Se descarga el `.mobileprovision`.

Y después, a base64:

```sh
base64 -i Certificados.p12          | pbcopy   # → IOS_CERTIFICADO_P12_BASE64
base64 -i Turismo_AppStore.mobileprovision | pbcopy   # → IOS_PERFIL_BASE64
```

En macOS `base64` no corta las líneas, así que sale en una sola. Si se usa otra
herramienta que sí las corte, hay que pegarlo igual de corrido.

> El nombre del perfil **no** se escribe en ningún secreto: el workflow lo lee
> del propio `.mobileprovision` con `security cms` y `PlistBuddy`. Un nombre
> escrito a mano falla recién al exportar, con un mensaje que habla de otra cosa.

**No verificado:** que estos cuatro valores produzcan un `.ipa` que App Store
Connect acepte. El procedimiento es el estándar de Xcode, pero hasta que exista
la cuenta no hay forma de comprobarlo acá.

---

## 3. El orden de las cosas en App Store Connect

El orden importa: cada paso necesita el anterior y saltearse uno da errores que
no explican la causa.

1. **La cuenta.** Apple Developer Program, USD 99 al año. La titularidad tiene
   que ser de una persona mayor o de la entidad; una cuenta de tipo
   *Organization* además admite equipo con roles, y una *Individual*
   históricamente no. Conviene preguntarlo antes de inscribirse, junto con la
   **exención de la cuota** para entidades públicas y educativas: si la app va a
   nombre de la municipalidad o la gobernación, puede que no se pague.
2. **El App ID** (`net.caaguazu.turismo`), en el portal de desarrolladores.
3. **El registro de la app** en App Store Connect: nombre, idioma principal, ese
   bundle ID y un SKU interno.
4. **Certificado y perfil**, los de la sección anterior.
5. **La primera subida.** La hace el workflow, con la clave de la API. Si no
   estuviera cargada, desde una Mac hay dos caminos sobre lo que deja:
   - **Transporter** (gratis, en la Mac App Store): se arrastra el `.ipa`.
   - **Organizador de Xcode**: se abre el `.xcarchive` —por eso el workflow lo
     sube siempre— y se distribuye desde ahí, sin recompilar.
6. **TestFlight** antes de enviar a revisión. Es gratis, no pasa por revisión
   para pruebas internas, y es la primera vez que la app se va a ver en un
   teléfono de verdad. Ese paso no se debería saltear: hoy **nadie vio esta app
   en un iPhone físico**, ni iOS ni Android.
7. **Enviar a revisión.**

### Qué hay que completar en la ficha, y quién

Lo que el código ya resuelve:

- **Cifrado para exportación**: `ITSAppUsesNonExemptEncryption: false` está
  declarado en el `Info.plist`. Sin eso, App Store Connect pregunta en cada
  subida.
- **Manifiesto de privacidad**: `PrivacyInfo.xcprivacy` viaja dentro.

Lo que es trabajo de una persona:

- [ ] **Capturas.** Ver la sección 4.
- [ ] **Nombre, subtítulo, descripción y novedades.** Es texto de producto y en
      este proyecto **lo escribe una persona**, nunca un agente. Los huecos,
      con su límite de caracteres, están en
      [`docs/app-store/ficha.md`](app-store/ficha.md).
- [ ] **Categoría** y el cuestionario de **clasificación por edad**.
- [ ] **URL de política de privacidad**, obligatoria. El borrador está en
      [`docs/privacidad-borrador.md`](privacidad-borrador.md) y hay que
      publicarlo en una URL de `caaguazu.net`.
- [ ] **App Privacy** (la "etiqueta nutricional"): **no** es *Data Not
      Collected*, por el asistente. Lo que se escribe ahí sale del teléfono,
      el servidor guarda la charla hasta 2 horas y la pasa a un proveedor de
      IA: se declara *Other User Content*, no vinculado, para la funcionalidad
      de la app. Todo lo demás sí es "no": sin push, sin token de
      dispositivo, sin telemetría, sin un solo permiso. Las respuestas exactas
      están en [`ficha.md`](app-store/ficha.md), y coinciden con
      `PrivacyInfo.xcprivacy`.

---

## 4. Las capturas

Apple cambió las medidas y los nombres más de una vez, así que lo que manda es
[la página de especificaciones](https://developer.apple.com/help/app-store-connect/reference/app-information/screenshot-specifications)
en el momento de subir. Al día de hoy:

- **Obligatoria**: al menos una captura de *iPhone con Dynamic Island (pantalla
  mediana)* — **1179 × 2556** o **1206 × 2622** píxeles en vertical.
- **Grande** (opcional, se escala sola si falta): 1260 × 2736, 1290 × 2796 o
  1320 × 2868.
- Hasta 10 por tamaño, en `.png`, `.jpg` o `.jpeg`.

Las de la ficha las saca **"Capturas para la tienda"**: recorre ocho pantallas
en un iPhone 16 Pro (1206 × 2622), en los tres idiomas, con la barra de estado
de las capturas de Apple, y mide cada una. Se corre a mano cuando el contenido
del panel es el que se quiere mostrar, porque fotografía lo publicado.

El workflow **"Captura de iOS"** es otra cosa: corre en cada push para saber si
la app dibuja. También prefiere un simulador de la medida obligatoria e imprime
los píxeles de cada captura.

Comprobado en una corrida real: arrancó el iPhone 16 Pro (iOS 18.5) al primer
intento y las tres capturas salieron de **1206 × 2622**, la medida obligatoria.

El arranque del simulador tiene límite de tiempo y, si uno no arranca, prueba el
siguiente (hasta tres). Hace falta: una vez el iPhone 17 Pro sobre iOS 26 dejó
colgado `simctl` 22 minutos hasta que lo cortó el timeout del job, y por eso
ahora va último en la lista de preferidos.

Eso resuelve el formato, no la elección: *qué* pantallas mostrar y en qué orden
es una decisión de producto.

---

## 5. Los riesgos de revisión que valen la pena mirar

No son trámites: son los motivos por los que Apple rechaza apps parecidas a
esta.

- **Secciones vacías (guías 2.1 y 4.2).** El 6 de noviembre de 2026 el panel
  tenía 6 lugares y ningún artículo, recorrido ni evento. Una sección vacía se
  lee como app sin terminar. Hay que cargar contenido antes de mandarla.
- **El asistente y la IA de terceros (guía 5.1.2(i)).** Desde noviembre de
  2025 Apple pide avisar en la app, y pedir permiso, antes de mandar datos
  personales a una IA de terceros. La pantalla del asistente hoy no avisa nada.
  La recomendación y por qué está en la guía.
- **Funcionalidad mínima (guía 4.2).** Fue un riesgo real mientras la app de iOS
  era sólo el mapa. Ya no: comparte las cuatro secciones, el buscador, las
  fichas, los artículos, los recorridos y el asistente con la de Android, porque
  todas las pantallas viven en `:interfaz`. Conviene igual que la ficha de la
  tienda muestre esa amplitud.
- **Contenido ajeno.** Es una guía de un distrito con contenido de un panel
  editorial propio, no un agregador de contenido de terceros.
- **Sin cuenta para usar la app.** No hace falta registrarse para ver nada, así
  que no hay que dar credenciales de prueba al revisor. El panel de promotores
  es otra cosa y no está en la app.
- **Permisos.** No se pide ninguno: ni ubicación, ni cámara, ni calendario, ni
  notificaciones. Agendar un evento abre el calendario del teléfono con el
  formulario lleno y la persona confirma; eso no requiere permiso y es
  deliberado.
- **Atribución de OpenStreetMap.** Obligatoria por la licencia ODbL y dibujada
  visible sobre el mapa, no detrás de un botón.

---

## 6. Qué está comprobado y qué no

Comprobado en CI, en un runner macOS:

- que `:compartido` y `:ios` compilan para iOS, y que el framework se enlaza;
- que las pruebas del contrato pasan **en el simulador**;
- que la app arranca y dibuja —hay captura, en
  [`docs/imagenes/inicio-en-iphone.png`](imagenes/inicio-en-iphone.png);
- que compila y enlaza para **teléfono** (`-sdk iphoneos`), sin firmar;
- que las siete comprobaciones del paquete fallan cuando deben: se probaron
  contra un `.app` fabricado, una por una;
- que **archiva en Release**, que es lo que nadie había comprobado nunca en este
  proyecto —los otros dos workflows trabajan en Debug— y que lo que queda
  adentro del `.app` es lo que tiene que estar. La primera corrida verde dijo
  esto:

  ```
  === el mapa ===            glifos: 6, PMTiles: 2.0M
  === los textos ===         los tres idiomas estan
  === las tipografias ===    7
  === el icono ===           compilado
  === la version ===         plist dice: 1.6.0 (18) / deberia: 1.6.0 (18)
  === la arquitectura ===    Non-fat file: arm64
  tamano del .app: 42M
  ```

  Y dejó dos archivos: `Turismo-1.6.0-18.xcarchive.zip` (27 MB) y
  `dSYMs-1.6.0-18.zip` (9,4 MB). El nombre con la versión correcta no es
  cosmético: es la prueba de que el número salió de `version.properties` y llegó
  hasta el `Info.plist`.

- que **archiva con Xcode 26**, que es lo que Apple exige desde abril de 2026
  (corrida del 6 de noviembre de 2026);
- que el chequeo de APIs con motivo obligatorio **sirve**: en su primera corrida
  encontró dos categorías que el manifiesto no declaraba —`SystemBootTime`
  (`systemUptime`) y `DiskSpace` (`statfs`, `fstatfs`), las dos de bibliotecas
  enlazadas— y la subida habría sido rechazada con ITMS-91053. Declaradas, la
  corrida siguiente pasó entera:

  ```
  ok  FileTimestamp  — stat fstat lstat NSFileModificationDate
  ok  SystemBootTime — systemUptime
  ok  DiskSpace      — statfs fstatfs
  ok  UserDefaults   — NSUserDefaults
  ```
- que "Capturas para la tienda" saca las capturas en **1206 × 2622, sin canal
  alfa**, en los tres idiomas.

No comprobado, y hace falta la cuenta o un teléfono — no hay forma de
adelantarlo desde acá:

- firmar, exportar el `.ipa` y que App Store Connect lo acepte;
- cómo se ve y se siente en un iPhone físico;
- la propia revisión de Apple.
