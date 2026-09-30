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
 * framework, y el proyecto de Xcode se genera de `ios/xcode/project.yml`.
 *
 * Este modulo es **solo la cascara**: un archivo con `puntoDeEntrada()`. Las
 * pantallas, el sistema visual y los datos viven en :interfaz y son los mismos
 * que usa :app. Si esto crece, es que algo se esta escribiendo dos veces.
 *
 * NADA de este modulo compila en Linux: Kotlin/Native no cruza a iOS sin macOS
 * y Xcode. La verificacion vive en el workflow `verificar-ios.yml`, que corre en
 * un runner macOS. Es la unica prueba de que esto compila, y por eso existe.
 *
 * :app no depende de este modulo ni al reves. Las dos cascaras cuelgan de
 * :interfaz y no se conocen entre si.
 */
kotlin {
    // Sin iosX64: maplibre-compose 0.17.0 publica iosArm64, iosSimulatorArm64,
    // macosArm64, android, jvm y js — el simulador Intel no esta. Verificado en
    // su Gradle module metadata, no supuesto.
    listOf(iosArm64(), iosSimulatorArm64()).forEach { objetivo ->
        objetivo.binaries.framework {
            // TurismoKit y no Turismo: el modulo de la app en Xcode ya se
            // llama Turismo, y Swift no puede importar un modulo con el mismo
            // nombre que el que se esta compilando. Ademas separa bien las dos
            // cosas: esto es la biblioteca, aquello es la app.
            baseName = "TurismoKit"
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
            // Una sola dependencia, y es todo lo que esta cascara necesita:
            // :interfaz trae consigo :compartido, Compose y maplibre-compose, y
            // expone `Aplicacion()`, que es exactamente la misma funcion que
            // dibuja la app de Android.
            implementation(project(":interfaz"))
        }
    }
}
