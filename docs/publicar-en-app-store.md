# Publicar en la App Store

Lo que hace el repositorio, lo que hay que hacer a mano, y en qué orden. Para
Google Play el documento equivalente es
[`docs/publicar-en-tiendas.md`](publicar-en-tiendas.md).

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

## 2. Los cuatro secretos de firma

Hay que cargarlos en **Settings → Secrets and variables → Actions** del
repositorio. Mientras falte alguno, el workflow archiva sin firmar y lo dice en
el resumen de la corrida.

| Secreto | Qué es |
|---|---|
| `IOS_TEAM_ID` | los diez caracteres del equipo, arriba a la derecha en developer.apple.com |
| `IOS_CERTIFICADO_P12_BASE64` | el certificado de distribución **con su clave privada**, exportado como `.p12` y pasado a base64 |
| `IOS_CERTIFICADO_CLAVE` | la contraseña que se le puso al exportar el `.p12` |
| `IOS_PERFIL_BASE64` | el perfil de aprovisionamiento de App Store (`.mobileprovision`) en base64 |

### Cómo sacarlos, en la Mac

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
5. **La primera subida.** Desde una Mac hay dos caminos, los dos sobre lo que
   deja el workflow:
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
      este proyecto **lo escribe una persona**, nunca un agente. Lo que falta
      está listado en [`docs/textos-para-completar.md`](textos-para-completar.md).
- [ ] **Categoría** y el cuestionario de **clasificación por edad**.
- [ ] **URL de política de privacidad**, obligatoria. El borrador está en
      [`docs/privacidad-borrador.md`](privacidad-borrador.md) y hay que
      publicarlo en una URL de `caaguazu.net`.
- [ ] **App Privacy** (la "etiqueta nutricional"): con la misma auditoría que se
      hizo para Play, la respuesta es *Data Not Collected* en casi todo. La app
      no tiene push, ni token de dispositivo, ni telemetría saliendo del
      teléfono, y no pide un solo permiso.

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

El workflow **"Captura de iOS"** saca capturas del simulador y, desde este
cambio, **prefiere un simulador cuya pantalla coincida con la medida obligatoria
e imprime los píxeles de cada captura**, para que se sepa sin medir si sirven
para la tienda o sólo para mirar. Las deja como artefacto de la corrida.

Eso resuelve el formato, no la elección: *qué* pantallas mostrar y en qué orden
es una decisión de producto.

---

## 5. Los riesgos de revisión que valen la pena mirar

No son trámites: son los motivos por los que Apple rechaza apps parecidas a
esta.

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
  contra un `.app` fabricado, una por una.

Pendiente de la primera corrida verde, que es la que está en curso:

- que **archive en Release** en un runner macOS. Es lo que nadie comprobó nunca
  en este proyecto: los otros dos workflows trabajan en Debug.

No comprobado, y hace falta la cuenta o un teléfono — no hay forma de
adelantarlo desde acá:

- firmar, exportar el `.ipa` y que App Store Connect lo acepte;
- cómo se ve y se siente en un iPhone físico;
- la propia revisión de Apple.
