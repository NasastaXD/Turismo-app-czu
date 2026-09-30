plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

/**
 * La interfaz, una sola vez para las dos plataformas.
 *
 * Aca vive el sistema visual entero —`Tono`, `Radio`, `Elevacion`, `Medida`,
 * `Letra`, los iconos— y las pantallas. Que esto fuera posible no es suerte:
 * el proyecto decidio hace tiempo **no usar Material3** y dibujar su propio
 * sistema sobre `compose.foundation`, y justamente por eso las pantallas no
 * estaban atadas a Android. Al mudarlas, la mayoria no cambio ni una linea.
 *
 * Lo que si necesita `expect`/`actual` es lo que toca el sistema operativo, y
 * es poco y esta junto: el mapa, las preferencias, el idioma del telefono,
 * abrir otra app, y si las animaciones estan apagadas. Cada uno vive en su
 * archivo `.android.kt` / `.ios.kt` al lado del `expect`.
 *
 * :app queda con lo que de verdad es de Android: la Activity, la Application,
 * el registro a Logcat y los avisos con WorkManager.
 */
kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    // Los mismos dos objetivos que :compartido, y por la misma razon: el
    // simulador Intel no existe para estas dependencias. Compilar cualquiera
    // de los dos exige macOS.
    iosArm64()
    iosSimulatorArm64()


    // `expect object` sigue marcado como Beta y avisa una vez por archivo. Es
    // la herramienta que el lenguaje ofrece para esto y el proyecto la usa a
    // conciencia —`Preferencias`, `Sistema`, `Empaquetado`, `Archivos`—, asi
    // que el aviso solo tapa los avisos que si importan.
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":compartido"))

            api(compose.runtime)
            api(compose.foundation)
            api(compose.ui)

            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)

            // Imagenes remotas. Coil 3 es multiplataforma, asi que la ficha, el
            // mosaico y el articulo cargan sus fotos igual en los dos lados.
            implementation(libs.coil.compose)
        }

        androidMain.dependencies {
            // El mapa de Android: el SDK nativo de MapLibre dentro de un
            // AndroidView, que es lo que :app ya usaba.
            implementation(libs.maplibre)
            implementation(libs.coil.network)

            // El gesto de volver y el pedido de permiso son de la Activity, y
            // el ciclo de vida del MapView hay que atarlo al de la pantalla.
            // Las tres cosas viven aca, en el unico archivo de cada una que es
            // de Android.
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.lifecycle.runtime.compose)
        }

        // Los guardianes del proyecto viven aca: revisan que ninguna pantalla
        // escriba texto visible, que ningun radio ni elevacion salga fuera de
        // los tokens, y que las claves de texto que se piden existan. Corren
        // en la JVM porque lo que hacen es leer archivos de codigo.
        androidUnitTest.dependencies {
            implementation(libs.junit)
            implementation(libs.kotlinx.coroutines.test)
        }

        iosMain.dependencies {
            // El mapa de iOS: el envoltorio de MapLibre para Compose
            // Multiplatform. Dibuja el mismo estilo y el mismo .pmtiles.
            implementation(libs.maplibre.compose)
            // El nucleo de red de Coil sin motor: el motor es nuestro, sobre la
            // misma NSURLSession que ya usa `Http`. Ver `RedDeImagenes.ios.kt`.
            implementation(libs.coil.network.core)
        }
    }
}

android {
    namespace = "net.caaguazu.turismo.interfaz"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
