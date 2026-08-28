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
        versionCode = 7
        versionName = "0.8.0"

        // Solo los idiomas del proyecto: cada locale extra pesa en el APK.
        resourceConfigurations += setOf("es", "en", "gn")

        // x86 solo sirve para emuladores. Cada ABI de mas son 12 MB de MapLibre.
        ndk { abiFilters += setOf("arm64-v8a", "armeabi-v7a") }

        // La URL base nunca va quemada en el codigo. Mientras la API no este
        // publicada, la app arranca contra los mocks que viajan en los assets.
        buildConfigField("String", "URL_BASE", "\"https://caaguazu.net/wp-json/czu-app/v1/\"")
        buildConfigField("boolean", "USAR_MOCKS", "true")
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
            // Firma de depuracion por ahora: el keystore propio esta pendiente de decision.
            signingConfig = signingConfigs.getByName("debug")
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
    implementation(libs.coil.compose)
    implementation(libs.coil.network)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
