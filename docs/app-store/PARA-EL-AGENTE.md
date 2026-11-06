# Para el agente que ayude a publicar en la App Store

Esto es para un agente (Claude Code u otro) que corre en la **computadora con
Windows** del dueño del proyecto y lo ayuda a mandar la app de iPhone a la App
Store. Leelo entero antes de hacer nada.

## Lo primero

1. **`CLAUDE.md`**, en la raíz. No es un readme: son las reglas del proyecto, y
   algunas las hace cumplir la compilación. Las que más importan acá:
   - **No escribís texto de producto.** Nada de descripciones, subtítulos,
     palabras clave ni avisos para la app. Como mucho un título o tres palabras.
     Si hace falta un texto, se lo pedís a la persona.
   - **Se dice lo que no está verificado.** No digas "listo" sobre algo que no
     viste funcionar.
2. **`docs/app-store/GUIA.md`**: los pasos, en el orden en que se hacen. Es lo
   que la persona sigue; vos la acompañás.
3. **`docs/app-store/ficha.md`**: las respuestas de App Store Connect.

## Cómo es el camino, y por qué no hay Mac

Esta máquina es Windows, y **no se compila nada de iOS acá**: Kotlin/Native y
Xcode solo corren en macOS. Lo hace GitHub Actions en sus runners macOS, gratis
porque el repositorio (`NasastaXD/Turismo-app-czu`) es público:

- **"Publicar iOS (App Store)"** (`.github/workflows/publicar-ios.yml`):
  pruebas, Xcode 26, archivado en Release, comprobaciones del paquete,
  comprobación de las APIs con motivo obligatorio, firma, `.ipa` y subida a App
  Store Connect.
- **"Capturas para la tienda"** (`.github/workflows/capturas-tienda.yml`): las
  capturas en 1206 × 2622, en los tres idiomas.

No sugieras alquilar una Mac ni servicios de compilación de terceros: no hacen
falta, y el proyecto evita servicios externos a propósito.

## Lo que podés hacer

**Con PowerShell**, el certificado (pasos 4 y 7 de la guía):

```powershell
cd docs\app-store
powershell -ExecutionPolicy Bypass -File .\preparar-firma.ps1 -Paso solicitud -Correo <correo> -Nombre "<nombre>"
# ...la persona sube la solicitud a Apple y baja distribution.cer, el perfil y el .p8...
powershell -ExecutionPolicy Bypass -File .\preparar-firma.ps1 -Paso secretos -Certificado <cer> -Perfil <mobileprovision> -ClaveApi <p8> -Equipo <TEAM_ID> -IdClave <KEY_ID> -Emisor <ISSUER_ID> -Cargar
```

El script necesita Git para Windows (trae OpenSSL) y, para `-Cargar`, GitHub CLI
con `gh auth login` hecho.

**Con GitHub CLI**, todo lo de Actions:

```powershell
gh workflow run "Publicar iOS (App Store)" --repo NasastaXD/Turismo-app-czu --ref main
gh run list --repo NasastaXD/Turismo-app-czu --workflow "Publicar iOS (App Store)" --limit 3
gh run watch <id> --repo NasastaXD/Turismo-app-czu
gh run view <id> --repo NasastaXD/Turismo-app-czu --log-failed
gh run download <id> --repo NasastaXD/Turismo-app-czu --dir capturas
```

`gh workflow run` solo funciona si el workflow ya está en `main` (paso 0 de la
guía).

## Lo que no hacés

- **No subas nada de `firma-ios` a ningún repositorio**, ni lo pegues en un chat,
  un issue o un commit. Es la clave privada del certificado y la clave de la API
  de Apple.
- **No toques el keystore de Android** ni propongas rotarlo: el dueño ya decidió.
- **No inventes respuestas para App Store Connect.** Las que están en `ficha.md`
  salen del código; si algo no coincide con lo que ves en la app, decilo.
- **No apagues el asistente para pasar la revisión** ni ninguna otra función:
  una función que aparece después de aprobada la app es una violación de las
  reglas de Apple.
- **No abras PR ni hagas push** sin que te lo pidan. Los cambios de este
  proyecto se acumulan como commits en la rama de trabajo hasta que se pide
  **"Release!"** (`CLAUDE.md`, §8).

## Si un workflow falla

Leé el log del paso que falló (`gh run view <id> --log-failed`) **antes** de
cambiar nada. La tabla "Si algo falla" del final de `GUIA.md` cubre los errores
esperables. Tres que conviene saber de antemano:

- **"Comprobar las APIs que Apple obliga a justificar" falla.** El binario usa
  una función que Apple obliga a declarar en `ios/xcode/PrivacyInfo.xcprivacy`.
  El log dice qué categoría, qué símbolos y qué motivos caben. Elegí el motivo
  que describa **lo que de verdad hace la app** (casi siempre: `C617.1` para
  archivos dentro del contenedor de la app, `35F9.1` para medir tiempo
  transcurrido, `E174.1` para ver si hay lugar antes de escribir), agregalo y
  volvé a correr. No agregues categorías que el log no pidió.
- **La subida dice que el número de compilación ya existe.** Subí `codigo` en
  `version.properties` (y nada más) y volvé a correr.
- **Algo de firma.** Casi siempre es que el certificado, el perfil y el App ID
  no son del mismo trío. Rehacer los pasos 5 y 7 de la guía.

Si el error no está en ninguna de esas, buscá en los comentarios del workflow:
están escritos explicando por qué cada paso es como es, y muchos son errores que
ya pasaron.

## Lo que ya está verificado y lo que no

Verificado (en GitHub Actions, sin cuenta de Apple):

- La app compila, se archiva en Release y lleva adentro el mapa, los textos, las
  tipografías, el ícono, la versión correcta y el manifiesto de privacidad.
- Arranca y dibuja en el simulador.
- `preparar-firma.ps1` arma un `.p12` que se abre con su contraseña y carga los
  siete secretos exactos (probado con PowerShell 7 y un certificado falso; no en
  un Windows real).

**No verificado**, porque necesita la cuenta de Apple:

- la firma real, el `.ipa` y la subida a App Store Connect;
- la revisión de Apple;
- cómo se ve y se siente la app en un iPhone físico.

La primera corrida con los secretos cargados es la que va a decir lo primero.
Si falla, es lo esperable de un camino que nunca corrió: leé el log y arreglá
eso, sin cambiar lo que no falló.
