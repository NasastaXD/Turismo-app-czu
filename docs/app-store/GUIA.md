# Publicar la app de iPhone desde Windows

**No hace falta una Mac, ni alquilar una.** GitHub compila en sus propias Mac
—gratis, porque el repositorio es público— y el workflow **"Publicar iOS (App
Store)"** ya compila, firma y sube la app a App Store Connect. Todo lo demás se
hace en el navegador.

Lo que te toca a vos:

1. Pagar la cuenta de Apple.
2. Sacar cuatro cosas de la web de Apple (un certificado, un perfil, una clave y
   el número de equipo). Para el certificado, un script de esta carpeta hace la
   parte que en una Mac haría el Llavero.
3. Cargarlas en GitHub, una sola vez.
4. Apretar **Run workflow**.
5. Completar la ficha en App Store Connect y mandarla a revisión.

| | Cuánto |
|---|---|
| Cuenta de Apple Developer | USD 99 por año (o gratis, ver paso 1) |
| GitHub, Windows, el script | gratis |
| Tiempo tuyo, la primera vez | una tarde |
| Cada compilación | ~25 minutos, sin hacer nada |
| Revisión de Apple | normalmente 1 a 2 días |

Archivos de esta carpeta:

| Archivo | Para qué |
|---|---|
| `GUIA.md` | esto |
| `preparar-firma.ps1` | el certificado desde Windows (pasos 4 a 7) |
| `ficha.md` | cada campo de App Store Connect con su respuesta |
| `sitio/privacidad.html`, `sitio/soporte.html` | las dos páginas que Apple exige, listas para publicar |
| `PARA-EL-AGENTE.md` | instrucciones para un agente que te ayude desde tu compu |

---

## Antes de empezar: tres cosas que pueden hacer que la rechacen

Ninguna es de código. Las tres se resuelven antes de mandarla a revisión;
TestFlight (paso 9) se puede hacer igual sin resolverlas.

### 1. El contenido del panel

El 6 de noviembre de 2026, lo publicado en caaguazu.net era **6 lugares, 0
artículos, 0 recorridos y 0 eventos**. Un revisor de Apple que abre
**Artículos** y **Recorridos** y los encuentra vacíos lo lee como una app sin
terminar (guías 2.1 y 4.2), que es de los rechazos más comunes.

De paso: varios títulos están en mayúsculas (`BIBLIOTECA PUBLICA MUNICIPAL`) y
uno dice **"MUNICIPALIDAD DE CAAGUZÚ"**, con una letra de menos. Las capturas de
la tienda muestran lo que haya en el panel, así que conviene corregirlo antes de
sacarlas.

### 2. El asistente y la regla de la IA de terceros

Desde noviembre de 2025, la guía **5.1.2(i)** de Apple dice que hay que
**avisar claramente cuando se comparten datos personales con terceros,
"incluida una IA de terceros", y obtener permiso explícito antes**. El
asistente manda lo que la persona escribe a un proveedor de IA externo.

Hoy la pantalla del asistente arranca vacía, sin ningún aviso. La política de
privacidad y la etiqueta de privacidad ya lo dicen (paso 11 y `ficha.md`), pero
un revisor puede pedir que se diga **en la app, antes de la primera pregunta**.

**Lo que recomiendo:** un aviso de una o dos líneas la primera vez que se abre
el asistente, con un botón para aceptar, que no vuelva a aparecer. El mecanismo
es poco código; **el texto lo tiene que escribir una persona**, por la regla del
proyecto. Si lo escribís, el agente lo conecta.

La alternativa —apagar el asistente para la revisión y prenderlo después— **no
conviene**: una función que aparece después de aprobada la app es justo lo que
Apple prohíbe como "función oculta o no documentada", y puede costar la cuenta.

### 3. Nadie la vio en un iPhone

Ni la de iPhone ni la de Android se vieron nunca en un teléfono de verdad.
TestFlight (paso 9) es para eso: es gratis, no pasa por revisión y se instala en
cualquier iPhone.

---

## Paso 0. Mezclar el PR #31

Los workflows de iOS viven en el PR #31 (`claude/apk-programming-wczoo7`).
GitHub solo muestra el botón **Run workflow** de un workflow que ya está en
`main`, así que primero hay que mezclarlo: en GitHub, el PR → *Ready for
review* → *Merge pull request*.

