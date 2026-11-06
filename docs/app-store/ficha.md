# La ficha en App Store Connect, campo por campo

Lo que hay que completar en appstoreconnect.apple.com, en el orden en que lo
pide, con la respuesta de cada campo. Las respuestas salen del código de la app
1.6.0 y del plugin del panel, no de suposiciones.

Tres clases de campos:

- **Con respuesta**: la respuesta está acá y se copia.
- **‹Hueco›**: es texto de producto o un dato que solo sabe el dueño. En este
  proyecto **el texto de producto lo escribe una persona** (`CLAUDE.md`, §2), así
  que no hay borrador: está el límite de caracteres y lo que Apple mira.
- **Decisión**: hay que elegir; está la recomendación y por qué.

---

## 1. Información de la app (*App Information*)

| Campo | Respuesta |
|---|---|
| Nombre | ‹hueco› — hasta 30 caracteres, y **tiene que ser único en toda la App Store**: si ya existe una app con ese nombre, no deja guardarlo |
| Subtítulo | ‹hueco› — hasta 30 caracteres. Aparece debajo del nombre |
| Idioma principal | **Spanish (Mexico)** — ver la nota de abajo |
| Bundle ID | `net.caaguazu.turismo` (aparece en la lista si el App ID ya está registrado) |
| SKU | `caaguazu-turismo` — es interno, no lo ve nadie, pero no se puede cambiar después |
| Categoría principal | **Travel** (Viajes) |
| Categoría secundaria | **Navigation**, o ninguna. Es opcional |
| Derechos de contenido | **Sí, contiene contenido de terceros, y tengo los derechos**: el mapa son datos de OpenStreetMap (licencia ODbL, con la atribución visible sobre el mapa) y las fichas, fotos y artículos los publica el equipo del panel. Ver la nota de abajo |
| Clasificación por edad | Sección 4 |

> **Idioma principal.** App Store Connect no tiene "español de Paraguay": tiene
> *Spanish (Mexico)* y *Spanish (Spain)*. México es el que usan las tiendas de
> Latinoamérica. Después se agregan **English (U.S.)** y **Portuguese
> (Brazil)** como localizaciones, cada una con sus textos y sus capturas: la app
> ya está en los tres idiomas y las capturas salen en los tres.

> **Las fotos.** Al marcar que hay derechos sobre el contenido, se está
> afirmando que cada foto publicada en el panel es de quien la subió o tiene
> permiso. Vale la pena confirmarlo con los promotores antes, sobre todo las
> fotos que parezcan sacadas de internet.

> **El nombre en el teléfono no es el mismo en las dos apps.** En Android la app
> se llama **Caaguazú** (`app_name`); en iPhone se llama **Turismo**
> (`CFBundleDisplayName` en `ios/xcode/project.yml`). Es lo que aparece debajo
> del ícono. Conviene decidir uno y usarlo en los dos lados. Es una línea en
> cada plataforma; no lo cambié porque el nombre lo decide el dueño.

---

## 2. Precio y disponibilidad

| Campo | Respuesta |
|---|---|
| Precio | **Gratis** (USD 0) |
| Disponibilidad | **Decisión.** Todos los países, o solo Paraguay y los vecinos. Todos es lo más simple; ver la nota de la Unión Europea |
| Compras dentro de la app | Ninguna |

> **Unión Europea (DSA).** Para estar en las tiendas de la UE, App Store Connect
> exige declarar si la cuenta es *trader* (comerciante). Si se declara que sí,
> Apple **muestra públicamente** dirección, teléfono y correo. Una municipalidad,
> una ONG o una persona que no comercia con la app normalmente declara que
> **no**. Si no se quiere contestar eso, se sacan los países de la UE de la
> disponibilidad y no lo pide.

---

## 3. Privacidad de la app (*App Privacy*)

Es la "etiqueta nutricional" que se ve en la ficha. **Tiene que coincidir con
`ios/xcode/PrivacyInfo.xcprivacy`**, que ya está escrito con estas mismas
respuestas.

| Pregunta | Respuesta |
|---|---|
| URL de la política de privacidad | ‹hueco› — la URL donde se publique `docs/app-store/sitio/privacidad.html` |
| ¿Usted o sus socios recolectan datos de esta app? | **Sí** |
| Tipos de datos | Solo **User Content → Other User Content** (contenido del usuario → otro contenido) |
| ¿Para qué se usa? | Solo **App Functionality** (funcionalidad de la app) |
| ¿Está vinculado a la identidad del usuario? | **No** |
| ¿Se usa para rastreo (*tracking*)? | **No** |

**Por qué "sí" y no "no recolecta datos":** es el asistente. Lo que la persona
escribe sale del teléfono, el servidor guarda la conversación hasta 2 horas y la
manda a un proveedor de IA externo. Para Apple, "recolectar" es justamente eso:
que salga del teléfono y quede más tiempo que el necesario para contestar en el
momento. **No vinculado**, porque no hay cuentas: la conversación se reconoce
por un identificador al azar.

Todo lo demás —ubicación, contactos, identificadores, compras, diagnóstico,
historial de navegación— es **no**. La app no tiene analítica, ni publicidad, ni
informes de fallos que salgan del teléfono.

> **Una sola duda razonable: las búsquedas.** Lo que se escribe en Buscar viaja
> al servidor en la URL del pedido. El panel no lo guarda, pero el registro de
> accesos del hosting sí guarda URLs, como cualquier sitio web. La práctica
> habitual es no declararlo; si el hosting guarda esos registros mucho tiempo,
> declarar también **Search History**, no vinculado, para App Functionality, es
> la respuesta más prudente.

---

## 4. Clasificación por edad

