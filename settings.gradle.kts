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

// :app es la app Android de siempre.
// :compartido es lo que las dos plataformas usan igual — hoy el contrato con el
//   panel. Kotlin puro, sin interfaz, sin Compose: por eso :app lo puede
//   consumir sin arrastrar nada nuevo.
// :ios es la version de iOS, con Compose Multiplatform. Solo compila en macOS.
include(":app")
include(":compartido")
include(":ios")
