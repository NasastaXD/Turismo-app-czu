# El proyecto de Xcode

Estos dos archivos de Swift son todo lo que la app de iOS necesita en su
propio lenguaje. El resto —pantallas, datos, mapa— es Kotlin compartido con
Android. **Si este directorio crece, algo se hizo mal.**

El proyecto de Xcode en sí (`.xcodeproj`) no está en el repo porque
generarlo requiere un Mac, y el entorno donde se escribe este proyecto es
Linux. Lo que sigue son los pasos para armarlo una vez.

## 1. Crear el proyecto

En Xcode: **App**, interfaz **SwiftUI**, lenguaje **Swift**. Borrar el
`ContentView.swift` y el `…App.swift` que genera, y agregar los dos
archivos de este directorio en su lugar.

## 2. Compilar el framework de Kotlin

```sh
./gradlew :ios:linkDebugFrameworkIosSimulatorArm64
```

Sale en `ios/build/bin/iosSimulatorArm64/debugFramework/Turismo.framework`.
Para el dispositivo real es `linkDebugFrameworkIosArm64` (y
`linkReleaseFramework…` para release).

En **Build Settings** del target: agregar el directorio del framework a
**Framework Search Paths**, y el framework a **Frameworks, Libraries, and
Embedded Content** como *Do Not Embed* — es estático.

Lo prolijo después es un **Run Script** como primera fase de build que
llame a Gradle, para que el framework se regenere solo. Sin eso hay que
acordarse de correr Gradle a mano cada vez que cambia el Kotlin, y
olvidarse produce un error de compilación confuso.

## 3. El mapa, que es lo que más fácil se rompe

Arrastrar `app/src/main/assets/map/` al proyecto y elegir **Create folder
references**, *no* "Create groups".

Esto importa: con grupos, Xcode aplana la estructura de directorios y las
rutas de los glifos (`map/glifos/{fontstack}/{range}.pbf`) dejan de
resolver. El mapa igual dibuja, pero sin una sola etiqueta — ni calles, ni
nombres de lugares. Es un fallo silencioso y confunde.

El bundle tiene que quedar con `map/caaguazu.pmtiles` y `map/glifos/…`
adentro, con esa forma exacta.

## 4. Qué debería verse

El distrito de Caaguazú en vectorial, centrado, con las etiquetas en
Poppins y `© OpenStreetMap` abajo. Todo desde el archivo embebido: se puede
comprobar en modo avión, que es la prueba de que no hay ningún servidor de
tiles de por medio.

**Nadie vio esto todavía.** El CI comprueba que compile y que enlace; que
dibuje bien lo comprueba una persona con un iPhone.
