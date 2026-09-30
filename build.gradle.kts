// Todos los plugins se declaran aca con `apply false` y cada modulo aplica el
// que le toca. Kotlin Android y Kotlin Multiplatform comparten classpath: si
// un modulo pidiera la version por su cuenta, Gradle no puede comprobar la
// compatibilidad y falla ("already on the classpath with an unknown version").
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.compose.multiplatform) apply false
}
