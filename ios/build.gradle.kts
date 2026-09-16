plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
}

/**
 * La version de iOS.
 *
 * Es Compose Multiplatform: el mismo Kotlin y el mismo Compose que la app
 * Android, no una reescritura en Swift. Lo que Xcode consume de aca es un
 * framework; el proyecto de Xcode en si todavia no existe — generarlo requiere
 * un Mac, y este entorno es Linux.
 *
 * NADA de este modulo compila en Linux: Kotlin/Native no cruza a iOS sin macOS
 * y Xcode. La verificacion vive en el workflow `verificar-ios.yml`, que corre en
 * un runner macOS. Es la unica prueba de que esto compila, y por eso existe.
 *
 * :app no depende de este modulo ni al reves. La app que esta por entrar a Play
 * no ve Compose Multiplatform ni maplibre-compose por ningun lado.
 */
kotlin {
    // Sin iosX64: maplibre-compose 0.17.0 publica iosArm64, iosSimulatorArm64,
    // macosArm64, android, jvm y js — el simulador Intel no esta. Verificado en
    // su Gradle module metadata, no supuesto.
    listOf(iosArm64(), iosSimulatorArm64()).forEach { objetivo ->
        objetivo.binaries.framework {
            baseName = "Turismo"
            // Estatico: un framework dinamico obliga a firmarlo y embeberlo
            // aparte, y no gana nada para un solo consumidor.
            isStatic = true
        }
    }

    sourceSets {
        // El accesor `iosMain`, no `by getting`: la jerarquia por omision arma
        // ese source set de forma diferida —despues de este bloque—, asi que
        // buscarlo por nombre aca falla. Y nunca `by creating`: un source set
        // creado a mano no cuelga de la jerarquia, el codigo quedaria fuera de
        // la compilacion y el framework se armaria vacio sin que nada se queje.
        iosMain.dependencies {
            implementation(project(":compartido"))

            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.ui)

            implementation(libs.maplibre.compose)
        }
    }
}