## Paso 1. La cuenta de Apple Developer

En **developer.apple.com/programs/enroll**, con un Apple ID.

- **Individual** si va a tu nombre. Se hace en el día. En la tienda aparece tu
  nombre como vendedor.
- **Organization** si va a nombre de una institución. Pide un número **D-U-N-S**
  (gratis, pero tarda una a dos semanas en salir) y que quien se inscribe pueda
  firmar por la institución. En la tienda aparece la institución.

> **Exención de la cuota.** Apple no cobra los USD 99 a entidades **gubernamentales,
> educativas y sin fines de lucro** que publican apps gratis
> (developer.apple.com/support/membership-fee-waiver). Si la app va a nombre de
> la municipalidad, la gobernación o una institución educativa, conviene
> pedirlo antes de pagar.

Cuando la cuenta está activa, anotá tu **Team ID**: diez letras y números, en
**developer.apple.com/account**, sección *Membership details*. Es el secreto
`IOS_TEAM_ID`.

## Paso 2. Registrar el identificador de la app

**developer.apple.com/account/resources/identifiers** → **+** → *App IDs* →
*App* → Continue.

- Description: `Turismo Caaguazu`
- Bundle ID: **Explicit**, `net.caaguazu.turismo`
- Capabilities: ninguna. La app no usa ninguna.

Register.

## Paso 3. Crear la app en App Store Connect

**appstoreconnect.apple.com** → *Apps* → **+** → *New App*:

- Platform: **iOS**
- Name: el nombre en la tienda (único en toda la App Store, hasta 30 caracteres)
- Primary language: **Spanish (Mexico)** — por qué, en `ficha.md`
- Bundle ID: `net.caaguazu.turismo` (aparece en la lista por el paso 2)
- SKU: `caaguazu-turismo`
- User access: Full access

## Paso 4. El certificado: la solicitud

Necesitás **Git para Windows** instalado (git-scm.com): trae OpenSSL, que es lo
que usa el script.

En PowerShell, desde la carpeta del repositorio:

```powershell
cd docs\app-store
powershell -ExecutionPolicy Bypass -File .\preparar-firma.ps1 -Paso solicitud -Correo tu@correo.com -Nombre "Tu Nombre"
```

Crea `C:\Users\<vos>\firma-ios\` con dos archivos: la **clave privada** y la
**solicitud** (`solicitud.certSigningRequest`).

> **La carpeta `firma-ios` no se borra y no se sube a ningún lado.** La clave
> privada que está ahí es la mitad del certificado que no tiene Apple: sin ella
> el certificado no sirve y hay que pedir otro. Guardala también en otro lugar
> seguro (un pendrive, un gestor de contraseñas).

Después, en el navegador: **developer.apple.com/account/resources/certificates**
→ **+** → **Apple Distribution** → Continue → subí `solicitud.certSigningRequest`
→ **Download**. Baja `distribution.cer`.

## Paso 5. El perfil

**developer.apple.com/account/resources/profiles** → **+** → en *Distribution*,
**App Store Connect** → Continue → App ID `net.caaguazu.turismo` → el
certificado del paso 4 → nombre `Turismo App Store` → **Download**. Baja un
`.mobileprovision`.

## Paso 6. La clave de la API de App Store Connect

Es lo que deja al workflow subir la app sin una Mac.

**appstoreconnect.apple.com** → *Users and Access* → *Integrations* → *App Store
Connect API* → **Team Keys** → **+**:

- Name: `GitHub Actions`
- Access: **App Manager**

**Download API Key** baja `AuthKey_XXXXXXXXXX.p8`. **Apple deja bajarlo una
sola vez**: guardalo junto con la carpeta `firma-ios`.

En esa misma página anotá dos cosas:

- el **Key ID** de la clave (diez caracteres) → `ASC_KEY_ID`
- el **Issuer ID**, arriba de la tabla (un código largo con guiones) →
  `ASC_ISSUER_ID`

## Paso 7. Cargar los secretos en GitHub

Con **GitHub CLI** instalado (cli.github.com) y `gh auth login` hecho, un solo
comando arma el `.p12` y carga los siete secretos:

```powershell
powershell -ExecutionPolicy Bypass -File .\preparar-firma.ps1 -Paso secretos `
  -Certificado "$HOME\Downloads\distribution.cer" `
  -Perfil      "$HOME\Downloads\Turismo_App_Store.mobileprovision" `
  -ClaveApi    "$HOME\Downloads\AuthKey_XXXXXXXXXX.p8" `
  -Equipo TU_TEAM_ID -IdClave TU_KEY_ID -Emisor TU_ISSUER_ID `
  -Cargar
```