El cuestionario nuevo de Apple (2025). Todo **No** o **Ninguno**, lo que da
**4+**:

| Bloque | Respuesta |
|---|---|
| Controles dentro de la app (control parental, verificación de edad) | No |
| Acceso web sin restricciones | No — los enlaces abren el navegador del sistema, no se navega dentro de la app |
| Contenido generado por usuarios | No — nada de lo que alguien escribe se muestra a otra persona |
| Redes sociales | No |
| Mensajería y chat | No — el asistente no comunica a una persona con otra |
| Publicidad | No |
| Temas maduros, médico o bienestar, sexualidad, violencia | Ninguno |
| Juegos de azar, concursos, *loot boxes* | No |

> Si el cuestionario que aparezca pregunta por **IA generativa o chatbots**
> (la página de Apple no lo menciona hoy, pero lo cambian seguido), la respuesta
> es **sí**: el asistente genera texto con un modelo de IA, limitado a lo
> publicado en la app.

---

## 5. La versión (1.6.0)

| Campo | Respuesta |
|---|---|
| Capturas | Las del workflow **"Capturas para la tienda"**, carpeta de cada idioma. Van en la casilla de **iPhone de 6,3"** ("Dynamic Island, pantalla mediana"), que es la única obligatoria. Hasta 10; el orden lo decide quien las sube |
| Texto promocional | ‹hueco› — hasta 170 caracteres. Se puede cambiar sin mandar una versión nueva |
| Descripción | ‹hueco› — hasta 4000 caracteres. Apple rechaza descripciones que prometen lo que la app no hace, así que lo que se nombre tiene que estar en la app el día de la revisión |
| Palabras clave | ‹hueco› — hasta 100 caracteres en total, separadas por comas, sin espacios después de la coma. No repetir palabras del nombre: ya cuentan |
| URL de soporte | ‹hueco› — **obligatoria**. Una página con una forma real de contactar; `docs/app-store/sitio/soporte.html` está lista para eso |
| URL de marketing | Opcional. Puede ser caaguazu.net |
| Versión | `1.6.0` (sale de `version.properties`, la misma de Android) |
| Copyright | `2026 ‹nombre del titular›` |
| Build | El que subió el workflow, cuando termina de procesarse |
| Publicación | **Manual** ("Manually release this version"). Así, aprobada, sale cuando se decida y no sola en medio de la noche |

---

## 6. Información para la revisión (*App Review Information*)

| Campo | Respuesta |
|---|---|
| ¿Requiere iniciar sesión? | **No.** Desmarcar "Sign-in required" |
| Contacto | ‹nombre›, ‹teléfono con código de país›, ‹correo›. Lo usa el revisor si tiene una duda; nadie más lo ve |
| Notas | Las de abajo, en inglés: es el idioma en que trabajan los revisores |
| Archivo adjunto | No hace falta |

### Notas para el revisor

No es texto de producto: es una explicación para quien revisa, y conviene que
diga exactamente lo que va a encontrar. **Leerlas antes de pegarlas**: si algo
cambió en el panel o en la app, corregirlo.

```
This app is a tourism guide to the district of Caaguazú, Paraguay. Places,
events, articles and routes are published by the local tourism team through the
caaguazu.net editorial panel, and the app loads them from
https://caaguazu.net/wp-json/czu-app/v1/.

No account or sign-in is needed. Every section is available on first launch.

- Home, Search, Articles and Routes are the four sections of the bottom bar.
- The map (Search > map button) is embedded in the app: OpenStreetMap data as
  offline vector tiles, with the required attribution visible on the map. It
  does not use or request location.
- The round button in the middle of the bar opens an assistant that answers
  questions using only the content published in the app. Questions are sent to
  our server, which uses a third-party AI provider to write the answer; this is
  disclosed in the privacy policy and in App Privacy (Other User Content, not
  linked, App Functionality). Each answer lists the published items it is based
  on, and they open inside the app.
- Directions open in Google Maps (the app if installed, otherwise the
  website). "Schedule" on an event opens the system share sheet with an .ics
  file, so it can be added to Calendar. The app requests no permissions at all.
- The app is available in Spanish, English and Portuguese and follows the
  device language; it can be changed in Settings (round button at the top
  right of each section).
```

---

## 7. Lo que ya resuelve el código

No hay que contestarlo: viene dentro de la app.

- **Cifrado**: `ITSAppUsesNonExemptEncryption = false` en el `Info.plist`. La app
  solo usa HTTPS, que está exento, y por eso App Store Connect no pregunta por
  la exportación en cada subida.
- **Manifiesto de privacidad**: `PrivacyInfo.xcprivacy`, con el asistente
  declarado y las APIs con motivo obligatorio que el workflow comprueba contra
  el binario.
- **Ícono**: el de 1024 × 1024, opaco, del mismo dibujo que el de Android.
- **Versión y número de compilación**: de `version.properties`.

---

## 8. Todos los huecos, juntos

Para no tener que buscarlos:

- [ ] Nombre en la tienda (30) y subtítulo (30)
- [ ] El nombre debajo del ícono: ¿Caaguazú o Turismo? (sección 1)
- [ ] Descripción (4000), texto promocional (170) y palabras clave (100), en
      cada idioma que se cargue
- [ ] Responsable, correo de contacto y ciudad, en `privacidad.html` y
      `soporte.html`
- [ ] El proveedor de IA que está cargado en el panel, en `privacidad.html`
- [ ] La fecha de la política
- [ ] Las URL de privacidad y de soporte, una vez publicadas
- [ ] Contacto para el revisor
- [ ] Copyright: el nombre del titular
- [ ] Disponibilidad y la declaración de la UE (sección 2)
