pluginManagement {
    repositories {
        google { content { includeGroupByRegex("com\\.android.*|com\\.google.*|androidx.*") } }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Turismo Caaguazu"

// :app        la cascara de Android: la Activity, la Application, el registro
//             a Logcat y los avisos con WorkManager. Nada de pantallas.
// :compartido  el contrato con el panel, la red, la cache y el disco. Kotlin
//             puro, sin Compose de interfaz.
// :interfaz    el sistema visual y TODAS las pantallas, una sola vez para las
//             dos plataformas. Es donde vive la app de verdad.
// :ios         la cascara de iOS: el UIViewController que entrega a Swift.
//
// Las dos cascaras son chicas a proposito. Si una empieza a crecer, es que algo
// que deberia compartirse se esta escribiendo dos veces.
include(":app")
include(":compartido")
include(":interfaz")
include(":ios")