Sin `-Cargar`, deja cada secreto en un archivo de
`firma-ios\secretos\` y los cargás a mano: en GitHub, el repositorio →
*Settings* → *Secrets and variables* → *Actions* → *New repository secret*. El
nombre del archivo es el nombre del secreto; el contenido, el valor.

| Secreto | Qué es |
|---|---|
| `IOS_TEAM_ID` | el Team ID (paso 1) |
| `IOS_CERTIFICADO_P12_BASE64` | el certificado con su clave privada (lo arma el script) |
| `IOS_CERTIFICADO_CLAVE` | la contraseña del `.p12` (la inventa el script) |
| `IOS_PERFIL_BASE64` | el perfil (paso 5) |
| `ASC_KEY_ID` | el Key ID (paso 6) |
| `ASC_ISSUER_ID` | el Issuer ID (paso 6) |
| `ASC_KEY_P8` | el contenido del `.p8` (paso 6), tal cual, con sus líneas `BEGIN` y `END` |

El script comprueba antes de armar nada que el certificado sea pareja de la
clave privada: si el certificado se pidió con otra solicitud, lo dice y no
sigue.

## Paso 8. Compilar y subir

En GitHub: **Actions** → **Publicar iOS (App Store)** → **Run workflow** →
rama `main` → **Run workflow**.

Tarda unos 25 minutos. Al terminar, la página de la corrida dice arriba qué
pasó:

- **"Subido a App Store Connect"**: en 5 a 30 minutos más aparece en
  App Store Connect → tu app → *TestFlight*. Apple manda un correo cuando
  termina de procesarla, o uno con el motivo si la rechaza.
- **"La subida falló"**: el motivo está en el paso *Subir a App Store Connect*.
  El `.ipa` firmado queda igual en los artefactos de la corrida.

Si falla antes, la tabla del final dice qué significa cada error.

## Paso 9. Probarla en TestFlight

1. App Store Connect → tu app → **TestFlight** → *Internal Testing* → **+** →
   agregate a vos. Para sumar a otros del equipo, primero se los invita en
   *Users and Access* (hasta 100). Para gente de afuera está *External
   Testing*, que pasa por una revisión corta de Apple.
2. En el iPhone, instalá **TestFlight** desde la App Store y abrí la invitación
   que llega por correo.

Qué mirar, como mínimo:

- [ ] Que abra, y que los textos no salgan entre ‹ángulos›.
- [ ] Que las fotos carguen.
- [ ] El mapa: que se mueva, que tenga nombres de calles y los pines, y la
      atribución `© OpenStreetMap`.
- [ ] Una ficha: que la hoja suba sobre la foto.
- [ ] El asistente: una pregunta, una respuesta y que sus fuentes se abran.
- [ ] Cambiar el idioma en Ajustes.
- [ ] En modo avión: que siga mostrando lo ya visto.

## Paso 10. Las capturas

**Después de cargar el contenido del panel.** Las capturas muestran lo que esté
publicado en ese momento.

**Actions** → **Capturas para la tienda** → **Run workflow** (idiomas: `es en
pt`). Tarda unos 30 minutos. En el artefacto `capturas-tienda` hay una carpeta
por idioma con ocho capturas de **1206 × 2622**, la medida obligatoria: el
inicio, una ficha (arriba y abajo), buscar, el mapa, artículos, recorridos y el
asistente.

Mirálas antes de subirlas: si alguna salió mal (una pantalla que no se abrió,
una foto que no terminó de cargar), se corre de nuevo. En App Store Connect van
en la casilla **iPhone 6,3"**; con esa sola alcanza, Apple escala el resto. Se
eligen y ordenan a mano: no tienen que ir las ocho.

## Paso 11. Publicar la política de privacidad y el soporte

Apple exige dos URL públicas: la **política de privacidad** y una página de
**soporte** con una forma real de contactar.

1. Completá lo que está entre `‹ángulos›` en `sitio/privacidad.html` y
   `sitio/soporte.html`: responsable, correo, ciudad, el proveedor de IA que
   está cargado en el panel y la fecha.
2. Que la lea alguien con criterio legal. Describe con exactitud lo que hace la
   app, pero si alcanza para la ley es otra pregunta.
3. Publicalas. Dos formas:
   - subir los dos archivos al hosting de caaguazu.net, por ejemplo a una
     carpeta `app/` (quedan en `caaguazu.net/app/privacidad.html`);
   - o crear dos páginas en WordPress y pegar, en un bloque **HTML
     personalizado**, lo que hay entre `<main>` y `</main>`.

La misma política sirve para Google Play.

## Paso 12. Completar la ficha

`ficha.md` tiene cada campo, en el orden en que lo pide App Store Connect, con
la respuesta. Los textos de venta —descripción, subtítulo, palabras clave—
quedaron como huecos con su límite de caracteres: los escribís vos.

## Paso 13. Mandar a revisión

App Store Connect → tu app → la versión **1.6.0**:

1. En **Build**, elegí el que subió el workflow.
2. Revisá que no quede ningún campo en rojo.
3. **Add for Review** → **Submit for Review**.

Llega un correo cuando cambia de estado. Si la aprueban y elegiste publicación
manual, aparece un botón **Release this version**.

### Si la rechazan

El motivo llega en **App Review** (dentro de App Store Connect), con el número
de la guía. Se contesta ahí mismo. Si hace falta cambiar algo en la app, se
corrige, se sube un build nuevo (paso 8, con el `codigo` subido) y se manda otra
vez. Un rechazo no es grave ni deja antecedentes: es parte del trámite.

---

## Las versiones siguientes

1. Subir la versión en **`version.properties`**: `nombre` (lo que ve la gente,
   por ejemplo `1.7.0`) y `codigo` (un número más que el anterior). Es el mismo
   archivo para Android y iPhone.
2. **Run workflow** en *Publicar iOS (App Store)*.
3. En App Store Connect, **+** al lado de *iOS App* para crear la versión nueva,
   elegir el build, completar *What's New* y mandar.

Los secretos no se vuelven a cargar. El certificado vence al año: cuando pase,
se repiten los pasos 4, 5 y 7 (el workflow avisa con un error de firma).

---

## Si algo falla

| Dónde | Qué dice | Qué es |
|---|---|---|
| Elegir Xcode 26 | "La imagen no trae Xcode 26 estable" | GitHub cambió la imagen. Cambiar `runs-on: macos-15` por `macos-26` en el workflow |
| Comprobar las APIs | "FALTA NSPrivacyAccessedAPICategory…" | Una biblioteca usa una función que Apple obliga a justificar. Agregar esa categoría con su motivo en `ios/xcode/PrivacyInfo.xcprivacy`; el error dice cuáles caben |
| Preparar la firma | "MAC verification failed" | La contraseña del `.p12` no coincide. Volver a correr el paso 7 entero |
| Archivar en Release | "No profile for team … matching …" | El perfil no es de este App ID o de este certificado. Rehacer el paso 5 eligiendo esos dos, y el paso 7 |
| Archivar en Release | "certificate … not found" o "no identity" | El certificado no corresponde a la clave privada, o venció |
| Subir | "The bundle version must be higher" | Ese `codigo` ya se subió. Subirlo en `version.properties` y volver a correr |
| Subir | "Authentication failed" / 401 | `ASC_KEY_ID`, `ASC_ISSUER_ID` o `ASC_KEY_P8` mal cargados, o la clave fue revocada |
| Correo de Apple | ITMS-91053 "Missing API declaration" | Lo mismo que "Comprobar las APIs": una categoría sin declarar que el workflow no vio |
| Correo de Apple | ITMS-90725 "SDK version issue" | Se compiló con un Xcode viejo: revisar el paso "Elegir Xcode 26" |

## ¿Y si GitHub no alcanza?

Para lo que hace esta app, alcanza. Si algún día hiciera falta una Mac de
verdad —por ejemplo, para mirar algo en Xcode—, se puede alquilar una por horas
(MacinCloud, por ejemplo) y seguir `docs/compilar-en-mac.md`. Para publicar, no
hace falta.
