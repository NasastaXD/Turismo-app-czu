plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
}

/**
 * Lo que las dos plataformas usan igual.
 *
 * Hoy es el contrato con el panel: los modelos y el analizador. Es Kotlin puro
 * —ni interfaz, ni Compose, ni nada de Android— y eso es a proposito: asi :app
 * lo consume sin sumar una sola dependencia nueva, y el lado de iOS lo consume
 * sin arrastrar Compose de androidx.
 *
 * Lo que todavia NO esta aca, y es el trabajo que sigue: Http (usa
 * HttpURLConnection, que no existe en iOS), Registro, Ajustes, Cache y Guardado
 * (archivos y preferencias del sistema). Cada uno necesita expect/actual.
 */
kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    // Telefono real y simulador en Mac con Apple Silicon. El simulador Intel
    // (iosX64) queda afuera a proposito: ni androidx.compose.runtime ni
    // maplibre-compose publican esa variante, asi que declararlo daria una
    // dependencia irresoluble. Compilar cualquiera de los dos exige macOS —
    // Kotlin/Native no cruza a iOS desde Linux.
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.serialization.json)
            // Unicamente por @Immutable en los modelos. Es el mismo artefacto y
            // la misma version que :app ya tiene via foundation/ui, asi que del
            // lado Android no cambia nada del grafo de dependencias.
            api(libs.compose.runtime)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

android {
    namespace = "net.caaguazu.turismo.compartido"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
