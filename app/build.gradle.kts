plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "net.caaguazu.turismo"
    compileSdk = 36

    defaultConfig {
        applicationId = "net.caaguazu.turismo"
        minSdk = 24
        targetSdk = 36
        versionCode = 16
        versionName = "1.5.0"

        // Solo los idiomas del proyecto: cada locale extra pesa en el APK.
        // Son los mismos tres que ofrece el selector. El guarani sale de aca
        // hasta que haya textos: tenerlo listado sin traducciones no agregaba
        // nada, y dejaba afuera al portugues, que si las tiene.
        resourceConfigurations += setOf("es", "en", "pt")

        // x86 solo sirve para emuladores. Cada ABI de mas son 12 MB de MapLibre.
        ndk { abiFilters += setOf("arm64-v8a", "armeabi-v7a") }

        // La URL base nunca va quemada en el codigo, aunque hoy solo exista una.
        buildConfigField("String", "URL_BASE", "\"https://caaguazu.net/wp-json/czu-app/v1/\"")
    }

    // La clave de release vive fuera del repo. CI la escribe a disco a partir de
    // un secreto y pasa la ruta y las contraseñas por variable de entorno; un
    // build local sin esas variables simplemente no arma este signingConfig.
    val rutaKeystoreRelease = System.getenv("RELEASE_KEYSTORE_PATH")
    val hayKeystoreRelease = rutaKeystoreRelease != null && file(rutaKeystoreRelease).exists()

    signingConfigs {
        if (hayKeystoreRelease) {
            create("release") {
                storeFile = file(rutaKeystoreRelease!!)
                storePassword = System.getenv("RELEASE_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("RELEASE_KEY_ALIAS")
                keyPassword = System.getenv("RELEASE_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        debug {
            // Sin minificar: las trazas tienen que leerse durante el desarrollo.
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            // Con la clave propia disponible, cada release queda firmada igual
            // que la anterior y una actualizacion se instala encima. Sin ella
            // (un build local, o CI antes de que exista el secreto) cae a la de
            // depuracion, que es la que habia antes de esto.
            signingConfig = if (hayKeystoreRelease) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
        }
    }

    // Un APK por arquitectura mas uno universal. MapLibre lleva 12 MB de codigo
    // nativo por ABI; un universal con las cuatro arquitecturas seria absurdo.
    splits {
        abi {
            isEnable = true
            reset()
            include("arm64-v8a", "armeabi-v7a")
            isUniversalApk = true
        }
    }

    androidResources {
        // PMTiles necesita lectura por posicion: no debe comprimirse dentro del APK.
        noCompress += "pmtiles"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources.excludes += setOf(
            "/META-INF/{AL2.0,LGPL2.1}",
            "META-INF/*.version",
            "kotlin/**",
            "DebugProbesKt.bin",
        )
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Sin Material3: el diseno es propio de punta a punta y su tema no se usa.
    implementation(libs.compose.foundation)
    implementation(libs.compose.ui)
    debugImplementation(libs.compose.ui.tooling)
    implementation(libs.compose.ui.tooling.preview)

    implementation(libs.maplibre)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)

    // Para los avisos. Es la unica forma de que Android deje correr una revision
    // periodica sobreviviendo a Doze y al reinicio del telefono, y no arrastra
    // ningun servicio externo: sin ella, la alternativa seria Firebase.
    implementation(libs.androidx.work)
    implementation(libs.coil.compose)
    implementation(libs.coil.network)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
