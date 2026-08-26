# MapLibre usa JNI: sus clases nativas no pueden renombrarse.
-keep class org.maplibre.android.** { *; }
-keep class com.mapbox.** { *; }
-dontwarn org.maplibre.**

# kotlinx.serialization genera serializadores por companion.
-keepclassmembers class ** {
    *** Companion;
}
-keepclasseswithmembers class ** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Los numeros de linea son lo unico que hace legible una traza ofuscada.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SRC
