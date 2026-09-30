# Compilar la app de iPhone en una Mac

Todo lo que sigue se hace **una sola vez**. Después, cada vez que quieras una
versión nueva son los pasos 5 y 6 y nada más.

No hace falta saber Kotlin ni Swift: son comandos que se copian y se pegan.

---

## Lo que tiene que tener la Mac

| Qué | Cómo se consigue | Cuánto tarda |
|---|---|---|
| **macOS** con chip Apple (M1 o más nuevo) | — | — |
| **Xcode** 15 o más nuevo | App Store, gratis | ~1 hora de descarga |
| **Java 21** | `brew install --cask temurin@21` | 5 min |
| **XcodeGen** | `brew install xcodegen` | 1 min |

> **La Mac tiene que ser de chip Apple, no Intel.** No es un capricho del
> proyecto: la biblioteca del mapa (`maplibre-compose`) no publica versión para
> el simulador de Intel, así que en una Mac Intel el proyecto no compila. Está
> explicado en `ios/xcode/project.yml`, en el comentario de `EXCLUDED_ARCHS`.

Si no tenés Homebrew —el `brew` de arriba— se instala con una línea desde
[brew.sh](https://brew.sh).

---

## 1. Traer el proyecto

```sh
git clone https://github.com/NasastaXD/Turismo-app-czu.git
cd Turismo-app-czu
git checkout claude/apk-programming-wczoo7
```

## 2. Abrir Xcode una vez

Abrilo, aceptá las licencias y dejá que termine de instalar sus componentes.
Xcode no se vuelve a tocar hasta el paso 6.

Después, en una terminal:

```sh
sudo xcode-select --switch /Applications/Xcode.app/Contents/Developer
xcodebuild -runFirstLaunch
```

## 3. Compilar la biblioteca de Kotlin

Es donde viven las pantallas, y es lo que Xcode va a consumir.

```sh
./gradlew :ios:linkDebugFrameworkIosSimulatorArm64
```

**La primera vez tarda entre diez y veinte minutos** y baja varios cientos de
MB: el compilador de Kotlin para iOS se descarga aparte. Las veces siguientes
son menos de dos minutos.

## 4. Generar el proyecto de Xcode

El `.xcodeproj` **no está guardado en el repositorio**, se arma de un archivo de
texto. Eso es a propósito: un `.xcodeproj` son miles de líneas generadas que
nadie puede revisar, y así el proyecto se puede armar sin abrir Xcode.

```sh
cd ios/xcode
xcodegen generate
```

Eso deja un `Turismo.xcodeproj` al lado.

## 5. Compilar y ver la app en el simulador

Desde `ios/xcode`:

```sh
open Turismo.xcodeproj
```

En Xcode: arriba a la izquierda elegí un iPhone del simulador —cualquiera—
y apretá el botón de play. La primera compilación tarda unos minutos.

Si preferís no abrir Xcode:

```sh
xcodebuild -project Turismo.xcodeproj -scheme Turismo \
  -configuration Debug -sdk iphonesimulator \
  -destination 'platform=iOS Simulator,name=iPhone 16' build
```

## 6. Instalarla en un iPhone de verdad

Para esto **sí** hace falta la cuenta del Apple Developer Program (US$ 99/año).
Con un Apple ID común se puede instalar en tu propio teléfono por siete días,
que alcanza para probar.

1. En Xcode: *Settings → Accounts →* agregá tu Apple ID.
2. Elegí el proyecto **Turismo** en el panel izquierdo, pestaña
   *Signing & Capabilities*, marcá *Automatically manage signing* y elegí tu
   equipo.
3. Conectá el iPhone por cable, elegilo arriba en lugar del simulador, y play.

> El proyecto viene con la firma **apagada sólo para el simulador**. Para un
> teléfono Xcode la pide y la resuelve solo con tu cuenta; no hay nada que
> cambiar a mano.

---

## Cuando algo falle

**`cannot find 'PuntoDeEntradaKt' in scope`**
La biblioteca de Kotlin no está compilada, o se compiló para otra cosa. Volvé
al paso 3. Si la Mac es Intel, no hay arreglo: ver la advertencia de arriba.

**`No such module 'TurismoKit'`**
Lo mismo: falta el paso 3, o se corrió desde otra carpeta. Tiene que ser desde
la raíz del repositorio.

**El mapa se dibuja pero sin nombres de calles ni lugares**
Xcode aplanó la carpeta `map/`. En el proyecto, esa carpeta tiene que aparecer
en azul (referencia de carpeta) y no en amarillo (grupo). Se arregla borrando
el `.xcodeproj` y volviendo al paso 4.

**La app se ve con otra tipografía**
Faltó la carpeta `font` en el bundle. Mismo arreglo: borrar el `.xcodeproj` y
regenerar.

**Todos los textos salen entre ‹angulitos›**
Faltó la carpeta `textos`. Mismo arreglo. Los angulitos son a propósito: es la
forma de que sea imposible confundir un hueco sin texto con texto terminado.

---

## Lo que ya está resuelto y no vas a tener que tocar

- El icono, en todos los tamaños que pide Apple.
- El archivo de privacidad (`PrivacyInfo.xcprivacy`) que Apple exige.
- La declaración de cifrado, que si no te la pregunta en cada subida.
- El identificador de la app, el mismo que en Android.
- Que compile también para teléfono y no sólo para el simulador: lo comprueba
  el CI en cada cambio, con `lipo`.

## Lo que todavía no

Mandarla a la App Store. La app de iPhone es nueva y conviene mirarla en un
teléfono antes de que la mire un revisor de Apple — está explicado en la guía
de publicación, sección 4.
